package com.digitalbuana.smiles.data.roster;

import java.util.Collection;
import java.util.Collections;

import android.graphics.drawable.Drawable;

import com.digitalbuana.smiles.data.account.StatusMode;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.extension.avatar.AvatarManager;
import com.digitalbuana.smiles.data.extension.vcard.VCardManager;

public class AbstractContact extends BaseEntity {

	public AbstractContact(String account, String user) {
		super(account, user);
	}

	public String getName() {
		String vCardName = VCardManager.getInstance().getName(user);
		if (!"".equals(vCardName))
			return vCardName;
		return user;
	}
	public StatusMode getStatusMode() {
		return PresenceManager.getInstance().getStatusMode(account, user);
	}

	public String getStatusText() {
		return PresenceManager.getInstance().getStatusText(account, user);
	}
	public ResourceItem getResourceItem() {
		return PresenceManager.getInstance().getResourceItem(account, user);
	}

	public Collection<? extends Group> getGroups() {
		return Collections.emptyList();
	}

	public Drawable getAvatar() {
		return AvatarManager.getInstance().getUserAvatar(user);
	}

	public Drawable getAvatarForContactList() {
		return AvatarManager.getInstance().getUserAvatarForContactList(user);
	}

	public boolean isConnected() {
		return true;
	}

}
