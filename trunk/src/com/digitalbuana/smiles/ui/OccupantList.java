package com.digitalbuana.smiles.ui;

import java.util.Collection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.activity.ProfileDetailActivity;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.OnAccountChangedListener;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.data.extension.muc.Occupant;
import com.digitalbuana.smiles.data.intent.AccountIntentBuilder;
import com.digitalbuana.smiles.data.intent.EntityIntentBuilder;
import com.digitalbuana.smiles.data.roster.OnContactChangedListener;
import com.digitalbuana.smiles.ui.adapter.OccupantListAdapter;
import com.digitalbuana.smiles.ui.helper.ManagedListActivity;
import com.digitalbuana.smiles.utils.StringUtils;
import com.digitalbuana.smiles.xmpp.address.Jid;

/**
 * Represent list of occupants in the room.
 * 
 * @author alexander.ivanov
 * 
 */
public class OccupantList extends ManagedListActivity implements
		OnAccountChangedListener, OnContactChangedListener{

	private String account;
	private String room;
	private OccupantListAdapter listAdapter;
	private String TAG = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing())
			return;

		account = getAccount(getIntent());
		room = Jid.getBareAddress(getUser(getIntent()));
		if (account == null || room == null || !MUCManager.getInstance().hasRoom(account, room)) {
			Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
			finish();
			return;
		}
		setContentView(R.layout.list);
		listAdapter = new OccupantListAdapter(this, account, room);
		setListAdapter(listAdapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Occupant user = (Occupant) listAdapter.getItem(arg2);
				String userJid = StringUtils.replaceStringEquals(user.getJid());
				
				Log.e(TAG, "user seleted " + userJid);
				
				if(userJid != null){
					if(userJid.contains("/")){
						String[] expluser = userJid.split("/");
						if(expluser.length>0)
							userJid = expluser[0];
					}
					startActivity(ProfileDetailActivity.createIntent(OccupantList.this, account, userJid));
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Application.getInstance().addUIListener(OnAccountChangedListener.class,this);
		Application.getInstance().addUIListener(OnContactChangedListener.class,this);
		listAdapter.onChange();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Application.getInstance().removeUIListener(OnAccountChangedListener.class, this);
		Application.getInstance().removeUIListener(OnContactChangedListener.class, this);
	}

	@Override
	public void onContactsChanged(Collection<BaseEntity> entities) {
		if (entities.contains(new BaseEntity(account, room)))
			listAdapter.onChange();
	}

	@Override
	public void onAccountsChanged(Collection<String> accounts) {
		if (accounts.contains(account))
			listAdapter.onChange();
	}

	public static Intent createIntent(Context context, String account,
			String user) {
		return new EntityIntentBuilder(context, OccupantList.class).setAccount(account).setUser(user).build();
	}

	private static String getAccount(Intent intent) {
		return AccountIntentBuilder.getAccount(intent);
	}

	private static String getUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}
}
