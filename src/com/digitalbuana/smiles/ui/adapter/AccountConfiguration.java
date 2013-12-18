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
import java.util.TreeMap;

import com.digitalbuana.smiles.data.roster.GroupStateProvider;

public class AccountConfiguration extends GroupConfiguration {

	private final TreeMap<String, GroupConfiguration> groups;

	public AccountConfiguration(String account, String user,
			GroupStateProvider groupStateProvider) {
		super(account, user, groupStateProvider);
		groups = new TreeMap<String, GroupConfiguration>();
	}

	public GroupConfiguration getGroupConfiguration(String group) {
		return groups.get(group);
	}

	public void addGroupConfiguration(GroupConfiguration groupConfiguration) {
		groups.put(groupConfiguration.getUser(), groupConfiguration);
	}
	public Collection<GroupConfiguration> getSortedGroupConfigurations() {
		ArrayList<GroupConfiguration> groups = new ArrayList<GroupConfiguration>(this.groups.values());
		Collections.sort(groups);
		return Collections.unmodifiableCollection(groups);
	}

}