package com.jso.tagit2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public class TagIt2DatabaseHelper extends SQLiteOpenHelper {

    public final static String URI_BASE = "com.jso.tagit2.provider";

    public final static String DB_NAME = "tagit2.db";
    public final static int VERSION = 1;

    public final static IDatabaseTable[] _tables = new IDatabaseTable[] { new UsersTable(),
                                                                        new FishersTable(),
                                                                        new BaitsTable(),
                                                                        new SpeciesTable(),
                                                                        new CatchesTable()};

    public TagIt2DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (IDatabaseTable table : _tables)
            table.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (IDatabaseTable table : _tables)
            table.onUpgrade(db, oldVersion, newVersion);
    }
}
