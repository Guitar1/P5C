package com.zzx.police.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import com.zzx.police.R;
import com.zzx.police.adapter.FTPDownAdapter;
import com.zzx.police.data.FTPDownLoadInfo;
import com.zzx.police.data.Values;
import com.zzx.police.utils.FTPDownUtil;
import com.zzx.police.utils.ProtocolUtils;
import com.zzx.police.view.ftpdown.FloatingWindowBig;
import com.zzx.police.view.ftpdown.FloatingWindowSmall;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by wanderFly on 2017/11/28.
 * 控制中心在文件下发时，该服务会去管理与FTP文件下载相关的ui操作
 */

public class FTPDownService extends Service implements FloatingWindowSmall.OnSmallWindowClickListener, FloatingWindowBig.OnBigWindowListener, FTPDownAdapter.OnFtpDownStatusClick {
    private static final String TAG = "FTPDownService";
    private static final boolean DEBUG = false;
    private WindowManager mWindowManager;
    //定义大小悬浮窗的layout参数
    private WindowManager.LayoutParams mParamsSmall;
    private WindowManager.LayoutParams mParamsBig;
    //定义大小悬浮窗对象
    private FloatingWindowBig mWindowBig;
    private FloatingWindowSmall mWindowSmall;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mIsAddedSmallWindow;
    private boolean mIsAddedBigWindow;
    private FTPDownUtil mFtpDownUtil;
    private FTPHandler mHandler;
    private ExecutorService mExecutorService;
    /**
     * 这个容器用来保存没有上传完的文件下发列表
     * 注:容器里面保存的{@link FTPDownLoadInfo}的消息都是初始化状态,
     * 里面的进度信息不具有真实性,只是用来在没有下载完成时，开机时继续下载.
     * 下载时会根据本地文件的大小进行断点下载.
     */
    private LinkedHashMap<String, FTPDownLoadInfo> mMap;
    /**
     * @see #mMap 可能会在下载和ui等线程同时调用,所以对该对象进行加锁操作
     * 注:在将该对象写入到文件时，虽然是在非UI线程中操作，但因为加锁的原因，
     * 如果此时，UI线程需要获取该锁，则要进入等待状态.这里因为是向本地中写入文件，
     * 且数据量较小，写入的时间是可控的，不会造成UI线程等待太久而卡顿.
     */
    private final Object mMapLock = new Object();


