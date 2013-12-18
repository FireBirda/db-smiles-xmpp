package com.digitalbuana.smiles.data.friends;

import java.util.ArrayList;
import java.util.Collections;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

public class AdminManager implements OnLoadListener {

	private final static AdminManager instance;

	static {
		instance = new AdminManager();
		Application.getInstance().addManager(instance);
	}

	public static AdminManager getInstance() {
		return instance;
	}

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	// Database fields
	private String[] allNotification = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_MESSAGE, MySQLiteHelper.COLUMN_TIME };

	public AdminManager() {
		dbHelper = new MySQLiteHelper(Application.getInstance()
				.getApplicationContext());
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	private AdminModel cursorToFriends(Cursor cursor) {
		AdminModel admin = new AdminModel(cursor.getLong(0),
				cursor.getString(1), cursor.getString(2));
		return admin;
	}

	public void addNotification(String msg, String time) {
		addNotificationByName(msg, time);
	}

	private void addNotificationByName(String msg, String time) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_MESSAGE, msg);
		values.put(MySQLiteHelper.COLUMN_TIME, time);
		values.put(MySQLiteHelper.COLUMN_SAVE, 0);
		long insertId = database.insert(MySQLiteHelper.TABLE_ADMIN, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_ADMIN,
				allNotification, MySQLiteHelper.COLUMN_ID + " = " + insertId,
				null, null, null, null);
		cursor.moveToFirst();
		AdminModel friend = cursorToFriends(cursor);
		if (Application.getInstance().isInitialized()) {
			adminProvider.add(new AdminNotification(AccountManager
					.getInstance().getAccountKu(), AppConstants.XMPPServerHost,
					friend), true);
		}
		cursor.close();
	}

	private Cursor getUnreadNotification() {
		return database.query(MySQLiteHelper.TABLE_ADMIN, allNotification,
				MySQLiteHelper.COLUMN_SAVE + " = 0", null, null, null, null);

	}

	public void removeNotification(AdminModel friends) {
		long id = friends.getID();
		database.delete(MySQLiteHelper.TABLE_ADMIN, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public void deleteAllNotification() {
		ArrayList<AdminModel> list = getAllNotification();
		for (int x = 0; x < list.size(); x++) {
			long id = list.get(x).getID();
			database.delete(MySQLiteHelper.TABLE_ADMIN,
					MySQLiteHelper.COLUMN_ID + " = " + id, null);
		}
	}

	public ArrayList<AdminModel> getAllNotification() {
		ArrayList<AdminModel> list = new ArrayList<AdminModel>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_ADMIN,
				allNotification, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			AdminModel name = cursorToFriends(cursor);
			list.add(name);
			cursor.moveToNext();
		}
		cursor.close();
		Collections.reverse(list);
		return list;
	}

	private final EntityNotificationProvider<AdminNotification> adminProvider = new EntityNotificationProvider<AdminNotification>(
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
				adminProvider);
	}

	public void removeAdminNotifications(String account, String user) {
		adminProvider.remove(account, user);
	}

	public void setUpdateTable() {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.execSQL("ALTER TABLE " + MySQLiteHelper.TABLE_ADMIN
					+ " ADD COLUMN " + MySQLiteHelper.COLUMN_SAVE + " BOOLEAN;");
		} catch (RuntimeException e) {
			return;
		}
	}

	public void markAsRead() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SAVE, 1);
		db.update(MySQLiteHelper.TABLE_ADMIN, values, null, null);
	}

	public int getUnread() {
		try {
			return getUnreadNotification().getCount();
		} catch (SQLiteException e) {
			return 0;
		}

	}

}
