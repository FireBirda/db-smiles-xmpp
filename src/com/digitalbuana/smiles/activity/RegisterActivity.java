package com.digitalbuana.smiles.activity;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;
import com.digitalbuana.smiles.GCMIntentService;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.helper.PhoneContactHelper;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.RelatedFriendsManager;
import com.digitalbuana.smiles.dialog.SmilesDefaultDialog;
import com.digitalbuana.smiles.dialog.SmilesProgressDialog;
import com.digitalbuana.smiles.ui.helper.BaseUIActivity;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.RecentUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;

@SuppressLint({ "NewApi", "DefaultLocale" })
public class RegisterActivity extends BaseUIActivity implements OnClickListener {
	AsyncTask<Void, Void, Void> mRegisterTask;

	private SharedPreferences appPreference;

	// Code
	private FrameLayout containerWidth;
	private FrameLayout btnShowTerms;
	private FrameLayout btnNext;
	private EditText editCountry;
	private EditText editPhone;
	private CheckBox checkTerms;

	// Terms
	private FrameLayout containerBlackTerms;
	private FrameLayout containerTerms;
	private WebView webTerms;
	private FrameLayout btnHideTerms;

	// Verification
	private ProgressBar veriLoading;
	private CountDownTimer waitVeriTimer;
	private final static int veriCountdownTimerMax = 61;
	private int veriCountdownTimer = veriCountdownTimerMax;
	private boolean isCanSendSMS = true;
	private FrameLayout conteinerBlackVeri;
	private FrameLayout btnVeriResend;
	private FrameLayout btnVeriConfirm;
	private EditText editVeriCode;

	// Verification
	private FrameLayout conteinerBlackID;
	private EditText editID;
	private EditText editEmail;
	private EditText editFullName;
	private RadioGroup radioSex;
	private EditText editPassword;
	private EditText editPasswordConfirm;
	private CheckBox checkShowPass;
	private FrameLayout btnDoneAccount;

	private static String postPhoneNumber = "";
	private static String contryCode;
	private static String phoneNum;

	private static String regID = "";
	private static String verifyCode = "";
	private static boolean isGetVeri = true;
	private Spinner countryName;

	private String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	private String[] countryNameList = null;
	private String[] countryCodeList = null;
	private String[] countryIso = null;

	private String countryCode;

	private String TAG = getClass().getSimpleName();
	private AQuery aq;
	private SharedPreferences.Editor editor;

