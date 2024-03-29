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
package com.digitalbuana.smiles.data.extension.vcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.database.Cursor;

import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.OnLoadListener;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.OnAccountRemovedListener;
import com.digitalbuana.smiles.data.connection.ConnectionItem;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.connection.OnDisconnectListener;
import com.digitalbuana.smiles.data.connection.OnPacketListener;
import com.digitalbuana.smiles.data.extension.avatar.AvatarManager;
import com.digitalbuana.smiles.data.roster.OnRosterChangedListener;
import com.digitalbuana.smiles.data.roster.OnRosterReceivedListener;
import com.digitalbuana.smiles.data.roster.RosterContact;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.data.roster.StructuredName;
import com.digitalbuana.smiles.xmpp.address.Jid;
import com.digitalbuana.smiles.xmpp.vcard.VCard;

public class VCardManager implements OnLoadListener, OnPacketListener,
		OnDisconnectListener, OnRosterReceivedListener,
		OnAccountRemovedListener {

	private static final StructuredName EMPTY_STRUCTURED_NAME = new StructuredName(
			null, null, null, null, null);

	private final Collection<VCardRequest> requests;
	private final Set<String> invalidHashes;
	private final Map<String, StructuredName> names;
	private final ArrayList<String> accountRequested;
	private final static VCardManager instance;

	static {
		instance = new VCardManager();
		Application.getInstance().addManager(instance);
	}

	public static VCardManager getInstance() {
		return instance;
	}

	private VCardManager() {
		requests = new ArrayList<VCardRequest>();
		invalidHashes = new HashSet<String>();
		names = new HashMap<String, StructuredName>();
		accountRequested = new ArrayList<String>();
	}

	@Override
	public void onLoad() {
			final Map<String, StructuredName> names = new HashMap<String, StructuredName>();
			Cursor cursor = VCardTable.getInstance().list();
			try {
				if (cursor.moveToFirst()) {
					do {
						names.put(
								VCardTable.getUser(cursor),
								new StructuredName(VCardTable.getNickName(cursor),
										VCardTable.getFormattedName(cursor),
										VCardTable.getFirstName(cursor), VCardTable
												.getMiddleName(cursor), VCardTable
												.getLastName(cursor)));
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
			Application.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onLoaded(names);
				}
			});
	}

	private void onLoaded(Map<String, StructuredName> names) {
		this.names.putAll(names);
	}

	@Override
	public void onRosterReceived(AccountItem accountItem) {
		String account = accountItem.getAccount();
		if (!accountRequested.contains(account)&& SettingsManager.connectionLoadVCard()) {
			String bareAddress = Jid.getBareAddress(accountItem.getRealJid());
			if (bareAddress != null) {
				request(account, bareAddress, null);
				accountRequested.add(account);
			}
		}

		// Request vCards for new contacts.
		for (RosterContact contact : RosterManager.getInstance().getContacts()){
			if (account.equals(contact.getUser()) && !names.containsKey(contact.getUser()))
				request(account, contact.getUser(), null);
		}

	}

	@Override
	public void onDisconnect(ConnectionItem connection) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		Iterator<VCardRequest> iterator = requests.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getAccount().equals(account))
				iterator.remove();
		}
	}

	@Override
	public void onAccountRemoved(AccountItem accountItem) {
		accountRequested.remove(accountItem.getAccount());
	}

	/**
	 * Requests vCard.
	 * 
	 * @param account
	 * @param bareAddress
	 * @param hash
	 *            avatar's hash that was intent to request vCard. Can be
	 *            <code>null</code>.
	 */
	public void request(String account, String bareAddress, String hash) {
//		Log.i("dheinaku", "request");
		if (hash != null && invalidHashes.contains(hash))
			return;
		// User can change avatar before first request will be completed.
		for (VCardRequest check : requests)
			if (check.getUser().equals(bareAddress)) {
				if (hash != null)
					check.addHash(hash);
				return;
			}
		VCard packet = new VCard();
		packet.setTo(bareAddress);
		packet.setType(Type.GET);
		VCardRequest request = new VCardRequest(account, bareAddress,packet.getPacketID());
		requests.add(request);
		if (hash != null)
			request.addHash(hash);
		try {
			ConnectionManager.getInstance().sendPacket(account, packet);
		} catch (NetworkException e) {
			requests.remove(request);
			onVCardFailed(account, bareAddress);
		}
	}

	public String getName(String bareAddress) {
		StructuredName name = names.get(bareAddress);
		if (name == null)
			return "";
		return name.getBestName();
	}

	/**
	 * Get uses's name information.
	 * 
	 * @param bareAddress
	 * @return <code>null</code> if there is no info.
	 */
	public StructuredName getStructucedName(String bareAddress) {
		return names.get(bareAddress);
	}

	private void onVCardReceived(final String account,final String bareAddress, final VCard vCard) {
//		Log.e(AppConstants.TAG, "onVCardReceived");
		for (OnVCardListener listener : Application.getInstance().getUIListeners(OnVCardListener.class))
			listener.onVCardReceived(account, bareAddress, vCard);
	}

	private void onVCardFailed(final String account, final String bareAddress) {
//		Log.i("dheinaku", "onVCardFailed");
		for (OnVCardListener listener : Application.getInstance().getUIListeners(OnVCardListener.class))
			listener.onVCardFailed(account, bareAddress);
	}

	@Override
	public void onPacket(ConnectionItem connection, final String bareAddress, Packet packet) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		if (packet instanceof Presence && ((Presence) packet).getType() != Presence.Type.error) {
//			Log.i(AppConstants.TAG, "Presence");
			if (bareAddress == null)
				return;
			// Request vCard for new users
			if (!names.containsKey(bareAddress)){
				request(account, bareAddress, null);
			}
		} else if (packet instanceof IQ) {
			IQ iq = (IQ) packet;
			if (iq.getType() != Type.ERROR && !(packet instanceof VCard))
				return;
			String packetId = iq.getPacketID();
			VCardRequest request = null;
			Iterator<VCardRequest> iterator = requests.iterator();
			while (iterator.hasNext()) {
				VCardRequest check = iterator.next();
				if (check.getPacketId().equals(packetId)) {
					request = check;
					iterator.remove();
					break;
				}
			}
			if (request == null || !request.getUser().equals(bareAddress))
				return;
			final StructuredName name;
//			Log.i(AppConstants.TAG, "-------------------TEST-------------------");
			if (iq.getType() == Type.ERROR) {
				onVCardFailed(account, bareAddress);
				invalidHashes.addAll(request.getHashes());
				if (names.containsKey(bareAddress)){
					return;
				}
				name = EMPTY_STRUCTURED_NAME;
			} else if (packet instanceof VCard) {
				VCard vCard = (VCard) packet;
				onVCardReceived(account, bareAddress, vCard);
				String hash = vCard.getAvatarHash();
				for (String check : request.getHashes())
					if (!check.equals(hash))
						invalidHashes.add(check);
				AvatarManager.getInstance().onAvatarReceived( bareAddress, hash, vCard.getAvatar());
				name = new StructuredName(
						vCard.getNickName(),
						vCard.getFormattedName(),
						vCard.getFirstName(),
						vCard.getMiddleName(),
						vCard.getLastName());
			} else throw new IllegalStateException();
			names.put(bareAddress, name);
			for (RosterContact rosterContact : RosterManager.getInstance().getContacts())
				if (rosterContact.getUser().equals(bareAddress))
					for (OnRosterChangedListener listener : Application.getInstance().getManagers(OnRosterChangedListener.class))
						listener.onContactStructuredInfoChanged(rosterContact, name);
			Application.getInstance().runInBackground(new Runnable() {
				@Override
				public void run() {
					VCardTable.getInstance().write(bareAddress, name);
				}
			});
			if (iq.getFrom() == null) { // account it self
				AccountManager.getInstance().onAccountChanged(account);
			} else {
				RosterManager.getInstance().onContactChanged(account, bareAddress);
			}
		}
	}

}
