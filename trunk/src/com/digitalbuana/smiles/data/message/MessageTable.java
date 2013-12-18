/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * 
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * 
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.digitalbuana.smiles.data.message;

import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.digitalbuana.smiles.data.DatabaseManager;
import com.digitalbuana.smiles.data.entity.AbstractEntityTable;

/**
 * Storage with messages.
 * 
 * @author alexander.ivanov
 */
public class MessageTable extends AbstractEntityTable {

	private static final class Fields implements AbstractEntityTable.Fields {

		private Fields() {
		}

		public static final String ISREADBYFRIEND = "readbyfriend";
		public static final String PACKETID = "packetid";

		/**
		 * Message archive collection tag.
		 */
		public static final String TAG = "tag";

		/**
		 * User's resource or nick in chat room.
		 */
		public static final String RESOURCE = "resource";

		/**
		 * Text message.
		 */
		public static final String TEXT = "text";

		/**
		 * Message action.
		 * <ul>
		 * <li>Must be empty string for usual text message.</li>
		 * <li>Must be one of names in MessageAction.</li>
		 * </ul>
		 * 
		 * {@link #TEXT} can contains some description on this action.
		 */
		public static final String ACTION = "action";

		/**
		 * Time when this message was created locally.
		 */
		public static final String TIMESTAMP = "timestamp";

		/**
		 * Receive and send delay.
		 */
		public static final String DELAY_TIMESTAMP = "delay_timestamp";

		/**
		 * Whether message is incoming.
		 */
		public static final String INCOMING = "incoming";

		/**
		 * Whether incoming message was read.
		 */
		public static final String READ = "read";

		/**
		 * Whether this outgoing message was sent.
		 */
		public static final String SENT = "sent";

		/**
		 * Whether this outgoing message was not received.
		 */
		public static final String ERROR = "error";

		/**
		 * is delivered status
		 * */
		public static final String DELIVERED = "delivered";

	}

	private static final String NAME = "messages";
	private static final String[] PROJECTION = new String[] { Fields._ID,
			Fields.ACCOUNT, Fields.USER, Fields.RESOURCE, Fields.TEXT,
			Fields.ACTION, Fields.TIMESTAMP, Fields.DELAY_TIMESTAMP,
			Fields.INCOMING, Fields.READ, Fields.SENT, Fields.ERROR,
			Fields.TAG, Fields.PACKETID, Fields.ISREADBYFRIEND,
			Fields.DELIVERED };

	private static final String[] RECENT = new String[] { Fields.ACCOUNT,
			Fields.USER };

	private final DatabaseManager databaseManager;
	private SQLiteStatement insertNewMessageStatement;
	private final Object insertNewMessageLock;

	private final static MessageTable instance;

	private String TAG = getClass().getSimpleName();

	static {
		instance = new MessageTable(DatabaseManager.getInstance());
		DatabaseManager.getInstance().addTable(instance);
	}

	public static MessageTable getInstance() {
		return instance;
	}

	private MessageTable(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
		insertNewMessageStatement = null;
		insertNewMessageLock = new Object();
	}

	@Override
	public void create(SQLiteDatabase db) {
		String sql;
		sql = "CREATE TABLE " + NAME + " (" + Fields._ID
				+ " INTEGER PRIMARY KEY," + Fields.ACCOUNT + " TEXT,"
				+ Fields.USER + " TEXT," + Fields.RESOURCE + " TEXT,"
				+ Fields.TEXT + " TEXT," + Fields.ACTION + " TEXT,"
				+ Fields.TIMESTAMP + " INTEGER," + Fields.DELAY_TIMESTAMP
				+ " INTEGER," + Fields.INCOMING + " BOOLEAN," + Fields.READ
				+ " BOOLEAN," + Fields.SENT + " BOOLEAN," + Fields.ERROR
				+ " BOOLEAN," + Fields.TAG + " TEXT, " + Fields.PACKETID
				+ " TEXT, " + Fields.ISREADBYFRIEND + " BOOLEAN);";
		DatabaseManager.execSQL(db, sql);
		sql = "CREATE INDEX " + NAME + "_list ON " + NAME + " ("
				+ Fields.ACCOUNT + ", " + Fields.USER + ", " + Fields.TIMESTAMP
				+ " ASC)";
		DatabaseManager.execSQL(db, sql);
	}

