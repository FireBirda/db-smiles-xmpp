package com.digitalbuana.smiles.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppConfiguration {

	private final static AppConfiguration instance;
	private static SharedPreferences appPreference;

	static {
		instance = new AppConfiguration();
		Application.getInstance().addManager(instance);
	}
	
	public static AppConfiguration getInstance() {
		return instance;
	}
	public AppConfiguration() {
		appPreference =  PreferenceManager.getDefaultSharedPreferences(Application.getInstance().getApplicationContext());
		refreshTemp();
	}
	
	public void refreshTemp(){
		String savedListSticker = appPreference.getString("APPCONFIG_JSON", "");
		parseAppConfig(savedListSticker);
	}
	
	public void setTempJSON(String json){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("APPCONFIG_JSON", json);
        editor.commit();
        parseAppConfig(json);
	}
	
	private void parseAppConfig(String json){
		try {
			JSONObject jsonKu = new JSONObject(json);
			String active = jsonKu.getString("active");
			String forceUpdate = jsonKu.getString("forceupdate");
			setVersion(jsonKu.getJSONObject("version").getString("android"));
			setTimeSticker( jsonKu.getJSONObject("lastupdate").getString("sticker"));
			setTimeIkonia(jsonKu.getJSONObject("lastupdate").getString("ikonia"));
			setTimeRightMenu(jsonKu.getJSONObject("lastupdate").getJSONObject("rightmenu").getString("lastdate"));
			String rightMenuActive = jsonKu.getJSONObject("lastupdate").getJSONObject("rightmenu").getString("active");
			if(active.equals("true")){
				setIsActive(true);
			}
			if(forceUpdate.equals("true")){
				setIsForceUpdate(true);
			}
			if(rightMenuActive.equals("true")){
				setIsRightActive(true);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void setIsActive(boolean value){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putBoolean("APPCONFIG_IS_ACTIVE", value);
        editor.commit();
	}
	public boolean isActive(){
		boolean temp = appPreference.getBoolean("APPCONFIG_IS_ACTIVE", true);
		return temp;
	}
	public void setIsRightActive(boolean value){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putBoolean("APPCONFIG_RIGHT_ACTIVE", value);
        editor.commit();
	}
	
	public boolean isRightActive(){
		boolean temp = appPreference.getBoolean("APPCONFIG_RIGHT_ACTIVE", false);
		return temp;
	}
	public void setIsForceUpdate(boolean value){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putBoolean("APPCONFIG_IS_UPDATE", value);
        editor.commit();
	}
	public boolean isForceUpdate(){
		boolean temp = appPreference.getBoolean("APPCONFIG_IS_UPDATE", false);
		return temp;
	}
	public void setVersion(String value){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("APPCONFIG_VERSION", value);
        editor.commit();
	}
	public String getVersion(){
		String temp = appPreference.getString("APPCONFIG_VERSION", "");
		return temp;
	}
	public void setTimeSticker(String time){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("APPCONFIG_STICKER_TIME", time);
        editor.commit();
	}
	public String getTimeSticker(){
		String temp = appPreference.getString("APPCONFIG_STICKER_TIME", "");
		return temp;
	}
	public void setTimeIkonia(String time){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("APPCONFIG_IKONIA_TIME", time);
        editor.commit();
	}
	public String getTimeIkonia(){
		String temp = appPreference.getString("APPCONFIG_IKONIA_TIME", "");
		return temp;
	}
	public void setTimeRightMenu(String time){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("APPCONFIG_RIGHTMENU_TIME", time);
        editor.commit();
	}
	public String getTimeRightMenu(){
		String temp = appPreference.getString("APPCONFIG_RIGHTMENU_TIME", "");
		return temp;
	}
	
	public void setLatitude(double value){
		if(value!=0){
			SharedPreferences.Editor editor = appPreference.edit();
			editor.putString("LATITUDEKU", Double.toString(value));
	        editor.commit();
		}
	}
	public double getLatitude(){
		String temp =  appPreference.getString("LATITUDEKU", "0");
		double value = Double.parseDouble(temp);
		return value;
	}
	public void setLongitude(double value){
		if(value!=0){
			SharedPreferences.Editor editor = appPreference.edit();
			editor.putString("LONGITUDEKU", Double.toString(value));
			editor.commit();
		}
	}
	public double getLongitude(){
		String temp =  appPreference.getString("LONGITUDEKU", "0");
		double value = Double.parseDouble(temp);
		return value;
	}
	
	public void setLastLatitude(double value){
		if(value!=0){
			SharedPreferences.Editor editor = appPreference.edit();
			editor.putString("LATITUDEKULAST", Double.toString(value));
	        editor.commit();
		}
	}
	public double getLastLatitude(){
		String temp =  appPreference.getString("LATITUDEKULAST", "0");
		double value = Double.parseDouble(temp);
		return value;
	}
	public void setLastLongitude(double value){
		if(value!=0){
		SharedPreferences.Editor editor = appPreference.edit();
		editor.putString("LONGITUDEKULAST", Double.toString(value));
        editor.commit();
		}
	}
	public double getLastLongitude(){
		String temp =  appPreference.getString("LONGITUDEKULAST", "0");
		double value = Double.parseDouble(temp);
		return value;
	}
	
	
}
