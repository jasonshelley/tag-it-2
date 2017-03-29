package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class BaitsTable extends BaseDatabaseTable {

    public static String TABLE_NAME = "Baits";

    public static String COL_BAIT_ID = "BaitId";
    public static String COLSPEC_BAIT_ID = COL_BAIT_ID +  " TEXT UNIQUE";

    public static String COL_NAME = "Name";
    public static String COLSPEC_NAME = COL_NAME + " TEXT";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + COLSPEC_ID + ", "
            + COLSPEC_IS_SYNCED + ", "
            + COLSPEC_LAST_MODIFIED + ", "
            + COLSPEC_BAIT_ID + ", "
            + COLSPEC_NAME + ", "
            + COLSPEC_SELECTION_COUNT
            + ")";

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
       if (oldVersion < 6) {
            db.execSQL("drop table " + TABLE_NAME);
        }
    }

}
