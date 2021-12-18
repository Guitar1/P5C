package com.zzx.police.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import com.zzx.police.R;

import java.util.Locale;

/**@author Tomy
 * Created by Tomy on 2014/6/13.
 */
public class TTSToast {
    private static final String TAG = "TTSToast";
    private static TextToSpeech mTTS = null;
    private static Toast mToast;

    public static void init(Context context) {
        if (mTTS == null) {
            mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != 0) {
                        return;
                    }

                    int result = mTTS.setLanguage(Locale.CHINA);
                    if ((result != TextToSpeech.LANG_NOT_SUPPORTED) && (result != TextToSpeech.LANG_MISSING_DATA)) {
                        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                            }

                            @Override
                            public void onDone(String utteranceId) {
                            }

                            @Override
                            public void onError(String utteranceId) {
                            }
                        });
                    }
                }
            });
        }
        mToast = Toast.makeText(context, R.string.app_name, Toast.LENGTH_SHORT);
    }

    public static void release() {
        if (mTTS != null) {
            if (mTTS.isSpeaking())
                mTTS.stop();
            mTTS.shutdown();
        }
        mToast = null;
    }

    public static void showToast(Context context, String msg, boolean needTTS, int show_time) {
        if (mToast == null) {
            mToast = Toast.makeText(context, R.string.app_name, Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.setDuration(show_time);
        mToast.show();
        if (needTTS)
            mTTS.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }

    public static void showToast(Context context, int msgId, boolean needTTS) {
        showToast(context, context.getString(msgId), needTTS, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, int msgId, boolean needTTS, int show_time) {
        showToast(context, context.getString(msgId), needTTS, show_time);
    }

    /**默认关闭语音播报
     * */
    public static void showToast(Context context, int msgId) {
        showToast(context, context.getString(msgId), false, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, int msgId, int show_time) {
        showToast(context, context.getString(msgId), false, show_time);
    }

    /**默认关闭语音播报
     * */
    public static void showToast(Context context, String msg) {
        showToast(context, msg, false, Toast.LENGTH_SHORT);
    }

    /**默认关闭语音播报
     * */
    public static void showToast(Context context, String msg, int showTime) {
        showToast(context, msg, false, showTime);
    }
}
