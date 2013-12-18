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

import java.util.HashMap;
import java.util.Map;

import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.connection.ConnectionItem;
import com.digitalbuana.smiles.data.connection.OnAuthorizedListener;

import android.net.Uri;

public class OAuthManager implements OnAuthorizedListener,
		OnAccountRemovedListener {

	private final Map<String, String> jids;

	private final static OAuthManager instance;

	static {
		instance = new OAuthManager(Application.getInstance());
		Application.getInstance().addManager(instance);
	}

	public static OAuthManager getInstance() {
		return instance;
	}

	private OAuthManager(Application application) {
		jids = new HashMap<String, String>();
	}

	private OAuthProvider getOAuthProvider()
			throws UnsupportedOperationException {
		for (OAuthProvider provider : Application.getInstance().getManagers(OAuthProvider.class))
				return provider;
		throw new UnsupportedOperationException();
	}

	public String requestRefreshToken( String code)
			throws NetworkException {
		return getOAuthProvider().requestRefreshToken(code);
	}


	public String getAssignedJid(String account) {
		return jids.get(account);
	}

	public String getUrl() {
		return getOAuthProvider().getUrl();
	}

	public boolean isValidUri(Uri uri) {
		return getOAuthProvider().isValidUri(uri);
	}

	@Override
	public void onAuthorized(ConnectionItem connection) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		String jid = connection.getRealJid();
		if (jid == null)
			return;
		jids.put(account, jid);
	}

	@Override
	public void onAccountRemoved(AccountItem accountItem) {
		jids.remove(accountItem.getAccount());
	}

}
