package com.digitalbuana.smiles.data;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RelatedFriendsManager  {

	private final static RelatedFriendsManager instance;
	private static SharedPreferences appPreference;
	
	private static ArrayList<String> listRelatedFriends;

	static {
		instance = new RelatedFriendsManager();
	}
	
	public static RelatedFriendsManager getInstance() {
		return instance;
	}
	
	private RelatedFriendsManager() {
		appPreference =  PreferenceManager.getDefaultSharedPreferences(Application.getInstance().getApplicationContext());
		refreshListBlockedFriends();
	}
	
	public ArrayList<String> getListRelated(){
		return listRelatedFriends;
	}
	public void setBlockedFriends(ArrayList<String> replaced){
		listRelatedFriends =replaced;
		refreshingTemp();
	}
	
	public void setBlockedFriends(String name){
		if(!listRelatedFriends.contains(name)){
			listRelatedFriends.add(name);
			refreshingTemp();
		}
	}
	
	public void setNumberContact(int number){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putInt("LIST_RELATED_FRIENDS_COUNT", number);
		editor.commit();
	}
	public int getNumberContact(){
		return appPreference.getInt("LIST_RELATED_FRIENDS_COUNT", 0);
	}
	
	private void refreshingTemp(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < listRelatedFriends.size(); i++) {
		    sb.append(listRelatedFriends.get(i)).append(",");
		}
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("LIST_RELATED_FRIENDS", sb.toString());
		editor.commit();
	}
	
	public void deleteBlockedFriends(String name) {
		if(listRelatedFriends.contains(name)){
			listRelatedFriends.remove(listRelatedFriends.indexOf(name));
			refreshingTemp();
		}
	}
	
	
	public void refreshListBlockedFriends(){
		listRelatedFriends = new ArrayList<String>();
		String savedList = appPreference.getString("LIST_RELATED_FRIENDS", "");
		String[] playlists = savedList.split(",");
		for (int i = 0; i < playlists.length; i++) {
			listRelatedFriends.add(playlists[i]);
		}
		if(savedList.length()<=2){
			listRelatedFriends.clear();
		}
	}
	
	public void deleteAll(){
		listRelatedFriends.clear();
		refreshingTemp();
	}
	
}
