package com.zzx.soundrecord.util;

import android.content.Context;
import android.provider.Settings;

import com.zzx.soundrecord.values.Values;

public class SystemUtil {
    public static String getDeviceNum(Context context) {
        String deviceNum = null;
        deviceNum = Settings.System.getString(context.getContentResolver(),
                Values.DEVICE_NUMBER);
        if (deviceNum == null || deviceNum.equals("")) {
            deviceNum = Values.DEVICE_DEFAULT_NUM;
        }
        return deviceNum;
    }

    public static String getDeviceNumber(Context context) {
        String deviceNumber = null;
        deviceNumber = Settings.System.getString(context.getContentResolver(),
                Values.DEVICE_NUM);
        if (deviceNumber == null || deviceNumber.equals(""))
            deviceNumber = Values.DEVICE_DEFAULT_NUM_4;
        return deviceNumber;
    }

    public static String getPoliceNumber(Context context) {
        String policeNumber = null;
        policeNumber = Settings.System.getString(context.getContentResolver(),
                Values.POLICE_NUMBER);
        if (policeNumber == null || policeNumber.equals("")) {
            policeNumber = Values.DEFAULT_NUMBER;
        }
        return policeNumber;
    }
}
