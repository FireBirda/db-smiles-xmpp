package com.digitalbuana.smiles.utils;

import com.digitalbuana.smiles.data.Application;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;



public class ViewUtilities {

	
	private static ViewUtilities instance;
	private static int width, height;
	private static int density =160;
	private static boolean isTablet=false;
	private static boolean isLandscape=false;
	
	
	private ViewUtilities() {
	}
	public static ViewUtilities GetInstance() {
		if (instance == null) {instance = new ViewUtilities();}
		return instance;
	}
	

	public void setResolution(Activity act) {
		Display display = act.getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		height = display.getHeight();	
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);    
		density = metrics.densityDpi;
		isTablet(act.getApplicationContext());
		if(width>height){
			isLandscape=true;
		}else {
			isLandscape=false;
		}
	}
	
	
	
	public boolean isTablet() {
		return isTablet;
	}
	
	public boolean isLandscape() {
		return isLandscape;
	}

	private void isTablet(Context context) {
		try {
			// Compute screen size
			DisplayMetrics dm = context.getResources().getDisplayMetrics();
			float screenWidth = dm.widthPixels / dm.xdpi;
			float screenHeight = dm.heightPixels / dm.ydpi;
			if(dm.widthPixels>dm.heightPixels){
				screenWidth = dm.widthPixels / dm.xdpi;
				screenHeight = dm.heightPixels / dm.ydpi;
			}
				if(dm.heightPixels<=500){
					isTablet = false;
				} else {
					double size = Math.sqrt(Math.pow(screenWidth, 2)+ Math.pow(screenHeight, 2));
					isTablet = size >= 6;
					if (Integer.parseInt(Build.VERSION.SDK) <= 11) {
						isTablet=false;
				    }
				}
		} catch (Throwable t) {
			isTablet = false;
		}
	}
	
	public int getDensity() {
		return density;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	public int convertDPtoPX(int dp){
		DisplayMetrics dm = Application.getInstance().getApplicationContext().getResources().getDisplayMetrics();
		return (int)((dp * dm.density) + 0.5);
	}
	
	public int convertPXtoDPint (int px){
		DisplayMetrics dm =  Application.getInstance().getApplicationContext().getResources().getDisplayMetrics();
		return (int) ((px/dm.density)+0.5);
	}
}
