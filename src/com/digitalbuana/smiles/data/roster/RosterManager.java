package com.digitalbuana.smiles.data.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.LogManager;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.OnAccountDisabledListener;
import com.digitalbuana.smiles.data.account.OnAccountEnabledListener;
import com.digitalbuana.smiles.data.account.OnAccountRemovedListener;
import com.digitalbuana.smiles.data.connection.ConnectionItem;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.connection.OnDisconnectListener;
import com.digitalbuana.smiles.data.connection.OnPacketListener;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.entity.NestedMap;
import com.digitalbuana.smiles.data.extension.archive.OnArchiveModificationsReceivedListener;
import com.digitalbuana.smiles.data.extension.muc.RoomChat;
import com.digitalbuana.smiles.data.extension.muc.RoomContact;
import com.digitalbuana.smiles.data.message.AbstractChat;
import com.digitalbuana.smiles.data.message.ChatContact;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.xmpp.address.Jid;

public class RosterManager implements OnDisconnectListener, OnPacketListener,
		OnAccountEnabledListener, OnAccountDisabledListener,
		OnArchiveModificationsReceivedListener, OnAccountRemovedListener {

	private final NestedMap<RosterGroup> rosterGroups;
	private final NestedMap<RosterContact> rosterContacts;
	private final Set<String> requestedRosters;
	private final Set<String> receivedRosters;

	private final static RosterManager instance;

	static {
		instance = new RosterManager();
		Application.getInstance().addManager(instance);
	}

	public static RosterManager getInstance() {
		return instance;
	}

	private RosterManager() {
		rosterGroups = new NestedMap<RosterGroup>();
		rosterContacts = new NestedMap<RosterContact>();
		receivedRosters = new HashSet<String>();
		requestedRosters = new HashSet<String>();
	}

	public Collection<RosterContact> getContacts() {
		return Collections.unmodifiableCollection(rosterContacts.values());
	}

	public Collection<RosterGroup> getRosterGroups() {
		return Collections.unmodifiableCollection(rosterGroups.values());
	}

	public RosterContact getRosterContact(String account, String user) {
		return rosterContacts.get(account, user);
	}
	public boolean isRosterSubscribe(String account, String user) {
		return rosterContacts.get(account, user).getSubscribed();
	}
	public String getVurbosename(String account, String user) {
		return rosterContacts.get(account, user).getVurboseName();
	}

	public AbstractContact getBestContact(String account, String user) {
		AbstractChat abstractChat = MessageManager.getInstance().getChat(account, user);
		if (abstractChat != null && abstractChat instanceof RoomChat)
			return new RoomContact((RoomChat) abstractChat);
		RosterContact rosterContact = getRosterContact(account, user);
		if (rosterContact != null)
			return rosterContact;
		if (abstractChat != null)
			return new ChatContact(abstractChat);
		return new ChatContact(account, user);
	}
	void addRosterGroup(RosterGroup group) {
		rosterGroups.put(group.getAccount(), group.getName(), group);
	}

	void addRosterContact(RosterContact contact) {
		rosterContacts.put(contact.getAccount(), contact.getUser(), contact);
	}

	private void addContact(RosterContact contact, String name, Map<RosterContact, String> addedContacts) {
		addRosterContact(contact);
		addedContacts.put(contact, name);
	}

	private void removeContact(RosterContact contact,
			Collection<RosterContact> removedContacts) {
		rosterContacts.remove(contact.getAccount(), contact.getUser());
		removedContacts.add(contact);
	}

	private void setName(RosterContact contact, String name,String vurboseName, Map<RosterContact, String> renamedContacts) {
		contact.setName(name);
		contact.setVurboseName(vurboseName);
		renamedContacts.put(contact, name);
	}
	
	private void addGroup(
			RosterContact contact,
			String groupName,
			Collection<RosterGroup> addedGroups,
			Map<RosterContact, Collection<RosterGroupReference>> addedGroupReference) {
		RosterGroup rosterGroup = rosterGroups.get(contact.getAccount(),groupName);
		if (rosterGroup == null) {
			rosterGroup = new RosterGroup(contact.getAccount(), groupName);
			addRosterGroup(rosterGroup);
			addedGroups.add(rosterGroup);
		}
		RosterGroupReference groupReference = new RosterGroupReference(rosterGroup);
		contact.addGroupReference(groupReference);
		Collection<RosterGroupReference> collection = addedGroupReference.get(contact);
		if (collection == null) {
			collection = new ArrayList<RosterGroupReference>();
			addedGroupReference.put(contact, collection);
		}
		collection.add(groupReference);
	}
	private void removeGroupReference(
			RosterContact contact,
			RosterGroupReference groupReference,
			Collection<RosterGroup> removedGroups,
			Map<RosterContact, Collection<RosterGroupReference>> removedGroupReference) {
		contact.removeGroupReference(groupReference);
		Collection<RosterGroupReference> collection = removedGroupReference
				.get(contact);
		if (collection == null) {
			collection = new ArrayList<RosterGroupReference>();
			removedGroupReference.put(contact, collection);
		}
		collection.add(groupReference);
		RosterGroup rosterGroup = groupReference.getRosterGroup();
		for (RosterContact check : rosterContacts.values())
			for (RosterGroupReference reference : check.getGroups())
				if (reference.getRosterGroup() == rosterGroup)
					return;
		rosterGroups.remove(rosterGroup.getAccount(), rosterGroup.getName());
		removedGroups.add(rosterGroup);
	}

	public Collection<String> getGroups(String account) {
		return Collections.unmodifiableCollection(rosterGroups.getNested(account).keySet());
	}

	public String getName(String account, String user) {
		RosterContact contact = getRosterContact(account, user);
		if (contact == null)
			return user;
		return contact.getName();
	}

	public Collection<String> getGroups(String account, String user) {
		RosterContact contact = getRosterContact(account, user);
		if (contact == null)
			return Collections.emptyList();
		return contact.getGroupNames();
	}

	public void createContact(String account, String bareAddress, String name,
			Collection<String> groups) throws NetworkException {
		RosterPacket packet = new RosterPacket();
		packet.setType(IQ.Type.SET);
		RosterPacket.Item item = new RosterPacket.Item(bareAddress, name);
		for (String group : groups)
			if (group.trim().length() > 0)
				item.addGroupName(group);
		packet.addRosterItem(item);
		ConnectionManager.getInstance().sendPacket(account, packet);
	}

	public void removeContact(String account, String bareAddress)
			throws NetworkException {
		RosterPacket packet = new RosterPacket();
		packet.setType(IQ.Type.SET);
		RosterPacket.Item item = new RosterPacket.Item(bareAddress, "");
		item.setItemType(RosterPacket.ItemType.remove);
		packet.addRosterItem(item);
		ConnectionManager.getInstance().sendPacket(account, packet);
	}

	public void setNameAndGroup(String account, String bareAddress,
			String name, Collection<String> groups) throws NetworkException {
		RosterContact contact = getRosterContact(account, bareAddress);
		if (contact == null)
			throw new NetworkException(R.string.ENTRY_IS_NOT_FOUND);
		if (contact.getRealName().equals(name)) {
			HashSet<String> check = new HashSet<String>(contact.getGroupNames());
			if (check.size() == groups.size()) {
				check.removeAll(groups);
				if (check.isEmpty())
					return;
			}
		}
		RosterPacket packet = new RosterPacket();
		packet.setType(IQ.Type.SET);
		RosterPacket.Item item = new RosterPacket.Item(bareAddress, name);
		for (String group : groups)
			item.addGroupName(group);
		packet.addRosterItem(item);
		ConnectionManager.getInstance().sendPacket(account, packet);
	}

	/**
	 * Requests to remove group from all contacts in account.
	 * 
	 * @param account
	 * @param group
	 * @throws NetworkException
	 */
	public void removeGroup(String account, String group)
			throws NetworkException {
		RosterPacket packet = new RosterPacket();
		packet.setType(IQ.Type.SET);
		for (RosterContact contact : rosterContacts.getNested(account).values()) {
			HashSet<String> groups = new HashSet<String>(contact.getGroupNames());
			if (!groups.remove(group))
				continue;
			RosterPacket.Item item = new RosterPacket.Item(contact.getUser(),contact.getRealName());
			for (String one : groups)
				item.addGroupName(one);
			packet.addRosterItem(item);
		}
		if (packet.getRosterItemCount() == 0)
			return;
		ConnectionManager.getInstance().sendPacket(account, packet);
	}

	/**
	 * Requests to remove group from all contacts in all accounts.
	 * 
	 * @param group
	 * @throws NetworkException
	 */
	public void removeGroup(String group) throws NetworkException {
		NetworkException networkException = null;
		boolean success = false;
		for (String account : AccountManager.getInstance().getAccounts()) {
			try {
				removeGroup(account, group);
			} catch (NetworkException e) {
				if (networkException == null)
					networkException = e;
				continue;
			}
			success = true;
		}
		if (!success && networkException != null)
			throw networkException;
	}

	/**
	 * Requests to rename group.
	 * 
	 * @param account
	 * @param oldGroup
	 *            can be <code>null</code> for "no group".
	 * @param newGroup
	 * @throws NetworkException
	 */
	public void renameGroup(String account, String oldGroup, String newGroup) throws NetworkException {
		if (newGroup.equals(oldGroup))
			return;
		RosterPacket packet = new RosterPacket();
		packet.setType(IQ.Type.SET);
		for (RosterContact contact : rosterContacts.getNested(account).values()) {
			HashSet<String> groups = new HashSet<String>(
					contact.getGroupNames());
			if (!groups.remove(oldGroup)
					&& !(oldGroup == null && groups.isEmpty()))
				continue;
			groups.add(newGroup);
			RosterPacket.Item item = new RosterPacket.Item(contact.getUser(),contact.getRealName());
			for (String one : groups)
				item.addGroupName(one);
			packet.addRosterItem(item);
		}
		if (packet.getRosterItemCount() == 0)
			return;
		ConnectionManager.getInstance().sendPacket(account, packet);
	}

	/**
	 * Requests to rename group from all accounts.
	 * 
	 * @param oldGroup
	 *            can be <code>null</code> for "no group".
	 * @param newGroup
	 * @throws NetworkException
	 */
	public void renameGroup(String oldGroup, String newGroup) throws NetworkException {
		NetworkException networkException = null;
		boolean success = false;
		for (String account : AccountManager.getInstance().getAccounts()) {
			try {
				renameGroup(account, oldGroup, newGroup);
			} catch (NetworkException e) {
				if (networkException == null)
					networkException = e;
				continue;
			}
			success = true;
		}
		if (!success && networkException != null)
			throw networkException;
	}

	/**
	 * @param account
	 * @return Whether roster for specified account has been received.
	 */
	public boolean isRosterReceived(String account) {
		return receivedRosters.contains(account);
	}

	/**
	 * Sets whether contacts in accounts are enabled.
	 * 
	 * @param account
	 * @param enabled
	 */
	private void setEnabled(String account, boolean enabled) {
		for (RosterContact contact : rosterContacts.getNested(account).values())
			contact.setEnabled(enabled);
	}

	@Override
	public void onAccountEnabled(AccountItem accountItem) {
		setEnabled(accountItem.getAccount(), true);
	}

	@Override
	public void onArchiveModificationsReceived(ConnectionItem connection) {
		if (!(connection instanceof AccountItem))
			return;
		// Request roster only when server side archive modifications
		// received.
		String account = ((AccountItem) connection).getAccount();
		requestedRosters.add(account);
		try {
			ConnectionManager.getInstance().sendPacket(account,
					new RosterPacket());
		} catch (NetworkException e) {
			LogManager.exception(this, e);
		}
	}

	@Override
	public void onDisconnect(ConnectionItem connection) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		for (RosterContact contact : rosterContacts.getNested(account).values())
			contact.setConnected(false);
		requestedRosters.remove(account);
		receivedRosters.remove(account);
	}

	@Override
	public void onAccountDisabled(AccountItem accountItem) {
		setEnabled(accountItem.getAccount(), false);
	}

	@Override
	public void onAccountRemoved(AccountItem accountItem) {
		rosterGroups.clear(accountItem.getAccount());
		rosterContacts.clear(accountItem.getAccount());
	}

	@Override
	public void onPacket(ConnectionItem connection, String bareAddress, Packet packet) {
//		if(packet.getFrom()!=null){
//			RosterContact rCon = getRosterContact(AccountManager.getInstance().getAccountKu(), bareAddress);
//			rCon.setVurboseName(packet.getFrom());
//			Log.e(AppConstants.TAG, "SET1 : "+packet.getFrom());
//		}
//		
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		if (!(packet instanceof RosterPacket))
			return;
		
		if (((RosterPacket) packet).getType() != IQ.Type.ERROR) {
			boolean rosterWasReceived = requestedRosters.remove(account);
			ArrayList<RosterContact> remove = new ArrayList<RosterContact>();
			if (rosterWasReceived)
				for (RosterContact contact : rosterContacts.getNested(account).values()) {
					contact.setConnected(true);
					remove.add(contact);
				}
			RosterPacket rosterPacket = (RosterPacket) packet;
			ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();
			Collection<RosterGroup> addedGroups = new ArrayList<RosterGroup>();
			Map<RosterContact, String> addedContacts = new HashMap<RosterContact, String>();
			Map<RosterContact, String> renamedContacts = new HashMap<RosterContact, String>();
			Map<RosterContact, Collection<RosterGroupReference>> addedGroupReference = new HashMap<RosterContact, Collection<RosterGroupReference>>();
			Map<RosterContact, Collection<RosterGroupReference>> removedGroupReference = new HashMap<RosterContact, Collection<RosterGroupReference>>();
			Collection<RosterContact> removedContacts = new ArrayList<RosterContact>();
			Collection<RosterGroup> removedGroups = new ArrayList<RosterGroup>();

			for (RosterPacket.Item item : rosterPacket.getRosterItems()) {
				
				String user = Jid.getBareAddress(item.getUser());
				if (user == null)
					continue;
				entities.add(new BaseEntity(account, user));
				RosterContact contact = getRosterContact(account, user);
				if (item.getItemType() == RosterPacket.ItemType.remove) {
					if (contact != null)
						removeContact(contact, removedContacts);
				} else {
					String name = item.getName();
					if (name == null)
						name = "";
					if (contact == null) {
						contact = new RosterContact(account, user, name, item.getUser());
						if(item.getItemType() == ItemType.both )
						{
							addContact(contact, name, addedContacts);
							contact.setSubscribed(true);							
						} else {
							contact.setSubscribed(false);
							//removedContacts.add(contact);
							removeContact(contact, removedContacts);
						}
						
					} else {
						remove.remove(contact);
						if(contact.getSubscribed()){							
							if (!contact.getRealName().equals(name)){
								setName(contact, name, packet.getFrom(), renamedContacts);
							}
						}
						
					}
					ArrayList<RosterGroupReference> removeGroupReferences = new ArrayList<RosterGroupReference>(
							contact.getGroups());
					for (String groupName : item.getGroupNames()) {
						RosterGroupReference rosterGroup = contact.getRosterGroupReference(groupName);
						if (rosterGroup == null)
							addGroup(contact, groupName, addedGroups, addedGroupReference);
						else
							removeGroupReferences.remove(rosterGroup);
					}
					for (RosterGroupReference rosterGroup : removeGroupReferences)
						removeGroupReference(contact, rosterGroup,removedGroups, removedGroupReference);
					
//					contact.setSubscribed(item.getItemType() == ItemType.both || item.getItemType() == ItemType.to);					
					if(item.getItemType() == ItemType.both )
					{
						contact.setSubscribed(true);
					} else {
						contact.setSubscribed(false);
					}
				}
			}
			for (RosterContact contact : remove) {
				entities.add(new BaseEntity(account, contact.getUser()));
				removeContact(contact, removedContacts);
			}
			for (OnRosterChangedListener listener : Application.getInstance().getManagers(OnRosterChangedListener.class))
				listener.onRosterUpdate(addedGroups, addedContacts,renamedContacts, addedGroupReference,removedGroupReference, removedContacts, removedGroups);
				onContactsChanged(entities);
			if (rosterWasReceived) {
				AccountItem accountItem = (AccountItem) connection;
				receivedRosters.add(account);
				for (OnRosterReceivedListener listener : Application.getInstance().getManagers(OnRosterReceivedListener.class))
					listener.onRosterReceived(accountItem);
				AccountManager.getInstance().onAccountChanged(account);
			}
		}
	}

	/**
	 * Notifies registered {@link OnContactChangedListener}.
	 * 
	 * @param entities
	 */
	public void onContactsChanged(final Collection<BaseEntity> entities) {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (OnContactChangedListener onContactChangedListener : Application.getInstance().getUIListeners(OnContactChangedListener.class))
					onContactChangedListener.onContactsChanged(entities);
			}
		});
	}

	/**
	 * Notifies registered {@link OnContactChangedListener}.
	 * 
	 * @param entities
	 */
	public void onContactChanged(String account, String bareAddress) {
		final ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();
		entities.add(new BaseEntity(account, bareAddress));
		onContactsChanged(entities);
	}

}
