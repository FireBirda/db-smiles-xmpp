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

public class VisitorManager implements OnLoadListener {

	private final static VisitorManager instance;

	static {
		instance = new VisitorManager();
		Application.getInstance().addManager(instance);
	}

	public static VisitorManager getInstance() {
		return instance;
	}

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	// Database fields
	private String[] allFriends = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_JID,
			MySQLiteHelper.COLUMN_TIME, };

	public VisitorManager() {
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
				"");
		return friends;
	}

	public void addVisitorByName(String name, String time) {
		addVisitor(name, time);
	}

	private void addVisitor(String name, String time) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NAME, name);
		values.put(MySQLiteHelper.COLUMN_JID, name + "@"
				+ AppConstants.XMPPServerHost);
		values.put(MySQLiteHelper.COLUMN_TIME, time);
		long insertId = database.insert(MySQLiteHelper.TABLE_VISITOR, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_VISITOR,
				allFriends, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		FriendsModel friend = cursorToFriends(cursor);
		if (Application.getInstance().isInitialized()) {
			visitorProvider.add(new VisitorNotification(AccountManager
					.getInstance().getAccountKu(), friend.getJID(), friend),
					true);
		}
		cursor.close();
	}

	public void removeVisitor(FriendsModel friends) {
		long id = friends.getID();
		database.delete(MySQLiteHelper.TABLE_VISITOR, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public void deleteAllVisitor() {
		ArrayList<FriendsModel> list = getAllVisitor();
		for (int x = 0; x < list.size(); x++) {
			long id = list.get(x).getID();
			database.delete(MySQLiteHelper.TABLE_VISITOR,
					MySQLiteHelper.COLUMN_ID + " = " + id, null);
		}
	}

	public ArrayList<FriendsModel> getAllVisitor() {
		ArrayList<FriendsModel> list = new ArrayList<FriendsModel>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_VISITOR,
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

	private final EntityNotificationProvider<VisitorNotification> visitorProvider = new EntityNotificationProvider<VisitorNotification>(
			R.drawable.ic_stat_attention) {
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
