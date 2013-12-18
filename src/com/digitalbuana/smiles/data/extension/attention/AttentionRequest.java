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
package com.digitalbuana.smiles.data.extension.attention;

import android.content.Intent;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.activity.ChatViewActivity;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.notification.EntityNotificationItem;
import com.digitalbuana.smiles.data.roster.RosterManager;

public class AttentionRequest extends BaseEntity implements
		EntityNotificationItem {

	public AttentionRequest(String account, String user) {
		super(account, user);
	}

	@Override
	public Intent getIntent() {
		return ChatViewActivity.createAttentionRequestIntent( Application.getInstance(), account, user);
	}

	@Override
	public String getTitle() {
		return RosterManager.getInstance().getBestContact(account, user).getName();
	}

	@Override
	public String getText() {
		return  Application.getInstance().getString(R.string.pay_attention);
	}

}
