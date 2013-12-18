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
package com.digitalbuana.smiles.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.intent.EntityIntentBuilder;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.data.roster.SubscriptionRequest;
import com.digitalbuana.smiles.ui.adapter.AccountChooseAdapter;
import com.digitalbuana.smiles.ui.dialog.ConfirmDialogBuilder;
import com.digitalbuana.smiles.ui.dialog.ConfirmDialogListener;
import com.digitalbuana.smiles.ui.dialog.DialogBuilder;
import com.digitalbuana.smiles.utils.StringUtils;

public class ContactAdd extends GroupListActivity implements
		View.OnClickListener, OnItemSelectedListener {


	private static final String ACTION_SUBSCRIPTION_REQUEST = "com.digitalbuana.smiles.data.SUBSCRIPTION_REQUEST";

	private static final String SAVED_ACCOUNT = "com.digitalbuana.smiles.ui.ContactAdd.SAVED_ACCOUNT";
	private static final String SAVED_USER = "com.digitalbuana.smiles.ui.ContactAdd.SAVED_USER";
	private static final String SAVED_NAME = "com.digitalbuana.smiles.ui.ContactAdd.SAVED_NAME";

//	private static final int DIALOG_SUBSCRIPTION_REQUEST_ID = 0x20;

	private String account;
	private String user;

	private SubscriptionRequest subscriptionRequest;

	private String userJIDtoAdd="";

//	private Spinner accountView;
	private EditText userView;
	private EditText nameView;
	private String TAG = getClass().getSimpleName();

	@Override
	protected void onInflate(Bundle savedInstanceState) {
		setContentView(R.layout.contact_add);

		ListView listView = getListView();
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.contact_add_header, listView,false);
		listView.addHeaderView(view, null, false);

//		accountView = (Spinner) view.findViewById(R.id.contact_account);
//		accountView.setAdapter(new AccountChooseAdapter(this));
//		accountView.setOnItemSelectedListener(this);
		userView = (EditText) view.findViewById(R.id.contact_user);
		nameView = (EditText) view.findViewById(R.id.contact_name);
		((Button) view.findViewById(R.id.ok)).setOnClickListener(this);

		String name;
		Intent intent = getIntent();
		if (savedInstanceState != null) {
			account = savedInstanceState.getString(SAVED_ACCOUNT);
			user = savedInstanceState.getString(SAVED_USER);
			name = savedInstanceState.getString(SAVED_NAME);
		} else {
			account = getAccount(intent);
			user = getUser(intent);
			if (account == null || user == null)
				name = null;
			else {
				name = RosterManager.getInstance().getName(account, user);
				if (user.equals(name))
					name = null;
			}
		}
		if (account == null) {
			Collection<String> accounts = AccountManager.getInstance().getAccounts();
			if (accounts.size() == 1)
				account = accounts.iterator().next();
		}
//		if (account != null) {
//			for (int position = 0; position < accountView.getCount(); position++)
//				if (account.equals(accountView.getItemAtPosition(position))) {
//					accountView.setSelection(position);
//					break;
//				}
//		}
		if (user != null)
			userView.setText(StringUtils.replaceStringEquals(user));
		if (name != null)
			nameView.setText(name);
		if (ACTION_SUBSCRIPTION_REQUEST.equals(intent.getAction())) {
			subscriptionRequest = PresenceManager.getInstance().getSubscriptionRequest(account, user);
			if (subscriptionRequest == null) {
				Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
				finish();
				return;
			}
		} else {
			subscriptionRequest = null;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_ACCOUNT,AccountManager.getInstance().getAccountKu());
		outState.putString(SAVED_USER, userView.getText().toString());
		outState.putString(SAVED_NAME, nameView.getText().toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ok:
			String user = userView.getText().toString();
			userJIDtoAdd = user+"@"+AppConstants.XMPPServerHost;
			addFriendsWithUsername();
			break;
		default:
			break;
		}
	}


	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		if(urlKu==AppConstants.APIAddFriends){
			doAddFirends();
		}
	}
	
	
	public void addFriendsWithUsername() {
		List<NameValuePair> postDatanya = new ArrayList<NameValuePair>(2);
		postDatanya.add(new BasicNameValuePair("username", AccountManager.getInstance().getAccountKu()));
		postDatanya.add(new BasicNameValuePair("friendname", StringUtils.replaceStringEquals(userJIDtoAdd)));
		doPostAsync(context, AppConstants.APIAddFriends, postDatanya, null, true);
	}
	
	private void doAddFirends(){
		try {
			RosterManager.getInstance().createContact(account, userJIDtoAdd, nameView.getText().toString(), getSelected());
			PresenceManager.getInstance().requestSubscription(account, userJIDtoAdd);
			PresenceManager.getInstance().acceptSubscription(account,userJIDtoAdd);
			Log.e(TAG, "adding : "+account+"      ----> "+userJIDtoAdd);
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			finish();
			return;
		}
		MessageManager.getInstance().openChat(account, user);
		finish();
	}
	@Override
	Collection<String> getInitialGroups() {
		String account = AccountManager.getInstance().getAccountKu();
		if (account == null)
			return Collections.emptyList();
		return RosterManager.getInstance().getGroups(account);
	}

	@Override
	Collection<String> getInitialSelected() {
		return Collections.emptyList();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String account = AccountManager.getInstance().getAccountKu();
		if (account == null) {
			onNothingSelected(parent);
		} else {
			HashSet<String> groups = new HashSet<String>(RosterManager.getInstance().getGroups(account));
			groups.addAll(getSelected());
			setGroups(groups, getSelected());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		setGroups(getSelected(), getSelected());
	}

	public static Intent createIntent(Context context) {
		return createIntent(context, null);
	}

	private static Intent createIntent(Context context, String account,String user) {
		return new EntityIntentBuilder(context, ContactAdd.class).setAccount(account).setUser(user).build();
	}

	public static Intent createIntent(Context context, String account) {
		return createIntent(context, account, null);
	}

	public static Intent createSubscriptionIntent(Context context,String account, String user) {
		Intent intent = createIntent(context, account, user);
		intent.setAction(ACTION_SUBSCRIPTION_REQUEST);
		return intent;
	}

	private static String getAccount(Intent intent) {
		return EntityIntentBuilder.getAccount(intent);
	}

	private static String getUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}

}
