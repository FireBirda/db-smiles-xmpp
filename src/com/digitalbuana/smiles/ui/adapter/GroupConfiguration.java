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
package com.digitalbuana.smiles.ui.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.GroupManager;
import com.digitalbuana.smiles.data.roster.GroupStateProvider;
import com.digitalbuana.smiles.data.roster.ShowOfflineMode;

/**
 * Group representation in the contact list.
 */
public class GroupConfiguration extends BaseEntity {

	private final ArrayList<AbstractContact> abstractContacts;

	private boolean empty;

	private final boolean expanded;

	private int total;

	private int online;

	private final ShowOfflineMode showOfflineMode = ShowOfflineMode.always;

	public GroupConfiguration(String account, String group,	GroupStateProvider groupStateProvider) {
		super(account, group);
		abstractContacts = new ArrayList<AbstractContact>();
		expanded = groupStateProvider.isExpanded(account, group);
		//showOfflineMode = groupStateProvider.getShowOfflineMode(account, group);
		empty = true;
		total = 0;
		online = 0;
	}

	public void addAbstractContact(AbstractContact abstractContact) {
		abstractContacts.add(abstractContact);
	}

	public Collection<AbstractContact> getAbstractContacts() {
		return abstractContacts;
	}

	public void sortAbstractContacts(Comparator<AbstractContact> comparator) {
		Collections.sort(abstractContacts, comparator);
	}

	public void increment(boolean online) {
		this.total++;
		if (online)
			this.online++;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setNotEmpty() {
		empty = false;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public int getTotal() {
		return total;
	}

	public int getOnline() {
		return online;
	}

	public ShowOfflineMode getShowOfflineMode() {
		return showOfflineMode;
	}

	@Override
	public int compareTo(BaseEntity another) {
		final String anotherUser = another.getUser();
		int result = account.compareTo(another.getAccount());
		if (result != 0) {
			if (user.compareTo(another.getUser()) != 0) {
				if (user == GroupManager.ACTIVE_CHATS)
					return -1;
				if (anotherUser == GroupManager.ACTIVE_CHATS)
					return 1;
			}
			return result;
		}
		result = user.compareTo(anotherUser);
		if (result != 0) {
			if (user == GroupManager.ACTIVE_CHATS)
				return -1;
			if (anotherUser == GroupManager.ACTIVE_CHATS)
				return 1;
			if (user == GroupManager.IS_ACCOUNT)
				return -1;
			if (anotherUser == GroupManager.IS_ACCOUNT)
				return 1;
			if (user == GroupManager.NO_GROUP)
				return -1;
			if (anotherUser == GroupManager.NO_GROUP)
				return 1;
			if (user == GroupManager.IS_ROOM)
				return -1;
			if (anotherUser == GroupManager.IS_ROOM)
				return 1;
			return result;
		}
		return 0;
	}

}