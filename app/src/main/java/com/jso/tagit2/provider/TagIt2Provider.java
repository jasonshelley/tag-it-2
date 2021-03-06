package com.jso.tagit2.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jso.tagit2.database.BaitsTable;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.FishersTable;
import com.jso.tagit2.database.IDatabaseTable;
import com.jso.tagit2.database.SpeciesTable;
import com.jso.tagit2.database.TagIt2DatabaseHelper;
import com.jso.tagit2.database.UsersTable;

import java.util.List;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class TagIt2Provider extends ContentProvider {

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private TagIt2DatabaseHelper dbHelper;

    private static final int TABLE_MASK         = 0xf0;
    private static final int BY_ID_MASK         = 0x01;

    private static final int BAITS              = 0x10;
    private static final int BAIT_BY_ID         = BAITS | BY_ID_MASK;

    private static final int CATCHES            = 0x20;
    private static final int CATCH_BY_ID        = CATCHES | BY_ID_MASK;

    private static final int FISHERS            = 0x30;
    private static final int FISHER_BY_ID       = FISHERS | BY_ID_MASK;

    private static final int SPECIES            = 0x40;
    private static final int SPECIES_BY_ID      = SPECIES | BY_ID_MASK;

    private static final int USERS              = 0x50;
    private static final int USER_BY_ID         = USERS | BY_ID_MASK;

    static {
        uriMatcher.addURI(Contract.AUTHORITY, UsersTable.TABLE_NAME, USERS);

        uriMatcher.addURI(Contract.AUTHORITY, CatchesTable.TABLE_NAME, CATCHES);
        uriMatcher.addURI(Contract.AUTHORITY, CatchesTable.TABLE_NAME + "/#", CATCH_BY_ID);

        uriMatcher.addURI(Contract.AUTHORITY, BaitsTable.TABLE_NAME, BAITS);
        uriMatcher.addURI(Contract.AUTHORITY, BaitsTable.TABLE_NAME + "/#", BAIT_BY_ID);

        uriMatcher.addURI(Contract.AUTHORITY, FishersTable.TABLE_NAME, FISHERS);
        uriMatcher.addURI(Contract.AUTHORITY, FishersTable.TABLE_NAME + "/#", FISHER_BY_ID);

        uriMatcher.addURI(Contract.AUTHORITY, SpeciesTable.TABLE_NAME, SPECIES);
        uriMatcher.addURI(Contract.AUTHORITY, SpeciesTable.TABLE_NAME + "/#", SPECIES_BY_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new TagIt2DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder bob = new SQLiteQueryBuilder();

        int uriMatch = uriMatcher.match(uri);
        List<String> pathSegments = uri.getPathSegments();
        String tableName = "";
        String id = "";

        if ((uriMatch & BY_ID_MASK) != 0) {  // we have an id at the end of the uri
            id = uri.getLastPathSegment();
            tableName = pathSegments.get(pathSegments.size() - 2);
        } else
            tableName = uri.getLastPathSegment();

        bob.setTables(tableName);

        // set selection
        if ((uriMatch & BY_ID_MASK) != 0) {
            selection = tableName + "." + IDatabaseTable.COL_ID + "=" + id;
            selectionArgs = null;
        }

        Cursor c = bob.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return getTableName(uri);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        if (tableName == null)
            return Uri.EMPTY;

        long id = db.insert(tableName, null, values);

        Uri resultUri = Uri.parse("content://" + Contract.AUTHORITY + "/" + tableName + "/" + id);
//        getContext().getContentResolver().notifyChange(Contract.CATCHES_URI, null);
        getContext().getContentResolver().notifyChange(resultUri, null);

        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        if (tableName == null)
            return -1;


//        getContext().getContentResolver().notifyChange(Contract.CATCHES_URI, null);
        getContext().getContentResolver().notifyChange(uri, null);

        return db.delete(tableName, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int uriMatch = uriMatcher.match(uri);
        List<String> pathSegments = uri.getPathSegments();
        String tableName = "";
        String id = "";

        if ((uriMatch & BY_ID_MASK) != 0) {  // we have an id at the end of the uri
            id = uri.getLastPathSegment();
            tableName = pathSegments.get(pathSegments.size() - 2);
        } else
            tableName = uri.getLastPathSegment();

        // set selection
        if ((uriMatch & BY_ID_MASK) != 0) {
            selection = IDatabaseTable.COL_ID + "=" + id;
            selectionArgs = null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return db.update(tableName, values, selection, selectionArgs);
    }

    private String getTableName(Uri uri)
    {
        int uriMatch = uriMatcher.match(uri);

        switch (uriMatch & TABLE_MASK) {
            case BAITS:
                return BaitsTable.TABLE_NAME;

            case CATCHES:
                return CatchesTable.TABLE_NAME;

            case FISHERS:
                return FishersTable.TABLE_NAME;

            case SPECIES:
                return SpeciesTable.TABLE_NAME;

            case USERS:
                return UsersTable.TABLE_NAME;

            default:
                return null;
        }

    }

    public static class Contract {
        public final static String AUTHORITY = "com.jso.tagit2.provider";
        public final static Uri CATCHES_URI = Uri.parse("content://" + AUTHORITY + "/" + CatchesTable.TABLE_NAME);  // a view on the catches table
        public final static Uri BAITS_URI = Uri.parse("content://" + AUTHORITY + "/" + BaitsTable.TABLE_NAME);  // a view on the catches table
        public final static Uri FISHERS_URI = Uri.parse("content://" + AUTHORITY + "/" + FishersTable.TABLE_NAME);  // a view on the catches table
        public final static Uri SPECIES_URI = Uri.parse("content://" + AUTHORITY + "/" + SpeciesTable.TABLE_NAME);  // a view on the catches table

        public final static String CATCHES_DEFAULT_SORTORDER = CatchesTable.COL_TIMESTAMP + " ASC";

        public final static String[] DEFAULT_PROJECTION = new String [] { "*" };

        public final static String[] BAIT_PROJECTION = new String[] {
            BaitsTable.COL_ID,
                BaitsTable.COL_BAIT_ID,
                BaitsTable.COL_NAME
        };

        public final static String[] FISHER_PROJECTION = new String[] {
                FishersTable.COL_ID,
                FishersTable.COL_FISHER_ID,
                FishersTable.COL_NAME
        };

        public final static String[] SPECIES_PROJECTION = new String[] {
                SpeciesTable.COL_ID,
                SpeciesTable.COL_SPECIES_ID,
                SpeciesTable.COL_NAME
        };

        public final static String[] CATCHES_PROJECTION = new String[] {
                CatchesTable.COL_ID,
                CatchesTable.COL_IS_SYNCED,
                CatchesTable.COL_LAST_MODIFIED,
                CatchesTable.COL_CATCH_ID,
                CatchesTable.COL_FISHER,
                CatchesTable.COL_BAIT,
                CatchesTable.COL_SPECIES,
                CatchesTable.COL_LATITUDE,
                CatchesTable.COL_LONGITUDE,
                CatchesTable.COL_LOCATION_DESC,
                CatchesTable.COL_LENGTH,
                CatchesTable.COL_WEIGHT,
                CatchesTable.COL_THUMBNAIL_PATH,
                CatchesTable.COL_IMAGE_PATH,
                CatchesTable.COL_TIMESTAMP,

        };
    }
}
