package com.zzx.police.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.caah.mppclient.api.bean.request.BaseRequestBean;
import com.caah.mppclient.api.bean.response.BaseResponseBean;
import com.caah.mppclient.api.net.ApiAsyncTask;
import com.caahtech.mppclientapi_js.MPPClientAPI_JS;
import com.caahtech.mppclientapi_js.ServerTimeBean;
import com.zzx.police.data.FileInfo;
import com.zzx.police.data.StoragePathConfig;
import com.zzx.police.data.Values;
import com.zzx.police.database.HttpDatabaseUtil;
import com.zzx.police.utils.DeviceUtils;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.utils.FTPContinue;
import com.zzx.police.utils.MD5_Police;
import com.zzx.police.utils.SystemUtil;
import com.zzx.police.utils.XMLUtils;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.RequestDate;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseServer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**@author Tomy
 * Created by Tomy on 2015-06-20.
 */
public class WebThread extends Thread implements ApiAsyncTask.ApiRequestListener {
    private static final String TIME_END    = "endtime";
    private static final String TIME_START  = "starttime";
    private static final String FTP_SERVER_ADDRESS = "httpFtpAddress";
    private static final String FTP_SERVER_PORT = "httpFtpPort";
    private static final int START_UPLOAD = 0x11;
    private static final String TAG = "WebThread: ";
    private static final String START_TIME = "starttime";
    private static final String STOP_TIME = "stoptime";
    private static final String ACTION_START_RECORD = "startRecord";
    private static final String ACTION_STOP_RECORD = "stopRecord";
    public final String SERVER_VERSION  = "WebServer/1.1";
    public final String FILE_TYPE       = "targettype";
    public final String FTP_SERVER_URL  = "ftpserverurl";
    public final String FTP_USR_NAME    = "username";
    public final String FTP_USR_PWD     = "password";
    public final String FTP_DIR = "documentno";
    public final String APP_NAME    = "appappname";
    public final String AUTH_CODE   = "authcode";

    public static final int TYPE_ALL    = 0;
    public static final int TYPE_PIC    = 1;
    public static final int TYPE_VIDEO  = 2;
    public static final int FILE_TYPE_VID = 0;
    public static final int FILE_TYPE_IMG = 1;
    public static final int FILE_TYPE_ALL = -1;
    public static final int FILE_TYPE_SND = 2;
    public static final String BUS_HTTP_UPLOAD = "HttpUpload";
    public static final int EVENT_HTTP_UPLOAD = 10;
    private final NetReceiver mNetReceiver;

    private ServerSocket mServerSocket;
    private int mPort = 0;
    public boolean mIsLoop = false;
    private Context mContext;
    private FTPContinue mFtpContinue;
    private int mFileCount = 0;
    private int mEventCount = 0;
    private List<FileInfo> mList;
    private Handler mHandler;
    private String[] mFtp;
    private String mFtpUser;
    private String mFtpPwd;
    private final Object mObject = new Object();
    private boolean mUploadReleased = false;
    private boolean mIsFirst = true;
    private AlarmManager mAlarmManager;
    private SimpleDateFormat mFormat;
    private static final String TIME_FORMAT = "yyyyMMddHHmmss";
    private PendingIntent mPIStart;
    private PendingIntent mPIStop;
    private Receiver mReceiver;

