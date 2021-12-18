package com.zzx.police.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.widget.Toast;

import com.zzx.police.MainActivity;
import com.zzx.police.R;
import com.zzx.police.command.VehicleUploadRequest;
import com.zzx.police.data.IntentInfo;
import com.zzx.police.data.UploadInfo;
import com.zzx.police.data.Values;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.utils.FTPContinue;
import com.zzx.police.utils.MD5;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2014/6/23.
 */
public class FTPService extends Service {

    private static final String TAG = "FTPService";
    private FTPContinue mFTPContinue    = null;
    private List<String> mFileList  = new ArrayList<>();
    private static int mFinishCount = 0;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Values.ACTION_UPLOAD_CANCEL.equals(intent.getAction())) {
				if (mFTPContinue != null) {
					mFTPContinue.abortDateDisconnect();
                    Toast.makeText(getApplicationContext(), getString(R.string.upload_cancel),
                            Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	private IFTPService.Stub stub = new IFTPService.Stub() {
		@Override
		public void basicTypes(int anInt, long aLong, boolean aBoolean,
				float aFloat, double aDouble, String aString)
				throws RemoteException {
		}

        @Override
        public boolean uploadFile(String filePath) throws RemoteException {
       /*     if (MainActivity.mProtocolUtils == null || !MainActivity.mProtocolUtils.isSocketCreated() || !mLoginSuccess) {
                return false;
            }
            if (!mFileList.contains(filePath))
                mFileList.add(filePath);
            Values.LOG_I(TAG, "filePath = " + filePath);
            File file = new File(filePath);
            String md5 = MD5.md5Sum(filePath);
            MainActivity.mProtocolUtils.sendUpload(file.getName(), md5, VehicleUploadRequest.STATUS_REQUEST);
            IntentInfo info = new IntentInfo(Values.ACTION_UPLOAD_STATE, 1);
            EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info);
            return true;*/
            if (Settings.System.getString(getContentResolver(),Values.POLICE_NUMBER)!=""){
                if (!mFileList.contains(filePath))
                    mFileList.add(filePath);
                String[] fileinfo=filePath.split("/");
                UploadInfo info = new UploadInfo(fileinfo[fileinfo.length-1], Settings.System.getString(getContentResolver(),Values.POLICE_NUMBER));
                EventBusUtils.postEvent(Values.BUS_EVENT_FTP, info);
                IntentInfo info2 = new IntentInfo(Values.ACTION_UPLOAD_STATE, 1);
                EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info2);
                return true;
            }else {
                return false;
            }
        }
    };
    private Intent mIntent;
    private boolean mLoginSuccess = false;
    private ServiceObserver mObserver;

    class ServiceObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ServiceObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            connectFtpService();
        }
    }

    @Override
    public void onCreate() {
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(Values.ACTION_UPLOAD_CANCEL);
		registerReceiver(mReceiver, mFilter);
        EventBusUtils.registerEvent(Values.BUS_EVENT_FTP_UPLOAD_FINISH, this);
        EventBusUtils.registerEvent(Values.BUS_EVENT_FTP, this);
        connectFtpService();
        mObserver = new ServiceObserver(new Handler());
        getContentResolver().registerContentObserver(Settings.System.getUriFor(Values.FTP_IP), true, mObserver);
        getContentResolver().registerContentObserver(Settings.System.getUriFor(Values.FTP_NAME), true, mObserver);
        super.onCreate();
    }

    private void connectFtpService() {
        new Thread() {
            @Override
            public void run() {
                if (mFTPContinue != null) {
                    try {
                        mFTPContinue.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ContentResolver resolver    = FTPService.this.getContentResolver();
                String ip   = Settings.System.getString(resolver, Values.FTP_IP);
                String port = Settings.System.getString(resolver, Values.FTP_SET_PORT);
                String user = Settings.System.getString(resolver, Values.FTP_NAME);
                String pass = Settings.System.getString(resolver, Values.FTP_PASS);
                if (ip == null || ip.equals("") || port == null || port.equals("") || user == null || user.equals("") || pass == null || pass.equals("")) {
                    mLoginSuccess = false;
                    return;
                }
                int iPort = Integer.parseInt(port);
                mFTPContinue = new FTPContinue(FTPService.this, null, ip, iPort, user, pass);
                super.run();
            }
        }.start();
    }

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
        if (mFTPContinue != null) {
            mFTPContinue.release();
            mFTPContinue = null;
        }
        getContentResolver().unregisterContentObserver(mObserver);
        mFinishCount = 0;
        mFileList.clear();
		EventBusUtils.unregisterEvent(Values.BUS_EVENT_FTP_UPLOAD_FINISH, this);
		EventBusUtils.unregisterEvent(Values.BUS_EVENT_FTP, this);
		super.onDestroy();
	}

    void onEventMainThread(Integer event) {
        switch (event) {
            case Values.FTP_LOGIN_FAILED:
                mLoginSuccess = false;
                break;
            case Values.FTP_LOGIN_SUCCESS:
                mLoginSuccess = true;
                break;
        }
    }

    void onEventBackgroundThread(String fileName) {
        assert MainActivity.mProtocolUtils != null;
        String md5 = MD5.md5Sum(fileName);
        File file = new File(fileName);
        if (mFileList.size() != 0 && (++mFinishCount) >= mFileList.size()) {
            mFinishCount = 0;
            mFileList.clear();
            IntentInfo info = new IntentInfo(Values.ACTION_UPLOAD_STATE, 0);
            EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info);
        }
        MainActivity.mProtocolUtils.sendUpload(file.getName(), md5, VehicleUploadRequest.STATUS_FINISH);
    }

    void onEvent(UploadInfo info) {
        for (int i = 0; i <= 5; i++) {
            if (mFTPContinue == null) {
                if (i == 5)
                    return;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        for (String filePath : mFileList) {
            File file = new File(filePath);
            if (file.getName().equals(info.mFileName)) {
                sendUploadBroadcast(info.mFileName);
                IntentInfo info1 = new IntentInfo(Values.ACTION_UPLOAD_STATE, 1);
                EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info1);
                mFTPContinue.upload(filePath, info.mRemoteDir);
                break;
            }
        }
    }

    private void sendUploadBroadcast(String fileName) {
        if (mIntent == null)
            mIntent = new Intent(Values.ACTION_UPLOAD_FILE);
        mIntent.putExtra(Values.EXTRA_UPLOAD_FILE, fileName);
        sendBroadcast(mIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null)
            return START_NOT_STICKY;
        switch (action) {
            case Values.ACTION_UPLOAD_LOG:
                String filePath = intent.getStringExtra(Values.UPLOAD_FILE_NAME);
                uploadFile(filePath);
                break;
        }
        return START_NOT_STICKY;
    }

    private void uploadFile(String filePath) {
        if (MainActivity.mProtocolUtils == null || !MainActivity.mProtocolUtils.isSocketCreated() || mFTPContinue == null) {
            return;
        }
        if (!mFileList.contains(filePath))
            mFileList.add(filePath);
        Values.LOG_I(TAG, "filePath = " + filePath);
        File file = new File(filePath);
        String md5 = MD5.md5Sum(filePath);
        MainActivity.mProtocolUtils.sendUpload(file.getName(), md5, VehicleUploadRequest.STATUS_REQUEST);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }
}
