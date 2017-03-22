package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by jshelley on 22/03/2017.
 */

public abstract class BaseDatabaseTable implements IDatabaseTable {
    @Override
    public void onCreate(SQLiteDatabase db) {
        // do nothing
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("alter table " + getTableName() + " add " + COLSPEC_IS_SYNCED);
            db.execSQL("alter table " + getTableName() + " add " + COLSPEC_LAST_MODIFIED);
        }
    }

    protected abstract String getTableName();
}
