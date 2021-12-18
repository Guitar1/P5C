package com.zzx.police.utils;

import android.content.Context;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zzx.police.data.FTPUploadInfo;
import com.zzx.police.data.Values;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ConcurrentLinkedQueue;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;


/**
 * Created by wanderFly on 2018/1/16.
 * 以前用FTP上传文件时，使用的是{@link FTPContinue}这个类，现在为了方便调试，
 * 重新创建一个FTP文件上传工具类，仅仅用于文件上传.该类的实现原理与{@link FTPDownUtil}
 * 相似，由于不是在同一时期添加该功能，所以并没有将该功能合并到{@link FTPDownUtil}这个类中.
 */

public class FTPUploadUtil {
    private static final boolean DEBUG = true;
    //private static final String TAG = "FTPUploadUtil";
    private static final String TAG = "CameraService";
    private FTPUploadStatusListener mStatusListener;
    private static FTPClient mFtpClient;
    private Context mContext;
    private String mFtpHost = "";
    private String mFtpUserName = "";
    private String mFtpPassword = "";
    /**
     * 登录FTP服务器成功时，FTP客户端获取到的当前目录
     *
     * @see #connectAndSetCurrentDir() 连接时使用这个方法连接，
     * 当前字符串表示的值才有效
     */
    private String mLoginDir = "";
    private int mFtpPort = 21;

    public FTPUploadUtil(Context context, FTPUploadStatusListener listener) {
        this(context,
                listener,
                settingCheckNULL(context, Values.FTP_NAME,FTPDownUtil.FTP_DEFAULT_USER),
                settingCheckNULL(context, Values.FTP_PASS,FTPDownUtil.FTP_DEFAULT_PASS),
                settingCheckNULL(context, Values.FTP_IP,FTPDownUtil.FTP_ZHONG_DUN_IP),//FTPDownUtil.FTP_DEFAULT_IP,
                Settings.System.getInt(context.getContentResolver(), Values.FTP_SET_PORT,FTPDownUtil.FTP_DEFAULT_PORT));
    }

    private FTPUploadUtil(Context context,
                          FTPUploadStatusListener statusListener,
                          String userName,
                          String password,
                          String hostIp,
                          int hostPort) {

        d("FTPUploadUtil: 构造函数");

        mContext = context.getApplicationContext();
        mStatusListener = statusListener;
        mFtpUserName = userName;
        mFtpPassword = password;
        mFtpHost = hostIp;
        mFtpPort = hostPort;

        mFtpClient = new FTPClient();
        mFtpClient.setAutoNoopTimeout(5 * 1000);
        mFtpClient.setPassive(true);   //MTK
        //mFtpClient.setPassive(false);//小米
        //initUploadThread();

    }
    public static String settingCheckNULL(Context mContext, String name, String defult) {
        String values = Settings.System.getString(mContext.getContentResolver(), name);
        if (values != null) {
            return values;
        } else {
            return defult;
        }


    }

    private void d(String msg) {
        if (DEBUG) Log.w(TAG, "" + msg);
    }

    private void d(@NonNull String tag, String msg) {
        if (DEBUG) Log.d(tag, "" + msg);
    }

    private void e(String msg) {
        Log.e(TAG, "" + msg);
    }

    private void resetLoginInfo(FTPUploadInfo info) {
        mFtpUserName = info.getFtpAccount();
        mFtpPassword = info.getFtpPwd();
        mFtpHost = info.getFtpIp();
        mFtpPort = info.getFtpPort();
    }