    @Override
    public void onCreate() {
        super.onCreate();
        d("onCreate():");
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        android.graphics.Point point = new android.graphics.Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        mScreenHeight = point.y;
        initBigWindow();
        addSmallWindow();
        //addBigWindow();
        mHandler = new FTPHandler(new WeakReference<>(this));
        mFtpDownUtil = new FTPDownUtil(this, statusListener);
        mMap = new LinkedHashMap<>();
        mExecutorService = Executors.newCachedThreadPool();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        d("onBind():");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        d("onStartCommand():");
        if (!mIsAddedSmallWindow && !mIsAddedBigWindow) {
            addSmallWindow();
        }
        ArrayList<FTPDownLoadInfo> lists = intent.getParcelableArrayListExtra(ProtocolUtils.KEY_FTP_DOWN_INFO_LIST);
        d("" + lists);
        LinkedHashMap<String, FTPDownLoadInfo> map;
        if (lists != null) {
            map = new LinkedHashMap<>();
            synchronized (mMapLock) {
                for (FTPDownLoadInfo info : lists) {
                    d(info.toString());
                    map.put(info.getFileName(), info);
                    mMap.put(info.getFileName(), info);
                }
                if (mMap.size() != 0) {
                    mWindowBig.setButtonMinVis();
                }
            }
            addOrUpdateDataToBigWindow(map);
            mFtpDownUtil.addFileToBuffer(map);
        }
        updateRate();
        /**方式一:新建线程用于保存文件*/
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mMapLock) {
                    FTPDownLoadInfo.saveToFile(FTPDownService.this, mMap, FTPDownLoadInfo.SAVE_FILE_NAME);
                }
            }
        }).start();*/
        /**方式二:以前PC端是所有文件上传到FTP服务器后，才下发文件相关信息,所以方式一，不会造成短时间创建大量的线程
         * 缺点是，PC端进行文件下发到手机端收到信息，可能会很久.
         * 现在为了手机端响应更快，PC端会对下发的文件按照从小到大的顺序排列，然后上传到FTP服务器，只要上传好一个就会下发一条信息,
         * 如果文件特别小，或者需要下发的文件已经在FTP服务器中，则会在短时间内下发很多消息，此时方式一就会创建很多线程
         * 所以用下面的方式会减少线程的不必要创建.(上面方式一从测试来看虽然影响不大,从编程思想上此时情况下用方式二更好)
         * */
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mMapLock) {
                    FTPDownLoadInfo.saveToFile(FTPDownService.this, mMap, FTPDownLoadInfo.SAVE_FILE_NAME);
                }
            }
        });
        //testSaveToFile(this,lists);
        //testSaveToFile(this,mMap);
        //isUIThread();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 测试代码
     */
    @SuppressWarnings("all")
    private void testSaveToFile(Context context, ArrayList<FTPDownLoadInfo> list) {
        FTPDownLoadInfo.saveToFile(this, list, FTPDownLoadInfo.SAVE_FILE_NAME);
        ArrayList<FTPDownLoadInfo> temp = FTPDownLoadInfo.getArrayListFromFile(this, FTPDownLoadInfo.SAVE_FILE_NAME);
        e("" + temp.getClass().toString());
        Iterator<FTPDownLoadInfo> iterator = temp.iterator();
        while (iterator.hasNext()) {
            e("onStartCommand: " + iterator.next().toString());
        }
    }

    /**
     * 测试代码
     */
    @SuppressWarnings("all")
    private void testSaveToFile(Context context, LinkedHashMap<String, FTPDownLoadInfo> map) {
        FTPDownLoadInfo.saveToFile(this, map, FTPDownLoadInfo.SAVE_FILE_NAME);
        LinkedHashMap<String, FTPDownLoadInfo> temp = FTPDownLoadInfo.getLinkHashMapFromFiles(this, FTPDownLoadInfo.SAVE_FILE_NAME);
        e("" + temp.getClass().toString());
        Iterator<Map.Entry<String, FTPDownLoadInfo>> iterator = temp.entrySet().iterator();
        while (iterator.hasNext()) {
            e("onStartCommand: " + iterator.next().getValue().toString());
        }
    }

    @Override
    public void onDestroy() {
        d("onDestroy():");
        mFtpDownUtil.release();
        removeSmallWindow();
        removeBigWindow();
        super.onDestroy();
    }

    @Override
    public void onSmallWindowClick() {
        d("onSmallWindowClick");
        addBigWindow();
    }

    @Override
    public void onBigWindowKeyBack() {
        addSmallWindow();
    }

    @Override
    public void onBigWindowButtonClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                /**方式一:只是调用了服务的{@link #onDestroy()}方法，没有真正意义的销毁服务.里面的数据还在*/
                //onDestroy();
                /**方式二:服务被销毁，服务所持有的对象也被释放.*/
                destroyService();
                break;
            case R.id.btn_min:
                addSmallWindow();
                break;
        }
    }

    private void destroyService() {
        Intent intent = new Intent(this, FTPDownService.class);
        stopService(intent);
    }

    @Override
    public void onFtpAdapterDownStatusClick(FTPDownLoadInfo info) {
        switch (info.getStatus()) {
            case FTPDownUtil.DOWN_STATUS_COMPLETED:
                addSmallWindow();
                startToFileManager();
                break;
            case FTPDownUtil.DOWN_STATUS_FILE_NO_FIND:
                removeDataFromBigWindow(info);
                break;
            //case FTPDownUtil.DOWN_STATUS_ABORTED:
            //case FTPDownUtil.DOWN_STATUS_FAILED://中止或失败放到下面的错误中(因为错误中包含有这两种情况)
            case FTPDownUtil.DOWN_STATUS_ERROR:
                info.setStatus(FTPDownUtil.DOWN_STATUS_DEFAULT);
                addOrUpdateDataToBigWindow(info);
                mFtpDownUtil.addFileToBuffer(info);
                break;
        }
    }


    private void d(String msg) {
        d(TAG, "" + msg);
    }

    private void d(String tag, String msg) {
        if (DEBUG)
            Log.d(tag, "当前线程名:" + Thread.currentThread().getName() + " id:" + Thread.currentThread().getId() + " " + msg);
    }

    private void e(String msg) {
        if (DEBUG) Log.e(TAG, "" + msg);
    }

    /**
     * 初始化小窗口
     */
    private void initSmallWindow() {
        if (mWindowSmall == null) {
            mWindowSmall = new FloatingWindowSmall(this);
            mWindowSmall.setOnSmallWindowLister(this);
            if (mParamsSmall == null) {
                mParamsSmall = new WindowManager.LayoutParams();
                mParamsSmall.type = WindowManager.LayoutParams.TYPE_PHONE;
                mParamsSmall.format = PixelFormat.RGBA_8888;
                mParamsSmall.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                mParamsSmall.gravity = Gravity.START | Gravity.TOP;
                mParamsSmall.width = mWindowSmall.getViewWidth();
                mParamsSmall.height = mWindowSmall.getViewHeight();
                mParamsSmall.x = mScreenWidth;
                mParamsSmall.y = mScreenHeight / 2;
            }
        }
    }

    /**
     * 初始化大窗口
     */
    private void initBigWindow() {
        if (mWindowBig == null) {
            mWindowBig = new FloatingWindowBig(this);
            mWindowBig.setOnBigWindowListener(this);
            mWindowBig.getAdapter().setOnFtpDownStatusClick(this);
            if (mParamsBig == null) {
                mParamsBig = new WindowManager.LayoutParams();
                mParamsBig.type = WindowManager.LayoutParams.TYPE_PHONE;
                mParamsBig.format = PixelFormat.RGBA_8888;
                //mLayoutParamsBig.flags= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                mParamsBig.gravity = Gravity.START | Gravity.TOP;
                mParamsBig.x = mScreenWidth / 2 - mWindowBig.getViewWidth();
                mParamsBig.y = mScreenHeight / 2 - mWindowBig.getViewHeight();
                mParamsBig.width = mWindowBig.getViewWidth();
                mParamsBig.height = mWindowBig.getViewHeight();
            }
        }
    }

    /**
     * 添加小窗口
     */
    private void addSmallWindow() {
        d("addSmallWindow: ");
        removeBigWindow();
        initSmallWindow();
        if (mWindowSmall != null && mParamsSmall != null && !mIsAddedSmallWindow) {
            mIsAddedSmallWindow = true;
            mWindowSmall.setParams(mParamsSmall);
            mWindowManager.addView(mWindowSmall, mParamsSmall);
        }
    }

    /**
     * 添加大窗口
     */
    private void addBigWindow() {
        d("addBigWindow: ");
        removeSmallWindow();
        initBigWindow();
        if (mWindowBig != null && mParamsBig != null && !mIsAddedBigWindow) {
            mIsAddedBigWindow = true;
            mWindowManager.addView(mWindowBig, mParamsBig);
        }
    }

    /**
     * 移除小窗口
     */
    private void removeSmallWindow() {
        d("removeSmallWindow: ");
        if (mWindowSmall != null && mIsAddedSmallWindow) {
            mIsAddedSmallWindow = false;
            mWindowManager.removeViewImmediate(mWindowSmall);
        }
    }

    /**
     * 移除大窗口
     */
    private void removeBigWindow() {
        d("removeBigWindow: ");
        if (mWindowBig != null && mIsAddedBigWindow) {
            mIsAddedBigWindow = false;
            mWindowManager.removeViewImmediate(mWindowBig);
        }
    }

    private boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * <p>
     * 应放到UI线程中进行操作,同理其它的也要注意线程问题
     * </p>
     */
    private void addOrUpdateDataToBigWindow(FTPDownLoadInfo info) {
        if (mWindowBig != null) {
            mWindowBig.getAdapter().addOrUpdateData(info, mIsAddedBigWindow);
        }
    }

    private void addOrUpdateDataToBigWindow(LinkedHashMap<String, FTPDownLoadInfo> infos) {
        if (mWindowBig != null) {
            mWindowBig.getAdapter().addOrUpdateData(infos, mIsAddedBigWindow);
        }
    }

    private void removeDataFromBigWindow(FTPDownLoadInfo info) {
        if (mWindowBig != null) {
            mWindowBig.getAdapter().removeData(info, mIsAddedBigWindow);
            synchronized (mMapLock) {
                mMap.remove(info.getFileName());
            }
            updateRate();
        }
    }


    private void startToFileManager() {
        try {
            Intent intent = new Intent();
            intent.setClassName(Values.PACKAGE_NAME_FILE_MANAGER_NEW, Values.CLASS_NAME_FILE_MANAGER_NEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("index", 3);//跳转到文件管理器中文本对应的列表
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新已经下载的文件数比例
     */
    private void updateRate() {
        synchronized (mMapLock) {
            if (mWindowBig != null) {
                int all = mWindowBig.getAdapter().getCount();
                int remain = mMap.size();
                int downCount = all - remain;
                String rate = downCount + "/" + all;
                mWindowBig.setRate(rate);
            }
        }
    }

    private FTPDownUtil.FTPDownStatusListener statusListener = new FTPDownUtil.FTPDownStatusListener() {
        @Override
        public void downReady(FTPDownLoadInfo info) {
            d(TAG, "downReady: " + info.getFileName());
            info.setStatus(FTPDownUtil.DOWN_STATUS_READY);
            send(info, FTPDownUtil.DOWN_STATUS_READY);
        }

        @Override
        public void started(FTPDownLoadInfo info) {
            d(TAG, "started: " + info.getFileName());
            info.setStatus(FTPDownUtil.DOWN_STATUS_START);
            send(info, FTPDownUtil.DOWN_STATUS_START);

        }

        @Override
        public void transferred(FTPDownLoadInfo info, float percentUpdate) {
            d(TAG, "transferred: " + info.getFileName() + " percentUpdate:" + percentUpdate);
            info.setProgress(percentUpdate);
            info.setStatus(FTPDownUtil.DOWN_STATUS_TRANSFERRED);
            send(info, FTPDownUtil.DOWN_STATUS_TRANSFERRED);
        }

        @Override
        public void completed(FTPDownLoadInfo info) {
            d(TAG, "completed: " + info.getFileName());
            info.setStatus(FTPDownUtil.DOWN_STATUS_COMPLETED);
            send(info, FTPDownUtil.DOWN_STATUS_COMPLETED);

            /**文件下载完成，从未下载完的链表中移除，并将数据保存到文件中*/
            synchronized (mMapLock) {
                mMap.remove(info.getFileName());
                FTPDownLoadInfo.saveToFile(FTPDownService.this, mMap, FTPDownLoadInfo.SAVE_FILE_NAME);
                send(MSG_UPDATE_RATE);
                send(mMap.size() == 0 ? MSG_UPDATE_BUTTON_CLOSE : MSG_UPDATE_BUTTON_MIN);
            }
        }

        @Override
        public void aborted(FTPDownLoadInfo info) {
            d(TAG, "aborted: " + info.getFileName());
            info.setStatus(FTPDownUtil.DOWN_STATUS_ABORTED);
            send(info, FTPDownUtil.DOWN_STATUS_ABORTED);
        }

        @Override
        public void failed(FTPDownLoadInfo info) {
            d(TAG, "failed: " + info.getFileName());
            info.setStatus(FTPDownUtil.DOWN_STATUS_FAILED);
            send(info, FTPDownUtil.DOWN_STATUS_FAILED);
        }

        @Override
        public void networkIsUnavailable() {
            d(TAG, "networkIsUnavailable: ");
            mHandler.sendEmptyMessage(FTPDownUtil.DOWN_STATUS_NETWORK_IS_UNAVAILABLE);
        }

        @Override
        public void loginStatus(boolean isSuccess) {
            d(TAG, "loginStatus: " + isSuccess);
            send(isSuccess ? FTPDownUtil.DOWN_STATUS_LOGIN_SUCCESS : FTPDownUtil.DOWN_STATUS_LOGIN_FAILED);

        }

        @Override
        public void fileNoFind(FTPDownLoadInfo info) {
            e("fileNoFind: " + info.getFileName());
            info.setStatus(FTPDownUtil.DOWN_STATUS_FILE_NO_FIND);
            send(info, FTPDownUtil.DOWN_STATUS_FILE_NO_FIND);
        }

        /**
         * @param info        为null:在没有下载文件的时候就已经出问题
         *                  不为null:在下载文件的时候出现的问题
         * @param errorInfo 错误信息
         * */
        @Override
        public void error(@Nullable FTPDownLoadInfo info, String errorInfo) {
            e("error: " + errorInfo);
            if (info != null) {
                info.setStatus(FTPDownUtil.DOWN_STATUS_ERROR);
                send(info, FTPDownUtil.DOWN_STATUS_ERROR);
            }
        }
    };


    private void send(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    private void send(int arg1, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        mHandler.sendMessage(msg);
    }

    private void send(FTPDownLoadInfo obj, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }

    private static final int MSG_UPDATE_RATE = FTPDownUtil.DOWN_STATUS_ERROR + 10;
    private static final int MSG_UPDATE_BUTTON_MIN = MSG_UPDATE_RATE + 1;
    private static final int MSG_UPDATE_BUTTON_CLOSE = MSG_UPDATE_RATE + 2;

    private static final class FTPHandler extends Handler {
        private WeakReference<FTPDownService> mWk;

        FTPHandler(WeakReference<FTPDownService> wk) {
            mWk = wk;
        }

        @Override
        public void handleMessage(Message msg) {
            FTPDownService service = mWk.get();
            if (service == null)
                return;
            switch (msg.what) {
                case FTPDownUtil.DOWN_STATUS_READY:
                case FTPDownUtil.DOWN_STATUS_START:
                case FTPDownUtil.DOWN_STATUS_TRANSFERRED:
                case FTPDownUtil.DOWN_STATUS_COMPLETED:
                case FTPDownUtil.DOWN_STATUS_ABORTED:
                case FTPDownUtil.DOWN_STATUS_FAILED:
                case FTPDownUtil.DOWN_STATUS_FILE_NO_FIND:
                case FTPDownUtil.DOWN_STATUS_ERROR:
                    FTPDownLoadInfo info = (FTPDownLoadInfo) msg.obj;
                    if (service.mWindowBig != null)
                        service.mWindowBig.getAdapter().addOrUpdateData(info, service.mIsAddedBigWindow);
                    if (service.mWindowSmall != null)
                        service.mWindowSmall.setPercent(info);
                    if (msg.what == FTPDownUtil.DOWN_STATUS_ERROR) {
                        info.setStatus(FTPDownUtil.DOWN_STATUS_DEFAULT);
                        service.mFtpDownUtil.addFileToBuffer(info);
                    }
                    /**
                     * 这里提供一种可选择的方案:ListView自动跳转到正在下载的Item项中
                     * (基本功能是有的，需要时可以进一步完善)
                     * */
                    //if (msg.what == FTPDownUtil.DOWN_STATUS_START)
                    //    setAutoToVisibility(service, info);
                    break;
                case FTPDownUtil.DOWN_STATUS_NETWORK_IS_UNAVAILABLE:
                    break;
                case FTPDownUtil.DOWN_STATUS_LOGIN_FAILED:
                    if (service.mWindowBig != null)
                        service.mWindowBig.setFTPLoginFailed();
                    break;
                case FTPDownUtil.DOWN_STATUS_LOGIN_SUCCESS:
                    if (service.mWindowBig != null)
                        service.mWindowBig.setFTPLoginSuccess();
                    break;
                case MSG_UPDATE_RATE:
                    service.updateRate();
                    break;
                case MSG_UPDATE_BUTTON_MIN:
                    if (service.mWindowBig != null)
                        service.mWindowBig.setButtonMinVis();
                    break;
                case MSG_UPDATE_BUTTON_CLOSE:
                    if (service.mWindowBig != null)
                        service.mWindowBig.setButtonCloseVis();
                    break;
            }
        }

        /**
         * 设置开始下载文件的时候，是否自动将对应的Item项显示在ListView的可见范围中
         *
         * @param service 下载服务
         * @param info    需要显示的Item项
         */
        private void setAutoToVisibility(FTPDownService service, FTPDownLoadInfo info) {
            if (service.mWindowBig != null) {
                if (service.mWindowBig.getListView() != null) {
                    setListViewPosition(service.mWindowBig.getAdapter().getLinkedHashMap(), info, service.mWindowBig.getListView(), 0);
                }
            }
        }

        /**
         * 将ListView中的某个Item对象，滑动到该ListView中的指定项.
         *
         * @param map      ListView适配器中用来保存数据的容器
         * @param info     需要滑动的Item项对象
         * @param listView ListView对象
         * @param offSet   偏移量
         *                 <p>
         *                 0:该ListView可见项的最后一项
         *                 1:该ListView可见项的倒数第二项
         *                 同理:可以通过获取该ListView可见项的数目使需要滑动的Item项居中
         *                 <p/>
         */
        private void setListViewPosition(
                LinkedHashMap<String, FTPDownLoadInfo> map,
                FTPDownLoadInfo info,
                ListView listView,
                int offSet) {

            int curPosition = getCurrentPosition(map, info);
            if (curPosition != -1) {
                int intentPosition = curPosition + offSet;      //想要设置的位置
                int remaining = map.size() - intentPosition - 1;//剩下的空间(Item位置是从0开始)
                if (remaining >= 0) {
                    listView.smoothScrollToPosition(intentPosition);
                } else {
                    listView.smoothScrollToPosition(map.size() - 1);
                }

            }
        }

        /**
         * 获取指定FTPDownInfo对象在适配器容器中保存的位置
         * <p>
         * 例如:在开始下载时，获取到开始下载文件对象的位置，然后通过
         * {@link #setListViewPosition(LinkedHashMap, FTPDownLoadInfo, ListView, int)}
         * 方法就可以将该下载对象对应的Item项自动滑动到ListView的可见区域
         * </p>
         *
         * @return -1 该容器中没有对应的FTPDownInfo对象
         */
        private int getCurrentPosition(LinkedHashMap<String, FTPDownLoadInfo> map, FTPDownLoadInfo info) {
            Object[] keys = map.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                if (key.equals(info.getFileName()))
                    return i;
            }
            return -1;
        }
    }
}
