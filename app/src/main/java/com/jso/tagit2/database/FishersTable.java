package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class FishersTable implements IDatabaseTable {

    public static String TABLE_NAME = "Fishers";

    public static String COL_FISHER_ID = "FisherId";
    public static String COLSPEC_FISHER_ID = "TEXT UNIQUE";

    public static String COL_NAME = "Name";
    public static String COLSPEC_NAME = "TEXT";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + SQL_COL_ID + ", "
            + String.format(COL_FMT, COL_FISHER_ID, COLSPEC_FISHER_ID)
            + String.format(COL_FMT_LAST, COL_NAME, COLSPEC_NAME)
            + ")";

    @Override
    public void onCreate(SQLiteDatabase db) { db.execSQL(TABLE_CREATE); }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
