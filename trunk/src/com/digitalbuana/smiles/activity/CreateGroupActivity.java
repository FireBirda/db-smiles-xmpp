package com.digitalbuana.smiles.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.packet.VCard;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.adapters.GroupInviteAdapter;
import com.digitalbuana.smiles.awan.helper.ScreenHelper;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.data.intent.EntityIntentBuilder;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.notification.NotificationManager;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.FontUtils;

public class CreateGroupActivity extends ManagedActivity implements OnClickListener {

	private static final String ACTION_MUC_INVITE = "com.digitalbuana.smiles.data.MUC_INVITE";

	private static final String SAVED_ACCOUNT = "com.digitalbuana.smiles.activity.MUCEditor.SAVED_ACCOUNT";
	private static final String SAVED_ROOM = "com.digitalbuana.smiles.activity.MUCEditor.SAVED_ROOM";

	private static final int DIALOG_MUC_INVITE_ID = 100;

	private String account;
	private String room;
	
	private FrameLayout btnBack, profileBlackCaptureFrom;
	private FrameLayout btnCreate;
	private EditText editName;
	private String myAccount;
	private GroupInviteAdapter contactListAdapter = null;
	private ListView listView;
	private String TAG = getClass().getSimpleName();
	private EditText searchTextGroupCreate;
	public static TextView memberCounterTextView;
	private ImageView groupAvatarCreate;
	
	private int PICK_FROM_CAMERA = 8572187;
	private int PICK_FROM_GALLERY = 8572188;
	private int CROP_FROM_CAMERA = 8572189;
	
	private FrameLayout btnCaptureCamera;
	private FrameLayout btnCaptureGallery;
	private FrameLayout profleBtnCloseSelect;
	
	private Uri mImageCaptureUri;
	private static final int ICON_SIZE = 100;
	
	private File fileAvatar;
	
	private int isAvatarChange=0;	
	
	private CheckBox onlyAdminCanInvite;
	
