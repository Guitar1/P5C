package com.zzx.police.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

public class ProximitySensor implements SensorEventListener {
    private static final String TAG = "ProximitySensor";
    private Sensor mSensor;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;

    @SuppressLint("InvalidWakeLockTag")
    public ProximitySensor(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);// TYPE_GRAVITY
        if (mSensor!=null){
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        //息屏设置
        mPowerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                TAG);
    }
    public void onDestroy(){
        //传感器取消监听
        mSensorManager.unregisterListener(this);
        if (mWakeLock.isHeld())
            mWakeLock.release();
        mWakeLock = null;
        mPowerManager = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] ==0.0){
            if (!mWakeLock.isHeld())
                mWakeLock.acquire();
        }else {
            if (mWakeLock.isHeld())
                mWakeLock.release();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