	public void setUpdateMessageTable() {
		try {
			SQLiteDatabase db = databaseManager.getWritableDatabase();
			db.execSQL("ALTER TABLE messages ADD COLUMN delivered BOOLEAN;");
			db.execSQL("ALTER TABLE messages ADD COLUMN packetid TEXT;");
			db.execSQL("ALTER TABLE messages ADD COLUMN readbyfriend BOOLEAN;");
		} catch (RuntimeException e) {
			return;
		}
	}

	@Override
	public void migrate(SQLiteDatabase db, int toVersion) {
		super.migrate(db, toVersion);
		String sql;
		switch (toVersion) {
		case 4:
			sql = "CREATE TABLE messages (_id INTEGER PRIMARY KEY,"
					+ "account INTEGER," + "user TEXT," + "text TEXT,"
					+ "timestamp INTEGER," + "delay_timestamp INTEGER,"
					+ "incoming BOOLEAN," + "read BOOLEAN,"
					+ "notified BOOLEAN, packetid TEXT, readbyfriend BOOLEAN);";
			DatabaseManager.execSQL(db, sql);
			sql = "CREATE INDEX messages_list ON messages (account, user, timestamp ASC);";
			DatabaseManager.execSQL(db, sql);
			break;
		case 8:
			DatabaseManager.dropTable(db, "messages");
			sql = "CREATE TABLE messages (_id INTEGER PRIMARY KEY,"
					+ "account TEXT," + "user TEXT," + "text TEXT,"
					+ "timestamp INTEGER," + "delay_timestamp INTEGER,"
					+ "incoming BOOLEAN," + "read BOOLEAN,"
					+ "notified BOOLEAN, packetid TEXT, readbyfriend BOOLEAN);";
			DatabaseManager.execSQL(db, sql);
			sql = "CREATE INDEX messages_list ON messages (account, user, timestamp ASC);";
			DatabaseManager.execSQL(db, sql);
			break;
		case 10:

			db.execSQL("ALTER TABLE messages ADD COLUMN packetid TEXT;");
			db.execSQL("ALTER TABLE messages ADD COLUMN readbyfriend BOOLEAN;");

			sql = "ALTER TABLE messages ADD COLUMN send BOOLEAN;";
			DatabaseManager.execSQL(db, sql);
			sql = "ALTER TABLE messages ADD COLUMN error BOOLEAN;";
			DatabaseManager.execSQL(db, sql);
			sql = "UPDATE messages SET send = 1, error = 0 WHERE incoming = 0;";
			DatabaseManager.execSQL(db, sql);
			break;
		case 15:
			sql = "UPDATE messages SET send = 1 WHERE incoming = 1;";
			DatabaseManager.execSQL(db, sql);
			break;
		case 17:
			sql = "ALTER TABLE messages ADD COLUMN save BOOLEAN;";
			DatabaseManager.execSQL(db, sql);
			sql = "UPDATE messages SET save = 1;";
			DatabaseManager.execSQL(db, sql);
			break;
		case 23:
			sql = "ALTER TABLE messages ADD COLUMN resource TEXT;";
			DatabaseManager.execSQL(db, sql);
			sql = "UPDATE messages SET resource = \"\";";
			DatabaseManager.execSQL(db, sql);
			sql = "ALTER TABLE messages ADD COLUMN action TEXT;";
			DatabaseManager.execSQL(db, sql);
			sql = "UPDATE messages SET action = \"\";";
			DatabaseManager.execSQL(db, sql);
			break;
		case 27:
			DatabaseManager.renameTable(db, "messages", "old_messages");
			sql = "CREATE TABLE messages (_id INTEGER PRIMARY KEY,"
					+ "account TEXT," + "user TEXT," + "resource TEXT,"
					+ "text TEXT," + "action TEXT," + "timestamp INTEGER,"
					+ "delay_timestamp INTEGER," + "incoming BOOLEAN,"
					+ "read BOOLEAN," + "notified BOOLEAN," + "send BOOLEAN,"
					+ "error BOOLEAN);";
			DatabaseManager.execSQL(db, sql);
			sql = "INSERT INTO messages ("
					+ "account, user, resource, text, action, timestamp, delay_timestamp, incoming, read, notified, send, error"
					+ ") SELECT "
					+ "account, user, resource, text, action, timestamp, delay_timestamp, incoming, read, notified, send, error"
					+ " FROM old_messages WHERE save;";
			DatabaseManager.execSQL(db, sql);
			DatabaseManager.dropTable(db, "old_messages");
			// Create index after drop old index.
			sql = "CREATE INDEX messages_list ON messages (account, user, timestamp ASC);";
			DatabaseManager.execSQL(db, sql);
			break;
		case 28:
			DatabaseManager.renameTable(db, "messages", "old_messages");
			sql = "CREATE TABLE messages (_id INTEGER PRIMARY KEY,"
					+ "account TEXT," + "user TEXT," + "resource TEXT,"
					+ "text TEXT," + "action TEXT," + "timestamp INTEGER,"
					+ "delay_timestamp INTEGER," + "incoming BOOLEAN,"
					+ "read BOOLEAN," + "notified BOOLEAN," + "sent BOOLEAN,"
					+ "error BOOLEAN);";
			DatabaseManager.execSQL(db, sql);
			sql = "INSERT INTO messages ("
					+ "account, user, resource, text, action, timestamp, delay_timestamp, incoming, read, notified, sent, error"
					+ ") SELECT "
					+ "account, user, resource, text, action, timestamp, delay_timestamp, incoming, read, notified, send, error"
					+ " FROM old_messages;";
			DatabaseManager.execSQL(db, sql);
			DatabaseManager.dropTable(db, "old_messages");
			sql = "CREATE INDEX messages_list ON messages (account, user, timestamp ASC);";
			DatabaseManager.execSQL(db, sql);
			break;
		case 58:
			sql = "ALTER TABLE messages ADD COLUMN tag TEXT;";
			DatabaseManager.execSQL(db, sql);
			break;
		case 61:
			DatabaseManager.renameTable(db, "messages", "old_messages");
			sql = "CREATE TABLE messages (_id INTEGER PRIMARY KEY,"
					+ "account TEXT," + "user TEXT," + "resource TEXT,"
					+ "text TEXT," + "action TEXT," + "timestamp INTEGER,"
					+ "delay_timestamp INTEGER," + "incoming BOOLEAN,"
					+ "read BOOLEAN," + "sent BOOLEAN," + "error BOOLEAN,"
					+ "tag TEXT);";
			DatabaseManager.execSQL(db, sql);
			sql = "INSERT INTO messages ("
					+ "account, user, resource, text, action, timestamp, delay_timestamp, incoming, read, sent, error, tag"
					+ ") SELECT "
					+ "account, user, resource, text, action, timestamp, delay_timestamp, incoming, read, sent, error, tag"
					+ " FROM old_messages;";
			DatabaseManager.execSQL(db, sql);
			DatabaseManager.dropTable(db, "old_messages");
			sql = "CREATE INDEX messages_list ON messages (account, user, timestamp ASC);";
			DatabaseManager.execSQL(db, sql);
			break;
		default:
			break;
		}
	}