	private Dialog waitDialog;
	private AQuery aq;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing())
			return;
		
		aq = new AQuery(this);		
		waitDialog = ScreenHelper.getDialogProgress(this);		
		myAccount = AccountManager.getInstance().getAccountKu();
		
		setContentView(R.layout.activity_create_group);
		
		onlyAdminCanInvite = (CheckBox)findViewById(R.id.only_admin_can_invite_checkbox);
		
		btnCaptureCamera = (FrameLayout)findViewById(R.id.profleBtnCamera);
		btnCaptureGallery = (FrameLayout)findViewById(R.id.profileBtnGallery);
		profleBtnCloseSelect = (FrameLayout)findViewById(R.id.profleBtnCloseSelect);
		
		btnCaptureCamera.setOnClickListener(this);
		btnCaptureGallery.setOnClickListener(this);
		profleBtnCloseSelect.setOnClickListener(this);
		
		profileBlackCaptureFrom = (FrameLayout)findViewById(R.id.profileBlackCaptureFrom);
		
		groupAvatarCreate = (ImageView)findViewById(R.id.groupAvatarCreate);
		groupAvatarCreate.setOnClickListener(this);
		
		searchTextGroupCreate = (EditText)findViewById(R.id.searchTextGroupCreate);
		memberCounterTextView = (TextView)findViewById(R.id.memberCounterTextView);
		
		listView = (ListView)findViewById(R.id.createGroupFriendList);
		
		LinearLayout rootView = (LinearLayout)findViewById(R.id.createGroupRootView);
		btnBack = (FrameLayout)findViewById(R.id.createGroupBtnBack);
		
		btnCreate = (FrameLayout)findViewById(R.id.createGroupBtnCreate);
		editName = (EditText)findViewById(R.id.createGroupEditName);
				
		btnBack.setOnClickListener(this);
		btnCreate.setOnClickListener(this);	
		
		editName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				String currentText = s.toString();
				if(currentText.contains(" ")){
					Toast.makeText(CreateGroupActivity.this, getString(R.string.space_on_diclaimer), Toast.LENGTH_LONG).show();
					currentText = currentText.replace(" ", "").toLowerCase();
					editName.setText(currentText);
				}
			}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		editName.setOnFocusChangeListener(new OnFocusChangeListener() {			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus){
					String lastChar = editName.getText().toString().trim().toLowerCase();
					editName.setText(lastChar);
				}
			}
		});
		
		FontUtils.setRobotoFont(context, rootView);
		
		account = AccountManager.getInstance().getActiveAccount().getAccount();
		
		Intent intent = getIntent();
		if (savedInstanceState != null) {
			room = savedInstanceState.getString(SAVED_ROOM);
		} else {
			room = getUser(intent);
		}
		getContactList();
		
	}
	
	private void getContactList(){
		contactListAdapter = new GroupInviteAdapter(CreateGroupActivity.this, R.layout.item_friend_select);
		listView.setAdapter(contactListAdapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);	
		if(!contactListAdapter.isEmpty()){
			searchTextGroupCreate.addTextChangedListener(new friendFiltering());
			LayoutParams params = listView.getLayoutParams();
			params.height = 98 * contactListAdapter.getCount();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_ACCOUNT, account);
		outState.putString(SAVED_ROOM, room);
	}	
	
	@SuppressLint("DefaultLocale")
	private void createGroup(){
		
		Calendar c = Calendar.getInstance(); 
		int seconds = c.get(Calendar.SECOND);
		int hour = c.get(Calendar.HOUR);
		int day = c.get(Calendar.DAY_OF_YEAR);
		int month = c.get(Calendar.MONTH);
		int year = c.get(Calendar.YEAR);
		String uniqueKey = AppConstants.UniqueKeyGroup+seconds+""+hour+""+day+""+month+""+year;

		AccountItem account = AccountManager.getInstance().getActiveAccount();
		String serverName = AppConstants.XMPPGroupsServer;
		String roomName = editName.getText().toString()+uniqueKey;
		String room = roomName.toLowerCase();
		room = room + "@" + serverName;
		
		String nickname = AccountManager.getInstance().getNickName(account.getAccount());
		
		if (this.account != null && this.room != null)
			if (!account.equals(this.account) || !room.equals(this.room)) {
				MUCManager.getInstance().removeRoom(this.account, this.room);
				MessageManager.getInstance().closeChat(this.account,this.room);
				NotificationManager.getInstance().removeMessageNotification(this.account, this.room);
			}
		
		MUCManager.getInstance().createRoom(account.getAccount(), room, nickname, "", true);
		waitingToSendInvitation(room);		
		sendBookmark(room);
	}
	
	private void sendBookmark(String value){
		
		Map<String, String> parms = new HashMap<String, String>();
		parms.put("r", AppConstants.ofSecret);
		parms.put("bn", value+" by " + this.account);
		parms.put("bv", value);
		parms.put("bu", this.account);
		
		aq.ajax(AppConstants.APIAddBookmark, parms, String.class, new AjaxCallback<String>(){
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				// TODO Auto-generated method stub
				super.callback(url, object, status);
				if(object != null){
					Log.w(TAG, object);
				}
			}
		});
	}
	
	private void waitingToSendInvitation(final String roomName){		
		waitDialog.show();		
		new CountDownTimer(3000, 1000) {		
			@Override
			public void onTick(long millisUntilFinished) {}			
			@Override
			public void onFinish() {
				if(MUCManager.getInstance().hasRoom(account, roomName)){
					sendInvitation(roomName);
				}
			}
		}.start();				
	}
	
	private void sendInvitation(String roomName){
		if(contactListAdapter != null){
			Vector<String> buffNum = contactListAdapter.getBuffName();
			if(buffNum.size()>0){
				for(int a = 0; a < buffNum.size(); a++){
					try {																
						String friendToInvite = buffNum.get(a);									
						MUCManager.getInstance().invite(myAccount, roomName, friendToInvite);
					} catch (NetworkException e) {}
				}
				setGroupAvatar(roomName);
			}else
				Log.e(TAG, " ::: buffNum == 0");
		}else
			Log.e(TAG, " ::: contactListAdapter == null");	
		if(waitDialog.isShowing())
			waitDialog.dismiss();
		finish();
	}
	
	private void setGroupAvatar(final String roomName){
		if(isAvatarChange==1){
			final VCard groupVcard = new VCard();
			groupVcard.setType(Type.SET);
			final Connection connection = AccountManager.getInstance().getActiveAccount().getConnectionThread().getXMPPConnection();
			new Thread(){
				public void run(){
					try {
						final byte[] bumapByt;
						groupVcard.load(connection, roomName);
						Drawable drawable = groupAvatarCreate.getDrawable();
						Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
						Bitmap resized = Bitmap.createScaledBitmap(bmp, 128, 128, true);
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						resized.compress(Bitmap.CompressFormat.PNG, 80, stream);
						bumapByt = stream.toByteArray();
						groupVcard.setAvatar(bumapByt);
						groupVcard.save(connection);
						Presence presence = new Presence(Presence.Type.available);
						String vurbose = PresenceManager.getInstance().getVurbose(AccountManager.getInstance().getAccountKu(), AccountManager.getInstance().getActiveAccount().getRealJid());
		                presence.setFrom(vurbose);
		                Log.d(TAG,"Vurbose Name :" + vurbose);
		                final String lastAvatar = groupVcard.getAvatarHash();
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
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					} catch (NetworkException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}finally{
						this.interrupt();
					}
				}
			}.start();
		}
	}
	
	@Override
	public void onClick(View v) {
		if(v==btnBack){
			finish();
		} else if(v==btnCreate){
			if(!editName.getText().toString().equals(""))
				createGroup();
			else
				showToast("Group name can't be empty");
		} else if( v == groupAvatarCreate){
			showPopupForGroupAvatar();
		}else if( v == btnCaptureCamera ){
			selectFrom(PICK_FROM_CAMERA);
		} else if(v==btnCaptureGallery){
			selectFrom(PICK_FROM_GALLERY);
		} else if  ( v == profleBtnCloseSelect ){
			profileBlackCaptureFrom.setVisibility(View.GONE);
		}
	}
	
	private void selectFrom(int from){
		if(from==PICK_FROM_CAMERA){
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);			
	        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "smiles_group_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
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
                    groupAvatarCreate.setImageBitmap(photo);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
                    fileAvatar = new File(Environment.getExternalStorageDirectory()+ File.separator +"smilesGroupAvatar.jpg");
                    try {
						fileAvatar.createNewFile();
						FileOutputStream fo = new FileOutputStream(fileAvatar);;
						fo.write(bytes.toByteArray());
						fo.close();
						
					} catch (IOException e){ /*failedUpdateProfile("Error Updating Profile");*/ }
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
	
	private void hideCaptureFrom(){
		if(profileBlackCaptureFrom.getVisibility()==View.VISIBLE){
			profileBlackCaptureFrom.setVisibility(View.GONE);
			profileBlackCaptureFrom.startAnimation(fadeout);
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
	
	private void showPopupForGroupAvatar(){
		profileBlackCaptureFrom.setVisibility(View.VISIBLE);
	}
	
	public static Intent createIntent(Context context) {
		return new Intent(context, CreateGroupActivity.class);
	}
	public static Intent createIntent(Context context, String account,String room) {
		return new EntityIntentBuilder(context, CreateGroupActivity.class).setAccount(account).setUser(room).build();
	}
	public static Intent createInviteIntent(Context context, String account, String user) {
		Intent intent = createIntent(context, account, user);
		intent.setAction(ACTION_MUC_INVITE);
		return intent;
	}
	private static String getUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}
	private void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	private class friendFiltering implements TextWatcher{
		@Override
		public void afterTextChanged(Editable arg0) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			//if( contactListAdapter.getCount() > 0 ){
				contactListAdapter.getFilter().filter(s);
			//}
		}
	}
}
