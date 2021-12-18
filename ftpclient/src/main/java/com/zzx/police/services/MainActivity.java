package com.zzx.police.services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends Activity implements View.OnClickListener {
    private IFTPService mService = null;
    private Button mBtnUpload   = null;
    public static final String ACTION_FILE_UPLOAD_FINISH = "action_FileUploadFinish";
    public static final String UPLOAD_FILE_NAME = "upload_file_name";
    public static final String ACTION_FILE_UPLOAD_LENGTH = "action_FileUploadLength";
    public static final String UPLOAD_FILE_LENGTH = "upload_file_length";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String fileName = intent.getStringExtra(UPLOAD_FILE_NAME);
            if (action.equals(ACTION_FILE_UPLOAD_FINISH)) {
                Log.i(getPackageName(), fileName + " upload finished");
            } else if (action.equals(ACTION_FILE_UPLOAD_LENGTH)) {
                long length = intent.getLongExtra(UPLOAD_FILE_LENGTH, 0);
                Log.i(getPackageName(), fileName + " upload " + length);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnUpload = (Button) findViewById(R.id.btn_upload);
        mBtnUpload.setOnClickListener(this);
        Intent intent = new Intent("android.intent.action.FTPService");
        if (bindService(intent, mConnection, BIND_AUTO_CREATE)) {
            Log.i(getPackageName(), "bind success");
        } else {
            Log.i(getPackageName(), "bind failed");
        }
        IntentFilter filter = new IntentFilter(ACTION_FILE_UPLOAD_LENGTH);
        filter.addAction(ACTION_FILE_UPLOAD_FINISH);
        registerReceiver(mReceiver, filter);
        try {
            Context remoteContext = createPackageContext("com.zzx.setting", Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences preferences = remoteContext.getSharedPreferences("com.zzx.setting_preferences", Context.MODE_MULTI_PROCESS);
            Log.i(getPackageName(), "key_record_section = " + preferences.getString("key_record_section", "0"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        unbindService(mConnection);
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IFTPService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upload:
                try {
                    mService.uploadFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "test.3gp", "/");
                    mService.uploadFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "test.png", "/");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
