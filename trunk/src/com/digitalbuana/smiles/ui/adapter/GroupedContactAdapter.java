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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.Group;
import com.digitalbuana.smiles.data.roster.GroupManager;
import com.digitalbuana.smiles.data.roster.GroupStateProvider;
import com.digitalbuana.smiles.data.roster.ShowOfflineMode;
import com.digitalbuana.smiles.utils.StringUtils;

/**
 * Provide grouping implementation for the list of contacts.
 * 
 * @author alexander.ivanov
 * 
 */
public abstract class GroupedContactAdapter<Inflater extends BaseContactInflater, StateProvider extends GroupStateProvider> 
	extends SmoothContactAdapter<Inflater> {
	
	static final Collection<Group> NO_GROUP_LIST;
	static final int TYPE_CONTACT = 0;
	static final int TYPE_GROUP = 1;
	
	static {
		Collection<Group> groups = new ArrayList<Group>(1);
		groups.add(new Group() {
			@Override
			public String getName() {
				return GroupManager.NO_GROUP;
			}
		});
		NO_GROUP_LIST = Collections.unmodifiableCollection(groups);
	}
	
	final StateProvider groupStateProvider;
	private final LayoutInflater layoutInflater;

	public GroupedContactAdapter(Activity activity, ListView listView, Inflater inflater, StateProvider groupStateProvider) {
		super(activity, listView, inflater);
		layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.groupStateProvider = groupStateProvider;
	}

	public StateProvider getGroupStateProvider() {
		return groupStateProvider;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		Object object = getItem(position);
		if (object instanceof AbstractContact){
			return TYPE_CONTACT;
		} else if (object instanceof GroupConfiguration){
			return TYPE_GROUP;
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getItemViewType(position) == TYPE_CONTACT) {
			return super.getView(position, convertView, parent);
		} else if (getItemViewType(position) == TYPE_GROUP) {
			final View view;
			final GroupViewHolder viewHolder;
			final GroupConfiguration configuration = (GroupConfiguration) getItem(position);
			int totalGroupMember = configuration.getTotal();
			if(totalGroupMember==0){				
				return layoutInflater.inflate(R.layout.chat_viewer_empty, parent,false);
			}
			//if (convertView == null) {
				view = layoutInflater.inflate(R.layout.base_group_item, parent,false);
//				TypedArray typedArray = activity.obtainStyledAttributes(R.styleable.ContactList);
//				view.setBackgroundDrawable(typedArray.getDrawable(R.styleable.ContactList_expanderBackground));
//				((ImageView) view.findViewById(R.id.indicator)).setImageDrawable(typedArray.getDrawable(R.styleable.ContactList_expanderIndicator));
//				typedArray.recycle();
				viewHolder = new GroupViewHolder(view);
				//view.setTag(viewHolder);
			//} else {
			//	view = convertView;
			//	viewHolder = (GroupViewHolder) view.getTag();
			//}
			
			
			
			
			final String name = GroupManager.getInstance().getGroupName( configuration.getAccount(), configuration.getUser());
			String finalTitle = name + " ("+ totalGroupMember + ")";

			if(configuration.isExpanded()){
				viewHolder.dramatisir.setVisibility(View.VISIBLE);
			} else {
				viewHolder.dramatisir.setVisibility(View.GONE);
			}
			
			viewHolder.name.setText(finalTitle.toUpperCase());
			if(name.contains(Application.getInstance().getString(R.string.group_active_chat))){
				viewHolder.background.setBackgroundResource(R.color.barChat);
			} else if(name.contains(Application.getInstance().getString(R.string.group_room))){
				viewHolder.background.setBackgroundResource(R.color.barGrup);
			} else if(name.contains(Application.getInstance().getString(R.string.group_room_room))){
				viewHolder.background.setBackgroundResource(R.color.barRoom);
			} else if(name.contains(Application.getInstance().getString(R.string.group_default))){
				viewHolder.background.setBackgroundResource(R.color.barFriends);
			} else {
				viewHolder.background.setBackgroundResource(R.color.barOther);
			}			
			
			viewHolder.indicator.setImageLevel(configuration.isExpanded() ? 1 : 0);
			
			String namaku = StringUtils.replaceStringEquals(viewHolder.name.getText().toString());
			viewHolder.name.setText(namaku.trim());
			
			return view;
		} else
			throw new IllegalStateException();
	}

	protected GroupConfiguration getGroupConfiguration(AccountConfiguration accountConfiguration, String name) {
		GroupConfiguration groupConfiguration = accountConfiguration.getGroupConfiguration(name);
		if (groupConfiguration != null)
			return groupConfiguration;
		groupConfiguration = new GroupConfiguration(accountConfiguration.getAccount(), name, groupStateProvider);
		accountConfiguration.addGroupConfiguration(groupConfiguration);
		return groupConfiguration;
	}

	protected GroupConfiguration getGroupConfiguration(
			TreeMap<String, GroupConfiguration> groups, String name) {
		GroupConfiguration groupConfiguration = groups.get(name);
		if (groupConfiguration != null)
			return groupConfiguration;
		groupConfiguration = new GroupConfiguration(GroupManager.NO_ACCOUNT, name, groupStateProvider);
		groups.put(name, groupConfiguration);
		return groupConfiguration;
	}

	protected void addContact(AbstractContact abstractContact, String group,
			boolean online, TreeMap<String, AccountConfiguration> accounts,
			TreeMap<String, GroupConfiguration> groups,
			ArrayList<AbstractContact> contacts, boolean showAccounts,
			boolean showGroups) {
		if (showAccounts) {
			final String account = abstractContact.getAccount();
			final AccountConfiguration accountConfiguration;
			accountConfiguration = accounts.get(account);
			if (accountConfiguration == null)
				return;
			if (showGroups) {
				GroupConfiguration groupConfiguration = getGroupConfiguration(accountConfiguration, group);
				if (accountConfiguration.isExpanded()) {
					groupConfiguration.setNotEmpty();
					if (groupConfiguration.isExpanded())
						groupConfiguration.addAbstractContact(abstractContact);
				}
				groupConfiguration.increment(online);
			} else {
				if (accountConfiguration.isExpanded())
					accountConfiguration.addAbstractContact(abstractContact);
			}
			accountConfiguration.increment(online);
		} else {
			if (showGroups) {
				GroupConfiguration groupConfiguration = getGroupConfiguration(groups, group);
				groupConfiguration.setNotEmpty();
				if (groupConfiguration.isExpanded())
					groupConfiguration.addAbstractContact(abstractContact);
				groupConfiguration.increment(online);
			} else {
				contacts.add(abstractContact);
			}
		}
	}

	protected boolean addContact(AbstractContact abstractContact,
			boolean online, TreeMap<String, AccountConfiguration> accounts,
			TreeMap<String, GroupConfiguration> groups,
			ArrayList<AbstractContact> contacts, boolean showAccounts,
			boolean showGroups, boolean showOffline) {
		boolean hasVisible = false;
		if (showAccounts) {
			final AccountConfiguration accountConfiguration;
			accountConfiguration = accounts.get(abstractContact.getAccount());
			if (accountConfiguration == null)
				return hasVisible;
			if (showGroups) {
				Collection<? extends Group> abstractGroups = abstractContact.getGroups();
				if (abstractGroups.size() == 0)
					abstractGroups = NO_GROUP_LIST;
				for (Group abstractGroup : abstractGroups) {
					GroupConfiguration groupConfiguration = getGroupConfiguration(accountConfiguration, abstractGroup.getName());
					if (online
							|| (groupConfiguration.getShowOfflineMode() == ShowOfflineMode.always)
							|| (accountConfiguration.getShowOfflineMode() == ShowOfflineMode.always && groupConfiguration
									.getShowOfflineMode() == ShowOfflineMode.normal)
							|| (accountConfiguration.getShowOfflineMode() == ShowOfflineMode.normal
									&& groupConfiguration.getShowOfflineMode() == ShowOfflineMode.normal && showOffline)) {
						// ............. group
						// ......... | A | N | E
						// ....... A | + | + | -
						// account N | + | ? | -
						// ....... E | + | - | -
						hasVisible = true;
						if (accountConfiguration.isExpanded()) {
							groupConfiguration.setNotEmpty();
							if (groupConfiguration.isExpanded())
								groupConfiguration.addAbstractContact(abstractContact);
						}
					}
					groupConfiguration.increment(online);
				}
			} else {
				if (online
						|| (accountConfiguration.getShowOfflineMode() == ShowOfflineMode.always)
						|| (accountConfiguration.getShowOfflineMode() == ShowOfflineMode.normal && showOffline)) {
					hasVisible = true;
					if (accountConfiguration.isExpanded())
						accountConfiguration.addAbstractContact(abstractContact);
				}
			}
			accountConfiguration.increment(online);
		} else {
			if (showGroups) {
				Collection<? extends Group> abstractGroups = abstractContact.getGroups();
				if (abstractGroups.size() == 0)
					abstractGroups = NO_GROUP_LIST;
				for (Group abstractGroup : abstractGroups) {
					GroupConfiguration groupConfiguration = getGroupConfiguration(groups, abstractGroup.getName());
					if (online || (groupConfiguration.getShowOfflineMode() == ShowOfflineMode.always)
							|| (groupConfiguration.getShowOfflineMode() == ShowOfflineMode.normal && showOffline)) {
						groupConfiguration.setNotEmpty();
						hasVisible = true;
						if (groupConfiguration.isExpanded())
							groupConfiguration.addAbstractContact(abstractContact);
					}
					groupConfiguration.increment(online);
				}
			} else {
				if (online || showOffline) {
					hasVisible = true;
					contacts.add(abstractContact);
				}
			}
		}
		return hasVisible;
	}


	public void setExpanded(String account, String group, boolean expanded) {
		groupStateProvider.setExpanded(account, group, expanded);
		onChange();
	}

	private static class GroupViewHolder {
		final RelativeLayout background;
		final ImageView dramatisir;
		final ImageView indicator;
		final TextView name;
		final LinearLayout groupParent;

		public GroupViewHolder(View view) {
			background = (RelativeLayout) view.findViewById(R.id.background);
			indicator = (ImageView) view.findViewById(R.id.indicator);
			name = (TextView) view.findViewById(R.id.name);
			dramatisir = (ImageView) view.findViewById(R.id.dramatisir);
			groupParent = (LinearLayout)view.findViewById(R.id.groupParent);
		}
	}

}
