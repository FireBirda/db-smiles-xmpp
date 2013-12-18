package com.digitalbuana.smiles.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.GCMIntentService;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.data.AppConfiguration;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.intent.AccountIntentBuilder;
import com.digitalbuana.smiles.dialog.SmilesDefaultDialog;
import com.digitalbuana.smiles.dialog.SmilesProgressDialog;
import com.digitalbuana.smiles.ui.helper.BaseUIActivity;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.RecentUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;
import com.google.android.gcm.GCMRegistrar;

public class LoginActivity extends BaseUIActivity implements
View.OnClickListener,
LocationListener
{
	
	private ScrollView scrollView;
	private EditText editUsername;
	private EditText editPassword;
	private FrameLayout btnForgot;
	private FrameLayout btnLogin;
	private TextView warningMessage;
	
	private static String tempUsername=null;
	private String tempPassword=null;
	
	private Animation shake;
	private Animation fadein;
	
	private LocationManager locationManager;
	private String provider;
	
	private String TAG = getClass().getSimpleName();
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor settingsEditor;
	private AQuery aq;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (isFinishing())
			return;
		
		aq = new AQuery(this);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		settingsEditor = mSettings.edit();
		
		tempUsername = mSettings.getString(AppConstants.USERNAME_KEY, null);
		tempPassword = mSettings.getString(AppConstants.PASSWORD_KEY, null);
		
		setContentView(R.layout.activity_login);
		//Casting
		rootView = (FrameLayout)findViewById(R.id.loginRootView);
		FontUtils.setNormalRobotoFont(context, rootView);
		
		scrollView = (ScrollView)findViewById(R.id.loginScrollView);
		editUsername = (EditText)findViewById(R.id.loginEditUsername);
		editPassword = (EditText)findViewById(R.id.loginEditPassword);
		btnForgot = (FrameLayout)findViewById(R.id.loginBtnForget);
		btnLogin = (FrameLayout)findViewById(R.id.loginBtnLogin);
		warningMessage = (TextView)findViewById(R.id.loginWarningMessage);
		
		if(tempUsername != null)
			editUsername.setText(tempUsername);
		
		if(tempPassword != null)
			editPassword.setText(tempPassword);		
		
		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
		
		//SettingListener
		btnForgot.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		
		warningMessage.setVisibility(View.GONE);		
		
		//Location
		// Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // Define the criteria how to select the locatioin provider -> use
	    // default
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);
	    // Initialize the location fields
	    if (location != null) {
	    	AppConfiguration.getInstance().setLongitude(location.getLongitude());
	    	AppConfiguration.getInstance().setLatitude(location.getLatitude());
	    	onLocationChanged(location);
	    } else {
	      
	    }
	    GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		regID =  GCMRegistrar.getRegistrationId(context);
		
		if(tempUsername != null && tempPassword != null)
			doLogin();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//editUsername.setText(tempUsername);
		scrollView.startAnimation(fadein);
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
		locationManager.requestLocationUpdates(provider, 400, 1, this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		 locationManager.removeUpdates(this);
	}
	
	@Override
	protected void resettingView() {
		super.resettingView();
		if(viewWidth>=ViewUtilities.GetInstance().convertDPtoPX(400)){
			scrollView.getLayoutParams().width=ViewUtilities.GetInstance().convertDPtoPX(400);
		}
	}
	
	private boolean checkForm(){
		if(editUsername.getText().toString().trim().equals("gia")){
			if(editPassword.getText().length()<=4){
				editPassword.startAnimation(shake);
				warningMessage.setVisibility(View.VISIBLE);
				String falied = getResources().getString(R.string.stringFailedPassword);
				warningMessage.setText(falied);
				editPassword.requestFocus();
				return false;
			}else
				return true;			
		}
	    if (editUsername.getText().length()<=4) {
	    	editUsername.startAnimation(shake);
			warningMessage.setVisibility(View.VISIBLE);
			String falied = getResources().getString(R.string.stringFailedID);
			warningMessage.setText(falied);
			editUsername.requestFocus();
			return false;
	    }
		if(editPassword.getText().length()<=4){
			editPassword.startAnimation(shake);
			warningMessage.setVisibility(View.VISIBLE);
			String falied = getResources().getString(R.string.stringFailedPassword);
			warningMessage.setText(falied);
			editPassword.requestFocus();
			return false;
		}
		return true;
	}
	
	private void doForgetPassword(){
		String userName = editUsername.getText().toString();
		if(userName != null && !userName.equals("")){
			Map<String, String> parms = new HashMap<String, String>();
			parms.put("username", userName);
			aq.progress(ScreenHelper.getDialogProgress(this)).ajax(AppConstants.APIUserForgotPassword, 
					parms, JSONObject.class, new AjaxCallback<JSONObject>(){
				@Override
				public void callback(String url, JSONObject json,
						AjaxStatus status) {
					// TODO Auto-generated method stub
					if( json != null ){
						try {
							String message = json.getString("MESSAGE");
							showToast(message);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							Log.e(TAG, e.getMessage());
						}
					}
				}
			});
		}else{
			showToast(getString(R.string.username_empty_disclaimer));
			scrollView.startAnimation(shake);
		}		
	}
	
	private void showToast(String message){
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	private String regID;
	
	private void doLogin(){
		InputMethodManager imm  = (InputMethodManager)context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	    if(imm.isActive()){
	    	imm.hideSoftInputFromWindow(editUsername.getWindowToken(), 0); 
	    }
		if(checkForm()){
			final SmilesProgressDialog dialog = new SmilesProgressDialog(context, null);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			isTryLogin= false;
			regID =  RecentUtils.getRegID();
			if(regID.equals("")){
				new CountDownTimer(GCMIntentService.BACKOFF_MILLI_SECONDS*GCMIntentService.MAX_ATTEMPTS,GCMIntentService.BACKOFF_MILLI_SECONDS) {
					@Override
					public void onTick(long millisUntilFinished) {
						regID = RecentUtils.getRegID();
						if(!regID.equals("")){
							this.cancel();
						}
					}
					@Override
					public void onFinish() {
						if(regID.equals("")){
							if(dialog!=null){
								dialog.dismiss();
							}
							
							AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
							 builder.setMessage(getString(R.string.CONNECTION_FAILED)+", do you want to retry?")               
							 	.setPositiveButton(R.string.formGlobalOk, 
							 		new DialogInterface.OnClickListener() {                   
							 			public void onClick(DialogInterface dialog, int id) {                       
							 				// FIRE ZE MISSILES!    
							 				doLogin();
							 			}               
							 		}
							 	)               
							 	.setNegativeButton(R.string.formGlobalCancel, 
							 		new DialogInterface.OnClickListener() {                   
							 			public void onClick(DialogInterface dialog, int id) {                       
							 				// User cancelled the dialog
							 				finish();
							 			}               
							 		}
							 	);
							 AlertDialog failedDialog = builder.create();
							 failedDialog.show();	
							 	
							//Toast.makeText(context, "Failed to get RegID..", Toast.LENGTH_SHORT).show();
						} else {
							if(!isTryLogin){
								if(dialog!=null){
									dialog.dismiss();
								}
								doPostLogin();
							}
						}
						this.cancel();
					}
				}.start();				
			}else{
				if(!isTryLogin){
					if(dialog!=null){
						dialog.dismiss();
					}
					doPostLogin();
				}
			}
		}
	}
	
	private static boolean isTryLogin= false;
	
	private void doPostLogin(){
		isTryLogin=true;
		TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String carrierName = manager.getNetworkOperatorName();
		if( carrierName.length() <= 1 ){
			carrierName = "Wi-Fi";
		}
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("username", editUsername.getText().toString()));
		postData.add(new BasicNameValuePair("password", editPassword.getText().toString()));
		postData.add(new BasicNameValuePair("regid", RecentUtils.getRegID()));
		postData.add(new BasicNameValuePair("imei", RecentUtils.getImei()));
		postData.add(new BasicNameValuePair("carrier", carrierName));
		postData.add(new BasicNameValuePair("long", ""+AppConfiguration.getInstance().getLongitude()));
		postData.add(new BasicNameValuePair("lat", ""+AppConfiguration.getInstance().getLongitude()));
		doPostAsync(context, AppConstants.APILogin, postData, null, true);	
	}
	
	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		if(urlKu==AppConstants.APILogin){
			parseLoginResult(result);
		}
	}
	
	private void parseLoginResult(String result){
		try {
			JSONObject json = new JSONObject(result);
			String status = json.getString("STATUS");
			if(status.equals("FAILED")){
				String message = json.getString("MESSAGE");
				gagalLogin(message);
			} else {
				final String username = json.getString("USERNAME");
				String message = json.getString("FULLNAME");
				SmilesDefaultDialog dialog = new SmilesDefaultDialog(context, status, "Welcome : "+message);
				dialog.show();
				dialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						settingsEditor.putString(AppConstants.USERNAME_KEY, username);
						settingsEditor.putString(AppConstants.PASSWORD_KEY, editPassword.getText().toString());
						settingsEditor.commit();
						suksesLogin(username);
					}
				});
			}
		} catch (JSONException e) {
			gagalLogin("Failed to Login..");
		}
	}
	private void suksesLogin(String username){
		String account;
		try {
			File file = new File(Environment.getExternalStorageDirectory()+ File.separator +"smilesAvatar.jpg");		
			boolean deleted = file.delete();		
			if(deleted)
				Log.i(TAG, "avatar successfully deleted");
			else
				Log.i(TAG, "avatar failed deleted");
			account = AccountManager.getInstance().addAccount(username.replace("@", "%40"), editPassword.getText().toString());
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			return;
		}
		setResult(RESULT_OK, createAuthenticatorResult(this, account));
		finish();
	}

	private void gagalLogin(final String message){
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				warningMessage.setVisibility(View.VISIBLE);
				warningMessage.setText(message);
				warningMessage.startAnimation(shake);
				warningMessage.requestFocus();
			}
		});		
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.loginBtnForget:doForgetPassword();break;
		case R.id.loginBtnLogin:doLogin();break;
		}
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, LoginActivity.class);
	}

	private static Intent createAuthenticatorResult(Context context, String account) {
		return new AccountIntentBuilder(null, null).setAccount(account).build();
	}

	public static String getAuthenticatorResultAccount(Intent intent) {
		return AccountIntentBuilder.getAccount(intent);
	}

	@Override
	public void onLocationChanged(Location location) {
		AppConfiguration.getInstance().setLatitude(location.getLatitude());
		AppConfiguration.getInstance().setLongitude(location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		this.provider = provider;
		locationManager.requestLocationUpdates(provider, 400, 1, this);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		this.provider = provider;
		locationManager.requestLocationUpdates(provider, 400, 1, this);
	}
}
