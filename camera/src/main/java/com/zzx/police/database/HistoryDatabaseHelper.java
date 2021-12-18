package com.zzx.police.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zzx.police.data.Values;

/**@author Tomy
 * Created by Tomy on 2014/7/9.
 */
public class HistoryDatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    public static final String TABLE_NAME = "HttpUpload";

    public HistoryDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public HistoryDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        this(context, name, factory, VERSION);
    }
    public HistoryDatabaseHelper(Context context, String name) {
        this(context, name, null);
    }

    public HistoryDatabaseHelper(Context context) {
        this(context, Values.DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String pre = "CREATE TABLE ";
        String keyId = "(" + Values.TABLE_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
        String common = Values.TABLE_FILE_TYPE + " INT, " + Values.TABLE_FILE_NAME + " TEXT, " + Values.TABLE_CREATE_DATE + " TEXT, " + Values.TABLE_START_TIME + " INT, " + Values.TABLE_END_TIME + " INT );";
        String table = pre + Values.TABLE_NAME + keyId + common;
        db.execSQL(table);
        String htpCom = Values.TABLE_FILE_TYPE + " INT, " + Values.TABLE_FILE_NAME + " TEXT, " + Values.TABLE_REMOTE_DIR_NAME + " TEXT, "
                + Values.TABLE_FILE_PATH + " TEXT, " + Values.TABLE_CREATE_DATE + " TEXT );";
        String htpTab = pre + TABLE_NAME + keyId + htpCom;
        db.execSQL(htpTab);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
