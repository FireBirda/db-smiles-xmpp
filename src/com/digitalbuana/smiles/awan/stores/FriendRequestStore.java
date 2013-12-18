package com.digitalbuana.smiles.awan.stores;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.digitalbuana.smiles.awan.model.FriendRequestModel;

public class FriendRequestStore {
	
	private ArrayList<FriendRequestModel> list = null;
	
	public FriendRequestStore(JSONObject json){
		if(json != null){
			list = new ArrayList<FriendRequestModel>();
			try {
				JSONArray arr = json.getJSONArray("DATA");
				for(int a = 0; a < arr.length(); a++){
					JSONObject jObj = arr.getJSONObject(a);
					FriendRequestModel frm = new FriendRequestModel();
					frm.setRelCode(jObj.getString("rel_code"));
					frm.setUserId(jObj.getString("user_id"));
					frm.setUserName(jObj.getString("username"));
					frm.setEmail(jObj.getString("email"));
					frm.setFullName(jObj.getString("fullname"));
					frm.setGender(jObj.getString("gender"));
					frm.setGenderPreff(jObj.getString("gender_preference"));
					frm.setStatus(jObj.getString("status"));
					frm.setOnline(jObj.getString("online"));
					frm.setIsBlocked(jObj.getString("is_blocked"));
					frm.setAvatar(jObj.getString("avatar"));
					list.add(frm);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<FriendRequestModel> getList(){
		return list;
	}
	
}
