///**
// * Copyright (c) 2013, Redsolution LTD. All rights reserved.
// * 
// * This file is part of Xabber project; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License, Version 3.
// * 
// * Xabber is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// * See the GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License,
// * along with this program. If not, see http://www.gnu.org/licenses/.
// */
//package com.digitalbuana.smiles.ui;
//
//import java.util.Collection;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//
//import com.digitalbuana.smiles.R;
//import com.digitalbuana.smiles.activity.ChatViewer;
//import com.digitalbuana.smiles.data.Application;
//import com.digitalbuana.smiles.data.account.OnAccountChangedListener;
//import com.digitalbuana.smiles.data.entity.BaseEntity;
//import com.digitalbuana.smiles.data.message.AbstractChat;
//import com.digitalbuana.smiles.data.message.OnChatChangedListener;
//import com.digitalbuana.smiles.data.roster.OnContactChangedListener;
//import com.digitalbuana.smiles.ui.adapter.ChatListAdapter;
//import com.digitalbuana.smiles.ui.helper.ManagedListActivity;
//
//public class ChatList extends ManagedListActivity implements
//		OnAccountChangedListener, OnContactChangedListener,
//		OnChatChangedListener, OnItemClickListener {
//
//	private ChatListAdapter listAdapter;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		if (isFinishing())
//			return;
//
//		setContentView(R.layout.list);
//		listAdapter = new ChatListAdapter(this);
//		setListAdapter(listAdapter);
//		getListView().setOnItemClickListener(this);
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		Application.getInstance().addUIListener(OnAccountChangedListener.class,this);
//		Application.getInstance().addUIListener(OnContactChangedListener.class,this);
//		Application.getInstance().addUIListener(OnChatChangedListener.class,this);
//		listAdapter.onChange();
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		Application.getInstance().removeUIListener(OnAccountChangedListener.class, this);
//		Application.getInstance().removeUIListener(OnContactChangedListener.class, this);
//		Application.getInstance().removeUIListener(OnChatChangedListener.class, this);
//	}
//
//	@Override
//	public void onChatChanged(String account, String user, boolean incoming) {
//		listAdapter.onChange();
//	}
//
//	@Override
//	public void onContactsChanged(Collection<BaseEntity> addresses) {
//		listAdapter.onChange();
//	}
//
//	@Override
//	public void onAccountsChanged(Collection<String> accounts) {
//		listAdapter.onChange();
//	}
//
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position,
//			long id) {
//		AbstractChat abstractChat = (AbstractChat) parent.getAdapter().getItem(position);
//		startActivity(ChatViewer.createIntent(this, abstractChat.getAccount(),abstractChat.getUser()));
//		finish();
//	}
//
//	public static Intent createIntent(Context context) {
//		return new Intent(context, ChatList.class);
//	}
//
//}
