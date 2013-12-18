package com.digitalbuana.smiles.awan.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.androidquery.util.AQUtility;

public class ImageHelper {

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static void saveAvatarToCache(Drawable drawable) {
		Bitmap avatarBitmap = ImageHelper.drawableToBitmap(drawable);
		File avatar = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "smilesAvatar.jpg");
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		avatarBitmap.compress(CompressFormat.JPEG, 80, bos);
		byte[] bitmapdata = bos.toByteArray();

		// write the bytes in file
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(avatar);
			fos.write(bitmapdata);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("saveAvatarToCache",
					"FileNotFoundException::" + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("saveAvatarToCache", "IOException::" + e.getMessage());
		}

	}

	public static void setAqueryCachePath() {

		String state = Environment.getExternalStorageState();

		File ext;
		File cacheDir;

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			ext = Environment.getExternalStorageDirectory();
			cacheDir = new File(ext.getAbsolutePath(), "SMILES/cache/");
			AQUtility.setCacheDir(cacheDir);
		} else {
			// Something else is wrong. It may be one of many other states,
			// but
			// all we need
			// to know is we can neither read nor write
			ext = Environment.getDataDirectory();
			cacheDir = new File(ext.getAbsolutePath());
			if (cacheDir.canRead())
				AQUtility.setCacheDir(cacheDir);
		}
	}

}
