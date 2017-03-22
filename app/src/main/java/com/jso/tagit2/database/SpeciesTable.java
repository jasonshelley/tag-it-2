package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class SpeciesTable extends BaseDatabaseTable {

    public static String TABLE_NAME = "Species";

    public static String COL_SPECIES_ID = "SpeciesId";
    public static String COLSPEC_SPECIES_ID = COL_SPECIES_ID + " TEXT UNIQUE";

    public static String COL_NAME = "Name";
    public static String COLSPEC_NAME = COL_NAME + " TEXT";

    public static String COL_BAG_LIMIT = "BagLimit";
    public static String COLSPEC_BAG_LIMIT = COL_BAG_LIMIT + " INTEGER";

    // Minimum size in cm
    public static String COL_SIZE_MIN = "SizeMin";
    public static String COLSPEC_SIZE_MIN = COL_SIZE_MIN + " INTEGER";

    // Maximum size in cm
    public static String COL_SIZE_MAX = "SizeMax";
    public static String COLSPEC_SIZE_MAX = COL_SIZE_MAX + " INTEGER";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + COLSPEC_ID + ", "
            + COLSPEC_IS_SYNCED + ", "
            + COLSPEC_LAST_MODIFIED + ", "
            + COLSPEC_SPECIES_ID + ", "
            + COLSPEC_BAG_LIMIT + ", "
            + COLSPEC_SIZE_MIN + ", "
            + COLSPEC_SIZE_MAX + ", "
            + COLSPEC_SELECTION_COUNT + ", "
            + COLSPEC_NAME
            + ")";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion < 4) {
            db.execSQL("alter table " + TABLE_NAME + " add column " + COLSPEC_SELECTION_COUNT);
        }
    }
}
