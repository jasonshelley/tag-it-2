package com.jso.tagit2.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JSHELLEY on 6/03/2017.
 */

public interface IDatabaseTable {
    public static String COL_ID = "_id";
    public static String SQL_COL_ID = COL_ID + " integer primary key autoincrement";

    public final static String COL_FMT = "%1$s %2$s, ";
    public final static String COL_FMT_LAST = "%1$s %2$s";
    public final static String FK_FMT = "FOREIGN KEY(%1$s) REFERENCES %2$s(%1$s), ";
    public final static String FK_FMT_LAST = "FOREIGN KEY(%1$s) REFERENCES %2$s(%1$s)";

    void onCreate(SQLiteDatabase db);
    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
