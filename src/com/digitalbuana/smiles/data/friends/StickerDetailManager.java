package com.digitalbuana.smiles.data.friends;

import java.util.ArrayList;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.digitalbuana.smiles.data.Application;

public class StickerDetailManager {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	// Database fields
	private String[] allFriends = {
			MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_SID,
			MySQLiteHelper.COLUMN_NAME,
			MySQLiteHelper.COLUMN_THUMB,
			};
	public StickerDetailManager() {	
		dbHelper = new MySQLiteHelper(Application.getInstance().getApplicationContext());
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	private StickerDetailModel cursorToSticker(Cursor cursor) {
		StickerDetailModel model = new StickerDetailModel(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				cursor.getString(3)
				);
		return model;
	}

	
	public void addStickerPackage(StickerPackageModel sticker){
		ArrayList<StickerDetailModel> list = getAllStickerPackage();
		for(int x=0;x<list.size();x++){
			if(!list.get(x).equals(sticker)){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_SID, sticker.getPackageID());
				values.put(MySQLiteHelper.COLUMN_NAME, sticker.getname());
				values.put(MySQLiteHelper.COLUMN_THUMB, sticker.getThumbnail());
				long insertId = database.insert(MySQLiteHelper.TABLE_STICKER_DETAIL, null, values);
				Cursor cursor = database.query(MySQLiteHelper.TABLE_STICKER_DETAIL,  allFriends, MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
				cursor.moveToFirst();
				cursorToSticker(cursor);
				cursor.close();
			}
		}
	}
	
	public void removeStickerPackage(StickerPackageModel sticker) {
	    ArrayList<StickerDetailModel> list = getAllStickerPackage();
		for(int x=0;x<list.size();x++){
			if(list.get(x).equals(sticker)){
				 long id = list.get(x).getID();
				 database.delete(MySQLiteHelper.TABLE_STICKER_DETAIL, MySQLiteHelper.COLUMN_ID+ " = " + id, null);
			}
		}
	}
	
	public void deleteAllFriends() {
		ArrayList<StickerDetailModel> list = getAllStickerPackage();
		for(int x=0;x<list.size();x++){
			long id = list.get(x).getID();
		    database.delete(MySQLiteHelper.TABLE_STICKER_DETAIL, MySQLiteHelper.COLUMN_ID+ " = " + id, null);
		}
	}
	
	public ArrayList<StickerDetailModel> getAllStickerPackage() {
		ArrayList<StickerDetailModel> list = new ArrayList<StickerDetailModel>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_STICKER_DETAIL, allFriends, null, null, null, null, null);
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	StickerDetailModel name = cursorToSticker(cursor);
	    	list.add(name);
		    cursor.moveToNext();
		}
		cursor.close();
		return list;
	}
}
