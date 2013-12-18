package com.digitalbuana.smiles.data.friends;

import java.util.ArrayList;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.digitalbuana.smiles.data.Application;

public class StickerPackageManager {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	// Database fields
	private String[] allFriends = {
			MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_SID,
			MySQLiteHelper.COLUMN_NAME,
			MySQLiteHelper.COLUMN_DESC,
			MySQLiteHelper.COLUMN_PRICE,
			MySQLiteHelper.COLUMN_THUMB,
			MySQLiteHelper.COLUMN_ALLOW_USE,
			MySQLiteHelper.COLUMN_SAVE
			};
	public StickerPackageManager() {	
		dbHelper = new MySQLiteHelper(Application.getInstance().getApplicationContext());
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	private StickerPackageModel cursorToStickerPackage(Cursor cursor) {
		boolean allowUse = cursor.getString(6).equals("Y");
		StickerPackageModel model = new StickerPackageModel(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				cursor.getString(3),
				cursor.getString(4),
				cursor.getString(5),
				allowUse
				);
		return model;
	}
	
	public boolean isStickerDownload() {
		return false;
	}

	
	public void addStickerPackage(StickerPackageModel sticker){
		ArrayList<StickerPackageModel> list = getAllStickerPackage();
		for(int x=0;x<list.size();x++){
			if(!list.get(x).equals(sticker)){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_SID, sticker.getPackageID());
				values.put(MySQLiteHelper.COLUMN_NAME, sticker.getname());
				values.put(MySQLiteHelper.COLUMN_DESC, sticker.getDescription());
				values.put(MySQLiteHelper.COLUMN_PRICE, sticker.getPrice());
				values.put(MySQLiteHelper.COLUMN_THUMB, sticker.getThumbnail());
				if(sticker.getAllowUse()){
					values.put(MySQLiteHelper.COLUMN_ALLOW_USE, "Y");
				} else {
					values.put(MySQLiteHelper.COLUMN_ALLOW_USE, "N");
				}
				long insertId = database.insert(MySQLiteHelper.TABLE_STICKER_PACKAGE, null, values);
				Cursor cursor = database.query(MySQLiteHelper.TABLE_STICKER_PACKAGE,  allFriends, MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
				cursor.moveToFirst();
				cursorToStickerPackage(cursor);
				cursor.close();
			}
		}
	}
	
	public void removeStickerPackage(StickerPackageModel sticker) {
	    ArrayList<StickerPackageModel> list = getAllStickerPackage();
		for(int x=0;x<list.size();x++){
			if(list.get(x).equals(sticker)){
				 long id = list.get(x).getID();
				 database.delete(MySQLiteHelper.TABLE_STICKER_PACKAGE, MySQLiteHelper.COLUMN_ID+ " = " + id, null);
			}
		}
	}
	
	public void deleteAllFriends() {
		ArrayList<StickerPackageModel> list = getAllStickerPackage();
		for(int x=0;x<list.size();x++){
			long id = list.get(x).getID();
		    database.delete(MySQLiteHelper.TABLE_STICKER_PACKAGE, MySQLiteHelper.COLUMN_ID+ " = " + id, null);
		}
	}
	
	public ArrayList<StickerPackageModel> getAllStickerPackage() {
		ArrayList<StickerPackageModel> list = new ArrayList<StickerPackageModel>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_STICKER_PACKAGE, allFriends, null, null, null, null, null);
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	StickerPackageModel name = cursorToStickerPackage(cursor);
	    	list.add(name);
		    cursor.moveToNext();
		}
		cursor.close();
		return list;
	}
}
