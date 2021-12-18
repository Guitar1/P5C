package com.zzx.police;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

import com.zzx.police.command.GpsData;
import com.zzx.police.command.VehicleStatus;
import com.zzx.police.data.BaseCmd;
import com.zzx.police.data.CmdEnum;
import com.zzx.police.data.GpsInfo;
import com.zzx.police.data.LoginInfo;
import com.zzx.police.data.Values;
import com.zzx.police.utils.ByteIntConvert;
import com.zzx.police.utils.EventBusUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**@author Tomy
 * Created by Tomy on 2015/7/20.
 */
public class ZZXClient implements Camera.PreviewCallback{
    public static final String DEFAULT_IP = "192.168.199.150";
    public static final String DEFAULT_PORT = "6200";

    private static final int LINK_SERVICE       = 0x2;
    private static final int REGISTER_SERVICE   = 0x3;
    private static final int SEND_BEAT          = 0x4;
    private static final int SEND_START_VIDEO   = 0x5;
    private static final int SEND_ACM     =       0x6;
    private static final int SEND_GPS     =       0x7;
    private static final String TAG = "ZZXClient" ;
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    private HandlerThread mThread;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(7);

    private Socket mCmdSocket;
    private String mIp;
    private int mPort;

    private InputStream mCmdInput;
    private OutputStream mCmdOut;
    private boolean mCmdSocketCreated = false;
    private Handler mHandler;
    private BaseCmd mBeat;
    private byte[] mBeatBuffer;
    private byte[] mStartVideoBuffer;

    private StatusListener mStatusListener;

    private String mUsername;
    private String mPasswork;

    /**Real Time Video*/
    private Socket mSocketVideo = null;
    private OutputStream mOutStreamVideo = null;
    private InputStream mInStreamVideo = null;

    /**表明语音/视频正在申请中,再未返回结果前不能再申请.
     * */
    public boolean mAudioRequesting = false;
    public boolean mVideoRequesting = false;

    private BaseCmd mACK; //接受开始视频的时候的应答命令的cmd
    private byte[] mACKBuffer;

    private BaseCmd mVideo;
    private byte[]  mVideoBuffer;
    public static int mCmdId = 0;
    private SimpleDateFormat mDateFormat;

    /**Register the status listener.
     * Note:The callback won't be sure run in the UI thread.
     * {@link StatusListener#onConnected()}
     * {@link StatusListener#onDisconnected()}
     * */
    public void registerStatusListener(StatusListener listener) {
        mStatusListener = listener;
    }

    public ZZXClient(String username,String psw) {
        this(DEFAULT_IP, DEFAULT_PORT,username,psw);
    }

    public ZZXClient(Context context,String username,String psw) {
        this(Settings.System.getString(context.getContentResolver(), Values.SERVICE_IP_ADDRESS), Settings.System.getString(context.getContentResolver(), Values.SERVICE_IP_PORT),username,psw);
    }
    public ZZXClient(String ip, String port,String username,String psw) {
        this(ip, Integer.parseInt(port), username, psw);
    }
    public ZZXClient(String ip, int port,String username,String psw) {
        mIp     = ip;
        mPort   = port;
        mThread = new HandlerThread(getClass().getName());
        mThread.start();
        mUsername = username;
        mPasswork = psw;
        mHandler = new MainHandler(mThread.getLooper(), new WeakReference<>(this));
        linkService();
    }

    private void linkService() {
        if (mIp != null && mExecutor != null)
            mExecutor.execute(new LinkThread());
    }

    private void registerService(String username,String psw) {
        /**发送登录的socket**/
        sendMsg(new LoginInfo(username, psw).getByte());

        if (mBeat == null) {
            mBeat = new BaseCmd();
            mBeat.mCmdSize = BaseCmd.BASE_CMD_SIZE;
            mBeat.mCmd = CmdEnum.CMD_Heartbeat;
            mBeatBuffer= mBeat.getBytesSmall();
        }
        /**发送心跳包的socket**/
        mHandler.sendEmptyMessage(SEND_BEAT);
        mHandler.sendEmptyMessageDelayed(SEND_GPS, 4000);
    }

    private void sendBeat() {
        sendMsg(mBeatBuffer);
    }

    private void sendStartVideo()
    {
        sendMsg(mStartVideoBuffer);
    }

    private void sendACM() {
        sendMsg(mACKBuffer);
    }

