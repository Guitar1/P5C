package com.zzx.phone.data;

import android.util.Log;

/**@author Tomy
 * Created by Tomy on 2014/6/23.
 */
public class Values {

    /** 中国联通运营商 **/
    public static final String CHN_UNICOM_CARRIER_NAME_1 = "CHN-CUGSM";
    public static final String CHN_UNICOM_CARRIER_NAME_2 = "CHN-UNICOM";
    public static final String CHN_UNICOM_CARRIER_NAME_3 = "中国联通";
    public static final String CHN_UNICOM_CARRIER_NAME_4 = "China Unicom";

    /** 中国移动运营商 **/
    public static final String CHN_MOBILE_CARRIER_NAME_1 = "CHINA MOBILE";
    public static final String CHN_MOBILE_CARRIER_NAME_2 = "中国移动";

    /** 中国电信运营商 **/
    public static final String CHN_CDMA_CARRIER_NAME_1 = "EVDO";
    public static final String CHN_CDMA_CARRIER_NAME_2 = "CDMA";
    public static final String CHN_CDMA_CARRIER_NAME_3 = "中国电信";
    public static final String CHN_CDMA_CARRIER_NAME_4 = "CHINA TELECOM";

    /** Phone Event Broadcast **/
    /** 已挂断 **/
    public static final String PHONE_DISCONNECT = "PHONE_DISCONNECT";
    public static final String PHONE_DISCONNECTING = "PHONE_DISCONNECTING";
    /** 正在拨号 **/
    public static final String PHONE_INCOME_RINGING = "PHONE_INCOMING_RING";
    /** 已接通 **/
    public static final String PHONE_CONNECTED = "PHONE_CONNECTED";
    /** 有来电接入 **/
    public static final String PHONE_NEW_INCOME = "PHONE_NEW_RINGING_CONNECTION";
    private static final String TAG = "Phone";
    private static boolean DEBUG = true;

    public static void LOGE(String tag, Object msg) {
        if (DEBUG)
            Log.e(TAG, tag + msg);
    }
    public static void LOGD(String tag, Object msg) {
        if (DEBUG)
            Log.d(TAG, tag + msg);
    }
    public static void LOGI(String tag, Object msg) {
        if (DEBUG)
            Log.i(TAG, tag + msg);
    }
    public static void LOGV(String tag, Object msg) {
        if (DEBUG)
            Log.v(TAG, tag + msg);
    }
}
