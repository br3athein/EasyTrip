package com.example.br3athe_in.easyTrip.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBAssistant extends SQLiteOpenHelper implements BaseColumns {
	private static final String LOG_TAG = "TravelLogs";

	private static final String DB_NAME = "travels.db";
	private static final String DB_TABLE = "Travels";
	private static final String KEY_NAME = "Name";
	private static final String KEY_VAL = "Value";

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
		Log.d(LOG_TAG, "Database created");
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
