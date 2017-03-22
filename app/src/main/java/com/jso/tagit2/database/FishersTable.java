package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class FishersTable extends BaseDatabaseTable {

    public static String TABLE_NAME = "Fishers";

    public static String COL_FISHER_ID = "FisherId";
    public static String COLSPEC_FISHER_ID = COL_FISHER_ID + " TEXT UNIQUE";

    public static String COL_NAME = "Name";
    public static String COLSPEC_NAME = COL_NAME + " TEXT";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + COLSPEC_ID + ", "
            + COLSPEC_IS_SYNCED + ", "
            + COLSPEC_LAST_MODIFIED + ", "
            + COLSPEC_FISHER_ID + ", "
            + COLSPEC_SELECTION_COUNT + ", "
            + COLSPEC_NAME
            + ")";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) { db.execSQL(TABLE_CREATE); }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion < 4) {
            db.execSQL("alter table " + TABLE_NAME + " add column " + COLSPEC_SELECTION_COUNT);
        }
    }
}
