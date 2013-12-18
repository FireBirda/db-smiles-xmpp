package com.digitalbuana.smiles.data;
import android.database.sqlite.SQLiteDatabase;
public interface DatabaseTable {
	void create(SQLiteDatabase db);
	void migrate(SQLiteDatabase db, int toVersion);
	void clear();
}
