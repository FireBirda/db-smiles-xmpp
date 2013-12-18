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
package com.digitalbuana.smiles.data.account;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.database.Cursor;

import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.OnLoadListener;
import com.digitalbuana.smiles.data.OnWipeListener;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.StickerManager;
import com.digitalbuana.smiles.data.connection.ConnectionState;
import com.digitalbuana.smiles.data.extension.vcard.VCardManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.xmpp.address.Jid;

public class AccountManager implements OnLoadListener, OnWipeListener {

	private final Collection<SavedStatus> savedStatuses;

	private final Map<String, AccountItem> accountItems;

	private final Collection<String> enabledAccounts;

	private final Application application;
	private final static AccountManager instance;
	static {
		instance = new AccountManager();
		Application.getInstance().addManager(instance);
	}

	public static AccountManager getInstance() {
		return instance;
	}

	private AccountManager() {
		this.application = Application.getInstance();
		enabledAccounts = new HashSet<String>();
		savedStatuses = new ArrayList<SavedStatus>();
		accountItems = new HashMap<String, AccountItem>(1);
	}

	@Override
	public void onLoad() {
		final Collection<SavedStatus> savedStatuses = new ArrayList<SavedStatus>();
		final Collection<AccountItem> accountItems = new ArrayList<AccountItem>(
				1);
		Cursor cursor = StatusTable.getInstance().list();
		try {
			if (cursor.moveToFirst()) {
				do {
					savedStatuses.add(new SavedStatus(StatusTable
							.getStatusMode(cursor), StatusTable
							.getStatusText(cursor)));
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}

		cursor = AccountTable.getInstance().list();
		try {
			if (cursor.moveToFirst()) {
				do {
					AccountItem accountItem = new AccountItem(
							AccountTable.getUserName(cursor),
							AccountTable.getPassword(cursor),
							AccountTable.getServerName(cursor),
							AccountTable.getPriority(cursor),
							AccountTable.getStatusMode(cursor),
							AccountTable.getStatusText(cursor),
							AccountTable.isEnabled(cursor),
							AccountTable.isSaslEnabled(cursor),
							AccountTable.isCompression(cursor),
							AccountTable.getKeyPair(cursor),
							AccountTable.getLastSync(cursor),
							AccountTable.getArchiveMode(cursor));
					accountItem.setId(AccountTable.getId(cursor));
					accountItems.add(accountItem);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}

		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onLoaded(savedStatuses, accountItems);
			}
		});

	}

	public void setRegID(String regID) {

	}

	private void onLoaded(Collection<SavedStatus> savedStatuses,
			Collection<AccountItem> accountItems) {
		this.savedStatuses.addAll(savedStatuses);
		int x = 0;
		for (AccountItem accountItem : accountItems) {
			addAccount(accountItem);
			if (x > 1) {
				removeAccount(accountItem.getAccount());
			}
			x++;
		}

	}

	private void addAccount(AccountItem accountItem) {
		accountItems.put(accountItem.getAccount(), accountItem);
		if (accountItem.isEnabled())
			enabledAccounts.add(accountItem.getAccount());
		for (OnAccountAddedListener listener : application
				.getManagers(OnAccountAddedListener.class))
			listener.onAccountAdded(accountItem);
		if (accountItem.isEnabled()) {
			onAccountEnabled(accountItem);
			if (accountItem.getRawStatusMode().isOnline())
				onAccountOnline(accountItem);
		}
		onAccountChanged(accountItem.getAccount());
	}

	public void onAccountChanged(String account) {
		Collection<String> accounts = new ArrayList<String>(1);
		accounts.add(account);
		onAccountsChanged(accounts);
	}

	public AccountItem getAccount(String account) {
		return accountItems.get(account);
	}

	void requestToWriteAccount(final AccountItem accountItem) {
		final String userName = accountItem.getConnectionSettings()
				.getUserName();
		final String password = accountItem.getConnectionSettings()
				.getPassword();
		final int priority = accountItem.getPriority();
		final StatusMode statusMode = accountItem.getRawStatusMode();
		final String statusText = accountItem.getStatusText();
		final boolean enabled = accountItem.isEnabled();
		final boolean saslEnabled = accountItem.getConnectionSettings()
				.isSaslEnabled();
		final boolean compression = accountItem.getConnectionSettings()
				.useCompression();
		final KeyPair keyPair = accountItem.getKeyPair();
		final Date lastSync = accountItem.getLastSync();
		final ArchiveMode archiveMode = accountItem.getArchiveMode();
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				accountItem.setId(AccountTable.getInstance().write(
						accountItem.getId(), userName, password, priority,
						statusMode, statusText, enabled, saslEnabled,
						compression, keyPair, lastSync, archiveMode));
			}
		});
	}

	private AccountItem addAccount(String userName, String password,
			String serverName, int priority, StatusMode statusMode,
			String statusText, boolean enabled, boolean saslEnabled,
			boolean compression, KeyPair keyPair, Date lastSync,
			ArchiveMode archiveMode) {

		AccountItem accountItem = new AccountItem(userName, password,
				serverName, priority, statusMode, statusText, enabled,
				saslEnabled, compression, keyPair, lastSync, archiveMode);

		requestToWriteAccount(accountItem);
		addAccount(accountItem);
		accountItem.updateConnection(true);
		return accountItem;
	}

	public String addAccount(String user, String password)
			throws NetworkException {
		AccountItem accountItem = addAccount(user, password,
				AppConstants.XMPPServerHost, 0, StatusMode.available,
				SettingsManager.statusText(), true, true, true, null, null,
				ArchiveMode.local);
		onAccountChanged(accountItem.getAccount());
		SettingsManager.enableContactsShowAccount();
		return accountItem.getAccount();
	}

	private void removeAccountWithoutCallback(final String account) {
		final AccountItem accountItem = getAccount(account);
		boolean wasEnabled = accountItem.isEnabled();
		accountItem.setEnabled(false);
		accountItem.updateConnection(true);
		if (wasEnabled) {
			if (accountItem.getRawStatusMode().isOnline())
				onAccountOffline(accountItem);
			onAccountDisabled(accountItem);
		}
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				AccountTable.getInstance().remove(account, accountItem.getId());
			}
		});
		accountItems.remove(account);
		enabledAccounts.remove(account);
		for (OnAccountRemovedListener listener : application
				.getManagers(OnAccountRemovedListener.class))
			listener.onAccountRemoved(accountItem);
		removeAuthorizationError(account);
	}

	public void removeAccount(String account) {
		removeAccountWithoutCallback(account);
		onAccountChanged(account);
	}

	public void doLogout() {
		// FileTransferKuManager.getInstance().removeFileTransferManager();
		for (String accountString : AccountManager.getInstance().getAccounts()) {
			removeAccountWithoutCallback(accountString);
			onAccountChanged(accountString);
		}
		FriendsManager.getInstance().getFriendsBlockedManager()
				.deleteAllFriends();
		FriendsManager.getInstance().getFriendsListManager().deleteAllFriends();
		FriendsManager.getInstance().getFriendsPenddingHeConfirmManager()
				.deleteAllFriends();
		FriendsManager.getInstance().getFriendsWaitingMeApproveManager()
				.deleteAllFriends();
		FriendsManager.getInstance().getVisitorManager().deleteAllVisitor();
		FriendsManager.getInstance().getFriendsUpdateManager()
				.deleteAllFriendsUpdate();
		FriendsManager.getInstance().getAdminUpdateManager()
				.deleteAllNotification();
		StickerManager.getInstance().deleteAllDownloaded();
	}

	public void updateAccount(String account, String userName, String password,
			int priority, boolean enabled, boolean saslEnabled,
			boolean compression, ArchiveMode archiveMode) {

		AccountItem result;
		AccountItem accountItem = getAccount(account);

		result = accountItem;
		boolean reconnect = false;
		for (OnAccountSyncableChangedListener listener : application
				.getManagers(OnAccountSyncableChangedListener.class))
			listener.onAccountSyncableChanged(result);

		boolean changed = result.isEnabled() != enabled;
		result.setEnabled(enabled);
		if (result.getPriority() != priority) {
			result.setPriority(priority);
			try {
				PresenceManager.getInstance().resendPresence(account);
			} catch (NetworkException e) {
			}
		}
		if (result.getArchiveMode() != archiveMode) {
			reconnect = (result.getArchiveMode() == ArchiveMode.server) != (archiveMode == ArchiveMode.server);
			result.setArchiveMode(archiveMode);
			for (OnAccountArchiveModeChangedListener listener : application
					.getManagers(OnAccountArchiveModeChangedListener.class))
				listener.onAccountArchiveModeChanged(result);
		}
		if (changed && enabled) {
			enabledAccounts.add(account);
			onAccountEnabled(result);
			if (result.getRawStatusMode().isOnline())
				onAccountOnline(result);
		}
		if (changed || reconnect) {
			result.updateConnection(true);
			result.forceReconnect();
		}
		if (changed && !enabled) {
			enabledAccounts.remove(account);
			if (result.getRawStatusMode().isOnline())
				onAccountOffline(result);
			onAccountDisabled(result);
		}
		requestToWriteAccount(result);
		onAccountChanged(result.getAccount());
	}

	public void setKeyPair(String account, KeyPair keyPair) {
		AccountItem accountItem = getAccount(account);
		accountItem.setKeyPair(keyPair);
		requestToWriteAccount(accountItem);
	}

	public void setLastSync(String account, Date lastSync) {
		AccountItem accountItem = getAccount(account);
		accountItem.setLastSync(lastSync);
		requestToWriteAccount(accountItem);
	}

	public void setPassword(String account, String password) {
		AccountItem accountItem = getAccount(account);
		updateAccount(account, accountItem.getConnectionSettings()
				.getUserName(), password, accountItem.getPriority(),
				accountItem.isEnabled(), accountItem.getConnectionSettings()
						.isSaslEnabled(), accountItem.getConnectionSettings()
						.isSaslEnabled(), accountItem.getArchiveMode());
	}

	public void setArchiveMode(String account, ArchiveMode archiveMode) {
		AccountItem accountItem = getAccount(account);
		AccountManager.getInstance().updateAccount(account,
				accountItem.getConnectionSettings().getUserName(),
				accountItem.getConnectionSettings().getPassword(),
				accountItem.getPriority(), accountItem.isEnabled(),
				accountItem.getConnectionSettings().isSaslEnabled(),
				accountItem.getConnectionSettings().isSaslEnabled(),
				archiveMode);
	}

	public ArchiveMode getArchiveMode(String account) {
		AccountItem accountItem = getAccount(account);
		if (accountItem == null)
			return ArchiveMode.available;
		return accountItem.getArchiveMode();
	}

	public CommonState getCommonState() {
		boolean disabled = false;
		boolean offline = false;
		boolean waiting = false;
		boolean connecting = false;
		boolean roster = false;
		boolean online = false;

		for (AccountItem accountItem : accountItems.values()) {
			ConnectionState state = accountItem.getState();
			if (state == ConnectionState.connected)
				online = true;
			if (RosterManager.getInstance().isRosterReceived(
					accountItem.getAccount()))
				roster = true;
			if (state == ConnectionState.connecting
					|| state == ConnectionState.authentication)
				connecting = true;
			if (state == ConnectionState.waiting)
				waiting = true;
			if (accountItem.isEnabled())
				offline = true;
			disabled = true;
		}

		if (online)
			return CommonState.online;
		else if (roster)
			return CommonState.roster;
		else if (connecting)
			return CommonState.connecting;
		if (waiting)
			return CommonState.waiting;
		else if (offline)
			return CommonState.offline;
		else if (disabled)
			return CommonState.disabled;
		else
			return CommonState.empty;
	}

	private boolean hasSameBareAddress(String account) {
		String bareAddress = Jid.getBareAddress(account);
		for (AccountItem check : accountItems.values())
			if (!check.getAccount().equals(account)
					&& Jid.getBareAddress(check.getAccount()).equals(
							bareAddress))
				return true;
		return false;
	}

	public String getVerboseName(String account) {
		AccountItem accountItem = getAccount(account);
		if (accountItem == null)
			return account;
		if (hasSameBareAddress(account))
			return account;
		else
			return Jid.getBareAddress(account);
	}

	public String getNickName(String account) {
		String jid = OAuthManager.getInstance().getAssignedJid(account);
		String result = VCardManager.getInstance().getName(
				Jid.getBareAddress(jid));
		if ("".equals(result))
			return getVerboseName(account);
		else
			return result;
	}

	/**
	 * Sets status for account.
	 * 
	 * @param account
	 * @param statusMode
	 * @param statusText
	 */
	public void setStatus(String account, StatusMode statusMode,
			String statusText) {
		addSavedStatus(statusMode, statusText);
		AccountItem accountItem = getAccount(account);
		setStatus(accountItem, statusMode, statusText);
		try {
			PresenceManager.getInstance().resendPresence(account);
		} catch (NetworkException e) {
		}
		boolean found = false;
		for (AccountItem check : accountItems.values())
			if (check.isEnabled()
					&& SettingsManager.statusMode() == check.getRawStatusMode()) {
				found = true;
				break;
			}
		if (!found)
			SettingsManager.setStatusMode(statusMode);
		found = false;
		for (AccountItem check : accountItems.values())
			if (check.isEnabled()
					&& SettingsManager.statusText().equals(
							check.getStatusText())) {
				found = true;
				break;
			}
		if (!found)
			SettingsManager.setStatusText(statusText);
		onAccountChanged(account);
	}

	public void wakeUp() {
		resendPresence();
	}

	public void resendPresence() {
		for (AccountItem accountItem : accountItems.values())
			try {
				PresenceManager.getInstance().resendPresence(
						accountItem.getAccount());
			} catch (NetworkException e) {

			}
	}

	/**
	 * Sets status for account.
	 * 
	 * @param account
	 * @param statusMode
	 * @param statusText
	 */
	private void setStatus(AccountItem accountItem, StatusMode statusMode,
			String statusText) {
		boolean changed = accountItem.isEnabled()
				&& accountItem.getRawStatusMode().isOnline() != statusMode
						.isOnline();
		accountItem.setStatus(statusMode, statusText);
		if (changed && statusMode.isOnline())
			onAccountOnline(accountItem);
		accountItem.updateConnection(true);
		if (changed && !statusMode.isOnline())
			onAccountOffline(accountItem);
		requestToWriteAccount(accountItem);
	}

	/**
	 * Sets status for all accounts.
	 * 
	 * @param statusMode
	 * @param statusText
	 *            can be <code>null</code> if value was not changed.
	 */
	public void setStatus(StatusMode statusMode, String statusText) {
		SettingsManager.setStatusMode(statusMode);
		if (statusText != null) {
			addSavedStatus(statusMode, statusText);
			SettingsManager.setStatusText(statusText);
		}
		for (AccountItem accountItem : accountItems.values()) {
			setStatus(accountItem, statusMode,
					statusText == null ? accountItem.getStatusText()
							: statusText);
		}
		resendPresence();
		onAccountsChanged(new ArrayList<String>(AccountManager.getInstance()
				.getAllAccounts()));
	}

	public Collection<String> getAccounts() {
		return Collections.unmodifiableCollection(enabledAccounts);
	}

	public int getAccountSize() {
		return Collections.unmodifiableCollection(enabledAccounts).size();
	}

	public AccountItem getActiveAccount() {
		AccountItem selected = null;
		for (String accountString : AccountManager.getInstance().getAccounts()) {
			selected = AccountManager.getInstance().getAccount(accountString);
		}
		return selected;
	}

	public String getAccountKu() {
		try {
			if (getActiveAccount().getAccount() != null)
				return getActiveAccount().getAccount();
			else
				return "";
		} catch (NullPointerException e) {
			return "";
		}
	}

	public boolean getIsConnected() {
		try {
			return getActiveAccount().getConnectionThread().getXMPPConnection()
					.isConnected();
		} catch (NullPointerException e) {
			return false;
		}
	}

	public Collection<String> getAllAccounts() {
		return Collections.unmodifiableCollection(accountItems.keySet());
	}

	/**
	 * Save status in presets.
	 * 
	 * @param statusMode
	 * @param statusText
	 */
	private void addSavedStatus(final StatusMode statusMode,
			final String statusText) {
		SavedStatus savedStatus = new SavedStatus(statusMode, statusText);
		if (savedStatuses.contains(savedStatus))
			return;
		savedStatuses.add(savedStatus);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				StatusTable.getInstance().write(statusMode, statusText);
			}
		});
	}

	public void removeSavedStatus(final SavedStatus savedStatus) {
		if (!savedStatuses.remove(savedStatus))
			return;
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				StatusTable.getInstance().remove(savedStatus.getStatusMode(),
						savedStatus.getStatusText());
			}
		});
	}

	/**
	 * Clear list of status presets.
	 */
	public void clearSavedStatuses() {
		savedStatuses.clear();
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				StatusTable.getInstance().clear();
			}
		});
	}

	/**
	 * @return List of preset statuses.
	 */
	public Collection<SavedStatus> getSavedStatuses() {
		return Collections.unmodifiableCollection(savedStatuses);
	}

	/**
	 * @return Selected account to show contacts. <code>null</code> if
	 *         <ul>
	 *         <li>there is no selected account,</li>
	 *         <li>selected account does not exists or disabled,</li>
	 *         <li>Group by account is enabled.</li>
	 *         </ul>
	 */
	public String getSelectedAccount() {
		if (SettingsManager.contactsShowAccounts())
			return null;
		String selected = SettingsManager.contactsSelectedAccount();
		if (enabledAccounts.contains(selected))
			return selected;
		return null;
	}

	public void removeAuthorizationError(String account) {
		// authorizationErrorProvider.remove(account);
	}

	public void addAuthenticationError(String account) {
		// authorizationErrorProvider.add(new
		// AccountAuthorizationError(account),true);
	}

	public void removePasswordRequest(String account) {
		// passwordRequestProvider.remove(account);
	}

	public void addPasswordRequest(String account) {
		// passwordRequestProvider.add(new PasswordRequest(account), true);
	}

	public void onAccountsChanged(String account) {
		Collection<String> accounts = new ArrayList<String>(1);
		accounts.add(account);
		onAccountsChanged(accounts);
	}

	public void onAccountsChanged(final Collection<String> accounts) {

		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (OnAccountChangedListener accountListener : Application
						.getInstance().getUIListeners(
								OnAccountChangedListener.class))
					accountListener.onAccountsChanged(accounts);
			}
		});
	}

	public void onAccountEnabled(AccountItem accountItem) {
		for (OnAccountEnabledListener listener : application
				.getManagers(OnAccountEnabledListener.class))
			listener.onAccountEnabled(accountItem);
	}

	public void onAccountOnline(AccountItem accountItem) {
		for (OnAccountOnlineListener listener : application
				.getManagers(OnAccountOnlineListener.class))
			listener.onAccountOnline(accountItem);
	}

	public void onAccountOffline(AccountItem accountItem) {
		for (OnAccountOfflineListener listener : application
				.getManagers(OnAccountOfflineListener.class))
			listener.onAccountOffline(accountItem);
	}

	public void onAccountDisabled(AccountItem accountItem) {
		for (OnAccountDisabledListener listener : application
				.getManagers(OnAccountDisabledListener.class))
			listener.onAccountDisabled(accountItem);
	}

	@Override
	public void onWipe() {
		AccountTable.getInstance().wipe();
	}

}
