package com.digitalbuana.smiles.awan.stores;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.digitalbuana.smiles.awan.model.RecomendedModel;

public class RecomendedStore {
	
	private ArrayList<RecomendedModel> list = null;
	private String TAG = getClass().getSimpleName();
	
	public RecomendedStore(JSONObject json){
		if(json != null && !json.equals("")){
			list = new ArrayList<RecomendedModel>();
			try {
				JSONArray arr = json.getJSONArray("DATA");
				for(int a = 0; a < arr.length(); a++){
					JSONObject jo = arr.getJSONObject(a);
					RecomendedModel rm = new RecomendedModel();
					rm.setUserId(jo.getString("user_id"));
					rm.setUserName(jo.getString("username"));
					rm.setFullName(jo.getString("fullname"));
					rm.setMsisdn(jo.getString("msisdn"));
					rm.setEmail(jo.getString("email"));
					list.add(rm);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				Log.e(TAG, e.getMessage());
			}
		}
	}
	
	public ArrayList<RecomendedModel> getList(){
		return this.list;
	}
	
}
