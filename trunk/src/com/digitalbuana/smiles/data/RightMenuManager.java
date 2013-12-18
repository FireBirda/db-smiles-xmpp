/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * 
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * 
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.digitalbuana.smiles.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RightMenuManager  {

	private final static RightMenuManager instance;
	private static SharedPreferences appPreference;
	
	private ArrayList<RightMenuModel> listMenu;

	
	static {
		instance = new RightMenuManager();
		Application.getInstance().addManager(instance);
	}

	public static RightMenuManager getInstance() {
		return instance;
	}

	public RightMenuManager() {
		appPreference =  PreferenceManager.getDefaultSharedPreferences(Application.getInstance().getApplicationContext());
		listMenu = new ArrayList<RightMenuManager.RightMenuModel>();
		refreshStickerTemp();
	}

	
	public void setStickerPackage(String json){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("TMP_RIGHTMENU", json);
		editor.commit();
		listMenu = parseSticker(json, "TMP_RIGHTMENU_TIME");
	}

	
	public String getRightMenuDate(){
		return  appPreference.getString("TMP_RIGHTMENU_TIME", "");
	}

	private void refreshStickerTemp(){
		String savedListSticker = appPreference.getString("TMP_RIGHTMENU", "");
		listMenu = parseSticker(savedListSticker, "TMP_RIGHTMENU_TIME");
	}
	
	
	private ArrayList<RightMenuModel> parseSticker(String json, String nameTime){
		ArrayList<RightMenuModel> list = new ArrayList<RightMenuManager.RightMenuModel>();
		if(json.length()>=10){
			try {
				JSONObject jsonKu = new JSONObject(json);
				JSONArray jsArray = jsonKu.optJSONArray("DATA");
				String date = jsonKu.optString("TIME");
				SharedPreferences.Editor editor = appPreference.edit();
				editor.putString(nameTime, date);
				editor.commit();
				if(jsArray!=null){
					list.clear();
					for (int i = 0; i < jsArray.length(); i++) {
						String packageID = jsArray.optJSONObject(i).getString("package_id");
						String name = jsArray.optJSONObject(i).getString("package_name");
						String desc = jsArray.optJSONObject(i).getString("package_desc");
						String package_price = jsArray.optJSONObject(i).getString("package_price");
						String package_thumbnail = jsArray.optJSONObject(i).getString("package_thumbnail");
						RightMenuModel stricer = new RightMenuModel(packageID, name, desc, package_price, package_thumbnail);
						list.add(stricer);
					}
				}
			} catch (JSONException e) {
				return list;
			}
		}
		return list;
	}
	
	public class RightMenuModel
	{
		String packageID;
		String name;
		String description;
		String price;
		String thumbnail;
		
		RightMenuModel(String packageID, String name, String desc, String price, String thumbnail){
			this.name = name;
			this.packageID = packageID;
			this.description=desc;
			this.price = price;
			this.thumbnail = thumbnail;
		}
	}

}
