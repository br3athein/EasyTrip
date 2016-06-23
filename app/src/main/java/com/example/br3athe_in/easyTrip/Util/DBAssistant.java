package com.example.br3athe_in.easyTrip.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.br3athe_in.easyTrip.R;

import java.util.ArrayList;
import java.util.HashMap;

public class DBAssistant extends SQLiteOpenHelper implements BaseColumns, DBKeys {
	private static final String LOG_TAG = "Custom";

	private static final String DATABASE_CREATE_SCRIPT =
			"create table " + DB_TABLE
					+ " (" + BaseColumns._ID + " integer primary key autoincrement, "
					+ KEY_NAME + " text not null, "
					+ KEY_VAL + " BLOB"
					+ ")";

	public DBAssistant(Context context) {
		super(context, DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_SCRIPT);
		Log.d(LOG_TAG, "Database created");
	}

	public Cursor getContentCursor(SQLiteDatabase db) {
		return db.query(
				DB_TABLE, new String[]{BaseColumns._ID, KEY_NAME, KEY_VAL}, null, null, null, null, null
		);
	}

	public boolean remove(int killId) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			db.execSQL("delete from " + DB_TABLE + " where " + BaseColumns._ID + " = " + killId);
			return true;
		} catch (SQLException e) {
			return false;
		}
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

		SQLiteDatabase db = getWritableDatabase();
		db.insert(DB_TABLE, null, cv);
		db.close();
		return true;
	}

	public ArrayList<Travel> readTravels(@NonNull ArrayList<Integer> idOut) {
		try {
			ArrayList<Travel> content = new ArrayList<>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = getContentCursor(db);
			Log.d(LOG_TAG, String.format("Cursor generated with length %d", cursor.getCount()));
			cursor.moveToFirst();

			do {
				int extractId = cursor.getColumnIndex(KEY_VAL);
				Log.d(
						LOG_TAG, "Attempts to get BLOB from column " + cursor.getColumnName(extractId) + "...");
				byte[] serializedTravel = cursor.getBlob(extractId);
				Log.d(
						LOG_TAG,
						"BLOB extraction executed, having length " + serializedTravel.length +
								". Deserializing...");
				content.add(Travel.deserialize(serializedTravel));
				idOut.add(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
				Log.d(LOG_TAG, "Deserializing done, BLOB  successfully casted to Travel.");
			} while (cursor.moveToNext());
			cursor.close();
			db.close();

			return content;
		} catch (SQLException e) {
			Log.d(LOG_TAG, "Something is wrong with the DB. " +
					"Perhaps, you forgot to initialize it, moron.");
			return new ArrayList<>();
		} catch (Exception e) {
			Log.d(LOG_TAG, "Something is wrong with the cursor. " +
					"Don't u fuck with empty databases.");
			return new ArrayList<>();
		}
	}

	public ArrayList<HashMap<String, String>> extractFillContent(ArrayList<Travel> travels, Context context) {
		ArrayList<HashMap<String, String>> result = new ArrayList<>();

		for(Travel t : travels) {
			HashMap<String, String> currentTravel = new HashMap<>();
			currentTravel.put(KEY_NAME, t.title);
			currentTravel.put(KEY_VAL,
					String.format(
							context.getString(
									R.string.travel_underline), t.citiesToVisit.size(), t.getLengthKms()));
			result.add(currentTravel);
		}
		return result;
	}

	public void nukeDB(boolean really) {
		if(really) {
			try {
				SQLiteDatabase db = getWritableDatabase();
				db.delete(DB_TABLE, null, null);

				db.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
