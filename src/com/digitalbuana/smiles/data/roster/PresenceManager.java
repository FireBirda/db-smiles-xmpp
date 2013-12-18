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
package com.digitalbuana.smiles.data.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.util.StringUtils;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.OnLoadListener;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.OnAccountDisabledListener;
import com.digitalbuana.smiles.data.account.StatusMode;
import com.digitalbuana.smiles.data.connection.ConnectionItem;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.connection.OnDisconnectListener;
import com.digitalbuana.smiles.data.connection.OnPacketListener;
import com.digitalbuana.smiles.data.entity.NestedMap;
import com.digitalbuana.smiles.data.extension.archive.OnArchiveModificationsReceivedListener;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.notification.EntityNotificationProvider;
import com.digitalbuana.smiles.data.notification.NotificationManager;
import com.digitalbuana.smiles.xmpp.address.Jid;

/**
 * Process contact's presence information.
 * 
 * @author alexander.ivanov
 * 
 */
public class PresenceManager implements OnArchiveModificationsReceivedListener,
		OnPacketListener, OnLoadListener, OnAccountDisabledListener,
		OnDisconnectListener {

	private final EntityNotificationProvider<SubscriptionRequest> subscriptionRequestProvider;

	private final HashMap<String, HashSet<String>> requestedSubscriptions;

	private final NestedMap<ResourceContainer> presenceContainers;
	private final ArrayList<String> readyAccounts;

	private final static PresenceManager instance;

	private final NestedMap<String> vurboseContainers;

	private String TAG = getClass().getSimpleName();

	static {
		instance = new PresenceManager();
		Application.getInstance().addManager(instance);
	}

	public static PresenceManager getInstance() {
		return instance;
	}

	private PresenceManager() {
		subscriptionRequestProvider = new EntityNotificationProvider<SubscriptionRequest>(
				R.drawable.ic_stat_subscribe);
		requestedSubscriptions = new HashMap<String, HashSet<String>>();
		presenceContainers = new NestedMap<ResourceContainer>();
		vurboseContainers = new NestedMap<String>();
		readyAccounts = new ArrayList<String>();
	}

	@Override
	public void onLoad() {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onLoaded();
			}
		});
	}

	private void onLoaded() {
		NotificationManager.getInstance().registerNotificationProvider(
				subscriptionRequestProvider);
	}

	public SubscriptionRequest getSubscriptionRequest(String account,
			String user) {
		return subscriptionRequestProvider.get(account, user);
	}

	public void requestSubscription(String account, String bareAddress)
			throws NetworkException {
		Presence packet = new Presence(Presence.Type.subscribe);
		packet.setTo(bareAddress);
		ConnectionManager.getInstance().sendPacket(account, packet);
		HashSet<String> set = requestedSubscriptions.get(account);
		if (set == null) {
			set = new HashSet<String>();
			requestedSubscriptions.put(account, set);
		}
		set.add(bareAddress);

		boolean isFound = false;
		for (int i = 0; i < FriendsManager.getInstance()
				.getFriendsBlockedManager().getAllFriends().size(); i++) {
			if (FriendsManager
					.getInstance()
					.getFriendsBlockedManager()
					.getAllFriends()
					.get(i)
					.getName()
					.equals(com.digitalbuana.smiles.utils.StringUtils
							.replaceStringEquals(bareAddress))) {
				isFound = true;
			}
		}
		if (!isFound) {
			FriendsManager.getInstance().getFriendsPenddingHeConfirmManager()
					.addFriendsByJID(bareAddress);
		}

	}

	private void removeRequestedSubscription(String account, String bareAddress) {
		HashSet<String> set = requestedSubscriptions.get(account);
		if (set != null)
			set.remove(bareAddress);
	}

	public void acceptSubscription(String account, String bareAddress)
			throws NetworkException {
		Presence packet = new Presence(Presence.Type.subscribed);
		packet.setTo(bareAddress);
		ConnectionManager.getInstance().sendPacket(account, packet);
		subscriptionRequestProvider.remove(account, bareAddress);
		removeRequestedSubscription(account, bareAddress);
		FriendsManager.getInstance().getFriendsPenddingHeConfirmManager()
				.removeFriendsJID(bareAddress);
		FriendsManager.getInstance().getFriendsWaitingMeApproveManager()
				.removeFriendsJID(bareAddress);
		FriendsManager.getInstance().getFriendsListManager()
				.addFriendsByJID(bareAddress);
	}

	public void discardSubscription(String account, String bareAddress)
			throws NetworkException {
		Presence packet = new Presence(Presence.Type.unsubscribed);
		packet.setTo(bareAddress);
		ConnectionManager.getInstance().sendPacket(account, packet);
		subscriptionRequestProvider.remove(account, bareAddress);
		removeRequestedSubscription(account, bareAddress);
		FriendsManager.getInstance().getFriendsPenddingHeConfirmManager()
				.removeFriendsJID(bareAddress);
		FriendsManager.getInstance().getFriendsWaitingMeApproveManager()
				.removeFriendsJID(bareAddress);
	}

	public boolean hasSubscriptionRequest(String account, String bareAddress) {
		return getSubscriptionRequest(account, bareAddress) != null;
	}

	public ResourceItem getResourceItem(String account, String bareAddress) {
		ResourceContainer resourceContainer = presenceContainers.get(account,
				bareAddress);
		if (resourceContainer == null)
			return null;
		return resourceContainer.getBest();
	}

	public Collection<ResourceItem> getResourceItems(String account,
			String bareAddress) {
		ResourceContainer container = presenceContainers.get(account,
				bareAddress);
		if (container == null)
			return Collections.emptyList();
		return container.getResourceItems();
	}

	public StatusMode getStatusMode(String account, String bareAddress) {
		ResourceItem resourceItem = getResourceItem(account, bareAddress);
		if (resourceItem == null)
			return StatusMode.unavailable;
		return resourceItem.getStatusMode();
	}

	public String getVurbose(String account, String bareAddress) {
		String resourceContainer = vurboseContainers.get(account, bareAddress);
		if (resourceContainer == null)
			return "";
		return resourceContainer;
	}

	public String getStatusText(String account, String bareAddress) {
		ResourceItem resourceItem = getResourceItem(account, bareAddress);
		if (resourceItem == null)
			return "";
		return resourceItem.getStatusText();
	}

	@Override
	public void onPacket(ConnectionItem connection, String bareAddress,
			Packet packet) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		if (packet instanceof Presence) {
			if (bareAddress == null)
				return;
			final Presence presence = (Presence) packet;
			if (presence.getType() == Presence.Type.subscribe) {

				// Subscription request
				HashSet<String> set = requestedSubscriptions.get(account);
				if (set != null && set.contains(bareAddress)) {
					try {
						acceptSubscription(account, bareAddress);

					} catch (NetworkException e) {
					}
					subscriptionRequestProvider.remove(account, bareAddress);
				} else {
					FriendsManager.getInstance()
							.getFriendsWaitingMeApproveManager()
							.addFriendsByJID(bareAddress);
					FriendsManager.getInstance()
							.getFriendsPenddingHeConfirmManager()
							.removeFriendsJID(bareAddress);
					subscriptionRequestProvider.add(new SubscriptionRequest(
							account, bareAddress), null);
				}
				return;
			}

			String verbose = StringUtils.parseResource(presence.getFrom());

			// if (presence.isAvailable()) {
			// String getUser = presence.getFrom().replace(
			// "/" + AppConstants.XMPPConfResource, "");
			// tryToResend(getUser);
			// }

			String resource = Jid.getResource(presence.getFrom());
			ResourceContainer resourceContainer = presenceContainers.get(
					account, bareAddress);
			ResourceItem resourceItem;
			if (resourceContainer == null) {
				resourceItem = null;
			} else {
				resourceItem = resourceContainer.get(resource);
			}

			StatusMode previousStatusMode = getStatusMode(account, bareAddress);
			String previousStatusText = getStatusText(account, bareAddress);
			if (presence.getType() == Type.available) {
				StatusMode statusMode = StatusMode.createStatusMode(presence);
				String statusText = presence.getStatus();
				int priority = presence.getPriority();
				if (statusText == null)
					statusText = "";
				if (priority == Integer.MIN_VALUE)
					priority = 0;
				if (resourceItem == null) {
					if (resourceContainer == null) {
						resourceContainer = new ResourceContainer();
						presenceContainers.put(account, bareAddress,
								resourceContainer);
						vurboseContainers.put(account, bareAddress,
								presence.getFrom());
					}
					resourceContainer.put(resource, new ResourceItem(verbose,
							statusMode, statusText, priority));
					resourceContainer.updateBest();
				} else {
					resourceItem.setVerbose(verbose);
					resourceItem.setStatusMode(statusMode);
					resourceItem.setStatusText(statusText);
					resourceItem.setPriority(priority);
					resourceContainer.updateBest();
				}
			} else if (presence.getType() == Presence.Type.error
					|| presence.getType() == Type.unavailable) {
				if (presence.getType() == Presence.Type.error
						&& "".equals(resource) && resourceContainer != null) {
					presenceContainers.remove(account, bareAddress);
					vurboseContainers.remove(account, bareAddress);
				} else if (resourceItem != null) {
					resourceContainer.remove(resource);
					resourceContainer.updateBest();
				}
			}

			// Notify about changes
			StatusMode newStatusMode = getStatusMode(account, bareAddress);
			String newStatusText = getStatusText(account, bareAddress);
			if (previousStatusMode != newStatusMode
					|| !previousStatusText.equals(newStatusText))
				for (OnStatusChangeListener listener : Application
						.getInstance()
						.getManagers(OnStatusChangeListener.class))
					if (previousStatusMode == newStatusMode)
						listener.onStatusChanged(account, bareAddress,
								resource, newStatusText);
					else
						listener.onStatusChanged(account, bareAddress,
								resource, newStatusMode, newStatusText);

			RosterContact rosterContact = RosterManager.getInstance()
					.getRosterContact(account, bareAddress);
			if (rosterContact != null) {
				ArrayList<RosterContact> rosterContacts = new ArrayList<RosterContact>();
				rosterContacts.add(rosterContact);
				for (OnRosterChangedListener listener : Application
						.getInstance().getManagers(
								OnRosterChangedListener.class))
					listener.onPresenceChanged(rosterContacts);
			}

			RosterManager.getInstance().onContactChanged(account, bareAddress);
		} else if (packet instanceof RosterPacket
				&& ((RosterPacket) packet).getType() != IQ.Type.ERROR) {
			RosterPacket rosterPacket = (RosterPacket) packet;
			for (RosterPacket.Item item : rosterPacket.getRosterItems()) {
				if (item.getItemType() == ItemType.both
						|| item.getItemType() == ItemType.from) {
					String user = Jid.getBareAddress(item.getUser());
					if (user == null)
						continue;
					subscriptionRequestProvider.remove(account, user);
				}
			}
		}
	}

	/*
	 * private void tryToResend(final String getUser) {
	 * Application.getInstance().runInBackground(new Runnable() {
	 * 
	 * @Override public void run() { // TODO Auto-generated method stub
	 * 
	 * Cursor unSendList = MessageTable.getInstance().unSent(getUser);
	 * 
	 * Log.v(TAG, "presence unsent from " + getUser + " : " +
	 * unSendList.getCount());
	 * 
	 * try {
	 * 
	 * if (unSendList.moveToFirst()) {
	 * 
	 * do {
	 * 
	 * boolean isDeliverered = MessageTable .isDelivered(unSendList); boolean
	 * isIncomming = MessageTable .isIncoming(unSendList); boolean
	 * isReadByFriend = MessageTable .getReadByFriend(unSendList); boolean
	 * isError = MessageTable.hasError(unSendList); boolean isSent =
	 * MessageTable.isSent(unSendList); Date dateIs =
	 * MessageTable.getTimeStamp(unSendList); ChatAction action = MessageTable
	 * .getAction(unSendList); Date delayTimeStamp = MessageTable
	 * .getDelayTimeStamp(unSendList);
	 * 
	 * String resource = MessageTable.getUser(unSendList);
	 * 
	 * String defaultUserSender = AccountManager .getInstance().getAccountKu();
	 * 
	 * boolean isOnline = PresenceManager.getInstance()
	 * .getStatusMode(defaultUserSender, resource) .isOnline();
	 * 
	 * if (isOnline && resource != null && !resource.equals("") && isSent &&
	 * !isError && !isReadByFriend && !isIncomming && !isDeliverered &&
	 * !resource .contains(AppConstants.XMPPGroupsServer) && !resource
	 * .contains(AppConstants.XMPPRoomsServer)) {
	 * 
	 * 
	 * String packetId = MessageTable .getPacketId(unSendList);
	 * 
	 * MessageTable.getInstance().markAsUnSent( packetId);
	 * 
	 * 
	 * if (defaultUserSender != null) {
	 * 
	 * String textIs = MessageTable .getText(unSendList);
	 * 
	 * String packetId = MessageTable .getPacketId(unSendList);
	 * 
	 * Message message = new Message(); message.setTo(resource);
	 * message.setType(Message.Type.chat); message.setBody(textIs);
	 * message.setThread(packetId); message.setPacketID(packetId);
	 * 
	 * Log.d(TAG, "retry : " + packetId);
	 * 
	 * AbstractChat chat = new RegularChat( defaultUserSender, Jid
	 * .getBareAddress(resource));
	 * 
	 * MessageItem messageItem = new MessageItem( chat, "", resource, textIs,
	 * action, dateIs, delayTimeStamp, isIncomming, true, isSent, isError,
	 * isDeliverered, false, false, packetId, isReadByFriend);
	 * 
	 * ChatStateManager.getInstance() .updateOutgoingMessage(chat, message);
	 * 
	 * ReceiptManager.getInstance() .updateOutgoingMessage(chat, message,
	 * messageItem);
	 * 
	 * try { ConnectionManager.getInstance() .sendPacket(defaultUserSender,
	 * message); } catch (NetworkException e1) { }
	 * 
	 * try { new Thread().sleep(500); } catch (InterruptedException e) {
	 * Log.e(TAG, e.getMessage()); }
	 * 
	 * }
	 * 
	 * } } while (unSendList.moveToNext()); } } finally { unSendList.close(); }
	 * } }); }
	 */

	@Override
	public void onArchiveModificationsReceived(ConnectionItem connection) {
		if (!(connection instanceof AccountItem))
			return;
		// Send presence information only when server side archive modifications
		// received.
		String account = ((AccountItem) connection).getAccount();
		readyAccounts.add(account);
		Collection<String> previous = new HashSet<String>();
		for (NestedMap.Entry<ResourceContainer> entry : presenceContainers)
			previous.add(entry.getSecond());
		presenceContainers.clear(account);

		ArrayList<RosterContact> rosterContacts = new ArrayList<RosterContact>();
		for (String bareAddress : previous) {
			RosterContact rosterContact = RosterManager.getInstance()
					.getRosterContact(account, bareAddress);
			if (rosterContact != null)
				rosterContacts.add(rosterContact);
		}
		for (OnRosterChangedListener listener : Application.getInstance()
				.getManagers(OnRosterChangedListener.class))
			listener.onPresenceChanged(rosterContacts);
		try {
			resendPresence(account);
		} catch (NetworkException e) {
		}
	}

	@Override
	public void onDisconnect(ConnectionItem connection) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		readyAccounts.remove(account);
	}

	@Override
	public void onAccountDisabled(AccountItem accountItem) {
		requestedSubscriptions.remove(accountItem.getAccount());
		presenceContainers.clear(accountItem.getAccount());
		vurboseContainers.clear(accountItem.getAccount());
	}

	public void resendPresence(String account) throws NetworkException {
		if (!readyAccounts.contains(account))
			throw new NetworkException(R.string.NOT_CONNECTED);
		ConnectionManager.getInstance().sendPacket(account,
				AccountManager.getInstance().getAccount(account).getPresence());
	}

}
