package com.zzx.police.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zzx.libzzxdemo.audio.AudioTransManager;
import com.zzx.libzzxdemo.audio.AudioValue;
import com.zzx.police.MainActivity;
import com.zzx.police.ZZXConfig;
import com.zzx.police.command.BaseProtocol;
import com.zzx.police.command.VehicleAudioPassRegister;
import com.zzx.police.command.VehicleAudioRegister;
import com.zzx.police.command.VehicleCmdReply;
import com.zzx.police.command.VehicleFileUpload;
import com.zzx.police.command.VehicleFileUploadRegister;
import com.zzx.police.command.VehicleGpsReport;
import com.zzx.police.command.VehicleReceiveAudio;
import com.zzx.police.command.VehicleRegister;
import com.zzx.police.command.VehicleRequestVideoOrAudio;
import com.zzx.police.command.VehicleSOSReport;
import com.zzx.police.command.VehicleStatus;
import com.zzx.police.command.VehicleUploadRequest;
import com.zzx.police.command.VehicleVideoAuidoRegister;
import com.zzx.police.command.VehicleVideoPassRegister;
import com.zzx.police.data.FTPDownLoadInfo;
import com.zzx.police.data.FTPUploadInfo;
import com.zzx.police.data.IntentInfo;
import com.zzx.police.data.ServiceInfo;
import com.zzx.police.data.UploadInfo;
import com.zzx.police.data.Values;
import com.zzx.police.data.VehicleCmd;
import com.zzx.police.services.CameraService;
import com.zzx.police.services.FTPDownService;
import com.zzx.police.services.SOSService;
import com.zzx.police.services.TalkBackService;
import com.zzx.police.task.DatabaseQueryTask;
import com.zzx.police.webrtc.FastModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**@author Tomy
 * Created by Tomy on 2014/5/19.
 */
public class ProtocolUtils implements CameraService.OnCameraTypeChangeListener{
    private static final String TAG = BaseProtocol.TAG;

    private static final String BroadAction = "com.zzx.protocol";
    private static final String BroadCmd = "flag";
    private static final String BroadExtra = "command";
    private static final String BroadStatus = "status";

    private static final String Release = "release";
    private static final boolean DEBUG = true;
    private void sendBroadCast(String flag, String msg) {
        Intent intent = new Intent();
        intent.setAction(BroadAction);
        intent.putExtra(BroadCmd, flag);
        intent.putExtra(BroadExtra, msg);
        if (mContext != null) {
            mContext.sendBroadcast(intent);
        }
    }

    private void sendBroadRelease() {
        Intent intent = new Intent();
        intent.setAction(BroadAction);
        intent.putExtra(BroadStatus, Release);
        if (mContext != null) {
            mContext.sendBroadcast(intent);
        }
    }
    private Handler mHandler = null;
    /**Cmd
     * */
    private static Socket mClientCmd = null;//通信Socket Client
    private static OutputStream mOutputStreamCmd = null;//通信Socket  输出流
    private InputStream mInputStreamCmd = null;//通信Socket 输入流
    //判断是否是音视频时的音频，还是通话的音频
    private boolean mVideoAudioReport;

    private Timer mHeardBeatTimer = null;
    private SharedPreferences mSp = null;
    private int mGpsReportTotalCount = 0;
    private int mGpsReportCount = 0;
    private String mServiceIP = "";
    private int mPort = 0;
    private Context mContext = null;
    private FTPContinue mFtpContinue = null;
    private int mInterval = 0;
    private int interval = 0;
    private int mTimeNeedReset = 0;
    private boolean mReadThreadCreated = false;
    private boolean mNeedGps = false;
    /**
     * 视音频
     */
    private Socket mSocketVideoAudio = null;
    private OutputStream mOutStreamVideoAudio = null;
    private InputStream mInStreamVideoAudio = null;
    /**Real Time Video
     * */
    private Socket mSocketVideo = null;
    private OutputStream mOutStreamVideo = null;
    private InputStream mInStreamVideo = null;
    /**Talk Back
     * */
    private Socket mSocketAudioReport;//点击OK 2 中心发起监听0
    private Socket mSocketAudioReceive;//音频Socket  //对讲指令 1
    private OutputStream mOutStreamAudio;//音频Socket输出流
    private boolean isEnableUDP = false;//是否使用UDP进行音频传输
    private AudioSocketCreateThread2 audioThread;
    protected DatagramSocket datagramSocket;

    private int mTimeNeedGps = 0;
    private VehicleUploadRequest mUploadRequest = null;
    private VehicleCmdReply mCmdReply = null;
    private VehicleAudioRegister mAudioReply = null;
    private Executor mExecutor = Executors.newFixedThreadPool(7);
    private boolean mClientCreated =false;
    private boolean mReleased = false;
    private boolean mNeedBeat = false;
    /**表明与服务端是否正在语音交互,从而避免再次申请语音.
     * */
    private boolean mAudioReceivePassing = false;
    private boolean mAudioReportPassing = false;
    public boolean mVideoAudioReportPassing = false;
    private String mAudioReceiveUUID;
    private boolean mRegisterSuccess = false;
    /**表明语音/视频正在申请中,再未返回结果前不能再申请.
     * */
    public boolean mAudioRequesting = false;
    public boolean mVideoRequesting = false;
    private PowerManager.WakeLock mRealWakelock;
    private PowerManager mPowerManager;
    private boolean mWakeLocked = false;
    private String portStr = "";
    public static boolean mCenterUpload = false;//中心请求上传

    public ProtocolUtils(Context context, Handler handler) {
        mContext = context;
        ContentResolver resolver = mContext.getContentResolver();
        mServiceIP = Settings.System.getString(resolver, Values.SERVICE_IP_ADDRESS);
        String portStr = Settings.System.getString(resolver, Values.SERVICE_IP_PORT);
        Values.LOG_I(TAG, "ip = " + mServiceIP + "; port = " + portStr);
        if (mServiceIP != null && portStr != null && !mServiceIP.equals("") && !portStr.equals("")) {
            try {
                mPort = Integer.parseInt(portStr);
            } catch (Exception e) {
                return;
            }
        } else {
            XMLParser parser = XMLParser.getInstance();
            ServiceInfo info = parser.parserXMLFile("/etc/service.xml");
            parser.release();
            if (info != null) {
                mServiceIP = info.mServiceIp;
                mPort = info.mPort;
                Settings.System.putString(resolver, Values.SERVICE_IP_ADDRESS, mServiceIP);
            }
        }
        if (mServiceIP == null || mServiceIP.equals("") || mPort == 0) {
            release();
            return;
        }
        if (handler == null) {
            mHandler = new Handler();
        } else {
            this.mHandler = handler;
        }
        mSp = mContext.getSharedPreferences(Values.PREFERENCE_NAME, Context.MODE_PRIVATE);
        createSocket();
        setTimer();
    }

    /**通过 {@link #mHandler}来发送消息.
     * @param msg 若msg不为null,则直接发送msg.
     * @param what 若msg为null,则只发送emptyMessage.
     * @param delay 若delay不为0则延迟发送.
     * */
    private void sendMessage(Message msg, int what, long delay) {
        if (mHandler == null) {
            return;
        }
        if (msg != null) {
            if (delay == 0)
                mHandler.sendMessage(msg);
            else
                mHandler.sendMessageDelayed(msg, delay);
        } else if (delay == 0){
            mHandler.sendEmptyMessage(what);
        } else {
            mHandler.sendEmptyMessageDelayed(what, delay);
        }
    }

