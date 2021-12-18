package com.zzx.police.manager;

import android.app.PendingIntent;
import android.telephony.SmsManager;

/**@author Tomy
 * Created by Tomy on 2014-11-24.
 */
public class SMSManager {
    private SmsManager mSMSManager;
    public SMSManager() {
        mSMSManager = SmsManager.getDefault();
    }

    public void sendSMS(String message, String number, PendingIntent sendIntent, PendingIntent rcvIntent) {
        mSMSManager.sendTextMessage(number, null, message, sendIntent, rcvIntent);
    }
}
