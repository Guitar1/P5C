package com.zzx.police.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.zzx.police.R;
import com.zzx.police.utils.DeviceUtils;
import com.zzx.police.utils.SOSInfoUtils;

import java.util.List;


public class SOSService extends Service {

    private static final int SMS_FAIL_SEND_TIMES = 3;
    private ServiceState mServiceState = null;
    private SignalStrength mSignalStrength = null;
    private TelephonyManager mManager = null;
    private SmsManager mSmsManager = null;
    private PendingIntent mSOSSendPI;
    private PendingIntent mBootRcvPI;
    private PendingIntent mIMEISendPI;
    private PendingIntent mIMEIRcvPI;

    private SmsSendBroadCastReceive mSendReceive;
    private ContentResolver mResolver;
    private Handler mHandler;

    public static final String ACTION_SOS_SMS_SEND = "Action_SMS_Send";
    public static final String ACTION_SMS_RECEIVED = "Action_SMS_Rcv";

    /** 发送中心下发短信 **/
    public static final String ACTION_CENTER_MSG    = "ActionCenterMsg";
    public static final String EXTRA_CENTER_MSG     = "CenterMsg";
    /** 开机启动发送 **/
    private SendSmsRunnable mSOSSend;
    /** Sim卡变化发送 **/
    private SendSmsRunnable mSimChangeSend;
    /** Sim卡工作一小时发送 **/
    private SendSmsRunnable mSimOneSend;
    private final static String TAG = "AlarmService: ";
    private PhoneSignalStateListener mListener;
    public static final String SEND_CENTER_NUMBER       = "18098914618";
    /** 发送SOS短信 **/
    public static final String ACTION_SOS_MSG = "ActionSOSMsg";
    /** 检测运行一小时 **/
    public static final String ACTION_CHECK_ONE_HOUR    = "ActionCheckOne";
    /** Sim卡更换 **/
    public static final String ACTION_SIM_CHANGED       = "ActionSimChanged";
    public static final String SIM_ONE_HOUR = "SimOneHour";
    public static final int SIM_ONE_HOUR_NOT_SEND   = -1;
    public static final int SIM_ONE_HOUR_FINISH_SEND = 0;
    public static final String SIM_NUMBER = "simNumber";

    public static final String ACTION_BOOT_SMS_RECEIVED = "Action_Boot_Rcv";
    public static final String ACTION_IMEI_SMS_SEND     = "Action_Imei_Send";
    public static final String ACTION_IMEI_SMS_RECEIVED = "Action_Imei_Rcv";
    private boolean mNeedCheck = false;
    private int mTime = 0;
    private boolean mSimOneStart = false;
    private boolean mSimChanged = false;
    private int mNeedSendCount = 0;
    private String mOldNumber;
    private boolean mHasSend = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mListener = new PhoneSignalStateListener();
        mManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mManager.listen(mListener,
                PhoneStateListener.LISTEN_SERVICE_STATE | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        mResolver = getContentResolver();
        initReceiver();
        mHandler = new Handler();
        if (mSmsManager == null) {
            mSmsManager = SmsManager.getDefault();
            Intent sendIntent = new Intent(ACTION_SOS_SMS_SEND);
            mSOSSendPI = PendingIntent.getBroadcast(this, 0, sendIntent, 0);
            Intent rcvIntent = new Intent(ACTION_BOOT_SMS_RECEIVED);
            mBootRcvPI = PendingIntent.getBroadcast(this, 0, rcvIntent, 0);
            Intent imeiSend = new Intent(ACTION_IMEI_SMS_SEND);
            mIMEISendPI = PendingIntent.getBroadcast(this, 0, imeiSend, 0);
            Intent imeiRcv = new Intent(ACTION_IMEI_SMS_RECEIVED);
            mIMEIRcvPI = PendingIntent.getBroadcast(this, 0, imeiRcv, 0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if (intent == null || (action = intent.getAction()) == null) {
            return START_NOT_STICKY;
        }
        switch (action) {
            case ACTION_SOS_MSG:
                if (!mHasSend) {
                    mHasSend = true;
                    mSOSSend = new SendSmsRunnable(ACTION_SOS_MSG, R.string.sos_msg, "", mSOSSendPI, mBootRcvPI);
                    mHandler.post(mSOSSend);
                }
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mSendReceive != null)
            unregisterReceiver(mSendReceive);
        if (mSOSSend != null) {
            mHandler.removeCallbacks(mSOSSend);
        }
        if (mSimOneSend != null) {
            mHandler.removeCallbacks(mSimOneSend);
        }
        if (mSimChangeSend != null) {
            mHandler.removeCallbacks(mSimChangeSend);
        }
    }

    private void initReceiver() {
        mSendReceive = new SmsSendBroadCastReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SOS_SMS_SEND);
        filter.addAction(ACTION_BOOT_SMS_RECEIVED);
        filter.addAction(ACTION_IMEI_SMS_SEND);
        filter.addAction(ACTION_IMEI_SMS_RECEIVED);
        registerReceiver(mSendReceive, filter);
    }


    private class PhoneSignalStateListener extends PhoneStateListener {

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            mServiceState = serviceState;
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength;
        }

    }