    public WebThread(int port, Context context) {
        super();
        HandlerThread handler = new HandlerThread("web_thread");
        handler.start();
        mHandler = new EventHandler(handler.getLooper());
        mPort = port;
        mContext = context;
        EventBusUtils.registerEvent(BUS_HTTP_UPLOAD, this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetReceiver = new NetReceiver();
        mContext.registerReceiver(mNetReceiver, filter);
        restoreFtpInfo();
        boolean connected = SystemUtil.isWifiNetworkConnected(mContext);
        Values.LOG_E(TAG, "mFtpUser = " + mFtpUser + "; netState = " + connected);
        if (!mFtpUser.equals("") && connected) {
            mHandler.sendEmptyMessageDelayed(START_UPLOAD, 1000);
            MPPClientAPI_JS.uc_getServerTime(context, this, new BaseRequestBean());
//            EventBusUtils.postEvent(BUS_HTTP_UPLOAD, EVENT_HTTP_UPLOAD);
        }
        mFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        initManager();
    }

    private void initManager() {
        if (mAlarmManager == null) {
            mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        }
        Intent startIntent = new Intent(ACTION_START_RECORD);
        Intent stopIntent = new Intent(ACTION_STOP_RECORD);
        mPIStart = PendingIntent.getBroadcast(mContext, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mPIStop = PendingIntent.getBroadcast(mContext, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        IntentFilter filter = new IntentFilter(ACTION_START_RECORD);
        filter.addAction(ACTION_STOP_RECORD);
        mReceiver = new Receiver();
        mContext.registerReceiver(mReceiver, filter);
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case ACTION_START_RECORD:

                    break;
                case ACTION_STOP_RECORD:
                    break;
            }
        }
    }

    @Override
    public void onPreExecute(String s) {

    }

    @Override
    public void onPostExecute(String action, Object obj) {
        if (action.equals(MPPClientAPI_JS.UC_GET_SERVER_TIME)) {
            @SuppressWarnings("unchecked")
            BaseResponseBean<ServerTimeBean> response = (BaseResponseBean<ServerTimeBean>) obj;
            if (response.getCode().equals(ApiAsyncTask.OK)) {
                if (response.getRows().size() == 0) {
                    //getMessageBox().Show("没有获取到服务器时间。", "提示");
                } else {
                    long serverTime = response.getRows().get(0).getServerTime();
                    timeSynchronization(serverTime);
                }
            }
        }

    }

    @Override
    public void onProgress(String s, int i) {

    }

    /**
     * 设置系统时间
     * @param time 系统时间
     */
    public void timeSynchronization(long time) {
        try {
            SystemClock.setCurrentTimeMillis(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*String formatTime = getUTC8Time(time);
        ArrayList<String> envList = new ArrayList<>();
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            envList.add(envName + "=" + env.get(envName));
        }
        String[] envp = envList.toArray(new String[0]);
        String command;
        command = "date -s " + formatTime;
        try {
            Runtime.getRuntime().exec(new String[] { "su", "-c", command }, envp);
        } catch (Exception e) {
            e.printStackTrace();

        }*/
    }

    private String getUTC8Time(long time) {
//		long utc8Time = time - (3600 * 1000 * 8);
        return new SimpleDateFormat("yyyyMMdd.HHmmss", Locale.getDefault()).format(time);
    }


    class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case START_UPLOAD:
                    checkUpload();
                    break;
            }
        }
    }

    class NetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!SystemUtil.isWifiNetworkConnected(context)) {
                mEventCount = 0;
                mFileCount = 0;
            } else {
//                timeSynchronization(System.currentTimeMillis() + 10 * 60 * 1000);
                Values.LOG_E(TAG, "mUploadReleased = " + mUploadReleased + "; mIsFirst = " + mIsFirst);
                MPPClientAPI_JS.uc_getServerTime(context, WebThread.this, new BaseRequestBean());
                if (mUploadReleased || mIsFirst) {
                    mIsFirst = false;
                    mHandler.sendEmptyMessage(START_UPLOAD);
//                    EventBusUtils.postEvent(BUS_HTTP_UPLOAD, EVENT_HTTP_UPLOAD);
                }
            }
        }
    }

    public void release() {
        if (mContext != null && mNetReceiver != null) {
            mContext.unregisterReceiver(mNetReceiver);
            mContext = null;
        }
    }

    public static class FileStatus {
        public String mFileName;
        public boolean mSuccess = false;
        public String mDirName;
        public FileStatus(String fileName, String dirName, boolean success) {
            mFileName = fileName;
            mSuccess = success;
            mDirName = dirName;
        }
        public FileStatus(){}
    }

    @Override
    public void run() {
        super.run();
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
            //创建服务器套接字
            mServerSocket = new ServerSocket(mPort);
            //创建HTTP协议处理器
            BasicHttpProcessor httpPro = new BasicHttpProcessor();
            //创建HTTP协议拦截器
            httpPro.addInterceptor(new RequestDate());
            httpPro.addInterceptor(new ResponseServer());
            httpPro.addInterceptor(new ResponseContent());
            httpPro.addInterceptor(new ResponseConnControl());
            //创建HTTP服务
            HttpService httpService = new HttpService(httpPro,
                    new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory());
            //创建HTTP参数
            HttpParams params = new BasicHttpParams();
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, SERVER_VERSION);
            //设置HTTP参数
            httpService.setParams(params);
            //创建HTTP请求执行器注册表
            HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
            registry.register("*query", new FtpControl());
            registry.register("*getStatus", new GetStatus());
            registry.register("*startVideo", new StartVideo());
            registry.register("*stopVideo", new StopVideo());
            //设置HTTP请求执行器
            httpService.setHandlerResolver(registry);

            /** 循环接收客户端 */
            mIsLoop = true;
            while (mIsLoop && !interrupted()) {
                //接收客户端套接字
                Socket socket = mServerSocket.accept();
                //绑定到服务器端HTTP连接
                DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
                connection.bind(socket, params);
                //派送至新线程处理请求
                Thread workerThread = new WorkerThread(httpService, connection);
                workerThread.setDaemon(true);
                workerThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                EventBusUtils.unregisterEvent(BUS_HTTP_UPLOAD, this);
                if (mServerSocket != null) {
                    mServerSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FtpControl implements HttpRequestHandler {
        public FtpControl() {}

        @Override
        public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            RequestLine requestLine = httpRequest.getRequestLine();
            String method = requestLine.getMethod();
            if ("GET".equals(method)) {
                doGet(requestLine, httpResponse);
            }
        }

        private void doGet(final RequestLine requestLine, final HttpResponse response) throws UnsupportedEncodingException {
            if (!CameraService.mReceiveStartRecordFromClient) {
                CameraService.mReceiveStartRecordFromClient = true;
            }
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RECEIVE_HTTP_UPLOAD);
            response.setStatusCode(HttpStatus.SC_OK);
            StringEntity entity = new StringEntity(XMLUtils.createXML(DeviceUtils.getPoliceNum(mContext), 1, false), HTTP.UTF_8);
            response.setEntity(entity);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        String urlCmd = requestLine.getUri();
                        Map<String, String> paramMap = getParam(urlCmd);
                        String ftpServer = paramMap.get(FTP_SERVER_URL);
                        mFtpUser = paramMap.get(FTP_USR_NAME);
                        mFtpPwd = paramMap.get(FTP_USR_PWD);
                        mFtp = ftpServer.split(":");
                        storeFtpInfo();
                        String ftpDirName = "/" + paramMap.get(FTP_DIR);
                        /*String startTime = paramMap.get(TIME_START);
                        String endTime = paramMap.get(TIME_END);
                        String type = paramMap.get(FILE_TYPE);
                        type = String.valueOf(reserveType(Integer.parseInt(type)));
                        String createDate = startTime.substring(0, 8);
                        startTime = startTime.substring(8);
                        endTime = endTime.substring(8);*/
                        String filePath = Settings.System.getString(mContext.getContentResolver(), Values.HTTP_UPLOAD_PATH);
                        Settings.System.putString(mContext.getContentResolver(), Values.HTTP_UPLOAD_PATH, "");
                        if (filePath != null && !filePath.equals("")) {
                            FileInfo info = new FileInfo();
                            info.mFilePath = filePath;
                            info.mFileName = new File(filePath).getName();
                            info.mDirName = ftpDirName;
                            HttpDatabaseUtil.insertHistory(mContext, info);
                            if (mList == null) {
                                mList = new ArrayList<>();
                            }
                            mList.add(info);
                        }
                        /*if (type.equals(FILE_TYPE_ALL + "")) {
                            List<FileInfo> imgList = DatabaseUtils.queryHistory(mContext, FILE_TYPE_IMG + "", createDate, startTime, endTime, ftpDirName);
                            List<FileInfo> vidList = DatabaseUtils.queryHistory(mContext, FILE_TYPE_VID + "", createDate, startTime, endTime, ftpDirName);
                            HttpDatabaseUtil.insertHistory(mContext, imgList);
                            HttpDatabaseUtil.insertHistory(mContext, vidList);
                            synchronized (mObject) {
                                if (mList == null) {
                                    mList = imgList;
                                } else if (imgList != null) {
                                    mList.addAll(imgList);
                                }
                                mList.addAll(vidList);
                            }
                        } else {
                            synchronized (mObject) {
                                List<FileInfo> list = DatabaseUtils.queryHistory(mContext, type, createDate, startTime, endTime, ftpDirName);
                                if (mList == null) {
                                    mList = list;
                                } else {
                                    mList.addAll(list);
                                }
                                HttpDatabaseUtil.insertHistory(mContext, list);
                            }
                        }*/
                        startUpload();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 2000);

        }
    }

    private void storeFtpInfo() {
        if (mContext != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(FTP_USR_NAME, mFtpUser).putString(FTP_USR_PWD, mFtpPwd).putString(FTP_SERVER_ADDRESS, mFtp[0]).putInt(FTP_SERVER_PORT, Integer.parseInt(mFtp[1])).apply();
        }
    }

    private void restoreFtpInfo() {
        if (mContext != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            mFtpUser = preferences.getString(FTP_USR_NAME, "");
            mFtpPwd = preferences.getString(FTP_USR_PWD, "");
            mFtp = new String[2];
            mFtp[0] = preferences.getString(FTP_SERVER_ADDRESS, "");
            mFtp[1] = String.valueOf(preferences.getInt(FTP_SERVER_PORT, 21));
        }
    }

    private void startUpload() {
        Values.LOG_E(TAG, "mList.size = " + mList.size());
        if (mList != null && mList.size() > 0) {
            mFileCount = mList.size();
            if (mFtpContinue == null || mFtpContinue.mReleased) {
                mUploadReleased = false;
                mFtpContinue = null;
                mFtpContinue = new FTPContinue(mContext, null, mFtp[0], Integer.parseInt(mFtp[1]), mFtpUser, mFtpPwd);
                FileInfo info = mList.get(0);
                String filePath = !info.mFilePath.equals("") ? info.mFilePath : getFileName(info.mFileType, info.mCreateDate, info.mFileName);
                Values.LOG_E(TAG, "upload fileName = " + filePath);
                mFtpContinue.upload(filePath, info.mDirName);
            }
        }
    }

    void onEventBackgroundThread(Integer event) {
        synchronized (mObject) {
            mList = HttpDatabaseUtil.queryHistory(mContext);
        }
        startUpload();
        /*try {
            if ((++mEventCount) >= mFileCount) {
                releaseUpload();
            } else if (mList != null && mList.size() > mEventCount) {
                FileInfo info = mList.get(mEventCount);
                mFtpContinue.upload(getFileName(info.mFileType, info.mCreateDate, info.mFileName), info.mDirName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void checkUpload() {
        if (SystemUtil.isNetworkConnected(mContext)) {
            synchronized (mObject) {
                mList = HttpDatabaseUtil.queryHistory(mContext);
            }
            startUpload();
        }
    }

    void onEventBackgroundThread(FileStatus status) {
        try {
            if (status.mSuccess) {
                HttpDatabaseUtil.deleteHistory(mContext, status.mFileName, status.mDirName);
            }
            if ((++mEventCount) >= mFileCount) {
                releaseUpload();
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(START_UPLOAD, 5 * 1000);
                }
            } else if (mList != null && mList.size() > mEventCount) {
                FileInfo info = mList.get(mEventCount);
                String filePath = !info.mFilePath.equals("") ? info.mFilePath : getFileName(info.mFileType, info.mCreateDate, info.mFileName);
                mFtpContinue.upload(filePath, info.mDirName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**@see #startUpload()
     * */
    private void releaseUpload() {
        mEventCount = 0;
        mFileCount = 0;
        mUploadReleased = true;
        if (mFtpContinue != null) {
            mFtpContinue.release();
        }
        if (mList != null) {
            mList.clear();
            mList = null;
        }
    }

    private String getFileName(int type, String createDate, String fileName) {
        String dirPath = "/";
        switch (type) {
            case Values.FILE_TYPE_IMG:
                dirPath = StoragePathConfig.getPicDirPath(mContext) + File.separator + createDate;
                break;
            case Values.FILE_TYPE_VID:
                dirPath =StoragePathConfig.getVideoDirPath(mContext)+ File.separator + createDate;
                break;
            case Values.FILE_TYPE_SND:
                dirPath = StoragePathConfig.getSoundDirPath(mContext) + File.separator + createDate;
                break;
        }
        return new File(dirPath, fileName).getAbsolutePath();
    }

    private int reserveType(int type) {
        switch (type) {
            case TYPE_PIC:
                return FILE_TYPE_IMG;
            case TYPE_VIDEO:
                return FILE_TYPE_VID;
        }
        return FILE_TYPE_ALL;
    }

    private String getCheckCode(String[] args) {
        String checkCode = "";
        for (String arg : args) {
            checkCode += (arg + "|");
        }
        return MD5_Police.encrypt(checkCode + "Aa9sdDkkdialaij39z=sdfj39dkahdsdfl").substring(0, 16);
    }

    private class GetStatus implements HttpRequestHandler {
        public GetStatus() {}

        @Override
        public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            RequestLine requestLine = httpRequest.getRequestLine();
            String method = requestLine.getMethod();
            if ("GET".equals(method)) {
                doGet(requestLine, httpResponse);
            }
        }

        private void doGet(RequestLine requestLine, HttpResponse response) throws UnsupportedEncodingException {
            response.setStatusCode(HttpStatus.SC_OK);
            StringEntity entity = new StringEntity(XMLUtils.createXML(DeviceUtils.getPoliceNum(mContext), 1, true), HTTP.UTF_8);
            response.setEntity(entity);
            /*if (!CameraService.mReceiveStartRecordFromClient) {
                CameraService.mReceiveStartRecordFromClient = true;
            }*/
            /*EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RECEIVE_START_RECORD);*/
        }
    }

    private void setAlarm(long time, String type) {
        if (mAlarmManager == null || mContext == null) {
            return;
        }
        switch (type) {
            case START_TIME:
                mAlarmManager.cancel(mPIStart);
                mAlarmManager.set(AlarmManager.ELAPSED_REALTIME, time, mPIStart);
                break;
            case STOP_TIME:
                mAlarmManager.cancel(mPIStop);
                mAlarmManager.set(AlarmManager.ELAPSED_REALTIME, time, mPIStop);
                break;
        }
    }

    private class StartVideo implements HttpRequestHandler {
        public StartVideo() {}

        @Override
        public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            RequestLine requestLine = httpRequest.getRequestLine();
            String method = requestLine.getMethod();
            if ("GET".equals(method)) {
                doGet(requestLine, httpResponse);
            }
        }

        private void doGet(RequestLine requestLine, HttpResponse response) throws UnsupportedEncodingException {
            if (!CameraService.mReceiveStartRecordFromClient) {
                CameraService.mReceiveStartRecordFromClient = true;
            }
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RECEIVE_START_RECORD);
            response.setStatusCode(HttpStatus.SC_OK);
            StringEntity entity = new StringEntity(XMLUtils.createXML(DeviceUtils.getPoliceNum(mContext), 1, true), HTTP.UTF_8);
            response.setEntity(entity);
