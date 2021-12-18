package com.zzx.phone.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zzx.phone.data.Values;
import com.zzx.phone.manager.PhoneManager;
import com.zzx.phone.utils.EventBusUtils;
import com.zzx.phone.utils.SystemUtils;


/**
 *@author Tomy
 * 描述： 监听电话状态
 * 2014-2-28
 */
public class PhoneStateReceiver extends BroadcastReceiver {

	public static final String TAG = "PhoneView";
    private PhoneManager mPhoneManager  = null;
	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		Log.i(TAG, "action=" + action);
		if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {// 去电
			Log.i(TAG, "去电");
            if (!SystemUtils.checkSIMCardState(context)) {
                return;
            }
            String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (mPhoneManager == null) {
                mPhoneManager = new PhoneManager(context);
            }
            mPhoneManager.setInteractivePhoneNumber(phoneNumber);
            EventBusUtils.postEvent(TelephonyManager.ACTION_PHONE_STATE_CHANGED, PhoneManager.PhoneState.OUT_GOING);
		} else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {// 處理來電
            Log.i(TAG, TelephonyManager.ACTION_PHONE_STATE_CHANGED);
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
            switch (tm.getCallState()) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(TAG, "挂断");
                    EventBusUtils.postEvent(TelephonyManager.ACTION_PHONE_STATE_CHANGED, PhoneManager.PhoneState.IDLE);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Values.LOGI(TAG, "接听");
                    EventBusUtils.postEvent(TelephonyManager.ACTION_PHONE_STATE_CHANGED, PhoneManager.PhoneState.ACTIVE);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i(TAG, "来电");
                    String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    if (mPhoneManager == null) {
                        mPhoneManager = new PhoneManager(context);
                    }
                    mPhoneManager.setInteractivePhoneNumber(phoneNumber);
                    EventBusUtils.postEvent(TelephonyManager.ACTION_PHONE_STATE_CHANGED, PhoneManager.PhoneState.RINGING);
                    break;
            }
		}
	}

}
