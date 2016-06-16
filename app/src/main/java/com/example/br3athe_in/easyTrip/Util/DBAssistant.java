package com.example.br3athe_in.easyTrip.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBAssistant extends SQLiteOpenHelper implements BaseColumns {
	static final String LOG_TAG = "TravelLogs";

	static final String DB_NAME = "travels.db";
	static final String DB_TABLE = "Travels";
	static final String KEY_NAME = "Name";
	static final String KEY_VAL = "Value";

	private static final String DATABASE_CREATE_SCRIPT =
					"create table " + DB_TABLE
									+ " (" + BaseColumns._ID + " integer primary key autoincrement, "
									+ KEY_NAME + " text not null, "
									+ KEY_VAL + " integer"
									+ ");";

	public DBAssistant(Context context) {
		super(context, DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_SCRIPT);
		ContentValues cv = new ContentValues();
		cv.put(KEY_NAME, "first");
		cv.put(KEY_VAL, 1);
		db.insert(DB_TABLE, null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nop. It's not implemented. It won't be. Never.
	}
}