    private int getSignalStrength(ServiceState serviceState,
                                  SignalStrength signalStrength) {
        int strengthLevel;
        if (serviceState == null
                || signalStrength == null
                || (!hasService(serviceState)
                && serviceState.getState() != ServiceState.STATE_EMERGENCY_ONLY))
            return 0;

        if (signalStrength.isGsm()) {// is gsm
            int asu = signalStrength.getGsmSignalStrength();

            if (asu <= 2 || asu == 99)
                strengthLevel = 0;
            else if (asu >= 12)
                strengthLevel = 4;
            else if (asu >= 8)
                strengthLevel = 3;
            else if (asu >= 5)
                strengthLevel = 2;
            else
                strengthLevel = 1;
        } else {
            strengthLevel = getEvdoLevel(signalStrength);
            if (strengthLevel == 0) {
                strengthLevel = getCdmaLevel(signalStrength);
            } else {
                strengthLevel = getCdmaLevel(signalStrength);
            }
        }
        return strengthLevel;
    }

    private boolean hasService(ServiceState serviceState) {
        if (serviceState != null) {
            switch (serviceState.getState()) {
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_POWER_OFF:
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    private int getCdmaLevel(SignalStrength mSignalStrength) {
        final int cdmaDbm = mSignalStrength.getCdmaDbm();
        final int cdmaEcio = mSignalStrength.getCdmaEcio();
        int levelDbm;
        int levelEcio;

        if (cdmaDbm >= -75)
            levelDbm = 4;
        else if (cdmaDbm >= -85)
            levelDbm = 3;
        else if (cdmaDbm >= -95)
            levelDbm = 2;
        else if (cdmaDbm >= -100)
            levelDbm = 1;
        else
            levelDbm = 0;

        // Ec/Io are in dB*10
        if (cdmaEcio >= -90)
            levelEcio = 4;
        else if (cdmaEcio >= -110)
            levelEcio = 3;
        else if (cdmaEcio >= -130)
            levelEcio = 2;
        else if (cdmaEcio >= -150)
            levelEcio = 1;
        else
            levelEcio = 0;

        return (levelDbm < levelEcio) ? levelDbm : levelEcio;
    }

    private int getEvdoLevel(SignalStrength mSignalStrength) {
        int evdoDbm = mSignalStrength.getEvdoDbm();
        int evdoSnr = mSignalStrength.getEvdoSnr();
        int levelEvdoDbm;
        int levelEvdoSnr;

        if (evdoDbm >= -65)
            levelEvdoDbm = 4;
        else if (evdoDbm >= -75)
            levelEvdoDbm = 3;
        else if (evdoDbm >= -90)
            levelEvdoDbm = 2;
        else if (evdoDbm >= -105)
            levelEvdoDbm = 1;
        else
            levelEvdoDbm = 0;

        if (evdoSnr >= 7)
            levelEvdoSnr = 4;
        else if (evdoSnr >= 5)
            levelEvdoSnr = 3;
        else if (evdoSnr >= 3)
            levelEvdoSnr = 2;
        else if (evdoSnr >= 1)
            levelEvdoSnr = 1;
        else
            levelEvdoSnr = 0;
        return (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
    }

    private class SmsSendBroadCastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SOS_SMS_SEND:
                    checkSOSSend(getResultCode());
                    break;
                case ACTION_IMEI_SMS_SEND:
                    if (mSimChanged) {
                        checkSimChangeSend(getResultCode());
                    }
                    if (mSimOneStart) {
                        checkOneHourSend(getResultCode());
                    }
                    break;
            }
        }
    }

    private void checkOneHourSend(int resultCode) {
        if (mSimOneSend == null)
            return;
        switch (resultCode) {
            case Activity.RESULT_OK:
                mHandler.removeCallbacks(mSimOneSend);
                mSimOneSend.mHasSendSms = true;
                mSimOneStart = false;
                Settings.System.putInt(mResolver, SIM_ONE_HOUR, SIM_ONE_HOUR_FINISH_SEND);
                checkNeedStop();
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            case SmsManager.RESULT_ERROR_RADIO_OFF:
            case SmsManager.RESULT_ERROR_NULL_PDU:
                mSimOneSend.mHasSendSms = false;
                break;
        }
    }

    private void checkNeedStop() {
        if (--mNeedSendCount <= 0) {
            mHasSend = false;
            stopSelf();
        }
    }

    private void checkSOSSend(int resultCode) {
        if (mSOSSend == null) {
            return;
        }
        switch (resultCode) {
            case Activity.RESULT_OK:
                mSOSSend.mHasSendSms = true;
                mHandler.removeCallbacks(mSOSSend);
                checkNeedStop();
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            case SmsManager.RESULT_ERROR_RADIO_OFF:
            case SmsManager.RESULT_ERROR_NULL_PDU:
                mSOSSend.mHasSendSms = false;
                break;
        }
    }

    private void checkSimChangeSend(int resultCode) {
        if (mSimChangeSend == null) {
            return;
        }
        switch (resultCode) {
            case Activity.RESULT_OK:
                mSimChangeSend.mHasSendSms = true;
                mHandler.removeCallbacks(mSimChangeSend);
                mSimChanged = false;
                checkNeedStop();
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            case SmsManager.RESULT_ERROR_RADIO_OFF:
            case SmsManager.RESULT_ERROR_NULL_PDU:
                mSimChangeSend.mHasSendSms = false;
                break;
        }
    }

    private class SendSmsRunnable implements Runnable {
        private String mNumber;
        private int mMsgId;
        private int mSendTime = 0;
        public boolean mHasSendSms = false;
        public PendingIntent mSendPI;
        public PendingIntent mRcvPI;
        public String mMsg;

        public SendSmsRunnable(String msg, int msgId, String number, PendingIntent sendPI, PendingIntent rcvPI) {
            mMsgId  = msgId;
            mNumber = number;
            mSendPI = sendPI;
            mRcvPI  = rcvPI;
            mMsg = msg;
        }

        @Override
        public void run() {
            checkSendAlarm(mNumber, mMsgId);
            mHandler.postDelayed(this, 15000);
        }

        /**
         * 判断是否发送防盗短信
         */
        private void checkSendAlarm(String number, int msgId) {
            int strengthLevel = getSignalStrength(mServiceState, mSignalStrength);
            if (strengthLevel >= 2 && mSendTime < SMS_FAIL_SEND_TIMES
                    && !mHasSendSms) {
                mSendTime++;
                mHasSendSms = true;
                sendAlarmMsg(number, msgId);
            }
        }

        /**
         * 发送防盗短信
         */
        private void sendAlarmMsg(String number, int msgId) {
            try {
                List<String> sosList = SOSInfoUtils.getSOSNumList(SOSService.this);
                if (sosList != null && sosList.size() > 0) {
                    mNeedSendCount = sosList.size();
                    String policeNumber = DeviceUtils.getPoliceNum(SOSService.this);
                    String msg = getString(msgId, policeNumber);
                    for (String numberInt : sosList) {
                        mSmsManager.sendTextMessage(numberInt, null, msg, mSendPI, mRcvPI);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
