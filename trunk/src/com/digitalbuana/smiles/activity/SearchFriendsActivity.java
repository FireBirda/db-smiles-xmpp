package com.digitalbuana.smiles.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.AddFriendsAdapter.OnAddFriendsListener;
import com.digitalbuana.smiles.awan.adapters.SearchListAdapter;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.FriendsModel;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;

public class SearchFriendsActivity extends ManagedActivity implements
		OnClickListener, OnAddFriendsListener {

	private ImageButton goSearchButton;
	private EditText friendToInviteEditText;
	private String TAG = getClass().getSimpleName();
	private Dialog loadingDialog;
	private TextView inviteHeaderTItle, doneTextButton;
	private ArrayList<String> resultList;
	private ListView friendToInviteList;
	private String userJIDtoAdd = "";
	private FrameLayout inviteBtnBack;
	private LinearLayout rootView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waiting_to_invite);
		friendToInviteEditText = (EditText) findViewById(R.id.friendToInviteEditText);
		goSearchButton = (ImageButton) findViewById(R.id.goSearchButton);
		inviteHeaderTItle = (TextView) findViewById(R.id.inviteHeaderTItle);
		doneTextButton = (TextView) findViewById(R.id.doneTextButton);
		friendToInviteList = (ListView) findViewById(R.id.friendToInviteList);
		inviteBtnBack = (FrameLayout) findViewById(R.id.inviteBtnBack);
		rootView = (LinearLayout) findViewById(R.id.inviteParentView);
		inviteBtnBack.setOnClickListener(this);
		doneTextButton.setVisibility(View.GONE);
		inviteHeaderTItle.setText(R.string.addFriendsTitle2);
		goSearchButton.setOnClickListener(this);
		goSearchButton.setVisibility(View.VISIBLE);
		loadingDialog = com.digitalbuana.smiles.awan.helper.ScreenHelper
				.getDialogProgress(this);
		FontUtils.setRobotoFont(context, rootView);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == goSearchButton) {
			if (!friendToInviteEditText.getText().toString().equals(""))
				getFriendSearchList();
			else
				Toast.makeText(this, "Please input a Text to search",
						Toast.LENGTH_LONG).show();
		} else if (v == inviteBtnBack) {
			finish();
		}
	}

	private void getFriendSearchList() {
		loadingDialog.show();
		new Thread() {
			public void run() {
				Connection connection = AccountManager.getInstance()
						.getActiveAccount().getConnectionThread()
						.getXMPPConnection();
				UserSearchManager search = new UserSearchManager(connection);
				try {
					Form searchForm = search
							.getSearchForm(AppConstants.XMPPSearchAPI);
					Form answerForm = searchForm.createAnswerForm();
					answerForm.setAnswer("Username", true);
					answerForm.setAnswer("Name", true);
					answerForm.setAnswer("Email", true);
					answerForm.setAnswer("search", friendToInviteEditText
							.getText().toString());
					ReportedData data = search.getSearchResults(answerForm,
							AppConstants.XMPPSearchAPI);
					if (data.getRows() != null) {
						resultList = new ArrayList<String>();
						Iterator<Row> it = data.getRows();
						while (it.hasNext()) {
							Row row = it.next();
							Iterator iterator = row.getValues("jid");
							if (iterator.hasNext()) {
								String value = StringUtils
										.replaceStringEquals(iterator.next()
												.toString());
								if (!value.equals(AccountManager.getInstance()
										.getActiveAccount().getAccount()))
									resultList.add(value);
							}
						}
					}
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					Log.e(TAG, e.getMessage());
				} catch (NullPointerException e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(SearchFriendsActivity.this,
									"Empty Search Result", Toast.LENGTH_LONG)
									.show();
						}
					});
				} finally {
					this.interrupt();
					if (loadingDialog.isShowing())
						loadingDialog.dismiss();

					runOnUiThread(new Runnable() {
						public void run() {
							if (resultList != null && !resultList.isEmpty()) {
								SearchListAdapter sla = new SearchListAdapter(
										SearchFriendsActivity.this,
										R.layout.adapter_addfriends,
										resultList, SearchFriendsActivity.this);
								friendToInviteList.setAdapter(sla);
							} else {
								Toast.makeText(SearchFriendsActivity.this,
										"Empty Search Result",
										Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			}
		}.start();
	}

	@Override
	protected void finishAsync(String result) {
		// TODO Auto-generated method stub
		super.finishAsync(result);
		if (urlKu == AppConstants.APIAddFriends) {
			if (result.length() >= 5) {
				doAddFriends();
			}
		}
	}

	@Override
	public void addFriendsWithUsername(String userWithoutJID) {
		// TODO Auto-generated method stub
		ArrayList<FriendsModel> list = FriendsManager.getInstance()
				.getFriendsListManager().getAllFriends();
		boolean found = false;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getName().equals(userWithoutJID)) {
				found = true;
			}
		}
		if (!found) {
			userJIDtoAdd = userWithoutJID + "@" + AppConstants.XMPPServerHost;
			List<NameValuePair> postDatanya = new ArrayList<NameValuePair>(2);
			postDatanya.add(new BasicNameValuePair("username", AccountManager
					.getInstance().getAccountKu()));
			postDatanya
					.add(new BasicNameValuePair("friendname", userWithoutJID));
			doPostAsync(this, AppConstants.APIAddFriends, postDatanya, null,
					true);
		} else {
			Toast.makeText(this, "Already Friends with " + userWithoutJID,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void doAddFriends() {
		AccountItem accountItem = AccountManager.getInstance()
				.getActiveAccount();
		Log.i(TAG, "Adding : " + userJIDtoAdd);
		ArrayList<String> groups = new ArrayList<String>();
		Collections.sort(groups);
		try {
			PresenceManager.getInstance().requestSubscription(
					accountItem.getAccount(), userJIDtoAdd);
			Toast.makeText(
					context,
					"Success add : "
							+ StringUtils.replaceStringEquals(userJIDtoAdd),
					Toast.LENGTH_SHORT).show();
			FriendsManager.getInstance().getFriendsPenddingHeConfirmManager()
					.addFriendsByJID(userJIDtoAdd);
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			Toast.makeText(
					context,
					"Error add : "
							+ StringUtils.replaceStringEquals(userJIDtoAdd),
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}
}
