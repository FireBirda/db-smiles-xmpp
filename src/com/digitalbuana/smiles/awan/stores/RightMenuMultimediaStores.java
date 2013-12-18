package com.digitalbuana.smiles.awan.stores;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.digitalbuana.smiles.awan.model.RightMenuMultimediaModel;

public class RightMenuMultimediaStores {
	
	private ArrayList<RightMenuMultimediaModel> list = null;
	
	public RightMenuMultimediaStores(JSONObject jObt){
		try {
			JSONArray jArr = jObt.getJSONArray("streaming");
			if(jArr.length() > 0){
				list = new ArrayList<RightMenuMultimediaModel>();
				for(int a = 0; a < jArr.length(); a++){
					JSONObject row = jArr.getJSONObject(a);
					RightMenuMultimediaModel hm = new RightMenuMultimediaModel();
					hm.setTitle(row.getString("title"));
					hm.setIcon(row.getString("icon"));
					hm.setType(row.getString("type"));
					hm.setDetail(row.getString("detail"));
					hm.setParm(row.getString("parm"));
					list.add(hm);
				}
			}			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<RightMenuMultimediaModel> getList(){
		return this.list;
	}
}
