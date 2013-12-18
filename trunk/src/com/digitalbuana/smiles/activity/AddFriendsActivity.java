package com.digitalbuana.smiles.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.AddFriendsAdapter;
import com.digitalbuana.smiles.adapter.AddFriendsAdapter.OnAddFriendsListener;
import com.digitalbuana.smiles.awan.activity.InviteFrom;
import com.digitalbuana.smiles.awan.helper.PhoneContactHelper;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.RelatedFriendsManager;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.FriendsModel;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;

@SuppressLint("DefaultLocale")
public class AddFriendsActivity extends ManagedActivity implements
		OnClickListener, OnAddFriendsListener {

	private FrameLayout btnBack, inviteToContactButton, inviteToEmailButton,
			SearchFromServerButton;
	private ProgressBar progressSearch;
	private FrameLayout btnSearch;
	private EditText editKeyword;

	private ArrayList<String> searchResult;
	private AddFriendsAdapter adapterAddFriends;

	private GridViewKu gridSearch;

	private String userJIDtoAdd = "";
	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;
	private String TAG = getClass().getSimpleName();
	private TextView friendRecommenTitle;
	private String tmpRec;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_add_friends);
		friendRecommenTitle = (TextView) findViewById(R.id.friendRecommenTitle);

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		settingsEditor = mSettings.edit();

		rootView = (FrameLayout) findViewById(R.id.addFriendsRootView);

		// Casting
		btnBack = (FrameLayout) findViewById(R.id.addFriendsBtnBack);

		SearchFromServerButton = (FrameLayout) findViewById(R.id.SearchFromServerButton);

		progressSearch = (ProgressBar) findViewById(R.id.addFriendsProgressSearch);
		btnSearch = (FrameLayout) findViewById(R.id.addFriendsBtnSearch);
		editKeyword = (EditText) findViewById(R.id.addFriendsEditKeyword);

		gridSearch = (GridViewKu) findViewById(R.id.addFriendsGridView);

		// Listener
		btnBack.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		SearchFromServerButton.setOnClickListener(this);

		FontUtils.setRobotoFont(context, rootView);

		progressSearch.setVisibility(View.VISIBLE);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				readContacts();
			}
		});
		InputMethodManager imm = (InputMethodManager) context
				.getApplicationContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
		}
		searchResult = RelatedFriendsManager.getInstance().getListRelated();
		adapterAddFriends = new AddFriendsAdapter(context, searchResult, this);
		gridSearch.setAdapter(adapterAddFriends);

		inviteToContactButton = (FrameLayout) findViewById(R.id.inviteToContactButton);
		inviteToEmailButton = (FrameLayout) findViewById(R.id.inviteToEmailButton);

		inviteToContactButton.setOnClickListener(this);
		inviteToEmailButton.setOnClickListener(this);

		new Thread("get contact list...") {
			public void run() {
				try {
					PhoneContactHelper.getContactList(AddFriendsActivity.this);
				} finally {
					this.interrupt();
					// load from last temporary cache
					tmpRec = mSettings.getString(
							getString(R.string.recomended_cache), null);
					if (tmpRec != null)
						parseRecomendedResult(tmpRec);
				}
			}
		}.start();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void searchFrends() {
		InputMethodManager imm = (InputMethodManager) context
				.getApplicationContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
		}
		if (asyncTask != null) {
			asyncTask.cancel(true);
		}
		progressSearch.setVisibility(View.GONE);
		if (editKeyword.getText().toString().length() >= 2) {
			List<NameValuePair> postData = new ArrayList<NameValuePair>();
			postData.add(new BasicNameValuePair("keyword", editKeyword
					.getText().toString()));
			doPostAsync(context, AppConstants.APISearch, postData, null, false);
			progressSearch.setVisibility(View.VISIBLE);
		} else {
			Toast.makeText(context, "Min 2 Char Length..", Toast.LENGTH_SHORT)
					.show();
			editKeyword.startAnimation(shake);
			searchResult = RelatedFriendsManager.getInstance().getListRelated();
			adapterAddFriends.setList(searchResult);
		}
	}

	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		if (progressSearch.getVisibility() == View.VISIBLE
				|| progressSearch != null) {
			progressSearch.setVisibility(View.GONE);
		}
		if (urlKu == AppConstants.APISearch) {
			parseSearchResult(result);
		} else if (urlKu == AppConstants.APIUploadContact) {
			settingsEditor.putString(getString(R.string.recomended_cache),
					result);
			settingsEditor.commit();
			parseRecomendedResult(result);
		} else if (urlKu == AppConstants.APIAddFriends) {
			if (result.length() >= 5) {
				doAddFriends();
			}
		}
	}

	private void parseRecomendedResult(String result) {
		Log.i(TAG, result);
		try {
			JSONObject json = new JSONObject(result);
			JSONArray usersArray = json.optJSONArray("DATA");
			if (usersArray != null) {
				searchResult.clear();
				for (int x = 0; x < usersArray.length(); x++) {
					String nama = usersArray.getJSONObject(x).getString(
							"username");
					searchResult.add(nama);
				}
				adapterAddFriends.setList(searchResult);
				RelatedFriendsManager.getInstance().setBlockedFriends(
						searchResult);
			}
		} catch (Exception e) {

		}
	}

	private void parseSearchResult(String result) {
		try {
			JSONObject json = new JSONObject(result);
			String status = json.getString("STATUS");
			if (status.equals("FAILED")) {
				String message = json.getString("MESSAGE");
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			} else {
				JSONArray usersArray = json.optJSONArray("USERS");
				if (usersArray != null) {
					searchResult.clear();
					for (int x = 0; x < usersArray.length(); x++) {
						String nama = usersArray.getJSONObject(x).getString(
								"username");
						searchResult.add(nama);
					}
					Toast.makeText(context,
							"Found : " + usersArray.length() + " Users",
							Toast.LENGTH_SHORT).show();
					adapterAddFriends.setList(searchResult);
				} else {
					Toast.makeText(context, "No Users Found",
							Toast.LENGTH_SHORT).show();
				}
			}
		} catch (JSONException e) {
			Toast.makeText(context, "Failed to Search..", Toast.LENGTH_SHORT)
					.show();
		}

	}

	public static Intent createIntent(Context context) {
		return new Intent(context, AddFriendsActivity.class);
	}

	@Override
	public void onClick(View v) {

		String tmpRecomendedContact = mSettings.getString(
				getString(R.string.recomended_cache), null);

		if (v == btnBack) {
			finish();
		} else if (v == btnSearch) {
			searchFrends();
		} else if (v == inviteToContactButton) {
			if (tmpRecomendedContact == null) {
				Toast.makeText(context,
						getString(R.string.contact_generated_desclaimer),
						Toast.LENGTH_SHORT).show();
			} else {
				setInvitation(tmpRecomendedContact,
						getString(R.string.addFriendsItem1));
			}
		} else if (v == inviteToEmailButton) {
			if (tmpRecomendedContact == null) {
				Toast.makeText(context,
						getString(R.string.contact_generated_desclaimer),
						Toast.LENGTH_SHORT).show();
			} else {
				setInvitation(tmpRecomendedContact,
						getString(R.string.addFriendsItem2));
			}
		} else if (v == SearchFromServerButton) {
			startActivity(new Intent(this, SearchFriendsActivity.class));
		}
	}

	private void setInvitation(String tmpJson, String type) {
		Intent i = new Intent(this, InviteFrom.class);
		i.putExtra("json", tmpJson);
		i.putExtra("type", type);
		startActivity(i);
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private void readContacts() {

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String countryCode = tm.getSimCountryIso();
		String getCountryRaw = readRaw(context, R.raw.countrycode);
		String getCountryCode = parseCountryCodeXml(getCountryRaw,
				countryCode.toUpperCase());
		if (getCountryCode == null) {
			getCountryCode = "62";
		}

		String apiJsonTemplate = "{\"t\":[phonecontainer],\"e\":[mailcontainer]}";

		/*
		 * load phone contact cache
		 */
		Set<String> phoneList = mSettings.getStringSet(
				AppConstants.LOCAL_PHONE_CONTACT_CACHE_TAG, null);

		ArrayList<String> contactTelphone = new ArrayList<String>();

		if (phoneList == null)
			phoneList = PhoneContactHelper.tmpSetPhone;
		if (phoneList != null) {
			StringBuilder sb = new StringBuilder();

			for (String phone : phoneList) {

				if (phone.substring(0, 1).equals("0"))
					phone = getCountryCode + phone.substring(1);

				if (phone.contains(",")) {
					String[] splitNum = phone.split(",");
					if (splitNum.length > 0) {
						for (int a = 0; a < splitNum.length; a++) {
							sb.append("\"" + splitNum[a] + "\"").append(",");
							contactTelphone.add(splitNum[a]);
						}
					}
				} else {
					sb.append("\"" + phone + "\"").append(",");
					contactTelphone.add(phone);
				}

			}
			RelatedFriendsManager.getInstance().setNumberContact(
					contactTelphone.size());
			String tempPostUn = sb.toString().substring(0,
					sb.toString().length() - 1);
			apiJsonTemplate = apiJsonTemplate.replace("phonecontainer",
					tempPostUn);
		}

		/*
		 * load mail contact cache
		 */
		Set<String> mailList = mSettings.getStringSet(
				AppConstants.LOCAL_MAIL_CONTACT_CACHE_TAG, null);
		if (mailList == null)
			mailList = PhoneContactHelper.tmpSetMail;
		if (mailList != null) {
			StringBuilder sb2 = new StringBuilder();

			for (String mail : mailList) {
				if (mail != null && !mail.trim().equals(""))
					sb2.append("\"" + mail.trim() + "\"").append(",");
			}

			String tempPostUn = "";

			if (!sb2.toString().isEmpty())
				tempPostUn = sb2.toString().substring(0,
						sb2.toString().length() - 1);

			apiJsonTemplate = apiJsonTemplate.replace("mailcontainer",
					tempPostUn);
		}

		final String tempPost = apiJsonTemplate;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
				String username = AccountManager.getInstance()
						.getActiveAccount().getAccount();
				postData.add(new BasicNameValuePair("username", username));
				postData.add(new BasicNameValuePair("list", tempPost));
				doPostAsync(context, AppConstants.APIUploadContact, postData,
						null, false);
			}
		});
	}

	public static String readRaw(Context c, int resId) {
		InputStream raw = c.getResources().openRawResource(resId);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try {
			i = raw.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = raw.read();
			}
			raw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArrayOutputStream.toString();
	}

	public static String parseCountryCodeXml(String xmlData, String countryCode) {
		if (countryCode == null || countryCode.equals(""))
			return null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlData));
			Document doc = docBuilder.parse(is);
			NodeList nodes = doc.getElementsByTagName("icc");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node firstNode = nodes.item(i);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) firstNode;
					NodeList firstList = element
							.getElementsByTagName(countryCode);
					Element firstElement = (Element) firstList.item(0);
					if (firstElement.getChildNodes() == null)
						return null;
					NodeList textFNList = firstElement.getChildNodes();
					return ((Node) textFNList.item(0)).getNodeValue().trim();
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;

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

	@Override
	public void addFriendsWithUsername(String userWithoutJID) {
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
			doPostAsync(context, AppConstants.APIAddFriends, postDatanya, null,
					true);
		} else {
			Toast.makeText(context, "Already Friends with " + userWithoutJID,
					Toast.LENGTH_SHORT).show();
		}
	}
}