	private Cursor checkMessageExists(String packetId, Date timeStamp,
			String user) {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		String query = "SELECT * FROM " + NAME + " WHERE " + Fields.PACKETID
				+ " = '" + packetId + "' AND " + Fields.TIMESTAMP + " = "
				+ String.valueOf(timeStamp.getTime()) + " AND " + Fields.USER
				+ " = '" + user + "'";
		return db.rawQuery(query, null);

	}

	/**
	 * Save new message to the database.
	 * 
	 * @return Assigned id.
	 */
	public long add(String account, String bareAddress, String tag,
			String resource, String text, ChatAction action, Date timeStamp,
			Date delayTimeStamp, boolean incoming, boolean read, boolean sent,
			boolean error, String packetId, boolean isreadbyfriend) {

		if (incoming) {
			Cursor checkMessageExist = this.checkMessageExists(packetId,
					timeStamp, bareAddress);
			if (checkMessageExist.getCount() > 0) {
				checkMessageExist.close();
				Log.e(TAG, "message exists...");
				return 0;
			}
			checkMessageExist.close();
		}

		final String actionString;
		if (action == null)
			actionString = "";
		else
			actionString = action.name();
		synchronized (insertNewMessageLock) {
			if (insertNewMessageStatement == null) {
				SQLiteDatabase db = databaseManager.getWritableDatabase();
				insertNewMessageStatement = db.compileStatement("INSERT INTO "
						+ NAME + " (" + Fields.ACCOUNT + ", " + Fields.USER
						+ ", " + Fields.RESOURCE + ", " + Fields.TEXT + ", "
						+ Fields.ACTION + ", " + Fields.TIMESTAMP + ", "
						+ Fields.DELAY_TIMESTAMP + ", " + Fields.INCOMING
						+ ", " + Fields.READ + ", " + Fields.SENT + ", "
						+ Fields.ERROR + ", " + Fields.TAG + ", "
						+ Fields.PACKETID + ", " + Fields.ISREADBYFRIEND + ", "
						+ Fields.DELIVERED + ") VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			}
			insertNewMessageStatement.bindString(1, account);
			insertNewMessageStatement.bindString(2, bareAddress);
			insertNewMessageStatement.bindString(3, resource);
			insertNewMessageStatement.bindString(4, text);
			insertNewMessageStatement.bindString(5, actionString);
			insertNewMessageStatement.bindLong(6, timeStamp.getTime());

			if (delayTimeStamp == null)
				insertNewMessageStatement.bindNull(7);
			else
				insertNewMessageStatement.bindLong(7, delayTimeStamp.getTime());

			insertNewMessageStatement.bindLong(8, incoming ? 1 : 0);
			insertNewMessageStatement.bindLong(9, read ? 1 : 0);
			insertNewMessageStatement.bindLong(10, sent ? 1 : 0);
			insertNewMessageStatement.bindLong(11, error ? 1 : 0);

			if (tag == null)
				insertNewMessageStatement.bindNull(12);
			else
				insertNewMessageStatement.bindString(12, tag);

			insertNewMessageStatement.bindString(13, packetId);

			insertNewMessageStatement.bindLong(14, isreadbyfriend ? 1 : 0);
			insertNewMessageStatement.bindLong(15, 0);
			long in = 0;// = insertNewMessageStatement.executeInsert();

			try {
				in = insertNewMessageStatement.executeInsert();
			} catch (SQLException e) {
				Log.e(TAG, e.getMessage());
			}
			markPacketId(in, packetId);

			return in;
		}
	}

	public void markAsReadByFriend(String ids) {
		if (ids == null || ids.equals("")) {
			return;
		}
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Fields.ISREADBYFRIEND, 1);
		db.update(NAME, values, Fields.PACKETID + "='" + ids + "'", null);
	}

	public void markAsUnSent(String ids) {
		if (ids == null || ids.equals("")) {
			return;
		}
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Fields.SENT, 0);
		db.update(NAME, values, Fields.PACKETID + "='" + ids + "'", null);
	}

