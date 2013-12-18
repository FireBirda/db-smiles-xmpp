package com.digitalbuana.smiles.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.RequestFriendsAdapter;
import com.digitalbuana.smiles.adapter.RequestFriendsAdapter.OnFriendsReqListener;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.entity.BaseEntity;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.roster.OnContactChangedListener;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;

public class FriendsRequestActivity extends ManagedActivity implements
		OnClickListener, OnFriendsReqListener, OnContactChangedListener {

	private String account;
	private FrameLayout btnBack;

	private GridViewKu gridRequestFrends;
	private GridViewKu gridPenddingFrends;
	private RequestFriendsAdapter reqAdapter;
	private RequestFriendsAdapter pendAdapter;

	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		settingsEditor = mSettings.edit();
		settingsEditor.putBoolean(AppConstants.LEFTMENU_NOTIF_TAG, false);
		settingsEditor.commit();

		setContentView(R.layout.activity_friend_request);

		rootView = (FrameLayout) findViewById(R.id.friendsRequestRootView);
		btnBack = (FrameLayout) findViewById(R.id.friendsRequestBtnBack);

		gridRequestFrends = (GridViewKu) findViewById(R.id.friendsRequestGridViewKu);
		gridPenddingFrends = (GridViewKu) findViewById(R.id.penddingRequestGridViewKu);

		btnBack.setOnClickListener(this);

		FontUtils.setRobotoFont(context, rootView);

		reqAdapter = new RequestFriendsAdapter(context, 0, this);
		gridRequestFrends.setAdapter(reqAdapter);
		pendAdapter = new RequestFriendsAdapter(context, 1, this);
		gridPenddingFrends.setAdapter(pendAdapter);

	}

	@Override
	protected void onResume() {
		super.onResume();
		reqAdapter.notifyDataSetChanged();
		pendAdapter.notifyDataSetChanged();
		account = AccountManager.getInstance().getAccountKu();
		Application.getInstance().addUIListener(OnContactChangedListener.class,
				this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Application.getInstance().removeUIListener(
				OnContactChangedListener.class, this);
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, FriendsRequestActivity.class);
	}

	@Override
	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		}
	}

	private ArrayList<String> getSelected() {
		ArrayList<String> list = new ArrayList<String>(0);
		Collections.sort(list);
		return list;
	}

	private static String targetName = "";

	private void doConfirmFriends(String jid) {
		List<NameValuePair> postDatanya = new ArrayList<NameValuePair>(2);
		postDatanya.add(new BasicNameValuePair("username", AccountManager
				.getInstance().getAccountKu()));
		postDatanya.add(new BasicNameValuePair("friendname", StringUtils
				.replaceStringEquals(jid)));
		doPostAsync(context, AppConstants.APIAddFriends, postDatanya, null,
				true);
		targetName = jid;
	}

	private void doDeleteFriends(String jid) {
		List<NameValuePair> postDatanya = new ArrayList<NameValuePair>(2);
		postDatanya.add(new BasicNameValuePair("username", AccountManager
				.getInstance().getAccountKu()));
		postDatanya.add(new BasicNameValuePair("rel_code", StringUtils
				.replaceStringEquals(jid)));
		doPostAsync(context, AppConstants.APIDeleteFriends, postDatanya, null,
				true);
		targetName = jid;
	}

	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		if (urlKu == AppConstants.APIAddFriends) {
			if (result.length() >= 5) {
				finishConfirmFirends(targetName);
			}
		} else if (urlKu == AppConstants.APIDeleteFriends) {
			if (result.length() >= 5) {
				finishCancelPendding(targetName);
			}
		}
	}

	private void finishConfirmFirends(String jid) {
		try {
			RosterManager.getInstance().createContact(account, jid,
					StringUtils.replaceStringEquals(jid), getSelected());
			PresenceManager.getInstance().requestSubscription(account, jid);
			PresenceManager.getInstance().acceptSubscription(account, jid);
			FriendsManager.getInstance().getFriendsPenddingHeConfirmManager()
					.removeFriendsJID(jid);
			FriendsManager.getInstance().getFriendsWaitingMeApproveManager()
					.removeFriendsJID(jid);
			FriendsManager.getInstance().getFriendsListManager()
					.addFriendsByJID(jid);
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			return;
		}
		reqAdapter.notifyDataSetChanged();
		pendAdapter.notifyDataSetChanged();
	}

	private void finishCancelPendding(String jid) {
		try {
			RosterManager.getInstance().removeContact(account, jid);
			PresenceManager.getInstance().discardSubscription(account, jid);
			FriendsManager.getInstance().getFriendsPenddingHeConfirmManager()
					.removeFriendsJID(jid);
			FriendsManager.getInstance().getFriendsListManager()
					.removeFriendsJID(jid);
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			return;
		}
		pendAdapter.notifyDataSetChanged();
	}

	@Override
	public void onReqFriendsListener(int type, String jid) {
		switch (type) {
		case 0:
			doConfirmFriends(jid);
			break;
		case 1:
			doDeleteFriends(jid);
			break;
		}
		reqAdapter.notifyDataSetChanged();
		pendAdapter.notifyDataSetChanged();
	}

	@Override
	public void onContactsChanged(Collection<BaseEntity> entities) {
		reqAdapter.notifyDataSetChanged();
		pendAdapter.notifyDataSetChanged();
	}
}
