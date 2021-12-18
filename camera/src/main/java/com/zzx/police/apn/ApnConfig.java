package com.zzx.police.apn;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/22 0022.
 */
public class ApnConfig {

    private static final String TAG = ApnConfig.class.getName();

    private Context mcontext;

    public ApnConfig(Context context) {
        mcontext = context;
    }

    /**
     * content://telephony/carriers                  //取得全部apn列表
     * content://telephony/carriers/preferapn        //取得当前设置的apn
     * content://telephony/carriers/current          //取得current=1的apn列表
     */

    private static final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static final Uri CURRENT_APN_URI = Uri.parse("content://telephony/carriers/current");

    public Map<String, ApnInfo> GetAllApn() {
        Map<String, ApnInfo> map = new HashMap<>();
        ContentResolver resolver = mcontext.getContentResolver();
        Cursor cr = resolver.query(APN_TABLE_URI, null, null, null, null);
        while (cr != null && cr.moveToNext()) {
            ApnInfo info = new ApnInfo();
            info.setId(cr.getString(cr.getColumnIndex(ApnInfo.APN_ID)));
            info.setName(cr.getString(cr.getColumnIndex(ApnInfo.APN_NAME)));
            info.setApn(cr.getString(cr.getColumnIndex(ApnInfo.APN_APN)));
            info.setMcc(cr.getString(cr.getColumnIndex(ApnInfo.APN_MCC)));
            info.setMnc(cr.getString(cr.getColumnIndex(ApnInfo.APN_MNC)));
            info.setCurrent(cr.getString(cr.getColumnIndex(ApnInfo.APN_CURRENT)));
            info.setType(cr.getString(cr.getColumnIndex(ApnInfo.APN_TYPE)));
            info.setUser(cr.getString(cr.getColumnIndex(ApnInfo.APN_USER)));
            info.setAuthenticationtype(cr.getInt(cr.getColumnIndex(ApnInfo.APN_AUTHENTICATION_TYPE)));
            info.setPassword(cr.getString(cr.getColumnIndex(ApnInfo.APN_PASSWORD)));
            map.put(info.getName(), info);
            Log.d(TAG, info.getId() + " " + info.getName() + " " + info.getApn() + " " + info.getMcc() + " " + info.getMnc() + " " +
                            info.getNumeric() + " " + info.getCurrent() + " " + info.getType() + " " + info.getUser() + " " + info.getPassword() +
                    " " + info.getAuthenticationtype());
        }
        if (cr != null && !cr.isClosed()) {
            cr.close();
        }
        return map;
    }

    public void SetNowApn(int id) {
        ContentResolver resolver = mcontext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", id);
        int i = resolver.update(PREFERRED_APN_URI, values, null, null);
        Log.d(TAG, "set apn " + (i == 1 ? "success" : "failure"));
    }

    public void InsertApn(ApnInfo apnInfo) {
        ContentResolver resolver = mcontext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ApnInfo.APN_NAME, apnInfo.getName());
        values.put(ApnInfo.APN_APN, apnInfo.getApn());
        values.put(ApnInfo.APN_MCC, apnInfo.getMcc());
        values.put(ApnInfo.APN_MNC, apnInfo.getMnc());
        values.put(ApnInfo.APN_NUMERIC, apnInfo.getNumeric());
        values.put(ApnInfo.APN_CURRENT, apnInfo.getCurrent());
        values.put(ApnInfo.APN_TYPE, apnInfo.getType());
        values.put(ApnInfo.APN_PROTOCOL, apnInfo.getProtocol());
        values.put(ApnInfo.APN_ROAMING, apnInfo.getRoaming());
        //   if(apnInfo.getUser()!=null){
        values.put(ApnInfo.APN_USER, apnInfo.getUser());
        values.put(ApnInfo.APN_AUTHENTICATION_TYPE, apnInfo.getAuthenticationtype());
        values.put(ApnInfo.APN_PASSWORD, apnInfo.getPassword());
        //}


        Uri uri = resolver.insert(APN_TABLE_URI, values);
        apnInfo.setId(uri.getPath().split("/", 3)[2]);
        Log.d(TAG, "insert " + uri.getPath());
    }

    public void deleteApn(int id) {
        ContentResolver resolver = mcontext.getContentResolver();
        int n = resolver.delete(APN_TABLE_URI, ApnInfo.APN_ID + "=?", new String[]{id + ""});
        Log.d(TAG, "delete " + (n == 1 ? "success" : "failure"));
    }

    public void deleteApnbyName(String name) {
        Iterator<ApnInfo> iterator = GetAllApn().values().iterator();
        while (iterator.hasNext()) {
            ApnInfo info = iterator.next();
            if (name.equals(info.getName())) {
                deleteApn(Integer.parseInt(info.getId()));
            }
        }
    }

    public void setApnUse(ApnInfo info) {
        info.setId("0");
        Iterator<ApnInfo> iterator = this.GetAllApn().values().iterator();
        while (iterator.hasNext()) {
            ApnInfo apnInfo = iterator.next();
            if (info.isSame(apnInfo)) {
                info = apnInfo;
            }
        }
        if (info.getId().equals("0")) {
            InsertApn(info);
        }
        SetNowApn(Integer.parseInt(info.getId()));
    }

}
