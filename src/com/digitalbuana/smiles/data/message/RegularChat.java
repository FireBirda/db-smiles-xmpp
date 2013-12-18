/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * 
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * 
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.digitalbuana.smiles.data.message;

import java.util.ArrayList;
import java.util.Date;

import net.java.otr4j.OtrException;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.MUCUser;

import android.database.Cursor;

import com.digitalbuana.smiles.data.LogManager;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.SettingsManager.SecurityOtrMode;
import com.digitalbuana.smiles.data.extension.archive.MessageArchiveManager;
import com.digitalbuana.smiles.data.extension.otr.OTRManager;
import com.digitalbuana.smiles.data.extension.otr.OTRUnencryptedException;
import com.digitalbuana.smiles.data.extension.otr.SecurityLevel;
import com.digitalbuana.smiles.xmpp.address.Jid;
import com.digitalbuana.smiles.xmpp.archive.SaveMode;
import com.digitalbuana.smiles.xmpp.delay.Delay;
import com.digitalbuana.smiles.xmpp.muc.MUC;

public class RegularChat extends AbstractChat {

	private String resource;
	private String TAG = getClass().getSimpleName();

	public RegularChat(String account, String user) {
		super(account, user);
		resource = null;
	}

	public String getResource() {
		return resource;
	}

	@Override
	public String getTo() {
		if (resource == null)
			return user;
		else
			return user + "/" + resource;
	}

	@Override
	public Type getType() {
		return Type.chat;
	}

	@Override
	protected boolean canSendMessage() {
		if (super.canSendMessage()) {
			if (SettingsManager.securityOtrMode() != SecurityOtrMode.required)
				return true;
			SecurityLevel securityLevel = OTRManager.getInstance()
					.getSecurityLevel(account, user);
			if (securityLevel != SecurityLevel.plain)
				return true;
			try {
				OTRManager.getInstance().startSession(account, user);
			} catch (NetworkException e) {
			}
		}
		return false;
	}

	@Override
	protected String prepareText(String text) {
		text = super.prepareText(text);
		try {
			return OTRManager.getInstance().transformSending(account, user,
					text);
		} catch (OtrException e) {
			LogManager.exception(this, e);
			return null;
		}
	}

	public static boolean getIsReadByFriend(long id) {
		Cursor cur = MessageTable.getInstance().getIsReadByFriend(id);
		if (cur.moveToFirst())
			return MessageTable.getReadByFriend(cur);
		else
			return false;
	}

	public ArrayList<MessageItem> getMessageList(String account, String user) {
		final ArrayList<MessageItem> messageItems = new ArrayList<MessageItem>();
		Cursor cursor = MessageTable.getInstance().list(account, user);
		try {
			if (cursor.moveToFirst()) {
				do {
					MessageItem messageItem = createMessageItem(cursor);
					messageItems.add(messageItem);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return messageItems;
	}

	private MessageItem createMessageItem(Cursor cursor) {
		MessageItem messageItem = new MessageItem(this,
				MessageTable.getTag(cursor), MessageTable.getResource(cursor),
				MessageTable.getText(cursor), MessageTable.getAction(cursor),
				MessageTable.getTimeStamp(cursor),
				MessageTable.getDelayTimeStamp(cursor),
				MessageTable.isIncoming(cursor), MessageTable.isRead(cursor),
				MessageTable.isSent(cursor), MessageTable.hasError(cursor),
				false, false, false, MessageTable.getPacketId(cursor),
				MessageTable.getReadByFriend(cursor));
		messageItem.setId(MessageTable.getId(cursor));
		return messageItem;
	}

	@Override
	public MessageItem newMessage(String text) {

		/* load langsung dari database ???????? */

		return newMessage(
				null,
				text,
				null,
				null,
				false,
				false,
				false,
				false,
				MessageArchiveManager.getInstance().getSaveMode(account, user,
						getThreadId()) != SaveMode.fls, "", false);
	}

	@Override
	protected boolean onPacket(String bareAddress, Packet packet) {

		if (!super.onPacket(bareAddress, packet))
			return false;

		final String resource = Jid.getResource(packet.getFrom());
		if (packet instanceof Presence) {
			final Presence presence = (Presence) packet;
			if (this.resource != null
					&& presence.getType() == Presence.Type.unavailable
					&& this.resource.equals(resource))
				this.resource = null;
		} else if (packet instanceof Message) {
			final Message message = (Message) packet;
			if (message.getType() == Message.Type.error)
				return true;
			MUCUser mucUser = MUC.getMUCUserExtension(message);
			if (mucUser != null && mucUser.getInvite() != null)
				return true;

			String text = message.getBody();

			/*
			 * packetId ini sudah benar
			 */
			String packetId = message.getPacketID();
			String subject = message.getSubject();

			Date dateToChat = new Date();

			if (subject != null && !subject.equals("null"))
				dateToChat = new Date(Long.parseLong(subject));
			else
				dateToChat = new Date();

			if (text == null)
				return true;
			String thread = message.getThread();
			updateThreadId(thread);
			boolean unencrypted = false;
			try {
				text = OTRManager.getInstance().transformReceiving(account,
						user, text);
			} catch (OtrException e) {
				if (e.getCause() instanceof OTRUnencryptedException) {
					text = ((OTRUnencryptedException) e.getCause()).getText();
					unencrypted = true;
				} else {
					LogManager.exception(this, e);
					return true;
				}
			}
			// System message received.
			if (text == null)
				return true;
			if (!"".equals(resource))
				this.resource = resource;
			newMessage(
					resource,
					text,
					null,
					dateToChat,
					true,
					true,
					unencrypted,
					Delay.isOfflineMessage(Jid.getServer(account), packet),
					MessageArchiveManager.getInstance().getSaveMode(account,
							user, getThreadId()) != SaveMode.fls, packetId,
					false);
		}
		return true;
	}

	@Override
	protected void onComplete() {
		super.onComplete();
		sendMessages();
	}

}
