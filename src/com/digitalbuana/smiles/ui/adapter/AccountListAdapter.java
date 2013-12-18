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

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.connection.ConnectionState;
import com.digitalbuana.smiles.data.extension.avatar.AvatarManager;
import com.digitalbuana.smiles.ui.AccountList;
import com.digitalbuana.smiles.utils.Emoticons;

/**
 * Adapter for the list of accounts for {@link AccountList}.
 * 
 * @author alexander.ivanov
 * 
 */
public class AccountListAdapter extends BaseListEditorAdapter<String> {
	
	//private Activity _activity;
	private String TAG = getClass().getSimpleName();
	public AccountListAdapter(Activity activity) {
		super(activity);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		AccountManager accountManager = AccountManager.getInstance();
		if (convertView == null) {
			view = getActivity().getLayoutInflater().inflate(R.layout.account_list_item, parent, false);
		} else {
			view = convertView;
		}
		String account = getItem(position);
		((ImageView) view.findViewById(R.id.avatar)).setImageDrawable(AvatarManager.getInstance().getAccountAvatar(account));
		String statusText = accountManager.getVerboseName(account);
		if(statusText.contains(AppConstants.XMPPServerHost)){
			statusText.replace(AppConstants.XMPPServerHost, "");
		}
		((TextView) view.findViewById(R.id.name)).setText(statusText);
		AccountItem accountItem = accountManager.getAccount(account);
		ConnectionState state;
		if (accountItem == null)
			state = ConnectionState.offline;
		else
			state = accountItem.getState();
		((TextView) view.findViewById(R.id.status)).setText(
				Emoticons.getSmiledText(
					super.getActivity().getApplicationContext(), 
					getActivity().getString(state.getStringId())
				), 
			BufferType.SPANNABLE);
		return view;
	}


}
