package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class CatchesTable extends BaseDatabaseTable {

    public static String TABLE_NAME = "Catches";

    public static String COL_CATCH_ID = "CatchId";
    public static String COLSPEC_CATCH_ID = COL_CATCH_ID + " TEXT UNIQUE";

    public static String COL_FISHER = "Fisher";
    public static String COLSPEC_FISHER = COL_FISHER + " TEXT";

    public static String COL_BAIT = "Bait";
    public static String COLSPEC_BAIT = COL_BAIT + " TEXT";

    public static String COL_SPECIES = "Species";
    public static String COLSPEC_SPECIES = COL_SPECIES + " TEXT";
    
    public static String COL_LATITUDE = "Latitude";
    public static String COLSPEC_LATITUDE = COL_LATITUDE + " REAL";

    public static String COL_LONGITUDE = "Longitude";
    public static String COLSPEC_LONGITUDE = COL_LONGITUDE + " REAL";

    // result of geocoding lat and long
    public static String COL_LOCATION_DESC = "LocationDesc";
    public static String COLSPEC_LOCATION_DESC = COL_LOCATION_DESC + " TEXT";

    public static String COL_LENGTH = "Length";
    public static String COLSPEC_LENGTH = COL_LENGTH + " REAL";

    public static String COL_WEIGHT = "Weight";
    public static String COLSPEC_WEIGHT = COL_WEIGHT + " REAL";

    public static String COL_THUMBNAIL_PATH = "ThumbnailPath";
    public static String COLSPEC_THUMBNAIL_PATH = COL_THUMBNAIL_PATH + " TEXT";

    public static String COL_IMAGE_PATH = "ImagePath";
    public static String COLSPEC_IMAGE_PATH = COL_IMAGE_PATH + " TEXT";

    public static String COL_TIMESTAMP = "Timestamp";
    public static String COLSPEC_TIMETAMP = COL_TIMESTAMP + " INTEGER";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + COLSPEC_ID + ", "
            + COLSPEC_IS_SYNCED + ", "
            + COLSPEC_LAST_MODIFIED + ", "
            + COLSPEC_CATCH_ID + ", "
            + COLSPEC_FISHER + ", "
            + COLSPEC_BAIT + ", "
            + COLSPEC_SPECIES + ", "
            + COLSPEC_LATITUDE + ", "
            + COLSPEC_LONGITUDE + ", "
            + COLSPEC_LENGTH + ", "
            + COLSPEC_WEIGHT + ", "
            + COLSPEC_LOCATION_DESC + ", "
            + COLSPEC_THUMBNAIL_PATH + ", "
            + COLSPEC_IMAGE_PATH + ", "
            + COLSPEC_TIMETAMP
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

        if (oldVersion < 5) {
            db.execSQL("DROP TABLE " + TABLE_NAME);
            onCreate(db);
        }
    }
}
