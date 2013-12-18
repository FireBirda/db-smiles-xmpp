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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.digitalbuana.smiles.data.account.StatusMode;

/**
 * Contact in roster.
 * 
 * {@link #getUser()} always will be bare jid.
 * 
 * @author alexander.ivanov
 * 
 */
public class RosterContact extends AbstractContact {

	protected String name;
	protected String vurboseName;
	protected final Map<String, RosterGroupReference> groupReferences;
	protected boolean subscribed;
	protected boolean connected;
	protected boolean enabled;
	private Long rawId;
	private Long jidId;
	private Long nickNameId;
	private Long structuredNameId;

	private Long viewId;

	public RosterContact(String account, String user, String name, String vurboseName) {
		super(account, user);
		this.name = name;
		if(vurboseName!=null){
			this.vurboseName=vurboseName;
		} else {
			this.vurboseName="";
		}
		groupReferences = new HashMap<String, RosterGroupReference>();
		subscribed = false;
		connected = true;
		enabled = true;
		rawId = null;
		jidId = null;
		nickNameId = null;
		structuredNameId = null;
		viewId = null;
	}

	public String getRealName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}
	void setVurboseName(String name) {
		if(name!=null)
		this.vurboseName = name;
	}

	@Override
	public Collection<RosterGroupReference> getGroups() {
		return Collections.unmodifiableCollection(groupReferences.values());
	}

	Collection<String> getGroupNames() {
		return Collections.unmodifiableCollection(groupReferences.keySet());
	}

	RosterGroupReference getRosterGroupReference(String groupName) {
		return groupReferences.get(groupName);
	}

	void addGroupReference(RosterGroupReference groupReference) {
		groupReferences.put(groupReference.getName(), groupReference);
	}

	void removeGroupReference(RosterGroupReference groupReference) {
		groupReferences.remove(groupReference.getName());
	}

	
	void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

	@Override
	public StatusMode getStatusMode() {
		ResourceItem resourceItem = PresenceManager.getInstance().getResourceItem(account, user);
		if (resourceItem == null) {
			if (subscribed)
				return StatusMode.unavailable;
			else
				return StatusMode.unsubscribed;
		}
		return resourceItem.getStatusMode();
	}

	@Override
	public String getName() {
		if (!"".equals(name))
			return name;
		return super.getName();
	}
	
	public String getVurboseName() {
		return vurboseName;
	}
	
	public boolean getSubscribed() {
		return this.subscribed;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isEnabled() {
		return enabled;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	Long getRawId() {
		return rawId;
	}

	void setRawId(Long contactId) {
		this.rawId = contactId;
	}

	Long getJidId() {
		return jidId;
	}

	void setJidId(Long jidId) {
		this.jidId = jidId;
	}

	Long getNickNameId() {
		return nickNameId;
	}

	void setNickNameId(Long nameId) {
		this.nickNameId = nameId;
	}

	public Long getStructuredNameId() {
		return structuredNameId;
	}

	public void setStructuredNameId(Long structuredNameId) {
		this.structuredNameId = structuredNameId;
	}

	public Long getViewId() {
		return viewId;
	}

	public void setViewId(Long viewId) {
		this.viewId = viewId;
	}

}
