package com.digitalbuana.smiles.awan.stores;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.friends.FriendsModel;

public class FriendVisitsStore {
	
	private ArrayList<FriendsModel> list = null;
	
	public FriendVisitsStore(JSONObject object){
		try {
			String status = object.getString("STATUS");
			if(status.trim().equals("SUCCESS")){
				JSONArray arr = object.getJSONArray("DATA");
				if(arr.length()>0){
					list = new ArrayList<FriendsModel>();
					for(int a = 0; a < arr.length(); a++){
						JSONObject item = arr.getJSONObject(a);
						FriendsModel fm = new FriendsModel(
							Integer.valueOf(item.getString("user_id")), 
							item.getString("username"), 
							item.getString("username")+"@"+AppConstants.XMPPServerHost, 
							item.getString("dt").replace(" ", "+"),
							""
						);
						list.add(fm);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<FriendsModel> getList(){
		return this.list;
	}
	
}
