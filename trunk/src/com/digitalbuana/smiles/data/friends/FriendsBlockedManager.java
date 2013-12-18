package com.digitalbuana.smiles.data.friends;

import java.util.ArrayList;

import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.utils.StringUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FriendsBlockedManager {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	// Database fields
	private String[] allFriends = {
			MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_NAME,
			MySQLiteHelper.COLUMN_JID,
			};
	public FriendsBlockedManager() {	
		dbHelper = new MySQLiteHelper(Application.getInstance().getApplicationContext());
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	private FriendsModel cursorToFriends(Cursor cursor) {
		FriendsModel friends = new FriendsModel(cursor.getLong(0), cursor.getString(1),cursor.getString(2),"","");
		return friends;
	}
	
	public void addFriendsByJID(String jid) {
		String name = StringUtils.replaceStringEquals(jid);
		ArrayList<FriendsModel> list = getAllFriends();
		for(int x=0;x<list.size();x++){
			if(list.get(x).getJID().equals(jid)){
				removeFriendsJID(jid);
			}
		}
		addFriend(name,jid);
	}
	public void addFriendsByName(String name) {
		ArrayList<FriendsModel> list = getAllFriends();
		String jid = name+"@"+AppConstants.XMPPServerHost;
		for(int x=0;x<list.size();x++){
			if(list.get(x).getName().equals(name)){
				removeFriendsName(name);
			}
		}
		addFriend(name,jid);
	}
	
	private void addFriend(String name, String jid){
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NAME, name);
		values.put(MySQLiteHelper.COLUMN_JID, jid);
		long insertId = database.insert(MySQLiteHelper.TABLE_FRIENDSLIST_BLOCKED, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_FRIENDSLIST_BLOCKED,  allFriends, MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		cursorToFriends(cursor);
		cursor.close();
	}
	
	public void removeFriends(FriendsModel friends) {
	    ArrayList<FriendsModel> list = getAllFriends();
		for(int x=0;x<list.size();x++){
			if(list.get(x).equals(friends)){
				 long id = list.get(x).getID();
				 database.delete(MySQLiteHelper.TABLE_FRIENDSLIST_BLOCKED, MySQLiteHelper.COLUMN_ID+ " = " + id, null);
			}
		}
	}
	public void removeFriendsName(String name) {
	    ArrayList<FriendsModel> list = getAllFriends();
		for(int x=0;x<list.size();x++){
			if(list.get(x).getName().equals(name)){
				 long id = list.get(x).getID();
				 database.delete(MySQLiteHelper.TABLE_FRIENDSLIST_BLOCKED, MySQLiteHelper.COLUMN_ID+ " = " + id, null);
			}
		}
	}
	public void removeFriendsJID(String jid) {
	    ArrayList<FriendsModel> list = getAllFriends();
		for(int x=0;x<list.size();x++){
			if(list.get(x).getJID().equals(jid)){
				 long id = list.get(x).getID();
				 database.delete(MySQLiteHelper.TABLE_FRIENDSLIST_BLOCKED, MySQLiteHelper.COLUMN_ID+ " = " + id, null);
			}
		}
	}
	
	public void deleteAllFriends() {
		ArrayList<FriendsModel> list = getAllFriends();
		for(int x=0;x<list.size();x++){
			long id = list.get(x).getID();
		    database.delete(MySQLiteHelper.TABLE_FRIENDSLIST_BLOCKED, MySQLiteHelper.COLUMN_ID+ " = " + id, null);
		}
	}
	
	public ArrayList<FriendsModel> getAllFriends() {
		ArrayList<FriendsModel> list = new ArrayList<FriendsModel>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_FRIENDSLIST_BLOCKED, allFriends, null, null, null, null, null);
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	FriendsModel name = cursorToFriends(cursor);
	    	FriendsManager.getInstance().getFriendsListManager().removeFriends(name);
	    	FriendsManager.getInstance().getFriendsWaitingMeApproveManager().removeFriends(name);
	    	FriendsManager.getInstance().getFriendsPenddingHeConfirmManager().removeFriends(name);
	    	list.add(name);
		    cursor.moveToNext();
		}
		cursor.close();
		return list;
	}
}
