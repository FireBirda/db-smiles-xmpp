package com.digitalbuana.smiles.awan.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.activity.ProfileDetailActivity;
import com.digitalbuana.smiles.awan.adapters.InviteAdapter;
import com.digitalbuana.smiles.awan.helper.PhoneContactHelper;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.awan.model.Contact;
import com.digitalbuana.smiles.awan.model.RecomendedModel;
import com.digitalbuana.smiles.awan.stores.RecomendedStore;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountManager;

public class InviteFrom extends Activity implements OnClickListener {

	private String tmpJson = null;
	private String type = null;
	private ArrayList<Contact> contactListArr;
	private String TAG = getClass().getSimpleName();
	private InviteAdapter ia = null;
	private ListView contactList;
	private String[][] tmpList;
	private Button finishButton;
	private Vector<String> buffNum = null;
	private AQuery aq;
	private FrameLayout inviteBtnBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);

		aq = new AQuery(this);

		setContentView(R.layout.general_listview);
		inviteBtnBack = (FrameLayout) findViewById(R.id.inviteBtnBack);
		inviteBtnBack.setOnClickListener(this);

		contactList = (ListView) findViewById(R.id.search_result_list);
		finishButton = (Button) findViewById(R.id.finishButton);
		finishButton.setOnClickListener(this);
		finishButton.setVisibility(View.VISIBLE);

		Intent i = getIntent();
		tmpJson = i.getStringExtra("json");
		type = i.getStringExtra("type");

		if (PhoneContactHelper.tmpContactList.length > 0) {
			contactListArr = PhoneContactHelper.getContactArr();
			generateContactList();
		} else {
			Toast.makeText(this,
					getString(R.string.failed_to_get_contact_list),
					Toast.LENGTH_LONG).show();
		}

	}

	private void generateContactList() {

		RecomendedStore rs;

		try {

			rs = new RecomendedStore(new JSONObject(tmpJson));
			ArrayList<RecomendedModel> arrContact = rs.getList();
			ArrayList<RecomendedModel> finalContact = new ArrayList<RecomendedModel>();

			for (RecomendedModel contact : arrContact) {
				if (type.equals(getString(R.string.addFriendsItem1))) {
					// contact
					if (contact.getMsisdn() != null
							&& !contact.getMsisdn().trim().equals("")) {
						RecomendedModel c = new RecomendedModel();
						c.setUserName(contact.getUserName());
						c.setMsisdn(contact.getMsisdn());
						finalContact.add(c);
					}

				} else if (type.equals(getString(R.string.addFriendsItem2))) {
					// email

					if (contact.getMail() != null
							&& !contact.getMail().trim().equals("")) {
						RecomendedModel c = new RecomendedModel();
						c.setUserName(contact.getUserName());
						c.setMsisdn(contact.getMail());
						finalContact.add(c);
					}
				}
			}

			tmpList = PhoneContactHelper.tmpContactList;

			if (type.equals(getString(R.string.addFriendsItem2))) {
				int tmpCounter = 0;
				for (int z = 0; z < tmpList[0].length; z++) {
					if (tmpList[2][z] != null && !tmpList[2][z].equals("")) {
						tmpCounter++;
					}
				}
				String[][] finalContactList = new String[3][tmpCounter];
				int yaitu = 0;
				for (int z = 0; z < tmpList[0].length; z++) {
					if (tmpList[2][z] != null && !tmpList[2][z].equals("")) {
						finalContactList[0][yaitu] = tmpList[0][z];
						finalContactList[1][yaitu] = tmpList[1][z];
						finalContactList[2][yaitu] = tmpList[2][z];
						yaitu++;
					}
				}
				tmpList = finalContactList;
			}
			ia = new InviteAdapter(this, tmpList, finalContact, type);
			contactList.setAdapter(ia);
			final ArrayList<RecomendedModel> _finalContact = finalContact;
			contactList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					String phoneNum = tmpList[1][arg2];
					String mail = tmpList[2][arg2];
					if (type.equals(getString(R.string.addFriendsItem1))) {
						for (RecomendedModel rm : _finalContact) {
							if (rm.getMsisdn().equals(phoneNum)) {
								goToProfileActivity(rm.getUserName());
							}
						}
					} else if (type.equals(getString(R.string.addFriendsItem2))) {
						for (RecomendedModel rm : _finalContact) {
							if (rm.getMail().equals(mail)) {
								goToProfileActivity(rm.getUserName());
							}
						}
					}
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}

	private void goToProfileActivity(String friend) {
		startActivity(ProfileDetailActivity.createIntent(this, AccountManager
				.getInstance().getAccountKu(), friend));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == finishButton) {
			if (ia != null) {
				buffNum = ia.getBuff();
				Map<String, String> parm = new HashMap<String, String>();
				parm.put("id", "5");
				aq.progress(ScreenHelper.getDialogProgress(this)).ajax(
						AppConstants.APIUserGetText, parm, JSONObject.class,
						new AjaxCallback<JSONObject>() {
							public void callback(String url, JSONObject object,
									AjaxStatus status) {
								// TODO Auto-generated method stub
								try {
									smsInvite(object.getString("CONTENT"));
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									// e.printStackTrace();
									Log.e(TAG, e.getMessage());
								}
							}
						});

			} else {
				Toast.makeText(this, getString(R.string.no_contact_selected),
						Toast.LENGTH_SHORT).show();
			}
		} else if (v == inviteBtnBack) {
			finish();
		}
	}

	private void smsInvite(String text) {
		String buffString = "";
		for (int a = 0; a < buffNum.size(); a++) {
			Log.i(TAG, "a:" + a + ", num:" + buffNum.get(a));
			if (buffString.equals(""))
				buffString = buffNum.get(a);
			else
				buffString = buffString + ";" + buffNum.get(a);
		}
		if (!buffString.equals("")) {
			if (type.equals(getString(R.string.addFriendsItem1))) {
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
				sendIntent.putExtra("address", buffString);
				sendIntent.putExtra("sms_body", text);
				sendIntent.setType("vnd.android-dir/mms-sms");
				startActivity(sendIntent);
			} else if (type.equals(getString(R.string.addFriendsItem2))) {
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("mailto:" + buffString));
				intent.putExtra(Intent.EXTRA_SUBJECT,
						getString(R.string.mail_invite_subject));
				intent.putExtra(Intent.EXTRA_TEXT, text);
				startActivity(intent);
			}
		}
		finish();
	}
}
