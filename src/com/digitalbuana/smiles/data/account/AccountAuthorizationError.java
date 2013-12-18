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

import android.content.Intent;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.entity.AccountRelated;
import com.digitalbuana.smiles.data.notification.AccountNotificationItem;
import com.digitalbuana.smiles.ui.AccountEditor;

public class AccountAuthorizationError extends AccountRelated implements
		AccountNotificationItem {

	public AccountAuthorizationError(String account) {
		super(account);
	}

	@Override
	public Intent getIntent() {
		return AccountEditor.createIntent(
				Application.getInstance(), account);
	}

	@Override
	public String getTitle() {
		return Application.getInstance().getString(
				R.string.AUTHENTICATION_FAILED);
	}

	@Override
	public String getText() {
		return AccountManager.getInstance().getVerboseName(account);
	}

}
