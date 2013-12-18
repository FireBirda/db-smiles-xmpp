package com.digitalbuana.smiles.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.SettingMenuAdapter;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.dialog.SmilesProgressDialog;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.FontUtils;

public class SettingsActivity extends ManagedActivity
implements
OnClickListener, OnItemClickListener
{

	private FrameLayout btnBack;
	private ListView settingList;
	private SettingMenuAdapter settingAdapter;
	private AQuery aq;
	private String TAG = getClass().getSimpleName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		aq = new AQuery(this);
		
		rootView = (FrameLayout)findViewById(R.id.settingsRootView);
		btnBack = (FrameLayout)findViewById(R.id.settingsBtnBack);
		
		btnBack.setOnClickListener(this);
		
		settingList = (ListView)findViewById(R.id.settingList);
		settingAdapter = new SettingMenuAdapter(this);
		settingList.setAdapter(settingAdapter);
		settingList.setOnItemClickListener(this);
		
		FontUtils.setRobotoFont(context, rootView);
	}
	
	@Override
	public void onClick(View v) {
		if(v==btnBack){
			finish();
		}
	}
	
	
	public static Intent createIntent(Context context) {
		return new Intent(context, SettingsActivity.class);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		// TODO Auto-generated method stub
		
		if(parent == settingList){
			
			if(((SettingMenuAdapter.ViewHolder)view.getTag()).isSelectable()){
				
				SmilesProgressDialog progressDialog = new SmilesProgressDialog(context, null);
				progressDialog.setCanceledOnTouchOutside(false);				
				Map<String, Object> params = new HashMap<String, Object>();
				
				switch (pos){
				case 0:
					context.startActivity(ProfileDetailActivity.createIntent(context, AccountManager.getInstance().getAccountKu(), AccountManager.getInstance().getAccountKu()));
					break;
				case 1:					
			        params.put("id", "8");
			        aq.progress(progressDialog).ajax(AppConstants.APIUserGetText, params, JSONObject.class, this, "callbackPost");
					break;
				case 2:					
			        params.put("id", "6");
			        aq.progress(progressDialog).ajax(AppConstants.APIUserGetText, params, JSONObject.class, this, "callbackPost");
					break;
				case 3:
					params.put("id", "7");
			        aq.progress(progressDialog).ajax(AppConstants.APIUserGetText, params, JSONObject.class, this, "callbackPost");
					break;
				case 4:
					startActivity(new Intent(this, CustomeSoundActivity.class));
					break;
				case 5:
					resetPassword();
					break;
				}
			}
		}
	}
	
	public void callbackPost(String url, JSONObject json, AjaxStatus status) {
		if(json!=null){
			try {
				String getText = json.getString("CONTENT");
				Dialog dialog = new Dialog(context);
				dialog.setCancelable(true);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.popup_browseview);
				WebView webContent = (WebView)dialog.findViewById(R.id.myWebView);
				webContent.loadData(getText, "text/html", "UTF-8");
				dialog.show();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				Log.e(TAG, e.getMessage());
			}
		}
	}
	
	private void resetPassword(){
		final Dialog dialog = new Dialog(context);
		dialog.setCancelable(true);
		dialog.setTitle(R.string.change_password);
		dialog.setContentView(R.layout.popup_reset_password);		
		dialog.show();
		
		final TextView currentPassword = (TextView)dialog.findViewById(R.id.currentPasswordText);
		final TextView newPassword = (TextView)dialog.findViewById(R.id.newPasswordText);
		final TextView newPassConf = (TextView)dialog.findViewById(R.id.newPasswordConfText);
		Button saveButton = (Button)dialog.findViewById(R.id.saveNewPasswordButton);		
		
		saveButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String current = currentPassword.getText().toString();
				String newPass = newPassword.getText().toString();
				String newPassC = newPassConf.getText().toString();
				if(!current.equals("") && !newPass.equals("") && !newPassC.equals("")){
					
					Map<String, String> params = new HashMap<String, String>();
					
					params.put("username", AccountManager.getInstance().getAccountKu());
					params.put("current_password", current);
					params.put("new_password1", newPass);
					params.put("new_password2", newPassC);
					
					aq.progress(ScreenHelper.getDialogProgress(SettingsActivity.this)).
						ajax(AppConstants.APIUserChangePassword, 
							params, 
							JSONObject.class, 
							new AjaxCallback<JSONObject>(){
								public void callback(String url, JSONObject object, AjaxStatus status) {
									if(object!=null){
										try {
											showToast(object.getString("MESSAGE"));
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											//e.printStackTrace();
											showToast(getString(R.string.error_charge));
											Log.e(TAG, e.getMessage());
										}
									}else
										showToast(getString(R.string.error_charge));
									
								};
							}
					);
					
				}else{
					showToast(getString(R.string.fill_all_data_disclaimer));
				}
				dialog.dismiss();
			}
			
		});
	}
	
	private void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
}