	public void markAsDelivered(String ids) {
		if (ids == null || ids.equals("")) {
			return;
		}
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Fields.DELIVERED, 1);
		int updateStatus = db.update(NAME, values, Fields.PACKETID + "='" + ids
				+ "'", null);
		Log.v("markAsDelivered", "updateStatus:" + updateStatus);
	}

	void markPacketId(Long ids, String packetId) {
		if (ids == null || ids.equals("") || packetId == null
				|| packetId.equals("")) {
			return;
		}
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Fields.PACKETID, packetId);
		db.update(NAME, values, Fields._ID + "=" + ids, null);
	}

	Cursor getPacketId(long id) {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, PROJECTION, Fields._ID + "=?", new String[] { ""
				+ id }, null, null, null);
	}

	Cursor getIsReadByFriend(long id) {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, PROJECTION, Fields._ID + "=?", new String[] { ""
				+ id }, null, null, null);
	}

	void markAsRead(Collection<Long> ids) {
		if (ids.isEmpty()) {
			return;
		}
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Fields.READ, 1);
		db.update(NAME, values, DatabaseManager.in(Fields._ID, ids), null);
	}

	void markAsSent(Collection<Long> ids) {
		if (ids.isEmpty())
			return;
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Fields.SENT, 1);
		db.update(NAME, values, DatabaseManager.in(Fields._ID, ids), null);
	}

	void markAsError(long id) {
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Fields.ERROR, 1);
		db.update(NAME, values, Fields._ID + " = ?",
				new String[] { String.valueOf(id) });
	}

	/**
	 * @param account
	 * @param bareAddress
	 * @return Result set with messages for the chat.
	 */
	public Cursor list(String account, String bareAddress) {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, PROJECTION, Fields.ACCOUNT + " = ? AND "
				+ Fields.USER + " = ?", new String[] { account, bareAddress },
				null, null, Fields._ID);
	}

	public Cursor last(String account, String bareAddress) {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, PROJECTION, Fields.ACCOUNT + " = ? AND "
				+ Fields.USER + " = ?", new String[] { account, bareAddress },
				null, null, Fields._ID + " DESC", "1");
	}

	public Cursor lastList() {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, new String[] { Fields.USER }, null, null,
				Fields.USER, null, Fields._ID + " DESC");
	}

	public Cursor unreadCounter(String account, String bareAddress) {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, PROJECTION, Fields.ACCOUNT + " = ? AND "
				+ Fields.USER + " = ? AND " + Fields.READ + " = ? ",
				new String[] { account, bareAddress, "0" }, null, null,
				Fields.TIMESTAMP);
	}

	public Cursor recentList() {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, RECENT, null, null, Fields.ACCOUNT + ","
				+ Fields.USER, null, Fields._ID + " DESC");
	}

	/**
	 * @return Messages to be sent.
	 */
	Cursor messagesToSend() {
		SQLiteDatabase db = databaseManager.getReadableDatabase();
		return db.query(NAME, PROJECTION, Fields.INCOMING + " = ? AND "
				+ Fields.SENT + " = ?", new String[] { "0", "0" }, null, null,
				Fields.TIMESTAMP);
	}

	/**
	 * Removes all read and sent messages.
	 * 
	 * @param account
	 */
	void removeReadAndSent(String account) {
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		db.delete(NAME, Fields.ACCOUNT + " = ? AND " + Fields.READ
				+ " = ? AND " + Fields.SENT + " = ?", new String[] { account,
				"1", "1" });
	}

	/**
	 * Removes all sent messages.
	 * 
	 * @param account
	 */
	void removeSent(String account) {
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		db.delete(NAME, Fields.ACCOUNT + " = ? AND " + Fields.SENT + " = ?",
				new String[] { account, "1", });
	}

	void removeMessages(Collection<Long> ids) {
		if (ids.isEmpty())
			return;
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		db.delete(NAME, DatabaseManager.in(Fields._ID, ids), null);
	}

	@Override
	protected String getTableName() {
		return NAME;
	}

	@Override
	protected String[] getProjection() {
		return PROJECTION;
	}

	public static long getId(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndex(Fields._ID));
	}

	public static String getTag(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.TAG));
	}

	public static String getResource(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.RESOURCE));
	}

	public static String getText(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.TEXT));
	}

	public static ChatAction getAction(Cursor cursor) {
		return ChatAction.getChatAction(cursor.getString(cursor
				.getColumnIndex(Fields.ACTION)));
	}

	public static boolean isDelivered(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(Fields.DELIVERED)) != 0;
	}

	public static boolean isIncoming(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(Fields.INCOMING)) != 0;
	}

	public static boolean isSent(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(Fields.SENT)) != 0;
	}

	public static boolean isRead(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(Fields.READ)) != 0;
	}

	public static boolean hasError(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(Fields.ERROR)) != 0;
	}

	public static Date getTimeStamp(Cursor cursor) {
		TimeZone.setDefault(TimeZone.getDefault());
		return new Date(cursor.getLong(cursor.getColumnIndex(Fields.TIMESTAMP)));
	}

	public static boolean getReadByFriend(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(Fields.ISREADBYFRIEND)) != 0;
	}

	public static String getPacketId(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.PACKETID));
	}

	public static String getUser(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.USER));
	}

	public static Date getDelayTimeStamp(Cursor cursor) {
		TimeZone.setDefault(TimeZone.getDefault());
		if (cursor.isNull(cursor.getColumnIndex(Fields.DELAY_TIMESTAMP)))
			return null;
		return new Date(cursor.getLong(cursor
				.getColumnIndex(Fields.DELAY_TIMESTAMP)));
	}
}