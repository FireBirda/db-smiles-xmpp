package com.digitalbuana.smiles.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.google.android.gcm.GCMRegistrar;

public class RecentUtils {

	
	private static RecentUtils instance;
	private static String TAG = "RecentUtils";
	
	private RecentUtils() {
	}
	public static RecentUtils GetInstance() {
		if (instance == null) {instance = new RecentUtils();}
		return instance;
	}
	
	//Get PHONE IMEI 
	public static String getImei() {
		Context ctx = Application.getInstance().getApplicationContext();
		TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String ime = telephonyManager.getDeviceId();
		String manufacture =  android.os.Build.MANUFACTURER;
		String product =  android.os.Build.PRODUCT;
		String id =  android.os.Build.ID;
		if(ime==null || ime.equals("")){
			ime = "ID-UNKNOWN";
		}
		return manufacture+"-"+product+"-"+id+"-"+ime;
	}
	
	//Get PHONE IMEI 
	public static boolean checkNetwork() {
		Context context = Application.getInstance().getApplicationContext();
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if (ni == null) {
			  Toast.makeText(context, "Please check your internet connection..", Toast.LENGTH_SHORT).show();
			  return false;
		  } else{
			   return true;
		  }
	}
	
	public static String getRegID(){
		Context context = Application.getInstance().getApplicationContext();
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		String regID =  GCMRegistrar.getRegistrationId(context);
//		Log.i(AppConstants.TAG, "REGID : "+regID);
		if(regID.equals("")){
			GCMRegistrar.register(context, AppConstants.GCMRegID);
			Log.d(TAG, GCMRegistrar.getRegistrationId(context));
//			Log.i(AppConstants.TAG, "REGID : null");
			return "";
		} else {
//			Log.i(AppConstants.TAG, "REGID : "+regID);
			return regID;
		}
	}
	
	public static String getCarrier(){
		Context context = Application.getInstance().getApplicationContext();
		TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getNetworkOperatorName();
	}

	
	public static float distFrom(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(toLatitude-fromLatitude);
	    double dLng = Math.toRadians(toLongitude-fromLongitude);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(fromLatitude)) * Math.cos(Math.toRadians(toLatitude)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    int meterConversion = 1609;

	    return new Float(dist * meterConversion).floatValue();
	}
}
