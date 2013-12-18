package com.digitalbuana.smiles.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.CheckedFriendsAdapter;
import com.digitalbuana.smiles.adapter.CheckedGroupsAdapter;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.roster.RosterContact;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.ui.helper.BaseUIActivity;
import com.digitalbuana.smiles.ui.widget.GridViewKu;
import com.digitalbuana.smiles.utils.FontUtils;
import com.digitalbuana.smiles.utils.ViewUtilities;

public class BroadcastMesssageActivity extends BaseUIActivity
implements
OnClickListener
{

	private FrameLayout containerBroadcast;
	private FrameLayout btnClose;
	
	private int indexSelect=0;
	private GridViewKu gridSelect;
	private FrameLayout btnSelectAll;
	private FrameLayout btnSelectGroup;
	private FrameLayout btnSelectCustom;
	private TextView txtSelectAll;
	private TextView txtSelectGroup;
	private TextView txtSelectCustom;
	
	
	private EditText editMessage;
	private FrameLayout btnSend;
	
	private Animation fadein;
	
	private CheckedFriendsAdapter checekdFriendsAdapter;
	private CheckedGroupsAdapter checekdGroupsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_broadcast_message);
		rootView = (FrameLayout)findViewById(R.id.broadcastRootView);
		
		//Casting
		containerBroadcast = (FrameLayout)findViewById(R.id.broadcastContainer);
		btnClose = (FrameLayout)findViewById(R.id.broadcastBtnClose);
		
		
		editMessage = (EditText)findViewById(R.id.broadcastEditMessage);
		btnSend = (FrameLayout)findViewById(R.id.broadcastBtnSend);
		
		gridSelect = (GridViewKu)findViewById(R.id.broadcastGridSelect);
		btnSelectAll = (FrameLayout)findViewById(R.id.broadcastBtnSelectAll);
		btnSelectGroup = (FrameLayout)findViewById(R.id.broadcastBtnSelectGroup);
		btnSelectCustom = (FrameLayout)findViewById(R.id.broadcastBtnSelectCustom);
		txtSelectAll = (TextView)findViewById(R.id.broadcastTxtSelectAll);
		txtSelectGroup = (TextView)findViewById(R.id.broadcastTxtSelectGroup);
		txtSelectCustom = (TextView)findViewById(R.id.broadcastTxtSelectCustom);
		
		//Setting Listener
		btnClose.setOnClickListener(this);
		rootView.setOnClickListener(this);
		btnSelectAll.setOnClickListener(this);
		btnSelectGroup.setOnClickListener(this);
		btnSelectCustom.setOnClickListener(this);
		btnSend.setOnClickListener(this);		

		//Anim
		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
		fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		
		final ImageView kakatuaView = (ImageView)findViewById(R.id.broadcasImgMulut);
		kakatuaView.setBackgroundResource(R.drawable.kakatua_mulut_anim);
		kakatuaView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) kakatuaView.getBackground();
		        anim.start();
			}
		});
		
		FontUtils.setRobotoFont(context, rootView);
		
		checekdFriendsAdapter = new CheckedFriendsAdapter(context);
		gridSelect.setAdapter(checekdFriendsAdapter);
		checekdGroupsAdapter = new CheckedGroupsAdapter(context);
		
	}
	
	public static Intent createIntent(Context context) {
		return new Intent(context, BroadcastMesssageActivity.class);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		rootView.startAnimation(fadein);
	}
	
	private void toggleSelect(){
		btnSelectAll.setBackgroundResource(R.color.AbuTuaVeryLow);
		btnSelectGroup.setBackgroundResource(R.color.AbuTuaVeryLow);
		btnSelectCustom.setBackgroundResource(R.color.AbuTuaVeryLow);
		txtSelectAll.setTextColor(getResources().getColor(R.color.TextSecondaryLight));
		txtSelectGroup.setTextColor(getResources().getColor(R.color.TextSecondaryLight));
		txtSelectCustom.setTextColor(getResources().getColor(R.color.TextSecondaryLight));
		if(indexSelect==0){
			gridSelect.setVisibility(View.GONE);
			gridSelect.setAdapter(null);
			btnSelectAll.setBackgroundResource(R.color.BiruNdogAsin);
			txtSelectAll.setTextColor(getResources().getColor(R.color.Putih));
		} else if(indexSelect==1){
			gridSelect.setVisibility(View.VISIBLE);
			btnSelectGroup.setBackgroundResource(R.color.BiruNdogAsin);
			txtSelectGroup.setTextColor(getResources().getColor(R.color.Putih));
			gridSelect.setAdapter(checekdGroupsAdapter);
		}else {
			gridSelect.setVisibility(View.VISIBLE);
			btnSelectCustom.setBackgroundResource(R.color.BiruNdogAsin);
			txtSelectCustom.setTextColor(getResources().getColor(R.color.Putih));
			gridSelect.setAdapter(checekdFriendsAdapter);
		}
	}
	private void sendBroadcast(String toUser, String textNya){
		String user = AccountManager.getInstance().getAccountKu();
		String textBroadcast = "BR04DC4STK03@"+textNya;
		MessageManager.getInstance().sendMessage(user,toUser,textBroadcast);
	}
	
	private void broadcastToAll(){
		for (RosterContact rosterContact : RosterManager.getInstance().getContacts()) {
			sendBroadcast(rosterContact.getUser(), editMessage.getText().toString());
		}
		finish();
	}
	private void broadcastToGroup(){
		String selected = AccountManager.getInstance().getActiveAccount().getAccount();
		ArrayList<RosterContact> listPushes = new ArrayList<RosterContact>();
		for(int x=0;x<checekdGroupsAdapter.checkedContact.size();x++){
		for (RosterContact rosterContact : RosterManager.getInstance().getContacts()) {
			for (String grup : RosterManager.getInstance().getGroups(selected, rosterContact.getUser())) {
				if(grup.equals(checekdGroupsAdapter.checkedContact.get(x).getName())){
					listPushes.add(rosterContact);
				}
			}
		}
		}
		for(int x=0;x<listPushes.size();x++){
			sendBroadcast(listPushes.get(x).getUser(), editMessage.getText().toString());
		}
		finish();
	}
	
	private void broadcastToCustom(){
		for(int x=0;x<checekdFriendsAdapter.checkedContact.size();x++){
			sendBroadcast(checekdFriendsAdapter.checkedContact.get(x).getUser(), editMessage.getText().toString());
		}
		finish();
	}
	
	
	@Override
	public void onClick(View v) {
		if(v==btnClose || v==rootView){
			InputMethodManager imm  = (InputMethodManager)context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		    if(imm.isActive()){
		    	imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0); 
		    } else {
				overridePendingTransition(0, R.anim.fadeout);
				finish();
		    }
		    overridePendingTransition(0, R.anim.fadeout);
			finish();
		} else if(v==btnSelectAll){
			indexSelect =0;
			toggleSelect();
		} else if(v==btnSelectGroup){
			indexSelect =1;
			toggleSelect();
		} else if(v==btnSelectCustom){
			indexSelect =2;
			toggleSelect();
		} else if(v==btnSend){
			if(indexSelect==0){
				broadcastToAll();
			} else if(indexSelect==1){
				broadcastToGroup();
			} else {
				broadcastToCustom();
			}
		}
	}
	
	@Override
	protected void resettingView() {
		super.resettingView();
		//Setting terms
		int widthTemp = (viewWidth*80)/100;
		if(widthTemp>=ViewUtilities.GetInstance().convertDPtoPX(480)){
			containerBroadcast.getLayoutParams().width=widthTemp;
		} else {
			containerBroadcast.getLayoutParams().width=viewWidth;
		}
	}

}
