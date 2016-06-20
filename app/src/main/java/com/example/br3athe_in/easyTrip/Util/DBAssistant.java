package com.example.br3athe_in.easyTrip.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

public class DBAssistant extends SQLiteOpenHelper implements BaseColumns {
	private static final String LOG_TAG = "Custom";

	public static final String DB_NAME = "travels.db";
	public static final String DB_TABLE = "Travels";
	public static final String KEY_NAME = "Name";
	public static final String KEY_VAL = "Value";

	private SQLiteDatabase db;

	private static final String DATABASE_CREATE_SCRIPT =
			"create table " + DB_TABLE
					+ " (" + BaseColumns._ID + " integer primary key autoincrement, "
					+ KEY_NAME + " text not null, "
					+ KEY_VAL + " BLOB"
					+ ");";

	public DBAssistant(Context context) {
		super(context, DB_NAME, null, 1);
		db = getWritableDatabase();
		Log.d(LOG_TAG, "DB is ok, lies at " + db.getPath());
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_SCRIPT);
		Log.d(LOG_TAG, "Database created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nop. It's not implemented. It won't be. Never.
	}

	public boolean writeTravel(Travel travel) {
		ContentValues cv = new ContentValues();
		// we got a serializable travel here

		cv.put(KEY_NAME, travel.title);
		cv.put(KEY_VAL, travel.serialize());

		db.insert(DB_TABLE, null, cv);

		db.close();

		return true;
	}

	public ArrayList<Travel> readTravels() {
		try {
			ArrayList<Travel> content = new ArrayList<>();

			SQLiteDatabase db = getReadableDatabase();

			Cursor cursor = db.query(
					DB_TABLE, new String[]{KEY_NAME, KEY_VAL}, null, null, null, null, null
			);

			Log.d(LOG_TAG, String.format("Cursor generated with length %d", cursor.getCount()));

			cursor.moveToFirst();

			do {
				int extractId = 1;
				Log.d(LOG_TAG, "Attempt to get BLOB from column " + cursor.getColumnName(extractId) + "...");
				byte[] serializedTravel = cursor.getBlob(extractId);
				Log.d(LOG_TAG, "BLOB extraction executed");

				content.add(Travel.deserialize(serializedTravel));
			} while (cursor.moveToNext());
			cursor.close();
			db.close();

			return content;
		} catch (SQLException e) {
			Log.d(LOG_TAG, "Something is wrong with the DB. " +
					"Perhaps, you forgot to initialize it, moron.");
			return null;
		} catch (Exception e) {
			Log.d(LOG_TAG, "Something is wrong with the cursor. " +
					"Don't u fuck with empty databases.");
			return null;
		}
	}

	public void nukeDB(boolean really) {
		if(really) {
			db.execSQL("drop table " + DB_TABLE);
		}
	}
}
