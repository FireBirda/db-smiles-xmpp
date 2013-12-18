package com.digitalbuana.smiles.data.friends;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "smilesss.db";
	public static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_FRIENDSLIST = "FREINDS_LIST";
	public static final String TABLE_FRIENDSLIST_BLOCKED = "FREINDS_LIST_BLOCKED";
	public static final String TABLE_FRIENDSLIST_PENDING = "FREINDS_LIST_PENDING";
	public static final String TABLE_FRIENDSLIST_REQUEST = "FREINDS_LIST_REQUEST";
	
	public static final String TABLE_STICKER_PACKAGE = "STICKER_LIST_PACKAGE";
	public static final String TABLE_STICKER_DETAIL = "STICKER_LIST_DETAIL";
	
	public static final String TABLE_ADMIN = "ADMIN_LIST";
	public static final String TABLE_VISITOR = "VISITOR_LIST";
	public static final String TABLE_FRIENDSUPDATE = "FRIENDSUPDATE_LIST";
	
	public static final String COLUMN_ID = "_ID";
	public static final String COLUMN_SID = "IDKU";
	public static final String COLUMN_NAME = "NAMAKU";
	public static final String COLUMN_JID = "JIDKU";
	public static final String COLUMN_TIME = "TIMEKU";
	public static final String COLUMN_MESSAGE = "MESSAGEKU";
	
	public static final String COLUMN_DESC = "DESCKU";
	public static final String COLUMN_THUMB = "THUMBKU";
	public static final String COLUMN_PRICE = "PRICEKU";
	public static final String COLUMN_ALLOW_USE = "ALLOWKU";
	public static final String COLUMN_SAVE = "SAVEKU";
	
	
	private static final String DATABASE_CREATE_FRIENDS =
			  "create table " + TABLE_FRIENDSLIST
		      + "(" + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_NAME + " text not null, "
		      + COLUMN_JID +" text);";
	private static final String DATABASE_CREATE_FRIENDS_BLOCKED =
			  "create table " + TABLE_FRIENDSLIST_BLOCKED
		      + "(" + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_NAME + " text not null, "
		      + COLUMN_JID +" text);";
	private static final String DATABASE_CREATE_FRIENDS_PENDING =
			  "create table " + TABLE_FRIENDSLIST_PENDING
		      + "(" + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_NAME + " text not null, "
		      + COLUMN_JID +" text);";
	private static final String DATABASE_CREATE_FRIENDS_REQUEST =
			  "create table " + TABLE_FRIENDSLIST_REQUEST
		      + "(" + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_NAME + " text not null, "
		      + COLUMN_JID +" text);";
	  
	private static final String DATABASE_CREATE_STICKER_PACKAGE =
			  "create table " + TABLE_STICKER_PACKAGE
		      + "(" + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_SID + " text not null, "
		      + COLUMN_NAME + " text , "
		      + COLUMN_DESC + " text , "
		      + COLUMN_PRICE + " text , "
		      + COLUMN_THUMB + " text , "
		      + COLUMN_ALLOW_USE + " text , "
		      + COLUMN_SAVE +" text);";
	
	private static final String DATABASE_CREATE_STICKER_DETAIL =
			  "create table " + TABLE_STICKER_DETAIL
		      + "(" + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_SID + " text not null, "
		      + COLUMN_NAME + " text , "
		      + COLUMN_THUMB +" text);";
	
	private static final String DATABASE_CREATE_VISITOR =
			  "create table " + TABLE_VISITOR
		      + "(" + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_NAME + " text not null, "
		      + COLUMN_JID + " text, "
		      + COLUMN_TIME +" text);";
	
	private static final String DATABASE_CREATE_FRIENDSUPDATE =
			  "create table " + TABLE_FRIENDSUPDATE
		      + "(" + COLUMN_ID + " integer primary key autoincrement, "
		      + COLUMN_NAME + " text not null, "
		      + COLUMN_JID + " text, "
		      + COLUMN_TIME + " text, "
		      + COLUMN_MESSAGE +" text);";
	
	private static final String DATABASE_CREATE_NOTIFICATION =
		  "create table " + TABLE_ADMIN
	      + "(" + COLUMN_ID + " integer primary key autoincrement, "
	      + COLUMN_MESSAGE + " text, "
	      + COLUMN_TIME +" text);";
	
	
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_FRIENDS);
		db.execSQL(DATABASE_CREATE_FRIENDS_BLOCKED);
		db.execSQL(DATABASE_CREATE_FRIENDS_PENDING);
		db.execSQL(DATABASE_CREATE_FRIENDS_REQUEST);
		db.execSQL(DATABASE_CREATE_STICKER_PACKAGE);
		db.execSQL(DATABASE_CREATE_STICKER_DETAIL);
		
		db.execSQL(DATABASE_CREATE_VISITOR);
		db.execSQL(DATABASE_CREATE_FRIENDSUPDATE);
		db.execSQL(DATABASE_CREATE_NOTIFICATION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSLIST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSLIST_BLOCKED);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSLIST_PENDING);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSLIST_REQUEST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STICKER_PACKAGE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STICKER_DETAIL);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_VISITOR);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSUPDATE);
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CREATE_NOTIFICATION);
		onCreate(db);
	}

}