	@SuppressLint("DefaultLocale")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing())
			return;

		aq = new AQuery(this);

		setContentView(R.layout.activity_register);
		countryName = (Spinner) findViewById(R.id.country_name_spinner);
		rootView = (FrameLayout) findViewById(R.id.registerRootView);

		// Casting
		containerWidth = (FrameLayout) findViewById(R.id.registerContainerNumber);
		btnShowTerms = (FrameLayout) findViewById(R.id.registerBtnShowTerms);
		btnNext = (FrameLayout) findViewById(R.id.registerBtnNext);
		editCountry = (EditText) findViewById(R.id.registerEditCountry);
		editPhone = (EditText) findViewById(R.id.registerEditPhone);
		checkTerms = (CheckBox) findViewById(R.id.registerCheckTerms);

		containerBlackTerms = (FrameLayout) findViewById(R.id.registerContainerBlackTerms);
		containerTerms = (FrameLayout) findViewById(R.id.registerContainerTerms);
		webTerms = (WebView) findViewById(R.id.registerWebTerms);
		btnHideTerms = (FrameLayout) findViewById(R.id.registerBtnHideTerms);

		veriLoading = (ProgressBar) findViewById(R.id.registerVerificationLoading);
		conteinerBlackVeri = (FrameLayout) findViewById(R.id.registerContainerBlackVeri);
		btnVeriResend = (FrameLayout) findViewById(R.id.registerVerificationBtnResend);
		btnVeriConfirm = (FrameLayout) findViewById(R.id.registerVerificationBtnConfirm);
		editVeriCode = (EditText) findViewById(R.id.registerVerificationEditNumber);

		conteinerBlackID = (FrameLayout) findViewById(R.id.registerContainerBlackID);
		editID = (EditText) findViewById(R.id.registerIDEditID);
		editEmail = (EditText) findViewById(R.id.registerIDEditEmail);
		editFullName = (EditText) findViewById(R.id.registerIDEditFullName);
		radioSex = (RadioGroup) findViewById(R.id.registerIDRadioSex);
		editPassword = (EditText) findViewById(R.id.registerIDEditPassword);
		editPasswordConfirm = (EditText) findViewById(R.id.registerIDEditPasswordConfirm);
		checkShowPass = (CheckBox) findViewById(R.id.registerIDCheckShowPassword);
		btnDoneAccount = (FrameLayout) findViewById(R.id.registerIDBtnDone);

		editID.addTextChangedListener(new TextWatcher() {
			@SuppressLint("DefaultLocale")
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				String currentText = s.toString();
				if (currentText.contains(" ")) {
					Toast.makeText(RegisterActivity.this,
							getString(R.string.space_on_diclaimer),
							Toast.LENGTH_LONG).show();
					currentText = currentText.replace(" ", "").toLowerCase();
					editID.setText(currentText);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		editPhone.addTextChangedListener(new TextWatcher() {

			@SuppressLint("DefaultLocale")
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				String currentText = s.toString();
				if (currentText.contains(" ")) {
					currentText = currentText.replace(" ", "").toLowerCase();
					editPhone.setText(currentText);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		editID.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (!hasFocus) {
					String lastChar = editID.getText().toString().trim()
							.toLowerCase();
					editID.setText(lastChar);
				}
			}
		});

		// Anim
		FontUtils.setRobotoFont(context, rootView);

		// Adding To Listener
		btnNext.setOnClickListener(this);
		btnShowTerms.setOnClickListener(this);
		btnHideTerms.setOnClickListener(this);
		btnVeriResend.setOnClickListener(this);
		btnVeriConfirm.setOnClickListener(this);
		containerBlackTerms.setOnClickListener(this);
		btnDoneAccount.setOnClickListener(this);
		checkShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					editPassword.setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					editPasswordConfirm.setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
				} else {
					editPassword
							.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
									| InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					editPasswordConfirm
							.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
									| InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
				}
			}
		});

		// Animating
		final ImageView imgAnim = (ImageView) findViewById(R.id.registerVerificationImgAnim);
		final ImageView loadingTerms = (ImageView) findViewById(R.id.registerLoadingTerms);

		imgAnim.setBackgroundResource(R.drawable.kakatua_kedip_anim);
		loadingTerms.setBackgroundResource(R.drawable.kakatua_junior_anim);
		rootView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) imgAnim
						.getBackground();
				anim.start();
				AnimationDrawable anim2 = (AnimationDrawable) loadingTerms
						.getBackground();
				anim2.start();
			}
		});

		appPreference = PreferenceManager
				.getDefaultSharedPreferences(Application.getInstance()
						.getApplicationContext());
		editor = appPreference.edit();

		IntentFilter SMSfilter = new IntentFilter(SMS_RECEIVED);
		this.registerReceiver(SMSbr, SMSfilter);
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		countryCode = tm.getSimCountryIso();
		getCountry();
		PhoneContactHelper.getSetLocalContact(this);
	}

	private void getCountry() {
		String countryList = appPreference.getString(
				getString(R.string.country_code_cache_key), null);
		if (countryList == null) {
			aq.progress(ScreenHelper.getDialogProgress(this)).ajax(
					AppConstants.countryCodeUrl, XmlDom.class, this,
					"countryCodeResult");
		}
	}

	public void countryCodeResult(String url, XmlDom xml, AjaxStatus status) {

		if (xml != null) {

			String countryList = xml.toString();

			try {

				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory
						.newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(countryList));
				Document doc = docBuilder.parse(is);
				NodeList nodes = doc.getElementsByTagName("ICC");

				for (int i = 0; i < nodes.getLength(); i++) {

					Node firstNode = nodes.item(i);

					if (firstNode.getNodeType() == Node.ELEMENT_NODE) {

						Element element = (Element) firstNode;

						NodeList allList = element.getChildNodes();
						int allListLength = allList.getLength();

						if (allListLength > 0) {

							countryNameList = new String[allListLength];
							countryCodeList = new String[allListLength];
							countryIso = new String[allListLength];
						}

						// String nodeName = element.getNodeName();
						String name = null, code = null;

						for (int k = 0; k < allListLength; k++) {
							Node AllChildNode = allList.item(k);
							Element allCountryElement = (Element) allList
									.item(k);
							if (AllChildNode.getNodeType() == Node.ELEMENT_NODE) {
								NodeList allNameList = allCountryElement
										.getElementsByTagName("NAME");
								NodeList allCodeList = allCountryElement
										.getElementsByTagName("PREFIX");

								Element allFirstNamex = (Element) allNameList
										.item(0);
								NodeList allTextName = allFirstNamex
										.getChildNodes();

								Element allCodeNamex = (Element) allCodeList
										.item(0);
								NodeList allCodeName = allCodeNamex
										.getChildNodes();

								name = ((Node) allTextName.item(0))
										.getNodeValue().trim();
								code = ((Node) allCodeName.item(0))
										.getNodeValue().trim();

								countryNameList[k] = name;
								countryCodeList[k] = code;
								countryIso[k] = AllChildNode.getNodeName();

							}
						}

						ArrayAdapter adapter = new ArrayAdapter(
								RegisterActivity.this,
								android.R.layout.simple_spinner_item,
								countryNameList);
						countryName.setAdapter(adapter);
						countryName
								.setOnItemSelectedListener(new OnItemSelectedListener() {

									@Override
									public void onItemSelected(
											AdapterView<?> arg0, View arg1,
											int arg2, long arg3) {
										// TODO Auto-generated method stub
										editCountry
												.setText(countryCodeList[arg2]);
										editPhone.requestFocus();
									}

									@Override
									public void onNothingSelected(
											AdapterView<?> arg0) {
										// TODO Auto-generated method stub

									}
								});
						for (int z = 0; z < countryIso.length; z++) {
							if (countryIso[z].equals(countryCode.toUpperCase())) {
								countryName.setSelection(z);
								editCountry.setText(countryCodeList[z]);
								editPhone.requestFocus();
							}
						}

					}
				}

			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			}
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(SMSbr);
		super.onDestroy();
	}

	private void setLastView(int index) {
		editor.putInt("INDEX_VIEW_REG", index);
		editor.commit();
	}

	private int getLastView() {
		return appPreference.getInt("INDEX_VIEW_REG", 0);
	}

	private void setIsVeri(boolean index) {
		editor.putBoolean("INDEX_VIEW_BOOL", index);
		editor.commit();
	}

	private boolean getIsVeri() {
		return appPreference.getBoolean("INDEX_VIEW_BOOL", false);
	}

	private void setCountryCode(String string) {
		editor.putString("INDEX_VIEW_COUNTRY", string);
		editor.commit();
	}

	private String getCountryCode() {
		return appPreference.getString("INDEX_VIEW_COUNTRY", "");
	}

	private void setPhoneNumber(String string) {

		editor.putString("INDEX_VIEW_PHONE", string);
		editor.commit();
	}

	private String getPhoneNumber() {
		return appPreference.getString("INDEX_VIEW_PHONE", "");
	}

	@Override
	protected void onResume() {
		super.onResume();
		hideKeyboard();
		isCanSendSMS = true;
		editCountry.setText(getCountryCode());
		editPhone.setText(getPhoneNumber().trim());
		postPhoneNumber = getCountryCode() + getPhoneNumber();
		verifyCode = "";
		if (getIsVeri()) {
			conteinerBlackVeri.setVisibility(View.GONE);
			containerBlackTerms.setVisibility(View.GONE);
			conteinerBlackID.setVisibility(View.VISIBLE);
			editID.requestFocus();
		}
		switch (getLastView()) {
		case 0:
			containerBlackTerms.setVisibility(View.GONE);
			conteinerBlackVeri.setVisibility(View.GONE);
			conteinerBlackID.setVisibility(View.GONE);
			editCountry.requestFocus();
			break;
		case 1:
			conteinerBlackVeri.setVisibility(View.VISIBLE);
			conteinerBlackID.setVisibility(View.GONE);
			containerBlackTerms.setVisibility(View.GONE);
			editVeriCode.requestFocus();
			break;
		case 2:
			conteinerBlackVeri.setVisibility(View.GONE);
			containerBlackTerms.setVisibility(View.GONE);
			conteinerBlackID.setVisibility(View.VISIBLE);
			editID.requestFocus();
			break;
		}
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) context
				.getApplicationContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
		}
	}

	private void showTerms() {
		hideKeyboard();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", "6");
		aq.progress(ScreenHelper.getDialogProgress(this)).ajax(
				AppConstants.APIUserGetText, params, JSONObject.class,
				new AjaxCallback<JSONObject>() {
					@Override
					public void callback(String url, JSONObject json,
							AjaxStatus status) {

						if (json != null) {
							try {
								String apiContent = json.getString("CONTENT");
								containerBlackTerms.setVisibility(View.VISIBLE);
								containerBlackTerms.startAnimation(fadein);
								webTerms.loadData(apiContent, "text/html",
										"UTF-8");
								webTerms.setWebViewClient(new WebViewClient() {
									ImageView loadingTerms = (ImageView) rootView
											.findViewById(R.id.registerLoadingTerms);

									@Override
									public void onPageFinished(WebView view,
											String url) {
										loadingTerms.setVisibility(View.GONE);
									}

									@Override
									public void onReceivedError(WebView view,
											int errorCode, String description,
											String failingUrl) {
										loadingTerms.setVisibility(View.GONE);
									}

									@Override
									public void onPageStarted(WebView view,
											String url, Bitmap favicon) {
										loadingTerms
												.setVisibility(View.VISIBLE);
									}
								});
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
								Log.e(TAG, e.getMessage());
							}
						}
					}
				});
	}

	public void ApiTermCallBack(String url, JSONObject json, AjaxStatus status) {
		if (json != null) {
			try {
				String apiContent = json.getString("CONTENT");
				containerBlackTerms.setVisibility(View.VISIBLE);
				containerBlackTerms.startAnimation(fadein);
				webTerms.loadData(apiContent, "text/html", "UTF-8");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				Log.e(TAG, e.getMessage());
			}
		}
	}

	private void hideTerms() {
		containerBlackTerms.setVisibility(View.GONE);
		containerBlackTerms.startAnimation(fadeout);
	}

	private void doConfirmVeri() {
		if (editVeriCode.getText().toString().equals(verifyCode)) {
			hideVeri();
			showAccount();
			setIsVeri(true);
		} else {
			postCompareVeri();
		}
	}

	private void postCompareVeri() {
		isGetVeri = false;
		postPhoneNumber = getCountryCode() + getPhoneNumber();
		List<NameValuePair> postData = new ArrayList<NameValuePair>(1);
		postData.add(new BasicNameValuePair("phone", postPhoneNumber));
		postData.add(new BasicNameValuePair("do_verify", "1"));
		doPostAsync(context, AppConstants.APIGetVrifySMS, postData, null, true);
	}

	private void postGetVeri() {
		isGetVeri = true;
		veriLoading.setVisibility(View.VISIBLE);
		isCanSendSMS = false;
		veriCountdownTimer = veriCountdownTimerMax;
		btnVeriResend.setEnabled(false);
		if (waitVeriTimer != null) {
			waitVeriTimer.cancel();
			waitVeriTimer = null;
		}
		waitVeriTimer = new CountDownTimer(veriCountdownTimerMax * 1000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				veriCountdownTimer = (int) (millisUntilFinished / 1000);
				TextView textresend = (TextView) findViewById(R.id.registerVerificationBtnResendTxt);
				textresend.setText("RESEND ( " + veriCountdownTimer + " )");
			}

			@Override
			public void onFinish() {
				veriLoading.setVisibility(View.GONE);
				isCanSendSMS = true;
				btnVeriResend.setEnabled(true);
				TextView textresend = (TextView) findViewById(R.id.registerVerificationBtnResendTxt);
				textresend.setText("RESEND");
			}
		}.start();
		boolean vailid = true;
		if (editCountry.getText().length() < 2) {
			vailid = false;
			editCountry.startAnimation(shake);
			editCountry.requestFocus();
		}
		if (editPhone.getText().toString().trim().length() <= 6 && vailid) {
			vailid = false;
			editPhone.startAnimation(shake);
			editPhone.requestFocus();
		}
		if (!checkTerms.isChecked() && vailid) {
			InputMethodManager imm = (InputMethodManager) context
					.getApplicationContext().getSystemService(
							Context.INPUT_METHOD_SERVICE);
			if (imm.isActive()) {
				imm.hideSoftInputFromWindow(editPhone.getWindowToken(), 0);
			}
			vailid = false;
			checkTerms.startAnimation(shake);
			checkTerms.requestFocus();
		}
		if (vailid) {
			postPhoneNumber = "";
			contryCode = editCountry.getText().toString();
			phoneNum = editPhone.getText().toString().trim();
			contryCode = contryCode.replaceAll("[^\\d.]", "");
			phoneNum = phoneNum.replaceAll("[^\\d.]", "");
			if (phoneNum != null) {
				String a_char = "" + phoneNum.charAt(0);
				if (a_char.equals("0")) {
					phoneNum = phoneNum.substring(1, phoneNum.length());
				}
			}
			postPhoneNumber = contryCode + phoneNum;
			List<NameValuePair> postData = new ArrayList<NameValuePair>(1);
			postData.add(new BasicNameValuePair("phone", postPhoneNumber));
			doPostAsync(context, AppConstants.APIGetVrifySMS, postData, null,
					true);
		}

	}

	private void postCreateAccount() {
		boolean isMale = true;
		if (radioSex.getCheckedRadioButtonId() == R.id.registerIDRadioOption1) {
			isMale = true;
		} else {
			isMale = false;
		}
		boolean isValid = true;
		if (editID.getText().length() < 5) {
			isValid = false;
			editID.startAnimation(shake);
			editID.requestFocus();
		}
		if (editEmail.getText().length() >= 5) {
			String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
			CharSequence inputStr = editEmail.getText().toString();
			Pattern pattern = Pattern.compile(expression,
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(inputStr);
			if (!matcher.matches()) {
				isValid = false;
				editEmail.startAnimation(shake);
				editEmail.requestFocus();
			}
		}
		if (editEmail.getText().length() <= 5) {
			isValid = false;
			editEmail.startAnimation(shake);
			editEmail.requestFocus();
		}
		if (editFullName.getText().length() < 5) {
			isValid = false;
			editFullName.startAnimation(shake);
			editFullName.requestFocus();
		}
		if (editPassword.getText().length() < 5) {
			isValid = false;
			editPassword.startAnimation(shake);
			editPassword.requestFocus();
		}
		if (editPasswordConfirm.getText().length() < 5) {
			isValid = false;
			editPasswordConfirm.startAnimation(shake);
			editPasswordConfirm.requestFocus();
		}
		if (!editPasswordConfirm.getText().toString()
				.equals(editPassword.getText().toString())
				&& editPassword.getText().length() >= 5
				&& editPasswordConfirm.getText().length() >= 5) {
			isValid = false;
			editPasswordConfirm.startAnimation(shake);
			editPasswordConfirm.requestFocus();
		}
		if (editID.getText().toString().trim().contains(" ")) {
			editID.startAnimation(shake);
			String currentText = editID.getText().toString().trim();
			currentText = currentText.replace(" ", "").toLowerCase();
			editID.setText(currentText);
			editID.requestFocus();
			isValid = false;
		}
		if (isValid) {
			final SmilesProgressDialog dialog = new SmilesProgressDialog(
					context, null);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			isTryPost = false;

			final List<NameValuePair> postData = new ArrayList<NameValuePair>(7);

			postData.add(new BasicNameValuePair("username", editID.getText()
					.toString()));
			postData.add(new BasicNameValuePair("email", editEmail.getText()
					.toString()));
			postData.add(new BasicNameValuePair("fullname", editFullName
					.getText().toString()));
			postData.add(new BasicNameValuePair("password", editPassword
					.getText().toString()));
			postData.add(new BasicNameValuePair("phone", postPhoneNumber));
			postData.add(new BasicNameValuePair("country", countryCode
					.toUpperCase()));

			if (isMale) {
				postData.add(new BasicNameValuePair("gender", "pria"));
			} else {
				postData.add(new BasicNameValuePair("gender", "wanita"));
			}
			// IMEI
			String deviceImei = RecentUtils.getImei();
			postData.add(new BasicNameValuePair("imei", deviceImei));

			regID = RecentUtils.getRegID();
			if (regID.equals("")) {
				new CountDownTimer(GCMIntentService.BACKOFF_MILLI_SECONDS
						* GCMIntentService.MAX_ATTEMPTS,
						GCMIntentService.BACKOFF_MILLI_SECONDS) {
					@Override
					public void onTick(long millisUntilFinished) {
						regID = RecentUtils.getRegID();
						if (!regID.equals("")) {
							this.cancel();
						}
					}

					@Override
					public void onFinish() {
						if (regID.equals("")) {
							Toast.makeText(context, "Failed to get RegID..",
									Toast.LENGTH_SHORT).show();
						} else {
							postData.add(new BasicNameValuePair("regid", regID));
							if (dialog != null) {
								dialog.dismiss();
							}
							if (!isTryPost) {
								isTryPost = true;
								doPostAsync(context, AppConstants.APIRegister,
										postData, null, true);
							}
						}
					}
				}.start();
			} else {
				postData.add(new BasicNameValuePair("regid", regID));
				if (dialog != null) {
					dialog.dismiss();
				}
				if (!isTryPost) {
					isTryPost = true;
					doPostAsync(context, AppConstants.APIRegister, postData,
							null, true);
				}
			}
		}
	}

	private static boolean isTryPost = false;

	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		Log.e(TAG, result);
		if (urlKu == AppConstants.APIGetVrifySMS) {
			try {
				JSONObject jsonKu = new JSONObject(result);
				String status = jsonKu.getString("STATUS").toUpperCase(
						Locale.getDefault());
				if (isGetVeri) {
					String message = jsonKu.getString("MESSAGE").toUpperCase(
							Locale.getDefault());
					if (status.equals("SUCCESS")) {
						conteinerBlackVeri.setVisibility(View.VISIBLE);
						conteinerBlackVeri.startAnimation(fadein);
						verifyCode = jsonKu.getString("CODE").toUpperCase(
								Locale.getDefault());
						setLastView(1);

						SharedPreferences.Editor editor = appPreference.edit();

						setCountryCode(contryCode);
						setPhoneNumber(phoneNum);

						editor.commit();

					} else if (message
							.equals("THIS MOBILE NUMBER IS ALREADY ASSOCIATED WITH AN ACCOUNT")) {
						showAccount();
					} else {
						SmilesDefaultDialog dialog = new SmilesDefaultDialog(
								context, "VERIFICATION ERROR", message);
						dialog.show();
					}
				} else {
					if (status.equals("SUCCESS")) {
						verifyCode = jsonKu.getString("CODE").toUpperCase(
								Locale.getDefault());
						if (editVeriCode.getText().toString()
								.equals(verifyCode)) {
							showAccount();
						} else {
							setLastView(1);
							doConfirmVeri();
						}
					} else {
						editVeriCode.requestFocus();
						editVeriCode.startAnimation(shake);
					}
				}

			} catch (JSONException e) {
				SmilesDefaultDialog dialog = new SmilesDefaultDialog(context,
						"VERIFICATION ERROR", "PLEASE CONTACT SUPPORT");
				dialog.show();
			}
		} else if (urlKu == AppConstants.APIRegister) {
			try {
				JSONObject jsonKu = new JSONObject(result);
				String status = jsonKu.getString("STATUS").toUpperCase(
						Locale.getDefault());
				String message = jsonKu.getString("MESSAGE").toUpperCase(
						Locale.getDefault());
				if (status.equals("SUCCESS")) {
					hideAccount();
					setLastView(0);
					setPhoneNumber("");
					setCountryCode("");
					SmilesDefaultDialog dialog = new SmilesDefaultDialog(
							context, "REGISTRATION SUCCESS", " PLEASE LOGIN ");
					dialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							setIsVeri(false);
							finish();
							startActivity(LoginActivity.createIntent(context));
						}
					});
					dialog.show();
				} else {
					SmilesDefaultDialog dialog = new SmilesDefaultDialog(
							context, "REGISTER ERROR", message);
					dialog.show();
				}
			} catch (JSONException e) {
				SmilesDefaultDialog dialog = new SmilesDefaultDialog(context,
						"REGISTER ERROR", "PLEASE CONTACT SUPPORT");
				dialog.show();
			}
		}
		urlKu = null;
	}

	private void resendVeri() {
		Log.i(TAG, "Can Send SMS ? " + isCanSendSMS);
		if (isCanSendSMS) {
			veriLoading.setVisibility(View.VISIBLE);
			isCanSendSMS = false;
			veriCountdownTimer = veriCountdownTimerMax;
			btnVeriResend.setEnabled(false);
			if (waitVeriTimer != null) {
				waitVeriTimer.cancel();
				waitVeriTimer = null;
			}
			waitVeriTimer = new CountDownTimer(veriCountdownTimerMax * 1000,
					1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					veriCountdownTimer = (int) (millisUntilFinished / 1000);
					TextView textresend = (TextView) findViewById(R.id.registerVerificationBtnResendTxt);
					textresend.setText("RESEND ( " + veriCountdownTimer + " )");
				}

				@Override
				public void onFinish() {
					veriLoading.setVisibility(View.GONE);
					isCanSendSMS = true;
					btnVeriResend.setEnabled(true);
					TextView textresend = (TextView) findViewById(R.id.registerVerificationBtnResendTxt);
					textresend.setText("RESEND");
				}
			}.start();
			postPhoneNumber = getCountryCode() + getPhoneNumber();
			List<NameValuePair> postData = new ArrayList<NameValuePair>(1);
			postData.add(new BasicNameValuePair("phone", postPhoneNumber));
			doPostAsync(context, AppConstants.APIGetVrifySMS, postData, null,
					true);
		}
	}

	private void hideVeri() {
		conteinerBlackVeri.setVisibility(View.GONE);
		conteinerBlackVeri.startAnimation(fadeout);
		// setLastView(0);
		// setPhoneNumber("");
		// setCountryCode("");
	}

	private void showAccount() {
		setLastView(2);
		editID.requestFocus();
		conteinerBlackVeri.setVisibility(View.GONE);
		conteinerBlackID.setVisibility(View.VISIBLE);
		conteinerBlackID.startAnimation(fadein);
	}

	private void hideAccount() {
		conteinerBlackID.setVisibility(View.GONE);
		conteinerBlackID.startAnimation(fadeout);
		hideKeyboard();
	}

	@Override
	public void onClick(View v) {
		if (v == btnVeriConfirm) {
			hideKeyboard();
			doConfirmVeri();
		} else if (v == btnVeriResend) {
			hideKeyboard();
			resendVeri();
		} else if (v == btnShowTerms) {
			hideKeyboard();
			showTerms();
		} else if (v == btnHideTerms) {
			hideKeyboard();
			hideTerms();
		} else if (v == btnNext) {
			hideKeyboard();
			postGetVeri();
		} else if (v == containerBlackTerms) {
			hideKeyboard();
			hideTerms();
		} else if (v == btnDoneAccount) {
			hideKeyboard();
			postCreateAccount();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (containerBlackTerms.getVisibility() == View.VISIBLE) {
			hideTerms();
		} else if (conteinerBlackVeri.getVisibility() == View.VISIBLE) {
			hideVeri();
		} else if (conteinerBlackID.getVisibility() == View.VISIBLE) {
			finish();
			// setLastView(0);
			// setPhoneNumber("");
			// setCountryCode("");
			// verifyCode="";
		} else {
			finish();
			// setLastView(0);
			// setPhoneNumber("");
			// setCountryCode("");
			// verifyCode="";
			setIsVeri(false);
		}
	}

	@Override
	protected void resettingView() {
		super.resettingView();
		if (viewWidth >= ViewUtilities.GetInstance().convertDPtoPX(500)) {
			containerWidth.getLayoutParams().width = ViewUtilities
					.GetInstance().convertDPtoPX(480);
		}
		// Setting terms
		int heightTerms = (viewHeight * 82) / 100;
		containerTerms.getLayoutParams().height = heightTerms;
		webTerms.getLayoutParams().height = heightTerms;
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, RegisterActivity.class);
	}

	BroadcastReceiver SMSbr = new BroadcastReceiver() {
		@SuppressLint("DefaultLocale")
		@Override
		public void onReceive(Context context, Intent intent) {
			// Called every time a new sms is received
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++)
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				if (messages.length > -1) {
					if (messages[0].getMessageBody().toUpperCase()
							.contains("SMILES")
							|| messages[0].getMessageBody().toUpperCase()
									.contains("DIGITALBUANA")) {
						Pattern intsOnly = Pattern.compile("\\d+");
						Matcher makeMatch = intsOnly.matcher(messages[0]
								.getMessageBody());
						makeMatch.find();
						String inputInt = makeMatch.group();
						editVeriCode.setText(inputInt);
						hideKeyboard();
						doConfirmVeri();
					}
				}
			}
		}
	};

	@SuppressLint("NewApi")
	private void sendNoificationToMyRegisteredFriend() {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String countryCode = tm.getSimCountryIso();
		String getCountryRaw = AddFriendsActivity.readRaw(context,
				R.raw.countrycode);
		String getCountryCode = AddFriendsActivity.parseCountryCodeXml(
				getCountryRaw, countryCode.toUpperCase());
		if (getCountryCode == null || getCountryCode.trim().equals("")) {
			getCountryCode = "62";
		}
		String apiJsonTemplate = "{\"t\":[phonecontainer],\"e\":[mailcontainer]}";
		/*
		 * load phone contact cache
		 */
		Set<String> phoneList = appPreference.getStringSet(
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
		Set<String> mailList = appPreference.getStringSet(
				AppConstants.LOCAL_MAIL_CONTACT_CACHE_TAG, null);
		if (mailList == null)
			mailList = PhoneContactHelper.tmpSetMail;
		if (mailList != null) {
			StringBuilder sb2 = new StringBuilder();

			for (String mail : mailList) {
				if (mail != null && !mail.trim().equals(""))
					sb2.append("\"" + mail.trim() + "\"").append(",");
			}
			String tempPostUn = sb2.toString().substring(0,
					sb2.toString().length() - 1);
			apiJsonTemplate = apiJsonTemplate.replace("mailcontainer",
					tempPostUn);
		}

		Map<String, String> params = new HashMap<String, String>();
		params.put("username", "");
		params.put("list", apiJsonTemplate);
		params.put("ft", "1");
		aq.ajax(AppConstants.APIUploadContact, params, JSONObject.class,
				new AjaxCallback<JSONObject>() {
					@Override
					public void callback(String url, JSONObject object,
							AjaxStatus status) {
						// TODO Auto-generated method stub
						super.callback(url, object, status);
						if (object != null) {
							Log.i(TAG,
									"successfully send notification to my registered friends");
						}
					}
				});
	}

}
