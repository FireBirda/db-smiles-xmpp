package com.digitalbuana.smiles.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.awan.model.FriendRequestModel;
import com.digitalbuana.smiles.awan.stores.FriendRequestStore;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.friends.FriendsManager;
import com.digitalbuana.smiles.data.friends.FriendsModel;
import com.digitalbuana.smiles.data.roster.RosterManager;
import com.digitalbuana.smiles.utils.FontUtils;

public class RequestFriendsAdapter extends BaseAdapter {
	
	private Context context;
	private LayoutInflater li;
	private ArrayList<FriendsModel> listFreindsReq;	
	private int TYPE=0;
	private static final int TYPE_REQUEST=0;
	private static final int TYPE_PENDING=1;
	private static final int TYPE_BLOCKED=2;
	
	private OnFriendsReqListener listener=null;
	private AQuery aq;
	
	public RequestFriendsAdapter(Context c, int type, OnFriendsReqListener listener) {
		aq = new AQuery(c);
		this.listener=listener;
		this.context = c;
		this.TYPE = type;
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Map<String, String> parms = new HashMap<String, String>();
		parms.put("username", AccountManager.getInstance().getAccountKu());
		switch (this.TYPE) {
			case TYPE_REQUEST:
				listFreindsReq = FriendsManager.getInstance().getFriendsWaitingMeApproveManager().getAllFriends();				
				if(listFreindsReq.isEmpty()){
					aq.ajax(
						AppConstants.APIRequestFriends, 
						parms,
						JSONObject.class, 
						new AjaxCallback<JSONObject>(){
							public void callback(String url, JSONObject object, com.androidquery.callback.AjaxStatus status) {
								if(object != null){
									FriendRequestStore frs = new FriendRequestStore(object);
									ArrayList<FriendRequestModel> requestList = frs.getList();
									if(requestList != null && requestList.size() > 0){
										for(FriendRequestModel frsl:requestList){
											FriendsManager.getInstance().getFriendsWaitingMeApproveManager().addFriendsByName(frsl.getUserName());
										}
										notifyDataSetChanged();
									}
								}
							};
						}
					);
				}
				break;
			case TYPE_PENDING:
				listFreindsReq =FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().getAllFriends();
				if(listFreindsReq.isEmpty()){
					aq.ajax(
						AppConstants.APIMyRequestFriends, 
						parms,
						JSONObject.class, 
						new AjaxCallback<JSONObject>(){
							public void callback(String url, JSONObject object, com.androidquery.callback.AjaxStatus status) {
								if(object != null){
									FriendRequestStore frs = new FriendRequestStore(object);
									ArrayList<FriendRequestModel> requestList = frs.getList();
									if(requestList != null && requestList.size() > 0){
										for(FriendRequestModel frsl:requestList){
											FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().addFriendsByName(frsl.getUserName());
										}
										notifyDataSetChanged();
									}
								}
							};
						}
					);
				}
				break;
			case TYPE_BLOCKED:
				listFreindsReq =FriendsManager.getInstance().getFriendsBlockedManager().getAllFriends();
				break;
		}
	}
	
	@Override
	public void notifyDataSetChanged() {
		switch (this.TYPE) {
		case TYPE_REQUEST:listFreindsReq =FriendsManager.getInstance().getFriendsWaitingMeApproveManager().getAllFriends();break;
		case TYPE_PENDING:listFreindsReq =FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().getAllFriends();break;
		case TYPE_BLOCKED:listFreindsReq =FriendsManager.getInstance().getFriendsBlockedManager().getAllFriends();break;
		}
		super.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return listFreindsReq.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int pos, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
	    	convertView = li.inflate(R.layout.adapter_friendrequest, null);
	        holder = new ViewHolder();
	        holder.rootView = (LinearLayout)convertView.findViewById(R.id.adapterFriendRequestRoot);
	        holder.avatar = (ImageView)convertView.findViewById(R.id.adapterFriendRequestAvatar);
	        holder.name = (TextView)convertView.findViewById(R.id.adapterFriendRequestName);
	        holder.btnTxt = (TextView)convertView.findViewById(R.id.adapterFriendRequestBtnTxt);
	        holder.btnApprove = (FrameLayout)convertView.findViewById(R.id.adapterFriendRequestBtnOK);
	        convertView.setTag(holder);
	        FontUtils.setRobotoFont(context, convertView);
	    } else {
	        holder = (ViewHolder) convertView.getTag();
	    }
		holder.name.setText(listFreindsReq.get(pos).getName());
		holder.avatar.setImageDrawable(RosterManager.getInstance().getBestContact(AccountManager.getInstance().getActiveAccount().getAccount(), listFreindsReq.get(pos).getJID()).getAvatar());
		switch (this.TYPE) {
		case TYPE_REQUEST:holder.btnTxt.setText("APPROVE");break;
		case TYPE_PENDING:holder.btnTxt.setText("CANCEL");break;
		case TYPE_BLOCKED:holder.btnTxt.setText("CANCEL");break;
		}
		holder.btnApprove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(listener!=null){
					listener.onReqFriendsListener(TYPE, listFreindsReq.get(pos).getJID());
				}
			}
		});
		
		return convertView;
	}

	public static class ViewHolder {
		
		LinearLayout rootView;
		ImageView avatar;
		TextView name; 
		TextView btnTxt; 
		FrameLayout btnApprove; 
	}
	
	public interface OnFriendsReqListener{
		public void onReqFriendsListener(int type, String jid);
	}
}