    private void sendGps() {
        if (mDateFormat == null) {
            mDateFormat = new SimpleDateFormat(FORMAT, Locale.getDefault());
        }
        GpsData data = new GpsData();
        GpsInfo info;
        data.mPoliceNumber = mUsername;
        if (MainActivity.mLocation != null) {
            data.mLongitude = String.format("%.6f", MainActivity.mLocation.getLongitude());
            data.mLatitude = String.format("%.6f", MainActivity.mLocation.getLatitude());
            data.mSpeed = String.format("%.2f", MainActivity.mLocation.getSpeed());
            data.mDateTime = mDateFormat.format(System.currentTimeMillis());
            String gpsData = data.toString();
            info = new GpsInfo(Integer.parseInt(mUsername), gpsData.length());
            sendMsg(info.getByte());
            sendMsg(gpsData);
        } else {
            data.mLongitude = String.format("%.6f", VehicleStatus.DEFAULT_LONG);
            data.mLatitude = String.format("%.6f", VehicleStatus.DEFAULT_LATI);
            data.mSpeed = "0.00";
            data.mDateTime = mDateFormat.format(System.currentTimeMillis());
            String gpsData = data.toString();
            info = new GpsInfo(Integer.parseInt(mUsername), gpsData.length());
            sendMsg(info.getByte());
            sendMsg(gpsData);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    /**连接线程**/
    class LinkThread extends Thread {
        @Override
        public void run() {
            try {
                SocketAddress service = new InetSocketAddress(mIp, mPort);
                mCmdSocket  = new Socket();
                mCmdSocket.bind(null);
                mCmdSocket.connect(service, 15 * 1000);
                mCmdSocket.setReuseAddress(true);
                mCmdSocket.setKeepAlive(true);
                mCmdOut     = mCmdSocket.getOutputStream();
                mCmdInput   = mCmdSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int count;
                mCmdSocketCreated = true;
                if (mStatusListener != null) {
                    mStatusListener.onConnected();
                }
                /**登录**/

                mHandler.sendEmptyMessageDelayed(REGISTER_SERVICE, 3 * 1000);
                while ((count = mCmdInput.read(buffer)) != -1) {
                    parserCenterReply(Arrays.copyOfRange(buffer, 0, count));
                }
            } catch (Exception e) {
                e.printStackTrace();
                reset();
            }
        }
    }

    private BaseCmd mBaseCmd;
    /**解析cmd命令**/
    private void parserCmd(byte[] msg) {
        int start = 0;
        boolean hasSign = false;
        for (int i = 0; i < msg.length; i++) {
            if (msg[i] == BaseCmd.BEAT_0) {
                if (msg[i + 1] == BaseCmd.BEAT_1) {
                    if (msg[i + 2] == BaseCmd.BEAT_2) {
                        if (msg[i + 3] == BaseCmd.BEAT_3) {
                            if (mBaseCmd == null) {
                                mBaseCmd = new BaseCmd();
                            }
                            start = i + 4;
                            hasSign = true;
                            break;
                        }
                    }
                }
            }
        }
        if (hasSign) {
            mBaseCmd.mId            = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start += 4));
            mBaseCmd.mCmd           = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start += 4));
            mBaseCmd.mMinorCmd      = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start += 4));
            mBaseCmd.mParam         = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start += 4));
            mBaseCmd.mFlag          = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start += 4));
            mBaseCmd.mReserved      = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start += 4));
            mBaseCmd.mCmdSize       = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start += 4));
            mBaseCmd.mExtendCount   = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start += 4));
            mBaseCmd.mDataLen       = ByteIntConvert.byteArrayToIntSmall(Arrays.copyOfRange(msg, start, start + 4));
        }
        Values.LOG_W(TAG, mBaseCmd.toString());
    }
    private void parserCenterReply(byte[] bytes) {
        parserCmd(bytes);
        Values.LOG_I("CameraService", "service_cmd" + mBaseCmd.mCmd);
        mCmdId = mBaseCmd.mId;
        switch (mBaseCmd.mCmd) {
            case CmdEnum.CMD_Video:
                Values.LOG_I("CameraService","Into_CMD_StartVideo");
                /**1。收到开始视频的指令的时候，发送应答指令。
                 * 2。开启发送视频socket
                 * **/
                if (mBaseCmd.mMinorCmd == CmdEnum.CMD_StartVideo) {
                    EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REGISTER_SUCCESS);
                } else if (mBaseCmd.mMinorCmd == CmdEnum.CMD_StopVideo) {/**停止视频的指令**/
                    EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REPORT_STOP);
                }
                break;
        }
    }
    /**解析中心回复命令
     * @see #parserCmd(String)
     * */
    private void parserCenterReply(String msg) {
        parserCenterReply(msg.getBytes());
    }
    private void parserCmd(String msg) {parserCmd(msg.getBytes());}
    /**发送socket指令**/
    private void sendMsg(byte[] msg) {
        if (mCmdOut == null) {
            return;
        }
        try {
            mCmdOut.write(msg);
            mCmdOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
            reset();
        }
    }
    /**发送socket指令**/
    private void sendMsg(String msg) {
        sendMsg(msg.getBytes());
    }

    private void release() {
        reset();
        try {
            mThread.quit();
            mExecutor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        try {
            mCmdSocketCreated = false;
            if (mCmdSocket != null) {
                mCmdSocket.close();
            }
            mCmdSocket  = null;
            mCmdInput   = null;
            mCmdOut     = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mStatusListener != null) {
            mStatusListener.onDisconnected();
        }
        mHandler.removeMessages(LINK_SERVICE);
        mHandler.sendEmptyMessageDelayed(LINK_SERVICE, 5 * 1000);
    }

        public interface StatusListener {
        void onConnected();
        void onDisconnected();

    }

    /**video视频流部分**/
    public OutputStream getOutStreamVideo() {
        return mCmdOut;
    }

    /**创建视频的socket**/
    class VideoSocketCreateThread extends Thread {
        private String mServiceAddress = null;
        private int mServicePort;
        public VideoSocketCreateThread(String address,int port ) {
            mServiceAddress = address;

            mServicePort=port;
        }
        @Override
        public void run() {
            if (mSocketVideo != null) {
                releaseVideoSocket();
            }
          //  String[] address = mServiceAddress.split(":");
            try {
              /*   InetAddress ip = Inet4Address.getByName(mServiceAddress);
                mSocketVideo = new Socket(ip, mPort);
                mSocketVideo.setKeepAlive(true);
*/
                SocketAddress service = new InetSocketAddress(mServiceAddress, mServicePort);
                mSocketVideo  = new Socket();
                mSocketVideo.bind(null);
                mSocketVideo.connect(service, 15 * 1000);
                mSocketVideo.setReuseAddress(true);
                mSocketVideo.setKeepAlive(true);


                mInStreamVideo = mSocketVideo.getInputStream();
                mOutStreamVideo = mSocketVideo.getOutputStream();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /**改新的指令**/
                        sendAudioVideoCmd(mOutStreamVideo);
                    }
                }, 1000);
                byte[] buffer = new byte[1024];
                int len;
                if (mSocketVideo == null || mSocketVideo.isClosed() || !mSocketVideo.isConnected() || mInStreamVideo == null) {
                    Values.LOG_E(TAG, "mSocketVideo == null || mSocketVideo.isClosed() || !mSocketVideo.isConnected() || mInStreamVideo == null");
                    return;
                }
                /**不断遍历去接受 Input socket的信息**/
                while ((len = mInStreamVideo.read(buffer)) != -1) {
                    Values.LOG_I(TAG, "mInStreamVideo.getBuffer = " + len);
                    String info = new String(buffer, 0, len, "UTF-8");
                    parserCenterReply(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**发送视频数据和视频头
     *
     * 注意:需要同步锁，避免上传视频的时候有其他socket加入**/
    private void sendAudioVideoCmd(OutputStream out ) {

        OutputStreamWriter writer;
        try {
            if (out == null) {
                Values.LOG_E(TAG, "sendAudioVideoCmd.mOutputStreamCmd = null");
                return;
            }
            writer = new OutputStreamWriter(out);//数据
            writer.flush();
            Values.LOG_E(TAG, "sendAudioVideoCmd" );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**释放视频的socket**/
    private void releaseVideoSocket() {
        try {
            if (mSocketVideo != null && !mSocketVideo.isClosed()) {
                mSocketVideo.close();
            }
            if (mInStreamVideo != null) {
                mInStreamVideo.close();
            }
            if (mOutStreamVideo != null) {
                mOutStreamVideo.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocketVideo = null;
        mInStreamVideo  = null;
        mOutStreamVideo = null;
        mVideoRequesting = false;
    }
    private static class MainHandler extends Handler {
        private WeakReference<ZZXClient> mClientReference;
        public MainHandler(Looper looper, WeakReference<ZZXClient> clientWeakReference) {
            super(looper);
            mClientReference = clientWeakReference;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ZZXClient client = mClientReference.get();
            if (client == null) {
                return;
            }

            switch (msg.what) {
                case LINK_SERVICE:
                    client.linkService();
                    break;
                case REGISTER_SERVICE:
                    client.registerService(client.mUsername,client.mPasswork);
                    break;
                case SEND_BEAT:
                    client.sendBeat();
                    sendEmptyMessageDelayed(SEND_BEAT, 10 * 1000);
                    break;
                case SEND_START_VIDEO:
                    client.sendStartVideo();
                    break;
                case SEND_ACM:
                    client.sendACM();
                    break;
                case SEND_GPS:
                    client.sendGps();
                    sendEmptyMessageDelayed(SEND_GPS, 5 * 1000);
                    break;
            }
        }
    }


}
