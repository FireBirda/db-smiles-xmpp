package com.digitalbuana.smiles.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.RequestFriendsAdapter;
import com.digitalbuana.smiles.adapter.RequestFriendsAdapter.OnFriendsReqListener;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.NetworkException;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.dialog.SmilesDefaultDialog;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.StringUtils;

public class BlockedFriendsActivity extends ManagedActivity
implements
OnClickListener, OnFriendsReqListener
{
	
	private FrameLayout btnBack;
	private RequestFriendsAdapter blockedAdapter;
	
	
	private GridViewKu gridView;
	private FrameLayout btnAdd;
	private EditText editName;
	
	private static boolean IsBlockDo = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_blocked_friends);
		
		rootView = (FrameLayout)findViewById(R.id.blockedFriendsRootView);
		btnBack = (FrameLayout)findViewById(R.id.blockedFriendsBtnBack);
		
		gridView = (GridViewKu)findViewById(R.id.blockedFriendsGridView);
		btnAdd = (FrameLayout)findViewById(R.id.blockedFriendsBtnAdd);
		editName = (EditText)findViewById(R.id.blockedFriendsEditName);
		
		btnBack.setOnClickListener(this);
		btnAdd.setOnClickListener(this);
		
		
		FontUtils.setRobotoFont(context, rootView);
		
		blockedAdapter= new RequestFriendsAdapter(context,2,this);
		gridView.setAdapter(blockedAdapter);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshingBlocked();
		blockedAdapter.notifyDataSetChanged();
	}
	
	private void refreshingBlocked(){
		InputMethodManager imm  = (InputMethodManager)context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	    if(imm.isActive()){
	    	imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0); 
	    }
	    List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
	    String username= AccountManager.getInstance().getActiveAccount().getAccount();
		postData.add(new BasicNameValuePair("username",  username));
		doPostAsync(context, AppConstants.APIBlockedList, postData, null, true);
	}
	
	private void addToBlocked(boolean isBlock, String targetname){
	    List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
	    String username= AccountManager.getInstance().getActiveAccount().getAccount();
		postData.add(new BasicNameValuePair("username",  username));
		postData.add(new BasicNameValuePair("targetname", targetname));
		if(isBlock){
			postData.add(new BasicNameValuePair("flag", "Y"));
		} else {
			postData.add(new BasicNameValuePair("flag", "N"));
		}
		IsBlockDo=isBlock;
		doPostAsync(context, AppConstants.APIBlocked, postData, null, true);
	}
	
	@Override
	protected void finishAsync(String result) {
		super.finishAsync(result);
		if(urlKu==AppConstants.APIBlocked){
			try {
				JSONObject jsonKu = new JSONObject(result);
				String status = jsonKu.getString("STATUS").toUpperCase(Locale.getDefault());
				String message = jsonKu.getString("MESSAGE").toUpperCase(Locale.getDefault());
				if(status.equals("SUCCESS")){
					if(IsBlockDo){
						doBlocking(editName.getText().toString()+"@"+AppConstants.XMPPServerHost);
					} else {
						doUnBlocking(editName.getText().toString()+"@"+AppConstants.XMPPServerHost);
					}
				} else {
					SmilesDefaultDialog dialog = new SmilesDefaultDialog(context, "ERROR", message);
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();
				}
			} catch (Exception e) {
				SmilesDefaultDialog dialog = new SmilesDefaultDialog(context, "ERROR", "PLEASE CONTACT SUPPORT");
				dialog.setCanceledOnTouchOutside(true);
				dialog.show();
			}
			
		} else if(urlKu==AppConstants.APIBlockedList){
			try {
				JSONObject json = new JSONObject(result);
				JSONArray usersArray = json.optJSONArray("DATA");
				if(usersArray!=null){
					for(int x =0; x<usersArray.length();x++){
						String nama = usersArray.getJSONObject(x).getString("username");
						doBlocking(nama+"@"+AppConstants.XMPPServerHost);
					}
				}
			} catch (Exception e) {
				
			}
		}
		blockedAdapter.notifyDataSetChanged();
	
	}
	
	
	
	public static Intent createIntent(Context context) {
		return new Intent(context, BlockedFriendsActivity.class);
	}
	
	@Override
	public void onClick(View v) {
		if(v==btnBack){
			finish();
		} else if(v==btnAdd){
			if(editName.getText().toString().length()>=2){
				InputMethodManager imm  = (InputMethodManager)context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			    if(imm.isActive()){
			    	imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0); 
			    }
				addToBlocked(true,editName.getText().toString());
			} else {
			editName.startAnimation(shake);
			}
		}
	}

	private void doBlocking(String jid){
		String account = AccountManager.getInstance().getAccountKu();
		try {
			RosterManager.getInstance().removeContact(account, jid);
			PresenceManager.getInstance().discardSubscription(account, jid);
			MessageManager.getInstance().closeChat(account, jid);
			
			
			FriendsManager.getInstance().getFriendsListManager().removeFriendsJID(jid);
			FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().removeFriendsJID(jid);
			FriendsManager.getInstance().getFriendsWaitingMeApproveManager().removeFriendsJID(jid);
			
			FriendsManager.getInstance().getFriendsBlockedManager().addFriendsByJID(jid);
			
			blockedAdapter.notifyDataSetChanged();
			

		} catch (NetworkException e) {
			Application.getInstance().onError(e);
			return;
		}
	}
	
	private void doUnBlocking(String jid){
		FriendsManager.getInstance().getFriendsBlockedManager().removeFriendsJID(jid);
		blockedAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onReqFriendsListener(int type, String jid) {
		addToBlocked(false,StringUtils.replaceStringEquals(jid));
		FriendsManager.getInstance().getFriendsBlockedManager().removeFriendsJID(jid);
		blockedAdapter.notifyDataSetChanged();
	}
}