    private void setTimer() {
        if (mHeardBeatTimer == null) {
            mHeardBeatTimer = new Timer();
            mHeardBeatTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if ((mReleased || mClientCreated) && ++mTimeNeedReset >= 30) {
                        resetSocket();
                        mTimeNeedReset = 0;
                    }
                    if (mClientCreated && mInterval != 0 && ++interval >= mInterval && mNeedBeat) {
                        sendCommand(BaseProtocol.HEAD_BEAT);
                        interval = 0;
                    }
                    if (mClientCreated && ++mTimeNeedGps >= 10 && mNeedGps) {
                        reportGps();
                        mTimeNeedGps = 0;
                    }
                }
            }, 1000, 1000);
        }
    }


    public void createSocket() {
        if (mClientCmd == null) {
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    InetAddress service = null;
                    try {
                        service = Inet4Address.getByName(mServiceIP);
                        mClientCmd = new Socket(service, mPort);
                        mClientCmd.setReuseAddress(true);
                        mClientCmd.setKeepAlive(true);
                        mOutputStreamCmd = mClientCmd.getOutputStream();
                        mInputStreamCmd = mClientCmd.getInputStream();
                    } catch (Exception e) {
                        Values.LOG_E(TAG, "createSocket Failed");
                        e.printStackTrace();
                        resetSocket();
                        return;
                    }
                    Values.LOG_E(TAG, "SocketConnected");
                    sendMessage(null, Values.SOCKET_CONNECTED, 0);
                    EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.SOCKET_CONNECTED);
                }
            }).start();*/
            mExecutor.execute(new CommSocketCreateThread());
        }
    }

    /**信令通道Socket创建线程
     * */
    class CommSocketCreateThread extends Thread {
        @Override
        public void run() {
            SocketAddress service;
            try {
                Values.LOG_I(TAG, "connect Service.ip = " + mServiceIP + "; port = " + mPort);
                service = new InetSocketAddress(mServiceIP, mPort);
                mClientCmd = new Socket();
                mClientCmd.bind(null);
                mClientCmd.connect(service, 5 * 1000);
                mClientCmd.setReuseAddress(true);
                mClientCmd.setKeepAlive(true);
                mOutputStreamCmd = mClientCmd.getOutputStream();
                mInputStreamCmd = mClientCmd.getInputStream();
            } catch (Exception e) {
                Values.LOG_E(TAG, "createSocket Failed");
                e.printStackTrace();
                resetSocket();
                return;
            }
            Values.LOG_E(TAG, "SocketConnected");
            sendMessage(null, Values.SOCKET_CONNECTED, 0);
            mClientCreated = true;
            EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.SOCKET_CONNECTED);
        }
    }

    public boolean isSocketCreated() {
        return mClientCreated && mRegisterSuccess;
    }

    class CommSocketReadThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len;
            if (mClientCmd == null || mClientCmd.isClosed() || !mClientCmd.isConnected() || mInputStreamCmd == null) {
                Values.LOG_E(TAG, "mClientCmd == null || mClientCmd.isClosed()");
                return;
            }
            mReadThreadCreated = true;
            mReleased = false;
            Values.LOG_W(TAG, "mInputStreamCmd.read(buffer)");
            try {
                while ((len = mInputStreamCmd.read(buffer)) != -1) {
                    Values.LOG_I(TAG, "getBuffer = " + len);
                    mTimeNeedReset = 0;
                    String info = new String(buffer, 0, len, "UTF-8");
                    parserMsg(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
                resetSocket();
            }

        }
    }

    private void resetSocket() {
        Values.LOG_I(TAG, "resetSocket()");
        reset();
        EventBusUtils.postEvent(Values.BUS_EVENT_RESET_SOCKET, Values.SOCKET_RECONNECTION);
    }

    public void startReceive() {
        if (!mReadThreadCreated) {
            mExecutor.execute(new CommSocketReadThread());
        }
    }

    public void closeSocket() {
        if (mClientCmd == null) {
            return;
        }
        try {
            mClientCmd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reportGps() {
        String com;
        VehicleGpsReport v30 = new VehicleGpsReport();
        getVehicleStatus(v30.mVehicleStatus, false);
        com = v30.getCommand();
        sendCommand(com);
    }

    /**发送信令通道指令方法
     * */
    private void sendCommand(String cmd) {
        OutputStreamWriter writer;
        try {
            if (mOutputStreamCmd == null) {
                Values.LOG_E(TAG, cmd + ".sendCommand.mOutputStreamCmd = null");
                return;
            }
            cmd = new String(cmd.getBytes(), "ASCII");
            writer = new OutputStreamWriter(mOutputStreamCmd);
            writer.write(cmd, 0, cmd.length());
            writer.flush();
            Values.LOG_E(TAG, "writeCommand = " + cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int i = 0;
    /**向服务器注册服务
     * */
    public void registerService(String policeNum, String password) {
        if (policeNum != null && !policeNum.equals(Values.POLICE_DEFAULT_NUM)) {
            VehicleRegister v1 = new VehicleRegister();
            v1.mWorkstationSerialNum = password;
            BaseProtocol.mDeviceSerialNum = policeNum;
            v1.IsG726 = ZZXConfig.isG726;
            getVehicleStatus(v1.mVehicleStatus, false);

            if (mAm != null) {
                mAm.release();
            }
            mAm = new AudioTransManager(new AudioTransManager.SendAudio() {
                @Override
                public void sendAudioData(byte[] bytes) {
                    Log.w("lzcdemo", "send audio " + bytes.length);
                    if (mVideoAudioReportPassing) {
                        sendJSON(bytes, 2);
                    } else {
                        try {
                            if (!isEnableUDP) {
                                if (mOutStreamAudio != null && bytes.length > 0) {
                                    mOutStreamAudio.write(bytes);
                                    mOutStreamAudio.flush();
                                }
//                            Log.d("123","tcp----AudioTransManager sendAudioData--------");
                            } else {
//                            Log.d("123","udp----AudioTransManager sendAudioData--------");
                                if ((audioThread != null) && (audioThread.ismIsThreadDisable())) {
//                                Log.d("123","udp----AudioTransManager sendAudioData send--------");
                                    audioThread.sendUDPMessage(bytes);
                                }
                                i++;
//                            StringBuffer buffer=new StringBuffer();
//                            for (int i=0;i<bytes.length;i++)
//                            {
//                                buffer.append( Integer.toHexString(bytes[ i ] & 0xFF));
//                            }
//                            Log.e("234","第"+i+"次发送数据,len:"+bytes.length+"data: "+buffer.toString());
                                Log.d("234", "第" + i + "次发送数据,len:" + bytes.length);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            mAm.endToSend();
                            mAm.endToReceive();
                            releaseAudioReportSocket();
                        }
                    }

                }
            });
            //   mAm.setmContext(mContext);
            mAm.setCodecType(v1.IsG726 ? AudioValue.CodecType.G726 : AudioValue.CodecType.AAC);
            String com = v1.getCommand();
            sendCommand(com);
        }

    }

    /**@see #parserMsg(String)
     * @see #parserCenterFtpInfo(String[])
     * */
    public void sendUpload(String fileName, String fileMD5, int status) {
        if (mUploadRequest == null)
            mUploadRequest = new VehicleUploadRequest();
        getVehicleStatus(mUploadRequest.mVehicleStatus, false);
        mUploadRequest.mStatus      = status;
        mUploadRequest.mFileName    = fileName;
        mUploadRequest.mFileMD5     = fileMD5;
        sendCommand(mUploadRequest.getCommand());
    }

    public static void getVehicleStatus(VehicleStatus status, boolean needStore) {
        if (MainActivity.mLocation == null) {
            status.mGpsStatus = VehicleStatus.GPS_NO;
            status.mLongitude = 00000.0000;
            status.mLatitude  = 0000.0000;
            status.mSpeed = 0;
        } else {
            status.mGpsStatus = VehicleStatus.GPS_OK;
            status.mLongitude = GpsConverter.convertToDegree(MainActivity.mLocation.getLongitude());
            status.mLatitude = GpsConverter.convertToDegree(MainActivity.mLocation.getLatitude());
            status.mSpeed = MainActivity.mLocation.getSpeed();
            status.mDirection = 0;
        }
    }
    private String mUvcResult;       //在实时上传时，如果选定的是{外接     } 摄像头 应该发送的命令
    private String[] mMainCameraCmd; //在实时上传时，如果选定的是{主或者前置} 摄像头 应该发送的命令
    @Override
    public void cameraTypeChange(@Values.CameraType int type) {
        if (type == Values.OPEN_MAIN_CAMERA || type == Values.OPEN_FRONT_CAMERA) {
            if (mMainCameraCmd != null) {
                //  parserCenterRealTimeVideo(mMainCameraCmd);
            }
        } else if (type == Values.OPEN_USB_CAMERA) {
            sendBroadCast(BaseProtocol.CENTER_REAL_TIME_VIDEO, mUvcResult);
        }
    }
    /**解析中心下发指令的主方法
     * cmd[3] 为中心指令关键字.
     * @see #parserCenterFtpInfo(String[]) 中心下发用来上传文件的FTP信息
     * @see #parserCenterGpsSet(String[]) 中心下发Gps的设置参数
     * @see #parserCenterHeardBeatSet(String[]) 中心下发心跳包的设置参数
     * @see #parserCenterHistoryQuery(String[]) 中心下发历史文件记录查询指令
     * @see #parserCenterHistoryUpload(String[]) 中心下发文件上传指令
     * @see #parserCenterRealTimeVideo(String[]) 中心下发实时视频请求
     * @see #parserCenterReply(String) 中心回复指令
     * @see #parserDownFtp(String) //广告机使用
     * @see #parserFTPInfo(String, String, String) //广告机使用
     * @see #parserCenterReceive(String[]) 中心下发对讲指令
     * @see #parserCenterTalk(String[]) //新对讲
     * @see #parserCenterVideoAudio(String[]) //视音频
     * */
    private void parserMsg(String info) {
        if (info.contains(BaseProtocol.HEAD_BEAT)) {
            return;
        }
        try {
            String[] results = info.split(BaseProtocol.SUFFIX);
            for (String result : results) {
                Values.LOG_I(BaseProtocol.TAG, "result = " + result);
                Log.e("FTPContinue", "result = " + result);
                String[] cmd = result.split(BaseProtocol.COMMA);
                switch (cmd[3]) {
                    case BaseProtocol.CENTER_REPLY:
                        parserCenterReply(result);
                        break;
                    case BaseProtocol.CENTER_GPS_SET:
                        parserCenterGpsSet(cmd);
                        break;
                    case BaseProtocol.CENTER_HEARD_BEAT_SET:
                        parserCenterHeardBeatSet(cmd);
                        break;
                    case BaseProtocol.CENTER_FTP_DOWNLOAD:
                        parserDownFtp(result);
                        break;
                    case BaseProtocol.CENTER_REAL_TIME_VIDEO:///
                        if (CameraService.getUsbCameraShow(mContext)) {
                            /**外置usb摄像头上传*/
                            sendBroadCast(BaseProtocol.CENTER_REAL_TIME_VIDEO, result);
                        } else {
                            /**本机自带摄像头上传*/
                            parserCenterRealTimeVideo(cmd);
                        }
                        mUvcResult = result;
                        mMainCameraCmd = cmd;
                        break;
                    case BaseProtocol.CENTER_FTP_INFO:
                        parserCenterFtpInfo(cmd);
                        break;
                    case BaseProtocol.CENTER_FILE_DOWNLOAD:
                        parserCenterFtpDown(cmd);
                        break;
                    case BaseProtocol.CENTER_HISTORY_QUERY:
                        //99dc0055,10004,,C110,140709 135153,140709 015959,134856,3,0100
                        parserCenterHistoryQuery(cmd);
                        break;
                    case BaseProtocol.CENTER_HISTORY_UPLOAD:////
                        parserCenterHistoryUpload(cmd);
                        break;
                    case BaseProtocol.CENTER_TALK_BACK:
                        parserCenterReceive(cmd);
                        break;
                    case BaseProtocol.CENTER_REMOTE_CONTROL:
                        parserCenterRemoteControl(cmd);
                        break;
                    case BaseProtocol.CENTER_MONITOR:
                        parserCenterReport(cmd);
                        break;
                    case BaseProtocol.CENTER_TALK_BACK_SERVER:
                        sendBroadCast(BaseProtocol.CENTER_TALK_BACK_SERVER, result);
//                        parserCenterTalkBackServer(cmd[5]);
                        break;
                    case BaseProtocol.CENTER_MESSAGE:
                        parserCenterMsg(cmd[5]);
                        break;
                    case BaseProtocol.CENTER_TALK_BACK_SERVER_NEW:
                        sendBroadCast(BaseProtocol.CENTER_TALK_BACK_SERVER_NEW, result);
                        break;
                    case BaseProtocol.CENTER_NEW_TALK_BACK://新的对讲指令
                        Log.i("123", "CENTER_NEW_TALK_BACK cmd=" + cmd[9]);
                        if ("1".equals(cmd[9])) {
                            if (mAudioReportPassing) {
                                Log.i("123", "CENTER_NEW_TALK_BACK mAudioReportPassing=true");
                                parserCenterAudio(cmd, BaseProtocol.THIRD);
                            } else {
                                Log.i("123", "CENTER_NEW_TALK_BACK mAudioReportPassing=false postEvent");
                                EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.TALK_SHOW_DIALOG, cmd);
                            }
                        } else {
                            mAm.endToSend();
                            mAm.endToReceive();
                            releaseAudioReceiveSocket();
                            releaseAudioReportSocket();
                            EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.TALK_DISMISS_DIALOG, cmd);
                        }
                        break;
                    case BaseProtocol.CENTER_VIDEO_AUDIO:
                        if ("1".equals(cmd[9])) {
                            mVideoAudioReportPassing = true;
                            mVideoAudioReport = true;
                            parserCenterVideoAudio(cmd);
                        } else {
                            //关实时视频
                            mVideoAudioReportPassing = false;
                            mVideoAudioReport = false;
                            EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REPORT_STOP);
                            releaseVideoAudioSocket();
                            releaseWakeLock();
                            mAm.endToSend();
                        }
                        break;
                    case BaseProtocol.CENTER_FILE_LIST:
                        parserCenterFile(cmd);
                        break;
                    case BaseProtocol.CENTER_FILE:
                        mCenterUpload = true;
                        parserCenterFtpFile(cmd);
                        break;
                    case BaseProtocol.CENTER_RTMP:
                        parserCenterRTMP(cmd);
                        break;
                    case BaseProtocol.CENTER_TYPE:
                        parserCenterType(cmd);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String VIDEO_RECORD = "0";
    private String PIC = "1";
    private String SOUND_RECORD = "2";
    private void parserCenterType(String[] cmd){
        String case_number = cmd[5];//案件号
        String message = cmd[6];//事件消息
        String operation = cmd[7];//操作样式 录音录像拍照
        String state = cmd[8];//开关
        Settings.System.putString(mContext.getContentResolver(),"case_number",case_number);
        if (operation.equals(VIDEO_RECORD)){
            if (state.equals("1")){
                if (!CameraService.mIsRecording){
                    sendBroad(Values.ACTION_UEVENT_RECORD);
                }
            }else if (state.equals("0")){
                if (CameraService.mIsRecording){
                    sendBroad(Values.ACTION_UEVENT_RECORD);
                }
            }
        }else if (operation.equals(PIC)){
            Intent picIntent = new Intent();
            picIntent.setAction(Values.ACTION_UEVENT_CAPTURE);
            picIntent.putExtra("zzx_state",2);
            mContext.sendBroadcast(picIntent);
        }else if (operation.equals(SOUND_RECORD)){
            if (state.equals("1")){
                if (!CameraService.isSoundReecord){
                    sendBroad(Values.ACTION_UEVENT_MIC);
                }
            }else if (state.equals("0")){
                if (CameraService.isSoundReecord){
                    sendBroad(Values.ACTION_UEVENT_MIC);
                }
            }
        }
    }

    private void sendBroad(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        mContext.sendBroadcast(intent);
    }

    private void parserCenterRTMP(String[] cmd) {
        String centerTime = cmd[4];
        String isStart = cmd[5];

        if(isStart.equals("1")) {
            //在录像时，关闭录像功能
            Intent closeCamera = new Intent();
            closeCamera.setAction(Values.ACTION_RELEASE_CAMERA);
            mContext.sendBroadcast(closeCamera);

            if(FastModel.getInstance().isMic()) {
                //在录音时，关闭录音功能
                Intent closeMic = new Intent();
                closeMic.setAction(Values.ACTION_UEVENT_MIC);
                mContext.sendBroadcast(closeMic);
            }

            String url = cmd[6];
            Intent openIntent = new Intent("open_push");
            openIntent.putExtra("push_url", url);
            mContext.sendBroadcast(openIntent);
        } else {
            Intent closeIntent = new Intent("close_push");
            mContext.sendBroadcast(closeIntent);
        }
    }

    /**
     * 中心获取文件列表
     */
    Map<String , String > map = new HashMap<>();
    long endTime;
    long startTime;
    public void parserCenterFile(String[] cmd){
        StringBuffer stringBuffer = new StringBuffer();
        String filename;
        String filepath;
        startTime = Long.parseLong(cmd[5]);
        endTime = Long.parseLong(cmd[6]);
        switch (cmd[7]){
            case "0"://视频
                getFileList(new File(Values.VID_DIR_PATH + File.separatorChar));
                break;
            case "1"://音频
                getFileList(new File(Values.SND_DIR_PATH + File.separatorChar));
                break;
            case "2"://图片
                getFileList(new File(Values.IMG_DIR_PATH + File.separatorChar));
                break;
            case "3"://文本
                getFileList(new File(Values.LOG_DIR_PATH + File.separatorChar));
                break;
            case "4"://全部
                getFileList(new File(Values.ROOT_PATH));
                break;
        }
        VehicleFileUpload report = new VehicleFileUpload();
//        VehicleFileFTPReport report = new VehicleFileFTPReport();
        if (filepaths.size() != 0){
            stringBuffer.delete(0,stringBuffer.length());
            for (int i = 0; i <filepaths.size() ; i++) {
                filename = new File(filepaths.get(i)).getName();
                filepath = filepaths.get(i);
                stringBuffer.append(filename).append("-").append(String.valueOf(FileUtils.filesize(filepath))).append("--");
                map.put(filename,filepath);
            }
            report.mFileName = stringBuffer.toString();
        }
        report.mDeviceSerialNum = cmd[1];
        report.mWorkstationSerialNum = cmd[2];
        report.mCenterCommand = cmd[3];
        report.mCenterTime = cmd[4];
        Log.e(TAG, "report: "+report.getCommand() );
        sendCommand(report.getCommand());
        filepaths.clear();
    }
    /**
     * 中心请求上传文件
     * @param cmd
     */
    static VehicleFileUpload fileUpload;
    private static String comment ;
    static UploadFileUtil mUpladFileUtil;
    public void parserCenterFtpFile(String[] cmd){
        String filename  = cmd[9];
        String ftpIp     = cmd[5];
        String ftpPort   = cmd[6];
        String ftpName   = cmd[7];
        String ftpPass   = cmd[8];
        Settings.System.putString(mContext.getContentResolver(),Values.FTP_IP,ftpIp);
        Settings.System.putString(mContext.getContentResolver(),Values.FTP_SET_PORT,ftpPort);
        Settings.System.putString(mContext.getContentResolver(),Values.FTP_NAME,ftpName);
        Settings.System.putString(mContext.getContentResolver(),Values.FTP_PASS,ftpPass);
        mUpladFileUtil = new UploadFileUtil(mContext);
        fileUpload = new VehicleFileUpload();
        fileUpload.mCenterCommand = cmd[3];
        fileUpload.mFileName = cmd[9];
        fileUpload.mCenterTime = cmd[4];
        Log.e(TAG, "parserCenterFtpFile: "+map.get(filename) );
        if (map.get(filename).equals("") || map.get(filename)==null){
            Log.e(TAG, "parserCenterFtpFile: NO" );
            fileUpload.mSuccess = BaseProtocol.SUCCESS;
            fileUpload.mCause = BaseProtocol.SECOND;
            sendFileUpload(fileUpload.getCommand());
        }else {
            Log.e(TAG, "parserCenterFtpFile: SUCCESS" );
            fileUpload.mSuccess = BaseProtocol.FAILED;
            mUpladFileUtil.uploadVideoFile( FTPUploadInfo.getFTPUploadInfo(mContext,map.get(filename)));
            comment = fileUpload.getCommand();
        }
        Log.e(TAG, "fileUpload: "+fileUpload.getCommand() );
    }

    public static void sendFileUpload(String cmd){
        OutputStreamWriter writer;
        try {
            if (mOutputStreamCmd == null) {
                Values.LOG_E(TAG, cmd + ".sendCommand.mOutputStreamCmd = null");
                return;
            }
            cmd = new String(cmd.getBytes(), "ASCII");
            writer = new OutputStreamWriter(mOutputStreamCmd);
            writer.write(cmd, 0, cmd.length());
            writer.flush();
            Values.LOG_E(TAG, "writeCommand = " + cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void reportFileUpload(String fileName){
        if (fileUpload!=null){
            fileUpload.mFileName= fileName;
        }
        comment = fileUpload.getCommand();
        Log.e(TAG, "reportFileUpload: "+comment );
        sendFileUpload(comment);
        mCenterUpload = false;
    }
    List<String> filepaths = new ArrayList<>();

    //获取文件列表
    public void getFileList(File file) {
        String name = null;
        long time ;
        File[] listFiles = file.listFiles();
        if (listFiles != null){
            for (File child:listFiles) {
                name = child.getName();
                if (child.isDirectory()){
                    if (name.equals("video")||name.equals("sound")||name.equals("pic")||name.equals("log")||name.equals("text")||name.equals("logcat_information")){
                        getFileList(new File(file.toString()+"/"+name));
                    }else {
                        time = Long.parseLong(name);
                        if (time >= startTime || time >= endTime){
                            Log.e(TAG, "getFileList: time"+name );
                            getFileList(new File(file.toString()+"/"+name));
                        }
                    }
                }else {
                    String policeNum = DeviceUtils.getPoliceNum(mContext);
                    if (!name.contains("GPS") && !name.contains("start") && name.contains(policeNum)){
                        Log.e(TAG, "getFileList: "+child.getName() );
                        filepaths.add(child.getPath());
                    }
                }
            }
        }
    }
    public void parserCenterVideoAudio(String[] cmd) {
        if (mSocketVideoAudio == null) {
            mSocketVideoAudio = new Socket();
        }
        IntentInfo info = new IntentInfo(Values.ACTION_MONITOR_STATE, 1);
        //开实时视频
        if (CameraService.mNeedReportVideo) {
            return;
        }
        VehicleVideoAuidoRegister register = new VehicleVideoAuidoRegister();
        info.mState = 1;
        register.mDeviceSerialNum = cmd[1];
        sendJSON(register.mDeviceSerialNum, 4, cmd[7]);
        mExecutor.execute(new VideoAudioSocketCreateThread(cmd[5] + ":" + cmd[6], content, mSocketVideoAudio, 2));
        EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info);

    }
    public void sendJSON(byte[] bytes, final int type) {
        VehicleVideoAuidoRegister register = new VehicleVideoAuidoRegister();
//        final JSONObject jsonObject = new JSONObject();
//        JSONObject jsonBody = new JSONObject();
//        JSONObject jsonHeader = new JSONObject();

        HeaderJson headerJson = new HeaderJson();
        headerJson.setPid(99);
        headerJson.setVersion("v1");
        headerJson.setUid(register.mDeviceSerialNum);
        BodyJson bodyJson = new BodyJson();
        bodyJson.setStream(bytes);
        bodyJson.setType(type);
        JsonUtil jsonUtil = new JsonUtil(headerJson,bodyJson);
        Log.i("xxq", JSON.toJSONString(jsonUtil));
        sendBytes(JSON.toJSONString(jsonUtil)+"\r\n");
//        sendAudioVideoRegisterCmd( mOutStreamVideoAudio,jsonObject.toString()+"\r\n");
    }
    String content;
    private void sendJSON(String uid, int action, String sessionId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject jsonBody = new JSONObject();
        JSONObject jsonHeader = new JSONObject();
        try {
            jsonHeader.put("version", "v1");
            jsonHeader.put("pid", 88);
            jsonHeader.put("uid", uid);
            jsonBody.put("action", action);
            jsonBody.put("sessionId", sessionId);
            jsonObject.put("header", jsonHeader);
            jsonObject.put("body", jsonBody);
            content = jsonObject.toString() + "\r\n";
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void sendBytes(String cmd){
        OutputStreamWriter writer;
        try {
            if (mOutStreamVideoAudio==null){
                return;
            }
            cmd = new String(cmd.getBytes(), "ASCII");
            writer = new OutputStreamWriter(mOutStreamVideoAudio);
            writer.write(cmd, 0, cmd.length());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void parserCenterMsg(String msg) {
        Intent intent = new Intent(mContext, SOSService.class);
        intent.setAction(SOSService.ACTION_CENTER_MSG);
        intent.putExtra(SOSService.EXTRA_CENTER_MSG, msg);
        mContext.startService(intent);
    }
    public void parserCenterAudio(String[] cmd, String result) {
        String sessionId = cmd[7];
        String username = cmd[8];
        String results = result + BaseProtocol.COMMA;
        replyToCenter(cmd[4], results, cmd[3], username, sessionId);

    }
    /**
     * 解析对讲服务器并启动对讲服务{@link TalkBackService}.
     * */
    private void parserCenterTalkBackServer(String address) {
        Intent intent = new Intent(mContext, TalkBackService.class);
        intent.setAction(TalkBackService.ACTION_CONNECT_SERVICE);
        intent.putExtra(TalkBackService.EXTRA_SERVICE_ADDRESS, address);
        mContext.startService(intent);
    }
    /**
     * 中心发起监听
     *
     * @see #parserCenterReceive(String[])
     */
    private void parserCenterReport(String[] cmd) {
        Values.LOG_E(TAG, "parserCenterReport.report = " + cmd[6] + "; mAudioReportPassing = " + mAudioReportPassing);
        if (cmd[6].equals("1")) {
            if (!mAudioReportPassing) {
                mAudioReportPassing = true;
                if (!isEnableUDP) {
                    if (mSocketAudioReport == null) {
                        mSocketAudioReport = new Socket();
                    }
                } else {
                    if (audioThread == null) {
                        audioThread.release();
                    }
                }
                VehicleAudioPassRegister register = new VehicleAudioPassRegister();
                getVehicleStatus(register.mVehicleStatus, false);
                register.mDeviceSerialNum = cmd[1];
                register.mCenterCmd = cmd[3];
                register.mReciveTime = cmd[4];
                register.mSessionId = cmd[5];
                register.mCallType = cmd[6];
                register.mUserName = cmd[8];
                register.mServiceIp = cmd[9];
                if (!isEnableUDP) {
//                    Log.d("123","tcp----new AudioSocketCreateThread2--------");
                    mExecutor.execute(new AudioSocketCreateThread(cmd[9], register.getCommand(), mSocketAudioReport, 0));
                } else {
//                    Log.d("123","udp----new AudioSocketCreateThread2--------");
                    audioThread = new AudioSocketCreateThread2(cmd[9], register.getCommand(), 0);
                    mExecutor.execute(audioThread);
                }
            }
        } else if (mAudioReportPassing) {
            mAm.endToSend();
            EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.AUDIO_REPORT_STOP);
        }
    }

    public boolean isAudioPassing() {
        return mAudioReportPassing;
    }

    /**发送SOS信息
     * */
    public void sendSOS(VehicleSOSReport report) {
        sendCommand(report.getCommand());
    }

    public void requestVideo() {
        mVideoRequesting = true;
        VehicleRequestVideoOrAudio request = new VehicleRequestVideoOrAudio(VehicleRequestVideoOrAudio.TYPE_VIDEO);
        getVehicleStatus(request.mVehicleStatus, false);
        sendCommand(request.getCommand());
    }

    public void requestAudio() {
        mAudioRequesting = true;
        VehicleRequestVideoOrAudio request = new VehicleRequestVideoOrAudio(VehicleRequestVideoOrAudio.TYPE_AUDIO);
        getVehicleStatus(request.mVehicleStatus, false);
        sendCommand(request.getCommand());
    }

    /**解析中心远程控制命令.
     * */
    private void parserCenterRemoteControl(String[] cmd) {
        String result = BaseProtocol.SUCCESS + BaseProtocol.COMMA + cmd[5];
        replyToCenter(cmd[4], result, null, cmd[3]);
        if (cmd[5].equals("1")) {
            reboot();
        }
    }

    private void reboot() {
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot(null);
    }

    /**
     * 解析中心下发的对讲指令
     * cmd[5]   会话ID
     * cmd[6]   启停标记   0:停止; 1:开始.
     * cmd[7]   音频源     1:Camera; 2:MIC
     * cmd[8]   通道号
     * cmd[9]   声道模式   0:单声道MONO; 1:立体声STEREO
     * cmd[10]  声道数目
     * cmd[11]  采样率
     * cmd[12]  采样位数   0:8bit; 1:16bit; 2:32bit.
     * cmd[13]  音频格式
     * cmd[14]  音频帧长度
     * cmd[15]  Socket地址及端口
     *
     * @see #parserCenterReport(String[])
     */
    private void parserCenterReceive(String[] cmd) {
        Values.LOG_E(TAG, "parserCenterReceive.receive = " + cmd[6] + "; mAudioReceivePassing = " + mAudioReportPassing);
        if (cmd[6].equals("1")) {
            if (!mAudioReportPassing) {
                mAudioReportPassing = true;
                if (!isEnableUDP) {
                    if (mSocketAudioReceive == null) {
                        mSocketAudioReceive = new Socket();
                    }
                } else {
                    if (audioThread != null) {
                        audioThread.release();
                    }
                }

                VehicleAudioPassRegister register1 = new VehicleAudioPassRegister();
                register1.mUUID = cmd[5];
                mAudioReceiveUUID = cmd[5];
                register1.mCenterCmd = cmd[3];
                register1.mRegisterType = VehicleAudioPassRegister.TYPE_TALK_RECEIVE;
                register1.mAudioSource = cmd[7];
                register1.mPassNumber = cmd[8];
                register1.mSoundMode = cmd[9];
                register1.mChannelCount = cmd[10];
                register1.mSampleRate = cmd[11];
                register1.mBiteSize = cmd[12];
                register1.mAudioFormat = cmd[13];
                register1.mFrameSize = cmd[14];
                register1.mServiceIp = cmd[15];
                register1.mSessionId = cmd[5];
                register1.mDeviceSerialNum = cmd[1];
                if (!isEnableUDP) {
                    Log.d("123", "tcp----new AudioSocketCreateThread2--------");
                    mExecutor.execute(new AudioSocketCreateThread(cmd[15], register1.getCommand(), mSocketAudioReceive, 1));
                } else {

                    Log.d("123", "udp----new AudioSocketCreateThread2--------");
                    audioThread = new AudioSocketCreateThread2(cmd[15], register1.getCommand(), 0);
                    mExecutor.execute(audioThread);
                }
                mAudioRequesting = false;
            }
        } else if (mAudioReceivePassing) {
//            EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.AUDIO_REPORT_STOP);//lzc改
            mAm.endToReceive();
            releaseAudioReceiveSocket();
        }
    }
    /**解析中心下发文件上传信息
     * cmd[7] 为回放标志 1: 回放控制; 0: 结束回放
     * cmd[8] 为回放控制标志 0: 开始回放; 1: 暂停回放 2: 更新回放 3: 快进回放
     * cmd[9] 为码流类型
     * cmd[10] 为通道号
     * cmd[11] 为录像开始时间 cmd[12] 为录像结束时间
     * cmd[13] 为Socket地址 IP:port
     * cmd[14] 为获取类型
     * @see VideoSocketCreateThread
     * */
    private void parserCenterHistoryUpload(String[] cmd) {
//        99dc0126,10004,,C500,140711 192445,f5c29940-9940-f5c2-a6eb-c54526d9c545,7,1,0,0,0100,140710 202142,140710 202152,219.136.187.242:6023
        if (cmd[7].equals("1")) {
            if (cmd[8].equals("0")) {
                VehicleFileUploadRegister videoPassRegister = new VehicleFileUploadRegister();
                getVehicleStatus(videoPassRegister.mVehicleStatus, false);
                videoPassRegister.mSessionCmd   = cmd[3];
                videoPassRegister.mSessionId    = cmd[5];
                videoPassRegister.mStreamType   = cmd[9];
                videoPassRegister.mPassNum      = cmd[10];
                videoPassRegister.mStartTime    = cmd[11];
                videoPassRegister.mEndTime      = cmd[12];
                videoPassRegister.mServiceAddress = cmd[13];
                mExecutor.execute(new VideoSocketCreateThread(videoPassRegister.mServiceAddress, cmd[3]));
            }
        }
    }

    /**历史记录查询
     *
     * */
    private void parserCenterHistoryQuery(String[] cmd) {
        String centerTime   = cmd[4];
        String[] date = cmd[5].split(" ");
        String createDate   = date[0];
        String startTime    = date[1];
        String endTime      = cmd[6];
        new DatabaseQueryTask(mContext, centerTime).execute("0", createDate, startTime, endTime);
    }

    public void reportHistoryListInfo(String centerTime, String historyInfo) {
        String results = BaseProtocol.SUCCESS + BaseProtocol.COMMA + BaseProtocol.COMMA;
        replyToCenter(centerTime, results, historyInfo, BaseProtocol.CENTER_HISTORY_QUERY);
    }

    /**中心下发文件上传所需的FTP信息
     * @see #sendUpload(String, String, int) 向服务器发送上传文件请求信息.
     * @see #parserMsg(String)
     * */
    private void parserCenterFtpInfo(String[] cmd) {
        String fileName = cmd[5];
        String ftpIp    = cmd[6];
        String ftpPort  = cmd[7];
        String ftpUser  = cmd[8];
        String ftpPass  = cmd[9];
        String ftpDir   = cmd[10];
        Values.LOG_I(TAG, fileName + ": " + ftpIp + ":" + ftpPort + ftpDir + "; " + ftpUser + ":" + ftpPass);
        replyToCenter(cmd[4], BaseProtocol.SUCCESS, null, cmd[3]);
        UploadInfo info = new UploadInfo(fileName, ftpDir);
        EventBusUtils.postEvent(Values.BUS_EVENT_FTP, info);
    }
    public static final String FTP_FILE_DOWN_DIR = Environment.getExternalStorageDirectory()
            + File.separator + "police"
            + File.separator + "text"
            + File.separator;
    /**
     * FTP文件下载信息的数组列表对应的KEY
     */
    public static final String KEY_FTP_DOWN_INFO_LIST = "key_ftp_down_info_list";

    /**
     * 控制中心下发文件下载所需的相关信息
     */
    private void parserCenterFtpDown(String[] cmd) {
        //String tag = "parserCenterFtpDown";
        String tag = "FTPDownService";
        //String tag = "FTPContinue";
        if (DEBUG) {
            Log.d(tag, "parserCenterFtpDown: ----------------start----------------");
            for (int i = 0; i < cmd.length; i++) {
                Log.d(tag, "parserCenterFtpDown[" + i + "]: " + cmd[i]);
            }
        }
        ArrayList<FTPDownLoadInfo> infos = new ArrayList<>();
        if (cmd.length >= 10) {
            String policeNumber = cmd[1];
            String dateAndTime = cmd[4];
            String ftpIp = cmd[5];
            String ftpPort = cmd[6];
            String ftpAccount = cmd[7];
            String ftpPwd = cmd[8];
            String filesPathAndName = cmd[9];                 //该条信息可能包含了多个需要下载的文件信息
            String[] infoArray = filesPathAndName.split("--");//按规则解析成文件数组
            for (int j = 0; j < infoArray.length; j++) {
                String[] fileInfo = infoArray[j].split("\\\\");
                String filePath = "/";
                for (int k = 0; k < (fileInfo.length - 1); k++) {
                    filePath += fileInfo[k] + "/";
                }
                String fileName = fileInfo[fileInfo.length - 1];
                if (DEBUG) {
                    Log.d(tag, "parserCenterFtpDown  文件[" + (j + 1) + "]:路径 " + filePath);
                    Log.d(tag, "parserCenterFtpDown  文件[" + (j + 1) + "]:名字 " + fileName);
                }
                infos.add(new FTPDownLoadInfo(
                        ftpAccount,
                        ftpPwd,
                        ftpIp,
                        filePath,
                        fileName,
                        FTP_FILE_DOWN_DIR,
                        Integer.valueOf(ftpPort)));
                //createFtp(false, ftpIp, Integer.valueOf(ftpPort), ftpAccount, ftpPwd);
                //downloadFile(filePath, fileName,FTP_FILE_DOWN_DIR);
            }
            if (!infos.isEmpty()) {
                Intent intent = new Intent(mContext, FTPDownService.class);
                intent.putParcelableArrayListExtra(KEY_FTP_DOWN_INFO_LIST, infos);
                mContext.startService(intent);
            }
        }
        if (DEBUG) {
            Log.d(tag, "parserCenterFtpDown: ----------------end----------------");
            Log.d(tag, " ");
        }
    }
    /**@see BaseProtocol#CENTER_REAL_TIME_VIDEO
     *
     * */
    private void parserCenterRealTimeVideo(String[] cmd) {
        Values.LOG_W(TAG, "parserCenterRealTimeVideo report = " + cmd[6]);
        final VehicleVideoPassRegister videoPassRegister;
        IntentInfo info = new IntentInfo(Values.ACTION_MONITOR_STATE, 1);
        if (cmd[6].equals("1")) {
            if (CameraService.mNeedReportVideo) {
                return;
            }
            info.mState = 1;
            videoPassRegister = new VehicleVideoPassRegister();
            getVehicleStatus(videoPassRegister.mVehicleStatus, false);
            videoPassRegister.mSessionCmd = cmd[3];
            videoPassRegister.mServiceAddress = cmd[8];
            videoPassRegister.mSessionId = cmd[5];
            videoPassRegister.mPassNum = cmd[7];
            if (mSocketVideo == null) {
                mExecutor.execute(new VideoSocketCreateThread(videoPassRegister.mServiceAddress, videoPassRegister.getCommand()));
            }
        } else if (cmd[6].equals("0")) {
            info.mState = 0;
            EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REPORT_STOP);
            releaseVideoSocket();
            releaseWakeLock();
        }
        EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info);
    }

    /**@see #parserCenterTalkBack(String[])
     * @see #releaseAudioReportSocket()
     * */
    /**创建视频的socket**/
    class VideoSocketCreateThread extends Thread {
        private String mServiceAddress = null;
        private String mReportCmd   = null;
        public VideoSocketCreateThread(String address, String reportCmd) {
            mServiceAddress = address;
            mReportCmd = reportCmd;
        }
        @Override
        public void run() {
            if (mSocketVideo != null) {
                releaseVideoSocket();
            }
            String[] address = mServiceAddress.split(":");
            try {
                InetAddress ip = Inet4Address.getByName(address[0]);
                mSocketVideo = new Socket(ip, Integer.parseInt(address[1]));
                mSocketVideo.setKeepAlive(true);
                mInStreamVideo = mSocketVideo.getInputStream();
                mOutStreamVideo = mSocketVideo.getOutputStream();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /**改新的指令**/
                        sendAudioVideoRegisterCmd(mOutStreamVideo, mReportCmd);
                    }
                }, 1000);
                byte[] buffer = new byte[1024];
                int len;
                if (mSocketVideo == null || mSocketVideo.isClosed() || !mSocketVideo.isConnected() || mInStreamVideo == null) {
                    Values.LOG_E(TAG, "mSocketVideo == null || mSocketVideo.isClosed() || !mSocketVideo.isConnected() || mInStreamVideo == null");
                    return;
                }
                while ((len = mInStreamVideo.read(buffer)) != -1) {
                    Values.LOG_I(TAG, "mInStreamVideo.getBuffer = " + len);
                    String info = new String(buffer, 0, len, "UTF-8");
                    parserMsg(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
                releaseVideoSocket();
            }
        }
    }

    /**
     * 创建视音频的socket
     */
    class VideoAudioSocketCreateThread extends Thread {
        private String mServiceAddress = null;
        private String mReportCmd = null;
        private int mIsReceive;

        public VideoAudioSocketCreateThread(String serviceAddress, String reportCmd, Socket socket, int isReceive) {
            mServiceAddress = serviceAddress;
            mReportCmd = reportCmd;
            mSocketVideoAudio = socket;
            mIsReceive = isReceive;
        }

        @Override
        public void run() {
            String[] address = mServiceAddress.split(":");
            try {
                SocketAddress service = new InetSocketAddress(address[0], Integer.parseInt(address[1]));
                mSocketVideoAudio.setKeepAlive(true);
                mSocketVideoAudio.connect(service, 3000);
                //    mSocketVideo.setSendBufferSize(400000);
                mInStreamVideoAudio = mSocketVideoAudio.getInputStream();
                mOutStreamVideoAudio = mSocketVideoAudio.getOutputStream();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /**改新的指令**/
                        sendAudioVideoRegisterCmd(mOutStreamVideoAudio, mReportCmd);
                    }
                }, 1000);
                byte[] buffer = new byte[1024];
                int len;
                if (mSocketVideoAudio == null || mSocketVideoAudio.isClosed() || !mSocketVideoAudio.isConnected() || mInStreamVideoAudio == null) {
                    Values.LOG_E(TAG, "mSocketVideo == null || mSocketVideo.isClosed() || !mSocketVideo.isConnected() || mInStreamVideo == null");
                    return;
                }
                while ((len = mInStreamVideoAudio.read(buffer)) != -1) {
                    String info = new String(buffer, 0, len, "UTF-8");
                    parserCenterReply();
                    mAm.startToSend();
                }
            } catch (Exception e) {
                e.printStackTrace();
                releaseVideoAudioSocket();
                mAm.endToSend();
            }
            super.run();
        }
    }
    private AudioTransManager mAm;

    /**
     * @see #parserCenterReceive(String[])
     */
    class AudioSocketCreateThread2 extends Thread {
        private InetAddress inetAddress;//对讲地址
        private int port;
        private String mReportCmd;//对讲指令
        private int mIsReceive;//是否接收 中心发起监听0 对讲指令 1 点击OK
        private boolean mIsCommand = true;//
        private boolean mIsThreadDisable = true;


        public AudioSocketCreateThread2(String serviceAddress, String reportCmd, int isReceive) {
//            Log.d("123","create AudioSocketCreateThread  = " + isReceive);
            String[] address = serviceAddress.split(":");
            try {
                inetAddress = InetAddress.getByName(address[0]);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            port = Integer.parseInt(address[1]);
//            Log.d("123","create  AudioSocketCreateThread2 inetAddress: "+ address[0]+"---port:"+port );
            mReportCmd = reportCmd;
            mIsReceive = isReceive;
            if (datagramSocket == null) {
                try {
                    datagramSocket = new DatagramSocket();
                    datagramSocket.setBroadcast(true);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }

        public void release() {
//            Log.d("123","AudioSocketCreateThread.release " );
            mIsThreadDisable = false;
            try {
                this.interrupt();
            } catch (Exception e) {

            }
            audioThread = null;
        }

        public boolean ismIsThreadDisable() {
            return mIsThreadDisable;
        }

        public void sendUDPMessage(String message) {
            if ((message == null) || (message.equals(""))) {
                return;
            }
            byte[] messageByte = message.getBytes();
            sendUDPMessage(messageByte);
        }

        public void sendUDPMessage(byte[] datas) {
//            Log.d("123","AudioSocketCreateThread.send datas: "+new String(datas) );
            if ((datas == null) || (datas.length == 0)) {
                return;
            }
            try {
                DatagramPacket p = new DatagramPacket(datas, datas.length, inetAddress,
                        port);
                if (datagramSocket != null) {
                    datagramSocket.send(p);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (datagramSocket == null) {
                return;
            }
            try {
//                Log.d("123","AudioSocketCreateThread.receive run 1160 " );
                byte[] buffer = new byte[2048];
                DatagramPacket datagramPacket = new DatagramPacket(buffer,
                        buffer.length);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //发送连接媒体服务器的指令
                        sendUDPMessage(mReportCmd);
                    }
                }, 1000);
//                Log.d("123","AudioSocketCreateThread.receive run 1177 " );
                while (mIsThreadDisable) {
//                    Log.d("123","AudioSocketCreateThread.receive run 1179 " );
                    datagramSocket.receive(datagramPacket);
                    byte[] data = Arrays.copyOfRange(datagramPacket.getData(), 0, datagramPacket.getLength());
                    if (mIsCommand) {
                        mIsCommand = false;
                        String info = new String(data, 0, data.length, "UTF-8");
                        String[] results;
                        String[] cmd;
                        switch (mIsReceive) {
                            case 0:
                                results = info.split(BaseProtocol.SUFFIX);
                                cmd = results[0].split(BaseProtocol.COMMA);
                                Values.LOG_E("lzcdemo", "send audio " + results[0] + " " + cmd);
                                if (cmd[8].equals(BaseProtocol.SUCCESS)) {
                                    mAm.startToSend();
                                }
                                break;
                            case 1:
                                results = info.split(BaseProtocol.SUFFIX);
                                cmd = results[0].split(BaseProtocol.COMMA);
                                if (cmd[8].equals(BaseProtocol.SUCCESS)) {
                                    if (mAudioReceiveUUID != null) {
                                        VehicleReceiveAudio receiveAudio = new VehicleReceiveAudio();
                                        receiveAudio.mSessionId = mAudioReceiveUUID;
                                        String audioCmd = receiveAudio.getCommand();
                                        sendCommand(audioCmd);
                                    }
                                    mAm.startToReceive();
                                }
                                break;
                            case 2:
                                results = info.split(BaseProtocol.SUFFIX);
                                Values.LOG_E("lzcdemo", "send audio " + results[0]);
                                VehicleAudioPassRegister receiveAudio = new VehicleAudioPassRegister();
                                String audioCmd = receiveAudio.getCommand();
                                sendCommand(audioCmd);
                                mAm.startToSend();
                                mAm.startToReceive();
                                break;
                        }
                    } else {

                        if (mAm != null) {

                            mAm.receiveAudioData(data);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                switch (mIsReceive) {
                    case 0:
                        mAm.endToSend();
                        releaseAudioReportSocket();
                        break;
                    case 1:
                    case 2:
                        if (!mVideoAudioReport){
                            mAm.endToSend();
                        }
                        mAm.endToReceive();
                        releaseAudioReceiveSocket();
                        break;
                }
            } finally {
                Values.LOG_I(TAG, "AudioRead Over");
            }
        }
    }
    /**@see #parserCenterReceive(String[])
     * */
    class AudioSocketCreateThread extends Thread {

        //        private Socket mSocketAudioReport;//点击OK 2 中心发起监听0
//        private Socket mSocketAudioReceive;//音频Socket  //对讲指令 1
        private String mServiceAddress;//对讲地址
        private String mReportCmd;//对讲指令
        private Socket mAudioSocket;//对讲socket
        private int mIsReceive;//是否接收 中心发起监听0 对讲指令 1 点击OK
        private boolean mIsCommand = true;//

        public AudioSocketCreateThread(String serviceAddress, String reportCmd, Socket socket, int isReceive) {
            Values.LOG_E(TAG, "AudioSocketCreateThread.isReceive = " + isReceive);
//            Log.d("123","AudioSocketCreateThread.isReceive = " + isReceive);
            mServiceAddress = serviceAddress;
            mReportCmd = reportCmd;
            mAudioSocket = socket;
            mIsReceive = isReceive;
        }

        @Override
        public void run() {
            String[] address = mServiceAddress.split(":");
            try {
                SocketAddress service = new InetSocketAddress(address[0], Integer.parseInt(address[1]));
                mAudioSocket.setKeepAlive(true);
                mAudioSocket.connect(service, 3000);
                InputStream inStreamAudio = mAudioSocket.getInputStream();
                final OutputStream outStreamAudio = mAudioSocket.getOutputStream();
                switch (mIsReceive) {
                    case 1:
                        break;
                    case 0:
                    case 2:
                        mOutStreamAudio = mAudioSocket.getOutputStream();
                        break;
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //发送连接媒体服务器的指令
                        sendAudioVideoRegisterCmd(outStreamAudio, mReportCmd);
                    }
                }, 1000);
                byte[] buffer = new byte[1024];
                int len;
                if (mAudioSocket == null || mAudioSocket.isClosed() || !mAudioSocket.isConnected() || inStreamAudio == null) {
                    Values.LOG_E(TAG, "mSocketAudioReceive == null || mSocketAudioReceive.isClosed() || !mSocketAudioReceive.isConnected() || mInStreamAudio == null");
                    return;
                }
                while ((len = inStreamAudio.read(buffer)) > 0) {
//                    Values.LOG_I(TAG, "mInStreamAudio.getBuffer = " + len);
                    if (mIsCommand) {
                        mIsCommand = false;
                        String info = new String(buffer, 0, len, "UTF-8");
                        String[] results;
                        String[] cmd;
                        switch (mIsReceive) {
                            case 0:
                                results = info.split(BaseProtocol.SUFFIX);
                                cmd = results[0].split(BaseProtocol.COMMA);
                                Values.LOG_E("lzcdemo", "send audio " + results[0] + " " + cmd);
                                if (cmd[8].equals(BaseProtocol.SUCCESS)) {
                                    mAm.startToSend();
                                }
                                break;
                            case 1:
                                results = info.split(BaseProtocol.SUFFIX);
                                cmd = results[0].split(BaseProtocol.COMMA);
                                if (cmd[8].equals(BaseProtocol.SUCCESS)) {
                                    if (mAudioReceiveUUID != null) {
                                        VehicleReceiveAudio receiveAudio = new VehicleReceiveAudio();
                                        receiveAudio.mSessionId = mAudioReceiveUUID;
                                        String audioCmd = receiveAudio.getCommand();
                                        sendCommand(audioCmd);
                                    }
                                    mAm.startToReceive();
                                }
                                break;
                            case 2:
                                results = info.split(BaseProtocol.SUFFIX);
                                Values.LOG_E("lzcdemo", "send audio " + results[0]);
                                VehicleAudioPassRegister receiveAudio = new VehicleAudioPassRegister();
                                String audioCmd = receiveAudio.getCommand();
                                sendCommand(audioCmd);
                                mAm.startToSend();
                                mAm.startToReceive();
                                break;
                        }
                    } else {

                        if (mAm != null) {
                            mAm.receiveAudioData(Arrays.copyOfRange(buffer, 0, len));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                switch (mIsReceive) {
                    case 0:
                        mAm.endToSend();
                        releaseAudioReportSocket();
                        break;
                    case 1:
                    case 2:
                        mAm.endToSend();
                        mAm.endToReceive();
                        releaseAudioReceiveSocket();
                        break;
                }
            } finally {
                Values.LOG_I(TAG, "AudioRead Over");
            }
        }
    }

    /**
     * 音视频通道连接后发送注册指令.
     * */
    private void sendAudioVideoRegisterCmd(OutputStream out, String cmd) {
        OutputStreamWriter writer;
        try {
            if (out == null) {
                Values.LOG_E(TAG, "sendAudioVideoRegisterCmd.mOutputStreamCmd = null");
                return;
            }
            cmd = new String(cmd.getBytes(), "ASCII");
            writer = new OutputStreamWriter(out);
            writer.write(cmd, 0, cmd.length());
            writer.flush();
            Values.LOG_E(TAG, "sendAudioVideoRegisterCmd = " + cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectFtp() {
        if (mFtpContinue != null) {
            mFtpContinue.disconnect();
            mFtpContinue = null;
        }
    }

    public void createFtp(boolean isList, String ip, int port, String userName, String password) {
        disconnectFtp();
        try {
            mFtpContinue = new FTPContinue(mContext, mHandler, ip, port, userName, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String remoteDir, String remoteName, String localDir) {
        mFtpContinue.download(remoteDir, remoteName, localDir, null);
    }

    private void parserDownFtp(String result) {

    }

    private void parserFTPInfo(String ftpInfo, String type, String name) {
        String localDir = "";
        String[] info = ftpInfo.split("\\$");
        for (String s : info) {
            Values.LOG_V(TAG, s);
        }
        try {
            disconnectFtp();
            mFtpContinue = new FTPContinue(mContext, mHandler, info[0], Integer.parseInt(info[1]), info[2], info[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mFtpContinue.download(info[4], name, localDir, null);
    }

    private void parserCenterHeardBeatSet(String[] infos) {
        mNeedBeat = true;
        mSp.edit().putInt(Values.KEY_HEARD_BEAT_INTERVAL, Integer.valueOf(infos[5])).apply();
        setHeardBeatTimer(Integer.valueOf(infos[5]));
        String results = BaseProtocol.SUCCESS + BaseProtocol.COMMA + BaseProtocol.COMMA;
        replyToCenter(infos[4], results, null, infos[3]);
    }

    private void setHeardBeatTimer(int interval) {
        mInterval = interval;
    }


    /**反馈给服务中心的回复指令 V0
     *
     * @param centerCommand 答复中心指令
     * @param centerTime 中心指令发送时间
     * @param result 结果
     * @param historyList 是否是答复历史记录的命令.若为空则清空,反之则设置
     * */
    private void replyToCenter(String centerTime, String result, String historyList, String centerCommand) {
        if (mCmdReply == null)
            mCmdReply = new VehicleCmdReply();
        if (historyList != null) {
            mCmdReply.setHistoryList(historyList);
        } else {
            mCmdReply.clearHistory();
        }
        getVehicleStatus(mCmdReply.mVehicleStatus, false);
        mCmdReply.mReplyResult = result;
        mCmdReply.mReplyMode = Values.MODE_AUTO;
        mCmdReply.mCenterTime = centerTime;
        mCmdReply.mCenterCommand = centerCommand;
        sendCommand(mCmdReply.getCommand());
    }
    /**
     * @param centerTime    中心指令发送时间
     * @param result        结果
     * @param centerCommand 答复中心指令
     * @param sessionId     会话ID
     * @param userName      客户名字
     */
    private void replyToCenter(String centerTime, String result, String centerCommand, String userName, String sessionId) {
        if (mAudioReply == null)
            mAudioReply = new VehicleAudioRegister();
        mAudioReply.mReplyResult = result;
        mAudioReply.mReplyMode = Values.MODE_AUTO;
        mAudioReply.mCenterTime = centerTime;
        mAudioReply.mCenterCommand = centerCommand;
        mAudioReply.mUserName = userName;
        mAudioReply.mSessionId = sessionId;
        sendCommand(mAudioReply.getCommand());
    }
    private void parserCenterGpsSet(String[] infos) {
        String time = infos[4];
        String code = infos[5];
        if (code.equals("1")) {
            mGpsReportTotalCount = Integer.parseInt(infos[8]);
            mGpsReportCount = 0;
            mNeedGps = true;
        } else if (code.equals("0")) {
            mNeedGps = false;
            sendMessage(null, Values.GPS_REMOVE, 0);
        }
        String results = 1 + BaseProtocol.COMMA + code + BaseProtocol.COMMA + BaseProtocol.COMMA;
        replyToCenter(time, results, null, infos[3]);
    }

    public OutputStream getOutStreamVideo() {
        return mOutStreamVideo;
    }

    public OutputStream getOutStreamAudio() {
        return mOutStreamAudio;
    }

    private void acquireRealWakeLock() {
        if (mRealWakelock == null) {
            if (mPowerManager == null) {
                mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            }
            mRealWakelock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ProtocolUtils.class.getName());
        }
        if (!mWakeLocked) {
            mRealWakelock.acquire();
            mWakeLocked = true;
        }
    }

    private void releaseWakeLock() {
        if (mRealWakelock != null && mWakeLocked) {
            Values.LOG_I(TAG, "mWakeLock != null");
            mRealWakelock.release();
            mRealWakelock = null;
        }
        mWakeLocked = false;
    }
    private void parserCenterReply() {
        acquireRealWakeLock();
        mVideoRequesting = false;
        sendMessage(null, Values.VIDEO_REGISTER_SUCCESS, 0);
        EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REGISTER_SUCCESS);
    }
    /**解析中心回复命令C0
     * @see #parserMsg(String)
     * */
    private void parserCenterReply(String result) {
        int what = -1;
        String[] infos = result.split(BaseProtocol.COMMA);

        switch (infos[5]) {
            case VehicleRegister.COMMAND:
                Values.LOG_I(TAG, infos[7] + infos[8]);
                if (infos[8].equals(BaseProtocol.SUCCESS)) {
                    what = Values.SERVICE_REGISTER_SUCCESS;
                    Values.LOG_I(TAG, "Login Register Success");
                    mRegisterSuccess = true;
                    IntentInfo success = new IntentInfo(Values.ACTION_LOGIN_STATE, 1);
                    EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, success);
                    checkNeedUpdateInit();
                } else if (infos[8].equals(BaseProtocol.FAILED)) {
                    what = Values.SERVICE_REGISTER_FAILED;
                    mRegisterSuccess = false;
                    Settings.System.putString(mContext.getContentResolver(), Values.POLICE_NUMBER, "");
                    BaseProtocol.mDeviceSerialNum = "";
                    sendFailedBroadcast();
                    release();
                    Values.LOG_E(TAG, "Login Register failed");
                    /*IntentInfo failed = new IntentInfo(Values.ACTION_LOGIN_STATE, 0);
                    EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, failed);*/
                }
                break;
            case VehicleVideoPassRegister.COMMAND:
                if (infos[8].equals(BaseProtocol.SUCCESS)) {
                    what = Values.VIDEO_REGISTER_SUCCESS;
                    Values.LOG_I(TAG, "VIDEO Register Success");
                    acquireRealWakeLock();
                } else if (infos[8].equals(BaseProtocol.FAILED)) {
                    what = Values.VIDEO_REGISTER_FAILED;
                    Values.LOG_E(TAG, "VIDEO Register failed");
                }
                mVideoRequesting = false;
                break;
            case VehicleAudioPassRegister.COMMAND:
                if (infos[8].equals(BaseProtocol.SUCCESS)) {
                    Values.LOG_I(TAG, "AUDIO Request Success");
                } else if (infos[8].equals(BaseProtocol.FAILED)) {
                    Values.LOG_I(TAG, "AUDIO Request failed");
                }
                break;
            case VehicleSOSReport.COMMAND:
                if (infos.length >= 9)
                    EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, infos[8]);
                return;
            case VehicleRequestVideoOrAudio.COMMAND:
                if (infos[8].equals(BaseProtocol.SUCCESS)) {
                    Values.LOG_I(TAG, "VIDEO/AUDIO Request Success");
                }
                break;
        }
        sendMessage(null, what, 0);
        EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, what);
    }

    //点击OK
    public void parserCenterTalk(String[] cmd) {
        Values.LOG_E(TAG, "parserCenterTalk.talk = " + cmd[6] + "; mAudioReportPassing = " + mAudioReportPassing);
//        if (cmd[6].equals("1")) {
        if (!mAudioReportPassing) {
            mAudioReportPassing = true;
            if (!isEnableUDP) {
                if (mSocketAudioReport == null) {
                    mSocketAudioReport = new Socket();
                }
            } else {
                ///////----------------------
                ///////----------------------
                if (audioThread != null) {
                    audioThread.release();
                }
            }
            VehicleAudioPassRegister register = new VehicleAudioPassRegister();
            register.mDeviceSerialNum = cmd[1];
            register.mCenterCmd = cmd[3];
            register.mReciveTime = cmd[4];
            register.mSessionId = cmd[7];
            register.mUserName = cmd[8];
            register.mCallType = cmd[9];
            if (!isEnableUDP) {
//                Log.d("123","tcp----new AudioSocketCreateThread--------");
                mExecutor.execute(new AudioSocketCreateThread(cmd[5] + ":" + cmd[6], register.getCommand(), mSocketAudioReport, 2));
            } else {
//                Log.d("123","udp----new AudioSocketCreateThread2--------");
                if (audioThread == null) ;
                {
                    audioThread = new AudioSocketCreateThread2(cmd[5] + ":" + cmd[6], register.getCommand(), 2);
                    mExecutor.execute(audioThread);
                }
            }
        } else if (mAudioReportPassing) {
            parserCenterAudio(cmd, BaseProtocol.THIRD);
            EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.AUDIO_REPORT_STOP);
        }
    }

    private void checkNeedUpdateInit() {
        boolean isCheck;
        if(ZZXConfig.IsCheckExternalStorage){
            isCheck=Environment.isExternalStorageRemovable() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        }else {
            isCheck=true;
        }
        if (isCheck) {
            String policeNum = DeviceUtils.getPoliceNum(mContext);
            String deviceNum = DeviceUtils.getDeviceNum(mContext);
            if (!policeNum.equals(Values.POLICE_DEFAULT_NUM) && !deviceNum.equals(Values.DEVICE_DEFAULT_NUM)) {
                String text = "police_num=" + policeNum + "\ndevice_num=" + deviceNum;
                FileWriter writer = null;
                try {
                    writer = new FileWriter(Values.INI_PATH, false);
                    writer.write(text);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void sendFailedBroadcast() {
        Intent intent = new Intent(Values.ACTION_REGISTER_FAILED);
        mContext.sendBroadcast(intent);
    }

    public void reset() {
        Values.LOG_W(TAG, "ProtocolUtils.reset()");
        closeSocket();
        releaseVideoSocket();
        releaseAudioReportSocket();

        IntentInfo failed = new IntentInfo(Values.ACTION_LOGIN_STATE, 0);
        EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, failed);
        /*if (CameraService.mNeedReportVideo) {
            EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REPORT_STOP);
            IntentInfo info = new IntentInfo(Values.ACTION_MONITOR_STATE, 0);
            EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info);
        }*/
        mRegisterSuccess = false;
        mReleased = true;
        mClientCreated = false;
        mReadThreadCreated = false;
        mNeedGps = false;
        mNeedBeat = false;
        mClientCmd = null;
        mInputStreamCmd = null;
        mOutputStreamCmd = null;
    }

    public void release() {
        reset();
        if (mHeardBeatTimer != null) {
            mHeardBeatTimer.cancel();
            mHeardBeatTimer = null;
        }
        mContext = null;
    }
    private void releaseVideoAudioSocket() {
        Values.LOG_W(TAG, "releaseVideoSocket");
        EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REPORT_STOP);
        try {
            if (mInStreamVideoAudio != null) {
                mInStreamVideoAudio.close();
            }
            if (mOutStreamVideoAudio != null) {
                mOutStreamVideoAudio.close();
            }
            if (mSocketVideoAudio != null && !mSocketVideoAudio.isClosed()) {
                mSocketVideoAudio.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocketVideoAudio = null;
        mInStreamVideoAudio = null;
        mOutStreamVideoAudio = null;
        mVideoRequesting = false;
        if (audioThread != null) {
            audioThread.release();
        }
        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
        IntentInfo info = new IntentInfo(Values.ACTION_MONITOR_STATE, 0);
        EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info);
    }
    private void releaseVideoSocket() {
        Values.LOG_W(TAG, "releaseVideoSocket");
        EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REPORT_STOP);
        try {
            if (mInStreamVideo != null) {
                mInStreamVideo.close();
            }
            if (mOutStreamVideo != null) {
                mOutStreamVideo.close();
            }
            if (mSocketVideo != null && !mSocketVideo.isClosed()) {
                mSocketVideo.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocketVideo = null;
        mInStreamVideo  = null;
        mOutStreamVideo = null;
        mVideoRequesting = false;
        IntentInfo info = new IntentInfo(Values.ACTION_MONITOR_STATE, 0);
        EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, info);
    }

    /**@see AudioSocketCreateThread
     * @see #parserCenterReceive(String[])
     * */
    public void releaseAudioReportSocket() {
        Values.LOG_W(TAG, "releaseAudioReportSocket()");

        try {
            if (mOutStreamAudio != null) {
                mOutStreamAudio.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (mSocketAudioReport != null && !mSocketAudioReport.isClosed()) {
                mSocketAudioReport.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocketAudioReport = null;
        mOutStreamAudio = null;
        mAudioReportPassing = false;
        mAudioRequesting = false;
        if (audioThread != null) {
            audioThread.release();
        }
        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }
    private void releaseAudioReceiveSocket() {
        Values.LOG_W(TAG, "releaseAudioReceiveSocket()");
        try {
            if (mSocketAudioReceive != null && !mSocketAudioReceive.isClosed()) {
                mSocketAudioReceive.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocketAudioReceive = null;
        mAudioReceivePassing = false;
        if (audioThread != null) {
            audioThread.release();
        }
        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }

    public void sendComand(String cmd) {
        sendCommand(cmd);
    }
}
