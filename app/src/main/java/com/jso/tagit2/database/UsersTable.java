package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class UsersTable implements IDatabaseTable {

    public static String TABLE_NAME = "Users";

    public static String COL_USER_ID = "UserId";
    public static String COLSPEC_USER_ID = "TEXT UNIQUE";

    public static String COL_USERNAME = "Username";
    public static String COLSPEC_USERNAME = "TEXT";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + IDatabaseTable.SQL_COL_ID + ", "
            + String.format(COL_FMT, COL_USER_ID, COLSPEC_USER_ID)
            + String.format(COL_FMT_LAST, COL_USERNAME, COLSPEC_USERNAME)
            + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
