package com.zzx.police.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


/**@author Tomy
 * Created by Tomy on 2015-04-20.
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        SmsUtils utils = new SmsUtils(context);
        Bundle bundle = intent.getExtras();
        abortBroadcast();
        SmsMessage[] smsMessages = null;
        Object[] pdus = null;
        if (bundle != null) {
            pdus = (Object[]) bundle.get("pdus");
        }
        if (pdus != null) {
            smsMessages = new SmsMessage[pdus.length];
            for (int i = 0; i < smsMessages.length; i++) {
                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
        }
        for (SmsMessage message : smsMessages) {
            /*String msg = message.getDisplayMessageBody();
            String emailBody = message.getEmailBody();
            String msgBody = message.getMessageBody();
            String emailFrom = message.getEmailFrom();
            int status = message.getStatus();
            String address = message.getOriginatingAddress();
            int icc = message.getIndexOnIcc();
            int identifier = message.getProtocolIdentifier();
            String subject = message.getPseudoSubject();
            String userData = new String(message.getUserData());
            int sim = message.getIndexOnSim();*/
//            utils.insertSmsMessage(message);
            Log.i("SmsReceiver", message.getDisplayMessageBody());
        }
    }
}
