package com.digitalbuana.smiles.data.message;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

import com.digitalbuana.smiles.awan.helper.MessageHelper;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.LogManager;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.connection.ConnectionItem;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.connection.OnDisconnectListener;
import com.digitalbuana.smiles.data.connection.OnPacketListener;
import com.digitalbuana.smiles.data.entity.NestedMap;
import com.digitalbuana.smiles.data.extension.muc.RoomChat;
import com.digitalbuana.smiles.utils.StringUtils;
import com.digitalbuana.smiles.xmpp.receipt.Received;
import com.digitalbuana.smiles.xmpp.receipt.Request;

public class ReceiptManager implements OnPacketListener, OnDisconnectListener {

	private final NestedMap<MessageItem> sent;
	private final static ReceiptManager instance;

	static {
		instance = new ReceiptManager();
		Application.getInstance().addManager(instance);

		Connection
				.addConnectionCreationListener(new ConnectionCreationListener() {
					@Override
					public void connectionCreated(final Connection connection) {
						ServiceDiscoveryManager.getInstanceFor(connection)
								.addFeature("urn:xmpp:receipts");
					}
				});
	}

	public static ReceiptManager getInstance() {
		return instance;
	}

	private ReceiptManager() {
		sent = new NestedMap<MessageItem>();
	}

	public void updateOutgoingMessage(AbstractChat abstractChat,
			Message message, MessageItem messageItem) {
		sent.put(abstractChat.getAccount(), message.getPacketID(), messageItem);
		if (abstractChat instanceof RoomChat)
			return;
		message.addExtension(new Request());
	}

	private String TAG = getClass().getSimpleName();

	@Override
	public void onPacket(ConnectionItem connection, String bareAddress,
			Packet packet) {

		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		final String user = packet.getFrom();
		if (user == null)
			return;

		if (!(packet instanceof Message))
			return;
		final Message message = (Message) packet;

		if (message.getType() == Message.Type.error) {
			final MessageItem messageItem = sent.remove(account,
					message.getPacketID());
			if (messageItem != null && !messageItem.isError()) {
				messageItem.markAsError();
				Application.getInstance().runInBackground(new Runnable() {
					@Override
					public void run() {
						if (messageItem.getId() != null)
							MessageTable.getInstance().markAsError(
									messageItem.getId());
					}
				});
				MessageManager.getInstance().onChatChanged(
						messageItem.getChat().getAccount(),
						messageItem.getChat().getUser(), false);
			}
		} else {

			for (PacketExtension packetExtension : message.getExtensions()) {

				// terima is received
				if (packetExtension instanceof Received) {

					Received received = (Received) packetExtension;
					String id = received.getId();

					if (id == null)
						id = message.getPacketID();
					if (id == null)
						continue;

					final MessageItem messageItem = sent.remove(account, id);
					try {
						messageItem.setPacketID(id);
					} catch (NullPointerException e) {

					}

					if (messageItem != null && !messageItem.isDelivered()) {

						MessageTable.getInstance().markAsDelivered(
								String.valueOf(id));

						messageItem.markAsDelivered();

						Context cntx = Application.getInstance()
								.getApplicationContext();
						SharedPreferences mSettings = PreferenceManager
								.getDefaultSharedPreferences(cntx);
						String deliverSound = mSettings.getString(
								AppConstants.SOUND_CHAT_SEND_TAG, null);

						if (deliverSound != null
								&& !deliverSound.equals("default")) {
							int resID = cntx.getResources().getIdentifier(
									deliverSound, "raw", cntx.getPackageName());
							MediaPlayer mediaPlayer = MediaPlayer.create(cntx,
									resID);
							mediaPlayer.start();
						}

						MessageManager.getInstance().onChatChanged(
								messageItem.getChat().getAccount(),
								messageItem.getChat().getUser(), false);

					}

				} else if (packetExtension instanceof Request) {

					// kirim received
					String id = message.getPacketID();

					if (id == null)
						continue;

					Message receipt = new Message(user);
					receipt.addExtension(new Received(id));
					receipt.setThread(message.getThread());
					try {

						ConnectionManager.getInstance().sendPacket(account,
								receipt);
						if (id != null && !id.equals(""))
							MessageHelper
									.sendMessageStatus(
											AccountManager.getInstance()
													.getAccountKu(),
											StringUtils
													.replaceStringEquals(user)
													.replace(
															"/"
																	+ AppConstants.XMPPConfResource,
															""), id, "");

					} catch (NetworkException e) {
						LogManager.exception(this, e);
					}
				}
			}

		}
	}

	@Override
	public void onDisconnect(ConnectionItem connection) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		sent.clear(account);
	}

}
