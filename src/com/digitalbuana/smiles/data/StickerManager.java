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

import java.io.File;
import java.util.ArrayList;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.androidquery.AQuery;
import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.connection.ConnectionManager;
import com.digitalbuana.smiles.data.message.AbstractChat;
import com.digitalbuana.smiles.data.message.ChatAction;
import com.digitalbuana.smiles.data.message.MessageManager;
import com.digitalbuana.smiles.data.message.RegularChat;
import com.digitalbuana.smiles.data.roster.PresenceManager;
import com.digitalbuana.smiles.data.roster.ResourceItem;
import com.digitalbuana.smiles.xmpp.address.Jid;

public class StickerManager  {

	private final static StickerManager instance;
	private static SharedPreferences appPreference;
	
	private ArrayList<StickerIconiaModel> listSticker;
	private ArrayList<StickerIconiaModel> listIconia;
	private String TAG = getClass().getSimpleName();
	
	private ArrayList<StickerIconiaModel> listSavedSticker;
	
	static {
		instance = new StickerManager();
		Application.getInstance().addManager(instance);
	}

	public static StickerManager getInstance() {
		return instance;
	}

	public StickerManager() {
		appPreference =  PreferenceManager.getDefaultSharedPreferences(Application.getInstance().getApplicationContext());
		listSticker = new ArrayList<StickerManager.StickerIconiaModel>();
		listIconia = new ArrayList<StickerManager.StickerIconiaModel>();
		listSavedSticker = new ArrayList<StickerManager.StickerIconiaModel>();
		refreshStickerTemp();
	}

	
	public void setStickerPackage(String json){
		if(json.length()>=10){
			SharedPreferences.Editor editor = appPreference.edit();
			editor.putString("TMP_STICKER_PACKAGE", json);
			editor.commit();
			listSticker = parseSticker(json, "TMP_STICKER_PACKAGE_TIME");
		}
	}
	public void setIconiaPackage(String json){
		if(json.length()>=10){
			SharedPreferences.Editor editor = appPreference.edit();
			editor.putString("TMP_ICONIA_PACKAGE", json);
			editor.commit();
			listIconia = parseSticker(json, "TMP_ICONIA_PACKAGE_TIME");
		}
	}
	public void deleteAllDownloaded(){
		for (int i = 0; i < getIkoniaListAll().size(); i++) {
			deleteSticker(getIkoniaListAll().get(i).getPackageID());
		}
		for (int i = 0; i < getStickerListAll().size(); i++) {
			deleteSticker(getStickerListAll().get(i).getPackageID());
		}
	}
	
