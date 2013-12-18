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

import android.database.Cursor;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.OnLoadListener;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.OnAccountRemovedListener;
import com.digitalbuana.smiles.data.entity.NestedMap;
import com.digitalbuana.smiles.data.entity.NestedMap.Entry;

public class GroupManager implements OnLoadListener, OnAccountRemovedListener,
		GroupStateProvider {

	public static final String IS_ROOM = "com.digitalbuana.smiles.data.IS_ROOM";
	public static final String IS_ROOM_ROOM = "com.digitalbuana.smiles.data.IS_ROOM_ROOM";	
	public static final String ACTIVE_CHATS = "com.digitalbuana.smiles.data.ACTIVE_CHATS";
	public static final String NO_GROUP = "com.digitalbuana.smiles.data.NO_GROUP";
	public static final String IS_ACCOUNT = "com.digitalbuana.smiles.data.IS_ACCOUNT";
	public static final String NO_ACCOUNT = "com.digitalbuana.smiles.data.NO_ACCOUNT";

	private final NestedMap<GroupConfiguration> groupConfigurations;

	private final static GroupManager instance;

	static {
		instance = new GroupManager();
		Application.getInstance().addManager(instance);
	}

	public static GroupManager getInstance() {
		return instance;
	}

	private GroupManager() {
		groupConfigurations = new NestedMap<GroupConfiguration>();
	}

	@Override
	public void onLoad() {
			final NestedMap<GroupConfiguration> groupConfigurations = new NestedMap<GroupConfiguration>();
			Cursor cursor = GroupTable.getInstance().list();
			try {
				if (cursor.moveToFirst()) {
					do {
						GroupConfiguration rosterConfiguration = new GroupConfiguration();
						rosterConfiguration.setExpanded(GroupTable.isExpanded(cursor));
						rosterConfiguration.setShowOfflineMode(GroupTable.getShowOfflineMode(cursor));
						groupConfigurations.put(GroupTable.getAccount(cursor), GroupTable.getGroup(cursor), rosterConfiguration);
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
			Application.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onLoaded(groupConfigurations);
				}
			});
	}

	private void onLoaded(NestedMap<GroupConfiguration> groupConfigurations) {
		this.groupConfigurations.addAll(groupConfigurations);
	}

	@Override
	public void onAccountRemoved(AccountItem accountItem) {
		groupConfigurations.clear(accountItem.getAccount());
	}

	public String getGroupName(String account, String group) {
		if (group == GroupManager.NO_GROUP)
			return Application.getInstance().getString(R.string.group_none);
		else if (group == GroupManager.IS_ROOM)
			return Application.getInstance().getString(R.string.group_room);
		else if (group == GroupManager.IS_ROOM_ROOM)
			return Application.getInstance().getString(R.string.group_room_room);
		else if (group == GroupManager.ACTIVE_CHATS)
			return Application.getInstance().getString(R.string.group_active_chat);
		else if (group == GroupManager.IS_ACCOUNT)
			return AccountManager.getInstance().getVerboseName(account);
		return group;
	}

	@Override
	public boolean isExpanded(String account, String group) {
		GroupConfiguration configuration = groupConfigurations.get(account,
				group);
		if (configuration == null)
			return true;
		return configuration.isExpanded();
	}

	@Override
	public ShowOfflineMode getShowOfflineMode(String account, String group) {
		GroupConfiguration configuration = groupConfigurations.get(account,
				group);
		if (configuration == null)
			return ShowOfflineMode.normal;
		return configuration.getShowOfflineMode();
	}

	@Override
	public void setExpanded(String account, String group, boolean expanded) {
		GroupConfiguration configuration = groupConfigurations.get(account,
				group);
		if (configuration == null) {
			configuration = new GroupConfiguration();
			groupConfigurations.put(account, group, configuration);
		}
		configuration.setExpanded(expanded);
		requestToWriteGroup(account, group, configuration.isExpanded(),
				configuration.getShowOfflineMode());
	}

	@Override
	public void setShowOfflineMode(String account, String group,
			ShowOfflineMode showOfflineMode) {
		GroupConfiguration configuration = groupConfigurations.get(account,
				group);
		if (configuration == null) {
			configuration = new GroupConfiguration();
			groupConfigurations.put(account, group, configuration);
		}
		configuration.setShowOfflineMode(showOfflineMode);
		requestToWriteGroup(account, group, configuration.isExpanded(),
				configuration.getShowOfflineMode());
	}

	/**
	 * Reset all show offline modes.
	 */
	public void resetShowOfflineModes() {
		for (Entry<GroupConfiguration> entry : groupConfigurations)
			if (entry.getValue().getShowOfflineMode() != ShowOfflineMode.normal)
				setShowOfflineMode(entry.getFirst(), entry.getSecond(),
						ShowOfflineMode.normal);
	}

	private void requestToWriteGroup(final String account, final String group,
			final boolean expanded, final ShowOfflineMode showOfflineMode) {
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				GroupTable.getInstance().write(account, group, expanded,showOfflineMode);
			}
		});
	}

}
