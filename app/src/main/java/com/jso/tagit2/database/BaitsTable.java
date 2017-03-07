package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class BaitsTable implements IDatabaseTable {

    public static String TABLE_NAME = "Baits";

    public static String COL_BAIT_ID = "FisherId";
    public static String COLSPEC_BAIT_ID = "TEXT UNIQUE";

    public static String COL_NAME = "Name";
    public static String COLSPEC_NAME = "TEXT";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + SQL_COL_ID + ", "
            + String.format(COL_FMT, COL_BAIT_ID, COLSPEC_BAIT_ID)
            + String.format(COL_FMT_LAST, COL_NAME, COLSPEC_NAME)
            + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
