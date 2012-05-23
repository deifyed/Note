/*
 * Practically a copy/paste of Googles NotePad tutorial' NotesDbAdapter.java
 * http://developer.android.com/resources/tutorials/notepad/index.html
 * If you are to use it, know its Google's.
 */

package net.develish.note;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLAdapter {
	
	// General variables
	private final String DTG_FORMAT = "yyyyMMddHHmmss";
	
	private static final String DATABASE_NAME = "notesdb";
	private static final String DATABASE_TABLE = "notestable";
	private static final int DATABASE_VERSION = 3;
	
	// Note keys
	public static final String KEY_TITLE = "ntitle";
	public static final String KEY_BODY = "nbody";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_LASTCHANGED = "lstchngd";
	

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	// String creating the notes table
	private static final String DATABASE_CREATE =
			"create table " + DATABASE_TABLE + " (" + KEY_ROWID + 
			" integer primary key autoincrement," + KEY_TITLE +
			" text not null, " + KEY_BODY + " text, " + KEY_LASTCHANGED +
			" long not null);";
	
	// String for upgrading from 0.0.4 to 0.0.5
	private static final String DATABASE_UPGRADE =
			"alter table " + DATABASE_TABLE + " add " + KEY_LASTCHANGED +
			" integer not null";
	
	private final Context ctx;
	
	/**
	 * Does some basic database functionality
	 * @author despicium
	 *
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			if(oldVersion > 3)
				db.execSQL(DATABASE_UPGRADE);
		}
	}
	
	public SQLAdapter(Context ctx) {
		
		this.ctx = ctx;
	}
	
	/**
	 * Opens a writable database for creation, editing and reading note data.
	 * @return SQLAdapter
	 * @throws SQLException
	 */
	public SQLAdapter open() throws SQLException {
		
		mDbHelper = new DatabaseHelper(ctx);
		mDb = mDbHelper.getWritableDatabase();
		return(this);
	}
	
	/**
	 * Closes the writable database. Remember to close after opening : )
	 */
	public void close() {
		mDbHelper.close();
	}
	
	/**
	 * Adds a new note to the note database.
	 * @param title The title of the new note
	 * @param body The body of the new note
	 * @return A long representing the row ID of the new note
	 */
	public long createNote(String title, String body) {
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_BODY, body);
		
		SimpleDateFormat formater = new SimpleDateFormat(DTG_FORMAT);
		
		initialValues.put(KEY_LASTCHANGED, Long.parseLong("" + formater.format(new Date())));
		
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}
	
	/**
	 * Removes a note from the note database.
	 * @param rowId The row ID of the note to be removed
	 * @return A boolean representing the success of the delete request
	 */
	public boolean deleteNote(long rowId) {
		
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * Fetches all notes from the note database
	 * @return A cursor representing the note table
	 */
	public Cursor fetchAllNotes() {
		
		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_BODY}, null, null, null, null, KEY_LASTCHANGED + " DESC");
	}
	
	/**
	 * Fetches a spesific note from the note table
	 * @param rowId The row ID of the note to be fetched
	 * @return A cursor containing the note data
	 * @throws SQLException
	 */
	public Cursor fetchNote(long rowId) throws SQLException {
		
		Cursor mCursor =
				mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if(mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}
	
	/**
	 * Updates note data of a spesific note
	 * @param rowId The row ID of the note to be updated
	 * @param title The new note title (doesn't need to be new, use old if title isn't updated)
	 * @param body The new body of the note
	 * @return A boolean representing the success of the update request
	 */
	public boolean updateNote(long rowId, String title, String body) {
		
		ContentValues args = new ContentValues();
		args.put(KEY_TITLE, title);
		args.put(KEY_BODY, body);
		
		SimpleDateFormat formater = new SimpleDateFormat(DTG_FORMAT);
		
		args.put(KEY_LASTCHANGED, Long.parseLong("" + formater.format(new Date())));
		
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
}
