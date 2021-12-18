package com.zzx.police.command;

import android.text.format.DateFormat;

import com.zzx.police.MainActivity;
import com.zzx.police.data.Values;
import com.zzx.police.utils.GpsConverter;
import com.zzx.police.utils.SystemUtil;

/**@author Tomy
 * Created by Tomy on 14-3-18.
 */
public class VehicleStatus {
    private static final String TAG = "VehicleStatus: ";
    private final String COMMA = ",";
    public static final String GPS_OK = "A";
    public static final String GPS_NO = "N";
    public static final String GPS_V  = "V";
    public static final double DEFAULT_LONG = 113.938985;
    public static final double DEFAULT_LATI = 22.563168;

    public VehicleStatus() {
        mBattery = String.valueOf(MainActivity.battery);
        mStorageTotal   = String.format("%.2f", SystemUtil.getExternalStorageSize(false));
        mStorageFree    = String.format("%.2f", SystemUtil.getExternalStorageSize(true));
        Values.LOG_I(TAG, "mBattery = " + mBattery + "; mStorageTotal = " + mStorageTotal + "; mStorageFree = " + mStorageFree);
    }

    private CharSequence mFormat = "yyMMdd kkmmss";
    public String mGpsStatus    = GPS_NO;
    public double mLongitude    = GpsConverter.convertToDegree(VehicleStatus.DEFAULT_LONG);
    public double mLatitude     = GpsConverter.convertToDegree(VehicleStatus.DEFAULT_LATI);
    public float mSpeed = 0;
    public float mDirection = 0;
    public String mVehicleStatus = "0000000000000000";
    public String mVehicleStatusMask = "0000000000000000";
    public String mStorageTotal = "";
    public String mStorageFree  = "";
    public String mBattery      = "50";
    public String mRouteNum     = "";
    public String mDriverNum    = "0";
    public String mNextNum      = "0";
    public String mCarStatus    = "1";
    public String mPeopleNum    = "0";

    private String format6(double value) {
        return String.format("%.4f", value);
    }
    private String format2(float value) {
        return String.format("%.2f", value);
    }

    public String getTime() {
        long time = System.currentTimeMillis();
        return String.valueOf(DateFormat.format(mFormat, time));
    }
    public String getValue() {
        return getTime() + COMMA + mGpsStatus + COMMA + format6(mLongitude) + COMMA + format6(mLatitude) + COMMA
                + format2(mSpeed) + COMMA + format2(mDirection) + COMMA
                + mVehicleStatus + COMMA + mVehicleStatusMask + COMMA + mStorageTotal + COMMA
                + mStorageFree + COMMA + mBattery + COMMA + mRouteNum + COMMA + mDriverNum + COMMA
                + mNextNum + COMMA + mCarStatus + COMMA + mPeopleNum + COMMA;
    }
}
