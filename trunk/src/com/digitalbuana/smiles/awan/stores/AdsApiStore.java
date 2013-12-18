package com.digitalbuana.smiles.awan.stores;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.digitalbuana.smiles.awan.model.AdsApiModel;

public class AdsApiStore {
	
	private ArrayList<AdsApiModel> list = null;
	
	public AdsApiStore(JSONObject json){
		if(json != null){
			list = new ArrayList<AdsApiModel>();
			try {
				JSONArray arr = json.getJSONArray("ads");
				for(int a = 0; a < arr.length(); a++){
					AdsApiModel aam = new AdsApiModel();
					JSONObject jObj = arr.getJSONObject(a);
					aam.setType(jObj.getString("type"));
					aam.setClientId(jObj.getString("clientid"));
					aam.setActive(jObj.getString("active"));
					list.add(aam);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<AdsApiModel> getList(){
		return list;
	}
}
