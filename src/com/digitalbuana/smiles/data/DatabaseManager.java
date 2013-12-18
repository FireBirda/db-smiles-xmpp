package com.digitalbuana.smiles.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.digitalbuana.smiles.data.entity.AbstractAccountTable;
public class DatabaseManager extends SQLiteOpenHelper implements
		OnLoadListener, OnClearListener {

	private static final String DATABASE_NAME = "smilesku.db";
	private static final int DATABASE_VERSION = 65;

	private static final SQLiteException DOWNGRAD_EXCEPTION = new SQLiteException("Database file was deleted");

	private final ArrayList<DatabaseTable> registeredTables;

	private final static DatabaseManager instance;

	static {
		instance = new DatabaseManager();
		Application.getInstance().addManager(instance);
	}

	public static DatabaseManager getInstance() {
		return instance;
	}

	private DatabaseManager() {
		super(Application.getInstance(), DATABASE_NAME, null, DATABASE_VERSION);
		registeredTables = new ArrayList<DatabaseTable>();
	}
	public void addTable(DatabaseTable table) {
		registeredTables.add(table);
	}

	@Override
	public void onLoad() {
		try {
			getWritableDatabase(); // Force onCreate or onUpgrade
		} catch (SQLiteException e) {
			if (e == DOWNGRAD_EXCEPTION) {
				// Downgrade occured
			} else {
				throw e;
			}
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (DatabaseTable table : registeredTables)
			table.create(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		/*
		 * ols smiles data destroyer
		 * */
		db.execSQL("DROP TABLE IF EXISTS chat");
		db.execSQL("DROP TABLE IF EXISTS recent_chat");
		db.execSQL("DROP TABLE IF EXISTS datanyauser");
		db.execSQL("DROP TABLE IF EXISTS media");
		db.execSQL("DROP TABLE IF EXISTS datanyaadmin");
		db.execSQL("DROP TABLE IF EXISTS sticker");
		db.execSQL("DROP TABLE IF EXISTS room");	
		
		db.execSQL("ALTER TABLE messages ADD COLUMN packetid TEXT;");
		db.execSQL("ALTER TABLE messages ADD COLUMN readbyfriend BOOLEAN;");
		
		//===============================================
		
		if (oldVersion > newVersion) {
			File file = new File(db.getPath());
			file.delete();
			throw DOWNGRAD_EXCEPTION;
			// This will interrupt getWritableDatabase() call from
			// DatabaseManager's constructor.
		} else {
			while (oldVersion < newVersion) {
				oldVersion += 1;
				migrate(db, oldVersion);
				for (DatabaseTable table : registeredTables)
					table.migrate(db, oldVersion);
				for (OnMigrationListener listener : Application.getInstance()
						.getManagers(OnMigrationListener.class))
					listener.onMigrate(oldVersion);
			}
		}
		onCreate(db);
	}

	/**
	 * Called on database migration.
	 * 
	 * @param db
	 * @param toVersion
	 */
	private void migrate(SQLiteDatabase db, int toVersion) {
		switch (toVersion) {
		case 42:
			dropTable(db, "geolocs");
			dropTable(db, "locations");
			break;
		default:
			break;
		}
	}

	@Override
	public void onClear() {
		for (DatabaseTable table : registeredTables)
			table.clear();
	}

	public void removeAccount(String account) {
		// TODO: replace with constraint.
		for (DatabaseTable table : registeredTables)
			if (table instanceof AbstractAccountTable)
				((AbstractAccountTable) table).removeAccount(account);
	}

	/**
	 * Builds IN statement for specified collection of values.
	 * 
	 * @param <T>
	 * @param column
	 * @param values
	 * @return "column IN (value1, ... valueN)" or
	 *         "(column IS NULL AND column IS NOT NULL)" if ids is empty.
	 */
	public static <T> String in(String column, Collection<T> values) {
		if (values.isEmpty())
			return new StringBuilder("(").append(column).append(" IS NULL AND ").append(column).append(" IS NOT NULL)").toString();
		StringBuilder builder = new StringBuilder(column);
		builder.append(" IN (");
		Iterator<T> iterator = values.iterator();
		while (iterator.hasNext()) {
			T value = iterator.next();
			if (value instanceof String)
				builder.append(DatabaseUtils.sqlEscapeString((String) value));
			else
				builder.append(value.toString());
			if (iterator.hasNext())
				builder.append(",");
		}
		builder.append(")");
		return builder.toString();
	}

	public static void execSQL(SQLiteDatabase db, String sql) {
		LogManager.iString(DatabaseManager.class.getName(), sql);
		db.execSQL(sql);
	}

	public static void dropTable(SQLiteDatabase db, String table) {
		execSQL(db, "DROP TABLE IF EXISTS " + table + ";");
	}

	public static void renameTable(SQLiteDatabase db, String table,
			String newTable) {
		execSQL(db, "ALTER TABLE " + table + " RENAME TO " + newTable + ";");
	}

	public static String commaSeparatedFromCollection(Collection<String> strings) {
		StringBuilder builder = new StringBuilder();
		for (String value : strings) {
			if (builder.length() > 0)
				builder.append(",");
			builder.append(value.replace("\\", "\\\\").replace(",", "\\,"));
		}
		return builder.toString();
	}

	public static Collection<String> collectionFromCommaSeparated(String value) {
		Collection<String> collection = new ArrayList<String>();
		boolean escape = false;
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < value.length(); index++) {
			char chr = value.charAt(index);
			if (!escape) {
				if (chr == '\\') {
					escape = true;
					continue;
				} else if (chr == ',') {
					collection.add(builder.toString());
					builder = new StringBuilder();
					continue;
				}
			}
			escape = false;
			builder.append(chr);
		}
		collection.add(builder.toString());
		return Collections.unmodifiableCollection(collection);
	}

}
