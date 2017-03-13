package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class SpeciesTable implements IDatabaseTable {

    public static String TABLE_NAME = "Species";

    public static String COL_SPECIES_ID = "SpeciesId";
    public static String COLSPEC_SPECIES_ID = "TEXT UNIQUE";

    public static String COL_NAME = "Name";
    public static String COLSPEC_NAME = "TEXT";

    public static String COL_BAG_LIMIT = "BagLimit";
    public static String COLSPEC_BAG_LIMIT = "INTEGER";

    // Minimum size in cm
    public static String COL_SIZE_MIN = "SizeMin";
    public static String COLSPEC_SIZE_MIN = "INTEGER";

    // Maximum size in cm
    public static String COL_SIZE_MAX = "SizeMax";
    public static String COLSPEC_SIZE_MAX = "INTEGER";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + SQL_COL_ID + ", "
            + String.format(COL_FMT, COL_SPECIES_ID, COLSPEC_SPECIES_ID)
            + String.format(COL_FMT, COL_BAG_LIMIT, COLSPEC_BAG_LIMIT)
            + String.format(COL_FMT, COL_SIZE_MIN, COLSPEC_SIZE_MIN)
            + String.format(COL_FMT, COL_SIZE_MAX, COLSPEC_SIZE_MAX)
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
