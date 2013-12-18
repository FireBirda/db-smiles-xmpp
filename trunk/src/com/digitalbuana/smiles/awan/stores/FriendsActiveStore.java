package com.digitalbuana.smiles.awan.stores;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.digitalbuana.smiles.awan.model.FriendsActiveModel;

public class FriendsActiveStore {	
	private ArrayList<FriendsActiveModel> list;
	private String TAG = getClass().getSimpleName();	
	public FriendsActiveStore(JSONObject json){
		try {			
			JSONArray arrData = json.getJSONArray("DATA");			
			if(arrData.length()>0){				
				list = new ArrayList<FriendsActiveModel>();				
				for(int a = 0; a < arrData.length(); a++){					
					FriendsActiveModel fam = new FriendsActiveModel();
					JSONObject tmpObject = arrData.getJSONObject(a);					
					fam.setRelCode(tmpObject.getString("rel_code"));
					fam.setUserId(tmpObject.getString("user_id"));
					fam.setUserName(tmpObject.getString("username"));
					fam.setEmail(tmpObject.getString("email"));
					fam.setFullName(tmpObject.getString("fullname"));
					fam.setGender(tmpObject.getString("gender"));
					fam.setGenderPreff(tmpObject.getString("gender_preference"));
					fam.setStatus(tmpObject.getString("status"));
					fam.setOnline(tmpObject.getString("online"));
					fam.setIsBlocked(tmpObject.getString("is_blocked"));
					fam.setAvatar(tmpObject.getString("avatar"));					
					list.add(fam);				}
			}else{
				Log.e(TAG, "data is null");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}	
	public ArrayList<FriendsActiveModel> getResult(){
		return list;
	}
}
