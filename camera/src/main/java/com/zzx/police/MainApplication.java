package com.zzx.police;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.example.location.locationApplaction;
import com.tencent.bugly.crashreport.CrashReport;
import com.zzx.police.services.FTPDownService;
import com.zzx.police.utils.CrashHandler;

/**
 * @author Tomy
 *         Created by Tomy on 2014/6/13.
 */
public class MainApplication extends locationApplaction {
    private static final String TAG = "MainApplication";
    public static final String ACTION_READ_BARCODE = "read_barcode";
    public static final String ACTION_REPORT_BARCODE = "report_barcode";
    public static final String EXTRA_BARCODE = "extra_barcode";
    private CrashHandler mHandler;
//    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        mHandler = CrashHandler.getInstance();
        mHandler.init(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(mHandler);
        //使用内存泄漏的检测
      /*  if (LeakCanary.isInAnalyzerProcess(this)) {//1
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);*/
        CrashReport.initCrashReport(getApplicationContext());
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
    /* public static RefWatcher getRefWatcher(Context context) {
        MainApplication application = (MainApplication) context.getApplicationContext();
        return application.refWatcher;
    }*/
    /**
     * 获取当前进程名
     */
    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }
    /**
     * 包名判断是否为主进程
     * if
     *
     * @param
     * @return
     */
    public boolean isMainProcess() {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return getApplicationInfo().packageName.equals(appProcess.processName);
            }
        }
        return false;

    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        stopFTPDownService();
        if (mHandler != null)
            mHandler.release();
    }
    private void stopFTPDownService(){
        Intent intent=new Intent(this, FTPDownService.class);
        stopService(intent);
    }
}
