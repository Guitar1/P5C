package com.zzx.police.utils;

import android.content.Context;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zzx.police.data.FTPDownLoadInfo;
import com.zzx.police.data.Values;

import java.io.File;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;


/**
 * Created by wanderFly on 2017/12/1
 * 以前用FTP上传或者下载的时候都是用的{@link FTPContinue}这个类进行相关操作,
 * 现在为了在实现FTP下载功能和调试的方便，重新创建一个新的类，仅仅用于FTP文件下载相关的操作
 */
public class FTPDownUtil {

    private static final boolean DEBUG = false;
    //private static final String TAG = "FTPDownUtil";
    private static final String TAG = "FTPDownService";
    public static final String FTP_DEFAULT_IP = "120.55.162.188";
    public static final String FTP_ZHONG_DUN_IP = "112.74.135.64";//中盾公司的服务器地址
    public static final String FTP_IP_OTHER = "218.64.169.250";
    public static final String FTP_DEFAULT_USER = "byandaftp";
    public static final String FTP_DEFAULT_PASS = "123456";
    public static final String FTP_USER_OTHER = "vsftpd";
    public static final String FTP_PASS_OTHER = "vsftpd";
    public static final int FTP_DEFAULT_PORT = 21;
    private String mFtpHost = "";
    private String mFtpUserName = "";
    private String mFtpPassword = "";
    private Context mContext;
    private FTPClient mFtpClient;
    private int mFtpPort = 21;
    private FTPDownStatusListener mStatusListener;


    public FTPDownUtil(Context mContext, FTPDownStatusListener statusListener) {
        this(mContext,
                statusListener,
                settingCheckNULL(mContext, Values.FTP_NAME,FTPDownUtil.FTP_DEFAULT_USER),
                settingCheckNULL(mContext, Values.FTP_PASS,FTPDownUtil.FTP_DEFAULT_PASS),
                settingCheckNULL(mContext, Values.FTP_IP,FTPDownUtil.FTP_ZHONG_DUN_IP),//FTPDownUtil.FTP_DEFAULT_IP,
                Settings.System.getInt(mContext.getContentResolver(), Values.FTP_SET_PORT,FTPDownUtil.FTP_DEFAULT_PORT));
    }

    public FTPDownUtil(Context context,
                       FTPDownStatusListener statusListener,
                       String userName,
                       String password,
                       String hostIp,
                       int hostPort) {

        d("FTPDownUtil: ");

        this.mContext = context.getApplicationContext();
        this.mStatusListener = statusListener;
        this.mFtpUserName = userName;
        this.mFtpPassword = password;
        this.mFtpHost = hostIp;
        this.mFtpPort = hostPort;

        mFtpClient = new FTPClient();
        mFtpClient.setAutoNoopTimeout(5 * 1000);
        mFtpClient.setPassive(true);   //MTK
        //mFtpClient.setPassive(false);//小米
        initDownThread();
    }


    private void d(String msg) {
        if (DEBUG) Log.d(TAG, "" + msg);
    }

    private void d(@NonNull String tag, String msg) {
        if (DEBUG) Log.d(TAG, "" + msg);
    }

    private void e(String msg) {
        if (DEBUG) Log.e(TAG, "" + msg);
    }

    private void resetLoginInfo(FTPDownLoadInfo info) {
        mFtpUserName = info.getFtpAccount();
        mFtpPassword = info.getFtpPwd();
        mFtpHost = info.getFtpIp();
        mFtpPort = info.getFtpPort();
    }

