package com.digitalbuana.smiles.activity;

import java.util.Collection;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.adapter.RoomsAdapter;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.account.AccountItem;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.extension.muc.MUCManager;
import com.digitalbuana.smiles.ui.helper.ManagedActivity;
import com.digitalbuana.smiles.utils.FontUtils;

public class RoomsActivity extends ManagedActivity
implements
OnClickListener, OnItemClickListener
{
	private FrameLayout btnBack;
	
	private GridView gridView;
	private RoomsAdapter adapterRoom;
	
	private static Collection<HostedRoom> listRooms;
	private String TAG = getClass().getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_rooms);
		
		rootView = (FrameLayout)findViewById(R.id.roomsRootView);
		btnBack = (FrameLayout)findViewById(R.id.roomsBtnBack);
		gridView = (GridView)findViewById(R.id.roomsGridView);
		
		btnBack.setOnClickListener(this);
		gridView.setOnItemClickListener(this);
		FontUtils.setRobotoFont(context, rootView);
		
		getRoomList();
	}
	
	private void getRoomList(){
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				try {
					listRooms = MultiUserChat.getHostedRooms(AccountManager.getInstance().getActiveAccount().getConnectionThread().getXMPPConnection(), AppConstants.XMPPRoomsServer);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapterRoom = new RoomsAdapter(context, listRooms);
							gridView.setAdapter(adapterRoom);
						}
					});
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static Intent createIntent(Context context) {
		return new Intent(context, RoomsActivity.class);
	}
	
	@Override
	public void onClick(View v) {
		if(v==btnBack){
			finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		if(parent==gridView){
			String roomJID = ((RoomsAdapter.ViewHolder)view.getTag()).getJIDRoom();
			Log.i(TAG, "ROOM JID : "+roomJID);
			if(roomJID.length()>=3){
				joinGroup(roomJID);	
			}
		}
	}	
	
	private void joinGroup(String roomJID){
		AccountItem account = AccountManager.getInstance().getActiveAccount();
		String room = roomJID;
		String nickname = AccountManager.getInstance().getNickName(account.getAccount());
//		MUCManager.getInstance().removeRoom(account.getAccount(), room);
//		MessageManager.getInstance().closeChat(account.getAccount(),room);
//		NotificationManager.getInstance().removeMessageNotification(account.getAccount(), room);
		MUCManager.getInstance().createRoom(account.getAccount(), room, nickname, "", true);
		finish();
	}
}
