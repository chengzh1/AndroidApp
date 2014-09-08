package com.fitmap.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.fitmap.utils.NewRunDataContract.NewRunSchema;

/**
 * SQLite helper class
 */
public class NewRunDbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";    //sqlite has no data type
    private static final String REAL_TYPE = " REAL";
    private static final String BOOL_TYPE = " INTEGER"; //sqlite has no boolean type
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NewRunSchema.TABLE_NAME +
            " (" +
                    NewRunSchema._ID + " INTEGER PRIMARY KEY," +
                    NewRunSchema.StartTime + TEXT_TYPE + "Unique" + COMMA_SEP +
                    NewRunSchema.StartL + REAL_TYPE + COMMA_SEP +
                    NewRunSchema.StartB + REAL_TYPE + COMMA_SEP +
                    NewRunSchema.EndL + REAL_TYPE + COMMA_SEP +
                    NewRunSchema.EndB + REAL_TYPE + COMMA_SEP +
                    NewRunSchema.Alarm + TEXT_TYPE + COMMA_SEP +
                    NewRunSchema.isEnd + BOOL_TYPE +
             " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NewRunSchema.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NewRun.db";

    public NewRunDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create schema
     * @param db database
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Upgrade schema
     * @param db database
     * @param oldVersion old version
     * @param newVersion new version
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Downgrade schema
     * @param db database
     * @param oldVersion old version
     * @param newVersion new version
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Delete table
     * @param db database
     */
    public void delete(SQLiteDatabase db){
        db.execSQL(SQL_DELETE_ENTRIES);
    }

    /**
     * Insert a tuple to table
     * @param db     database
     * @param date   date
     * @param startL start longitude
     * @param startB start latitude
     * @param endL   end longitude
     * @param endB   end latitude
     * @param alarm  if alarm
     * @return insert id
     */
    public long insert(SQLiteDatabase db, String date, double startL, double startB,
                       double endL, double endB, String alarm){
        ContentValues values = new ContentValues();
        values.put(NewRunSchema.StartTime, date);
        values.put(NewRunSchema.StartL, startL);
        values.put(NewRunSchema.StartB, startB);
        values.put(NewRunSchema.EndL, endL);
        values.put(NewRunSchema.EndB, endB);
        values.put(NewRunSchema.Alarm, alarm);
        values.put(NewRunSchema.isEnd, 0);
        long rowId = db.insert(
                        NewRunSchema.TABLE_NAME,
                        null,
                        values);
        return rowId;
    }

    /**
     * Delete a tuple by its position
     * @param db database
     * @param position position in the list
     * @return id of the deleted tuple
     */
    public int deleteByPosition(SQLiteDatabase db, int position){
        int id = -1;
        Cursor cursor = db.query(
                NewRunSchema.TABLE_NAME,  			    // The table to query
                NewRunSchema.projection,                // The columns to return
                null,                                	// The columns for the WHERE clause
                null,                            		// The values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                NewRunSchema.sortOrder                  // The sort order
        );
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            cursor.move(position);
            id = cursor.getInt(cursor.getColumnIndex(NewRunSchema._ID));
            db.delete(NewRunSchema.TABLE_NAME, NewRunSchema._ID + "=" + id, null);
        }
        cursor.close();
        return id;
    }

    /**
     * Set isEnd be true
     * @param db databse
     * @param statTime start time (it is unique)
     */
    public void endRun(SQLiteDatabase db, String statTime){
        ContentValues values = new ContentValues();
        values.put(NewRunSchema.isEnd, 1);
        db.update(NewRunSchema.TABLE_NAME, values, NewRunSchema.StartTime + "=\"" + statTime + "\"", null);
    }

}
