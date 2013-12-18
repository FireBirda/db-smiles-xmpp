package com.digitalbuana.smiles.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.account.StatusMode;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.intent.AccountIntentBuilder;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.dialog.SmilesConfirmDialog;
import com.digitalbuana.smiles.dialog.SmilesProgressDialog;
import com.digitalbuana.smiles.dialog.SmilesConfirmDialog.OnSmilesDialogClose;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.RecentUtils;
import com.digitalbuana.smiles.utils.StringUtils;

public class ProfileSettingsActivity extends ManagedActivity 
implements OnClickListener
{
	private static final String SAVED_TEXT = "com.digitalbuana.smiles.activity.StatusEditor.SAVED_TEXT";
	private static final String SAVED_MODE = "com.digitalbuana.smiles.activity.StatusEditor.SAVED_MODE";

	static final public int OPTION_MENU_CLEAR_STATUSES_ID = 1;

	static final public int CONTEXT_MENU_SELECT_STATUS_ID = 10;
	static final public int CONTEXT_MENU_EDIT_STATUS_ID = 11;
	static final public int CONTEXT_MENU_REMOVE_STATUS_ID = 12;
	private static final int ICON_SIZE = 150;

	private FrameLayout btnSelectAvatar;
	private ImageView avatar;
	private EditText editFullName;
	private EditText editStatus;
	private FrameLayout btnSave;
	private RadioGroup radioSex;
	
	private FrameLayout blackCapture;
	private FrameLayout btnCaptureCamera;
	private FrameLayout btnCaptureGallery;
	private FrameLayout btnCaptureclear;
	private FrameLayout btnCloseSelect;
	
	private int isAvatarChange=0;
	private boolean isNameChange=false;
	private boolean isStatusChange=false;
	
	private static SharedPreferences appPreference;
	private String TAG = getClass().getSimpleName();
	
	private SmilesProgressDialog progressDialog;
	
	private File fileAvatar;
	private SharedPreferences mSettings;
	//private SharedPreferences.Editor settingsEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		//settingsEditor = mSettings.edit();
		
		if (isFinishing())
			return;
		
		setContentView(R.layout.activity_profile_settings);
		
		rootView = (FrameLayout)findViewById(R.id.profileRootView);
		avatar= (ImageView)findViewById(R.id.profileAvatar);
		
		btnSelectAvatar = (FrameLayout)findViewById(R.id.profilebtnSelectAvatar);
		editFullName = (EditText)findViewById(R.id.profileEditName);
		editStatus = (EditText)findViewById(R.id.profileEditStatus);
		btnSave = (FrameLayout)findViewById(R.id.profileBtnSave);
		radioSex = (RadioGroup)findViewById(R.id.profileRadioSex);
		
		blackCapture = (FrameLayout)findViewById(R.id.profileBlackCaptureFrom);
		btnCaptureCamera = (FrameLayout)findViewById(R.id.profleBtnCamera);
		btnCaptureGallery = (FrameLayout)findViewById(R.id.profileBtnGallery);
		btnCaptureclear = (FrameLayout)findViewById(R.id.profileBtnClearAvatar);
		btnCloseSelect = (FrameLayout)findViewById(R.id.profleBtnCloseSelect);
		FontUtils.setRobotoFont(context, rootView);		
		
		btnSave.setOnClickListener(this);
		btnSelectAvatar.setOnClickListener(this);
		blackCapture.setOnClickListener(this);
		btnCaptureCamera.setOnClickListener(this);
		btnCaptureGallery.setOnClickListener(this);
		btnCaptureclear.setOnClickListener(this);
		btnCloseSelect.setOnClickListener(this);
		
		appPreference =  PreferenceManager.getDefaultSharedPreferences(context);
		
		String userAccount = mSettings.getString(AppConstants.USERNAME_KEY, null);
		if(userAccount == null)
			userAccount = AccountManager.getInstance().getAccountKu();
		
		AbstractContact abstractContact = RosterManager.getInstance().getBestContact(userAccount, userAccount+"@"+AppConstants.XMPPServerHost);
		fileAvatar = null;
		String profileName = getFullname();
		if(profileName.length()<=0){
			profileName = StringUtils.replaceStringEquals(abstractContact.getName());
		}
		String profileStatus = SettingsManager.statusText();
		if(profileStatus.length()<=0){
			profileStatus = getStatus();
		}
		boolean profileGender = getGender();
		if(profileGender){
			radioSex.check(R.id.profileRadioOption1);
		} else {
			radioSex.check(R.id.profileRadioOption2);
		}
		
		editFullName.setText(profileName);
		editStatus.setText(profileStatus);
		
		File fileAvatar = new File(Environment.getExternalStorageDirectory()+ File.separator +"smilesAvatar.jpg");
		if(fileAvatar.exists()){
			Bitmap myBitmap = BitmapFactory.decodeFile(fileAvatar.getAbsolutePath());
			avatar.setImageBitmap(myBitmap);
		}else
			avatar.setImageDrawable(abstractContact.getAvatar());
		
		
		isAvatarChange=0;
		isNameChange = false;
		isStatusChange = false;
		
		progressDialog = new SmilesProgressDialog(context, null);
		progressDialog.setCanceledOnTouchOutside(false);
		
		editFullName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				isNameChange=true;
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			@Override
			public void afterTextChanged(Editable arg0) {	
			}
		});
		editStatus.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				isStatusChange=true;
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {	
			}
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
	}	
	
	@Override
	public void onClick(View v) {
		if(v==btnSave){			
			saveToRoster();
		} else if(v==blackCapture|| v==btnCloseSelect){
			hideCaptureFrom();
		} else if(v==btnSelectAvatar){
			showCaptureFrom();
		} else if(v==btnCaptureCamera){
			selectFrom(PICK_FROM_CAMERA);
		} else if(v==btnCaptureGallery){
			selectFrom(PICK_FROM_GALLERY);
		} else if(v==btnCaptureclear){
			avatar.setImageResource(R.drawable.img_add_photo);
			isAvatarChange=2;
			hideCaptureFrom();
		}
	}
	
	private int PICK_FROM_CAMERA = 8572187;
	private int PICK_FROM_GALLERY = 8572188;
	private int CROP_FROM_CAMERA = 8572189;
	private Uri mImageCaptureUri;
	
	private void selectFrom(int from){
		if(from==PICK_FROM_CAMERA){
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);			
	        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "smiles_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
	        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
	        try {
	        	intent.putExtra("crop", "true");
		        intent.putExtra("aspectX", 1);
		        intent.putExtra("aspectY", 1);
		        intent.putExtra("outputX", ICON_SIZE);
		        intent.putExtra("outputY", ICON_SIZE);
		        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri.getPath());
	        	intent.putExtra("return-data", true);
	        	startActivityForResult(intent, CROP_FROM_CAMERA);
	        } catch (ActivityNotFoundException e) {
	        	e.printStackTrace();
	        }
		} else {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.putExtra("crop", "true");
	        intent.putExtra("aspectX", 1);
	        intent.putExtra("aspectY", 1);
	        intent.putExtra("outputX", ICON_SIZE);
	        intent.putExtra("outputY", ICON_SIZE);
	        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
	        intent.putExtra("return-data", true);
		    intent.setAction(Intent.ACTION_GET_CONTENT);
		    startActivityForResult(Intent.createChooser(intent, "Complete action using"), CROP_FROM_CAMERA);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			isAvatarChange= 1;
			if(requestCode==PICK_FROM_CAMERA){
				doCrop();
			} else if(requestCode==PICK_FROM_GALLERY){
				mImageCaptureUri = data.getData();
				doCrop();
			} else if(requestCode==CROP_FROM_CAMERA){
				Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    avatar.setImageBitmap(photo);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                    fileAvatar = new File(Environment.getExternalStorageDirectory()+ File.separator +"smilesAvatar.jpg");
                    try {
						fileAvatar.createNewFile();
						FileOutputStream fo = new FileOutputStream(fileAvatar);;
						fo.write(bytes.toByteArray());
						fo.close();
						
					} catch (IOException e) {
						failedUpdateProfile("Error Updating Profile");
					}
                }
                hideCaptureFrom();
			}else {
				isAvatarChange= 0;
				hideCaptureFrom();
			}
		} else {
			isAvatarChange= 0;
		}
	}	
	
	private void doCrop(){
		Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
            intent.setData(mImageCaptureUri);
            intent.putExtra("return-data", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("crop", "true");
            startActivityForResult(intent, CROP_FROM_CAMERA);
            return;
        } else {
    		intent.setData(mImageCaptureUri);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("crop", "true");
            intent.putExtra("return-data", true);
            ResolveInfo res = list.get(0);
            intent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(intent, CROP_FROM_CAMERA);
        }
	}
	
	private void showCaptureFrom(){
		blackCapture.setVisibility(View.VISIBLE);
		blackCapture.startAnimation(fadein);
	}
	
	private void hideCaptureFrom(){
		if(blackCapture.getVisibility()==View.VISIBLE){
			blackCapture.setVisibility(View.GONE);
			blackCapture.startAnimation(fadeout);
		}
	}
	
	private void saveProfile(){	
		
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", mSettings.getString(AppConstants.USERNAME_KEY, ""));
        
        if(isAvatarChange==1){
        	 params.put("image", fileAvatar);
		}
		if(isNameChange){
			setFullName(editFullName.getText().toString());
	        params.put("fullname", getFullname());
		}
		if(isStatusChange){
			setStatus(editStatus.getText().toString());
			params.put("status", getStatus());					
		}
		if(radioSex.getCheckedRadioButtonId()==R.id.profileRadioOption1){
			setGender(true);
			params.put("gender", "pria");
			params.put("gender_preference", "pria");
		} else {
			setGender(false);
			params.put("gender", "wanita");
			params.put("gender_preference", "wanita");
		}
		
		AQuery aq = new AQuery(getApplicationContext());
        aq.ajax(AppConstants.APIUpdateProfile, params, JSONObject.class, this, "callbackPost");		
		
	}
	
	private void finishWithConfirm(){
		if(isAvatarChange!=0||isNameChange||isStatusChange){
			SmilesConfirmDialog dialog = new SmilesConfirmDialog(context, new OnSmilesDialogClose() {
				@Override
				public void onSmilesDialogClose(boolean isConfirm) {
					if(isConfirm){
						finish();
					}
				}
			}, Application.getInstance().getString(R.string.formProfileTitle), "Are You Sure to Close Without Change ?");
			dialog.show();
		} else {
			finish();
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
		finishWithConfirm();
	}
	
	
	public void callbackPost(String url, JSONObject json, AjaxStatus status) {
		
		if(json!=null){
			try {
				String statusJson = json.getString("STATUS");
				if(statusJson.equals("SUCCESS")){
					successUpdateProfile();
				} else {
					String message = json.getString("MESSAGE");
					failedUpdateProfile("Error :"+message);
				}
			} catch (JSONException e) {
				failedUpdateProfile("Error while updating.[1]");
			}
		} else {
			failedUpdateProfile("Error while updating.[2]");
		}
	}
	
	private void failedUpdateProfile(final String mesage){
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(progressDialog!=null && progressDialog.isShowing()){
					progressDialog.dismiss();
				}
				Toast.makeText(context, mesage, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void successUpdateProfile(){
		pushUpdateProfile();		
	}
	
	private void pushUpdateProfile(){
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		String userAccount = mSettings.getString(AppConstants.USERNAME_KEY, null);
		if(userAccount == null)
			userAccount = AccountManager.getInstance().getAccountKu();
        
		params.put("username", userAccount);
        
        String messageUpdate = "UPDATE : ";
        if(isAvatarChange==1){
        	messageUpdate = messageUpdate+"AVATAR ,";
        }
        if(isNameChange){
        	messageUpdate = messageUpdate+"DISPLAY NAME ,";
        }
        if(isStatusChange){
        	messageUpdate = messageUpdate+"STATUS : "+getStatus();
        	SettingsManager.setStatusText(editStatus.getText().toString());
        }
        params.put("message", messageUpdate);
		AQuery aq = new AQuery(context);
        aq.ajax(AppConstants.APIPushService, params, JSONObject.class, this, "callbackPostPush");
	}
	private void successUpdateProfilePush(){
		if(progressDialog!=null && progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		finish();
	}
	
	public void callbackPostPush(String url, JSONObject json, AjaxStatus status) {
		successUpdateProfilePush();
	}
	
	private void saveToRoster() {
		
		if(RecentUtils.checkNetwork()){
			
			if(AccountManager.getInstance().getIsConnected()){
				
				progressDialog.show();
			
				new Thread(){
					@Override
					public void run() {
						final VCard userVcard = new VCard();
						userVcard.setType(Type.SET);
						final Connection connection = AccountManager.getInstance().getActiveAccount().getConnectionThread().getXMPPConnection();
						
								boolean isSavedToRoster = false;
								try {
									//userVcard.load(connection);
									userVcard.setNickName(editFullName.getText().toString());
									final byte[] bumapByt;
									if(isAvatarChange==1){
										Drawable drawable = avatar.getDrawable();
										Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
										Bitmap resized = Bitmap.createScaledBitmap(bmp, 128, 128, true);
										ByteArrayOutputStream stream = new ByteArrayOutputStream();
										resized.compress(Bitmap.CompressFormat.PNG, 80, stream);
										bumapByt = stream.toByteArray();
										userVcard.setAvatar(bumapByt);
									} else if(isAvatarChange==2){
										bumapByt = null;
										userVcard.setAvatar(bumapByt);
									} else {
										Drawable drawable = avatar.getDrawable();
										Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
										Bitmap resized = Bitmap.createScaledBitmap(bmp, 128, 128, true);
										ByteArrayOutputStream stream = new ByteArrayOutputStream();
										resized.compress(Bitmap.CompressFormat.PNG, 80, stream);
										bumapByt = stream.toByteArray();
										userVcard.setAvatar(bumapByt);
									}
									userVcard.save(connection);
									isAvatarChange= 1;
									Presence presence = new Presence(Presence.Type.available);
									String vurbose = PresenceManager.getInstance().getVurbose(AccountManager.getInstance().getAccountKu(), AccountManager.getInstance().getActiveAccount().getRealJid());
					                presence.setFrom(vurbose);
					                Log.d(TAG,"VUrbose Name :" + vurbose);
					                final String lastAvatar = userVcard.getAvatarHash();
									PacketExtension pe = new PacketExtension() {
										@Override
										public String toXML() {
											return "<status>Online</status>" +
							                          "<priority>1</priority>" +
							                          "<x xmlns=\"vcard-temp:x:update\">" +
							                          "<photo>"+bumapByt.toString()+"</photo>" +
							                          "</x>" +
							                          "<x xmlns=\"jabber:x:avatar\">" +
							                          "<hash>"+lastAvatar+"</hash>" +
							                          "</x>";
										}
										@Override
										public String getNamespace() {return null;}
										@Override
										public String getElementName() {return null;}
									};			                    
									presence.addExtension(pe);
									ConnectionManager.getInstance().sendPacket(AccountManager.getInstance().getAccountKu(), presence);
									AccountManager accountManager = AccountManager.getInstance();
									accountManager.setStatus(StatusMode.available, editStatus.getText().toString());
									//editStatus.setText(SettingsManager.statusText());
									isSavedToRoster = true;
								} catch (XMPPException e) {
									Log.e(TAG, e.getMessage());
								} catch (NetworkException e) {
									Log.e(TAG, e.getMessage());
								}catch(IllegalArgumentException e){
									Log.e(TAG, e.getMessage());
								}finally{
									this.interrupt();
									if(isSavedToRoster)
										saveProfile();
									else{
										if(progressDialog.isShowing())
											progressDialog.dismiss();
										failedUpdateProfile("Error Updating Profile, please try again later.");
									}										
								}								
							
					}
				}.start();
			} else {				
				failedUpdateProfile("Still Connecting to Server.. /r Be Patient...");				
			}			
		}		
	}
	
	public static Intent createIntent(Context context) {
		return ProfileSettingsActivity.createIntent(context, null);
	}
	public static Intent createIntent(Context context, String account) {
		return new AccountIntentBuilder(context, ProfileSettingsActivity.class).setAccount(account).build();
	}
	
	public void setFullName(String fullname){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("PROFILE_FULLNAME",fullname);
        editor.commit();
	}
	public String getFullname(){
		return appPreference.getString("PROFILE_FULLNAME", "");
	}
	public void setStatus(String fullname){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("PROFILE_STATUS",fullname);
        editor.commit();
	}
	public String getStatus(){
		return  appPreference.getString("PROFILE_STATUS", "");
	}
	public void setGender(boolean isMale){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putBoolean("PROFILE_GENDER", isMale);
        editor.commit();
	}
	public boolean getGender(){
		return  appPreference.getBoolean("PROFILE_GENDER", true);
	}
}
