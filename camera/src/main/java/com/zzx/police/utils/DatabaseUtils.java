package com.zzx.police.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zzx.police.data.FileInfo;
import com.zzx.police.data.Values;
import com.zzx.police.database.HistoryDatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2014/7/9.
 */
public class DatabaseUtils {

    public static final String TAG = "DatabaseUtils: ";

    public static List<FileInfo> queryHistory(Context context, String type, String createDate, String startTime, String endTime, String ftpDirName) {
        Values.LOG_I(TAG, "type = " + type + "; createDate = " + createDate + "; startTime = " + startTime + "; endTime = " + endTime);
        SQLiteDatabase database = null;
        List<FileInfo> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            HistoryDatabaseHelper helper = new HistoryDatabaseHelper(context);
            database = helper.getReadableDatabase();
            String querySql = "select * from " + Values.TABLE_NAME + " where type = ? and createDate = ? and startTime between ? and ?";
            Values.LOG_I(TAG, "querySql = " + querySql);
            cursor = database.rawQuery(querySql, new String[]{type, createDate, startTime, endTime});
            if (cursor == null) {
                Values.LOG_I(TAG, "cursor = null");
                return null;
            }
            cursor.moveToFirst();
            int fileTypeIndex   = cursor.getColumnIndexOrThrow(Values.TABLE_FILE_TYPE);
            int fileNameIndex   = cursor.getColumnIndexOrThrow(Values.TABLE_FILE_NAME);
            int createDateIndex = cursor.getColumnIndexOrThrow(Values.TABLE_CREATE_DATE);
            int startTimeIndex  = cursor.getColumnIndexOrThrow(Values.TABLE_START_TIME);
            int endTimeIndex    = cursor.getColumnIndexOrThrow(Values.TABLE_END_TIME);
            Values.LOG_I(TAG, "index = " + fileTypeIndex + fileNameIndex + createDateIndex + startTimeIndex + endTimeIndex + "; count = " + cursor.getCount());
            while (!cursor.isAfterLast()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.mFileType  = cursor.getInt(fileTypeIndex);
                fileInfo.mFileName  = cursor.getString(fileNameIndex);
                fileInfo.mCreateDate    = cursor.getString(createDateIndex);
                fileInfo.mStartTime = cursor.getLong(startTimeIndex);
                fileInfo.mEndTime   = cursor.getLong(endTimeIndex);
                if (ftpDirName != null) {
                    fileInfo.mDirName = ftpDirName;
                }
                list.add(fileInfo);
                Values.LOG_I(TAG, "fileInfo = " + fileInfo);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Values.LOG_E(TAG, "err = " + e.getMessage());
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
            values.put(Values.TABLE_FILE_TYPE, info.mFileType);
            values.put(Values.TABLE_CREATE_DATE, info.mCreateDate);
            values.put(Values.TABLE_START_TIME, info.mStartTime);
            values.put(Values.TABLE_END_TIME, info.mEndTime);
            database.insertOrThrow(Values.TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            Values.LOG_E(TAG, "err = " + e.getMessage());
            return false;
        } finally {
            if (database != null)
                database.close();
        }
        return true;
    }

    public static boolean deleteHistory(Context context, String[] list) {
        Values.LOG_I(TAG, "deleteList = " + Arrays.toString(list));
        SQLiteDatabase database = null;
        try {
            HistoryDatabaseHelper helper = new HistoryDatabaseHelper(context);
            database = helper.getWritableDatabase();
            for (String fileName : list) {
                database.delete(Values.TABLE_NAME, Values.TABLE_FILE_NAME + " = ?", new String[]{fileName});
            }
        } catch (Exception e) {
            e.printStackTrace();
            Values.LOG_E(TAG, "err = " + e.getMessage());
            return false;
        } finally {
            if (database != null)
                database.close();
        }
        return true;
    }
}
