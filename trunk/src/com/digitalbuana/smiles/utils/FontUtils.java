package com.digitalbuana.smiles.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontUtils {
	public static interface FontTypes {
        public static String LIGHT = "Light";
        public static String BOLD = "Bold";
    }

    private static Map<String, String> fontMap = new HashMap<String, String>();

    static {
        fontMap.put(FontTypes.LIGHT, "fonts/Roboto-Light.ttf");
        fontMap.put(FontTypes.BOLD, "fonts/Roboto-Bold.ttf");
    }
    
    private static Map<String, Typeface> typefaceCache = new HashMap<String, Typeface>();
    private static Typeface getRobotoTypeface(Context context, String fontType) {
    	String fontPath = fontMap.get(fontType);
    	 if (!typefaceCache.containsKey(fontType)){
    		 typefaceCache.put(fontType, Typeface.createFromAsset(context.getAssets(), fontPath));
    	 }
    	 return typefaceCache.get(fontType);
    }
    
    private static Typeface getRobotoTypeface(Context context, Typeface originalTypeface) { 
    	String robotoFontType = FontTypes.LIGHT; //default Light Roboto font
    	if (originalTypeface != null) {
    		 int style = originalTypeface.getStyle();
             switch (style) {
             case Typeface.BOLD:robotoFontType = FontTypes.BOLD;
             }
    	}
    	return getRobotoTypeface(context, robotoFontType);
    }
    
    public static void setRobotoFont(Context context, View view){
    	if (view instanceof ViewGroup){
    		 for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++){
    			 setRobotoFont(context, ((ViewGroup)view).getChildAt(i));
    		 }
    	}else if (view instanceof TextView){
    		Typeface currentTypeface = ((TextView) view).getTypeface();
    		((TextView) view).setTypeface(getRobotoTypeface(context, currentTypeface));
    	}
    	
    }
    
    public static void setNormalRobotoFont(Context context, View view){
    	if (view instanceof ViewGroup){
    		 for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++){
    			 setRobotoFont(context, ((ViewGroup)view).getChildAt(i));
    		 }
    	}else if (view instanceof TextView){
    		((TextView) view).setTypeface(typefaceCache.get(FontTypes.LIGHT), Typeface.NORMAL);
    	}
    	
    }
    
    public static void setBoldRobotoFont(Context context, View view){
    	if (view instanceof ViewGroup){
    		 for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++){
    			 setRobotoFont(context, ((ViewGroup)view).getChildAt(i));
    		 }
    	}else if (view instanceof TextView){
    		((TextView) view).setTypeface(typefaceCache.get(FontTypes.BOLD), Typeface.BOLD);
    	}
    }
    
    
    /**
     * <b>Set Actual Font Size</b>
     * Without Padding
     * @param context  get Context to generate actual dp size from screen;
     * @param px put display pixel for return font size;
     */
    public static void setFixFontHeight(Context context, int px, TextView tv){
    	DisplayMetrics dm = context.getResources().getDisplayMetrics();
    	int dp = (int) ((px/dm.density)+0.5);
    	int fontSize =  (int) ((int)dp*0.9);
    	setRobotoFont(context, tv);
    	tv.setTextSize(fontSize);
//    	holder.txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    }
    
    /**
     * <b>Set Actual Font Size</b><br>
     * With Padding:all
     * @param context  get Context to generate actual dp size from screen;
     * @param px put display pixel for return font size;
     * @param padding input padding
     */
    public static void setFixFontHeightWithPadding(Context context, int px, int padding,TextView tv){
    	DisplayMetrics dm = context.getResources().getDisplayMetrics();
    	int heightKu = (int) ((px/dm.density)+0.5);
    	int paddingKu = (int) ((padding/dm.density)+0.5);
    	int fontSize =  heightKu-paddingKu;
    	setRobotoFont(context, tv);
    	if(heightKu-paddingKu<=1){
    		fontSize=1;
    		tv.setTextSize(fontSize);
    	}else {
    		tv.setTextSize(fontSize);
    	}
    }
}
