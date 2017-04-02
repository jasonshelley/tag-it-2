package com.jso.tagit2.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jso.tagit2.models.Catch;

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

    public static String SQL_UPDATE_SELECTION_COUNTS = " UPDATE " + BaitsTable.TABLE_NAME +
            " SET " + BaitsTable.COL_SELECTION_COUNT +
            " = (SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COL_BAIT + "=NEW." + COL_BAIT + ")" +
            " WHERE " + BaitsTable.COL_NAME + " = NEW." + COL_BAIT + ";" +
            " UPDATE " + SpeciesTable.TABLE_NAME +
            " SET " + SpeciesTable.COL_SELECTION_COUNT +
            " = (SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COL_SPECIES + "=NEW." + COL_SPECIES + ")" +
            " WHERE " + SpeciesTable.COL_NAME + " = NEW." + COL_SPECIES + ";" +
            " UPDATE " + FishersTable.TABLE_NAME +
            " SET " + FishersTable.COL_SELECTION_COUNT +
            " = (SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COL_FISHER + "=NEW." + COL_FISHER + ")" +
            " WHERE " + FishersTable.COL_NAME + " = NEW." + COL_FISHER + ";";
    public static String AFTER_INSERT_TRIGGER = "CREATE TRIGGER after_insert_catch AFTER INSERT ON " + TABLE_NAME +
            " BEGIN" +
            SQL_UPDATE_SELECTION_COUNTS +
            " END";

    public static String AFTER_UPDATE_TRIGGER = "CREATE TRIGGER after_update_catch AFTER UPDATE ON " + TABLE_NAME +
            " BEGIN" +
            SQL_UPDATE_SELECTION_COUNTS +
            " END";

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
        db.execSQL(AFTER_INSERT_TRIGGER);
        db.execSQL(AFTER_UPDATE_TRIGGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);

        if (oldVersion < 5) {
            db.execSQL("DROP TABLE " + TABLE_NAME);
            onCreate(db);
        } if (oldVersion < 7) {
            db.execSQL(AFTER_INSERT_TRIGGER);
            db.execSQL(AFTER_UPDATE_TRIGGER);
        }
    }

    // It is assumed the the cursor has been moved to a valid position
    // It is the responsibility of the caller to close the cursor
    public static Catch fromCursor(Cursor cursor) {
        Catch c = new Catch();

        c._id = cursor.getLong(cursor.getColumnIndex(IDatabaseTable.COL_ID));
        c.catchId = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_CATCH_ID));

        c.isSynced = cursor.getInt(cursor.getColumnIndex(IDatabaseTable.COL_IS_SYNCED)) == 1;
        c.lastModified = cursor.getLong(cursor.getColumnIndex(IDatabaseTable.COL_LAST_MODIFIED));

        c.fisher = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_FISHER));
        c.bait = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_BAIT));
        c.species = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_SPECIES));

        c.latitude = cursor.getDouble(cursor.getColumnIndex(CatchesTable.COL_LATITUDE));
        c.longitude = cursor.getDouble(cursor.getColumnIndex(CatchesTable.COL_LONGITUDE));

        c.length = cursor.getDouble(cursor.getColumnIndex(CatchesTable.COL_LENGTH));
        c.weight = cursor.getDouble(cursor.getColumnIndex(CatchesTable.COL_WEIGHT));

        c.locationDescription = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_LOCATION_DESC));

        c.imagePath = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_IMAGE_PATH));
        c.thumbnailPath = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_THUMBNAIL_PATH));

        c.timestamp = cursor.getLong(cursor.getColumnIndex(CatchesTable.COL_TIMESTAMP));

        return c;
    }

    public static ContentValues getContentValues(Catch c) {
        ContentValues values = new ContentValues();
        values.put(CatchesTable.COL_CATCH_ID, c.catchId);
        values.put(CatchesTable.COL_IS_SYNCED, c.isSynced);
        values.put(CatchesTable.COL_LAST_MODIFIED, c.lastModified);
        values.put(CatchesTable.COL_FISHER, c.fisher);
        values.put(CatchesTable.COL_BAIT, c.bait);
        values.put(CatchesTable.COL_SPECIES, c.species);
        values.put(CatchesTable.COL_LATITUDE, c.latitude);
        values.put(CatchesTable.COL_LONGITUDE, c.longitude);
        values.put(CatchesTable.COL_LENGTH, c.length);
        values.put(CatchesTable.COL_WEIGHT, c.weight);
        values.put(CatchesTable.COL_LOCATION_DESC, c.locationDescription);
        values.put(CatchesTable.COL_IMAGE_PATH, c.imagePath);
        values.put(CatchesTable.COL_THUMBNAIL_PATH, c.thumbnailPath);
        values.put(CatchesTable.COL_TIMESTAMP, c.timestamp);

        return values;
    }
}
