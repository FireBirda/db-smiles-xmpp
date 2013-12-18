package com.digitalbuana.smiles.awan.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Window;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.digitalbuana.smiles.R;

public class ScreenHelper {

	private static int screenWidth = 0;
	private static int screenHeight = 0;
	private static DisplayMetrics displayMetric = null;

	public static int getScreenWidth(Activity a) {
		a.getWindowManager().getDefaultDisplay().getMetrics(getDisplayMetric());
		screenWidth = getDisplayMetric().widthPixels;
		return screenWidth;
	}

	public static int getScreenHeight(Activity a) {
		a.getWindowManager().getDefaultDisplay().getMetrics(getDisplayMetric());
		screenHeight = getDisplayMetric().heightPixels;
		return screenHeight;
	}

	private static DisplayMetrics getDisplayMetric() {
		if (displayMetric == null)
			displayMetric = new DisplayMetrics();
		return displayMetric;
	}

	public static ImageOptions AQueryImageOption() {
		ImageOptions options = new ImageOptions();
		// options.round = 5;
		options.fileCache = true;
		options.memCache = true;
		options.targetWidth = 0;
		options.animation = AQuery.FADE_IN_NETWORK;
		return options;
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device
	 * density.
	 * 
	 * @param dp
	 *            A value in dp (density independent pixels) unit. Which we need
	 *            to convert into pixels
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on
	 *         device density
	 */
	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	/**
	 * This method converts device specific pixels to density independent
	 * pixels.
	 * 
	 * @param px
	 *            A value in px (pixels) unit. Which we need to convert into db
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}

	public static Dialog getDialogProgress(Context context) {
		Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.loading_dialog);
		return dialog;
	}
}
