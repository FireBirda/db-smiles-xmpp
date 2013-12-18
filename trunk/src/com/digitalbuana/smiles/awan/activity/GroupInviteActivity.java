package com.digitalbuana.smiles.awan.activity;

import java.util.Vector;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.adapters.GroupInviteAdapter;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.FontUtils;

public class GroupInviteActivity extends ManagedActivity implements
		OnClickListener {

	private String myAccount;
	private String groupName;
	private String TAG = getClass().getSimpleName();
	private EditText friendToInviteEditText;
	private ListView friendToInviteList;

	private FrameLayout inviteBtnBack, inviteButtonDone;

	private GroupInviteAdapter contactListAdapter = null;
	private Dialog waitDialog;
	private LinearLayout rootView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		waitDialog = ScreenHelper.getDialogProgress(this);
		myAccount = AccountManager.getInstance().getAccountKu();
		Intent i = getIntent();
		groupName = i.getStringExtra("group");
		setContentView(R.layout.activity_waiting_to_invite);
		friendToInviteEditText = (EditText) findViewById(R.id.friendToInviteEditText);
		friendToInviteList = (ListView) findViewById(R.id.friendToInviteList);
		inviteBtnBack = (FrameLayout) findViewById(R.id.inviteBtnBack);
		inviteButtonDone = (FrameLayout) findViewById(R.id.inviteButtonDone);
		getContactList();
		inviteBtnBack.setOnClickListener(this);
		inviteButtonDone.setOnClickListener(this);
		rootView = (LinearLayout) findViewById(R.id.inviteParentView);
		FontUtils.setRobotoFont(context, rootView);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == inviteBtnBack) {
			finish();
		} else if (v == inviteButtonDone) {
			sendInvitation();
		}
	}

	private void getContactList() {
		contactListAdapter = new GroupInviteAdapter(this,
				R.layout.item_friend_select);
		friendToInviteList.setAdapter(contactListAdapter);
		friendToInviteList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		if (!contactListAdapter.isEmpty()) {
			friendToInviteEditText
					.addTextChangedListener(new friendFiltering());
		}
	}

	private class friendFiltering implements TextWatcher {
		@Override
		public void afterTextChanged(Editable arg0) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			contactListAdapter.getFilter().filter(s);
		}
	}

	private void sendInvitation() {
		if (contactListAdapter != null) {
			Vector<String> buffNum = contactListAdapter.getBuffName();
			if (buffNum.size() > 0) {
				for (int a = 0; a < buffNum.size(); a++) {
					try {
						String friendToInvite = buffNum.get(a);
						MUCManager.getInstance().invite(myAccount, groupName,
								friendToInvite);
					} catch (NetworkException e) {
					}
				}
			} else
				Log.e(TAG, " ::: buffNum == 0");
		} else
			Log.e(TAG, " ::: contactListAdapter == null");
		if (waitDialog.isShowing())
			waitDialog.dismiss();
		finish();
	}

}
