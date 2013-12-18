package com.digitalbuana.smiles.awan.stores;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.digitalbuana.smiles.awan.model.BookmarkConferenceModel;

public class BookmarkConferenceStore {
	
	private ArrayList<BookmarkConferenceModel> list = null;
	private String TAG = getClass().getSimpleName();
	
	public BookmarkConferenceStore(JSONArray object){
		if(object.length()>0){
			try {
				list = new ArrayList<BookmarkConferenceModel>();
				for(int a = 0; a < object.length(); a++){				
					JSONObject jObj = object.getJSONObject(a);
					BookmarkConferenceModel bcm = new BookmarkConferenceModel();					
					bcm.setName(jObj.getString("name"));
					bcm.setJid(jObj.getString("jid"));
					list.add(bcm);
				}
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}
	
	public ArrayList<BookmarkConferenceModel> getResult(){
		return this.list;
	}
}
