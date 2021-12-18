package com.zzx.police.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by zdm on 2019/10/29 0029.
 * 功能：
 */
public class SystemValuesUtils {
    private static final int DEFULT_SYSTEM_BATTERY = 0;
    private static final float DEFULT_SYSTEM_LONGITUDE = 0;

    public static int getSystemBattery(Context mContext) {
        if (mContext == null) {
            return 0;
        }
        return Settings.System.getInt(mContext.getContentResolver(), "zzx_system_battery", DEFULT_SYSTEM_BATTERY);
    }


    public static void setSystemBattery(int battery, Context mContext) {
        Settings.System.putInt(mContext.getContentResolver(), "zzx_system_battery", battery);
    }

    public static float getSystemLongitude(Context mContext) {
        if (mContext == null) {
            return 0f;
        }
        return Settings.System.getFloat(mContext.getContentResolver(), "zzx_system_longitude", DEFULT_SYSTEM_LONGITUDE);
    }

    public static void setSystemLongitude(float longitude, Context mContext) {
        Settings.System.putFloat(mContext.getContentResolver(), "zzx_system_longitude", longitude);
    }


    public static float getSystemLatitude(Context mContext) {
        if (mContext == null) {
            return 0f;
        }
        return Settings.System.getFloat(mContext.getContentResolver(), "zzx_system_latitude", DEFULT_SYSTEM_LONGITUDE);
    }


    public static void setSystemLatitude(float latitude, Context mContext) {
        Settings.System.putFloat(mContext.getContentResolver(), "zzx_system_latitude", latitude);
    }


    public static float getSystemAltitude(Context mContext) {
        if (mContext == null) {
            return 0;
        }
        return Settings.System.getFloat(mContext.getContentResolver(), "zzx_system_altitude", DEFULT_SYSTEM_LONGITUDE);
    }


    public static void setSystemAltitude(float altitude, Context mContext) {
        Settings.System.putFloat(mContext.getContentResolver(), "zzx_system_altitude", altitude);
    }


    public static int getSystemSpeed(Context mContext) {
        if (mContext == null) {
            return 0;
        }
        return Settings.System.getInt(mContext.getContentResolver(), "zzx_system_speed", DEFULT_SYSTEM_BATTERY);
    }

    public static void setSystemSpeed(int speed, Context mContext) {
        Settings.System.putInt(mContext.getContentResolver(), "zzx_system_speed", speed);
    }

    public static float getSystemVoltage(Context mContext) {
        if (mContext == null) {
            return 0;
        }
        return Settings.System.getFloat(mContext.getContentResolver(), "zzx_system_voltage", DEFULT_SYSTEM_LONGITUDE);
    }


    public static void setSystemVoltage(float voltage, Context mContext) {
        Settings.System.putFloat(mContext.getContentResolver(), "zzx_system_voltage", voltage);
    }

}
