package com.zzx.phone.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.zzx.phone.R;
import com.zzx.phone.data.Values;

/**@author Tomy
 * Created by Tomy on 2014/6/24.
 */
public class SystemUtils {
    private static final String TAG = "SystemUtils:";

    public static boolean checkSIMCardState(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        int state = manager.getSimState();
        if (state == TelephonyManager.SIM_STATE_UNKNOWN
                || state == TelephonyManager.SIM_STATE_ABSENT) {
            Values.LOGE(TAG, context.getString(R.string.sim_not_work));
            return false;
        }
        return true;
    }
}