    public static String settingCheckNULL(Context mContext, String name, String defult) {
        String values = Settings.System.getString(mContext.getContentResolver(), name);
        if (values != null) {
            return values;
        } else {
            return defult;
        }


    }
    private boolean connect() {
        d("connect():"
                + "\n" + "EPO_FTP_HOST:     " + mFtpHost
                + "\n" + "EPO_FTP_PORT:     " + mFtpPort
                + "\n" + "EPO_FTP_USER_NAME:" + mFtpUserName
                + "\n" + "EPO_FTP_PASSWORD: " + mFtpPassword);
        try {
            if (mFtpClient == null) {
                mFtpClient = new FTPClient();
                mFtpClient.setAutoNoopTimeout(5 * 1000);
                mFtpClient.setPassive(true);
            }
            if (mFtpClient.isConnected())
                return true;
            mFtpClient.connect(mFtpHost, mFtpPort);
            mFtpClient.login(mFtpUserName, mFtpPassword);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                mFtpClient.setPassive(true);
                mFtpClient.connect(mFtpHost, mFtpPort);
                mFtpClient.login(mFtpUserName, mFtpPassword);
            } catch (Exception e1) {
                e1.printStackTrace();
                e("FTP connect failed");
                if (mStatusListener != null) {
                    mStatusListener.loginStatus(false);
                }
                return false;
            }
        }
        if (mStatusListener != null) {
            mStatusListener.loginStatus(true);
        }
        d("connect: FTP connect success");
        return true;
    }

    public void release() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        }).start();
        releaseDownThread();
    }

    private void disconnect() {
        d("disconnect: -------开始------");
        try {
            if (mFtpClient != null && mFtpClient.isConnected()) {
                mFtpClient.abortCurrentDataTransfer(true);
                mFtpClient.logout();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mStatusListener != null) {
                mStatusListener.error(null, e.toString());
            }
        } finally {
            if (mFtpClient != null){
                mFtpClient.abruptlyCloseCommunication();
                mFtpClient = null;
            }
            mLoginStatus = false;
            if (mStatusListener!=null){
                mStatusListener.loginStatus(false);
            }
        }
        d("disconnect: -------结束------");
    }


    /**
     * 将要下载的文件添加到缓冲队列中
     *
     * @param info 需要下载的文件对象
     */
    public void addFileToBuffer(FTPDownLoadInfo info) {
        if (info == null)
            return;
        mFtpDownBuffer.add(info);
        initDownThread();
    }

    @SuppressWarnings("all")
    public void addFileToBuffer(LinkedHashMap<String, FTPDownLoadInfo> map) {
        if (map.isEmpty())
            return;
        Iterator<Map.Entry<String, FTPDownLoadInfo>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            mFtpDownBuffer.add(iterator.next().getValue());
        }
        initDownThread();
    }

    /**
     * 检测服务器中是否有需要下载的文件
     *
     * @param info 需要下载的文件对象
     */
    private boolean checkNeedDownFile(FTPDownLoadInfo info) {

        String remoteDir = info.getFilePath();
        String remoteFileName = info.getFileName();
        String localDirPath = info.getSavePath();

        d("checkNeedDownFile: "
                + "\n" + "下载文件路径:" + remoteDir
                + "\n" + "下载文件名字:" + remoteFileName
                + "\n" + "文件保存路径:" + localDirPath
                + "\n" + "当前FTP账号: " + mFtpClient.getUsername()
                + "\n" + "当前FTP密码: " + mFtpClient.getPassword()
                + "\n" + "当前FTP端口: " + mFtpClient.getPort()
                + "\n" + "当前FTP主机: " + mFtpClient.getHost()
        );
        //打印服务器当前目录下的所有文件(用于测试)
        /*if (DEBUG) {
            try {
                d("checkNeedDownFile: currentDir:" + mFtpClient.currentDirectory());
                FTPFile[] tempFile = mFtpClient.list();
                for (FTPFile f : tempFile) {
                    d("checkNeedDownFile: 文件:" + f.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                e("checkNeedDownFile:打印当前目录下文件出错:"+e.toString());
                if (mStatusListener!=null){
                    mStatusListener.error(e.toString());
                }
            }
        }*/

        try {
            if (!mFtpClient.currentDirectory().equals(remoteDir)) {
                mFtpClient.changeDirectory(remoteDir);
                //mFtpFiles = mFtpClient.list();
            }
            FTPFile[] ftpFiles = mFtpClient.list();
            boolean findFileSuccess = false;
            for (FTPFile ftpFile : ftpFiles) {
                d("checkNeedDownFile: -----ftpFile.getName():" + ftpFile.getName() + " remoteFileName:" + remoteFileName + " 比较值:" + ftpFile.getName().equals(remoteFileName));
                //if (ftpFile.getName().contains(remoteFileName)) {
                if (ftpFile.getName().equals(remoteFileName)) {
                    findFileSuccess = true;
                    readyToDown(info, ftpFile);
                    break;
                }
            }
            if (!findFileSuccess) {
                if (mStatusListener != null) {
                    mStatusListener.fileNoFind(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mStatusListener != null) {
                mStatusListener.error(info, e.toString());
            }
            e("checkNeedDownFile: " + e);
            return false;
        }
        return true;

    }

    private boolean readyToDown(FTPDownLoadInfo info, FTPFile ftpFile) {
        String remote = info.getFilePath() + info.getFileName();
        String local = info.getSavePath() + info.getFileName();

        try {
            File f = new File(local);
            if (f.exists()) {

                d("<--远程文件在本地已存在-->"
                        + "\n" + "remote: " + remote
                        + "\n" + "local : " + local
                        + "\n" + " localSize:" + f.length()
                        + "\n" + "remoteSize:" + ftpFile.getSize());

                long localSize = f.length();
                /**本地文件与远程文件大小相等，这里我们判定为两个文件完全一样*/
                if (localSize == ftpFile.getSize()) {
                    if (mStatusListener != null) {
                        mStatusListener.transferred(info, 1.0f);
                        mStatusListener.completed(info);
                    }
                    return true;
                }
                /**本地文件大于远程文件，这里我们判定为文件内容发生变化，以服务器提供的文件为准*/
                else if (localSize > ftpFile.getSize()) {
                    boolean deleteStatus = f.delete();
                    boolean makeDirStatus = f.getParentFile().mkdirs();
                    boolean createStatus = f.createNewFile();
                    d("<--远程文件小于本地文件 删除重新下载-->" +
                            "deleteStatus:" + deleteStatus + " makeDirStatus:" + makeDirStatus + " createStatus:" + createStatus);
                    if (mStatusListener != null)
                        mStatusListener.downReady(info);
                    mFtpClient.setType(FTPClient.TYPE_BINARY);//避免在下载中文文件时出现乱码情况.
                    mFtpClient.download(remote, f, 0, new DownLoadFTPDataTransferListener(info, ftpFile.getSize(), 0));

                }
                /**本地文件小于远程文件大小，这里我们判定为本地文件没有下载完,需要继续下载(虽然不一定正确)*/
                else {
                    if (mStatusListener != null)
                        mStatusListener.downReady(info);
                    mFtpClient.setType(FTPClient.TYPE_BINARY);//避免在下载中文文件时出现乱码情况.
                    mFtpClient.download(remote, new File(local), localSize, new DownLoadFTPDataTransferListener(info, ftpFile.getSize(), localSize));
                }
            } else {
                boolean makeDirStatus = f.getParentFile().mkdirs();
                boolean createStatus = f.createNewFile();
                d("<--本地没有远程文件-->"
                        + "remote = " + remote + "; local = " + local + " makeDirStatus:" + makeDirStatus + " createStatus:" + createStatus);
                if (mStatusListener != null)
                    mStatusListener.downReady(info);
                mFtpClient.setType(FTPClient.TYPE_BINARY);//避免在下载中文文件时出现乱码情况.
                mFtpClient.download(remote, f, 0, new DownLoadFTPDataTransferListener(info, ftpFile.getSize(), 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mStatusListener != null) {
                mStatusListener.error(info, e.toString());
            }
            e("readyToDown: 下载失败:" + remote + "\n" + e);
            return false;
        }
        return true;
    }


    public void abortDataDisconnect() {
        try {
            mFtpClient.abortCurrentDataTransfer(true);
            /*if (mFtpClient != null && mFtpClient.isConnected()) {
                 mFtpClient.abortCurrentDataTransfer(true);
			}*/
        } catch (Exception e) {
            e.printStackTrace();
            if (mStatusListener != null) {
                mStatusListener.error(null, e.toString());
            }
        }
    }


    private class DownLoadFTPDataTransferListener implements FTPDataTransferListener {

        private int totalTransferred = 0;
        private long mFileSize = -1;
        private float lastPercent = 0.0f;
        private FTPDownLoadInfo info;
        /**
         * 当进度传输的变化百分比大等于该值时，才将状态传递出去
         * 避免在实现对应接口刷新UI时，频率过快(与进度的显示精确度也相关)
         */
        private static final float RANGE = 0.01f;

        DownLoadFTPDataTransferListener(FTPDownLoadInfo info, long fileSize/*下载文件的大小*/, long startAt/*文件下载的起始位置*/) {
            if (fileSize <= 0) {
                throw new IllegalArgumentException("the size of file must be larger than zero.");
            }
            if (startAt < 0) {
                throw new IllegalArgumentException("the start position  must be greater than or equal to zero.");
            }
            this.info = info;
            this.mFileSize = fileSize;
            this.totalTransferred += startAt;
        }

        @Override
        public void started() {
            //d("started: 文件名:" + mRemote);
            if (mStatusListener != null)
                mStatusListener.started(info);
        }

        @Override
        public void transferred(int length) {
            totalTransferred += length;
            float percent = (float) totalTransferred / mFileSize;
            //d("transferred: 下载进度变化:" + Math.abs(percent - lastPercent));
            if (mStatusListener != null) {
                if (Math.abs(percent - lastPercent) >= RANGE || percent == 1) {
                    mStatusListener.transferred(info, percent);
                    lastPercent = percent;
                }
            }
        }

        @Override
        public void completed() {
            //d("completed: 文件名:" + mRemote);
            if (mStatusListener != null)
                mStatusListener.completed(info);
        }

        @Override
        public void aborted() {
            //e("aborted: 文件名:" + mRemote);
            if (mStatusListener != null)
                mStatusListener.aborted(info);
        }

        @Override
        public void failed() {
            //e("failed: 文件名:" + mRemote);
            if (mStatusListener != null)
                mStatusListener.failed(info);
        }
    }

    @IntDef({DOWN_STATUS_DEFAULT,
            DOWN_STATUS_READY,
            DOWN_STATUS_START,
            DOWN_STATUS_TRANSFERRED,
            DOWN_STATUS_COMPLETED,
            DOWN_STATUS_ABORTED,
            DOWN_STATUS_FAILED,
            DOWN_STATUS_NETWORK_IS_UNAVAILABLE,
            DOWN_STATUS_LOGIN_FAILED,
            DOWN_STATUS_LOGIN_SUCCESS,
            DOWN_STATUS_FILE_NO_FIND,
            DOWN_STATUS_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StatusCode {

    }

    public static final int DOWN_STATUS_DEFAULT = 0;
    public static final int DOWN_STATUS_READY = 1;
    public static final int DOWN_STATUS_START = 2;
    public static final int DOWN_STATUS_TRANSFERRED = 3;
    public static final int DOWN_STATUS_COMPLETED = 4;
    public static final int DOWN_STATUS_ABORTED = 5;
    public static final int DOWN_STATUS_FAILED = 6;
    public static final int DOWN_STATUS_NETWORK_IS_UNAVAILABLE = 7;
    public static final int DOWN_STATUS_LOGIN_FAILED = 8;
    public static final int DOWN_STATUS_LOGIN_SUCCESS = 9;
    public static final int DOWN_STATUS_FILE_NO_FIND = 10;
    public static final int DOWN_STATUS_ERROR = 11;

    public interface FTPDownStatusListener {
        /**
         * 下载--准备
         */
        void downReady(FTPDownLoadInfo info);

        /**
         * 下载--开始 {@link DownLoadFTPDataTransferListener#started()}
         */
        void started(FTPDownLoadInfo info);

        /**
         * 下载--进行 {@link DownLoadFTPDataTransferListener#transferred(int)}
         */
        void transferred(FTPDownLoadInfo info, float percentUpdate);

        /**
         * 下载--完成 {@link DownLoadFTPDataTransferListener#completed()}
         */
        void completed(FTPDownLoadInfo info);

        /**
         * 下载--中止 {@link DownLoadFTPDataTransferListener#aborted()}
         */
        void aborted(FTPDownLoadInfo info);

        /**
         * 下载--失败 {@link DownLoadFTPDataTransferListener#failed()}
         */
        void failed(FTPDownLoadInfo info);

        /**
         * 网络不可用
         */
        void networkIsUnavailable();

        /**
         * FTP账号登陆状态
         */
        void loginStatus(boolean isSuccess);

        /**
         * 没有找到需要下载的文件
         */
        void fileNoFind(FTPDownLoadInfo info);

        /**
         * ftpClient抛出异常信息
         * <p>注:
         *
         * @see #aborted(FTPDownLoadInfo)
         * @see #failed(FTPDownLoadInfo)
         * 上面的两个方法的调用是互斥的，具体可见{@link FTPClient#download(String, OutputStream, long, FTPDataTransferListener)}
         * 且{@link #aborted(FTPDownLoadInfo)#failed(FTPDownLoadInfo)}这两个方法的调用都会抛出一个异常信息.
         * 也就是说，当调用上面方法的时候，会触发{@link #error(FTPDownLoadInfo, String)}方法调用
         * 如果为了处理异常时方便，可以只处理{@link #error(FTPDownLoadInfo, String)}方法
         * </p>
         */
        void error(@Nullable FTPDownLoadInfo info, String errorInfo);
    }

    private FTPDownThread mDownThread;
    private ConcurrentLinkedQueue<FTPDownLoadInfo> mFtpDownBuffer = new ConcurrentLinkedQueue<>();
    //private ConcurrentSkipListSet<FTPDownLoadInfo> mFtpDownBuffer=new ConcurrentSkipListSet<>();
    private boolean mLoginStatus;

    private synchronized void initDownThread() {
        d("initDownThread: ");
        if (mDownThread == null) {
            mDownThread = new FTPDownThread();
            mDownThread.mIsOpen = true;
            mDownThread.start();
        }
    }

    private synchronized void releaseDownThread() {
        d("releaseDownThread: ");
        if (mDownThread != null) {
            mDownThread.mIsOpen = false;
            mDownThread.interrupt();
            mDownThread = null;
        }
    }

    private final class FTPDownThread extends Thread {
        private boolean mIsOpen;
        private int mCount;

        FTPDownThread() {
            d("FTPDownThread: ");
        }

        @Override
        public void run() {
            d("FTPDownThread  run: --------开始---------");
            while (mIsOpen) {
                d("FTPDownThread run: ---------当次循环--开始------");
                try {
                    if (!SystemUtil.isNetworkAvailable(mContext.getApplicationContext())) {
                        disconnect();
                        if (mStatusListener != null){
                            mStatusListener.networkIsUnavailable();
                        }
                        sleep(3000);
                        continue;
                    }
                    if (mLoginStatus) {
                        FTPDownLoadInfo info = mFtpDownBuffer.poll();
                        if (info != null) {
                            mCount = 0;
                            //在已经登陆的情况下，判断登陆的FTP账号地址等信息是否与要下载的文件信息相同
                            if (mFtpClient.getUsername().equals(info.getFtpAccount())
                                    && mFtpClient.getPassword().equals(info.getFtpPwd())
                                    && mFtpClient.getHost().equals(info.getFtpIp())
                                    && mFtpClient.getPort() == info.getFtpPort()) {
                                if (!checkNeedDownFile(info)) {
                                    disconnect();
                                    mFtpDownBuffer.add(info);
                                    sleep(3000);
                                    continue;
                                }
                            } else {
                                disconnect();
                                resetLoginInfo(info);
                                mFtpDownBuffer.add(info);
                                sleep(3000);
                                continue;
                            }
                        } else {
                            mCount++;
                            if (mCount == 5) {
                                disconnect();
                                releaseDownThread();
                            } else {
                                sleep(3000);
                            }
                            continue;
                        }
                    } else {
                        mLoginStatus = connect();
                        if (!mLoginStatus) {
                            sleep(3000);
                            continue;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    releaseDownThread();
                }
                d("FTPDownThread run: ---------当次循环--结束------");
            }
            d("FTPDownThread  run: --------结束---------");
        }

    }


}
