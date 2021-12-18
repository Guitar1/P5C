package com.zzx.police.webrtc;

//public static final String ACTION_RELEASE_CAMERA = "action_Release_Camera";  停止录像
//public static final String ACTION_UEVENT_MIC = "zzx_action_mic";  //停止录音

import android.content.Intent;

public class FastModel {
    //是否在视频通话
    private boolean isTalking = false;
    //是否在录像
    private boolean isRecording = false;
    //是否在录音
    private boolean isMic = false;

    private volatile static FastModel instance;
    public static FastModel getInstance() {
        if (instance == null) {
            synchronized (FastModel.class) {
                if (instance == null) {
                    instance = new FastModel();
                }
            }
        }
        return instance;
    }

    public boolean isTalking() {
        return isTalking;
    }

    public void setTalking(boolean talking) {
        isTalking = talking;
    }

    public boolean isMic() {
        return isMic;
    }

    public void setMic(boolean mic) {
        isMic = mic;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }
}