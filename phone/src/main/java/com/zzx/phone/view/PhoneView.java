package com.zzx.phone.view;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzx.phone.R;
import com.zzx.phone.data.Values;
import com.zzx.phone.manager.PhoneManager;
import com.zzx.phone.utils.EventBusUtils;

import static com.zzx.phone.manager.PhoneManager.PhoneState;

/**@author Tomy
 * Created by Tomy on 2014/6/24.
 */
public class PhoneView  extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "PhoneView";
    private Button mBtnAnswer   = null;
    private Button mBtnHangup   = null;
    private ImageButton mBtnKeyboard    = null;
    private ImageView mIvHead = null;
    private TextView mTvContact = null;
    private TextView mTvCallState   = null;
    private TextView mTvCallTime    = null;
    private PhoneManager mPhoneManager  = null;
    private Handler mHandler    = null;
    private Context mContext    = null;
    private WindowManager mWinManager   = null;
    private boolean isShow = false;
    private WindowManager.LayoutParams mParams = null;
    private boolean isTimeTaskRunning = false;
    private String DTMF_KEY = "dtmf_key";
    private String ACTION_SEND_DTMF = "zzx_SendDtmf";
    private Intent mIntent;

    public PhoneView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.fragment_phone, this);
        mPhoneManager = new PhoneManager(context);
        findChildView();
        mContext = context;
        mHandler = new Handler();
        mWinManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    public void setUpListener() {
        EventBusUtils.registerEvent(TelephonyManager.ACTION_PHONE_STATE_CHANGED, this);
    }

    private void findChildView() {
        mBtnAnswer  = (Button) findViewById(R.id.btn_answer);
        mBtnHangup  = (Button) findViewById(R.id.btn_hangup);
        mIvHead     = (ImageView) findViewById(R.id.iv_head_sculpture);
        mBtnKeyboard    = (ImageButton) findViewById(R.id.btn_keyboard);
        mTvCallTime = (TextView) findViewById(R.id.tv_call_time);
        mTvContact  = (TextView) findViewById(R.id.tv_contact_name);
        mTvCallState  = (TextView) findViewById(R.id.tv_call_state);
        mBtnAnswer.setOnClickListener(this);
        mBtnHangup.setOnClickListener(this);
        mBtnKeyboard.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_answer:
                mPhoneManager.doAnswerRingingCall();
                break;
            case R.id.btn_hangup:
                mPhoneManager.endCall();
                break;
            case R.id.btn_keyboard:

                break;
        }
    }

    private void sendDtmf(String dtmfKey) {
        if (mIntent == null)
            mIntent = new Intent(ACTION_SEND_DTMF);
        mIntent.putExtra(DTMF_KEY, dtmfKey);
        mContext.sendBroadcast(mIntent);
    }

    int second = 0;
    private Runnable timeCountTask = new Runnable() {

        @Override
        public void run() {
            second++;
            String holdingTime = String.format("%02d:%02d:%02d", second / 3600,
                    second % 3600 / 60, second % 60);
            mTvCallTime.setText(holdingTime);
            mHandler.postDelayed(this, 1000);
        }
    };

    public void onEventMainThread(PhoneState phoneState) {
        String phoneNumber  = mPhoneManager.getInteractivePhoneNumber();
        if (phoneState == PhoneState.ACTIVE) {
            Values.LOGI(TAG, "onEventMainThread.PhoneState = ACTIVE");
            mTvCallTime.setVisibility(VISIBLE);
            mTvCallState.setVisibility(VISIBLE);
            mTvCallState.setText(R.string.calling);
            if (!isTimeTaskRunning) {
                isTimeTaskRunning = true;
                mHandler.postDelayed(timeCountTask, 1000);
            }
        } else if (phoneState == PhoneState.OUT_GOING) {
            Values.LOGI(TAG, "onEventMainThread.PhoneState = OUT_GOING");
            mBtnAnswer.setVisibility(INVISIBLE);
            mBtnKeyboard.setVisibility(INVISIBLE);
            mTvCallTime.setVisibility(INVISIBLE);
            mTvContact.setText(phoneNumber);
            mTvCallState.setText(mContext.getString(R.string.out_going));
            if (!isShow()) {
                showDialog();
            }
        } else if (phoneState == PhoneState.IDLE) {
            Values.LOGI(TAG, "onEventMainThread.PhoneState = IDLE");
            isTimeTaskRunning = false;
            mHandler.removeCallbacks(timeCountTask);
            second = 0;
            dismissDialog();
        } else if (phoneState == PhoneState.RINGING) {
            Values.LOGI(TAG, "onEventMainThread.PhoneState = RINGING");
            mTvCallTime.setVisibility(INVISIBLE);
            mBtnKeyboard.setVisibility(INVISIBLE);
            mTvCallState.setVisibility(INVISIBLE);
            mTvContact.setText(phoneNumber);
            mBtnAnswer.setVisibility(VISIBLE);
            if (!isShow()) {
                showDialog();
            }
        }
    }

    private boolean isShow() {
        return isShow;
    }

    private void showDialog() {
        if (mParams == null) {
            mParams = new WindowManager.LayoutParams();
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        try {
            mWinManager.addView(this, mParams);
            isShow = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissDialog() {
        try {
            mWinManager.removeView(this);
            isShow  = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
