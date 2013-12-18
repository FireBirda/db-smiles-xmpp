package com.digitalbuana.smiles.data.friends;

import java.util.ArrayList;
import java.util.Collections;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.data.AppConstants;
import com.digitalbuana.smiles.data.Application;
import com.digitalbuana.smiles.data.OnLoadListener;
import com.digitalbuana.smiles.data.SettingsManager;
import com.digitalbuana.smiles.data.account.AccountManager;
import com.digitalbuana.smiles.data.notification.EntityNotificationProvider;
import com.digitalbuana.smiles.data.notification.NotificationManager;

public class FriendsUpdateManager implements OnLoadListener {
	private final static FriendsUpdateManager instance;

	static {
		instance = new FriendsUpdateManager();
		Application.getInstance().addManager(instance);
	}

	public static FriendsUpdateManager getInstance() {
		return instance;
	}

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	// Database fields
	private String[] allFriends = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_JID,
			MySQLiteHelper.COLUMN_TIME, MySQLiteHelper.COLUMN_MESSAGE, };

	public FriendsUpdateManager() {
		dbHelper = new MySQLiteHelper(Application.getInstance()
				.getApplicationContext());
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	private FriendsModel cursorToFriends(Cursor cursor) {
		FriendsModel friends = new FriendsModel(cursor.getLong(0),
				cursor.getString(1), cursor.getString(2), cursor.getString(3),
				cursor.getString(4));
		return friends;
	}

	public void addVisitorByName(String name, String time, String message) {
		addVisitor(name, time, message);
	}

	private void addVisitor(String name, String time, String message) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NAME, name);
		values.put(MySQLiteHelper.COLUMN_JID, name + "@"
				+ AppConstants.XMPPServerHost);
		values.put(MySQLiteHelper.COLUMN_TIME, time);
		values.put(MySQLiteHelper.COLUMN_MESSAGE, message);
		long insertId = database.insert(MySQLiteHelper.TABLE_FRIENDSUPDATE,
				null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_FRIENDSUPDATE,
				allFriends, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		FriendsModel friend = cursorToFriends(cursor);
		if (Application.getInstance().isInitialized()) {
			visitorProvider.add(new FriendsUpdateNotification(AccountManager
					.getInstance().getAccountKu(), friend.getJID(), friend),
					true);
		}
		cursor.close();
	}

	public void removeUpdate(FriendsModel friends) {
		long id = friends.getID();
		database.delete(MySQLiteHelper.TABLE_FRIENDSUPDATE,
				MySQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public void removeVisitorName(String name, String time) {
		ArrayList<FriendsModel> list = getFriendsUpdate();
		for (int x = 0; x < list.size(); x++) {
			if (list.get(x).getName().equals(name)
					&& list.get(x).getTime().equals(time)) {
				long id = list.get(x).getID();
				database.delete(MySQLiteHelper.TABLE_FRIENDSUPDATE,
						MySQLiteHelper.COLUMN_ID + " = " + id, null);
			}
		}
	}

	public void deleteAllFriendsUpdate() {
		ArrayList<FriendsModel> list = getFriendsUpdate();
		for (int x = 0; x < list.size(); x++) {
			long id = list.get(x).getID();
			database.delete(MySQLiteHelper.TABLE_FRIENDSUPDATE,
					MySQLiteHelper.COLUMN_ID + " = " + id, null);
		}
	}

	public ArrayList<FriendsModel> getFriendsUpdate() {
		ArrayList<FriendsModel> list = new ArrayList<FriendsModel>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_FRIENDSUPDATE,
				allFriends, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			FriendsModel name = cursorToFriends(cursor);
			list.add(name);
			cursor.moveToNext();
		}
		cursor.close();
		Collections.reverse(list);
		return list;
	}

	private final EntityNotificationProvider<FriendsUpdateNotification> visitorProvider = new EntityNotificationProvider<FriendsUpdateNotification>(
			R.drawable.ic_stat_friends_update) {
		@Override
		public Uri getSound() {
			return SettingsManager.eventsSound();
		}

		@Override
		public int getStreamType() {
			return AudioManager.STREAM_NOTIFICATION;
		}
	};

	@Override
	public void onLoad() {
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onLoaded();
			}
		});
	}

	private void onLoaded() {
		NotificationManager.getInstance().registerNotificationProvider(
				visitorProvider);
	}

	public void removeVisitorNotifications(String account, String user) {
		visitorProvider.remove(account, user);
	}

}
