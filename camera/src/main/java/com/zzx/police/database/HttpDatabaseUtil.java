package com.zzx.police.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zzx.police.data.FileInfo;
import com.zzx.police.data.Values;

import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2015/8/4.
 */
public class HttpDatabaseUtil {
    public static final String TAG = "DatabaseUtils: ";

    public static List<FileInfo> queryHistory(Context context) {
        SQLiteDatabase database = null;
        List<FileInfo> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            HistoryDatabaseHelper helper = new HistoryDatabaseHelper(context);
            database = helper.getReadableDatabase();
            String querySql = "select * from " + HistoryDatabaseHelper.TABLE_NAME;
            Values.LOG_I(TAG, "querySql = " + querySql);
            cursor = database.query(HistoryDatabaseHelper.TABLE_NAME, null, null, null, null, null, null);
            if (cursor == null) {
                Values.LOG_I(TAG, "cursor = null");
                return null;
            }
            cursor.moveToFirst();
            int fileNameIndex   = cursor.getColumnIndexOrThrow(Values.TABLE_FILE_NAME);
            int filePathIndex   = cursor.getColumnIndexOrThrow(Values.TABLE_FILE_PATH);
            int fileTypeIndex   = cursor.getColumnIndexOrThrow(Values.TABLE_FILE_TYPE);
            int createTimeIndex   = cursor.getColumnIndexOrThrow(Values.TABLE_CREATE_DATE);
            int dirNameIndex   = cursor.getColumnIndexOrThrow(Values.TABLE_REMOTE_DIR_NAME);
            Values.LOG_I(TAG, "index = " + fileNameIndex + dirNameIndex + "; count = " + cursor.getCount());
            while (!cursor.isAfterLast()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.mFileName  = cursor.getString(fileNameIndex);
                fileInfo.mFileType  = cursor.getInt(fileTypeIndex);
                fileInfo.mCreateDate  = cursor.getString(createTimeIndex);
                fileInfo.mDirName  = cursor.getString(dirNameIndex);
                fileInfo.mFilePath  = cursor.getString(filePathIndex);
                list.add(fileInfo);
                Values.LOG_I(TAG, "fileInfo = " + fileInfo);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
            if (database != null)
                database.close();

        }
        return list;
    }

    public static boolean insertHistory(Context context, FileInfo info) {
        SQLiteDatabase database = null;
        try {
            HistoryDatabaseHelper helper = new HistoryDatabaseHelper(context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Values.TABLE_FILE_NAME, info.mFileName);
            values.put(Values.TABLE_REMOTE_DIR_NAME, info.mDirName);
            values.put(Values.TABLE_FILE_TYPE, info.mFileType);
            values.put(Values.TABLE_CREATE_DATE, info.mCreateDate);
            values.put(Values.TABLE_FILE_PATH, info.mFilePath);
            database.insertOrThrow(HistoryDatabaseHelper.TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (database != null)
                database.close();
        }
        return true;
    }

    public static boolean insertHistory(Context context, List<FileInfo> infoList) {
        if (context == null || infoList == null || infoList.size() <= 0) {
            return true;
        }
        SQLiteDatabase database = null;
        try {
            HistoryDatabaseHelper helper = new HistoryDatabaseHelper(context);
            database = helper.getWritableDatabase();
            for (FileInfo info : infoList) {
                ContentValues values = new ContentValues();
                values.put(Values.TABLE_FILE_NAME, info.mFileName);
                values.put(Values.TABLE_REMOTE_DIR_NAME, info.mDirName);
                values.put(Values.TABLE_FILE_TYPE, info.mFileType);
                values.put(Values.TABLE_CREATE_DATE, info.mCreateDate);
                database.insertOrThrow(HistoryDatabaseHelper.TABLE_NAME, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (database != null)
                database.close();
        }
        return true;
    }

    public static boolean deleteHistory(Context context, String fileName, String dirName) {
        Values.LOG_I(TAG, "deleteList = " + fileName);
        SQLiteDatabase database = null;
        try {
            HistoryDatabaseHelper helper = new HistoryDatabaseHelper(context);
            database = helper.getWritableDatabase();
            String deleteSql = "delete from " + HistoryDatabaseHelper.TABLE_NAME + " where " + Values.TABLE_FILE_NAME + " = ? and "
                    + Values.TABLE_REMOTE_DIR_NAME + " = ?";
            database.execSQL(deleteSql, new String[] {fileName, dirName});
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (database != null)
                database.close();
        }
        return true;
    }
}
