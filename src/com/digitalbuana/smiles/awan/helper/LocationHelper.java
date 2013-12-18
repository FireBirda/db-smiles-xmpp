package com.digitalbuana.smiles.awan.helper;

import java.util.HashMap;
import java.util.Map;

import com.digitalbuana.smiles.data.AppConfiguration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

public class LocationHelper {
	public static void toggleGPS(boolean enable, Context c) {
	    String provider = Settings.Secure.getString(c.getContentResolver(), 
	        Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

	    if(provider.contains("gps") == enable) {
	        return; // the GPS is already in the requested state
	    }

	    final Intent poke = new Intent();
	    poke.setClassName("com.android.settings", 
	        "com.android.settings.widget.SettingsAppWidgetProvider");
	    poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	    poke.setData(Uri.parse("3"));
	    c.sendBroadcast(poke);
	}
	public static void turnGPSOn(Context c){
	    String provider = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

	    if(!provider.contains("gps")){ //if gps is disabled
	        final Intent poke = new Intent();
	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        poke.setData(Uri.parse("3")); 
	        c.sendBroadcast(poke);
	    }
	}
	public static Map<String, String> getLongLat(final Context c, Activity a){
		
		toggleGPS(true, c);		
		turnGPSOn(c);
		
		final LocationManager lm = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		boolean gpsForceToOn = false;
		
		if(!isGPS)
	    {
			gpsForceToOn = true;
	        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
	        intent.putExtra("enabled", true);
	        c.sendBroadcast(intent);	        
			
			//lm = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE); 
	        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	        
	        final LocationListener ll = new LocationListener() {
				
				
				
				@Override
				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onLocationChanged(Location location) {
					// TODO Auto-generated method stub
					//Log.i(TAG, "locationlisteners : longitude " + location.getLongitude());
					//Log.i(TAG, "locationlisteners : latitude " + location.getLatitude());
				}

				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					// TODO Auto-generated method stub
					
				}
			};
			a.runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					try{
						
						lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, ll);
						
					}catch(IllegalArgumentException e){
						
					}finally{}
					
				}
			});
			
	        
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }
		
		if(location == null){		
			location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
		double longitude = 0;
		double latitude = 0 ;
		
		if(location != null){			
			
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			
		}else{
			
			a.runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					//Toast.makeText(c, "Your GPS is turn off, please turn it on to get best friend search filtering", Toast.LENGTH_LONG).show();
				}
			});
			
		}		
		Map<String, String> result = new HashMap<String, String>();
		result.put("long", String.valueOf(longitude));
		result.put("lat", String.valueOf(latitude));
		
		if(gpsForceToOn){
	        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
	        intent.putExtra("disabled", true);
	        c.sendBroadcast(intent);
		}
		
		if(longitude != 0 && latitude != 0){
			AppConfiguration.getInstance().setLongitude(longitude);
	    	AppConfiguration.getInstance().setLatitude(latitude);
		}
		
		return result;
	}
}