    private boolean connectAndSetCurrentDir() {
        try {
            if (connect()) {
                mLoginDir = mFtpClient.currentDirectory();
                return true;
            } else
                return false;

        } catch (Exception e) {
            error(e, null, "FTPUpload#connectAndSetCurrentDir " + e.toString());
            return false;
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
                statusUpdate(UPLOAD_STATUS_LOGIN_FAILED, null);
                return false;
            }
        }
        statusUpdate(UPLOAD_STATUS_LOGIN_SUCCESS, null);
        d("connect: FTP connect success");
        return true;
    }

    private synchronized void disconnect() {
        d("disconnect: -------开始------");
        if (!mLoginStatus)
            return;
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
            if (mFtpClient != null) {
                mFtpClient.abruptlyCloseCommunication();
                mFtpClient = null;
            }
            mLoginStatus = false;
            mLoginDir = "";
            statusUpdate(UPLOAD_STATUS_LOGIN_FAILED, null);
        }
        d("disconnect: -------结束------");
    }

    public void release() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        }).start();
        releaseUploadThread();
    }

    /**
     * 将要上传的文件添加到缓冲队列中
     *
     * @param info 需要上传的文件对象
     */
    public void addFileToBuffer(FTPUploadInfo info) {
        if (info == null) {
            e("addFileToBuffer: 添加的FTP文件上传对象为 null");
            return;
        }
        mFtpUploadBuffer.add(info);
        initUploadThread();
    }

    private void error(Exception e, @Nullable FTPUploadInfo info, String errorInfo) {
        if (DEBUG) e.printStackTrace();
        if (mStatusListener != null)
            mStatusListener.error(info, "出错行号:" + e.getStackTrace()[1].getLineNumber() + " " + errorInfo);
    }

    private void statusUpdate(@UploadStatusCode int statusCode, @Nullable FTPUploadInfo info) {
        if (mStatusListener != null) {
            if (info != null) info.setStatus(statusCode);
            mStatusListener.statusUpdate(statusCode, info);
        }
    }


    /**
     * 准备上传文件到FTP服务器中
     *
     * @param info 需要上传的文件对象
     * @return 上传结果
     */
    @CheckCode
    private int readyToUpload(FTPUploadInfo info) {

        String localPath = info.getFileLocalPath();
        if (!localPath.endsWith("/"))//路径末尾没有文件分隔符，添加上分隔符
            localPath += "/";
        File localFile;
        try {
            localFile = new File(localPath + info.getFileLocalName());
            if (localFile.isDirectory()) {
                e("readyToUpload: 上传的文件为目录 ----不上传并移除---");
                /**当为目录时不上传*/
                return CHECK_NO;
            } else {
                d("readyToUpload: 根据路径和名字获取到本地文件成功 0.0");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusUpdate(UPLOAD_STATUS_FILE_NO_FIND, info);
            return CHECK_NO;
        }


        FTPFile remoteFile;
        try {
            FTPFile[] files = mFtpClient.list();
            remoteFile = checkFileName(files, info.getFileLocalName());
        } catch (Exception e) {
            error(e, info, "FTPUploadUtil#readyToUpload():" + e.toString());
            return CHECK_ERROR;
        }


        /**
         * 这里判断的方式按照FTPClient中上传方法
         * @see FTPClient#upload(File, long, FTPDataTransferListener)
         * 进行异常的判断
         * */
        if (!localFile.exists()) {
            statusUpdate(UPLOAD_STATUS_FILE_NO_FIND, info);
            return CHECK_NO;
        } else {
            FileInputStream input = null;
            try {
                input = new FileInputStream(localFile);
                try {
                    statusUpdate(UPLOAD_STATUS_READY, info);
                    if (mFtpClient != null)
                    mFtpClient.setType(FTPClient.TYPE_BINARY);
                    /**FTP服务器中不存在需要上传的文件*/
                    if (remoteFile == null) {
                        e("服务器中不存在需要上传的文件");
                        if (localFile.length() > 0){
                            mFtpClient.upload(localFile.getName(), input, 0, 0, new UploadFTPDataTransferListener(info, localFile.length(), 0));
                        }
                    }
                    /**FTP服务器中存在需要上传的文件*/
                    else {
                        e("服务器中存在需要上传的文件");
                        long remoteLength = remoteFile.getSize();
                        long localLength = localFile.length();
                        /**1:存在需要上传的文件且相等(这里判定为两个文件完全一样)*/
                        if (remoteLength == localLength) {
                            info.setProgress(1.0f);
                            statusUpdate(UPLOAD_STATUS_TRANSFERRED, info);
                            statusUpdate(UPLOAD_STATUS_COMPLETED, info);
                            return CHECK_OK;
                        }
                        /**2:存在需要上传的文件且小于需要上传的文件大小(这里判定为文件没有上传完成需要继续上传 注:不一定正确)*/
                        else if (remoteLength < localLength) {
                            mFtpClient.upload(localFile.getName(), input, remoteLength, remoteLength, new UploadFTPDataTransferListener(info, localLength, remoteLength));
                            //mFtpClient.append(localFile.getName(), input, remoteLength, new UploadFTPDataTransferListener(info, localLength, remoteLength));
                        }
                        /**3:存在需要上传的文件且大于需要上传的文件(这里判定为文件有变化，以本地文件为准)*/
                        else {
                            String deletePath = "";
                            if (info.getSaveRemotePath().endsWith("/"))
                                deletePath += remoteFile.getName();
                            else
                                deletePath += "/" + remoteFile.getName();
                            mFtpClient.deleteFile(deletePath);
                            mFtpClient.upload(localFile.getName(), input, 0, 0, new UploadFTPDataTransferListener(info, localLength, 0));
                        }
                    }
                } catch (FTPIllegalReplyException e) {
                    error(e, info, "FTPUploadUtil#readyToUpload():" + e.toString());
                    return CHECK_ERROR;
                } catch (FTPException e) {
                    error(e, info, "FTPUploadUtil#readyToUpload():" + e.toString());
                    return CHECK_ERROR;
                } catch (FTPDataTransferException e) {
                    e.printStackTrace();
                    /**
                     * 在这个异常抛出之前{@link FTPClient#upload(String, InputStream, long, long, FTPDataTransferListener)}
                     * 会去先调用{@link FTPDataTransferListener#failed()方法},为了避免重复，
                     * 所以这里并没有调用{@link FTPUploadStatusListener#error(FTPUploadInfo, String)}
                     * */
                    return CHECK_ERROR;
                } catch (FTPAbortedException e) {
                    e.printStackTrace();
                    /**
                     * 在这个异常抛出之前{@link FTPClient#upload(String, InputStream, long, long, FTPDataTransferListener)}
                     * 会去先调用{@link FTPDataTransferListener#aborted()方法},为了避免重复，
                     * 所以这里并没有调用{@link FTPUploadStatusListener#error(FTPUploadInfo, String)}
                     * */
                    return CHECK_ERROR;
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                error(e, info, "FTPUploadUtil#readyToUpload():" + e.toString());
                return CHECK_ERROR;
            } catch (Exception e) {
                error(e, info, "FTPUploadUtil#readyToUpload():" + e.toString());
                return CHECK_ERROR;
            }finally {
                try {
                    if (input != null)
                        input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return CHECK_OK;
    }


    /**
     * 检测目录是否相等
     * <>
     * 注:这个校验方式，仅仅适用于本公司的服务器(而且逻辑不合理)
     * </>
     *
     * @param info FTP文件上传对象
     */
    private int checkDirEqualByandaftp(FTPUploadInfo info) {
        try {
            Log.e(TAG, "checkDirEqualByandaftp: " + info.toString());
            //这里要求保存的路径为 "/警号"
            String savePath = info.getSaveRemotePath();
            String curDir = mFtpClient.currentDirectory();
            String[] valueSave = savePath.split("/");
            String policeNumber = valueSave[1];
            d("valuesSave:" + policeNumber + " curDir:" + curDir);
            if (curDir.contains(policeNumber)) {
                return CHECK_OK;
            } else {
                switch (checkFileName(policeNumber)) {
                    case CHECK_OK:
                        return changeRemoteDir(curDir + "/" + policeNumber) ? CHECK_OK : CHECK_ERROR;
                    case CHECK_NO:
                        if (createRemoteDir(policeNumber)) {
                            return changeRemoteDir(curDir + "/" + policeNumber) ? CHECK_ERROR : CHECK_OK;
                        } else {
                            return CHECK_ERROR;
                        }
                    case CHECK_ERROR:
                        return CHECK_ERROR;
                }
            }
        } catch (Exception e) {
            error(e, info, "FTPUploadUtil#checkDirEqualByandaftp " + e.toString());
            return CHECK_ERROR;
        }
        return CHECK_NO;
    }

    /**
     * 检测目录是否相等
     *
     * @param info FTP文件上传对象
     */
    @CheckCode
    private int checkDirEqual(FTPUploadInfo info) {
        try {
            e("checkDirEqual:" + info.toString());
            String savePath = info.getSaveRemotePath();
            String curDir = mFtpClient.currentDirectory();
            d("checkDirEqual: savePath:" + savePath + " curDir:" + curDir);
            if (savePath.equals("/")) {
                return curDir.equals("/") ? CHECK_OK : CHECK_NO;
            } else {
                if (curDir.equals("/")) {
                    return CHECK_NO;
                } else {
                    /**FTPClient获取到的文件目录在不为根目录的情况下，最后面没有文件分割符*/
                    if (savePath.endsWith("/")) {
                        return savePath.equals(curDir + "/") ? CHECK_OK : CHECK_NO;
                    } else {
                        return savePath.equals(curDir) ? CHECK_OK : CHECK_NO;
                    }
                }
            }
        } catch (Exception e) {
            error(e, info, "FTPUploadUtil#checkDirEqual():" + e.toString());
            return CHECK_ERROR;
        }
    }

    /**
     * 根据从FTP文件上传对象中获取到的保存路径信息，
     * 将FTPClient客户端对象切换到指定的保存路径中.
     * (服务器中路径不存在会重新创建)
     *
     * @param info FTP文件上传对象
     */
    @CheckCode
    private int checkAndChangeRemoteDir(FTPUploadInfo info) {

        try {
            d("checkAndChangeRemoteDir "+info.toString());
            String[] saveDirs = info.getSaveRemotePath().split("/");//得到的路径数组第一个可能为""
            String curDir = mFtpClient.currentDirectory();
            String buildDir = "/";
            //如果当前目录不为根目录，切换到根目录(这里所指示的根目录是，FTP客户端能访问到最上层目录)
            if (!curDir.equals("/")) {
                if (!changeRemoteDir("/"))
                    return CHECK_ERROR;
            }
            d("checkUploadFile: 检测前:curDir:" + mFtpClient.currentDirectory());
            for (int i = 0; i < saveDirs.length; i++) {
                d("------------循环开始----------" + (i + 1));
                if (!saveDirs[i].equals("")) {
                    buildDir += saveDirs[i] + "/";
                    d(buildDir);
                    switch (checkFileName(saveDirs[i])) {
                        case CHECK_ERROR:
                            return CHECK_ERROR;
                        case CHECK_OK:
                            if (changeRemoteDir(buildDir)) {
                                continue;
                            } else {
                                return CHECK_ERROR;
                            }
                        case CHECK_NO:
                            if (!createRemoteDir(saveDirs[i])) {
                                return CHECK_ERROR;
                            } else {
                                if (!changeRemoteDir(buildDir)) {
                                    return CHECK_ERROR;
                                }
                            }
                    }
                }
            }
            d("checkUploadFile: 检测后:curDir:" + mFtpClient.currentDirectory());

        } catch (Exception e) {
            error(e, info, "FTPUploadUtil#checkAndChangeRemoteDir():" + e.toString());
            return CHECK_ERROR;
        }
        return CHECK_OK;
    }

    /**
     * 在服务器的当前目录下创建新的目录
     * <p>
     * 注:如果传入的是带有路径的名字,名字前面的路径必须存在，否则会创建失败.
     * 所以推荐直接传入需要创建的目录名比较合适.
     * </p>
     *
     * @param dirName 创建的目录名字
     */

    private boolean createRemoteDir(@NonNull String dirName) {
        try {
            d("createRemoteDir: 创建前当前目录:" + mFtpClient.currentDirectory());
            d("createRemoteDir: 创建目录名字:" + dirName);
            mFtpClient.createDirectory(dirName);
        } catch (Exception e) {
            e("createRemoteDir: 创建目录失败");
            error(e, null, TAG + " createRemoteDir():" + e.toString());
            return false;
        }
        d("createRemoteDir: 创建目录成功");
        return true;
    }

    /**
     * 将服务器中的目录切换到指定目录
     */
    private boolean changeRemoteDir(@NonNull String path) {
        try {
            mFtpClient.changeDirectory(path);
            d("changeRemoteDir: 切换后目录:" + mFtpClient.currentDirectory());
        } catch (Exception e) {
            e("changeRemoteDir: 切换目录失败");
            error(e, null, TAG + " changeRemoteDir():" + e.toString());
            return false;
        }
        return true;
    }

    @IntDef({CHECK_ERROR, CHECK_OK, CHECK_NO})
    @Retention(RetentionPolicy.SOURCE)
    private @interface CheckCode {

    }

    private static final int CHECK_ERROR = 0;  // 操作时FTP抛出异常(比如网络丶配置等出现问题)
    private static final int CHECK_OK = 1;     // 检测相符合(或找到当前文件)
    private static final int CHECK_NO = 2;     // 检测不符合(或没有当前文件)

    /**
     * 检测FTP服务器中的当前目录中是否包含指定的目录或文件
     *
     * @param name 指定的目录或文件
     * @return 检测状态值
     */
    @CheckCode
    private int checkFileName(@NonNull String name) {
        try {
            FTPFile[] files = mFtpClient.list();
            return checkFileName(files, name) != null ? CHECK_OK : CHECK_NO;
        } catch (Exception e) {
            error(e, null, TAG + " checkFileName():" + e.toString());
            return CHECK_ERROR;
        }
    }

    /**
     * 检测从FTP服务器获取到的文件数组中是否包含指定名字的文件或目录
     *
     * @param files FTP服务器中获取到的当前目录下的所有文件
     * @param name  匹配的文件或目录名
     * @return 不为null:与之匹配的FTPFile对象  为null:则服务器没有与之匹配的文件(目录)
     */
    private FTPFile checkFileName(@NonNull FTPFile[] files, @NonNull String name) {
        for (FTPFile f : files) {
            if (name.equals(f.getName()))
                return f;
        }
        return null;
    }

    private class UploadFTPDataTransferListener implements FTPDataTransferListener {

        private int totalTransferred = 0;
        private long mFileSize = -1;
        private float lastPercent = 0.0f;
        private FTPUploadInfo info;
        /**
         * 当进度传输的变化百分比大等于该值时，才将状态传递出去
         * 避免在实现对应接口刷新UI时，频率过快(与进度的显示精确度也相关)
         */
        private static final float RANGE = 0.01f;

        UploadFTPDataTransferListener(FTPUploadInfo info, long fileSize/*上传文件的大小*/, long startAt/*上传文件的起始位置*/) {
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
            statusUpdate(UPLOAD_STATUS_START, info);
        }

        @Override
        public void transferred(int length) {
            totalTransferred += length;
            float percent = (float) totalTransferred / mFileSize;
            d("transferred: 当前进度:" + percent + " 上传进度变化:" + Math.abs(percent - lastPercent) + " 传递字节数:" + length);
            if (mStatusListener != null) {
                if (Math.abs(percent - lastPercent) >= RANGE || percent == 1) {
                    if (info != null) {
                        info.setProgress(percent);
                        info.setStatus(UPLOAD_STATUS_TRANSFERRED);
                    }
                    mStatusListener.statusUpdate(UPLOAD_STATUS_TRANSFERRED, info);
                    lastPercent = percent;
                }
            }
        }

        @Override
        public void completed() {
            statusUpdate(UPLOAD_STATUS_COMPLETED, info);
        }

        @Override
        public void aborted() {
            statusUpdate(UPLOAD_STATUS_ABORTED, info);
        }

        @Override
        public void failed() {
            statusUpdate(UPLOAD_STATUS_FAILED, info);
        }
    }

    @IntDef({UPLOAD_STATUS_DEFAULT,
            UPLOAD_STATUS_READY,
            UPLOAD_STATUS_START,
            UPLOAD_STATUS_TRANSFERRED,
            UPLOAD_STATUS_COMPLETED,
            UPLOAD_STATUS_ABORTED,
            UPLOAD_STATUS_FAILED,
            UPLOAD_STATUS_NO_NETWORK,
            UPLOAD_STATUS_LOGIN_FAILED,
            UPLOAD_STATUS_LOGIN_SUCCESS,
            UPLOAD_STATUS_FILE_NO_FIND,
            UPLOAD_STATUS_NO_UPLOAD_FILE,
            UPLOAD_STATUS_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UploadStatusCode {

    }


    /**
     * @see #UPLOAD_STATUS_DEFAULT        下载--默认状态(等待下载)
     * @see #UPLOAD_STATUS_READY          下载--准备
     * @see #UPLOAD_STATUS_START          下载--开始      {@link UploadFTPDataTransferListener#started()}
     * @see #UPLOAD_STATUS_TRANSFERRED    下载--进行      {@link UploadFTPDataTransferListener#transferred(int)}
     * @see #UPLOAD_STATUS_COMPLETED      下载--完成      {@link UploadFTPDataTransferListener#completed()}
     * @see #UPLOAD_STATUS_ABORTED        下载--中止(暂停) {@link UploadFTPDataTransferListener#aborted()}
     * @see #UPLOAD_STATUS_FAILED         下载--失败      {@link UploadFTPDataTransferListener#failed()}
     * @see #UPLOAD_STATUS_NO_NETWORK     网络不可用
     * @see #UPLOAD_STATUS_LOGIN_FAILED   登陆失败
     * @see #UPLOAD_STATUS_LOGIN_SUCCESS  登陆成功
     * @see #UPLOAD_STATUS_FILE_NO_FIND   上传文件不存在(根据路径和名字获取到的上传文件不存在)
     * @see #UPLOAD_STATUS_NO_UPLOAD_FILE 文件上传的缓冲队列已经没有文件需要上传
     * @see #UPLOAD_STATUS_ERROR          其它错误
     **/
    public static final int UPLOAD_STATUS_DEFAULT = 0;
    public static final int UPLOAD_STATUS_READY = 1;
    public static final int UPLOAD_STATUS_START = 2;
    public static final int UPLOAD_STATUS_TRANSFERRED = 3;
    public static final int UPLOAD_STATUS_COMPLETED = 4;
    public static final int UPLOAD_STATUS_ABORTED = 5;
    public static final int UPLOAD_STATUS_FAILED = 6;
    public static final int UPLOAD_STATUS_NO_NETWORK = 7;
    public static final int UPLOAD_STATUS_LOGIN_FAILED = 8;
    public static final int UPLOAD_STATUS_LOGIN_SUCCESS = 9;
    public static final int UPLOAD_STATUS_FILE_NO_FIND = 10;
    public static final int UPLOAD_STATUS_NO_UPLOAD_FILE = 11;
    public static final int UPLOAD_STATUS_ERROR = 12;

    public interface FTPUploadStatusListener {
        /**
         * 上传状态更新接口方法
         *
         * @param statusCode 状态码
         * @param info       FTP文件上传对象
         *                   为null:此次没有获取到文件上传对象,如断开连接、登录等操作;
         *                   不为null:此时已经从缓冲队列中获取到上传对象,
         *                   且该最新想上传状态已经写入到了该对象中
         */
        void statusUpdate(@UploadStatusCode int statusCode, @Nullable FTPUploadInfo info);

        /**
         * 其它错误
         *
         * @param info      文件上传对象
         * @param errorInfo 错误日志
         */
        void error(@Nullable FTPUploadInfo info, String errorInfo);

    }

    private FTPUploadUtil.FTPUploadThread mUploadThread;
    private ConcurrentLinkedQueue<FTPUploadInfo> mFtpUploadBuffer = new ConcurrentLinkedQueue<>();
    private volatile boolean mLoginStatus;

    private synchronized void initUploadThread() {
        d("initUploadThread: ");
        if (mUploadThread == null) {
            mUploadThread = new FTPUploadUtil.FTPUploadThread();
            mUploadThread.mIsOpen = true;
            mUploadThread.start();
        }
    }

    private synchronized void releaseUploadThread() {
        d("releaseUploadThread: ");
        if (mUploadThread != null) {
            mUploadThread.mIsOpen = false;
            mUploadThread.interrupt();
            mUploadThread = null;
        }
    }

    /**
     * 当出现网络等问题时，线程的休眠时间
     * <p>
     * 下面这段英文是服务器配置文件中的内容
     * You may change the default value for timing out a data connection.
     * data_connection_timeout=5 (秒)
     * 当出现网络问题，没有上传成功的文件在{data_connection_timeout}时间内会被锁定，
     * 当网络可用时，重新登录进行断点续传如果{data_connection_timeout}时间没有结束是无法进行断点续传的,
     * 所以将该设置设置为5秒，{@link #SLEEP_TIME}的时间一致.
     * <p>
     */
    private static final int SLEEP_TIME = 5000;      //单位:毫秒
    private static final int SLEEP_NO_FILE = 1000;   //没有文件时的线程休眠时间 单位:毫秒
    private static final int SLEEP_NO_NETWORK = 2000;//没有网络时的线程休眠时间 单位:毫秒
    private static final int COUNT = 60;             //在没有上传文件的情况下，检测的次数，达到该次数怎释放线程


    private final class FTPUploadThread extends Thread {
        private boolean mIsOpen;
        private int mCount;

        FTPUploadThread() {
            d("FTPUploadThread: ");
        }

        @Override
        public void run() {
            d("FTPUploadThread run: -------------开始-------------");
            while (mIsOpen) {
                d("FTPUploadThread run: ----------当次循环开始--------");
                try {
                    if (!SystemUtil.isNetworkAvailable(mContext)) {
                        disconnect();
                        statusUpdate(UPLOAD_STATUS_NO_NETWORK, null);
                        sleep(SLEEP_NO_NETWORK);
                        continue;
                    }
                    if (mLoginStatus) {
                        FTPUploadInfo info = mFtpUploadBuffer.peek();
                        if (info != null) {
                            mCount = 0;
                            /**在已经登陆的情况下，判断登陆的FTP账号地址等信息是否与要上传的文件信息相同*/
                            if (info.getFtpAccount().equals(mFtpClient.getUsername())
                                    && info.getFtpPwd().equals(mFtpClient.getPassword())
                                    && info.getFtpIp().equals(mFtpClient.getHost())
                                    && info.getFtpPort() == mFtpClient.getPort()) {
                                /**
                                 * <p>
                                 * 由于服务器的差异，当FTP客户端登录成功时，在没有切换目录的前提下，
                                 * 有时获取到的当前目录是不一样的，所以这里FTPUploadInfo保存的路径
                                 * 会根据{@link mLoginDir}重新生成新的保存目录,已经添加的就不添加了
                                 * <p/>
                                 * <p>
                                 * 注:上面的添加方式会造成，如果想在{@link mLoginDir}的目录下创建和它一样的
                                 * 目录，就会被人为的过滤掉,所以你如果想直接在FTP客户端的根目录"/",进行操作，
                                 * 可以将下面的代码注释掉，然后{@link #connectAndSetCurrentDir()}调用的地方改为
                                 * {@link #connect()}
                                 * <p/>
                                 * <P>
                                 * 在{@link mLoginDir}不为"/"根目录时，由于人为添加了目录前缀，相当于人为的限制了上传的文件
                                 * 的保存路径范围(本来时可以保存到根目录的，但是现在只能保存到{@link mLoginDir}对应的目录下)
                                 * </P>
                                 * */
                                if (mLoginDir != null && !mLoginDir.equals("") && !mLoginDir.equals("/")) {
                                    String oldSavePath = info.getSaveRemotePath();
                                    if (!oldSavePath.regionMatches(0, mLoginDir, 0, mLoginDir.length())) {
                                        info.setSaveRemotePath(mLoginDir + oldSavePath);//mLoginDir后面是没有"/"
                                    }
                                }

                                int code_1 = checkDirEqual(info);
                                //int code_1 = checkDirEqualByandaftp(info);
                                if (code_1 == CHECK_OK) {
                                    int code_2 = readyToUpload(info);
                                    if (code_2 == CHECK_OK) {
                                        mFtpUploadBuffer.remove(info);
                                    } else if (code_2 == CHECK_NO) {
                                        //这里文件没有找到时，处理方式暂时和文件上传完成的方式一样
                                        mFtpUploadBuffer.remove(info);
                                    } else {
                                        disconnect();
                                        sleep(SLEEP_TIME);
                                        continue;
                                    }
                                } else if (code_1 == CHECK_NO) {
                                    int code_3 = checkAndChangeRemoteDir(info);
                                    if (code_3 == CHECK_OK) {
                                        int code_4 = readyToUpload(info);
                                        if (code_4 == CHECK_OK) {
                                            mFtpUploadBuffer.remove(info);
                                        } else if (code_4 == CHECK_NO) {
                                            mFtpUploadBuffer.remove(info);
                                        } else {
                                            disconnect();
                                            sleep(SLEEP_TIME);
                                            continue;
                                        }
                                    } else if (code_3 == CHECK_NO) {
                                        /**
                                         * @see FTPUploadUtil#checkAndChangeRemoteDir(FTPUploadInfo)
                                         * 这个方法不会返回{@link CHECK_NO}这个值
                                         * */
                                    } else {
                                        disconnect();
                                        sleep(SLEEP_TIME);
                                        continue;
                                    }
                                } else {
                                    disconnect();
                                    sleep(SLEEP_TIME);
                                    continue;
                                }
                            } else {
                                disconnect();
                                resetLoginInfo(info);
                                sleep(SLEEP_TIME);
                                continue;
                            }
                        } else {
                            mCount++;
                            if (mCount == COUNT) {
                                disconnect();
                                releaseUploadThread();
                            } else {
                                //e("没有文件上传");
                                statusUpdate(FTPUploadUtil.UPLOAD_STATUS_NO_UPLOAD_FILE, null);
                                sleep(SLEEP_NO_FILE);
                            }
                            continue;
                        }
                    } else {
                        //mLoginStatus = connect();
                        mLoginStatus = connectAndSetCurrentDir();
                        if (!mLoginStatus) {
                            sleep(SLEEP_TIME);
                            continue;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    releaseUploadThread();
                }
                d("FTPUploadThread run: ----------当次循环结束--------");
            }
            d("FTPUploadThread run: -------------结束-------------");
        }
    }
}
