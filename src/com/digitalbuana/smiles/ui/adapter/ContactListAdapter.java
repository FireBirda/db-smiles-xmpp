package com.digitalbuana.smiles.ui.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.model.BookmarkConferenceModel;
import com.digitalbuana.smiles.awan.stores.BookmarkConferenceStore;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.CommonState;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.data.extension.muc.RoomChat;
import com.digitalbuana.smiles.data.extension.muc.RoomContact;
import com.digitalbuana.smiles.data.message.AbstractChat;
import com.digitalbuana.smiles.data.message.ChatContact;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.MessageTable;
import com.digitalbuana.smiles.data.notification.NotificationManager;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.GroupManager;
import com.digitalbuana.smiles.data.roster.RosterContact;
import com.digitalbuana.smiles.data.roster.RosterManager;

public class ContactListAdapter extends
		GroupedContactAdapter<ChatContactInflater, GroupManager> implements
		Runnable {
	private static final long REFRESH_INTERVAL = 1000; // default 1000
	private final View infoView;
	private final Handler handler;
	private final Object refreshLock;
	private boolean refreshRequested;
	private boolean refreshInProgess;
	private Date nextRefresh;

	private ListActivity act;
	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;

	private String TAG = getClass().getSimpleName();

	private Context cntx;
	private AQuery aq;

	private ImageView smilesHomeHeaderImage;
	private Animation myFadeInAnimation;
	private String tempUsername;

	public ContactListAdapter(ListActivity activity) {

		super(activity, activity.getListView(), new ChatContactInflater(
				activity), GroupManager.getInstance());

		cntx = Application.getInstance().getApplicationContext();

		aq = new AQuery(cntx);

		mSettings = PreferenceManager.getDefaultSharedPreferences(cntx);
		settingsEditor = mSettings.edit();

		tempUsername = mSettings.getString(AppConstants.USERNAME_KEY, null);

		act = activity;
		infoView = activity.findViewById(R.id.info);

		smilesHomeHeaderImage = (ImageView) activity
				.findViewById(R.id.smilesHomeHeaderImage);
		myFadeInAnimation = AnimationUtils.loadAnimation(cntx, R.anim.tween);

		handler = new Handler();
		refreshLock = new Object();
		refreshRequested = false;
		refreshInProgess = false;
		nextRefresh = new Date();

	}

	public void refreshRequest() {
		synchronized (refreshLock) {
			if (refreshRequested)
				return;
			if (refreshInProgess)
				refreshRequested = true;
			else {
				long delay = nextRefresh.getTime() - new Date().getTime();
				handler.postDelayed(this, delay > 0 ? delay : 0);
			}
		}
	}

	public void removeRefreshRequests() {
		synchronized (refreshLock) {
			refreshRequested = false;
			refreshInProgess = false;
			handler.removeCallbacks(this);
		}
	}

	@Override
	public void onChange() {

		synchronized (refreshLock) {
			refreshRequested = false;
			refreshInProgess = true;
			handler.removeCallbacks(this);
		}
		new Thread("refresh contact list...") {
			public void run() {
				Collection<RosterContact> rosterContacts = null;
				try {
					rosterContacts = RosterManager.getInstance().getContacts();
				} finally {
					if (rosterContacts != null) {
						final Collection<RosterContact> rosterContact = rosterContacts;
						act.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								generateContactList(rosterContact);
							}
						});

					}
					this.interrupt();
					getActiveChat();
				}
			}
		}.start();

	}

	private void getActiveChat() {

		new Thread("refresh active chat...") {
			public void run() {
				try {
					Cursor cursor = MessageTable.getInstance().lastList();
					if (cursor.getCount() > 0) {

						if (cursor.moveToFirst()) {
							do {
								String friendAccount = MessageTable
										.getUser(cursor);
								if (!MessageManager.getInstance()
										.hasActiveChat(tempUsername,
												friendAccount)
										&& tempUsername != null) {
									try {
										MessageManager.getInstance().openChat(
												tempUsername, friendAccount);
									} catch (IllegalStateException e) {
									}
								}
							} while (cursor.moveToNext());
						}
					}
				} finally {
					this.interrupt();
				}
			}
		}.start();

	}

	private void generateContactList(Collection<RosterContact> rosterContacts) {

		final boolean showOffline = true;// SettingsManager.contactsShowOffline();
		final boolean showGroups = true;// ettingsManager.contactsShowGroups();
		final boolean showEmptyGroups = true;// SettingsManager.contactsShowEmptyGroups();
		// final boolean stayActiveChats =
		// true;//SettingsManager.contactsStayActiveChats();
		final boolean showAccounts = false;// SettingsManager.contactsShowAccounts();
		final Comparator<AbstractContact> comparator = SettingsManager
				.contactsOrder();
		final CommonState commonState = AccountManager.getInstance()
				.getCommonState();
		final String selectedAccount = AccountManager.getInstance()
				.getSelectedAccount();

		final TreeMap<String, AccountConfiguration> accounts = new TreeMap<String, AccountConfiguration>();
		final TreeMap<String, GroupConfiguration> groups;
		final ArrayList<AbstractContact> contacts;
		final GroupConfiguration activeChats;

		if (commonState == CommonState.online) {
			smilesHomeHeaderImage.setImageResource(R.drawable.img_logo_smiles);
			smilesHomeHeaderImage.startAnimation(myFadeInAnimation);
		} else
			smilesHomeHeaderImage
					.setImageResource(R.drawable.img_logo_smiles_grey);

		final TreeMap<String, TreeMap<String, AbstractChat>> abstractChats = new TreeMap<String, TreeMap<String, AbstractChat>>();

		boolean hasVisible = false;

		for (String account : AccountManager.getInstance().getAccounts())
			accounts.put(account, null);

		for (AbstractChat abstractChat : MessageManager.getInstance()
				.getChats()) {
			if ((abstractChat instanceof RoomChat || abstractChat.isActive())
					&& accounts.containsKey(abstractChat.getAccount())) {
				final String account = abstractChat.getAccount();
				TreeMap<String, AbstractChat> users = abstractChats
						.get(account);
				if (users == null) {
					users = new TreeMap<String, AbstractChat>();
					abstractChats.put(account, users);
				}
				users.put(abstractChat.getUser(), abstractChat);
			}
		}

		if (filterString == null) {
			// Create arrays.
			if (showAccounts) {
				groups = null;
				contacts = null;

				for (Entry<String, AccountConfiguration> entry : accounts
						.entrySet()) {
					entry.setValue(new AccountConfiguration(entry.getKey(),
							GroupManager.IS_ACCOUNT, groupStateProvider));
				}

			} else {
				if (showGroups) {
					groups = new TreeMap<String, GroupConfiguration>();
					contacts = null;
				} else {
					groups = null;
					contacts = new ArrayList<AbstractContact>();
				}
			}

			activeChats = new GroupConfiguration(GroupManager.NO_ACCOUNT,
					GroupManager.ACTIVE_CHATS, groupStateProvider);

			// Build structure.
			for (final RosterContact rosterContact : rosterContacts) {

				if (!rosterContact.isEnabled())
					continue;

				final boolean online = rosterContact.getStatusMode().isOnline();
				final String account = rosterContact.getAccount();

				final TreeMap<String, AbstractChat> users = abstractChats
						.get(account);
				final AbstractChat abstractChat;

				if (users == null)
					abstractChat = null;
				else
					abstractChat = users.remove(rosterContact.getUser());

				if (abstractChat != null) {
					activeChats.setNotEmpty();
					hasVisible = true;

					if (activeChats.isExpanded())
						activeChats.addAbstractContact(rosterContact);

					activeChats.increment(true);
				}

				// own account
				if (selectedAccount != null && !selectedAccount.equals(account))
					continue;

				// add to GroupedContactAdapter
				if (addContact(rosterContact, online, accounts, groups,
						contacts, showAccounts, showGroups, showOffline))
					hasVisible = true;
			}

			for (TreeMap<String, AbstractChat> users : abstractChats.values())
				for (AbstractChat abstractChat : users.values()) {
					final AbstractContact abstractContact;
					if (abstractChat instanceof RoomChat) {
						abstractContact = new RoomContact(
								(RoomChat) abstractChat);
					} else {
						abstractContact = new ChatContact(abstractChat);
					}

					activeChats.setNotEmpty();
					hasVisible = true;

					if (activeChats.isExpanded()
							&& !(abstractChat instanceof RoomChat)) {
						activeChats.addAbstractContact(abstractContact);
						activeChats.increment(false);
					}

					// own account
					if (selectedAccount != null
							&& !selectedAccount.equals(abstractChat
									.getAccount()))
						continue;

					final String group;
					boolean online;
					if (abstractChat instanceof RoomChat) {
						if (abstractChat.getUser().contains(
								AppConstants.XMPPRoomsServer)) {
							group = GroupManager.IS_ROOM_ROOM;
						} else {
							group = GroupManager.IS_ROOM;
						}
						online = abstractContact.getStatusMode().isOnline();
					} else {
						group = GroupManager.NO_GROUP;
						online = false;
					}

					// add to GroupedContactAdapter
					addContact(abstractContact, group, online, accounts,
							groups, contacts, showAccounts, showGroups);
				}

			// Remove empty groups, sort and apply structure.
			baseEntities.clear();
			if (hasVisible) {
				// if (showActiveChats) {
				if (!activeChats.isEmpty()) {
					if (showAccounts || showGroups)
						baseEntities.add(activeChats);

					activeChats
							.sortAbstractContacts(ComparatorByChat.COMPARATOR_BY_CHAT);

					baseEntities.addAll(activeChats.getAbstractContacts());
				}
				// }
				if (showAccounts) {

					for (final AccountConfiguration rosterAccount : accounts
							.values()) {
						baseEntities.add(rosterAccount);
						if (showGroups) {
							if (rosterAccount.isExpanded())
								for (final GroupConfiguration rosterConfiguration : rosterAccount
										.getSortedGroupConfigurations())
									if (showEmptyGroups) {
										baseEntities.add(rosterConfiguration);
										rosterConfiguration
												.sortAbstractContacts(ComparatorByName.COMPARATOR_BY_NAME);
										baseEntities.addAll(rosterConfiguration
												.getAbstractContacts());
									}
						} else {
							rosterAccount
									.sortAbstractContacts(ComparatorByName.COMPARATOR_BY_NAME);
							baseEntities.addAll(rosterAccount
									.getAbstractContacts());
						}
					}

				} else {
					if (showGroups) {
						for (final GroupConfiguration rosterConfiguration : groups
								.values())
							if (showEmptyGroups) {
								baseEntities.add(rosterConfiguration);
								rosterConfiguration
										.sortAbstractContacts(ComparatorByName.COMPARATOR_BY_NAME);
								baseEntities.addAll(rosterConfiguration
										.getAbstractContacts());
							}
					} else {
						Collections.sort(contacts,
								ComparatorByName.COMPARATOR_BY_NAME);
						baseEntities.addAll(contacts);
					}
				}
			}

		} else {
			// Search
			final ArrayList<AbstractContact> baseEntities = new ArrayList<AbstractContact>();

			// Build structure.
			for (RosterContact rosterContact : rosterContacts) {
				if (!rosterContact.isEnabled())
					continue;
				final String account = rosterContact.getAccount();
				final TreeMap<String, AbstractChat> users = abstractChats
						.get(account);

				if (users != null)
					users.remove(rosterContact.getUser());
				if (rosterContact.getName().toLowerCase(locale)
						.contains(filterString))
					baseEntities.add(rosterContact);
			}
			for (TreeMap<String, AbstractChat> users : abstractChats.values())
				for (AbstractChat abstractChat : users.values()) {
					final AbstractContact abstractContact;
					if (abstractChat instanceof RoomChat)
						abstractContact = new RoomContact(
								(RoomChat) abstractChat);
					else
						abstractContact = new ChatContact(abstractChat);
					if (abstractContact.getName().toLowerCase(locale)
							.contains(filterString))
						baseEntities.add(abstractContact);
				}
			Collections.sort(baseEntities, comparator);
			this.baseEntities.clear();
			this.baseEntities.addAll(baseEntities);
			hasVisible = baseEntities.size() > 0;

		}

		if (commonState == CommonState.online) {
			if (myFadeInAnimation.isInitialized()) {
				smilesHomeHeaderImage.clearAnimation();
				myFadeInAnimation.cancel();
				myFadeInAnimation.reset();
			}
		}

		super.onChange();
		synchronized (refreshLock) {
			nextRefresh = new Date(new Date().getTime() + REFRESH_INTERVAL);
			refreshInProgess = false;
			handler.removeCallbacks(this); // Just to be sure.
			if (refreshRequested)
				handler.postDelayed(this, REFRESH_INTERVAL);
		}
	}

	@Override
	public void run() {

		onChange();

	}

	public void getBookmarkItem() {

		final String account = mSettings.getString(AppConstants.USERNAME_KEY,
				null);

		final String lastBookmarkList = mSettings.getString(
				AppConstants.BOOKMARK_LIST_TAG, null);
		Map<String, String> params = new HashMap<String, String>();
		params.put("bu", account);
		params.put("r", AppConstants.ofSecret);
		aq.ajax(AppConstants.APIBookmarkList, params, JSONArray.class,
				new AjaxCallback<JSONArray>() {
					@Override
					public void callback(String url, JSONArray object,
							AjaxStatus status) {
						// TODO Auto-generated method stub
						if (object != null) {
							if (lastBookmarkList == null) {
								settingsEditor.putString(
										AppConstants.BOOKMARK_LIST_TAG,
										object.toString());
								generateBookmark(account, object);
							} else {
								if (!lastBookmarkList.trim().equals(
										object.toString())) {

									settingsEditor.putString(
											AppConstants.BOOKMARK_LIST_TAG,
											object.toString());

									generateBookmark(account, object);

								} else {
									try {
										generateBookmark(account,
												new JSONArray(lastBookmarkList));
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										// e.printStackTrace();
										Log.e(TAG, e.getMessage());
									}
								}
							}
						}
					}
				});
	}

	private void generateBookmark(String account, JSONArray object) {
		BookmarkConferenceStore bcs = new BookmarkConferenceStore(object);
		ArrayList<BookmarkConferenceModel> list = bcs.getResult();
		if (list != null && list.size() > 0) {
			for (BookmarkConferenceModel confList : list) {
				String confJID = confList.getJid();
				if (!MUCManager.getInstance().hasRoom(account, confJID)) {
					if (account != null && confJID != null) {
						MUCManager.getInstance().removeRoom(account, confJID);
						MessageManager.getInstance()
								.closeChat(account, confJID);
						NotificationManager.getInstance()
								.removeMessageNotification(account, confJID);
					}
					MUCManager.getInstance().createRoom(account, confJID,
							account, "", true);
				} else {
					if (!MUCManager.getInstance().inUse(account, confJID)) {
						MUCManager.getInstance().joinRoom(account, confJID,
								true);
					}
				}
			}
		}
	}
}
