package com.digitalbuana.smiles.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.ContactAdapter;
import com.digitalbuana.smiles.adapter.chat.ChatMessageAdapter;
import com.digitalbuana.smiles.awan.helper.PhoneContactHelper;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.dialog.SmilesDefaultDialog;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.RecentUtils;

public class UploadFileActivity extends ManagedActivity
implements
View.OnClickListener
{

	public static final int TYPE_GALLERY_IMAGE=0;
	public static final int TYPE_GALLERY_VIDEO=1;
	public static final int TYPE_GALLERY_AUDIO=2;
	public static final int TYPE_CAMERA_IMAGE=3;
	public static final int TYPE_CONTACT=4;
	
	private final static int RESULT_CODE_GALLERY_CODE = 111111;
	private final static int RESULT_CODE_CAMERA_CODE = 111112;
	
	private String actionWithAccount;
	private String actionWithUser;
	private int type=0;
	
	//VIew
	private FrameLayout btnBack, uploadFileViewTemp;
	private FrameLayout btnSend, general_list_header;
	private ImageView thumbUpload;
	private LinearLayout viewConteiner;
	private ProgressBar progressView;
	private EditText editDesc;
	
	private File fileSend;
	private File fileThumb;
	private static boolean isSelect=false;
	private String TAG = getClass().getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_upload_file);
		uploadFileViewTemp = (FrameLayout)findViewById(R.id.uploadFileViewTemp);
		
		Intent intent = getIntent();
		actionWithAccount = AccountManager.getInstance().getAccountKu();
		actionWithUser = intent.getStringExtra("user");
		type = intent.getIntExtra("type", 0);
		if(actionWithUser==null||actionWithAccount==null){
			finish();			
		}
		isSelect=false;
		//Casting 
		rootView = (FrameLayout)findViewById(R.id.uploadFileRootView);
		btnBack = (FrameLayout)findViewById(R.id.uploadFileBtnBack);
		btnSend = (FrameLayout)findViewById(R.id.uploadFileBtnSend);
		progressView = (ProgressBar)findViewById(R.id.uploadFileProgress);
		viewConteiner = (LinearLayout)findViewById(R.id.uploadFileContainer);
		thumbUpload = (ImageView)findViewById(R.id.uploadFileThumb);
		editDesc = (EditText)findViewById(R.id.uploadFileEditDesc);
		//Listener
		btnBack.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		
		progressView.setVisibility(View.GONE);
		
		if(!isSelect){
			switch (type) {
				case TYPE_GALLERY_IMAGE:selectImage();isSelect=true;break;
				case TYPE_GALLERY_VIDEO:selectVideo();isSelect=true;break;
				case TYPE_GALLERY_AUDIO:selectAudio();isSelect=true;break;
				case TYPE_CAMERA_IMAGE:selectCamera();isSelect=true;break;
				case TYPE_CONTACT:selectContact();break;
			}
		}		
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
	}	
	
	public static Intent createIntent(Context context) {
		return new Intent(context, UploadFileActivity.class);
	}
	
	@Override
	public void onClick(View v) {
		if( v == rootView || v == btnBack ){
			finish();
		} else if(v==btnSend){
			postUploadImage();
		}
	}


	private void selectImage(){
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
	    photoPickerIntent.setType("image/*");
	    photoPickerIntent.putExtra("type", ChatMessageAdapter.TYPE_IMAGE);
	    startActivityForResult(photoPickerIntent, RESULT_CODE_GALLERY_CODE);
	}
	
	private void selectContact(){
		final String[][] strArrList = PhoneContactHelper.getContactList(this);		
		
		if(strArrList!=null){
			
			uploadFileViewTemp.setVisibility(View.GONE);
			
			final Dialog contactDialog = new Dialog(this);
			contactDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); 
			contactDialog.setContentView(R.layout.general_listview); 
			contactDialog.setCancelable(true);
			general_list_header = (FrameLayout)contactDialog.findViewById(R.id.general_list_header);
			general_list_header.setVisibility(View.GONE);
			ListView contactList = (ListView)contactDialog.findViewById(R.id.search_result_list);							
			ContactAdapter contactAdapter = new ContactAdapter(this, strArrList);
			contactList.setAdapter(contactAdapter);
			contactAdapter.notifyDataSetChanged();							
			contactList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0,
						View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					contactDialog.dismiss();
					String textToSend = strArrList[0][arg2]+", "+strArrList[1][arg2];
					MessageManager.getInstance().sendMessage(actionWithAccount,actionWithUser, textToSend);
					finish();
					isSelect=false;
				}
			});
			contactDialog.show();
		}else{
			Toast.makeText(this, R.string.failed_to_get_contact_list, Toast.LENGTH_LONG).show();
		}
	}
	private void selectVideo(){
		try{
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		    photoPickerIntent.setType("video/*");
		    photoPickerIntent.putExtra("type", ChatMessageAdapter.TYPE_VIDEO);
		    startActivityForResult(photoPickerIntent, RESULT_CODE_GALLERY_CODE);
		}catch(ActivityNotFoundException e){
			Toast.makeText(this, "Your device not supporting this action.", Toast.LENGTH_LONG).show();
		}		
	}
	private void selectAudio(){
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("audio/*");
		photoPickerIntent.putExtra("type", ChatMessageAdapter.TYPE_AUDIO);
	    startActivityForResult(photoPickerIntent, RESULT_CODE_GALLERY_CODE);
	}
	private void selectCamera(){
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
        startActivityForResult(cameraIntent, RESULT_CODE_CAMERA_CODE); 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			if(requestCode==RESULT_CODE_GALLERY_CODE){
			final Uri photoUri = data.getData();
			 if (photoUri != null){
				String[] filePathColumn = {MediaStore.Images.Media.DATA};
	        	Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null); 
	            cursor.moveToFirst();
	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            final String filePath = cursor.getString(columnIndex);
	            cursor.close();
	            fileSend  = new File(filePath);
	            if(type==TYPE_GALLERY_VIDEO){	            	
		            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.FULL_SCREEN_KIND);
		            thumbUpload.setImageBitmap(bitmap);
		            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
		            fileThumb = new File(Environment.getExternalStorageDirectory()+ File.separator +"videoThumb.jpg");
		            try {
		            	fileThumb.createNewFile();
			            FileOutputStream fo = new FileOutputStream(fileThumb);
			            fo.write(bytes.toByteArray());
			            fo.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
	            }else if(type==TYPE_GALLERY_AUDIO){
	            	thumbUpload.setImageResource(R.drawable.icon_audio);
	            }else {
	            	thumbUpload.setImageBitmap(BitmapFactory.decodeFile(filePath));
	            }
	            Log.i(TAG, "File Send name : "+fileSend.getName());
			 }
			} else if(requestCode==RESULT_CODE_CAMERA_CODE){
				 Bitmap photo = (Bitmap) data.getExtras().get("data"); 
				 thumbUpload.setImageBitmap(photo);
		         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		         photo.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
		         fileSend = new File(Environment.getExternalStorageDirectory()+ File.separator +"videoThumb.jpg");
		         try {
		        	 fileSend.createNewFile();
			         FileOutputStream fo = new FileOutputStream(fileSend);
			         fo.write(bytes.toByteArray());
			         fo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			finish();
			isSelect=false;
		}
	}
	private void postUploadImage(){
		if(RecentUtils.checkNetwork()||fileSend!=null){
			
			long fileSize = fileSend.length();			
			Log.e("postUploadImage", " ::: fileSize : "+fileSize);
			
			String url = AppConstants.APIUploadFile;
			Map<String, Object> params = new HashMap<String, Object>();
	        params.put("username", AccountManager.getInstance().getAccountKu());
	        params.put("type", "private");
	        params.put("file", fileSend);
	        if(fileThumb!=null){
	        	Log.e(TAG, "thumb : "+fileThumb.getName());
	        	params.put("thumb", fileThumb);
	        }
	        if(editDesc.getText().toString().length()>=1){
		        params.put("description", editDesc.getText().toString());
	        }
	        AQuery aq = new AQuery(getApplicationContext());
	        aq.ajax(url, params, JSONObject.class, this, "callbackPost");
	        viewConteiner.setVisibility(View.GONE);
	        progressView.setVisibility(View.VISIBLE);
		} else {
			finish();
			isSelect=false;
		}
	}
	
	public void callbackPost(String url, JSONObject json, AjaxStatus status) {		
		progressView.setVisibility(View.GONE);
		viewConteiner.setVisibility(View.VISIBLE);
		if(json!=null){		
				try {
					String statusJson = json.getString("STATUS");
					if(statusJson.equals("SUCCESS")){
						String urlImage = AppConstants.APIHost+json.getString("URL");
						String desc = json.getString("DESC");
						String thumb =  AppConstants.APIHost+json.getString("THUMB");
						successUploadImage(urlImage,desc,thumb);
					} else {
						String message = json.getString("MESSAGE");
						failedUploadImage(message);
					}
				} catch (JSONException e) {
					failedUploadImage("JSONException");
				}
		   } else {
			   failedUploadImage("file size that you send through the standard limit");
		   }
		}

	private void successUploadImage(String url, String desc,String thumb){
		String textToSend = "";
		if(type==TYPE_GALLERY_VIDEO){
			textToSend = AppConstants.UniqueKeyFileVideo+desc+AppConstants.UniqueKeyURL+url+AppConstants.UniqueKeyTHUMB+thumb;
		} else if(type==TYPE_GALLERY_AUDIO){
			textToSend = AppConstants.UniqueKeyFileAudio+desc+AppConstants.UniqueKeyURL+url+AppConstants.UniqueKeyTHUMB+thumb;
		}else {
			textToSend = AppConstants.UniqueKeyFileImage+desc+AppConstants.UniqueKeyURL+url+AppConstants.UniqueKeyTHUMB+thumb;
			
		}
		MessageManager.getInstance().sendMessage(actionWithAccount,actionWithUser, textToSend);
		finish();
		isSelect=false;
	}

	private void failedUploadImage(String message){
		SmilesDefaultDialog dialog = new SmilesDefaultDialog(context, "SEND FILE FAILED", "\n PLEASE CONTACT SUPPORT \n"+message);
		dialog.show();
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				finish();
			}
		});
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
		finish();
		isSelect=false;
	}

	
	@Override
	protected void resettingView() {
		super.resettingView();
		thumbUpload.getLayoutParams().height =  viewHeight/3;
	}
}