//            decodeParam(requestLine, START_TIME);
        }
    }

    private void decodeParam(RequestLine requestLine, String action) {
        String uri = requestLine.getUri();
        Map<String, String> paramMap = getParam(uri);
        if (paramMap != null) {
            String startTime = paramMap.get(action);
            try {
                Date date = mFormat.parse(startTime);
                setAlarm(date.getTime(), action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class StopVideo implements HttpRequestHandler {
        public StopVideo() {}

        @Override
        public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            RequestLine requestLine = httpRequest.getRequestLine();
            String method = requestLine.getMethod();
            if ("GET".equals(method)) {
                doGet(requestLine, httpResponse);
            }
        }

        private void doGet(RequestLine requestLine, HttpResponse response) throws UnsupportedEncodingException {
            response.setStatusCode(HttpStatus.SC_OK);
            StringEntity entity = new StringEntity(XMLUtils.createXML(DeviceUtils.getPoliceNum(mContext), 1, true), HTTP.UTF_8);
            response.setEntity(entity);
//            decodeParam(requestLine, STOP_TIME);
            if (CameraService.mReceiveStartRecordFromClient) {
                CameraService.mReceiveStartRecordFromClient = false;
            }
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RECEIVE_STOP_RECORD);
        }
    }

    private Map<String, String> getParam(String urlCmd) {
        if (urlCmd == null || urlCmd.equals("")) {
            return null;
        }
        Map<String, String> paramMap = new HashMap<>();
        try {
            //截取?之后的参数字串
            String cmd = urlCmd.substring(urlCmd.indexOf("?") + 1);
            String[] array = cmd.split("&");
            for (String param : array) {
                String[] keyValue = param.split("=");
                if (keyValue.length < 2) {
                    paramMap.put(keyValue[0], "");
                } else {
                    paramMap.put(keyValue[0], keyValue[1]);
                }
            }
            return paramMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    class WorkerThread extends Thread {
        public WorkerThread(HttpService httpService, HttpServerConnection conn) {
            super();
            mHttpService = httpService;
            mConn = conn;
        }

        private HttpService mHttpService;
        private HttpServerConnection mConn;

        @Override
        public void run() {
            super.run();
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!interrupted() && mConn.isOpen()) {
                    mHttpService.handleRequest(mConn, context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mConn.shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
