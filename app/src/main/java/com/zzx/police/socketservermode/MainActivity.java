package com.zzx.police.socketservermode;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Method;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int AP_CFG_CUSTOM_FILE_POLICE_NUMBER_LID = 42;
    private TextView mTvMsg;
    private NvRAMAgent mAgent = null;
    private Class<?> mServiceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        mTvMsg = (TextView) findViewById(R.id.text_view);
        try {
            mServiceClass = Class.forName("android.os.ServiceManager");
            Method method = mServiceClass.getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, "NvRAMAgent");
            mAgent = NvRAMAgent.Stub.asInterface(binder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread() {
            @Override
            public void run() {
                final String msg = "";
                mTvMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        mTvMsg.setText(msg);
                    }
                });
                super.run();
            }
        }.start();

        /*String number = "DSJ-A1-10004";
        byte[] numByte = number.getBytes();
        try {
            mAgent.writeFile(AP_CFG_CUSTOM_FILE_POLICE_NUMBER_LID, numByte);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
        byte[] buffer = null;
        try {
            buffer = mAgent.readFile(AP_CFG_CUSTOM_FILE_POLICE_NUMBER_LID);
            for (byte b : buffer) {
                Log.e(TAG, "b = " + b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}