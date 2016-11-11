package com.begood.smarthome.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author Administrator
 * 
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;

	private final static String dbFileName = "smarthome.db";

	public DatabaseHelper(Context context) {
		this(context, dbFileName, null, VERSION);
	}

	public DatabaseHelper(Context context, String name) {
		this(context, name, null, VERSION);
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(" CREATE TABLE syscfg(  key varchar(200)  ,   value varchar(200)  ,  def_value varchar(200)	);    ");
		db.execSQL(" CREATE TABLE [dev] ( [id]  varchar(20), [name] varchar(20)  );    ");
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
