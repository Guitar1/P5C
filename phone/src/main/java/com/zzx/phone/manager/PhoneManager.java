package com.zzx.phone.manager;

import android.content.Context;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zzx.phone.R;
import com.zzx.phone.data.Values;

import java.lang.reflect.Method;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * 
 * 描述：电话处理逻辑类
 * 陈伟斌
 * 2014-2-22
 */
public class PhoneManager {

	private Context mContext;
	private static String interactivePhoneNumber;
	private static PhoneState phoneState = PhoneState.IDLE;
    private Class<TelephonyManager> mTelephonyClass = null;
    private TelephonyManager mManager = null;
    private Object mITelephony = null;

    public PhoneManager(Context context) {
		mContext = context;
        mManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        mTelephonyClass = TelephonyManager.class;
        getITlephony();
	}

    private void getITlephony() {
        Method getITelephonyMethod;
        try {
            getITelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony",
                    (Class[]) null);
            getITelephonyMethod.setAccessible(true);

            mITelephony = getITelephonyMethod.invoke(mManager,
                    (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static enum PhoneState {
		IDLE// 正常态
		, RINGING// 响铃
		, ACTIVE// 通话中
		, OUT_GOING// 拨号中
	}

	/**
	 * 取得正在交互的电话号码
	 * 
	 * 陈伟斌
	 * 2014-2-25
	 */
	public String getInteractivePhoneNumber() {
		return interactivePhoneNumber;
	}

	/**
	 * 设置正在交互的电话号码
	 * 
	 * 陈伟斌
	 * 2014-2-25
	 */
	public void setInteractivePhoneNumber(String phoneNumber) {
		interactivePhoneNumber = phoneNumber;
	}

	public String getLocalPhoneNumber(Context context) {
        return ((TelephonyManager) context.getSystemService(TELEPHONY_SERVICE)).getLine1Number();
	}

	/**
	 * 接听电话
	 * 陈伟斌
	 * 2014-2-22
	 */
	public synchronized void doAnswerRingingCall() {
		/*// 插耳机
		Intent localIntent1 = new Intent(Intent.ACTION_HEADSET_PLUG);
		localIntent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		localIntent1.putExtra("state", 1);
		localIntent1.putExtra("microphone", 1);
		localIntent1.putExtra("name", "Headset");
		mContext.sendOrderedBroadcast(localIntent1,
				"android.permission.CALL_PRIVILEGED");
		// 按下耳机按钮
		Intent localIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON);
		KeyEvent localKeyEvent1 = new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_HEADSETHOOK);
		localIntent2.putExtra("android.intent.extra.KEY_EVENT", localKeyEvent1);
		mContext.sendOrderedBroadcast(localIntent2,
				"android.permission.CALL_PRIVILEGED");
		// 放开耳机按钮
		Intent localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
		KeyEvent localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP,
				KeyEvent.KEYCODE_HEADSETHOOK);
		localIntent3.putExtra("android.intent.extra.KEY_EVENT", localKeyEvent2);
		mContext.sendOrderedBroadcast(localIntent3,
				"android.permission.CALL_PRIVILEGED");
		// 拔出耳机
		Intent localIntent4 = new Intent(Intent.ACTION_HEADSET_PLUG);
		localIntent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		localIntent4.putExtra("state", 0);
		localIntent4.putExtra("microphone", 1);
		localIntent4.putExtra("name", "Headset");
		mContext.sendOrderedBroadcast(localIntent4,
				"android.permission.CALL_PRIVILEGED");*/
        Method answerRingMethod;
        try {
            answerRingMethod = mITelephony.getClass().getMethod("answerRingingCall", (Class[]) null);
            answerRingMethod.invoke(mITelephony);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * 
	 * 挂断电话
	 * 陈伟斌
	 * 2014-2-22
	 */
	public void endCall() {
		try {
			Method endCall = mITelephony.getClass().getDeclaredMethod("endCall");
			endCall.invoke(mITelephony);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 说明:忽略来电,静音
	 * Author:陈伟斌
	 * 2013-1-22
	 */
	public void ignoreInComing() {
		try {
			Method endCall = mITelephony.getClass().getDeclaredMethod(
					"silenceRinger");
			endCall.invoke(mITelephony);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPhoneState(PhoneState state) {
		phoneState = state;
		Log.i("test", " setPhoneState state=" + phoneState);
	}

	public PhoneState getPhoneState() {
		Log.i("test", " getPhoneState state=" + phoneState);
		return phoneState;
	}

	/**
	 * 打电话
	 * 
	 * @param phoneNumber
	 *            陈伟斌
	 *            2014-2-22
	 */
	public void call(String phoneNumber) {

		if (getPhoneState() != PhoneState.IDLE) {
			return;
		}

		this.setInteractivePhoneNumber(phoneNumber);
		try {
			Method call = mITelephony.getClass().getDeclaredMethod("call",
					String.class);
			call.invoke(mITelephony, phoneNumber);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 方法描述：
	 */

	public void setSpeakerOn(boolean isOn) {

		AudioManager audioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);

		audioManager.setMode(AudioManager.MODE_IN_CALL);
		audioManager.setSpeakerphoneOn(true);
	}

	public String getCarrierName() {

		TelephonyManager mTelManager = (TelephonyManager) mContext
				.getSystemService(TELEPHONY_SERVICE);
		String carrier = mTelManager.getNetworkOperatorName();

		int carrierId = 0;
		if (carrier.contains(Values.CHN_CDMA_CARRIER_NAME_1)
				|| carrier.contains(Values.CHN_CDMA_CARRIER_NAME_2)
				|| carrier.contains(Values.CHN_CDMA_CARRIER_NAME_3)
				|| carrier.contains(Values.CHN_CDMA_CARRIER_NAME_4)) {
			carrierId = R.string.carrier_CDMA;
		} else if (carrier.contains(Values.CHN_MOBILE_CARRIER_NAME_1)
				|| carrier.contains(Values.CHN_MOBILE_CARRIER_NAME_2)) {
			carrierId = R.string.carrier_MOBILE;
		} else if (carrier.contains(Values.CHN_UNICOM_CARRIER_NAME_1)
				|| carrier.contains(Values.CHN_UNICOM_CARRIER_NAME_2)
				|| carrier.contains(Values.CHN_UNICOM_CARRIER_NAME_3)
				|| carrier.contains(Values.CHN_UNICOM_CARRIER_NAME_4)) {
			carrierId = R.string.carrier_UNICOM;
		}
		if (carrierId == 0) {
			return carrier;
		}
		return mContext.getString(carrierId);
	}

}
