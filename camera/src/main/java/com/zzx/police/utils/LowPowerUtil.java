package com.zzx.police.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class LowPowerUtil {
    private Context mContext;
    private AudioManager audioManager;
    public LowPowerUtil(Context context){
        this.mContext = context;
        audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    }
    public void setPowerMode(boolean enabling){
        SystemUtil.setAirplaneModeOn(mContext,enabling);
//        silentSwitchOn(enabling);
        if (enabling){
            SystemUtil.setBrightness(11);
            writeToDevice(0,"/sys/devices/platform/zzx-misc/beidou_stats");
            closeGPSSetting();
        }

    }
    //静音
    int audioValue  = 0;
    private void silentSwitchOn(boolean enabling) {
        Settings.System.putInt(mContext.getContentResolver(),"audio_value",getVolume());
        Intent intent = new Intent();
        intent.setAction("system.voice.changed");

        if(audioManager != null){
            if (enabling){
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                audioValue = Settings.System.getInt(mContext.getContentResolver(),"audio_value",7);
                setVolume(0);
                intent.putExtra("voiceState",0);
                Log.d("Silent:", "RINGING 已被静音");
            }else {
                Toast.makeText(mContext,"audio_value"+audioValue,Toast.LENGTH_SHORT).show();
                setVolume(Settings.System.getInt(mContext.getContentResolver(),"audio_value",audioValue));
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }

        }
        mContext.sendBroadcast(intent);
    }
    private void setVolume(int volume) {
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, AudioManager.STREAM_SYSTEM);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, AudioManager.STREAM_RING);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, AudioManager.STREAM_NOTIFICATION);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, volume, AudioManager.STREAM_DTMF);
    }
    //获取当前音量值
    private int getVolume(){
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    public int getScreenBrightness(Context context) {
        int value = 0;
        ContentResolver cr = context.getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    public boolean getGPsState() {
        return Settings.Secure.isLocationProviderEnabled(
                mContext.getContentResolver(), LocationManager.GPS_PROVIDER);
    }
    public void openGPSSettings() {
        try {
            Settings.Secure.setLocationProviderEnabled(
                    mContext.getContentResolver(), LocationManager.GPS_PROVIDER, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeGPSSetting() {
        try {
            Settings.Secure.setLocationProviderEnabled(
                    mContext.getContentResolver(), LocationManager.GPS_PROVIDER, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * @param state    0 关闭，1打开
     * @param filePath ir_cut文件设备路径
     */
    private void writeToDevice(int state, String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            return;
        }
        FileWriter outputStream = null;
        try {
            outputStream = new FileWriter(file);
            if (state == 1) {
                outputStream.write("1");
            } else if (state == 0) {
                outputStream.write("0");
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