	public void setStickerDetail(String json, String packegeID){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("TMP_STICKER_DETAIL_"+packegeID, json);
		ArrayList<String> listStickerTemp = new ArrayList<String>();
		listStickerTemp.clear();
		try {
			JSONObject jsonKu = new JSONObject(json);
			JSONArray jsonArrayKu = jsonKu.getJSONArray("STICKERS");
			for (int i = 0; i < jsonArrayKu.length(); i++) {
				String thumbKu = jsonArrayKu.getJSONObject(i).getString("sticker_url");
				listStickerTemp.add(thumbKu);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		editor.commit();
		refreshStickerTemp();
	}
	public void setIconiaDetail(String json, String packegeID){
		
	}
	
	public void saveSticker(String packageID){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putBoolean("STICKER_IS_DOWLOAD_"+packageID, true);
		editor.commit();
		refreshStickerTemp();
	}
	
	public void removeSticker(String packageID){
		
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putBoolean("STICKER_IS_DOWLOAD_"+packageID, false);
		editor.commit();
		
		AQuery aq = new AQuery(Application.getInstance().getApplicationContext());
		ArrayList<String> stickerList = getListSticker(packageID);
		
		if(!stickerList.isEmpty()){
			for(String list:stickerList){
				try{
					File file = aq.getCachedFile(list);
					file.delete();
				}catch(NullPointerException e){}								
			}
		}
		
		refreshStickerTemp();
	}
	
	public void deleteSticker(String packageID){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putBoolean("STICKER_IS_DOWLOAD_"+packageID, false);
		editor.commit();
		refreshStickerTemp();
	}
	
	public boolean getIsDownlaod(String packageID){
		return  appPreference.getBoolean("STICKER_IS_DOWLOAD_"+packageID, false);
	}
	
	public ArrayList<String> getListSticker(String packageID){
		String json = appPreference.getString("TMP_LIST_STICKER_"+packageID, null);
	    ArrayList<String> tempList = new ArrayList<String>();
	    if (json != null) {
	        try {
	            JSONArray a = new JSONArray(json);
	            for (int i = 0; i < a.length(); i++) {
	                String url = a.optString(i);
	                tempList.add(url);
	            }
	        } catch (JSONException e) {
	        	Log.e(TAG, e.getMessage());
	        }
	    }
	    return tempList;
	}
	public ArrayList<StickerIconiaModel> getStickerListAll(){
		return listSticker;
	}
	
	public ArrayList<StickerIconiaModel> getStickerListAllDownloaded(){
		ArrayList<StickerIconiaModel> listTemp = new ArrayList<StickerManager.StickerIconiaModel>();
		for (int i = 0; i < listSticker.size(); i++) {
			if(listSticker.get(i).isDownlaoded()){
				listTemp.add(listSticker.get(i));
			}
		}
		return listTemp;
	}
	
	public ArrayList<StickerIconiaModel> getIkoniaListAll(){
		return listIconia;
	}
	public ArrayList<StickerIconiaModel> getIkoniaListAllDownloaded(){
		ArrayList<StickerIconiaModel> listTemp = new ArrayList<StickerManager.StickerIconiaModel>();
		for (int i = 0; i < listIconia.size(); i++) {
			if(listIconia.get(i).isDownlaoded()){
				listTemp.add(listIconia.get(i));
			}
		}
		return listTemp;
	}
	public String getStickerDate(){
		return  appPreference.getString("TMP_STICKER_PACKAGE_TIME", "");
	}
	public String getIkoniaDate(){
		return  appPreference.getString("TMP_ICONIA_PACKAGE_TIME", "");
	}
	
	public void refreshStickerTemp(){
		try{
			String savedListSticker = appPreference.getString("TMP_STICKER_PACKAGE", "");
			String savedListIconia = appPreference.getString("TMP_ICONIA_PACKAGE", "");
			listSticker = parseSticker(savedListSticker, "TMP_STICKER_PACKAGE_TIME");
			listIconia = parseSticker(savedListIconia, "TMP_ICONIA_PACKAGE_TIME");
		}catch(NullPointerException e){}		
	}

	public void sendSticker( String user, String messageBroadcast, Message.Type type) throws NetworkException {
		AbstractChat chat = MessageManager.getInstance().getOrCreateChat(AccountManager.getInstance().getActiveAccount().getAccount(), user);
		if (!(chat instanceof RegularChat))
			throw new NetworkException(R.string.ENTRY_IS_NOT_FOUND);
		String to = chat.getTo();
		if (Jid.getResource(to) == null || "".equals(Jid.getResource(to))) {
			ResourceItem resourceItem = PresenceManager.getInstance().getResourceItem(AccountManager.getInstance().getActiveAccount().getAccount(), user);
			if (resourceItem == null)
				throw new NetworkException(R.string.NOT_CONNECTED);
			to = resourceItem.getUser(user);
		}
		Message message = new Message(to, type);
		message.setBody(messageBroadcast);
		ConnectionManager.getInstance().sendPacket(AccountManager.getInstance().getActiveAccount().getAccount(), message);
		chat.newAction(null, null, ChatAction.sticker_message, "", false);
	}
	
	public ArrayList<String> parseStickerDetail(String result, String packageID){
		SharedPreferences.Editor editor = appPreference.edit();
		JSONArray a = new JSONArray();
		ArrayList<String> listSticker = new ArrayList<String>();
		try {
			JSONObject jsonKu = new JSONObject(result);
			JSONArray jsonArrayKu = jsonKu.getJSONArray("STICKERS");
			for (int i = 0; i < jsonArrayKu.length(); i++) {
				String thumbKu = jsonArrayKu.getJSONObject(i).getString("sticker_url");
				listSticker.add(thumbKu);
				a.put(thumbKu);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (listSticker.size()<=0) {
	        editor.putString("TMP_LIST_STICKER_"+packageID, null);
	    } else {
	        editor.putString("TMP_LIST_STICKER_"+packageID, a.toString());
	    }
	    editor.commit();
	    refreshStickerTemp();
		return listSticker;
	}
	
	
	private ArrayList<StickerIconiaModel> parseSticker(String json, String nameTime){
		ArrayList<StickerIconiaModel> list = new ArrayList<StickerManager.StickerIconiaModel>();
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
						//String allow_use = jsArray.optJSONObject(i).getString("allow_use");
						boolean allow = false;
						//if(allow_use.equals("true")){
							allow = true;
							ArrayList<String> listSticker = getListSticker(packageID);
							boolean isDownlaoded = getIsDownlaod(packageID);
							StickerIconiaModel stricer = new StickerIconiaModel(packageID, name, desc, package_price, package_thumbnail, allow, isDownlaoded, listSticker);
							list.add(stricer);
//							Log.e(AppConstants.TAG, "--------- "+name+" is download : "+stricer.isDownlaoded()+" list size : "+stricer.getSticker().size());
						//}
					}
				}
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
				return list;
			}
		}
		return list;
	}
	
	public class StickerIconiaModel
	{
		String packageID;
		String name;
		String description;
		String price;
		String thumbnail;
		boolean allow;
		boolean isDownload;
		ArrayList<String> listSticker;
		
		StickerIconiaModel(String packageID, String name, String desc, String price, String thumbnail, boolean allow, boolean isdownload, ArrayList<String> listSticekr){
			this.name = name;
			this.packageID = packageID;
			this.description=desc;
			this.price = price;
			this.thumbnail = thumbnail;
			this.allow=allow;
			this.isDownload = isdownload;
			this.listSticker = listSticekr;
		}
		
		public String getName(){
			return name;
		}
		public String getThumbnail(){
			return thumbnail;
		}
		public String getPackageID(){
			return packageID;
		}
		public boolean isDownlaoded(){
			return isDownload;
		}
		public ArrayList<String> getSticker(){
			return listSticker;
		}
	}

}
