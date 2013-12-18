package com.digitalbuana.smiles.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.BufferType;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.StickerGridAdapter;
import com.digitalbuana.smiles.awan.activity.PhotoFromWebActivity;
import com.digitalbuana.smiles.data.AppConfiguration;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.FriendsModel;
import com.digitalbuana.smiles.data.intent.EntityIntentBuilder;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.roster.AbstractContact;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.dialog.SmilesConfirmDialog;
import com.digitalbuana.smiles.dialog.SmilesConfirmDialog.OnSmilesDialogClose;
import com.digitalbuana.smiles.dialog.SmilesProgressDialog;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.Emoticons;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.RecentUtils;
import com.digitalbuana.smiles.utils.StringUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;
//import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

public class ProfileDetailActivity extends ManagedActivity
implements
OnClickListener
{

	private AQuery aq;
	
	private String account;
	private String user;
	private AbstractContact abstractContact;
	
	private FrameLayout btnBack;
	private TextView txtTitle;
	private ProgressBar progressTitle;
	
	//private PullToRefreshScrollView scrollView;
	
	private ImageView imgProfile;
	private TextView txtName;
	private TextView txtStatus;
	private TextView txtTime;
	private TextView txtDistance;
	private FrameLayout frameGender;
	
	private FrameLayout noItem;
	private GridViewKu gridPhotos;
	
	private StickerGridAdapter adapterPhotos;
	
	private boolean isMe=false;
	private boolean isFriend=false;
	private boolean isBlock=false;
	

	private ArrayList<String> userPhotos;
	
	
	private ArrayList<FriendsModel> listFriend;
	private ArrayList<FriendsModel> listBlockFriend;
	
	private FrameLayout btnAddFriend;
	private TextView btnAddFriendTxt;
	private FrameLayout btnSendAttention;
	private FrameLayout btnChat;
	private FrameLayout btnBlockFriend;
	private TextView btnBlockFriendTxt;
	
	private FrameLayout blackCapture;
	private FrameLayout profleBtnCloseSelect, profleBtnCamera, profileBtnGallery;	
	
	private int PICK_FROM_CAMERA = 8572187;
	private int PICK_FROM_GALLERY = 8572188;
	private int CROP_FROM_CAMERA = 8572189;
	private Uri mImageCaptureUri;
	private File fileAvatar;
	
	private ImageView avatar;
	private SmilesProgressDialog progressDialog;
	private String TAG = getClass().getSimpleName();
	
	private String apiAvatarThumb = "";
	private String apiAvatarFull = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_detail);
		Intent intent = getIntent();
		
		profleBtnCloseSelect = (FrameLayout)findViewById(R.id.profleBtnCloseSelect);
		profleBtnCamera = (FrameLayout)findViewById(R.id.profleBtnCamera);
		profileBtnGallery = (FrameLayout)findViewById(R.id.profileBtnGallery);
				
		profleBtnCloseSelect.setOnClickListener(this);
		profleBtnCamera.setOnClickListener(this);
		profileBtnGallery.setOnClickListener(this);
		
		account = AccountManager.getInstance().getAccountKu();
		user = getUser(intent);
		
		
		if (AccountManager.getInstance().getAccount(account) == null || user == null) {
			Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
			finish();
			return;
		}	

		rootView = (FrameLayout)findViewById(R.id.profileDetailRootView);
		btnBack = (FrameLayout)findViewById(R.id.profileDetailBtnBack);
		txtTitle = (TextView)findViewById(R.id.profileDetailTxtTitle);
		progressTitle = (ProgressBar)findViewById(R.id.profileDetailProgress);
		
		imgProfile = (ImageView) findViewById(R.id.profileDetailImgProfile);
		txtName = (TextView) findViewById(R.id.profileDetailTxtName);
		txtStatus = (TextView) findViewById(R.id.profileDetailTxtStatus);
		txtTime = (TextView) findViewById(R.id.profileDetailTxtTime);
		txtDistance = (TextView) findViewById(R.id.profileDetailTxtDistance);
		frameGender = (FrameLayout) findViewById(R.id.profileDetailGender);
		noItem = (FrameLayout) findViewById(R.id.profileDetailNoItemContainer);
		gridPhotos  = (GridViewKu)findViewById(R.id.profileDetailGridPhotos);
		
		
		btnAddFriend = (FrameLayout)findViewById(R.id.profileDetailBtnAddFriend);
		btnSendAttention = (FrameLayout)findViewById(R.id.profileDetailBtnSendAtttention);
		btnChat = (FrameLayout)findViewById(R.id.profileDetailBtnChat);
		btnBlockFriend = (FrameLayout)findViewById(R.id.profileDetailBtnBlock);
		btnAddFriendTxt = (TextView)findViewById(R.id.profileDetailTxtAddFriend);
		btnBlockFriendTxt = (TextView)findViewById(R.id.profileDetailTxtBLockFriend);
		
		btnBack.setOnClickListener(this);
		btnAddFriend.setOnClickListener(this);
		btnSendAttention.setOnClickListener(this);
		btnChat.setOnClickListener(this);
		btnBlockFriend.setOnClickListener(this);
		
		aq = new AQuery(this,rootView);
		
		userPhotos = new ArrayList<String>();
		adapterPhotos = new StickerGridAdapter(context, userPhotos, ViewUtilities.GetInstance().convertDPtoPX(100));
		gridPhotos.setAdapter(adapterPhotos);
		
		abstractContact = RosterManager.getInstance().getBestContact(account, user);
		user = abstractContact.getUser();
		txtTitle.setText(StringUtils.replaceStringEquals(abstractContact.getName())+Application.getInstance().getString(R.string.ProfileDetailTitle));
		
		if(user.equals(AccountManager.getInstance().getAccountKu())){			
			isMe = true;
			txtDistance.setVisibility(View.GONE);
		}
		
		imgProfile.setImageDrawable(abstractContact.getAvatar());
		imgProfile.setOnClickListener(this);
		
		txtName.setText(StringUtils.replaceStringEquals(user));
		txtStatus.setText(
			Emoticons.getSmiledText(
				context, 
				abstractContact.getStatusText()
			),
			BufferType.SPANNABLE
		);
		
		FontUtils.setRobotoFont(context, rootView);
		
		if(!isMe){
			
			listFriend = FriendsManager.getInstance().getFriendsListManager().getAllFriends();
			listBlockFriend = FriendsManager.getInstance().getFriendsBlockedManager().getAllFriends();
			
			for (int i = 0; i < listFriend.size(); i++) {
				if(listFriend.get(i).getName().equals(StringUtils.replaceStringEquals(user))){
					isFriend=true;
				}
			}
			
		}
		
		btnAddFriend.setVisibility(View.GONE);
		btnSendAttention.setVisibility(View.GONE);
		btnChat.setVisibility(View.GONE);
		btnBlockFriend.setVisibility(View.GONE);
		
		blackCapture = (FrameLayout)findViewById(R.id.profileBlackCaptureFrom);
		blackCapture.setOnClickListener(this);
		
		loadProfile(true);
	}
	
	private void hideAllVIew(){
		btnAddFriend.setVisibility(View.GONE);
		btnSendAttention.setVisibility(View.GONE);
		btnChat.setVisibility(View.GONE);
		btnBlockFriend.setVisibility(View.GONE);
		btnAddFriendTxt.setText(Application.getInstance().getString(R.string.FormGlobalAddAsFriend));
		gridPhotos.setVisibility(View.GONE);
		noItem.setVisibility(View.VISIBLE);
	}
	
	private void toggleIsFriendView(){
		
		hideAllVIew();
		if(isMe){
			
			btnAddFriend.setVisibility(View.VISIBLE);
			gridPhotos.setVisibility(View.VISIBLE);
			btnBlockFriend.setVisibility(View.VISIBLE);
			
			btnAddFriendTxt.setText(getString(R.string.formAccountTitle));
			btnBlockFriendTxt.setText(getString(R.string.FormGlobalUserAddPhoto));
			
		}else if(isFriend && !isMe){
			
			btnAddFriend.setVisibility(View.VISIBLE);
			btnSendAttention.setVisibility(View.VISIBLE);
			btnChat.setVisibility(View.VISIBLE);
			btnBlockFriend.setVisibility(View.VISIBLE);
			btnAddFriendTxt.setText(Application.getInstance().getString(R.string.FormGlobalRemoveFriends));
			gridPhotos.setVisibility(View.VISIBLE);
			
			ArrayList<String> groups = new ArrayList<String>();
			
			Collections.sort(groups);
			
			try {
				RosterManager.getInstance().createContact(abstractContact.getAccount(), abstractContact.getUser(), StringUtils.replaceStringEquals(abstractContact.getUser()), groups);
				PresenceManager.getInstance().acceptSubscription(abstractContact.getAccount(), abstractContact.getUser());
				FriendsManager.getInstance().getFriendsListManager().addFriendsByJID(abstractContact.getUser());
				FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().removeFriendsJID(abstractContact.getUser());
				FriendsManager.getInstance().getFriendsWaitingMeApproveManager().removeFriendsJID(abstractContact.getUser());
			} catch (NetworkException e) {
				e.printStackTrace();
			}
		} else if(!isFriend && !isMe){
			btnAddFriendTxt.setText(Application.getInstance().getString(R.string.FormGlobalAddAsFriend));
			for (int i = 0; i < listBlockFriend.size(); i++) {
				if(listBlockFriend.get(i).getName().equals(StringUtils.replaceStringEquals(user))){
					isBlock=true;
				}
			}
			if(isBlock){
				btnBlockFriend.setVisibility(View.VISIBLE);
				btnBlockFriendTxt.setText(Application.getInstance().getString(R.string.FormGlobalUnBlockFriend));
				FriendsManager.getInstance().getFriendsBlockedManager().addFriendsByJID(user);
				FriendsManager.getInstance().getFriendsListManager().removeFriendsJID(user);
				FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().removeFriendsJID(user);
				FriendsManager.getInstance().getFriendsWaitingMeApproveManager().removeFriendsJID(user);
				try {
					RosterManager.getInstance().removeContact(account, user);
					PresenceManager.getInstance().discardSubscription(account, user);
					MessageManager.getInstance().closeChat(account, user);
				} catch (NetworkException e) {
					e.printStackTrace();
				}
			} else {
				gridPhotos.setVisibility(View.VISIBLE);
				btnAddFriend.setVisibility(View.VISIBLE);
				btnBlockFriend.setVisibility(View.VISIBLE);
				btnAddFriendTxt.setText(Application.getInstance().getString(R.string.FormGlobalAddAsFriend));
				btnBlockFriendTxt.setText(Application.getInstance().getString(R.string.FormGlobalBlockFriend));
				FriendsManager.getInstance().getFriendsBlockedManager().removeFriendsJID(user);
			}
		}
	}
	
	private void loadProfile(boolean isView){
		noItem.setVisibility(View.VISIBLE);
		gridPhotos.setVisibility(View.GONE);
		if(RecentUtils.checkNetwork()){
			progressTitle.setVisibility(View.VISIBLE);
			List<NameValuePair> postDataa = new ArrayList<NameValuePair>(1);
			postDataa.add(new BasicNameValuePair("username", AccountManager.getInstance().getActiveAccount().getAccount()));
			postDataa.add(new BasicNameValuePair("targetname", StringUtils.replaceStringEquals(user)));
			if(!isView){
				postDataa.add(new BasicNameValuePair("push", "N"));
			}
			doPostAsync(context, AppConstants.APIGetProfile, postDataa, null, true);
		} else {
			finish();
		}
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				progressTitle.setVisibility(View.GONE);
			}
		});
		
		if(result.length()>10){
			try {
				JSONObject json = new JSONObject(result);
				String status = json.getString("STATUS");
				if(status.toUpperCase().equals("SUCCESS")){
					if(urlKu==AppConstants.APIGetProfile){
						parsingProfile(json);
					} else if(urlKu==AppConstants.APIAddFriends){
						successAddFriends();
					} else if(urlKu==AppConstants.APIDeleteFriends){
						successDeleteFriend();
					} else if(urlKu==AppConstants.APIBlocked){
						if(isBlock){
							successUnBlockFriend();
						} else {
							successBlockFriend();
						}
					}
				} else {
					String message = json.getString("MESSAGE");
					failedLoadProfile(message);
				}
			} catch (JSONException e) {
				failedLoadProfile("Please ContactSupport #1");
			}
		} else {
			failedLoadProfile("Please ContactSupport #2");
		}
	}
	private void successAddFriends(){
		AccountItem accountItem = AccountManager.getInstance().getActiveAccount(); 
		ArrayList<String> groups = new ArrayList<String>();
		Collections.sort(groups);
		try {
			PresenceManager.getInstance().requestSubscription(accountItem.getAccount(), abstractContact.getUser());
			Toast.makeText(context, "Success add : "+StringUtils.replaceStringEquals(abstractContact.getUser()), Toast.LENGTH_SHORT).show();
			FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().addFriendsByJID(user);
			FriendsManager.getInstance().getFriendsListManager().removeFriendsJID(user);
			isFriend=true;
			hideAllVIew();
			loadProfile(false);
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			Toast.makeText(context, "Error add : "+StringUtils.replaceStringEquals(abstractContact.getUser()), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}
	
	private void successDeleteFriend(){
	try {
			FriendsManager.getInstance().getFriendsListManager().removeFriendsJID(abstractContact.getUser());
			FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().removeFriendsJID(abstractContact.getUser());
			
			RosterManager.getInstance().removeContact(abstractContact.getAccount(), abstractContact.getUser());
			PresenceManager.getInstance().discardSubscription(abstractContact.getAccount(), abstractContact.getUser());
			MessageManager.getInstance().closeChat(abstractContact.getAccount(), abstractContact.getUser());
			Toast.makeText(context, "Success UnFriend : "+StringUtils.replaceStringEquals(abstractContact.getUser()), Toast.LENGTH_SHORT).show();
			isFriend=false;
			hideAllVIew();
			loadProfile(false);
		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			Toast.makeText(context, "Error add : "+StringUtils.replaceStringEquals(abstractContact.getUser()), Toast.LENGTH_SHORT).show();
		}
	}
	
	private void successBlockFriend(){
		isBlock=true;
		hideAllVIew();
		loadProfile(false);
		try {
			FriendsManager.getInstance().getFriendsListManager().removeFriendsJID(abstractContact.getUser());
			RosterManager.getInstance().removeContact(abstractContact.getAccount(), abstractContact.getUser());
			PresenceManager.getInstance().discardSubscription(abstractContact.getAccount(), abstractContact.getUser());
			MessageManager.getInstance().closeChat(abstractContact.getAccount(), abstractContact.getUser());
		} catch (NetworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void successUnBlockFriend(){
		isBlock=false;
		hideAllVIew();
		loadProfile(false);
	}
	
	@SuppressLint("SimpleDateFormat")
	private void parsingProfile(JSONObject json){
		try {
			JSONObject dataUser = json.getJSONObject("DATA");
			
			String fullnameUser = dataUser.getString("fullname");
			String statusUser = dataUser.getString("status");
			txtName.setText(fullnameUser);
			if(statusUser.length()>=1){
				txtStatus.setText(
					Emoticons.getSmiledText(
						context,
						statusUser
					),
					BufferType.SPANNABLE
				);
			}
			//Location
			String longitueS =dataUser.getString("longitude");
			String latitudeS =dataUser.getString("latitude");
			double longitude = Double.parseDouble(longitueS);
			double latitude = Double.parseDouble(latitudeS);
			double distance = RecentUtils.distFrom(AppConfiguration.getInstance().getLatitude(), AppConfiguration.getInstance().getLongitude(), latitude, longitude);
			String distanceS = distance+" M";
			if(distance>1000){
				distanceS = (long)Math.floor((distance/1000) + 0.5d)+" KM";
			} else {
				distanceS = (long)Math.floor(distance + 0.5d)+" M";
			}
			
			apiAvatarThumb = dataUser.getString("avatar");
			apiAvatarFull = dataUser.getString("avatar_full");
			
			aq.id(imgProfile).image(apiAvatarThumb, false, true);
			
			
			txtDistance.setText(distanceS);
			//Time
			String oldstring = dataUser.getString("created");
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oldstring);
			String newstring = new SimpleDateFormat("dd MMM yyyy").format(date);
			txtTime.setText("Register since : \n"+newstring);
			//Gender
			boolean userIsMale = !dataUser.getString("gender").equals("wanita");
			if(userIsMale){
				frameGender.setBackgroundResource(R.color.BiruNdogAsin);
			} else {
				frameGender.setBackgroundResource(R.color.Pink);
			}
			//ISFRIEND AND BLOCKED
			isFriend = dataUser.getString("is_friend").equals("true");
			isBlock = dataUser.getString("is_blocked").equals("true");
			toggleIsFriendView();
			//Photos
			String jsaString = json.getString("PHOTOS");
			if(jsaString.length()>=10){
				JSONArray jsarray = json.getJSONArray("PHOTOS");
				userPhotos.clear();
				if(jsarray!=null){
					for (int i = 0; i < jsarray.length(); i++) {
						String imageUrl = jsarray.getJSONObject(i).getString("image_url");
						userPhotos.add(imageUrl);
					}
					if(userPhotos.size()>=1){
						noItem.setVisibility(View.GONE);
						gridPhotos.setVisibility(View.VISIBLE);
						adapterPhotos.setList(userPhotos);
						gridPhotos.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								// TODO Auto-generated method stub
								Intent i = new Intent(ProfileDetailActivity.this, PhotoFromWebActivity.class);
								i.putExtra("photourl", userPhotos.get(arg2));
								startActivity(i);
							}
						});
					}
				}
			}
		} catch (Exception e) {
			failedLoadProfile("Please ContactSupport #3 \n "+e.getMessage());
		}
	}	
	
	private void failedLoadProfile(final String message){
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(context, "Error Loading Profile... \n"+message, Toast.LENGTH_SHORT).show();
			}
		});		
	}	

	public static Intent createIntent(Context context, String account, String user) {
		return new EntityIntentBuilder(context, ProfileDetailActivity.class).setAccount(account).setUser(user).build();
	}
	
	


	private static String getUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}
	
	private void sendAttention(){
		MessageManager.getInstance().sendMessage(abstractContact.getAccount(),abstractContact.getUser(), AppConstants.UniqueKeyAttention);
		startActivity(ChatViewActivity.createIntent(this, abstractContact.getAccount(),abstractContact.getUser()));
		finish();
	}
	
	private void openChat(){
		startActivity(ChatViewActivity.createIntent(this, abstractContact.getAccount(),abstractContact.getUser()));
		finish();
	}
	
	private void btnAddFriendClick(){
		if(isFriend){
			SmilesConfirmDialog dialog =new SmilesConfirmDialog(context, new OnSmilesDialogClose() {
				@Override
				public void onSmilesDialogClose(boolean isConfirm) {
					if(isConfirm){
						doPostUnFriends();
					}
				}
			}, "COMFIRM", "Are You Sure To Un Friend "+StringUtils.replaceStringEquals(user)+" ?");
			dialog.show();
		} else {
			SmilesConfirmDialog dialog =new SmilesConfirmDialog(context, new OnSmilesDialogClose() {
				@Override
				public void onSmilesDialogClose(boolean isConfirm) {
					if(isConfirm){
						doPostAddFriends();
					}
				}
			}, "COMFIRM", "Are You Sure To Add "+StringUtils.replaceStringEquals(user)+" As Friend ?");
			dialog.show();
		}
	}
	
	private void btnAddBlockedClicked(){
		if(isBlock){
			SmilesConfirmDialog dialog =new SmilesConfirmDialog(context, new OnSmilesDialogClose() {
				@Override
				public void onSmilesDialogClose(boolean isConfirm) {
					if(isConfirm){
						doPostBlockedFriends();
					}
				}
			}, "COMFIRM", "Are You Sure UnBlock "+StringUtils.replaceStringEquals(user)+" ?");
			dialog.show();
		} else {
			SmilesConfirmDialog dialog =new SmilesConfirmDialog(context, new OnSmilesDialogClose() {
				@Override
				public void onSmilesDialogClose(boolean isConfirm) {
					if(isConfirm){
						doPostBlockedFriends();
					}
				}
			}, "COMFIRM", "Are You Sure Block "+StringUtils.replaceStringEquals(user)+" ?");
			dialog.show();
		}


	}
	
	private void doPostAddFriends(){
		List<NameValuePair> postDatanya = new ArrayList<NameValuePair>(2);
		postDatanya.add(new BasicNameValuePair("username", AccountManager.getInstance().getAccountKu()));
		postDatanya.add(new BasicNameValuePair("friendname", StringUtils.replaceStringEquals(user)));
		doPostAsync(context, AppConstants.APIAddFriends, postDatanya, null, true);
	}
	
	private void doPostUnFriends(){
		List<NameValuePair> postDatanya = new ArrayList<NameValuePair>(2);
		postDatanya.add(new BasicNameValuePair("username", AccountManager.getInstance().getAccountKu()));
		postDatanya.add(new BasicNameValuePair("rel_code", StringUtils.replaceStringEquals(user)));
		doPostAsync(context, AppConstants.APIDeleteFriends, postDatanya, null, true);
	}
	
	private void doPostBlockedFriends(){
		List<NameValuePair> postDatanya = new ArrayList<NameValuePair>(3);
		postDatanya.add(new BasicNameValuePair("username", AccountManager.getInstance().getAccountKu()));
		postDatanya.add(new BasicNameValuePair("targetname", StringUtils.replaceStringEquals(user)));
		if(isBlock){
			postDatanya.add(new BasicNameValuePair("flag", "N"));
		} else {
			postDatanya.add(new BasicNameValuePair("flag", "Y"));
		}
		doPostAsync(context, AppConstants.APIBlocked, postDatanya, null, true);
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
	
	@Override
	public void onClick(View v) {
		if(v==btnBack){
			finish();
		} else if(v==btnAddFriend){
			if(!isMe)
				btnAddFriendClick();
			else
				startActivity(ProfileSettingsActivity.createIntent(this));
		}else if(v==btnSendAttention){
			sendAttention();
		}else if(v==btnChat){
			openChat();
		}else if(v==btnBlockFriend){
			if(isMe)
				showCaptureFrom();				
			else
				btnAddBlockedClicked();
		}
		else if(v==profleBtnCloseSelect){
			hideCaptureFrom();
		}else if(v==profleBtnCamera){
			selectFrom(PICK_FROM_CAMERA);
		}else if(v==profileBtnGallery){
			selectFrom(PICK_FROM_GALLERY);
		}else if( v == imgProfile){
			Intent i = new Intent(ProfileDetailActivity.this, PhotoFromWebActivity.class);
			i.putExtra("photourl", apiAvatarFull);
			startActivity(i);
		}
	}
	
	private void selectFrom(int from){
		if(from==PICK_FROM_CAMERA){
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "smiles_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
	        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
	        try {
	        intent.putExtra("return-data", true);
	        startActivityForResult(intent, from);
	        } catch (ActivityNotFoundException e) {
	        	e.printStackTrace();
	        }
		} else {
			Intent intent = new Intent();
			intent.setType("image/*");
		    intent.setAction(Intent.ACTION_GET_CONTENT);
		    startActivityForResult(Intent.createChooser(intent, "Complete action using"), from); 
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			if(requestCode==PICK_FROM_CAMERA){
				doCrop();
			} else if(requestCode==PICK_FROM_GALLERY){
				mImageCaptureUri = data.getData();
				doCrop();
			} else if(requestCode==CROP_FROM_CAMERA){
				Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    //avatar.setImageBitmap(photo);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    fileAvatar = new File(Environment.getExternalStorageDirectory()+ File.separator +"phototoupload.jpg");
                    try {
						fileAvatar.createNewFile();
						FileOutputStream fo = new FileOutputStream(fileAvatar);;
						fo.write(bytes.toByteArray());
						fo.close();
						
						Map<String, Object> params = new HashMap<String, Object>();
				        params.put("username", AccountManager.getInstance().getAccountKu());				       
				        params.put("image", fileAvatar);
				        params.put("friend_only", "N");	  
				        
						progressDialog = new SmilesProgressDialog(context, null);
						progressDialog.setCanceledOnTouchOutside(false);
						AQuery aq = new AQuery(getApplicationContext());
				        aq.progress(progressDialog).ajax(AppConstants.APIUserUploadPhoto, params, JSONObject.class, this, "callbackPost");
						
					} catch (IOException e) {
						Log.e(TAG, e.getMessage());
					}
                }
                hideCaptureFrom();
			}else {
				hideCaptureFrom();
			}
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
	public void callbackPost(String url, JSONObject json, AjaxStatus status) {
		
		if(json!=null){
			Log.i(TAG, json.toString());
			try {
				String statusJson = json.getString("STATUS");
				if(statusJson.equals("SUCCESS")){
					Toast.makeText(this, statusJson, Toast.LENGTH_LONG).show();
					loadProfile(true);
				} else {
					String message = json.getString("MESSAGE");
					Toast.makeText(this, getString(R.string.UploadFailedTitle)+", "+message, Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				//failedUpdateProfile("Please Contact Support");
				Toast.makeText(this, getString(R.string.UploadFailedTitle), Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, getString(R.string.UploadFailedTitle), Toast.LENGTH_LONG).show();
		}
	}
}
