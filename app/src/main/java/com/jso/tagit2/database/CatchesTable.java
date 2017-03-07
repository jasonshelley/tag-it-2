package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class CatchesTable implements IDatabaseTable {

    public static String TABLE_NAME = "Catches";

    public static String COL_CATCH_ID = "CatchId";
    public static String COLSPEC_CATCH_ID = "TEXT UNIQUE";

    public static String COL_FISHER_ID = FishersTable.COL_FISHER_ID;
    public static String COLSPEC_FISHER_ID = "TEXT";

    public static String COL_BAIT_ID = BaitsTable.COL_BAIT_ID;
    public static String COLSPEC_BAIT_ID = "TEXT";

    public static String COL_SPECIES_ID = SpeciesTable.COL_SPECIES_ID;
    public static String COLSPEC_SPECIES_ID = "TEXT";
    
    public static String COL_LATITUDE = "Latitude";
    public static String COLSPEC_LATITUDE = "REAL";

    public static String COL_LONGITUDE = "Longitude";
    public static String COLSPEC_LONGITUDE = "REAL";

    // result of geocoding lat and long
    public static String COL_LOCATION_DESC = "LocationDesc";
    public static String COLSPEC_LOCATION_DESC = "REAL";

    public static String COL_LENGTH = "Length";
    public static String COLSPEC_LENGTH = "REAL";

    public static String COL_WEIGHT = "Weight";
    public static String COLSPEC_WEIGHT = "REAL";

    public static String COL_THUMBNAIL_PATH = "ThumbnailPath";
    public static String COLSPEC_THUMBNAIL_PATH = "TEXT";

    public static String COL_IMAGE_PATH = "ImagePath";
    public static String COLSPEC_IMAGE_PATH = "TEXT";

    public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + SQL_COL_ID + ", "
            + String.format(COL_FMT, COL_CATCH_ID, COLSPEC_CATCH_ID)
            + String.format(COL_FMT, COL_FISHER_ID, COLSPEC_FISHER_ID)
            + String.format(FK_FMT, COL_FISHER_ID, FishersTable.TABLE_NAME)
            + String.format(COL_FMT, COL_BAIT_ID, COLSPEC_BAIT_ID)
            + String.format(FK_FMT, COL_BAIT_ID, BaitsTable.TABLE_NAME)
            + String.format(COL_FMT, COL_SPECIES_ID, COLSPEC_SPECIES_ID)
            + String.format(FK_FMT, COL_SPECIES_ID, SpeciesTable.TABLE_NAME)
            + String.format(COL_FMT, COL_LATITUDE, COLSPEC_LATITUDE)
            + String.format(COL_FMT, COL_LONGITUDE, COLSPEC_LONGITUDE)
            + String.format(COL_FMT, COL_LENGTH, COLSPEC_LENGTH)
            + String.format(COL_FMT, COL_WEIGHT, COLSPEC_WEIGHT)
            + String.format(COL_FMT, COL_LOCATION_DESC, COLSPEC_LOCATION_DESC)
            + String.format(COL_FMT, COL_THUMBNAIL_PATH, COLSPEC_THUMBNAIL_PATH)
            + String.format(COL_FMT_LAST, COL_IMAGE_PATH, COLSPEC_IMAGE_PATH)
            + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
