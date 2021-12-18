package com.zzx.police.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zzx.police.data.Values;

import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2014-11-17.
 */
public class SOSInfoUtils {
//    public static final String SOS_URL  = "content://com.zzx.setting/sos_telephone";
    public static final String SOS_URL  = "content://myproject.zzx.myset/sos_telephone";
    public static final String SOS_TELEPHONE_CONTACT ="sos_telephone_name";
    public static final String SOS_TELEPHONE_NUM = "sos_telephone_num";
    public static final String SOS_ID = "sos_id";


    public static List<String> getSOSNumList(Context context) {
        List<String> list = new ArrayList<>();
        ContentResolver mResolver = context.getContentResolver();
        Cursor cursor;
        try {
            cursor = mResolver.query(Uri.parse(SOS_URL), null, null, null, SOS_ID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (cursor == null || cursor.isAfterLast()) {
            return null;
        }
        cursor.moveToFirst();
        Values.LOG_W("zzx", "cursor.count = " + cursor.getCount());
        while (!cursor.isAfterLast()) {
            String num = cursor.getString(cursor.getColumnIndexOrThrow(SOS_TELEPHONE_NUM));
            list.add(num);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }
}
