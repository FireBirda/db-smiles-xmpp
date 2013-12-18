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

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountManager;


public class AccountTypeAdapter extends BaseAdapter {

	private final Activity activity;

	public AccountTypeAdapter(Activity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return AppConstants.XMPPConfName;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		if (convertView == null) {
			view = activity.getLayoutInflater().inflate(
					R.layout.account_type_item, parent, false);
		} else {
			view = convertView;
		}
		((ImageView) view.findViewById(R.id.avatar)).setImageResource(R.drawable.ic_launcher);
		((TextView) view.findViewById(R.id.name)).setText(AppConstants.XMPPConfName);
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		final View view;
		if (convertView == null) {
			view = activity.getLayoutInflater().inflate(R.layout.account_type_dropdown, parent, false);
		} else {
			view = convertView;
		}
		((ImageView) view.findViewById(R.id.avatar)).setImageResource(R.drawable.ic_launcher);
		((TextView) view.findViewById(R.id.name)).setText(AppConstants.XMPPConfName);
		return view;
	}

}
