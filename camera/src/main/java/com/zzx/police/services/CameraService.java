package com.zzx.police.services;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zzx.police.MainActivity;
import com.zzx.police.R;
import com.zzx.police.ZZXConfig;
import com.zzx.police.activity.PasswordBaseActivity;
import com.zzx.police.adapter.CaptureRecordPopListAdapter;
import com.zzx.police.autofocus.SensorController;
import com.zzx.police.command.BaseProtocol;
import com.zzx.police.command.VehicleSOSReport;
import com.zzx.police.command.VehicleStatus;
import com.zzx.police.data.FTPUploadInfo;
import com.zzx.police.data.FileInfo;
import com.zzx.police.data.IntentInfo;
import com.zzx.police.data.StoragePathConfig;
import com.zzx.police.data.Values;
import com.zzx.police.manager.RecordManager;
import com.zzx.police.manager.VibratorManager;
import com.zzx.police.task.DatabaseInsertTask;
import com.zzx.police.utils.AESSasynManage;
import com.zzx.police.utils.AudioEncoder2;
import com.zzx.police.utils.AudioSaver;
import com.zzx.police.utils.CheckUtils;
import com.zzx.police.utils.ConfigXMLParser;
import com.zzx.police.utils.DatabaseUtils;
import com.zzx.police.utils.DeleteFileUtil;
import com.zzx.police.utils.DeviceUtils;
import com.zzx.police.utils.EncryptionUtil;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.utils.FTPUploadUtil;
import com.zzx.police.utils.FileUtils;
import com.zzx.police.utils.LogUtil;
import com.zzx.police.utils.LolaLiveSDKUtil;
import com.zzx.police.utils.LowPowerUtil;
import com.zzx.police.utils.ProtocolUtils;
import com.zzx.police.utils.ProximitySensor;
import com.zzx.police.utils.RemotePreferences;
import com.zzx.police.utils.SOSInfoUtils;
import com.zzx.police.utils.SystemUtil;
import com.zzx.police.utils.TTSToast;
import com.zzx.police.utils.ThumbnailUtil;
import com.zzx.police.utils.TimeFormatUtils;
import com.zzx.police.utils.UploadFileUtil;
import com.zzx.police.utils.VideoCut;
import com.zzx.police.utils.VideoEncoder;
import com.zzx.police.utils.WifiAdmin;
import com.zzx.police.utils.ZZXMiscUtils;
import com.zzx.police.view.MainLayout;
import com.zzx.police.view.ftpdown.FloatingWindowBig;
import com.zzx.police.view.ftpdown.FloatingWindowSmall;
import com.zzx.police.webrtc.FastModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static android.content.SharedPreferences.Editor;
import static android.hardware.Camera.PictureCallback;
import static android.hardware.Camera.PreviewCallback;
import static android.hardware.Camera.open;
import static android.view.WindowManager.LayoutParams;
import static com.zzx.police.R.id.btn_zoom_increase;
import static com.zzx.police.data.Values.BLE_STATUS_PIC;
import static com.zzx.police.data.Values.BLE_STATUS_VON;
import static com.zzx.police.data.Values.BLE_STATUS_VOU;

/**
 * @author Tomy
 * Created by Tomy on 2014/6/26.
 */
public class CameraService extends Service implements SurfaceHolder.Callback, View.OnTouchListener, View.OnClickListener, PreviewCallback,
        GpsStatus.NmeaListener, LocationListener, Camera.AutoFocusCallback, SharedPreferences.OnSharedPreferenceChangeListener
        , FloatingWindowSmall.OnSmallWindowClickListener {
    public static final String WIFI_SCAN = "wifi_scan";
    public static final String GPS_SRT = "ZZXGpsSrt";
    public static final String RECORD_RESTART = "ZZXRecordRestart";
    private boolean IsPicing = false;

    /**
     * @see #LIGHT_COLOR_NO             无色(呼吸灯颜色) 此时呼吸灯相当于关闭状态
     * @see #LIGHT_COLOR_BLUE           蓝色 指示灯闪烁
     * @see #LIGHT_COLOR_RED            红色 指示灯闪烁
     * @see #LIGHT_COLOR_GREEN          绿色 指示灯闪烁
     * @see #LIGHT_COLOR_YELLOW         黄色 指示灯闪烁
     * @see #LIGHT_COLOR_BLUE_NO_FLASH  蓝色 不闪烁
     * @see #LIGHT_COLOR_RED_NO_FLASH   红色 不闪烁
     * @see #LIGHT_COLOR_GREEN_NO_FLASH 绿色 不闪烁
     */
    public static final String LIGHT_COLOR_NO = "0x000000 0 0 0 0 0 0";
    public static final String LIGHT_COLOR_BLUE = "0x0000ff 1 2 2 2 2 2";
    public static final String LIGHT_COLOR_RED = "0x007000 1 2 2 2 2 2";
    public static final String LIGHT_COLOR_GREEN = "0x700000 1 2 2 2 2 2";
    public static final String LIGHT_COLOR_YELLOW = "0x707000 1 2 2 2 2 2";
    public static final String LIGHT_COLOR_BLUE_NO_FLASH = "0x000070 0 0 0 0 0 0";
    public static final String LIGHT_COLOR_RED_NO_FLASH = "0x007000 0 0 0 0 0 0";
    public static final String LIGHT_COLOR_GREEN_NO_FLASH = "0x700000 0 0 0 0 0 0";
    /**
     * rgb ---> bgr
     * 无色(呼吸灯颜色) 此时呼吸灯相当于关闭状态
     */
    public static final String LIGHT_COLOR_NO_5G = "0x000000 0 0 0 0 0 0";
    /**
     * 蓝色 指示灯闪烁    bgr
     */
    public static final String LIGHT_COLOR_BLUE_5G = "0x100000 1 2 2 2 2 2";
    /**
     * 红色 指示灯闪烁
     */
    public static final String LIGHT_COLOR_RED_5G = "0x000010 1 2 2 2 2 2";
    /**
     * 绿色 指示灯闪烁
     */
    public static final String LIGHT_COLOR_GREEN_5G = "0x001000 1 2 2 2 2 2";
    /**
     * 黄色 指示灯闪烁
     **/
    public static final String LIGHT_COLOR_YELLOW_5G = "0x001010 1 2 2 2 2 2";
    /**
     * 蓝色 不闪烁
     */
    public static final String LIGHT_COLOR_BLUE_NO_FLASH_5G = "0x100000 0 0 0 0 0 0";
    /**
     * 红色 不闪烁
     */
    public static final String LIGHT_COLOR_RED_NO_FLASH_5G = "0x001000 0 0 0 0 0 0";
    /**
     * 绿色 不闪烁
     */
    public static final String LIGHT_COLOR_GREEN_NO_FLASH_5G = "0x001000 0 0 0 0 0 0";
    public static final int WIFI_SCAN_ENABLED = 1;
    public static final int WIFI_SCAN_DISABLED = -1;
    public static final int SCAN_RECORD_TIME = 3 * 60 * 1000;
    public static final String REPORT_QUALITY = "zzxReportQuality";
    public static final String ACTION_POWER_DOWN_MODE = "action_power_down_mode";
    public static final String MODE_STATUS = "status";
    private static final String TAG = "CameraService: ";
    /**
     * 监控视频流数据队列的大小.
     */
    private static final int REPORT_DEQUE_SIZE = 30;
    private static final String MTK_CAM_MODE = "mtk-cam-mode";
    private static final String CAMERA_ID = "camera_id";
    private static final String RECORDING_STATUES = "isAutoVideo";
    /**
     * 判断是否从警务通客户端收到开始录像命令.若已收到则不能手动控制录像或者停止.
     */
    public static boolean mReceiveStartRecordFromClient = false;
    public static boolean mNeedReportVideo = false;//这里理解为是否正在实时上传，感觉更符合当前场景
    public static boolean mIsRepeatSend = false;   //是否需要重新发送 开启实时上传的命令(在主摄像头丶前置摄像头丶外置摄像头切换时用到)
    public static PopupWindow mPopWindow;
    public static int mRecordTime = 0;
    public static int MODE_ON = 1;
    public static int MODE_OFF = 0;
    public static boolean isOpenPSM = false;//打开省点模式
    private static boolean mIsWindowMax = false;
    private static boolean mStorageNotEnough = false;
    private final Object mLockObject = new Object();
    private Camera mCamera = null;
    private WindowManager mWinManager = null;
    private MainLayout mRootContainer = null;
    private SurfaceView mSurfaceView = null;
    private ViewGroup mLayoutParams;
    private ViewGroup mLayoutPerform;
    //    private ImageButton mBtnFlashLight;
    private ImageButton mBtnShutterSound;
    private ImageButton mBtnResolution;
    private ImageButton mBtnMode;
    private ImageButton mBtnCaptureRecord;
    private ImageButton mBtnThumb;
    private ImageButton mBtnSwitcher;
    private ImageButton mBtnRecordAuto;
    private ImageButton mBtnZoomDecrease;
    private ImageButton mBtnZoomIncrease;
    private ImageButton mFlashEasyicon;
    private ImageView mIvMode;
    private ImageView mIvZoom;
    private ImageView mIvRecordState;
    private ImageView mIvImportantFile;
    private TextView mTvStorageNotEnough;
    private TextView mTvRecordTime;
    private TextView mTvCountTime;
    //定义大小悬浮窗的layout参数
    private WindowManager.LayoutParams mParamsSmall;
    //定义大小悬浮窗的layout参数
    private FloatingWindowBig mWindowBig;
    private FloatingWindowSmall mWindowSmall;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mIsAddedSmallWindow;
    private boolean mIsAddedBigWindow;
    private GestureDetector mDetector;
    private PreferenceManager mPreferenceManager = null;
    private Camera.Parameters mParameters;
    private List<String> mFlashModes = null;
    private boolean mFlashModeOnAvailable = false;
    private boolean mFlashModeOffAvailable = false;
    private boolean mCaptureFlashModeOn = false;
    private boolean mRecordFlashModeOn = false;
    private boolean mShutterSoundEnabled = true;
    private boolean mIsCapture = true;
    private boolean mModeFullScreen = false;
    private PopupWindow mSecondPopWindow = null;
    //    private View mRecordSetPopView;
//    private View mPicPopView;
    private ListView mPicModeChildView;
    private ListView mModeListView = null;
    private ListView mPicListView = null;
    private CaptureRecordPopListAdapter mPicModeRootAdapter = null;
    private CaptureRecordPopListAdapter mPicModeChildAdapter;
    private MediaRecorder mMediaRecorder = null;
    private Class<?> mPreferenceManagerCls;
    private Method mGetScreenMethod;
    private boolean mFirstInitPreference = true;
    private Resources mRes = null;
    private int mPicMode = Values.PIC_MODE_SIGNAL;
    private Editor mSPEditor = null;
    private static UploadFileHandler mUploadFileHandler;
    private Handler mHandler = null;
    private Handler mThreadHandler = null;
    private HandlerThread mHandlerThread;
    private CaptureRecordPopListAdapter mPicResolutionAdapter;
    public static boolean mIsRecording = false;
    /**
     * 1.没有实时上传时，正在录像，此时开启实时上传，为确保录像能够有正常数据，需要停止当前录像，然后重新录像.不然录像数据不正常
     */
    private boolean mIsStopRecording = false;
    /**
     * 文件名命名格式
     **/
    private SimpleDateFormat mFileDateFormat;
    /**
     * 录像时长格式
     **/
    private SimpleDateFormat mRecordDateFormat;
    private RecordManager mRecordManager;
    private String mVideoFilePath;
    private String mPicPath;
    private CaptureRecordPopListAdapter mRecordResolutionAdapter;
    private boolean isFirst = true;
    private LayoutParams mWindowParams;
    private PicResolutionItemListener mRecordRatioItemClickListener;
    private PicResolutionItemListener mCaptureRatioItemClickListener;
    private BroadcastReceiver mCameraReceiver;
    private MediaActionSound mCameraSound;
    private boolean mPreRecording = false;
    private PicModeListClickListener mPicModeRootClickListener;
    /**
     * 预录模式的时长.
     **/
    private int mPreRecordModeValue;
    /**
     * 预录模式
     **/
    private boolean mPreRecordMode = false;
    /**
     * 分段模式
     **/
    private boolean mPortRecordMode = false;
    /**
     * 预录的录像个数,>= 2时删除上一个.
     **/
    private int mPreRecordCount = 0;
    /**
     * 判断是否是人为的正在录像状态,与预录模式时有用
     **/
    private boolean mManualRecording = false;
    /**
     * 判断是否是人为的停止录像状态(包括按键及分段模式),与预录模式时有用.人为则判断是否执行预录模式.自动的则.
     **/
    private boolean mManualStopRecord = false;
    /**
     * 上次视频的路径,用来在预录模式下自动删除.
     **/
    private String mPreVideoFilePath;
    private boolean mContinuousCaptureMode = false;
    private int mContinuousCaptureTotalCount = 0;
    private Executor mExecutor;
    private SoundPool mBurstSound;
    private int mFastPicSoundID = 0;
    private int mPicSoundID = 0;
    private int mPicNorSoundID = 0;
    private int mPicModeSoundID = 0;
    private int mRecordModeSoundID = 0;
    private int mRecordNorSoundID = 0;
    private int mRecordStartSoundID = 0;
    private int mStartRecordSoundID = 0;
    private int mStopRecordSoundID = 0;
    private int mStreamID = 0;
    private int mCameraZoomMax = 0;
    private boolean mVideoLongPress = false;
    private int mVideoLongPressNum = 0;
    private boolean mImportantFileSign = false;
    private boolean mIntervalOrTimerMode = false;
    private boolean mCameraReady = false;
    private SurfaceHolder mSurfaceHolder;
    private int mIDLECount = 0;
    private boolean mCameraIDLEMode = false;
    private boolean mUserRecording = false;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager.WakeLock mRealWakelock;
    private AnimationDrawable mRecordAnimation;
    private PowerManager mPowerManager;
    private BitmapDrawable mImgThumbnail;
    private int mZoomLevel = 0;
    private boolean mCaptureEnabled = true;
    private String mDate;
    private int mCutTime = 0;
    private boolean mFrontCamera = false;
    private boolean mBehindCamera = false;  //区分后置两个摄像头
    private ImageView mBtnCameraSwitcher, mBtnFlash, mBtnLaser, mBtnIrcut, mBtnMenu,mBtnUsbCameraSwitcher;
    private ImageView mBtnLaserSwitcher;
    private boolean isBtnMenu = false;
    private boolean isOpenMenu = false;
    private boolean isSubject = false;
    private String mFileName;
    private String mGpsFileFullPath;
    private String mPicTime;
    private WifiReceiver mWifiReceiver;
    private boolean mWakeLocked = false;
    private boolean mQRScan;
    //用来计数检测TF卡的次数,超过则不再判断
    private int mCheckStorageCount = 0;
    private boolean mForestRanger = false;
    private View mReportContainer;
    private LayoutParams mReportParams;
    private boolean mReportShowed = false;


    //记录物理键！的按下的次数
    private boolean mHttpStartRecord = false;
    /**
     * 实时监控上传分辨率的宽度
     */
    private int mReportWidth;
    /**
     * 实时监控上传分辨率的高度
     */
    private int mReportHeight;
    private LocationManager mLocationManager;
    private String mNmea;
    private BufferedWriter mGpsWriter;
    private int mRecordCount = 0;
    private boolean mGpsSrtEnabled = true;
    /**
     * 代表是否监控的时候同时录像.
     */
    private boolean mReportWithRecord = false;
    private boolean isShowStorageNotEnoughDailog = true;
    private SoundPool mDidiSoundPool;
    private int mDidiStreamID;
    private int mDidiSoundID;
    private int mDidianSoundID;
    private int mTalkSoundID;
    private int mNumActionSos = 0;
    private View view_focus = null;
    private RelativeLayout mRlSufaceView;
    private int mCurrentOrientation = 0; // 当前角度
    private View mPowerSaveContainer;
    private View mNotEnoughContainer;

    private AudioReportTask mAudioTask;
    private boolean mEncode = true;
    private String mPrePicTime = "";
    private int mPreSameCount = 0;
    private PictureCallback mJpegPictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RESTART_PREVIEW);
            writeData(data);
            insertDatabases();
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.IMG_THUMBNAIL);
            IsPicing = false;
            isKeyPic = false;
            isContinuousPic = false;
            Settings.System.putString(getContentResolver(),"case_number","");
            if (!isScreen){
                mHandler.sendEmptyMessageDelayed(Values.RELEASE_CAMERA,2*60*1000);
            }
        }
    };
    private List<byte[]> mDataList = new ArrayList<>();
    private int mContinuousPicCount = 0;
    /**
     * 高速连拍模式为了更有效率的完成连拍速度,先只将拍摄的data保存到列表中,全部完成后再写入文件.
     */
    private boolean isContinuousPic =false;
    private PictureCallback mContinuousJpegPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mDataList.add(data);
            if (++mContinuousPicCount >= mContinuousCaptureTotalCount) {
                mBurstSound.stop(mStreamID);
                mContinuousPicCount = 0;
                mExecutor.execute(new StoreContinuousPicThread());
//                if (SystemUtil.isNetworkConnected(CameraService.this) && Settings.System.getInt(getContentResolver(),"ZZXAutomatic",0)==1 && paths.size() > 0) {
//                    for (int j = 0; j < paths.size(); j++) {
//                        mUploadFileUtil.uploadVideoFile(FTPUploadInfo.getFTPUploadInfo(CameraService.this, paths.get(j)));
//                    }
//                }
            }
//            paths.clear();
        }
    };
    private VideoEncoder mVideoEncoder = null;
    private Deque<byte[]> mDeque;
    private RealTimeTask mRealTimeTask;
    private int mPreCount = 0;
    private AudioSaver saver;
    private long mLastTime = 0;
    private long mTimeSpan = 0;
    public static boolean isSoundReecord = false;
    public static boolean isVideoReecord = false;
    private boolean isIrCutOpen = false;
    private boolean mTalkShowed = false;
    private boolean mAudioRecording = false;
    private AudioEncoder2 mAudioEncoder;
    private Map<String, Runnable> mSOSMap = new HashMap<>();
    private MODE mode = MODE.NONE;// 默认模式
    private static final int AUTO_WAIT_TIME = 3000;//单位:ms
    //保证触摸对焦的优先级高于自动对焦
    //即:1.正在进行自动对焦，可以进行触摸对焦
    //   2.正在进行触摸对焦，AUTO_WAIT_TIME 时间内不能进行自动对焦
    private boolean mIsTouchFocus = false;
    private final Object mFocusLock = new Object();
    private Animation mFocusViewAnim;
    private int mCameraId = 0;
    private String result = null;
    private AESSasynManage aesSasynManage;
    private String encryptingFileName;//加密后文件名字
    private String videoCutFilePath;//停止录像分段时需要分段文件路径
    private EncryptionUtil mEncryptionUtil;
    private UploadFileUtil mUploadFileUtil;
    private LowPowerUtil mLowPowerUtil;
//    private LolaLiveUtil mLolaLiveUtil;
    private LolaLiveSDKUtil mLolaLiveSDKUtil;
    private int lightvalue = 0;
    public boolean GPSstate;
    public static boolean isOccupy;//第三方应用是否占用摄像头
    private WifiAdmin mWifiAdmin;
    private String mUuid;
    private String imei;
    private boolean isScreen = false;
    /**
     * 点击显示焦点区域
     */
    View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @SuppressWarnings("deprecation")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            if(mCameraId==0){
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                //如果是单摄像头版本直接有聚焦模式
                synchronized (mFocusLock) {
                    mIsTouchFocus = true;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int width = view_focus.getWidth();
                    int height = view_focus.getHeight();
                    view_focus.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_focus_focusing));
                    view_focus.setX(event.getX() - (width / 2));
                    view_focus.setY(event.getY() - (height / 2));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mode = MODE.FOCUSING;
                    focusOnTouch(event);

                }
//            }
//            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT || mCamera.getNumberOfCameras() == 1) {
//                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
//                //如果是单摄像头版本直接有聚焦模式
//                synchronized (mFocusLock) {
//                    mIsTouchFocus = true;
//                }
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    int width = view_focus.getWidth();
//                    int height = view_focus.getHeight();
//                    view_focus.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_focus_focusing));
//                    view_focus.setX(event.getX() - (width / 2));
//                    view_focus.setY(event.getY() - (height / 2));
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    mode = MODE.FOCUSING;
//                    focusOnTouch(event);
//
//                }
//                //   }
//            }

            return true;
        }
    };

    /**
     * 为警务通增加判断是否使用扫描二维码即可判断为是否是警务通应用,是则开机录像.
     */
    public static boolean checkQRWifi(Context context) {
        return Settings.System.getInt(context.getContentResolver(), WIFI_SCAN, WIFI_SCAN_DISABLED) == WIFI_SCAN_ENABLED;
    }

    /**
     * 返回当前Camera界面是否最大化,即是否处于可视状态,来判断返回键是最小化还是退出.
     */
    public static boolean isWindowMax() {
        return mIsWindowMax;
    }

    public static void openPowerSaveMode(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_POWER_DOWN_MODE);
        intent.putExtra(MODE_STATUS, MODE_ON);
        context.sendBroadcast(intent);
    }

    public static void closePowerSaveMode(Context context) {
        Intent intent1 = new Intent();
        intent1.setAction(ACTION_POWER_DOWN_MODE);
        intent1.putExtra(MODE_STATUS, MODE_OFF);
        context.sendBroadcast(intent1);
    }

    //写入rgb灯的值
    private static void writeToDevice(int state, String filePath, String rgbstate) {
        File file = new File(filePath);
        FileWriter outputStream;
        try {
            outputStream = new FileWriter(file);
            if (state == 1) {
                outputStream.write(rgbstate);
            } else if (state == 0) {
                outputStream.write("0");
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /*try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    /**
     * 判断是否为外置SD卡
     */
    public static boolean isExternalStorageEnabled() {
        boolean isCheck;
        if (ZZXConfig.IsCheckExternalStorage) {
            isCheck = Environment.isExternalStorageRemovable() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } else {
            isCheck = true;
        }
        return isCheck;
    }

    /**
     * 计算角度，以45度为界限，小于45度不做操作，大于45度直接进位，增加45度（相当于44舍45入）
     *
     * @param x 当前角度值
     * @return 计算后的值
     */
    public static int calc(int x) {
        return ((x + 45) / 90 * 90) % 360;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ZZXConfig.IsRotate) {
            initOritntationListener(); // 开启感应器事件
        }
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        switch (action) {
            case Values.ACTION_MAX_WINDOW:
                maxWindow();
                String extra = intent.getStringExtra(Values.CAMERA_SERVICE_EXTRA_STRING);
                if (extra != null) {
                    if (extra.equals(Values.EXTRA_TAKE_PICTURE)) {
                        if (!isVideoReecord&&!isPreRecrod){
                            toggleToPictureMode();
                        }
                    } else if (extra.equals(Values.EXTRA_RECORD)) {
                        if (!isVideoReecord){
                            toggleToRecordMode();
                        }
                    }
                }
                //minWindow();
                break;
            case Values.ACTION_MIN_WINDOW:
                minWindow();
                break;
            case Values.ACTION_RELEASE_CAMERA:
                //释放摄像头
                releaseCamera();
                if (mFrontCamera) {
                    Settings.System.putInt(getContentResolver(), Values.CAMERA_ID, 0);
                } else {
                    Settings.System.putInt(getContentResolver(), Values.CAMERA_ID, 1);
                }

                break;
            case Values.ACTION_RESTART_CAMERA:
                break;
        }
        return START_NOT_STICKY;
    }

    /**
     * 每次开机启动或者人工停止录像时都会根据预设的录像模式来进行检测.以下根据优先权列举:
     * 1.   若设置了预录,则不断根据预录时间录制,自动录像第二个完成时删除上一个录像.且此模式下自动录像时不刷新UI.
     * 2.   若第一次开机启动未设置预录而设置了开机录像,则视分段录像有无设置来判断是否设置录像最大时间.此模式下刷新UI.
     */
    private void initRecordFromRecordMode() {
        SharedPreferences remotePreferences = RemotePreferences.getRemotePreferences(this);
        mPreRecordModeValue = Integer.parseInt(remotePreferences.getString(getKey(R.string.key_record_pre), "0"));
        Values.LOG_I(TAG, "initRecordFromRecordMode().mPreRecordModeValue = " + mPreRecordModeValue);
        if (mPreRecordModeValue != 0) {
            mPreRecordModeValue += 3;
//            acquireWakeLock();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isExternalStorageEnabled()) {
                        startPreRecordMode();
                        mHandler.removeCallbacks(this);
                    } else {
                        mHandler.postDelayed(this, 1000);
                    }
                }
            });
            return;
        } else if (isFirst && remotePreferences.getBoolean(getKey(R.string.key_record_auto), false)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isExternalStorageEnabled()) {
                        toggleToRecordMode();
                        startRecordAsUser();
                        mHandler.removeCallbacks(this);
                    } else {
                        if (mCheckStorageCount++ <= 5) {
                            mHandler.postDelayed(this, 1000);
                        } else {
                            mCheckStorageCount = 0;
                            mHandler.removeCallbacks(this);
                        }
                    }
                }
            });

        } else {
//            releaseWakeLock();
        }
        mPreRecordMode = false;
    }

    /**
     * 启动预录模式.
     */
    private void startPreRecordMode() {
        mPreRecordMode = true;
        initRecorderAndRecord();
//        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.BACK_START_RECORD);
        mPreRecordCount += 1;
        mHandler.sendEmptyMessageDelayed(Values.BACK_RECORD_STOP, mPreRecordModeValue * 1000);
    }

    @Override
    public void onCreate() {
        result = Settings.System.getString(getContentResolver(),"result");
        isFirst = true;
        mIsWindowMax = true;
        InitSoundPool();
        if (isOPen(this)){
            writeToDevice(1,"/sys/devices/platform/zzx-misc/beidou_stats");
        }else {
            writeToDevice(0,"/sys/devices/platform/zzx-misc/beidou_stats");
        }
        mQRScan = checkQRWifi(this);
        if (mRes == null)
            mRes = getResources();
        SharedPreferences preferences = RemotePreferences.getRemotePreferences(this);
        mExecutor = Executors.newCachedThreadPool();
        mSPEditor = preferences.edit();
        initCameraReceiver();
        initLocationListener();
        controllDevice(1, LIGHT_COLOR_GREEN_NO_FLASH);

        if (result==null){
            mFileDateFormat = new SimpleDateFormat(Values.FILE_NAME_DATA_FORMAT, Locale.getDefault());
        }else {
            mFileDateFormat = new SimpleDateFormat(Values.FILE_NAME_DATA_FORMATS, Locale.getDefault());
        }

        mRecordManager = new RecordManager(this);

        mHandler = new EventHandler();
        mUploadFileHandler = new UploadFileHandler(new WeakReference<>(CameraService.this));
        mHandlerThread = new HandlerThread(getPackageName());
        mHandlerThread.start();
        mThreadHandler = new ThreadHandler(new WeakReference<>(CameraService.this), mHandlerThread.getLooper());
        initThreadHandler();
        getContentResolver().registerContentObserver(Settings.System.getUriFor(Values.ACTION_SCREEN_KEEP_ON), true, new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                try {
                    int value = Settings.System.getInt(getContentResolver(), Values.ACTION_SCREEN_KEEP_ON, 0);
                    switch (value) {
                        case 1:
                            keepScreenOn();
                            break;
                        case 0:
                            notKeepScreenOn();
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    notKeepScreenOn();
                }
            }
        });
        mReportWithRecord = Settings.System.getInt(getContentResolver(), RECORD_RESTART, 1) == 1;
        EventBusUtils.registerEvent(Values.BUS_EVENT_CAMERA, this);
        EventBusUtils.registerEvent(Values.BUS_EVENT_SOCKET_CONNECT, this);
        initLayout();
        initWindowManager();
        initPopupWindow();
        initSensorController();
//        initAccelerometerSensor();//初始化加速度传感器
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        showswitchlayout(RemotePreferences.getRemotePreferences(this).getString(getKey(R.string.key_record_pre), "0"));
        mEncryptionUtil = new EncryptionUtil(this);
        //开机给未加密文件加密
//        if (ZZXConfig.isOK("ZZXEncryption")){
//            mEncryptionUtil.taskFile();
//        }
        mEncryptionUtil.taskFile();
        FileUtils.getUnencryptedFile(new File(Values.ROOT_PATH));
        mUploadFileUtil = new UploadFileUtil(this);
        if (ZZXConfig.isP5XD_SXJJ){
            if (SystemUtil.isNetworkConnected(this)){
                mUploadFileUtil.upFile();
            }
        }
        mLowPowerUtil = new LowPowerUtil(this);
        if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
            mWifiAdmin = new WifiAdmin(this);
            String imeis = SystemUtil.getIMEI(this);
            Log.i(TAG, "imeis: "+imeis);
            if (imeis!=null){
                imei = imeis.substring(imeis.length()-6,imeis.length());
            }else {
                imei = "000000";
            }
            mUuid = "mtm"+imei;
//            mLolaLiveSDKUtil = new LolaLiveSDKUtil(this,imei);
//            initLolaLiveSDK();
        }
        mHandler.sendEmptyMessageDelayed(Values.IS_HOME,60*1000);
        super.onCreate();
    }

    private void initLolaLiveSDK(){
        mLolaLiveSDKUtil = new LolaLiveSDKUtil(CameraService.this,imei);
//        mLolaLiveSDKUtil.initSession();
    }

    /**
     * 判断当前界面是否为桌面
     */
    private boolean inHome(){
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        List<String> strs = getHomes();
        if (strs != null && strs.size() > 0){
            return strs.contains(rti.get(0).topActivity.getPackageName());
        }else {
            return false;
        }

    }
    /**
     * 判断当前应用是否为智能翻译
     */
    private boolean isTranslation(){
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos  = mActivityManager.getRunningTasks(1);
        if (runningTaskInfos != null && runningTaskInfos.size() != 0){
            return (runningTaskInfos.get(0).topActivity.getPackageName().equals(Values.PACKAGE_NAME_TRANSLATION));
        }else {
            return false;
        }
    }
    /**
     * 获得属于桌面的应用的应用包名称
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        //属性
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo ri : resolveInfo){
            names.add(ri.activityInfo.packageName);
            Log.i(TAG, "packageName =" + ri.activityInfo.packageName);
        }
        return names;
    }
    private void initAccelerometerSensor() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> ls = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (ls.size() > 0) {
            Sensor s = ls.get(0);
            sm.registerListener(new SensorEventListener() {

                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        float a_x = Math.abs(event.values[0]);
                        float a_y =Math.abs(event.values[1]);
                        float a_z =Math.abs(event.values[2]);
//                        LogUtil.e("zzxzdm","a_x= "+a_x +" a_y= "+a_y+" a_z= "+a_z );
                        if (a_x > 32.2 || a_y > 32.2 || a_z > 32.2) {
                            if (!CheckUtils.IsContinuousClick(500)){
                                return;
                            }
                            mDidiStreamID = mBurstSound.play(mDidiSoundID, 1.0f, 1.0f, 0, 5, 1.0f);
                            mHandler.sendEmptyMessage(Values.ACCELEROMETER_SENSOR);
                        }
                    }
                }

                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }
    private boolean isPreRecrod = false;//是否预录
    private void showswitchlayout(String string) {
        if (Integer.parseInt(string) == 0) {  //预览情况下 不能切换到拍照模式
            mBtnSwitcher.setVisibility(View.VISIBLE);
            isPreRecrod = false;
        } else {
            isPreRecrod = true;
            mBtnSwitcher.setVisibility(View.GONE);
        }
        if (Settings.System.getInt(getContentResolver(),"camera_light_state",0)==1){
            writeToDevice(1,"/sys/devices/platform/zzx-misc/camera_light_state");
        }else {
            writeToDevice(0,"/sys/devices/platform/zzx-misc/camera_light_state");
        }
    }

    /**
     * 初始化各种声音文件
     **/
    private void InitSoundPool() {
        if (mBurstSound == null) {
            mBurstSound = new SoundPool(12, 7, 0);
            mFastPicSoundID = mBurstSound.load("/system/media/audio/ui/camera_shutter.ogg", 1);
            mPicSoundID = mBurstSound.load(this, R.raw.takepic, 1);
            mStartRecordSoundID = mBurstSound.load(this, R.raw.start_record, 1);
            mStopRecordSoundID = mBurstSound.load(this, R.raw.stop_record, 1);
            mPicModeSoundID = mBurstSound.load(this, R.raw.pic_mode, 1);
            mPicNorSoundID = mBurstSound.load(this, R.raw.camera_click, 1);
            mRecordNorSoundID = mBurstSound.load(this, R.raw.video_record, 1);
            mRecordStartSoundID = mBurstSound.load(this,R.raw.start_jobs,1);
            mRecordModeSoundID = mBurstSound.load(this, R.raw.record_mode, 1);
            mDidiSoundID = mBurstSound.load(this, R.raw.didi, 1);
            mDidianSoundID = mBurstSound.load(this, R.raw.didian, 1);
            mTalkSoundID =  mBurstSound.load(this, R.raw.cnwav, 1);
        }
    }

    private void initLocationListener() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        mLocationManager.addNmeaListener(this);
    }

    private void releaseLocationListener() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
            mLocationManager.removeNmeaListener(this);
        }
    }

    private void initThreadHandler() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateRecordState();
                mHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    /**
     * 每一秒刷新录像时间UI.
     */
    private void updateRecordState() {
        if (mRecordDateFormat == null) {
            mRecordDateFormat = new SimpleDateFormat(Values.RECORD_DATE_FORMAT, Locale.getDefault());
            mRecordDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        }
        if (ZZXConfig.isP5XJNKYD){
            mTvRecordTime.setVisibility(View.GONE);
            mIvRecordState.setVisibility(View.GONE);
            return;
        }
        if (mUserRecording) {
            if (!mTvRecordTime.isShown()) {
                mTvRecordTime.setVisibility(View.VISIBLE);
                mIvRecordState.setVisibility(View.VISIBLE);
                mRecordAnimation = (AnimationDrawable) mIvRecordState.getDrawable();
                mRecordAnimation.start();
            }
            String time = mRecordDateFormat.format(new Date((mRecordTime++) * 1000));
            mTvRecordTime.setText(time);
        } else {
            if (mTvRecordTime.isShown()) {
                mTvRecordTime.setVisibility(View.GONE);
                mIvRecordState.setVisibility(View.GONE);
                //20201209
                if (mPreRecordMode) {
                    mRecordTime = 0;
                }
                if (mRecordAnimation != null) {
                    mRecordAnimation.stop();
                    mRecordAnimation = null;
                }
            }
        }
    }

    private void initCameraReceiver() {
        mCameraReceiver = new CameraReceiver();
        IntentFilter filter = new IntentFilter(Values.ACTION_WIFI_SCAN_FINISHED);
        filter.setPriority(999);
        filter.addAction(Values.ACTION_UEVENT_RECORD);
        filter.addAction(Values.ACTION_UEVENT_CAPTURE);
        filter.addAction(Values.ACTION_UEVENT_MIC);
        filter.addAction(Values.ACTION_SYSTEM_KEY_BACK);
        filter.addAction(Values.ACTION_SYSTEM_KEY_HOME);
        filter.addAction(Values.ACTION_SYSTEM_KEY_MENU);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        filter.addAction(Intent.ACTION_BATTERY_LOW);

        filter.addAction(Values.PHOTOS);
        filter.addAction(Values.RED_CUT);
        filter.addAction(Values.COM_KEY_CODE_RECORD);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Values.ACTION_SOS);//  镭射灯
        filter.addAction(Values.ACTION_PPT);//  镭射灯
        filter.addAction(Values.ACTION_MIC_RECORD);
        filter.addAction(Values.SERVER_ACTION_MAX_WINDOW);
        filter.addAction(Values.SERVER_ACTION_MIN_WINDOW);
        filter.addAction(Values.ACTION_RECORD_PREFERENCE_NOTIFY);
        filter.addAction(Values.ACTION_CAMERA_LIGHT);
        filter.addAction(Values.ACTION_CAMERA_DARK);
        filter.addAction(Values.BLE_ACTION);
        filter.addAction(Values.ACTION_SOUND);
        filter.addAction(Values.FILE_UPLOAD);
        filter.addAction(Values.LOGCAT_MIC);//用于接受 录音的状态，用来判断 UI的开始录音的一键切换（停止录像）
        filter.addAction(Values.ACTION_RECEIVER_UVC_CAMERA);//USB摄像头
        filter.addAction(Values.ACTION_USB_CAMERA_STATUES);
        filter.addAction(Values.ACTION_EVENT_RECORD);
        filter.addAction(Values.ACTION_EVENT_SWITCH);
        filter.addAction(Values.ACTION_EVENT_PHONOS);
        filter.addAction(Values.ACTION_EVENT_SUBTRACT);
        filter.addAction(Values.ACTION_EVENT_ADD);
        filter.addAction(Values.ACTION_DROP);
        filter.addAction(Values.ZZX_ACTION_BATSW);
        filter.addAction(Values.ZFY_MEDIA_UPLOADED);
        filter.addAction(Values.ZFY_MEDIA_DELETED);
        filter.addAction(Values.ACTION_VIDEO_FPS);
        filter.addAction(Values.ZZX_FACE_RECOGNITION);
        filter.addAction(Values.ACTION_RECORD_DEFAULT_VALUE);
        filter.addAction(Values.ACTION_Distance_induction);//距离感应广播
        filter.addAction(Values.ACTION_PR);//P80自动红外
        registerReceiver(mCameraReceiver, filter);
        mWifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(Values.ACTION_RELEASE_CAMERA);
        intentFilter.addAction(Values.ACTION_RESTART_CAMERA);
        intentFilter.addAction(Values.ACTION_UVC_CONNECT);
        intentFilter.addAction(Values.ACTION_UVC_DISCONNECT);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(Values.ACTION_VIDEO_CALL_END);
        if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        }
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, intentFilter);
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        Values.LOG_W(TAG, "nmea = " + nmea);
        if (nmea != null && mIsRecording) {
            if (nmea.contains("GPRMC")) {
                mNmea = nmea;
            } else if (nmea.contains("GNRMC")) {
                mNmea = nmea.replace("GNRMC", "GPRMC");
            }
            //mNmea = testGPS(nmea);
            //Log.d(TAG, "onNmeaReceived: nmea:"+mNmea);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        // System.out.println("CustomCameraView onAutoFocus success = " + success);
        if (success) {
            mode = MODE.FOCUSED;
            view_focus.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_focus_focused));
        } else {
            mode = MODE.FOCUSFAIL;
            view_focus.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_focus_failed));
        }

        setFocusView();
    }

    private void setFocusView() {
        new Handler().postDelayed(new Runnable() {

            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                view_focus.setBackgroundDrawable(null);
                if (mIsTouchFocus) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsTouchFocus = false;
                        }
                    }, AUTO_WAIT_TIME);
                }
                if (mSensorController.isFocusLocked()) {
                    mSensorController.unlockFocus();
                }

            }
        }, 1 * 1000);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == getKey(R.string.key_record_pre)) {
            showswitchlayout(sharedPreferences.getString(getKey(R.string.key_record_pre), "0"));
        }
    }

    private void setQuickSerialMode(int pictures) {
        mContinuousCaptureTotalCount = pictures;
        mParameters.set("cap-mode", "continuousshot");
        mParameters.set("burst-num", pictures);
        mCamera.setParameters(mParameters);
    }

    private void clearQuickSerialMode() {
        mParameters.set("cap-mode", "normal");
        mParameters.set("burst-num", 1);
        mCamera.setParameters(mParameters);
    }

    private void recordSection() {
        mPortRecordMode = true;
        mPreRecordMode = false;
        mManualStopRecord = false;
        stopRecord();
        startRecord();
        mPortRecordMode = false;
    }

    private int mLastCutTime = 0;

    void onEventMainThread(Integer event) {
        Values.LOG_I(TAG, "onEventMainThread().event = " + event);
        switch (event) {
            case Values.BACK_START_RECORD:
                Values.LOG_I(TAG, "onEventBackgroundThread().event = BACK_START_RECORD");
                if (isExternalStorageEnabled())
                    initRecorderAndRecord();
                break;
            case Values.BACK_STOP_RECORD:

                //guoqi
                Values.LOG_I(TAG, "onEventBackgroundThread().event = BACK_STOP_RECORD");
                stopRecordAndRestoreDatabase();
                if (ZZXConfig.mVideoCut) {
                    mCutTime = mRecordTime - mLastCutTime;
                    mLastCutTime = mRecordTime;
                    if (mCutTime > MainActivity.CUT_VIDEO_RECORD_TIME) {
                        Settings.System.putInt(getContentResolver(), Values.VIDEO_CUT, -1);
                        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.CUT_VIDEO);
                    }
                }
                mRecordTime = 0;
                break;
            case Values.MSG_BIND_PREFERENCES:
                bindPreferences();
                popRecordSetting();
                break;
            case Values.VIDEO_REGISTER_SUCCESS:/**开始上传**/
                synchronized (mLockObject) {
                    if (mNeedReportVideo) {
                        return;
                    } else {
                        mNeedReportVideo = true;
                    }
                }
                //收到后台实时视频注册成功消息后,若正在录像则停止.若人为操作则设置mPreRecording为true,否则false.
                Values.LOG_W(TAG, "VIDEO_REGISTER_SUCCESS");
                if (mIsRecording) {
                    mPreRecording = mManualRecording;
                    mManualStopRecord = false;
                    mUserRecording = false;
                    stopRecord();
                    mIsStopRecording = true;
                }
                checkCameraNeedRestart();
                initRealTimeVideo();
                mBtnCaptureRecord.setClickable(true);

                if (mReportWithRecord  && mIsStopRecording) {
                    startRecordAsUser();
                    mIsStopRecording = false;
                }
                Values.LOG_I(TAG, "onEventMainThread().mNeedReportVideo = true");
                break;
            case Values.VIDEO_REPORT_STOP:/**停止录像**/
                Values.LOG_W(TAG, "VIDEO_REPORT_STOP");
                synchronized (mLockObject) {
                    if (!mNeedReportVideo) {
                        return;
                    }
                }
                //实时视频结束后,若前一个为人为录像,则重新启动,否则检测录像模式是否预录.
                if (mReportWithRecord && mIsRecording) {
                    mPreRecording = mManualRecording;//通过将表示人为的录像的变量值赋值给预录值，为 true 则重新启动
                    mManualStopRecord = false;
                    mUserRecording = false;
                    stopRecord();
                }
                releaseRealTimeVideo(true);
                if (mPreRecording) {
                    mPreRecording = false;
                    mManualStopRecord = false;
                    mUserRecording = true;
                    startRecord();
                } else {
                    initRecordFromRecordMode();
                    enableCaptureOrRecordSetting();
                    enableZoomButton();
                }
//                if (!isScreen){
//                    mHandler.sendEmptyMessageDelayed(Values.RELEASE_CAMERA,15*1000);
//                }
                Values.LOG_I(TAG, "onEventMainThread().mNeedReportVideo = false");
                break;

            case Values.VIDEO_STOP_RECORD:/**停止录像**/
                if (mIsRecording) {
                    stopRecord();
                }
                break;
            case Values.VIDEO_THUMBNAIL:
                if (!SystemUtil.isFileExists(mVideoFilePath)) {
                    return;
                }
                if (mIsWindowMax) {
                    Bitmap videoThumb = ThumbnailUtil.getVideoThumbnail(mVideoFilePath, mBtnThumb.getWidth() - 2, mBtnThumb.getHeight() - 2);
                    if (mImgThumbnail != null && mImgThumbnail.getBitmap()!=null) {
                        try {
                            mImgThumbnail.getBitmap().recycle();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        mImgThumbnail = null;
                    }
                    mImgThumbnail = new BitmapDrawable(mRes, videoThumb);
                    if (videoThumb != null) {
                        mBtnThumb.setImageDrawable(mImgThumbnail);
                    }
                }
                if (!mNeedReportVideo && (mManualStopRecord || mPreRecordMode)) {
                    initRecordFromRecordMode();
                }
                break;
            case Values.IMG_THUMBNAIL:
                Log.i(TAG, "IMG_THUMBNAIL: *********************************");
                if (!SystemUtil.isFileExists(mPicPath)) {
                    return;
                }
                getBitmap(mPicPath);
                picPath = mPicPath.replace(Values.FILE_SUFFIX_PIC,"_"+FileUtils.filesize(mPicPath)+Values.FILE_SUFFIX_PIC);
                File file = new File(mPicPath);
                file.renameTo(new File(picPath));
                if (Settings.System.getInt(getContentResolver(),"ZZXEncryption",0)==1){
                    mEncryptionUtil.addUnencry(picPath,Values.FILE_SUFFIX_PIC);
                }
                if (SystemUtil.isNetworkConnected(CameraService.this) && Settings.System.getInt(getContentResolver(),"ZZXAutomatic",0)==1 && picPath != null) {
                    if (mUploadFileUtil.isSetFTP()){
                        mUploadFileUtil.uploadVideoFile(FTPUploadInfo.getFTPUploadInfo(CameraService.this, picPath));
                    }
                }
                sendBroadcastSP(Values.SPXZ_TAKE_PICTURE,picPath);
//                if (one_up){
//
//                }else {
//                    Log.i(TAG, "IMG_THUMBNAIL: "+SystemUtil.isFileExists(picPath));
//                    if (!SystemUtil.isFileExists(picPath)) {
//                        return;
//                    }
//                    getBitmap(picPath);
//
//                }

                /**显示图片缩略图
//                 * */
//                Intent intent = new Intent(Values.ACTION_CAPTURE);
//                sendBroadcast(intent);
//                if (mIsWindowMax) {
//                    Bitmap imgThumb = ThumbnailUtil.getImageThumbnail(mPicPath, mBtnThumb.getWidth() - 2, mBtnThumb.getHeight() - 2);
//                    if (mImgThumbnail != null && mImgThumbnail.getBitmap()!=null) {
//                        try {
//                            mImgThumbnail.getBitmap().recycle();
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                        mImgThumbnail = null;
//                    }
//                    mImgThumbnail = new BitmapDrawable(mRes, imgThumb);
//                    if (imgThumb != null) {
//                        mBtnThumb.setImageDrawable(mImgThumbnail);
//                    }
//                }
//                if (!mIntervalOrTimerMode && !mIsRecording) {
//                    enableRecordAndCaptureButton();
//                }
//                if (mForestRanger) {
//                    showReportDialog();
//                }

                if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY)
                   Settings.System.putString(getContentResolver(),"LolaLive_Pic","");
                break;
            case Values.RESTART_PREVIEW:
                /**若非录像时高速连拍或者单拍时会在拍照完成前禁止拍照键按键,完成后才恢复.且在后台实时视频时还需要重新{@link android.hardware.Camera#setPreviewCallback(android.hardware.Camera.PreviewCallback)}
                 * 而若是定时或者间隔连拍时则不需要禁止,而是进入到特殊可取消模式.
                 * 录像时此处无需任何操作,目前在拍照后若不重新释放Camera则会造成边录边拍失败.需要在拍完照后释放,而间隔连拍时是在取消间隔连拍时释放.
                 * @see #startIntervalOrTimerCaptureMode()
                 * @see #clearIntervalOrTimerCaptureMode()
                 * */
                if (!mIsRecording) {
                    mParameters.set(MTK_CAM_MODE, 0);
                    mCamera.setParameters(mParameters);
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    if (mNeedReportVideo || ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY) {
                        mCamera.setPreviewCallback(this);
                    }
                    mCamera.startPreview();
                }
                break;
            case Values.ENABLE_CAPTURE_BUTTON:
                break;
            case Values.ENABLE_SOME_VIEW:
                clearIntervalOrTimerCaptureMode();
                enableCaptureOrRecordSetting();
                mContinuousCaptureMode = false;
                break;
            case Values.RECORD:
                if (mIsCapture)
                    toggleToRecordMode();
                if (!mManualRecording) {
                    mManualStopRecord = false;
                    mImportantFileSign = false;
                    checkCameraNeedRestart();
                    startRecordAsUser();
                } else {
                    if (mVideoLongPress && mVideoLongPressNum == 0) {
                        mImportantFileSign = true;
                        mIvImportantFile.setVisibility(View.VISIBLE);
                        mVideoLongPressNum++;
                        return;
                    }
                    stopRecordAsUserButton();
                }
                break;
            case Values.RECEIVE_STOP_RECORD:
                /** 收到停止录像指令无需保存录像结果. **/
                mHttpStartRecord = false;
                if (mIsRecording) {
                    recordSection();
                }
                break;
            case Values.RECEIVE_HTTP_UPLOAD:
                if (mIsRecording) {
                    recordSection();
                }
                break;
            case Values.MAIN_START_RECORD:
                mManualStopRecord = false;
                startRecord();
                break;
            case Values.STORAGE_NOT_ENOUGH:
                mStorageNotEnough = true;
                if (mIsRecording) {
                    stopRecordAsUserButton();
                }
                if (isSoundReecord && isExternalStorageEnabled()){
                    startRecordService(false);
                }
                mHandler.sendEmptyMessage(Values.WRITER_CLOSE);
                clearIntervalOrTimerCaptureMode();
                disableRecordAndCaptureButton();
                disableCaptureOrRecordSetting();
                if (ZZXConfig.isP5XD_SXJJ){
                    mTvStorageNotEnough.setText(R.string.storages_not_enough);
                }else {
                    mTvStorageNotEnough.setText(R.string.storage_not_enough);
                }
                mTvStorageNotEnough.setVisibility(View.VISIBLE);
                if (mPowerManager == null) {
                    mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
                }
                if (!mPowerManager.isScreenOn())
                    checkCameraNeedIDLE();

                if (isShowStorageNotEnoughDailog) {
                    if (MainActivity.isStorageNotEnought) {
                        //  mDidiSoundID = mBurstSound.load(this, R.raw.didi, 1);
//                        mBurstSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//                            @Override
//                            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                                mDidiStreamID = soundPool.play(sampleId, 1.0f, 1.0f, 0, -1, 1.0f);
//                            }
//                        });
                        mDidiStreamID = mBurstSound.play(mDidiSoundID, 1.0f, 1.0f, 0, -1, 1.0f);
                        showNotEnoughNewDialog();
                        if (ZZXConfig.isP5XD_SXJJ){
                            storage_state.setText(R.string.storages_not_enough);

                        }else {
                            storage_state.setText(R.string.storage_not_enough);
                        }
                        isShowStorageNotEnoughDailog = false;
                    }
                }
                break;
            case Values.STORAGE_OK:
                if (mStorageNotEnough) {
                    enableCaptureOrRecordSetting();
                    enableRecordAndCaptureButton();
                    mTvStorageNotEnough.setVisibility(View.GONE);
                    mStorageNotEnough = false;
                }
                isShowStorageNotEnoughDailog = true;
                break;
            case Values.VIDEO_CUT_MAIN:
                stopRecordAndRestoreDatabase();
                //       EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.CUT_VIDEO);
                break;
            case Values.RECEIVE_START_RECORD:
                /*if (mIsRecording) {
                    stopRecord();
                }
                startRecordAsUser();*/
                checkCameraNeedRestart();
                recordSection();
                mHttpStartRecord = true;
                break;
            case Values.SHOW_DIALOG_CHOOSE_POWER_SAVE_MODE:
                showPowerSaveDialog();
                break;
        }
    }

    private void getBitmap(String picPath){
        Intent intent = new Intent(Values.ACTION_CAPTURE);
        sendBroadcast(intent);
        if (mIsWindowMax) {
            Bitmap imgThumb = ThumbnailUtil.getImageThumbnail(picPath, mBtnThumb.getWidth() - 2, mBtnThumb.getHeight() - 2);
            if (mImgThumbnail != null && mImgThumbnail.getBitmap()!=null) {
                mBtnThumb.setImageDrawable(null);
                try {
                    mImgThumbnail.getBitmap().recycle();
                }catch (Exception e){
                    e.printStackTrace();
                }
                mImgThumbnail = null;
            }
            mImgThumbnail = new BitmapDrawable(mRes, imgThumb);
            if (imgThumb != null) {
                mBtnThumb.setImageDrawable(mImgThumbnail);
            }
        }
        if (!mIntervalOrTimerMode && !mIsRecording) {
            enableRecordAndCaptureButton();
        }
        if (mForestRanger) {
            showReportDialog();
        }
    }
    EventBusUtils.EventMessage eventMessage;
    private Vibrator vib;

    void onEventMainThread(Object what) {
        Log.i("123", "onEventMainThread,what:" + what.toString());
        if (what instanceof EventBusUtils.EventMessage) {
            eventMessage = ((EventBusUtils.EventMessage) what);
            switch (eventMessage.getEventId()) {
                case Values.TALK_SHOW_DIALOG:
//                    AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                    int current = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
//                    Settings.System.putInt(getContentResolver(),,current);
//                    mDidiSoundID = mBurstSound.load(this, R.raw.cnwav, 1);
                    vib = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
                    vib.vibrate(new long[]{2000, 5000, 2000, 5000}, 0);
                    mDidiStreamID = mBurstSound.play(mTalkSoundID, 1.0f, 1.0f, 0, -1, 1.0f);
//                    mBurstSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//                        @Override
//                        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                            mDidiStreamID = soundPool.play(sampleId, 1.0f, 1.0f, 0, -1, 1.0f);
//                        }
//                    });
                    showTalkDialog();
                    mHandler.sendEmptyMessageDelayed(Values.TALK_DIALOG, 60 * 1000);
                    break;
                case Values.TALK_DISMISS_DIALOG:
                    mHandler.removeMessages(Values.TALK_DIALOG);
                    vib.cancel();
                    mBurstSound.stop(mDidiStreamID);
                    dismissTalkDialog();
                    break;
            }
        }
    }
    public void showsmallUpLoad() {
        initSmallWindow();
        if (!mIsAddedSmallWindow) {
            mIsAddedSmallWindow = true;
            mWindowSmall.setParams(mParamsSmall);
            mWinManager.addView(mWindowSmall, mParamsSmall);
        }
    }

    public void addSmallWindow() {
        dismissUpLoadDialog();
        showsmallUpLoad();
    }
    private boolean mUpLoadLoadShowed = false;
    public void addBigWindow() {
        dismisssmallUpLoad();
        showUpLoadDialog();
        mUpLoadLoadShowed = true;
    }

    private void dismisssmallUpLoad() {
        if (mIsAddedSmallWindow) {
            mIsAddedSmallWindow = false;
            mWinManager.removeViewImmediate(mWindowSmall);
        }
    }

    private View mUpLoadContainer;
    public void showUpLoadDialog() {
        initupLoadLayout();
        if (!mUpLoadLoadShowed) {
            mUpLoadLoadShowed = true;
            mWinManager.addView(mUpLoadContainer, mReportParams);
        }
    }
    private void dismissUpLoadDialog() {
        if (mUpLoadLoadShowed) {
            mUpLoadLoadShowed = false;
            mWinManager.removeViewImmediate(mUpLoadContainer);

        }
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
                mParamsSmall.type = LayoutParams.TYPE_SYSTEM_ALERT;
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

    private void showTalkDialog() {
        initTaLKLayout();
        if (!mTalkShowed) {
            mTalkShowed = true;
            mWinManager.addView(mTalkContainer, mReportParams);
        }
        Log.i("123", "showTalkDialog");
    }
    private void dismissTalkDialog() {
        if (mTalkShowed) {
            mTalkShowed = false;
            mWinManager.removeViewImmediate(mTalkContainer);

        }
    }
    /**
     * 进入省点模式的对话框
     */
    private void showPowerSaveDialog() {//进入省点模式的对话框
        initPowerSaveLayout();
        mWinManager.addView(mPowerSaveContainer, mReportParams);
    }

    /**
     * 展示内存不足的通知
     */
    private void showNotEnoughNewDialog() {//内存不足提示框
        initNotEnoughLayout();
        mWinManager.addView(mNotEnoughContainer, mReportParams);
    }

    private void dismissPowerSaveDialog() {
        mWinManager.removeViewImmediate(mPowerSaveContainer);
    }
    TextView filename, schedue;
    ProgressBar progressBar;
    View bigLayout;

    private void initupLoadLayout() {
        if (mUpLoadContainer == null) {
            mUpLoadContainer = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_progress, null, false);
            filename = (TextView) mUpLoadContainer.findViewById(R.id.filename);
            schedue = (TextView) mUpLoadContainer.findViewById(R.id.schedue);
            bigLayout = mUpLoadContainer.findViewById(R.id.big_upload);
            bigLayout.setOnClickListener(this);
            mUpLoadContainer.findViewById(R.id.btn_min).setOnClickListener(this);
            progressBar = (ProgressBar) mUpLoadContainer.findViewById(R.id.progressBar1);
            mReportParams = new LayoutParams();
            mReportParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
            mReportParams.height = LayoutParams.WRAP_CONTENT;
            mReportParams.width = LayoutParams.WRAP_CONTENT;
            mReportParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mReportParams.alpha = 1;
            mReportParams.format = PixelFormat.RGBA_8888;
            mReportParams.gravity = Gravity.CENTER;
            mReportParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }
    private View mTalkContainer;
    private void initTaLKLayout() {
        if (mTalkContainer == null) {
            mTalkContainer = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.talk, null, false);
            mTalkContainer.findViewById(R.id.btn_talk_ok).setOnClickListener(this);
            mTalkContainer.findViewById(R.id.btn_talk_ignore).setOnClickListener(this);
            mReportParams = new LayoutParams();
            mReportParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
            mReportParams.height = LayoutParams.WRAP_CONTENT;
            mReportParams.width = LayoutParams.WRAP_CONTENT;
            mReportParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mReportParams.alpha = 1;
            mReportParams.format = PixelFormat.RGBA_8888;
            mReportParams.gravity = Gravity.CENTER;
            mReportParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }
    private void initPowerSaveLayout() {
        if (mPowerSaveContainer == null) {
            mPowerSaveContainer = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.choose_power_save, null, false);
            mPowerSaveContainer.findViewById(R.id.btn_report_ok).setOnClickListener(this);
            mPowerSaveContainer.findViewById(R.id.btn_report_ignore).setOnClickListener(this);
            mReportParams = new LayoutParams();
            mReportParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
            mReportParams.height = LayoutParams.WRAP_CONTENT;
            mReportParams.width = LayoutParams.WRAP_CONTENT;
            mReportParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mReportParams.alpha = 1;
            mReportParams.format = PixelFormat.RGBA_8888;
            mReportParams.gravity = Gravity.CENTER;
            mReportParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    private void dismissNotEnoughDialog() {
        mWinManager.removeViewImmediate(mNotEnoughContainer);
    }
    private TextView storage_state;
    private void initNotEnoughLayout() {
        if (mNotEnoughContainer == null) {
            mNotEnoughContainer = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.choose_not_enought, null, false);
            mNotEnoughContainer.findViewById(R.id.btn_not_enought_ok).setOnClickListener(this);
            mNotEnoughContainer.findViewById(R.id.btn_not_enought_ignore).setOnClickListener(this);
            storage_state = mNotEnoughContainer.findViewById(R.id.storage_state);
            mReportParams = new LayoutParams();
            mReportParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
            mReportParams.height = LayoutParams.WRAP_CONTENT;
            mReportParams.width = LayoutParams.WRAP_CONTENT;
            mReportParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mReportParams.alpha = 1;
            mReportParams.format = PixelFormat.RGBA_8888;
            mReportParams.gravity = Gravity.CENTER;
            mReportParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    /**
     * 护林员拍完照显示是否上传对话框
     */
    private void showReportDialog() {
        initReportLayout();
        if (!mReportShowed) {
            mReportShowed = true;
            mWinManager.addView(mReportContainer, mReportParams);
        }
    }

    private void dismissReportDialog() {
        if (mReportShowed) {
            mReportShowed = false;
            mWinManager.removeViewImmediate(mReportContainer);
        }
    }

    private void initReportLayout() {
        if (mReportContainer == null) {
            mReportContainer = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.report, null, false);
            RadioGroup group = (RadioGroup) mReportContainer.findViewById(R.id.group_report);
            group.setOnCheckedChangeListener(new ReportCheckChanged());
            mReportContainer.findViewById(R.id.btn_report_confirm).setOnClickListener(this);
            mReportContainer.findViewById(R.id.btn_report_cancel).setOnClickListener(this);
            mReportParams = new LayoutParams();
            mReportParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
            mReportParams.height = LayoutParams.WRAP_CONTENT;
            mReportParams.width = LayoutParams.WRAP_CONTENT;
            mReportParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mReportParams.alpha = 1;
            mReportParams.format = PixelFormat.RGBA_8888;
            mReportParams.gravity = Gravity.CENTER;
            mReportParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    /**
     * 初始化中心实时视频请求.设置预览大小为小分辨率,否则传输分辨率过大.
     * 此时需要调用{@link android.hardware.Camera#setPreviewCallback(android.hardware.Camera.PreviewCallback)}
     *
     * @see #onPreviewFrame(byte[], Camera)
     * @see #releaseRealTimeVideo(boolean)
     */
    private void initRealTimeVideo() {
//        acquireRealWakeLock();
        disableCaptureOrRecordSetting();
//        disableZoomButton();
        mCamera.stopPreview();
        /***设置分辨率为320*240**/
        int reportQuality = Settings.System.getInt(getContentResolver(), REPORT_QUALITY, 0);
        boolean qualityLow = reportQuality == 0;
        mReportWidth = mRes.getInteger(qualityLow ? R.integer.preview_width_low : R.integer.preview_width_high);
        mReportHeight = mRes.getInteger(qualityLow ? R.integer.preview_height_low : R.integer.preview_height_high);
        mParameters.setPreviewSize(mReportWidth, mReportHeight);
        mParameters.setPreviewFormat(ImageFormat.YV12);
        mCamera.setParameters(mParameters);
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
        if (MainActivity.mIsUseSDK) {
            initReportNew();
        } else {
            initReport();
        }
        mNeedReportVideo = true;
    }

    /**
     * 需修改.
     */
    private void initReport() {
        OutputStream outputStream = MainActivity.mProtocolUtils.getOutStreamVideo();
        if (outputStream == null) {
            releaseReport();
            return;
        }
        if (mDeque == null) {
            /**存放数据的队列**/
            mDeque = new LinkedBlockingDeque<>(REPORT_DEQUE_SIZE);
        }
        if (mVideoEncoder == null) {
            /**视频解码器，视频解码的配置**/
            mVideoEncoder = new VideoEncoder(this, outputStream, mReportWidth, mReportHeight);
            /**视频的解码的异步任务**/
            mRealTimeTask = new RealTimeTask();
            mRealTimeTask.executeOnExecutor(mExecutor);
        }
    }

    /**
     * 需修改.
     */
    private void initReportNew() {
        if (MainActivity.mZzxClient.getOutStreamVideo() == null) {
            releaseReport();
            return;
        }
        if (mDeque == null) {
            /**存放数据的队列**/
            mDeque = new LinkedBlockingDeque<>(REPORT_DEQUE_SIZE);
        }
        if (mVideoEncoder == null) {
            /**视频解码器，视频解码的配置**/
            mVideoEncoder = new VideoEncoder(this, MainActivity.mZzxClient.getOutStreamVideo(), mReportWidth, mReportHeight);
            /**视频的解码的异步任务**/
            mRealTimeTask = new RealTimeTask();
            mRealTimeTask.executeOnExecutor(mExecutor);
        }
    }

    /**
     * 需修改.
     */
    private void releaseReport() {
        releaseRealTask();
        releaseVideoEncoder();
        releaseDeque();
    }

    /**
     * 释放解码的异步任务
     **/
    private void releaseRealTask() {
        if (mRealTimeTask != null) {
            mRealTimeTask.cancel(true);
            mRealTimeTask = null;
        }
    }

    /**
     * @see #initRealTimeVideo()
     * @param isNeedStartPreview 是否需要重新打开预览
     */
    private void releaseRealTimeVideo(boolean isNeedStartPreview) {
        if (mNeedReportVideo) {
            mNeedReportVideo = false;
            releaseRealTask();
            releaseVideoEncoder();
            releaseDeque();
//            enableCaptureOrRecordSetting();
//            enableZoomButton();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            if (isNeedStartPreview) {
                if (ZZXConfig.isP5XJNKYD){
                    mParameters.setPreviewSize(640, 480);
                }else {
                    mParameters.setPreviewSize(1280, 720);
                }
                if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
                    mParameters.setPreviewFormat(ImageFormat.NV21);
                }
                mCamera.setParameters(mParameters);
                mCamera.startPreview();
            }
//            releaseRealWakeLock();
        }
    }

    private void releaseDeque() {
        if (mDeque != null) {
            mDeque.clear();
            mDeque = null;
        }
    }

    private void releaseVideoEncoder() {
        if (mVideoEncoder != null) {
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
    }

    void onEventBackgroundThread(Integer event) {
        switch (event) {
            case Values.STORE_DATA_BASE:
                conserveDatabase();
                break;
            case Values.BACK_STOP_PRE_MODE_RECORD:
                /**预录模式下录完后立即删除当前的录像.
                 * */
//                stopRecordAsMedia();
                deletePreModeVideo();
                /*if (mPreRecordCount >= 2) {
                    deletePreModeVideo();
                }*/
                if (!mNeedReportVideo && (mManualStopRecord || mPreRecordMode)) {
                    initRecordFromRecordMode();
                }
                break;
            case Values.BACK_DELETE_PRE_VIDEO_FILE:
                Values.LOG_I(TAG, "onEventBackgroundThread().event = BACK_DELETE_PRE_VIDEO_FILE");
                deletePreModeVideo();
                break;
            case Values.TAKE_PICTURE:
                if (IsPicing || isKeyPic || isContinuousPic){
                    return;
                }
                IsPicing = true;
                isKeyPic = true;
                isContinuousPic = true;
                Values.LOG_I(TAG, "onEventBackgroundThread().event = TAKE_PICTURE");
                if (mCameraSound == null)
                    mCameraSound = new MediaActionSound();
                if (mShutterSoundEnabled && !mContinuousCaptureMode)
                    if (!ZZXConfig.isP5XJNKYD ){
                        if (ZZXConfig.is4GC || ZZXConfig.is4GA || ZZXConfig.is4GA_SDTY){
                            mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
                        }else {
                            mBurstSound.play(mPicSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
                        }
                    }
                try {
                      mCamera.takePicture(null, null, null, getJpegPictureCallback());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Values.CUT_VIDEO:
                Settings.System.putInt(getContentResolver(), Values.VIDEO_CUT, -1);
                // File file = new File(mVideoFilePath);
                Values.LOG_E(TAG, "CUT_VIDEO");
                    String replaceFile = videoCutFilePath;
                    if (getVideoformat(this)){
                        replaceFile = replaceFile.replace(Values.FILE_SUFFIX_VID_AVI, Values.FILE_SUFFIX_IMPORTANT + Values.FILE_SUFFIX_VID);
                    }else {
                        replaceFile = replaceFile.replace(Values.FILE_SUFFIX_VID, Values.FILE_SUFFIX_IMPORTANT + Values.FILE_SUFFIX_VID);
                    }
                    File file = new File(videoCutFilePath);
                    File file1 = new File(replaceFile);
                    file.renameTo(file1);
                    VideoCut videoCut = new VideoCut(this);
                    Log.e("zzx", "CUT_VIDEO" + "   mCutTime" + mCutTime);
//                    LogUtil.e("zzx", "CUT_VIDEO" + "   mCutTime" + mCutTime);
                    videoCut.cut(mDate, MainActivity.CUT_VIDEO_DURATION_IN_MIN, mCutTime, file1.getName());
//                }
                break;
        }
    }

    void onEvent(Integer event) {
        switch (event) {
            case Values.AUDIO_REPORT_REGISTER_SUCCESS:
                startAudioReport();
                break;
            case Values.AUDIO_REPORT_STOP:
                if (mAudioTask != null) {
                    mAudioTask.cancel(true);
                    mAudioTask = null;
                }
                if (mAudioEncoder != null) {
                    mAudioEncoder.release();
                    mAudioEncoder = null;
                }
                MainActivity.mProtocolUtils.releaseAudioReportSocket();
                break;
        }
    }

    private void startAudioReport() {
        if (mAudioEncoder == null) {
            mAudioEncoder = new AudioEncoder2(8000, MainActivity.mProtocolUtils.getOutStreamAudio());
            mAudioEncoder.startReadAudioData();
        }
        if (mAudioTask == null) {
            mAudioTask = new AudioReportTask();
            mAudioTask.executeOnExecutor(mExecutor);
        }
    }

    /**
     * 删除预录模式下的上一个预录视频.
     */
    private void deletePreModeVideo() {
        if (mPreVideoFilePath != null && !mPreVideoFilePath.equals("")) {
            File file = new File(mPreVideoFilePath);
            if (file.exists()) {
                if (file.delete()) {
                    Values.LOG_I(TAG, "delete file " + mPreVideoFilePath + " success");
                } else {
                    Values.LOG_I(TAG, "delete file " + mPreVideoFilePath + " failed");
                }
            }
            file = new File(mGpsFileFullPath);
            if (file.exists()) {
                if (file.delete()) {
                    Values.LOG_I(TAG, "delete file " + mGpsFileFullPath + " success");
                } else {
                    Values.LOG_I(TAG, "delete file " + mGpsFileFullPath + " failed");
                }
            }
        }
    }

    private void initPopupWindow() {
        if (mPopWindow == null) {
            mPopWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopWindow.setFocusable(true);
            mPopWindow.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        }
        if (mSecondPopWindow == null) {
            mSecondPopWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mSecondPopWindow.setFocusable(true);
            mSecondPopWindow.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        }
    }

    private void popRecordSetting() {
        mModeListView.setBackgroundResource(R.drawable.bg_left);
        showPopupWindow(mPopWindow, mModeListView, mBtnMode, 5, 5);
        //showPopupWindow(mPopWindow, mModeListView, mBtnMode, 60, -65);
    }

    /**
     * 显示拍照模式一级菜单
     */
    private void popCaptureSetting() {
        initPicPopView();
        if (mPicModeRootClickListener == null)
            mPicModeRootClickListener = new PicModeListClickListener();
        mPicListView.setOnItemClickListener(mPicModeRootClickListener);
        mPicListView.setAdapter(mPicModeRootAdapter);
        mPicListView.setBackgroundResource(R.drawable.bg_left);
        showPopupWindow(mPopWindow, mPicListView, mBtnMode, 5, 5);
    }

    /**
     * 显示拍照分辨率设置窗
     */
    private void popCaptureRatioSetting() {
        initPicPopView();
        if (mCaptureRatioItemClickListener == null)
            mCaptureRatioItemClickListener = new PicResolutionItemListener(true);
        mPicListView.setOnItemClickListener(mCaptureRatioItemClickListener);
        mPicResolutionAdapter.setCheck(findIndex(), true);
        mPicListView.setAdapter(mPicResolutionAdapter);
        mPicListView.setBackgroundResource(R.drawable.bg_middle);
        showPopupWindow(mSecondPopWindow, mPicListView, mBtnResolution, -40, 0);
//        showPopupWindow(mSecondPopWindow, mPicListView, mBtnResolution, 60, -90);
    }

    /**
     * 显示录像分辨率设置窗
     */
    private void popRecordRatioSetting() {
        initPicPopView();
        if (mRecordRatioItemClickListener == null)
            mRecordRatioItemClickListener = new PicResolutionItemListener(false);
        mPicListView.setOnItemClickListener(mRecordRatioItemClickListener);
        mRecordResolutionAdapter.setCheck(findIndex(), true);
        mPicListView.setAdapter(mRecordResolutionAdapter);
        mPicListView.setBackgroundResource(R.drawable.bg_middle);
        showPopupWindow(mSecondPopWindow, mPicListView, mBtnResolution, -40, 0);
    }

    /**
     * 初始化拍照模式一级菜单弹出窗
     */
    private void initPicPopView() {
        if (mPicListView == null) {
            mPicListView = (ListView) ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_popup_window_container, null);
            mPicModeRootAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.array_mode_txt), 0, R.array.array_mode_default_icon, true, true);
            if (ZZXConfig.is4GA_TEST || ZZXConfig.is5G_liangjian){
                mPicResolutionAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.test_array_ratio_capture_txt), R.array.test_array_ratio_capture_txt, 0, true, false);
            }else {
                mPicResolutionAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.array_ratio_capture_txt), R.array.array_ratio_capture_txt, 0, true, false);
            }

            if (!checkQRWifi(this)) {
                if (ZZXConfig.isP5XJNKYD){
                    mRecordResolutionAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.jnkyd_array_custom_ratio_record), R.array.jnkyd_array_custom_ratio_record, 0, false, false);
                }else if (ZZXConfig.isP5C_RZXFD){
                    mRecordResolutionAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.rzxfzd_array_ratio_record), R.array.rzxfzd_array_ratio_record, 0, false, false);
                }else if (ZZXConfig.is4GA_TEST){
                    mRecordResolutionAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.test_array_ratio_record), R.array.test_array_ratio_record, 0, false, false);
                }else if (ZZXConfig.is4GA_HNYC){
                    mRecordResolutionAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.array_ratio_record_hnyc), R.array.array_ratio_record_hnyc, 0, false, false);
                }else {
                    mRecordResolutionAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.array_ratio_record), R.array.array_ratio_record, 0, false, false);
                }
            } else  {
                mRecordResolutionAdapter = new CaptureRecordPopListAdapter(this, mRes.getTextArray(R.array.array_ratio_record_wifi), R.array.array_ratio_record_wifi, 0, false, false);
            }
            mRecordResolutionAdapter.setCheck(0, true);
            mPicModeRootAdapter.setCheck(0, true);
            mPicResolutionAdapter.setCheck(0, true);
        }
    }

    private int findPicModeChildItemIndex(String value, int entryValuesId) {
        CharSequence[] entryValues = mRes.getStringArray(entryValuesId);
        for (int i = 0; i < entryValues.length; i++) {
            if (value.equals(entryValues[i].toString())) {
                return i;
            }
        }
        return 0;
    }
    TypedArray ratioIconList;
    private void refreshRatioBtn() {

        if (mIsCapture) {
            if (ZZXConfig.is4GA_TEST || ZZXConfig.is5G_liangjian){
                ratioIconList = mRes.obtainTypedArray(R.array.test_array_ratio_capture_icon);
            }else {
                ratioIconList = mRes.obtainTypedArray(R.array.array_ratio_capture_icon);
            }

        } else {
            if (!checkQRWifi(this)) {
                if(ZZXConfig.isP5XJNKYD){
                    ratioIconList = mRes.obtainTypedArray(R.array.jnkyd_array_custom_ratio_record_icon);
                }else if (ZZXConfig.isP5C_RZXFD){
                    ratioIconList = mRes.obtainTypedArray(R.array.rzxfzd_array_ratio_record_icon);
                }else if (ZZXConfig.is4GA_TEST){
                    ratioIconList = mRes.obtainTypedArray(R.array.test_array_ratio_record_icon);
                }else if (ZZXConfig.is4GA_HNYC){
                    ratioIconList = mRes.obtainTypedArray(R.array.array_ratio_record_icon_hnyc);
                }else {
                    ratioIconList = mRes.obtainTypedArray(R.array.array_ratio_record_icon);
                }
            } else  {
                ratioIconList = mRes.obtainTypedArray(R.array.array_ratio_record_icon_wifi);
            }
        }
        mBtnResolution.setBackground(ratioIconList.getDrawable(findIndex()));
        if (mIsCapture) {
            mBtnResolution.setId(R.id.btn_pic_ratio);
        } else {
            mBtnResolution.setId(R.id.btn_record_ratio);
        }
        ratioIconList.recycle();
    }

    private int findIndex() {
        SharedPreferences sp = RemotePreferences.getRemotePreferences(this);
        String ratio;
        CharSequence[] ratioList;
        if (mIsCapture) {
            if (ZZXConfig.is4GA_TEST|| ZZXConfig.is5G_liangjian){
                ratioList = mRes.getTextArray(R.array.test_array_ratio_capture_txt);
                ratio = sp.getString(getKey(R.string.key_ratio_picture), Values.RATIO_CAPTURE_DEFAULT_VALUE);
            }else {
                ratioList = mRes.getTextArray(R.array.array_ratio_capture_txt);
                ratio = sp.getString(getKey(R.string.key_ratio_picture), Values.RATIO_CAPTURE_DEFAULT_VALUE);
            }

        } else {
            if (ZZXConfig.isP5XJNKYD){
                ratioList =  mRes.getTextArray(R.array.jnkyd_array_custom_ratio_record);
                ratio = sp.getString(getKey(R.string.key_ratio_record), Values.JINAN_RECORD_DEFAULT_VALUE);
            }else if (ZZXConfig.isP5C_RZXFD){
                ratioList =  mRes.getTextArray(R.array.rzxfzd_array_ratio_record);
                ratio = sp.getString(getKey(R.string.key_ratio_record), Values.RATIO_RECORD_DEFAULT_VALUE);
            }else if (ZZXConfig.is4GA_TEST){
                ratioList =  mRes.getTextArray(R.array.test_array_ratio_record);
                ratio = sp.getString(getKey(R.string.key_ratio_record), Values.RATIO_RECORD_DEFAULT_VALUE);
            }else if (ZZXConfig.is4GA_HNYC){
                ratioList =  mRes.getTextArray(R.array.array_ratio_record_hnyc);
                ratio = sp.getString(getKey(R.string.key_ratio_record), Values.RATIO_RECORD_HNYC_VALUE);
            }else {
                ratioList = checkQRWifi(this) ? mRes.getTextArray(R.array.array_ratio_record_wifi) : mRes.getTextArray(R.array.array_ratio_record);
                ratio = sp.getString(getKey(R.string.key_ratio_record), Values.RATIO_RECORD_DEFAULT_VALUE);
            }

        }
        for (int i = 0; i < ratioList.length; i++) {
            if (ratio.equals(ratioList[i].toString())) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 给PopupWindow设置View并显示
     */
    private void showPopupWindow(PopupWindow popupWindow, ListView view, View anchor, int xOff, int yOff) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.setContentView(view);
        popupWindow.setWidth(view.getMeasuredWidth() + 10);
        popupWindow.setHeight(view.getMeasuredHeight() * view.getAdapter().getCount());
        popupWindow.showAsDropDown(anchor, xOff, yOff);
    }

    /**
     * 初始化主容器
     */
    private void initLayout() {
        mRootContainer = (MainLayout) ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_main, null);
        mRootContainer.setOnKeyBackPressedListener(new MainLayout.KeyBackPressedListener() {
            @Override
            public void onBackPressed() {
                if (mIsWindowMax) {
                    minWindow();
                }
            }
        });
        findChildView(mRootContainer);
        initViewListener();
    }

    /**
     * 初始化WindowManager并添加录像主容器
     *
     * @see #maxWindow() 最大化窗口即显示
     * @see #minWindow() 最小化窗口即退出
     */
    private void initWindowManager() {
        mIsWindowMax = true;
        mWinManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowParams = new LayoutParams();
        /**设置成ERROR即能覆盖状态栏也能获取点击事件
         * @see android.view.WindowManager.LayoutParams#TYPE_SYSTEM_OVERLAY 能覆盖但是焦点还是在下面的Activity所以获取不到点击事件
         * @see android.view.WindowManager.LayoutParams#TYPE_SYSTEM_ALERT 无法覆盖状态栏
         * @see android.view.WindowManager.LayoutParams#FLAG_NOT_FOCUSABLE 此标志位使该Window没有输入焦点,不会接收按钮及按键事件,而传递给它下面可以获得焦点的Window.若不设置则被覆盖的Activity无法响应返回键等操作.
         * */
        mWindowParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        mWindowParams.height = LayoutParams.MATCH_PARENT;
        mWindowParams.width = LayoutParams.MATCH_PARENT;
        int value;
        try {
            value = Settings.System.getInt(getContentResolver(), Values.ACTION_SCREEN_KEEP_ON, 0);
        } catch (Exception e) {
            value = 0;
        }
        switch (value) {
            case 1:
                mWindowParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | LayoutParams.FLAG_KEEP_SCREEN_ON
                        | LayoutParams.FLAG_FULLSCREEN;
                break;
            case 0:
                mWindowParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | LayoutParams.FLAG_FULLSCREEN;
                break;
        }
        mWindowParams.alpha = 1;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mWinManager.addView(mRootContainer, mWindowParams);
    }

    @Override
    public void onDestroy() {
        mExecutor = null;
        if (mBurstSound != null) {
            mBurstSound.unload(mFastPicSoundID);
            mBurstSound.unload(mPicSoundID);
            mBurstSound.unload(mRecordModeSoundID);
            mBurstSound.unload(mPicModeSoundID);
            mBurstSound.unload(mStartRecordSoundID);
            mBurstSound.unload(mStopRecordSoundID);
            mBurstSound.unload(mPicNorSoundID);
            mBurstSound.unload(mRecordNorSoundID);
            mBurstSound.unload(mDidiSoundID);
            mBurstSound.unload(mDidianSoundID);
            mBurstSound.unload(mTalkSoundID);
            mBurstSound.unload(mRecordStartSoundID);
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        releaseLocationListener();
        releaseRecorder();
        releaseCamera();
        unregisterReceiver(mCameraReceiver);
        unregisterReceiver(mWifiReceiver);
        EventBusUtils.unregisterEvent(Values.BUS_EVENT_CAMERA, this);
        EventBusUtils.unregisterEvent(Values.BUS_EVENT_SOCKET_CONNECT, this);
        if (mProximitySensor!=null){
            mProximitySensor.onDestroy();
            mProximitySensor=null;
        }
        if (mLolaLiveSDKUtil != null) {
            mLolaLiveSDKUtil.stop();
            mLolaLiveSDKUtil = null;
        }
        if (timer!=null){
            timer.cancel();
            timer = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.surface_view:
                mDetector.onTouchEvent(event);
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        //所有的点击事件
        switch (v.getId()) {
            case R.id.btn_flash_light:
                toggleFlashLight();
                break;
            case R.id.btn_pic_shutter:
                //   toggleShutterSound();
                break;
            case R.id.btn_pic_ratio:
                popCaptureRatioSetting();
                break;
            case R.id.btn_record_ratio:
                popRecordRatioSetting();
                break;
            case R.id.btn_pic_mode:
                popCaptureSetting();
                break;
            case R.id.btn_record_setting:
                if (mModeListView == null) {
                    mModeListView = (ListView) ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_popup_window_container, null);
                }
                if (mFirstInitPreference) {
                    if(ZZXConfig.isSongJian){
                        addPreferencesFromResource(R.xml.preference_record_set_cma);
                    }else if (ZZXConfig.is4G5_GJDW){
                        addPreferencesFromResource(R.xml.preference_record_set_gjdw);
                    }else {
                        addPreferencesFromResource(R.xml.preference_record_set);
                    }
                    mFirstInitPreference = false;
                } else {
                    popRecordSetting();
                }
                break;
            case R.id.btn_switch_mode:
                if (!CheckUtils.IsContinuousClick(500)){
                    return;
                }
                if (Integer.parseInt(RemotePreferences.getRemotePreferences(this).getString(getKey(R.string.key_record_pre), "0")) == 0) {  //预览情况下 不能切换到拍照模式
                    toggleMode();
                }
                break;
            case R.id.btn_thumb:
                minWindow();
                try {
                    Intent intent = new Intent();
                    if (!ZZXConfig.IsBrazilVerion) {
                        intent.setClassName(Values.PACKAGE_NAME_FILE_MANAGER_NEW, Values.CLASS_NAME_FILE_MANAGER_NEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (mIsCapture) {
                            intent.putExtra("index", 1);//跳转到图片
                        } else {
                            intent.putExtra("index", 0);//跳转到视频
                        }
                    } else {
                        intent.setClass(getApplicationContext(), PasswordBaseActivity.class);
                        intent.putExtra(PasswordBaseActivity.PACKAGE_NAME_FINISH_ACTIVITY, Values.PACKAGE_NAME_FILE_MANAGER_NEW);
                        intent.putExtra(PasswordBaseActivity.CLASS_NAME_FINISH_ACTIVITY, Values.CLASS_NAME_FILE_MANAGER_NEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(PasswordBaseActivity.EXTRA_KEY_FINISH_ACTIVITY, "index");
                        if (mIsCapture) {
                            intent.putExtra(PasswordBaseActivity.EXTRA_VALUES_FINISH_ACTIVITY, 1);//跳转到图片
                        } else {
                            intent.putExtra(PasswordBaseActivity.EXTRA_VALUES_FINISH_ACTIVITY, 0);//跳转到视频
                        }
                    }
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_capture:
            case R.drawable.btn_capture:
                if (!CheckUtils.IsContinuousClick(500)){
                    return;
                }
                if (mCamera == null) {
                    restartCamera();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            takePicture();
                        }
                    }, 1000);
                } else
                    takePicture();
                break;
            case R.id.btn_capture_mode_interval_timer:
                clearIntervalOrTimerCaptureMode();
                break;
            case R.drawable.btn_record_start:

                /**开始录像，post 事件**/
                /*if (mEncode) {
                    mEncode = false;
                    EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REGISTER_SUCCESS);
                } else {
                    mEncode = true;
                    EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_REPORT_STOP);
                }*/
                if (!checkQR()) {
                    startRecordAsUser();
                }
                if (isSoundReecord) {//一键切换功能，如果正在录音开启录像，停止录音
                    startRecordService(false);
                }
                mBtnCaptureRecord.setClickable(false);
                mHandler.sendEmptyMessageDelayed(Values.HANDLER_MAIN_OPEN_CLICK, 1500);

                break;
            case R.drawable.btn_record_stop:
                if (!checkQR()) {
                    stopRecordAsUserButton();
                }
                break;
            case R.id.btn_record_auto:
//                  toggleAutoMode();
                break;
            case R.id.btn_zoom_decrease:
                Log.e("zzx", "decrease");
                if (ZZXConfig.is4GA_TEST){
                    mIvZoom.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(Values.ZOOM_GONE, 3000);
                }
                cameraZoomDecrease();
                break;
            case btn_zoom_increase:
                Log.e("zzx", "increase");
                if (ZZXConfig.is4GA_TEST){
                    mIvZoom.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(Values.ZOOM_GONE, 3000);
                }
                cameraZoomIncrease();
                break;
            case R.id.btn_switch_camera:
                if (!CheckUtils.IsContinuousClick(500)){
                    return;
                }
                if(isKeyPic){
                    return;
                }
                switchCamera();
                break;
            case R.id.btn_switch_usbcamera:
                if (!CheckUtils.IsContinuousClick(500)){
                    return;
                }
                if(isKeyPic){
                    return;
                }
                if (!ZZXConfig.is4GA_TEST){
                    if (mIsRecording) {
                        mUserRecording = false;
                        stopRecord();
                    }
                }

                /**
                 * 1:需要在releaseRealTimeVideo方法前执行
                 * 2:通过判断实时上传对象是否为空来确定在切换到外置摄像头时，是否需要重新发送实时上传命令
                 * 3.收到外置摄像头开启预览成功后的广播后，发送实时上传命令，只发一次{@link ACTION_UVC_START_PREVIEW_OK}
                 * */
                if (mRealTimeTask != null) {
                    mIsRepeatSend = true;
                }
                releaseRealTimeVideo(false);
                if (!checkQR()) {
                    if (!ZZXConfig.is4GA_TEST){
                        stopRecordAsUserButton();
                    }
                }
                Settings.System.putInt(getContentResolver(), Values.OPEN_CAMERA_TYPE, Values.OPEN_USB_CAMERA);
                /**切换为usb摄像头*/
                minWindow();
                if (!ZZXConfig.is4GA_TEST){
                    releaseCamera();
                }
                Intent toUVCCamera = new Intent();
                toUVCCamera.setAction(Values.ACTION_ASTERN);
                toUVCCamera.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                toUVCCamera.putExtra(Values.INTENT_TYPE, Values.INTENT_TYPE_VALUE_BUTTON_REVERSE);
                toUVCCamera.putExtra(Values.STATUS, true);
                sendBroadcast(toUVCCamera);
                break;
            case R.id.btn_flash:
                if (isFlashOpened()) {
                    setflashEnable(false);
                } else {
                    setflashEnable(true);
                }
                break;
            case R.id.btn_ircut:
                if (isIrCutOpened()) {
                    setIrCutEnable(false);
                } else {
                    setIrCutEnable(true);
                }
                break;
            case R.id.btn_laser:
                if (isLaserOpened()) {
                    setlaserEnable(false);
                } else {
                    setlaserEnable(true);
                }
                break;
            case R.id.btn_menu:
                if (isBtnMenu) {
                    isOpenMenu = true;
                    mBtnFlash.setVisibility(View.VISIBLE);
                    if (isSubject) {
                        mBtnIrcut.setVisibility(View.VISIBLE);
                        mBtnLaser.setVisibility(View.VISIBLE);
                    }else {
                        mBtnLaser.setVisibility(View.VISIBLE);
                        mBtnIrcut.setVisibility(View.INVISIBLE);
                    }
                    mBtnZoomIncrease.setVisibility(View.INVISIBLE);
                    mBtnZoomDecrease.setVisibility(View.INVISIBLE);
                    mBtnMenu.setBackgroundResource(R.drawable.ic_open_light_on);
                } else {
                    isOpenMenu = false;
                    mBtnFlash.setVisibility(View.INVISIBLE);
//                    if (mCamera.getNumberOfCameras() > 1) {
                        mBtnLaser.setVisibility(View.INVISIBLE);
                        mBtnIrcut.setVisibility(View.INVISIBLE);
//                    }
                    mBtnZoomIncrease.setVisibility(View.VISIBLE);
                    mBtnZoomDecrease.setVisibility(View.VISIBLE);
                    mBtnMenu.setBackgroundResource(R.drawable.ic_open_light_off);
                }
                isBtnMenu = !isBtnMenu;
                break;
            case R.id.btn_report_confirm:
            case R.id.btn_report_cancel:
                dismissReportDialog();
                break;

            case R.id.btn_report_ok:
                openPowerSaveMode(getApplicationContext());
                isOpenPSM = true;
                dismissPowerSaveDialog();
                break;
            case R.id.btn_report_ignore:
                isOpenPSM = false;
                dismissPowerSaveDialog();
                break;
            case R.id.btn_not_enought_ignore:
                dismissNotEnoughDialog();
                mBurstSound.stop(mDidiStreamID);
                break;
            case R.id.btn_not_enought_ok:
                dismissNotEnoughDialog();
                mBurstSound.stop(mDidiStreamID);
                if (ZZXConfig.isP5XD_SXJJ){
                    if (SystemUtil.readSIMCard(this)){
                        SystemUtil.setMobileDataState(this,true);
                    }else {
                        SystemUtil.openWifi(this);
                    }
                }
                break;
            case R.id.btn_switch_laser:
                if ("1".equals(ZZXMiscUtils.read(ZZXMiscUtils.LASER_PATH))) {
                    mBtnLaserSwitcher.setBackgroundResource(R.drawable.laser_off);
                    ZZXMiscUtils.controlLaser(ZZXMiscUtils.CLOSE);
                } else {
                    ZZXMiscUtils.controlLaser(ZZXMiscUtils.OPEN);
                    mBtnLaserSwitcher.setBackgroundResource(R.drawable.laser_on);
                }
                break;
            case R.id.btn_talk_ok:
                mHandler.removeMessages(Values.TALK_DIALOG);
                vib.cancel();
                dismissTalkDialog();
                MainActivity.mProtocolUtils.parserCenterAudio((String[]) eventMessage.getMsg(), BaseProtocol.SUCCESS);
                MainActivity.mProtocolUtils.parserCenterTalk((String[]) eventMessage.getMsg());
                mBurstSound.stop(mDidiStreamID);
                break;
            case R.id.btn_talk_ignore:
                mHandler.removeMessages(Values.TALK_DIALOG);
                vib.cancel();
                MainActivity.mProtocolUtils.parserCenterAudio((String[]) eventMessage.getMsg(), BaseProtocol.SECOND);
                mBurstSound.stop(mDidiStreamID);
                dismissTalkDialog();
                break;
            case R.id.big_upload:
                addSmallWindow();
                break;
            case R.id.btn_min:
                addSmallWindow();
                break;
            case R.id.flash_easyicon:
                if (isLedFlash()){
                    writeToDevice(0,"/sys/devices/platform/zzx-misc/flash_test");
                }else {
                    writeToDevice(10,"/sys/devices/platform/zzx-misc/flash_test");
                }
                break;
        }
    }

    private boolean checkQR() {
        return mReceiveStartRecordFromClient;
    }

    private void switchCamera() {
        if (Camera.getNumberOfCameras() <= 1) {
            TTSToast.showToast(this, R.string.only_one_camera, false);
            return;
        }
        mBtnCaptureRecord.setClickable(false);
        releaseCamera();
        if (Camera.getNumberOfCameras() == 3){
            //后置
            if (mFrontCamera && !mBehindCamera) {//0-1
                mBehindCamera = true;
                mFrontCamera = false;
                isSubject = false;
                initCamera(1);
                mBtnIrcut.setVisibility(View.INVISIBLE);
//                showModeRatio();
//                mCameraName.setText(R.string.night_camera);
            } else if (!mFrontCamera && mBehindCamera) {//1-2
                mBehindCamera = false;
                mFrontCamera = false;
                isSubject = true;
                initCamera(Camera.getNumberOfCameras() - 1);
                if (isOpenMenu){
                    mBtnIrcut.setVisibility(View.VISIBLE);
                }else {
                    mBtnIrcut.setVisibility(View.INVISIBLE);
                }
//                showModeRatio();
//                mCameraName.setText(R.string.pic_camera);
//                toggleToPictureMode();
            } else if (!mFrontCamera && !mBehindCamera) {//2-0
                mFrontCamera = true;
                mBehindCamera = false;
                isSubject =false;
                initCamera(0);
                mBtnIrcut.setVisibility(View.INVISIBLE);
//                mCameraName.setText(R.string.front_camera);
//                toggleToRecordMode();
            }
        }else {
            if (mFrontCamera) {
                mFrontCamera = false;
                isSubject = true;
//                initCamera(Camera.getNumberOfCameras() - 1);
                initCamera(0);
                if (isOpenMenu){
                    mBtnIrcut.setVisibility(View.VISIBLE);
                }else {
                    mBtnIrcut.setVisibility(View.INVISIBLE);
                }
                //   showModeRatio();
            } else {
                mFrontCamera = true;
                isSubject = false;
                //   hideModeRatio();
//                initCamera(0);
                mBtnIrcut.setVisibility(View.INVISIBLE);
                initCamera(Camera.getNumberOfCameras() - 1);
            }
        }
        restartPreview();
    }

    /**
     * 相机焦距缩小
     *
     * @see #cameraZoomIncrease()
     */
    private void cameraZoomDecrease() {
        if (mCamera == null || mParameters == null) {
            return;
        }
//        mZoomLevel = mParameters.getZoom();
        if (mZoomLevel >= 2) {
            if (mZoomLevel == mCameraZoomMax) {
                mParameters.setZoom(mZoomLevel - 4);
                mZoomLevel -= 4;
            } else {
                mParameters.setZoom(mZoomLevel - 2);
                mZoomLevel -= 2;
            }
            mCamera.setParameters(mParameters);
            if (ZZXConfig.is4GA_TEST){
                refreshZoomLevel_4ga();
            }else {
                refreshZoomLevel();
            }
        }
    }

    private void refreshZoomLevel() {
        switch (mZoomLevel) {
            case 0:
                mIvZoom.setBackground(new ColorDrawable(android.R.color.transparent));
                break;
            case 2:
                mIvZoom.setBackgroundResource(R.drawable.zoom_2);
                break;
            case 4:
                mIvZoom.setBackgroundResource(R.drawable.zoom_4);
                break;
            case 6:
                mIvZoom.setBackgroundResource(R.drawable.zoom_8);
                break;
            case 8:
                mIvZoom.setBackgroundResource(R.drawable.zoom_8);
                break;
            case 10:
                mIvZoom.setBackgroundResource(R.drawable.zoom_16);
                break;
        }
    }
    private void refreshZoomLevel_4ga() {
        switch (mZoomLevel) {
            case 0:
                mIvZoom.setBackground(new ColorDrawable(android.R.color.transparent));
                break;
            case 2:
                mIvZoom.setBackgroundResource(R.drawable.zoom_4);
                break;
            case 4:
                mIvZoom.setBackgroundResource(R.drawable.zoom_8);
                break;
            case 6:
                mIvZoom.setBackgroundResource(R.drawable.zoom_16);
                break;
            case 8:
                mIvZoom.setBackgroundResource(R.drawable.zoom_16);
                break;
            case 10:
                mIvZoom.setBackgroundResource(R.drawable.zoom_32);
                break;
        }
    }

    /**
     * @see #cameraZoomDecrease()
     */
    private void cameraZoomIncrease() {
        if (mCamera == null || mParameters == null) {
            return;
        }
        if (mZoomLevel == mCameraZoomMax - 4) {
            mParameters.setZoom(mZoomLevel + 4);
            mZoomLevel += 4;
        } else if (mZoomLevel < mCameraZoomMax - 4) {
            mParameters.setZoom(mZoomLevel + 2);
            mZoomLevel += 2;
        }
        mCamera.setParameters(mParameters);
        if (ZZXConfig.is4GA_TEST){
            refreshZoomLevel_4ga();
        }else {
            refreshZoomLevel();
        }

    }

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            if (mPowerManager == null) {
                mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
            }
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Values.TAG_WAKE_LOCK);
        }
        if (!mWakeLocked) {
            mWakeLocked = true;
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLocked) {
            Values.LOG_I(TAG, "mWakeLock != null");
            mWakeLock.release();
            mWakeLock = null;
        }
        mWakeLocked = false;
    }

    private void acquireRealWakeLock() {
        if (mRealWakelock == null) {
            if (mPowerManager == null) {
                mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
            }
            mRealWakelock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Values.TAG_REAL_WAKE_LOCK);
            mRealWakelock.acquire();
        }
    }

    private void releaseRealWakeLock() {
        if (mRealWakelock != null) {
            Values.LOG_I(TAG, "mWakeLock != null");
            mRealWakelock.release();
            mRealWakelock = null;
        }
    }

    /**
     * 用户主动控制开始录像的入口.最后真正开始是{@link #startRecord()}此方法会进行相关的UI刷新操作.
     * 开启唤醒锁,避免CPU黑屏时睡眠.会先判断是否开启延录.若处于预录模式下则先关闭后台录像.
     * 再通过mHandler来异步录像.
     */
    private void startRecordAsUser() {
        Values.LOG_I(TAG, "startRecordAsUser");
        if (mCamera == null) {
            return;
        }
//        if (!mReportWithRecord && mNeedReportVideo)
//            return;
//        acquireWakeLock();
        mUserRecording = true;
        /**若不在录像状态则将录像时长清零,否则不变继续叠加.
         * */
        if (!mIsRecording)
            mRecordTime = 0;
        /**去除延录状态.
         * */
        mHandler.removeMessages(Values.HANDLER_MAIN_STOP_RECORD);
        SharedPreferences sp = RemotePreferences.getRemotePreferences(this);
        int delay = Integer.parseInt(sp.getString(getKey(R.string.key_record_delay), "0"));
        if (delay != 0) {
            mPreRecordMode = false;
            mManualStopRecord = false;
            mPreRecordCount = 0;
        }
        startRecord();
    }

    /**
     * 用户主动控制停止录像的入口.真正关闭操作是{@link #stopRecord()}此方法会进行相关的UI刷新操作.
     * 若处于延录状态则delay停止.
     * 释放唤醒锁.
     */
    private void stopRecordAsUserButton() {
        if (mCamera == null || !isExternalStorageEnabled()) {
            return;
        }
        mIvImportantFile.setVisibility(View.GONE);
        SharedPreferences sp = RemotePreferences.getRemotePreferences(this);
        int delay = Integer.parseInt(sp.getString(getKey(R.string.key_record_delay), "0"));
        mHandler.sendEmptyMessageDelayed(Values.HANDLER_MAIN_STOP_RECORD, delay * 1000);
        mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_start);
        mBtnCaptureRecord.setId(R.drawable.btn_record_start);

        if (mPreRecordMode) {
            mRecordTime = 0;
        }
        mLastCutTime = 0;
        Settings.System.putString(getContentResolver(),"case_number","");
        if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY)
            Settings.System.putString(getContentResolver(),"LolaLive_Record","");
    }

    private void stopRecordAsMain() {
//        releaseWakeLock();
        mPreRecording = false;
        mUserRecording = false;
        mManualStopRecord = true;
        mPreRecordMode = false;
        if (!mIsRecording) {
            mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_start);
            mBtnCaptureRecord.setId(R.drawable.btn_record_start);
        } else {
            stopRecord();
        }
    }

    /**
     * 切换是否开启自动录像模式
     */
    private void toggleAutoMode() {
        boolean auto = RemotePreferences.getRemotePreferences(this).getBoolean(getKey(R.string.key_record_auto), false);
        if (auto) {
            mBtnRecordAuto.setBackgroundResource(R.drawable.auto_record_off);
        } else {
            mBtnRecordAuto.setBackgroundResource(R.drawable.auto_record_on);
        }
        mSPEditor.putBoolean(getKey(R.string.key_record_auto), !auto).commit();
    }

    /**
     * 通过EventBus发送停止消息.在异步调用方法停止录像,同时往数据库里保存相应数据.
     *
     * @see #stopRecordAndRestoreDatabase()
     * @see #startRecord()
     * @see #mManualRecording
     * @see #mManualStopRecord
     * @see #recordSection()
     */
    private void stopRecord() {
        Values.LOG_I(TAG, "stopRecord()");
        sendLogcatRecord(false);
        mManualRecording = false;
        mHandler.removeMessages(Values.BACK_RECORD_STOP);
        IntentInfo start = new IntentInfo(Values.ACTION_START_RECORD, 0);
        EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, start);
//        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.BACK_STOP_RECORD);
        mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_start);
        mBtnCaptureRecord.setId(R.drawable.btn_record_start);
        Settings.System.putInt(getContentResolver(),
                Values.ACTION_SCREEN_NEVER, 0);//修改屏幕休眠状态
        stopRecordAndRestoreDatabase();
        if (ZZXConfig.mVideoCut) {
            mCutTime = mRecordTime - mLastCutTime;
            mLastCutTime = mRecordTime;
            if (mCutTime > MainActivity.CUT_VIDEO_RECORD_TIME) {
                Settings.System.putInt(getContentResolver(), Values.VIDEO_CUT, -1);
                EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.CUT_VIDEO);
            }
        }
//        mRecordTime = 0;
        if (!ZZXConfig.isP5XJNKYD){
            if (isVibrator(getApplication()) && !mPortRecordMode) {
                Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
                vib.vibrate(200);
            }
        }
        //controllDevice(1, "0x00ff00 0 0 0 0 0 0");

        isVideoReecord = false;
        sendVideoRecordNotify(isVideoReecord);

        turnofflaser();
        //  mParameters.set("mtk-cam-mode",1);
//        if (!isScreen){
//            mHandler.sendEmptyMessageDelayed(Values.RELEASE_CAMERA,15*1000);
//        }

    }
    private String mDurationFilePath;//获取文件时长的文件路径
    private String mGpsDurationPath;
    /**
     * @see #initRecorderAndRecord()
     * @see #disableCaptureOrRecordSetting()
     */
    private void stopRecordAndRestoreDatabase() {
        enableCaptureOrRecordSetting();
        closeGpsWriter();
        synchronized (mLockObject) {
            if (mIsRecording) {
                try {
                    if (mMediaRecorder == null)
                        return;
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                    mCamera.lock();
                    if (mVideoFilePath.contains("start")) {
                        String replaceVideoFilePath;
                        replaceVideoFilePath = mVideoFilePath.replace("start", FileUtils.filesize(mVideoFilePath)+"");
                        File file = new File(mVideoFilePath);
                        file.renameTo(new File(replaceVideoFilePath));
                        mVideoFilePath = replaceVideoFilePath;
                        videoCutFilePath = replaceVideoFilePath;
                        mDurationFilePath = replaceVideoFilePath;
                    }
                    if (mGpsFileFullPath.contains("start")) {
                        String replaceGpsFilePath = mGpsFileFullPath.replace( "start", FileUtils.filesize(mGpsFileFullPath)+"");
                        File file = new File(mGpsFileFullPath);
                        file.renameTo(new File(replaceGpsFilePath));
                        mGpsFileFullPath = replaceGpsFilePath;
                        mGpsDurationPath = replaceGpsFilePath;
                    }
                    if (mImportantFileSign) {
                        if (getVideoformat(this)){
                            if (!mVideoFilePath.endsWith(Values.FILE_SUFFIX_IMPORTANT_VID)) {
                                String replaceFile = mVideoFilePath;
                                replaceFile = replaceFile.replace(Values.FILE_SUFFIX_VID_AVI, Values.FILE_SUFFIX_IMPORTANT + Values.FILE_SUFFIX_VID_AVI);
                                File file = new File(mVideoFilePath);
                                file.renameTo(new File(replaceFile));
                                mVideoFilePath = replaceFile;
                                videoCutFilePath = replaceFile;
                                mDurationFilePath = replaceFile;
                                File gpsFile = new File(mGpsFileFullPath);
                                if (gpsFile.exists()) {
                                    String replace = mGpsFileFullPath;
                                    replace = replace.replace(Values.FILE_SUFFIX_SRT_GPS, Values.FILE_SUFFIX_IMPORTANT + Values.FILE_SUFFIX_SRT_GPS);
                                    gpsFile.renameTo(new File(replace));
                                    mGpsFileFullPath = replace;
                                    mGpsDurationPath = replace;
                                }
                            }
                        }else {
                            if (!mVideoFilePath.endsWith(Values.FILE_SUFFIX_IMPORTANT_VID_AVI)) {
                                String replaceFile = mVideoFilePath;
                                replaceFile = replaceFile.replace(Values.FILE_SUFFIX_VID, Values.FILE_SUFFIX_IMPORTANT + Values.FILE_SUFFIX_VID);
                                File file = new File(mVideoFilePath);
                                file.renameTo(new File(replaceFile));
                                mVideoFilePath = replaceFile;
                                videoCutFilePath = replaceFile;
                                mDurationFilePath = replaceFile;
                                File gpsFile = new File(mGpsFileFullPath);
                                if (gpsFile.exists()) {
                                    String replace = mGpsFileFullPath;
                                    replace = replace.replace(Values.FILE_SUFFIX_SRT_GPS, Values.FILE_SUFFIX_IMPORTANT + Values.FILE_SUFFIX_SRT_GPS);
                                    gpsFile.renameTo(new File(replace));
                                    mGpsFileFullPath = replace;
                                    mGpsDurationPath = replace;
                                }
                            }
                        }

                        mImportantFileSign = false;
                        mVideoLongPressNum = 0;
                    }
                    if (mVideoFilePath!=null){
                        if (Settings.System.getInt(getContentResolver(),"ZZXEncryption",0)==1){
                            if (getVideoformat(this)){
                                mEncryptionUtil.addUnencry(mDurationFilePath,Values.FILE_SUFFIX_VID_AVI);
                            }else {
                                mEncryptionUtil.addUnencry(mDurationFilePath,Values.FILE_SUFFIX_VID);
                            }
                            mEncryptionUtil.addUnencry(mGpsDurationPath,Values.FILE_SUFFIX_SRT_GPS);
                        }
                        /**视频录制完成后自动上传到FTP服务器*/
                        if (SystemUtil.isNetworkConnected(CameraService.this) && Settings.System.getInt(getContentResolver(),"ZZXAutomatic",0)==1
                               && mDurationFilePath != null) {
                            if (mUploadFileUtil.isSetFTP()){
                                mUploadFileUtil.uploadVideoFile(FTPUploadInfo.getFTPUploadInfo(CameraService.this, mDurationFilePath));
                                mUploadFileUtil.uploadVideoFile(FTPUploadInfo.getFTPUploadInfo(CameraService.this, mGpsDurationPath));
                            }
                        }
                    }
//                    LogUtil.e("结束录像文件：", mVideoFilePath);
                    sendBroadcastSP(Values.SPXZ_RECORD_VIDEO,mDurationFilePath);
                    mMediaRecorder = null;
                } catch (Exception e) {
                    e.printStackTrace();
                    releaseRecorder();
                    return;
                }
                mIsRecording = false;
                EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.VIDEO_THUMBNAIL);
                //  EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORE_DATA_BASE);
                if (mHttpStartRecord) {
                    mHttpStartRecord = false;
                    Settings.System.putString(getContentResolver(), Values.HTTP_UPLOAD_PATH, mVideoFilePath);
                }
            }
        }
    }

    /**
     * 只停止MediaRecorder,不进行任何别的数据相关操作.
     */
    private void stopRecordAsMedia() {
        if (mIsRecording) {
            try {
                if (mMediaRecorder == null)
                    return;
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mCamera.lock();
                mMediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
                releaseRecorder();
                return;
            }
            mIsRecording = false;
        }
    }

    /**
     * 录像结束,可以设置某些按键.
     */
    private void enableCaptureOrRecordSetting() {
        mBtnSwitcher.setClickable(true);
        mBtnMode.setClickable(true);
        mBtnResolution.setClickable(true);
        mBtnRecordAuto.setClickable(true);
        mBtnCameraSwitcher.setClickable(true);
        if (!ZZXConfig.is4GA_TEST){
            mBtnUsbCameraSwitcher.setClickable(true);
        }
    }

    /**
     * 录像结束将结束时间保存进数据库.
     */
    private void conserveDatabase() {
        try {
            String end = mFileDateFormat.format(new Date());
            new DatabaseInsertTask(this).execute(mVideoFilePath, end.split("_")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getKey(int id) {
        if (mRes == null) {
            mRes = getResources();
        }
        return mRes.getString(id);
    }

    /**
     * 获取分辨率
     *
     * @param ratioKey 录像或者拍照的Preference的keyId
     */
    private int[] getRatio(int ratioKey) {
        int[] ratioInt = new int[2];
        String ratio = null;
        switch (ratioKey) {
            case R.string.key_ratio_picture:
                ratio = RemotePreferences.getRemotePreferences(this).getString(getKey(ratioKey), Values.RATIO_CAPTURE_DEFAULT_VALUE);
                break;
            case R.string.key_ratio_record:
                if (ZZXConfig.isP5XJNKYD){
                    ratio = RemotePreferences.getRemotePreferences(this).getString(getKey(ratioKey), Values.JINAN_RECORD_DEFAULT_VALUE);
                }else {
                    ratio = RemotePreferences.getRemotePreferences(this).getString(getKey(ratioKey), Values.RATIO_RECORD_DEFAULT_VALUE);
                }
                break;
        }
        Values.LOG_W(TAG, "ratio = " + ratio);
        String[] ratios = ratio.split("x");
        ratioInt[0] = Integer.parseInt(ratios[0]);
        ratioInt[1] = Integer.parseInt(ratios[1]);
        if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
            switch (ratioInt[1]){
                case 1080:
                    Settings.System.putInt(getContentResolver(),"resolution",2);
                    break;
                case 720:
                    Settings.System.putInt(getContentResolver(),"resolution",1);
                    break;
                case 480:
                    Settings.System.putInt(getContentResolver(),"resolution",0);
                    break;
            }

        }
        return ratioInt;
    }

    /**
     * @see #stopRecordAndRestoreDatabase()
     */
    private void initRecorderAndRecord() {
        Log.e("TAG", "initRecorderAndRecord");
        clearIDLECount();
        if (mIsRecording || mCamera == null) {
            return;
        }
        mGpsSrtEnabled = Settings.System.getInt(getContentResolver(), GPS_SRT, 1) == 1;
        if (ZZXConfig.is4G5_BAD || ZZXConfig.isP5C_HLN){
            mGpsSrtEnabled = false;
        }
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        synchronized (mLockObject) {
            try {
                if (ZZXConfig.IsRotate) {
                    mCamera.unlock();
                    mMediaRecorder.setCamera(mCamera);
                } else {
                    // 设置矫正相机角度
                    recordDirection(mCurrentOrientation, mCamera);
                }
                if (mRecordManager.needSound())
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                int[] ratios = getRatio(R.string.key_ratio_record);
             /*   if (mFrontCamera) {
                    ratios[0] = 1280;
                    ratios[1] = 720;
                }*/
                int audioBitRate = 128000;
                int audioChannels = 2;
                int audioCodec = 0;
                int audioSampleRate = 0;
                int videoBitRate = 0;
                int videoCodec = 0;
                int videoFrameRate = 30;
                int videoFrameWidth = 0;
                int videoFrameHeight = 0;
                /*if (mQRScan) {
                    ratios[1] = 480;
                }*/
                if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
                    int resolution  = Settings.System.getInt(getContentResolver(),"resolution",-1);
                    switch (resolution){
                        case 0:
                            ratios[1] = 480;
                            break;
                        case 1:
                            ratios[1] = 720;
                            break;
                        case 2:
                            ratios[1] = 1080;
                            break;
                    }
                }

                if (ZZXConfig.isSongJian) { //送检设置w
                    switch (ratios[1]) {
                        case 1080:
                            audioCodec = MediaRecorder.AudioEncoder.HE_AAC;
                            audioSampleRate = 48000;
                            videoBitRate = 6000000 * 3;//3300000
                            videoCodec = MediaRecorder.VideoEncoder.H264;
                            videoFrameWidth = 1920;
                            videoFrameHeight = 1080;
                            break;
                        case 720:
                            audioCodec = MediaRecorder.AudioEncoder.HE_AAC;
                            audioSampleRate = 16000;
                            videoBitRate = 4000000 * 3;
                            //       videoBitRate = 12000000;
                            videoCodec = MediaRecorder.VideoEncoder.H264;
                            videoFrameWidth = 1280;
                            videoFrameHeight = 720;
                            break;
                        case 480:
                            audioCodec = MediaRecorder.AudioEncoder.HE_AAC;
                            audioSampleRate = 14000;
                            videoBitRate = 1600000 * 3;//3300000
                            videoCodec = MediaRecorder.VideoEncoder.H264;
                            videoFrameWidth = 864;
                            videoFrameHeight = 480;
                            break;
                        case 576:
                            CamcorderProfile profile1 = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                            audioCodec = profile1.audioCodec;
                            audioSampleRate = 14000;
                            videoBitRate = 800000 * 3;
                            videoCodec = profile1.videoCodec;
                            videoFrameWidth = 720;
                            videoFrameHeight = 576;
                            break;
                    }
                } else {
                    Log.d(TAG, "initRecorderAndRecord() called"+ratios[1]);
                    switch (ratios[1]) {//正常出货设置
                        case 1800:
                        case 1296:
                        case 1080:
                            int videobit = Settings.System.getInt(getContentResolver(),"video_rate",8);
                            audioCodec = MediaRecorder.AudioEncoder.HE_AAC;
                            audioSampleRate = 48000;
//                            videoBitRate = 12000000;//3300000  4GA
                            videoBitRate = videobit*1000000;
                            if (ZZXConfig.is4G5 || ZZXConfig.isP80 ){
                                videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;
                            }else {
                                videoCodec = MediaRecorder.VideoEncoder.H264;
                            }
                            videoFrameWidth = 1920;
                            videoFrameHeight = 1080;
                            break;
                        case 720:
                            int videobit_720 = Settings.System.getInt(getContentResolver(),"video_rate",4);
                            audioCodec = MediaRecorder.AudioEncoder.HE_AAC;
                            audioSampleRate = 16000;
//                            videoBitRate = 4800000;//4GA
                            videoBitRate = videobit_720*1000000;
                            if (ZZXConfig.is4G5 || ZZXConfig.isP80 ){
                                videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;
                            }else {
                                videoCodec = MediaRecorder.VideoEncoder.H264;
                            }
                            videoFrameWidth = 1280;
                            videoFrameHeight = 720;
                            break;
                        case 480:
                            int videobit_480 = Settings.System.getInt(getContentResolver(),"video_rate",2);
                            audioCodec = MediaRecorder.AudioEncoder.HE_AAC;
                            audioSampleRate = 14000;
//                            videoBitRate = 2500000;//4GA
                            videoBitRate = videobit_480*1000000;
                            if (ZZXConfig.is4G5 || ZZXConfig.isP80 ){
                                videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;
                            }else {
                                videoCodec = MediaRecorder.VideoEncoder.H264;
                            }
                            if (ZZXConfig.isP5XJNKYD){
                                videoFrameWidth = 640;
                            }else {
                                videoFrameWidth = 864;
                            }

                            videoFrameHeight = 480;
                            break;
                        case 576:
                            CamcorderProfile profile1 = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                            audioCodec = profile1.audioCodec;
                            audioSampleRate = 14000;
                            videoBitRate = 1500000;
                            videoCodec = profile1.videoCodec;
                            videoFrameWidth = 720;
                            videoFrameHeight = 576;
                            break;
                    }
                }
                if (ZZXConfig.is4GA_HNYC){
                    audioCodec = MediaRecorder.AudioEncoder.HE_AAC;
                    audioSampleRate = 48000;

                    videoCodec = MediaRecorder.VideoEncoder.H264;
                    switch (ratios[0]){
                        case 2560:
                            videoBitRate = 12000000;
                            videoFrameWidth = 1920;
                            videoFrameHeight = 1080;
                            break;
                        case 1920:
                            if (ratios[1]==1080){
                                videoBitRate = 8000000;
                                videoFrameWidth = 1280;
                                videoFrameHeight = 720;
                            }
                            if (ratios[1]==720){
                                videoBitRate = 4000000;
                                videoFrameWidth = 640;
                                videoFrameHeight = 480;
                            }
                            break;
                    }
                }
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setVideoFrameRate(videoFrameRate);
                mMediaRecorder.setVideoSize(videoFrameWidth, videoFrameHeight);
                Log.e("zzxzdm", "bf1----videoFrameWidth " + videoFrameWidth + "    videoFrameHeight=" + videoFrameHeight);
                mMediaRecorder.setVideoEncodingBitRate(videoBitRate);
                mMediaRecorder.setVideoEncoder(videoCodec);
                if (mRecordManager.needSound()) {
                    mMediaRecorder.setAudioEncodingBitRate(audioBitRate);
                    mMediaRecorder.setAudioChannels(audioChannels);
                    mMediaRecorder.setAudioSamplingRate(audioSampleRate);
                    mMediaRecorder.setAudioEncoder(audioCodec);
                }
                mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                    @Override
                    public void onInfo(MediaRecorder mr, int what, int extra) {
                        switch (what) {
                            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                                mMediaRecorder.setOnInfoListener(null);
                                recordSection();
                                break;
                        }
                    }
                });
                mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mr, int what,
                                        int extra) {
                        Values.LOG_E(TAG, "onError.what = " + what
                                + "; extra = " + extra);
                        releaseRecorder();
//                        releaseCamera();
                    }
                });
//                mPreVideoFilePath = mVideoFilePath;
                mVideoFilePath = getFilePath(Values.FILE_TYPE_VID);
                if (mGpsSrtEnabled) {
                    mGpsWriter = new BufferedWriter(new FileWriter(mGpsFileFullPath));
                }
                mPreVideoFilePath = mVideoFilePath;
//                LogUtil.e("开始录像文件：", mVideoFilePath);
                if (!mPreRecordMode) {
                    /**若处于预录模式,则不能使用最大录制时间而改用{@link #mHandler}来控制自动结束.
                     * */
                    int recordTime = mRecordManager.getRecorderTime();
                    if (recordTime != 0)
                        if (ZZXConfig.mVideoCut) {
                            mMediaRecorder.setMaxDuration(10 * 60 * 1000);
                        } else {
                            mMediaRecorder.setMaxDuration(recordTime);
                        }
                }
                if (mQRScan) {
                    mMediaRecorder.setMaxDuration(SCAN_RECORD_TIME);
                }
                mMediaRecorder.setMaxFileSize(3758096384L);
                mMediaRecorder.setOutputFile(mVideoFilePath);
                mMediaRecorder.prepare();
                mMediaRecorder.start();
                if (mNeedReportVideo ){
                    mCamera.setPreviewCallback(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
                releaseRecorder();
                releaseCamera();
                return;
            }
        }
        mIsRecording = true;
        if (mGpsSrtEnabled) {
            mThreadHandler.sendEmptyMessageDelayed(Values.WRITE_GPS_DATA, 2000);
        }

    }

    private void writeGpsInfo() {
        try {
            if (mIsRecording && mGpsWriter != null) {
                Values.LOG_W(TAG, "writeGpsInfo");
                mRecordCount++;
                String time = TimeFormatUtils.secToHour(mRecordCount);
                String temp = getString(R.string.gps_timestamp, mRecordCount, time, time);
                mGpsWriter.append(temp);
                mGpsWriter.append(mNmea).append("\n");
                mThreadHandler.sendEmptyMessageDelayed(Values.WRITE_GPS_DATA, 1000);
            } else {
                mThreadHandler.removeMessages(Values.WRITE_GPS_DATA);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeGpsWriter() {
        mThreadHandler.removeMessages(Values.WRITE_GPS_DATA);
        try {
            if (mGpsWriter != null) {
                mGpsWriter.close();
                mGpsWriter = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkNeedClearIntervalOrTimerCaptureMode() {
        if (mIntervalOrTimerMode) {
            clearIntervalOrTimerCaptureMode();
        }
    }

    /**
     * 清除空闲时间计数.超过{@link com.zzx.police.data.Values#IDLE_MAX_COUNT}分钟会关闭Camera
     */
    private void clearIDLECount() {
        mIDLECount = 0;
    }

    private CamcorderProfile getMTKCamcorderProfile(int quality) {
        Class<?> mCamcorderProfileClass = CamcorderProfile.class;
        Method getMTKProfileMethod;
        try {
            getMTKProfileMethod = mCamcorderProfileClass.getMethod("getMtk", Integer.TYPE);
            return (CamcorderProfile) getMTKProfileMethod.invoke(null, quality);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 此为用户主动录像时UI刷新方法.
     * 再通过EventBusUtils后台执行record.
     *
     * @see #stopRecord()
     * @see #restartPreview()
     * @see #stopRecordAsUserButton()
     * @see #startRecordAsUser()
     */
    private void startRecord() {
        sendLogcatRecord(true);
        mHandler.sendEmptyMessageDelayed(Values.START_RECORD_LOGCAT,400);
        //   mParameters.set("mtk-cam-mode", 0);
        mPreRecordCount = 0;
//        if (!mReportWithRecord && mNeedReportVideo)
//            return;
        Values.LOG_I(TAG, "startRecord()");
        mManualRecording = true;
        /**若正处於预录模式,取消自动停止事件并继续录像.
         * */
        mHandler.removeMessages(Values.BACK_RECORD_STOP);
        IntentInfo start = new IntentInfo(Values.ACTION_START_RECORD, 1);
        //这里会去发送广播，系统的frameWork层会接收到广播，并触发震动丶LED灯变红丶状态栏显示录像图标等操作
        EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, start);
//        mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_stop);
//        mBtnCaptureRecord.setId(R.drawable.btn_record_stop);
//        initRecorderAndRecord();
//        //手动录像则取消可能存在的预录的自动停止事件.
////        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.BACK_START_RECORD);
//        int time = mRecordManager.getRecorderTime();
//        /**处於预录模式下再启动分段录像也是需要通过Handler来延时发送停止事件.
//         * */
//        if (mPreRecordMode && time != 0) {
//            mHandler.sendEmptyMessageDelayed(Values.MAIN_STOP_RECORD, time);
//        }
//        disableCaptureOrRecordSetting();
        if (!ZZXConfig.isP5XJNKYD){
            if (isVibrator(getApplicationContext()) && !mPortRecordMode) {
                Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
                vib.vibrate(200);
            }
        }
        // controllDevice(1, "0x0000ff 1 2 2 2 2 2");
//        controllDevice(1, LIGHT_COLOR_RED);

        isVideoReecord = true;
        sendVideoRecordNotify(isVideoReecord);
//        turnonlaser();
//        mHandler.sendEmptyMessageDelayed(Values.HANDLER_MAIN_STOP_LASER, 5000);

    }

    private void initOritntationListener() {
        OrientationEventListener mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                // 不记录相等的值；不记录没获取到时返回的-1
                if (mCurrentOrientation != orientation && orientation >= 0) {
                    mCurrentOrientation = orientation; // 记录当前角度
                }
            }
        };
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
        }
//        int rotaion = 0;

//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            if (rotation <= 90) {
//                Log.e(TAG, "<= 45度，不管" + rotation);
//                mMediaRecorder.setOrientationHint(180);
//            } else if (rotation <= 90) {
//                Log.e(TAG, ">90度，度数" + rotation);
//                mMediaRecorder.setOrientationHint(0);
//            }
    }

    private void notifyRrecord(String logcatRecord, String zzxState, boolean value) {
        Intent recordintent = new Intent();
        recordintent.setAction(logcatRecord);
        recordintent.putExtra(zzxState, value);
        sendBroadcast(recordintent);
    }

    private boolean isVibrator(Context mcontext) {
        Log.e("Vibrator", "Vibrator" + Settings.System.getInt(mcontext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 1));
        return Settings.System.getInt(mcontext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 1;
    }

    private void sendVideoRecordNotify(boolean isRecording) {
        notifyRrecord(Values.ACTION_VIDEO_RECORD, "record_state", isRecording);
    }

    private void sendLogcatRecord(boolean isRecording) {
        if (!ZZXConfig.is4GA_CDHN || !ZZXConfig.isP5C_CDHN || !ZZXConfig.isP5X_CDHN){
            notifyRrecord(Values.LOGCAT_RECORD, Values.ZZX_STATE, isRecording);
        }
        if (isRecording) {
            if (!mPortRecordMode) {
                if (!ZZXConfig.isP5XJNKYD){
                    if (ZZXConfig.is4GC || ZZXConfig.is4GA_HNDW || ZZXConfig.is4GA_CDHN || ZZXConfig.is4GA_SDTY) {
                        if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
                            mBurstSound.play(mStartRecordSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
                        }else {
                            mBurstSound.play(mRecordNorSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
                        }
                    } else {
                        mBurstSound.play(mStartRecordSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
//                Toast.makeText(getApplicationContext(), R.string.start_record, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (!mPortRecordMode) {
                if (!ZZXConfig.isP5XJNKYD){
                    if (ZZXConfig.is4GC || ZZXConfig.is4GA_HNDW || ZZXConfig.is4GA_CDHN || ZZXConfig.is4GA_SDTY) {
                        if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
                            mBurstSound.play(mStopRecordSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
                        }else {
                            mBurstSound.play(mRecordNorSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
                        }
                    } else {
                        mBurstSound.play(mStopRecordSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
//                Toast.makeText(getApplicationContext(), R.string.stop_record, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 根据文件类型获得文件名,同时会生成需要的日期目录.
     */
    private String getFilePath(int type) {
        String time = mFileDateFormat.format(new Date());
        String[] timeValues = time.split("_");
        mDate = timeValues[0];
        if (time.equals(mPrePicTime)) {
            mPreSameCount++;
            mPrePicTime = time;
            time += "_" + mPreSameCount;
        } else {
            mPreSameCount = 0;
            mPrePicTime = time;
        }
        File saveDir = null;
        String case_number = null;
        mPicTime = timeValues[1];
        String policeNum = DeviceUtils.getPoliceNum(this);
        case_number = Settings.System.getString(getContentResolver(),"case_number");
        if (!TextUtils.isEmpty(case_number)){
            mFileName = case_number+"_"+policeNum + "_" + type + "_" + time + "_" + "start";
        }else {
            if (ZZXConfig.is4GC_SDJW  || ZZXConfig.is4GC_SY){
                mFileName = time + "_" + "start";
            }else {
                mFileName = policeNum + "_" + type + "_" + time + "_" + "start";
            }
        }

        //扫描二维码获取派警单编号
        switch (type) {
            case Values.FILE_TYPE_VID:
                saveDir = new File(StoragePathConfig.getVideoDirPath(this) + File.separator + mDate);
                String lolaLive_record = Settings.System.getString(getContentResolver(),"LolaLive_Record");
                if (!TextUtils.isEmpty(lolaLive_record)){
                    mFileName = mFileName.replace(mFileName,lolaLive_record+"_"+mFileName);
                }

                if (mImportantFileSign) {
                    mFileName += Values.FILE_SUFFIX_IMPORTANT;
                    mImportantFileSign = false;
                    mVideoLongPressNum = 0;
                }
                File gpsFile = new File(StoragePathConfig.getTextDirPath(this)+ File.separator + mDate);
                if (!gpsFile.exists() || !gpsFile.isDirectory()) {
                    if (!ZZXConfig.isP5C_HLN)
                    gpsFile.mkdirs();
                }
                mGpsFileFullPath = new File(gpsFile, mFileName + Values.FILE_SUFFIX_SRT_GPS).getAbsolutePath();
                if (getVideoformat(this)){
                    mFileName += Values.FILE_SUFFIX_VID_AVI;
                }else {
                    mFileName += Values.FILE_SUFFIX_VID;
                }
                break;
            case Values.FILE_TYPE_SND:
                saveDir = new File(StoragePathConfig.getSoundDirPath(this) + File.separator + mDate);
                mFileName += Values.FILE_SUFFIX_AUDIO;
                break;
            case Values.FILE_TYPE_IMG:
                saveDir = new File(StoragePathConfig.getPicDirPath(this) + File.separator + mDate);
                String lolaLive_Pic = Settings.System.getString(getContentResolver(),"LolaLive_Pic");
                if (!TextUtils.isEmpty(lolaLive_Pic)){
                    mFileName = mFileName.replace(mFileName,lolaLive_Pic+"_"+mFileName);
                }

                mFileName = mFileName.replace("_start","");
                mFileName += Values.FILE_SUFFIX_PIC;
                break;
        }
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            saveDir.mkdirs();
        }
//        Values.LOG_I(TAG, "getFileName = " + mFileName);
        File file = new File(saveDir, mFileName);
        return file.getAbsolutePath();
    }

    /**
     * 录像时不能切换模式,不能设置录像参数.
     * {@link #enableCaptureOrRecordSetting()}
     */
    private void disableCaptureOrRecordSetting() {
        mBtnSwitcher.setClickable(false);
        mBtnMode.setClickable(false);
        mBtnResolution.setClickable(false);
        mBtnRecordAuto.setClickable(false);
        mBtnCameraSwitcher.setClickable(false);
        if (!ZZXConfig.is4GA_TEST){
            mBtnUsbCameraSwitcher.setClickable(false);
        }
    }

    private void releaseRecorder() {
        try {
            if (mManualRecording) {
                stopRecordAsMain();
            }
            mIsRecording = false;
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private boolean one_up = false;//******忘记此判断意义
    private void takePicture() {
        if (!ZZXConfig.is4GA_CDHN && !ZZXConfig.isP5C_CDHN && !ZZXConfig.isP5X_CDHN){
            notifyLogcatPhotos();
        }
        LogUtil.i(TAG,"mCamera"+mCamera+"  isExternalStorageEnabled"+isExternalStorageEnabled());
        if (mCamera == null || !isExternalStorageEnabled()) {
            return;
        }
        if (!mIsCapture && !mIsRecording) {
            toggleToPictureMode();
        }
        SharedPreferences sp = RemotePreferences.getRemotePreferences(this);
        int mode = sp.getInt(getKey(R.string.key_mode_picture), Values.PIC_MODE_SIGNAL);
        String modeValue = "-1";
        int[] ratios = getRatio(R.string.key_ratio_picture);
        mParameters.setPictureSize(ratios[0], ratios[1]);
    /*    if (mFrontCamera) {
            mode = Values.PIC_MODE_SIGNAL;
            mParameters.setPictureSize(720, 576);
        }*/
        clearQuickSerialMode();
        if (mIsRecording && mode == Values.PIC_MODE_QUICK) {
            mode = Values.PIC_MODE_SIGNAL;
        }
        mContinuousCaptureMode = mode == Values.PIC_MODE_QUICK;
        if (mode != Values.PIC_MODE_SIGNAL && !mIsRecording) {
            disableCaptureOrRecordSetting();
        }
        // 设置矫正相机角度
        if (ZZXConfig.IsRotate) {
            fixDirection(mCurrentOrientation, mCamera);
        }
        switch (mode) {
            case Values.PIC_MODE_SIGNAL:
                if (mCaptureEnabled) {
                    if (!mIsRecording)
                    disableRecordAndCaptureButton();
                    one_up =true;
                    EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.TAKE_PICTURE);
                }
                return;
            case Values.PIC_MODE_QUICK:
                String mtkMode = mParameters.get(MTK_CAM_MODE);
                if (!"1".equals(mtkMode)) {
                    mParameters.set(MTK_CAM_MODE, 1);
                    mCamera.setParameters(mParameters);
                    mCamera.stopPreview();
                    mCamera.startPreview();
                }
                disableRecordAndCaptureButton();
                one_up =false;
                modeValue = sp.getString(getKey(R.string.key_mode_quick_series), Values.PIC_MODE_QUICK_DEFAULT_VALUE);
                Message msg = mHandler.obtainMessage();
                msg.what = Values.PIC_MODE_QUICK;
                msg.arg1 = Integer.parseInt(modeValue);
                mHandler.sendMessage(msg);
                break;
            case Values.PIC_MODE_INTERVAL:  //间隔拍照
                startIntervalOrTimerCaptureMode();
                modeValue = sp.getString(getKey(R.string.key_mode_interval), Values.PIC_MODE_INTERVAL_DEFAULT_VALUE);
                Message msg1 = mHandler.obtainMessage();
                msg1.what = Values.PIC_MODE_INTERVAL;
                msg1.arg1 = Integer.parseInt(modeValue);
                mHandler.sendMessage(msg1);
                break;
            case Values.PIC_MODE_TIMER:    //定时拍照
                startIntervalOrTimerCaptureMode();
                modeValue = sp.getString(getKey(R.string.key_mode_timer), Values.PIC_MODE_TIMER_DEFAULT_VALUE);
                MAX_COUNT = Integer.parseInt(modeValue);
                Message message = Message.obtain();
                message.what = Values.COUNT_DOWN;
                message.arg1 = Integer.parseInt(modeValue);
//                mHandler.sendMessageDelayed(message,1000);
                mHandler.sendEmptyMessageDelayed(Values.TAKE_PICTURE, Integer.parseInt(modeValue) * 1000);
                setCountDown(Integer.parseInt(modeValue) * 1000);
                timer.start();
                break;
        }
        Values.LOG_I(TAG, "mode = " + mode + "; modeValue = " + modeValue);
    }
    CountDownTimer timer;
    int MAX_COUNT = 0;
    /**
     * 倒计时
     * @param totalTime
     */
    private void setCountDown(int totalTime){

        timer = new CountDownTimer(totalTime+300,1000) {
            @Override
            public void onTick(long l) {
                //由于l获取值不是整数，所以上面+50毫秒
                String value = String.valueOf(Math.round (l / 1000));
                Log.e(TAG, l+"onTick: "+ value);
                mTvCountTime.setVisibility(View.VISIBLE);
                mTvCountTime.setText(value);

            }

            @Override
            public void onFinish() {
                mTvCountTime.setVisibility(View.GONE);
            }
        };
    }

    private void notifyLogcatPhotos() {
        Intent picintent = new Intent();
        picintent.setAction(Values.LOGCAT_PHOTOS);
        sendBroadcast(picintent);
    }
    private void notifyLogcatBattery(){
        Intent batteryintent = new Intent();
        batteryintent.setAction(Values.LOGCAT_BATTERY_LOW);
        sendBroadcast(batteryintent);
    }
    private void disableRecordAndCaptureButton() {
        mCaptureEnabled = false;
        if (mIsCapture) {
            mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_capture_quick);
        } else {
            mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_nor);
        }
        mBtnCaptureRecord.setClickable(false);
    }

    private void enableRecordAndCaptureButton() {
        mCaptureEnabled = true;
        if (mIsCapture) {
            mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_capture);
            mBtnCaptureRecord.setId(R.drawable.btn_capture);
        } else {
            mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_start);
            mBtnCaptureRecord.setId(R.drawable.btn_record_start);
        }
        mBtnCaptureRecord.setClickable(true);
    }

    /**
     * @see #clearIntervalOrTimerCaptureMode() 开始间隔连拍或者定时拍照两种可中途取消模式.
     */
    private void startIntervalOrTimerCaptureMode() {
        if (!mIsRecording) {
            toggleToPictureMode();
            mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_stop);
            mBtnCaptureRecord.setId(R.id.btn_capture_mode_interval_timer);
        }
        mIntervalOrTimerMode = true;
    }

    /**
     * @see #startIntervalOrTimerCaptureMode() 取消间隔连拍或者定时拍照模式.
     */
    private void clearIntervalOrTimerCaptureMode() {
        mIntervalOrTimerMode = false;
        if (!mIsRecording) {
            enableCaptureOrRecordSetting();
            mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_capture);
            mBtnCaptureRecord.setId(R.drawable.btn_capture);
            mBtnCaptureRecord.setClickable(true);
            mTvCountTime.setVisibility(View.GONE);
            if (timer!=null){
                timer.cancel();
                timer = null;
            }

        }
        if (mHandler.hasMessages(Values.PIC_MODE_INTERVAL)) {
            mHandler.removeMessages(Values.PIC_MODE_INTERVAL);
        }
        if (mHandler.hasMessages(Values.TAKE_PICTURE))
            mHandler.removeMessages(Values.TAKE_PICTURE);
    }

    /**
     * 高速连拍和其它模式使用的是不同的{@link android.hardware.Camera.PictureCallback}.
     *
     * @see #mContinuousJpegPictureCallback
     */
    private PictureCallback getJpegPictureCallback() {
        clearIDLECount();
        if (mContinuousCaptureMode) {
            if (mShutterSoundEnabled) {
                if (!ZZXConfig.isP5XJNKYD){
                    mStreamID = 0;
                    mStreamID = mBurstSound.play(mFastPicSoundID, 1.0f, 1.0f, 1, -1, 1.0f);
                }
            }
            return mContinuousJpegPictureCallback;
        } else {
            return mJpegPictureCallback;
        }
    }

    private void insertDatabases() {
        Log.e(TAG, "insertDatabases: "+isContinuousPic );
        try {
            FileInfo info = new FileInfo();
            info.mFileType = Values.FILE_TYPE_IMG;
            info.mFileName = mFileName;
            info.mCreateDate = mDate;
            info.mStartTime = Long.parseLong(mPicTime);
            DatabaseUtils.insertHistory(CameraService.this, info);
            IsPicing = false;
            isKeyPic = false;
            isContinuousPic = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeData(byte[] data) {
        FileOutputStream outSteam = null;
        mPicPath = null;
        try {
            mPicPath = getFilePath(Values.FILE_TYPE_IMG);
            outSteam = new FileOutputStream(mPicPath);
            outSteam.write(data);
            outSteam.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outSteam != null) {
                try {
                    outSteam.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 切换拍照-录像模式
     */
    private void toggleMode() {
        if (!mIsCapture) {
            toggleToPictureMode();
        } else {
            toggleToRecordMode();
        }
    }

    /**
     * 切换拍照-录像模式
     */
    private void initMode() {
        if (Integer.parseInt(RemotePreferences.getRemotePreferences(this).getString(getKey(R.string.key_record_pre), "0")) != 0) {
            toggleToRecordMode();
        }
    }

    /**
     * 切换声音开启状态,此方法需要API 17(4.2)
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void toggleShutterSound() {
        try {
            if (!mShutterSoundEnabled) {
                mShutterSoundEnabled = true;
                mBtnShutterSound.setBackgroundResource(R.drawable.shutter_sound_open);
                mSPEditor.putBoolean(Values.KEY_CAPTURE_SOUND, true).apply();
            } else {
                mShutterSoundEnabled = false;
                mBtnShutterSound.setBackgroundResource(R.drawable.shutter_sound_close);
                mSPEditor.putBoolean(Values.KEY_CAPTURE_SOUND, false).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换闪光灯状态
     */
    private void toggleFlashLight() {
        try {
            if (mFlashModes == null) {
                mFlashModes = mParameters.getSupportedFlashModes();
                for (String mode : mFlashModes) {
                    if (mode.equals(Camera.Parameters.FLASH_MODE_ON)) {
                        mFlashModeOnAvailable = true;
                    } else if (mode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                        mFlashModeOffAvailable = true;
                    }
                }
            }
            if (mIsCapture) {
                if (mCaptureFlashModeOn) {
                    if (mFlashModeOffAvailable) {
                        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCaptureFlashModeOn = false;
//                        mBtnFlashLight.setBackgroundResource(R.drawable.shutter_flash_close);
                    } else {
                        return;
                    }
                } else if (mFlashModeOnAvailable) {
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mCaptureFlashModeOn = true;
//                    mBtnFlashLight.setBackgroundResource(R.drawable.shutter_flash_open);
                } else {
                    return;
                }
            } else {
                if (mRecordFlashModeOn) {
                    if (mFlashModeOffAvailable) {
                        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mRecordFlashModeOn = false;
//                        mBtnFlashLight.setBackgroundResource(R.drawable.shutter_flash_close);
                    } else {
                        return;
                    }
                } else if (mFlashModeOnAvailable) {
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mRecordFlashModeOn = true;
//                    mBtnFlashLight.setBackgroundResource(R.drawable.shutter_flash_open);
                } else {
                    return;
                }
            }
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换到录像模式
     */
    private void toggleToRecordMode() {
        mIsCapture = false;
        mBtnMode.setBackgroundResource(R.drawable.dsj_h9_mc_nor);
        mBtnMode.setId(R.id.btn_record_setting);
        refreshRatioBtn();
        refreshAutoRecordButton();
        mBtnSwitcher.setBackgroundResource(R.drawable.mode_btn_picture);
        mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_start);
        mBtnCaptureRecord.setId(R.drawable.btn_record_start);
    }

    /**
     * 刷新自动录像按键.
     */
    private void refreshAutoRecordButton() {
        boolean auto = RemotePreferences.getRemotePreferences(this).getBoolean(getKey(R.string.key_record_auto), false);
        if (auto) {
            mBtnRecordAuto.setBackgroundResource(R.drawable.auto_record_on);
        } else {
            mBtnRecordAuto.setBackgroundResource(R.drawable.auto_record_off);
        }
    }

    /**
     * 切换到拍照模式
     */
    private void toggleToPictureMode() {
        if (mIsCapture)
            return;
        mIsCapture = true;
        mBtnMode.setBackgroundResource(R.drawable.mode);
        mBtnMode.setId(R.id.btn_pic_mode);
        refreshRatioBtn();
        mBtnSwitcher.setBackgroundResource(R.drawable.mode_btn_record);
        mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_capture);
        mBtnCaptureRecord.setId(R.drawable.btn_capture);
    }

    /**
     * 刷新闪光灯按键.
     */
    private void refreshFlashButton() {
        if (mIsCapture) {
            if (mCaptureFlashModeOn)
                refreshFlashMode(true, true);
            else
                refreshFlashMode(false, true);
        } else if (mRecordFlashModeOn) {
            refreshFlashMode(true, false);
        } else {
            refreshFlashMode(false, false);
        }
    }

    private void refreshFlashMode(boolean open, boolean isCapture) {
        if (!open) {
            if (mFlashModeOffAvailable) {
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                mBtnFlashLight.setBackgroundResource(R.drawable.shutter_flash_close);
            }
        } else if (mFlashModeOnAvailable) {
            if (!isCapture) {
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
//            mBtnFlashLight.setBackgroundResource(R.drawable.shutter_flash_open);
        }
        mCamera.setParameters(mParameters);
    }

    /**
     * 最大化窗口,显示预览
     *
     * @see #minWindow()
     */
    private void maxWindow() {
        checkCameraNeedRestart();
        clearIDLECount();
        mIsWindowMax = true;
        if (mWindowParams.width == LayoutParams.MATCH_PARENT && mWindowParams.height == LayoutParams.MATCH_PARENT) {
            return;
        }
        mWindowParams.width = LayoutParams.MATCH_PARENT;
        mWindowParams.height = LayoutParams.MATCH_PARENT;
        mWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if (mWinManager == null) {
            mWinManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        mWindowParams.flags &= ~LayoutParams.FLAG_NOT_FOCUSABLE;
        mWinManager.updateViewLayout(mRootContainer, mWindowParams);
        refreshViewState();
        checkPeripheralEnable();
        if (ZZXConfig.isSongJian) {
            if (isAutoInfraredOpened()) {
                cameraZoomIncrease();
            }
        }
    }

    private void refreshViewState() {
        refreshRatioBtn();
        refreshAutoRecordButton();
        refreshShutterSound();
    }

    private void refreshShutterSound() {
        SharedPreferences sp = RemotePreferences.getRemotePreferences(this);
        if (sp.getBoolean(Values.KEY_CAPTURE_SOUND, true)) {
            mShutterSoundEnabled = true;
            mBtnShutterSound.setBackgroundResource(R.drawable.shutter_sound_open);
        } else {
            mShutterSoundEnabled = false;
            mBtnShutterSound.setBackgroundResource(R.drawable.shutter_sound_close);
        }
    }

    /**
     * 將窗口最小化到右上角
     *
     * @see #maxWindow()
     */
    private void minWindow() {
        mIsWindowMax = false;
        mWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        mWindowParams.height = 1;
        mWindowParams.width = 1;
        mWindowParams.gravity = Gravity.END | Gravity.TOP;
        if (mWinManager == null) {
            mWinManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        mWindowParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        mWinManager.updateViewLayout(mRootContainer, mWindowParams);
        if (ZZXConfig.isSongJian) {
            if (isAutoInfraredOpened()) {
                cameraZoomDecrease();
            }
        }
//        if (!isVideoReecord){
//            mHandler.sendEmptyMessageDelayed(Values.RELEASE_CAMERA , 10*1000);
//        }
    }

    private void quitFullScreenMode() {
        mModeFullScreen = false;
        mLayoutPerform.setVisibility(View.VISIBLE);
        mLayoutParams.setVisibility(View.VISIBLE);
        mBtnCaptureRecord.setVisibility(View.VISIBLE);
    }

    private void enterFullScreenMode() {
        mModeFullScreen = true;
        mBtnCaptureRecord.setVisibility(View.GONE);
        mLayoutPerform.setVisibility(View.GONE);
        mLayoutParams.setVisibility(View.GONE);
    }

    /**
     * 初始化布局控件
     */
    private void findChildView(View rootView) {
        mIvMode = (ImageView) rootView.findViewById(R.id.iv_mode);
        mIvZoom = (ImageView) rootView.findViewById(R.id.iv_zoom_level);
        mIvImportantFile = (ImageView) rootView.findViewById(R.id.iv_important_file);
        mBtnMode = (ImageButton) rootView.findViewById(R.id.btn_pic_mode);
        mBtnThumb = (ImageButton) rootView.findViewById(R.id.btn_thumb);
        mBtnRecordAuto = (ImageButton) rootView.findViewById(R.id.btn_record_auto);
//        mBtnFlashLight  = (ImageButton) rootView.findViewById(R.id.btn_flash_light);
        mBtnResolution = (ImageButton) rootView.findViewById(R.id.btn_pic_ratio);
        mBtnCaptureRecord = (ImageButton) rootView.findViewById(R.id.btn_capture);
        mBtnShutterSound = (ImageButton) rootView.findViewById(R.id.btn_pic_shutter);
        mBtnZoomDecrease = (ImageButton) rootView.findViewById(R.id.btn_zoom_decrease);
        mBtnZoomIncrease = (ImageButton) rootView.findViewById(btn_zoom_increase);
        mSurfaceView = (SurfaceView) rootView.findViewById(R.id.surface_view);
        mLayoutParams = (ViewGroup) rootView.findViewById(R.id.layout_camera_params_set);
        mLayoutPerform = (ViewGroup) rootView.findViewById(R.id.layout_camera_perform);
        mBtnSwitcher = (ImageButton) rootView.findViewById(R.id.btn_switch_mode);
        mTvStorageNotEnough = (TextView) rootView.findViewById(R.id.tv_storage_not_enough);
        mTvRecordTime = (TextView) rootView.findViewById(R.id.tv_record_time);
        mTvCountTime = rootView.findViewById(R.id.tv_count_time);
        mIvRecordState = (ImageView) rootView.findViewById(R.id.iv_record_state);
        mBtnCameraSwitcher = (ImageView) rootView.findViewById(R.id.btn_switch_camera);
        mFlashEasyicon = rootView.findViewById(R.id.flash_easyicon);
        mFlashEasyicon.setOnClickListener(this);
        mBtnIrcut = (ImageView) rootView.findViewById(R.id.btn_ircut);
        mBtnIrcut.setOnClickListener(this);
        mBtnFlash = (ImageView) rootView.findViewById(R.id.btn_flash);
        mBtnFlash.setOnClickListener(this);
        mBtnLaser = (ImageView) rootView.findViewById(R.id.btn_laser);
        mBtnLaser.setOnClickListener(this);
        mBtnMenu = (ImageView) rootView.findViewById(R.id.btn_menu);
        mBtnMenu.setOnClickListener(this);
        mBtnCameraSwitcher.setOnClickListener(this);
        mBtnUsbCameraSwitcher = (ImageView) rootView.findViewById(R.id.btn_switch_usbcamera);
        mBtnUsbCameraSwitcher.setOnClickListener(this);
        checkHasTwoCamera();
        checkPeripheralEnable();
        mBtnLaserSwitcher = (ImageView) rootView.findViewById(R.id.btn_switch_laser);
        view_focus = rootView.findViewById(R.id.view_focus);
        initFocusViewAnim();
        mRlSufaceView = (RelativeLayout) rootView.findViewById(R.id.rl_surface);
        if (mBtnLaserSwitcher != null) {
            mBtnLaserSwitcher.setOnClickListener(this);
            refreshLaser();
        }

        /**这里若不返回true,则会把两边的点击事件传递给下层的SurfaceView,即响应全屏与退出全屏事件
         * */
        mLayoutParams.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mLayoutPerform.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        refreshAutoRecordButton();
        initMode();
        if (ZZXConfig.isP5XJNKYD){
            mBtnMode.setVisibility(View.GONE);
        }
        if (ZZXConfig.is4GA_CDHN || ZZXConfig.isP5C_CDHN || ZZXConfig.isP5X_CDHN){
            mBtnThumb.setVisibility(View.GONE);
        }
        if (ZZXConfig.isP5C_HLN){
            mFlashEasyicon.setVisibility(View.VISIBLE);
        }
    }

    private void checkPeripheralEnable() {
        if (isFlashOpened()) {
            mBtnFlash.setBackgroundResource(R.drawable.flash_on);
        } else {
            mBtnFlash.setBackgroundResource(R.drawable.flash_off);
        }

        if (isIrCutOpened()) {
            mBtnIrcut.setBackgroundResource(R.drawable.ircut_on);
        } else {
            mBtnIrcut.setBackgroundResource(R.drawable.ircut_off);
        }

        if (isLaserOpened()) {
            mBtnLaser.setBackgroundResource(R.drawable.laser_on);
        } else {
            mBtnLaser.setBackgroundResource(R.drawable.laser_off);
        }
    }

    private void refreshLaser() {
        if ("1".equals(ZZXMiscUtils.read(ZZXMiscUtils.LASER_PATH))) {
            mBtnLaserSwitcher.setBackgroundResource(R.drawable.laser_on);
        } else {
            mBtnLaserSwitcher.setBackgroundResource(R.drawable.laser_off);
        }
    }

    private void checkHasTwoCamera() {
        if (Camera.getNumberOfCameras() <= 1) {
            mBtnCameraSwitcher.setVisibility(View.GONE);
        }
    }

    private void hideModeRatio() {
        mBtnResolution.setVisibility(View.GONE);
        mBtnMode.setVisibility(View.GONE);
    }

    private void showModeRatio() {
        mBtnResolution.setVisibility(View.VISIBLE);
        mBtnMode.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化控件监听事件
     */
    private void initViewListener() {
        mSurfaceView.getHolder().addCallback(this);
        mBtnMode.setOnClickListener(this);
        mBtnResolution.setOnClickListener(this);
        mBtnShutterSound.setOnClickListener(this);
        mBtnZoomDecrease.setOnClickListener(this);
        mBtnZoomIncrease.setOnClickListener(this);
        mBtnCaptureRecord.setOnClickListener(this);
        mBtnThumb.setOnClickListener(this);
        mBtnSwitcher.setOnClickListener(this);
        mBtnRecordAuto.setOnClickListener(this);
        mRlSufaceView.setOnTouchListener(onTouchListener);
        refreshRatioBtn();
    }

    /**
     * 加载Preferences布局
     */
    public void addPreferencesFromResource(int preferencesResId) {
        PreferenceScreen resultScreen;
        try {
            if (mPreferenceManagerCls == null) {
                mPreferenceManagerCls = PreferenceManager.class;
            }
            getPreferenceManager();
            PreferenceScreen screen = getPreferenceScreen();
            resultScreen = inflatePreferenceScreen(this, preferencesResId, screen);
            Boolean success = setPreferences(resultScreen);
            if (success && resultScreen != null) {
                postBindPreferences();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean setPreferences(PreferenceScreen screen) {
        Boolean success = false;
        try {
            Method setScreenMethod = mPreferenceManagerCls.getDeclaredMethod("setPreferences", PreferenceScreen.class);
            setScreenMethod.setAccessible(true);
            success = (Boolean) setScreenMethod.invoke(mPreferenceManager, screen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public PreferenceScreen inflatePreferenceScreen(Context context, int resId, PreferenceScreen screen) {
        PreferenceScreen resultScreen = null;
        try {
            Method inflateFromRes = mPreferenceManagerCls.getMethod("inflateFromResource", Context.class, Integer.TYPE, PreferenceScreen.class);
            resultScreen = (PreferenceScreen) inflateFromRes.invoke(mPreferenceManager, context, resId, screen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultScreen;
    }

    public PreferenceManager getPreferenceManager() {
        if (mPreferenceManager == null) {
            try {
                if (mPreferenceManagerCls == null) {
                    mPreferenceManagerCls = PreferenceManager.class;
                }
                Constructor cons = mPreferenceManagerCls.getDeclaredConstructor(Context.class);
                cons.setAccessible(true);
                mPreferenceManager = (PreferenceManager) cons.newInstance(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mPreferenceManager;
    }

    public PreferenceScreen getPreferenceScreen() {
        PreferenceScreen preferenceScreen = null;
        try {
            if (mGetScreenMethod == null) {
                mGetScreenMethod = mPreferenceManagerCls.getDeclaredMethod("getPreferenceScreen", (Class[]) null);
                mGetScreenMethod.setAccessible(true);
            }
            preferenceScreen = (PreferenceScreen) mGetScreenMethod.invoke(mPreferenceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preferenceScreen;
    }

    private void postBindPreferences() {
        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.MSG_BIND_PREFERENCES);
    }

    /**
     * 加载解析完Preferences后绑定到mModeListView上.
     */
    private void bindPreferences() {
        try {
            Method getScreenMethod = mPreferenceManagerCls.getDeclaredMethod("getPreferenceScreen", (Class[]) null);
            getScreenMethod.setAccessible(true);
            PreferenceScreen preferenceScreen = (PreferenceScreen) getScreenMethod.invoke(mPreferenceManager);
            if (preferenceScreen != null) {
                preferenceScreen.bind(mModeListView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @see #initCamera(int)
     */
    private void releaseCamera() {
        mCameraReady = false;
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewDisplay(null);
                if (mNeedReportVideo ){
                    mCamera.setPreviewCallback(null);
                }
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    mCamera.release();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        mCamera = null;
        mParameters = null;
        clearIDLECount();
        releaseWakeLock();
    }

    private boolean initCamera(int index) {

        acquireWakeLock();
        try {
            mCamera = open(index);
            mParameters = mCamera.getParameters();
            mCameraId = index;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mParameters == null) {
            releaseCamera();
            try {
                mCamera = open(index);
                mParameters = mCamera.getParameters();
                mCameraId = index;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (mParameters == null)
                return false;
        }
        mCameraIDLEMode = false;

        /**YUV420SemiPlanar - NV21 高通
         * YUV420Planar - YV12 MTK
         * */

//        mParameters.unflatten(Values.CAMERA);
//        if (index == 0) {
//               mParameters.setPreviewSize(640, 480);
//        mParameters.setPreviewSize(320, 240);
        if (ZZXConfig.isP5XJNKYD){
            mParameters.setPreviewSize(640, 480);
        }else if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY ){
            mParameters.setPreviewSize(320, 240);
        }else {
            mParameters.setPreviewSize(1280, 720);
        }
//        }
        /* *else {
            mParameters.setPreviewSize(720, 576);
        }*/
//        mParameters.setPreviewFpsRange(30000, 50000);
        mParameters.setPreviewFormat(ImageFormat.NV21);
        mParameters.setJpegQuality(90);

        int orientation = getCameraOrientation();
        Log.e(TAG, "orientation = " + orientation);
        if (!ZZXConfig.is4G5  && !ZZXConfig.isP80){
            mCamera.setDisplayOrientation(orientation);
        }
        if (mCameraZoomMax == 0)
            mCameraZoomMax = mParameters.getMaxZoom();
        if (mZoomLevel != 0) {
            mParameters.setZoom(mZoomLevel);
        }
        mCamera.setErrorCallback(new Camera.ErrorCallback() {
            @Override
            public void onError(int error, Camera camera) {
                stopSelf();
            }
        });
        mCamera.setParameters(mParameters);
        mCameraReady = true;
        initCameraFoucsView(index);
        if (ZZXConfig.is4GC || ZZXConfig.is4GA|| ZZXConfig.is4G5 || ZZXConfig.isP80){
            view_focus.setVisibility(View.GONE);
        }
        if (Camera.getNumberOfCameras()==1){
            view_focus.setVisibility(View.GONE);
        }
        return true;
    }
    private void initCameraFoucsView(int index) { //只有在id为0的摄像头时候，才出现聚焦的Ui
        if (index == 0) {
            view_focus.setVisibility(View.GONE);
        } else {
            view_focus.setVisibility(View.VISIBLE);
        }
//        if (Camera.getNumberOfCameras() == 3){
//            if (index == 0){
//                view_focus.setVisibility(View.VISIBLE);
//            }else {
//                view_focus.setVisibility(View.GONE);
//            }
//        }else if(Camera.getNumberOfCameras() == 2) {
//            if (index == 0){
//                view_focus.setVisibility(View.VISIBLE);
//            }else {
//                view_focus.setVisibility(View.GONE);
//            }
//        } else {
//            if (index == 0) {
//                view_focus.setVisibility(View.VISIBLE);
//            } else {
//                view_focus.setVisibility(View.GONE);
//            }
//        }

    }

    //获取摄像头角度
    private int getCameraOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        return info.orientation;
    }

    private void checkCameraNeedRestart() {
        if (mCamera == null) {
            restartCamera();
        }
    }

    /**
     * @see #releaseCamera()
     */
    private void restartCamera() {
        Log.e(TAG, "restartCamera: "+Camera.getNumberOfCameras() );
        if (Camera.getNumberOfCameras() == 3) {
            if (!mFrontCamera) {
                if (!mBehindCamera) {
                    initCamera(Camera.getNumberOfCameras() - 1);
                    isSubject = true;
//                    mCameraName.setText(R.string.pic_camera);
                } else {
                    isSubject = false;
                    initCamera(0);
//                    mCameraName.setText(R.string.night_camera);
                }
            } else {
                isSubject = false;
                initCamera(1);
//                mCameraName.setText(R.string.front_camera);
            }
        }else {
            Log.e(TAG, "restartCamera: mFrontCamera="+mFrontCamera );
            if (!mFrontCamera) {
//                initCamera(Camera.getNumberOfCameras() - 1);
                isSubject = true;
                initCamera(0);
            } else {
                initCamera(Camera.getNumberOfCameras() - 1);
                isSubject = false;
//                initCamera(0);
            }
        }
        if (!mStorageNotEnough) {
            enableRecordAndCaptureButton();
            enableCaptureOrRecordSetting();
        }
        enableZoomButton();
        restartPreview();
    }


    /**
     * 重载方法 重启摄像头
     *
     * @param cameraId  摄像头id
     * @param autoVideo 是否自动开启录像
     */
    private void restartRecord(int cameraId, int autoVideo, Context context) {
        mFrontCamera = (cameraId == 1);
        if (autoVideo == 1) {
            Intent intent = new Intent();
            intent.setAction(Values.ACTION_UEVENT_RECORD);
            context.sendBroadcast(intent);
        }
    }

    /**
     * @see #initRealTimeVideo()
     * @see #releaseRealTimeVideo(boolean)
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
            if (mLolaLiveSDKUtil!=null)
                mLolaLiveSDKUtil.sendData(data);
        }
        if (mCamera == null || !mNeedReportVideo || mDeque == null || mDeque.size() >= REPORT_DEQUE_SIZE || mVideoEncoder == null || data == null) {
            return;
        }
        if (!mDeque.offer(data)) {
            Values.LOG_W(TAG, "video deque offer false ");
        }
//        mDeque.push(data);
        Values.LOG_W(TAG, "onPreviewFrame()");
        if (mReportWithRecord && mIsRecording) {
            try {
                Thread.sleep(130);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            initRecordFromRecordMode();
            isFirst = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Values.LOG_I(TAG, "surfaceChanged().width*height = " + width + " * " + height);
        /**不能在这里去stopPreview以及startPreview是因为一旦退出或者进入该预览界面都会导致surfaceChanged方法调用,
         * 而一旦startPreview则会导致录像失败.因为两者的输入流不同而且只能存活一个.
         * */
        /*try {
            mCamera.setPreviewDisplay(holder);
            restartPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
    int i=0;
    private void restartPreview() {
        if (mCamera == null) {
            return;
        }
        if (mCameraReady) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i(TAG,"  "+e.toString());
                releaseCamera();
//                stopSelf();
                return;
            }
        }

        try{
            if(ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
                mCamera.setPreviewCallback(this);
            }
            mCamera.startPreview();
            mBtnCaptureRecord.setClickable(true);
        }catch (Exception e){
            e.printStackTrace();
            i++;
            if (i>5){
                i = 0;
                return;
            }
            restartPreview();

        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
        if (mCamera != null) {
            try {
                releaseCamera();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void OpenIrCut() {
        if (isIrCutOpen) {
            turnOnIrCutLight();
            isIrCutOpen = false;
        }
    }

    private void releaseIrCut() {
        turnOffIrCutLight();
        if (isIrCutOpened()) {
            isIrCutOpen = true;
        } else {
            isIrCutOpen = false;
        }
    }

    private void takePictureByAction() {
        if (isExternalStorageEnabled()) {
            if (mCamera == null || mCameraIDLEMode) {
                restartCamera();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        takePicture();
                    }
                }, 500);
            } else if (mIntervalOrTimerMode) {
                clearIntervalOrTimerCaptureMode();
            } else if (!mContinuousCaptureMode) {
                takePicture();
            }
        }

    }

    private final void updateMicRgbState(Intent intent) {
        isSoundReecord = !isSoundReecord;
        if (isSoundReecord) {
            controllDevice(1, LIGHT_COLOR_YELLOW);
        } else {
            if (isVideoReecord) {
                controllDevice(1, LIGHT_COLOR_RED);
            } else {
                controllDevice(1, LIGHT_COLOR_GREEN_NO_FLASH);
            }
        }
    }

    public void controllDevice(int state, String rgbstate) {
        String filePath = "/sys/devices/platform/zzx-misc/rgb_led_stats";
        writeToDevice(state, filePath, rgbstate);
    }

    private boolean isIrCutOpened() {
        return isPeripheralsOpened("/sys/devices/platform/zzx-misc/ir_led_stats");
    }

    private boolean isFlashOpened() {
        return isPeripheralsOpened("/sys/devices/platform/zzx-misc/flash_stats");
    }

    private boolean isLedFlash(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/sys/devices/platform/zzx-misc/flash_test"));
            String status = reader.readLine();
            return status != null && !status.equals("0");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    private boolean isLaserOpened() {
        return isPeripheralsOpened("/sys/devices/platform/zzx-misc/lazer_stats");
    }

    private boolean isAutoInfraredOpened() {
        return isPeripheralsOpened("/sys/devices/platform/zzx-misc/camera_light_state");
    }

    private boolean isPeripheralsOpened(String path) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            String status = reader.readLine();
            return status != null && status.equals("1");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void setIrCutEnable(boolean isOpen) {
        synchronized (this) {
            if (isOpen) {
                turnOnIrCutLight();
            } else {
                turnOffIrCutLight();
            }
        }
    }

    private void setlaserEnable(boolean isOpen) {
        synchronized (this) {
            if (isOpen) {
                turnonlaser();
                mBtnLaser.setBackgroundResource(R.drawable.laser_on);
            } else {
                turnofflaser();
                mBtnLaser.setBackgroundResource(R.drawable.laser_off);
            }
        }
    }

    public void setflashEnable(boolean isOpen) {
        synchronized (this) {
            if (isOpen) {
                turnonflash();
                mBtnFlash.setBackgroundResource(R.drawable.flash_on);
            } else {
                turnoffflash();
                mBtnFlash.setBackgroundResource(R.drawable.flash_off);
            }
        }
    }

    /**
     * 描述：关闭Ir_cut
     */
    private void turnOffIrCutLight() {
        writeToDevice(0, "/sys/devices/platform/zzx-misc/ir_led_stats");
        if (ZZXConfig.is4GC || ZZXConfig.is4GA || ZZXConfig.is4G5  || ZZXConfig.isP80){
            writeToDevice(1, "/sys/devices/platform/zzx-misc/ir_cut_stats");
        }else {
            writeToDevice(0, "/sys/devices/platform/zzx-misc/ir_cut_stats");
        }

        mBtnIrcut.setBackgroundResource(R.drawable.ircut_off);
    }

    /**
     * 描述：打开Ir_cut
     */
    private void turnOnIrCutLight() {
        writeToDevice(1, "/sys/devices/platform/zzx-misc/ir_led_stats");
        if (ZZXConfig.is4GC || ZZXConfig.is4GA|| ZZXConfig.is4G5  || ZZXConfig.isP80){
            writeToDevice(0, "/sys/devices/platform/zzx-misc/ir_cut_stats");
        }else {
            writeToDevice(1, "/sys/devices/platform/zzx-misc/ir_cut_stats");
        }

        mBtnIrcut.setBackgroundResource(R.drawable.ircut_on);
    }

    private void turnoffflash() {
        writeToDevice(0, "/sys/devices/platform/zzx-misc/flash_stats");
    }

    private void turnonflash() {
        writeToDevice(1, "/sys/devices/platform/zzx-misc/flash_stats");
    }

    private void turnofflaser() {
        writeToDevice(0, "/sys/devices/platform/zzx-misc/lazer_stats");
    }

    private void turnonlaser() {
        writeToDevice(1, "/sys/devices/platform/zzx-misc/lazer_stats");
    }

    /**
     * @param state    0 关闭，1打开
     * @param filePath ir_cut文件设备路径
     */
    private void writeToDevice(int state, String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            return;
        }
        FileWriter outputStream = null;
        try {
            outputStream = new FileWriter(file);
            if (state == 1) {
                outputStream.write("1");
            } else if (state == 0) {
                outputStream.write("0");
            }else {
                outputStream.write(""+state);
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSOSMsg() {
        Intent intent = new Intent(SOSService.ACTION_SOS_MSG);
        intent.setClass(this, SOSService.class);
        startService(intent);
    }

    private void sendSOS(Context context) {
        try {
            String uid = String.valueOf(System.currentTimeMillis());
            Runnable runnable = new SOSReportRunnable(uid);
            mSOSMap.put(uid, runnable);
            mHandler.post(runnable);
            if (isVibrator(context)) {
                VibratorManager.vibrate(context, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void onEventBackgroundThread(String uid) {
        if (mSOSMap == null || mHandler == null) {
            return;
        }
        try {
            Runnable runnable = mSOSMap.get(uid);
            if (runnable != null) {
                mHandler.removeCallbacks(runnable);
                mSOSMap.remove(uid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notKeepScreenOn() {
        try {
            mWindowParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | LayoutParams.FLAG_NOT_FOCUSABLE
                    | LayoutParams.FLAG_FULLSCREEN;
            mWinManager.updateViewLayout(mRootContainer, mWindowParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void keepScreenOn() {
        try {
            mWindowParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | LayoutParams.FLAG_NOT_FOCUSABLE
                    | LayoutParams.FLAG_FULLSCREEN
                    | LayoutParams.FLAG_KEEP_SCREEN_ON;
            mWinManager.updateViewLayout(mRootContainer, mWindowParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前Camera是否需要进入空闲释放状态
     * 当处于 {@link #mPreRecordMode}预录模式,{@link #mIsRecording}正在录像,{@link #mNeedReportVideo}后台实时监控状态,{@link #mIntervalOrTimerMode}定时拍照或者间隔拍照模式下不能释放.
     */
    private void checkCameraNeedIDLE() {
        if (!mPreRecordMode && !mIsRecording && !mNeedReportVideo && !mIntervalOrTimerMode && !mContinuousCaptureMode && !mUserRecording) {
            cameraIDLEMode();
        } else {
            clearIDLECount();
        }
    }

    private void cameraIDLEMode() {
        mCameraIDLEMode = true;
        /*disableCaptureOrRecordSetting();
        disableRecordAndCaptureButton();
        disableZoomButton();*/
        if (!ZZXConfig.is4GC_SDJW && !ZZXConfig.is4GC_SY)
            mHandler.sendEmptyMessageDelayed(Values.RELEASE_CAMERA,2*60*1000);
//        releaseCamera();
    }

    /**
     * @see #enableZoomButton() ()
     */
    private void disableZoomButton() {
        mBtnZoomIncrease.setClickable(false);
        mBtnZoomDecrease.setClickable(false);
    }

    private void enableZoomButton() {
        mBtnZoomIncrease.setClickable(true);
        mBtnZoomDecrease.setClickable(true);
    }

    private void startRecordService(boolean longPress) {
        Intent intent = new Intent();
        intent.setClassName("com.zzx.soundrecord",
                "com.zzx.soundrecord.service.RecordService");
        intent.putExtra(Values.U_EVENT_LONG_PRESSED_STATE, longPress);
        startService(intent);
    }
    private ProximitySensor mProximitySensor;
    private void initProximitySensor(){
        if (mProximitySensor == null) {
            mProximitySensor = new ProximitySensor(this);
        }
    }
    private SensorController mSensorController;
    private void initSensorController() {
        if (mSensorController == null) {
            mSensorController = SensorController.getInstance(this);
        }
        mSensorController.onCreate();
        mSensorController.setCameraFocusListener(new SensorController.CameraFocusListener() {
            @Override
            public void onFocus() {
                synchronized (mFocusLock) {
                    if (mIsTouchFocus) {
                        return;
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //如果相机服务为大窗口，并且没有开始对焦
//                        if (mIsWindowMax && !mSensorController.isFocusLocked() && mCameraId == 0) {
                        if (mIsWindowMax && !mSensorController.isFocusLocked()) {
                            Log.d(TAG, "run: ------------自动对焦开始-------------");
                            view_focus.setBackground(getResources().getDrawable(R.drawable.ic_focus_focusing));
                            //自动对焦时，将对焦图片放到相机预览的正中间
                            view_focus.setX(mRlSufaceView.getWidth() / 2 - view_focus.getWidth() / 2);
                            view_focus.setY(mRlSufaceView.getHeight() / 2 - view_focus.getHeight() / 2);
                            view_focus.startAnimation(mFocusViewAnim);
                            //在对焦时加锁，避免正在对焦时接口再次调用 onFocus方法
                            mSensorController.lockFocus();
                            focusOnTouch(null);
                        }
                    }
                });

            }
        });
    }

    private void initFocusViewAnim() {
        if (mFocusViewAnim == null) {
            mFocusViewAnim = AnimationUtils.loadAnimation(this, R.anim.focusview_show);
        }
    }

    private Point mCenterPoint;

    /**
     * 当点击 mSurfaceViewContainer的正中间位置时的坐标(相对于屏幕左上角)
     * 目的:使自动对焦的区域是相机预览区域的正中间
     */
    private Point initViewFocusCenter() {
        if (mCenterPoint != null) {
            return mCenterPoint;
        } else {
            mCenterPoint = new Point();
        }

        int location[] = new int[2];
        mRlSufaceView.getLocationOnScreen(location);
        mCenterPoint.x = location[0] + mRlSufaceView.getWidth() / 2;
        mCenterPoint.y = location[1] + mRlSufaceView.getHeight() / 2;

        Log.d(TAG, "initViewFocusCenter: -----------start-----------");
        Log.d(TAG, "initViewFocusCenter: mSurfaceViewContainer.location[0]:" + location[0] + " location[1]:" + location[1]);
        Log.d(TAG, "initViewFocusCenter: centerRawX:" + mCenterPoint.x + " centerRawY:" + mCenterPoint.y);
        Log.d(TAG, "initViewFocusCenter: -----------end-----------");

        return mCenterPoint;
    }
    /**
     * 设置焦点和测光区域
     *
     * @param event
     */
    public void focusOnTouch(MotionEvent event) {

        int[] location = new int[2];
        mRlSufaceView.getLocationOnScreen(location);

        Rect focusRect;
        Rect meteringRect;
        if (event != null){
            focusRect = calculateTapArea(view_focus.getWidth(),
                    view_focus.getHeight(),
                    1f,
                    event.getRawX(),
                    event.getRawY(),
                    location[0],
                    location[0] + mRlSufaceView.getWidth(),
                    location[1], location[1] + mRlSufaceView.getHeight());
            meteringRect = calculateTapArea(view_focus.getWidth(),
                    view_focus.getHeight(),
                    1.5f,
                    event.getRawX(),
                    event.getRawY(),
                    location[0],
                    location[0] + mRlSufaceView.getWidth(),
                    location[1], location[1] + mRlSufaceView.getHeight());
        }else {
            int centerRawX = initViewFocusCenter().x;
            int centerRawY = initViewFocusCenter().y;
            focusRect = calculateTapArea(view_focus.getWidth(),
                    view_focus.getHeight(),
                    1f,
                    centerRawX,//event.getRawX(),
                    centerRawY,//event.getRawY(),
                    location[0],
                    location[0] + mRlSufaceView.getWidth(),
                    location[1], location[1] + mRlSufaceView.getHeight());
            meteringRect = calculateTapArea(view_focus.getWidth(),
                    view_focus.getHeight(),
                    1.5f,
                    centerRawX,//event.getRawX(),
                    centerRawY,//event.getRawY(),
                    location[0],
                    location[0] + mRlSufaceView.getWidth(),
                    location[1], location[1] + mRlSufaceView.getHeight());
        }

        if (mCamera == null)
            return;
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        // System.out.println("CustomCameraView getMaxNumFocusAreas = " +
        // parameters.getMaxNumFocusAreas());
        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(focusRect, 1000));

            parameters.setFocusAreas(focusAreas);
        }

        // System.out.println("CustomCameraView getMaxNumMeteringAreas = " +
        // parameters.getMaxNumMeteringAreas());
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));

            parameters.setMeteringAreas(meteringAreas);
        }

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
        }
        mCamera.autoFocus(this);
    }

    /**
     * 计算焦点及测光区域
     *
     * @param focusWidth
     * @param focusHeight
     * @param areaMultiple
     * @param x
     * @param y
     * @param previewleft
     * @param previewRight
     * @param previewTop
     * @param previewBottom
     * @return Rect(left, top, right, bottom) :  left、top、right、bottom是以显示区域中心为原点的坐标
     */
    public Rect calculateTapArea(int focusWidth, int focusHeight, float areaMultiple,
                                 float x, float y, int previewleft, int previewRight, int previewTop, int previewBottom) {
        int areaWidth = (int) (focusWidth * areaMultiple);
        int areaHeight = (int) (focusHeight * areaMultiple);
        int centerX = (previewleft + previewRight) / 2;
        int centerY = (previewTop + previewBottom) / 2;
        double unitx = ((double) previewRight - (double) previewleft) / 2000;
        double unity = ((double) previewBottom - (double) previewTop) / 2000;
        int left = clamp((int) (((x - areaWidth / 2) - centerX) / unitx), -1000, 1000);
        int top = clamp((int) (((y - areaHeight / 2) - centerY) / unity), -1000, 1000);
        int right = clamp((int) (left + areaWidth / unitx), -1000, 1000);
        int bottom = clamp((int) (top + areaHeight / unity), -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    public int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }

    /**
     * 校正角度
     *
     * @param orientation 点击拍照时设备的角度
     * @param camera
     */
    public void fixDirection(int orientation, Camera camera) {
        Log.e("TAG", "fixDirection");
        if (camera != null) {
//            MediaRecorder mMediaRecorder = new MediaRecorder();
//            mMediaRecorder.setCamera(mCamera);
            int result = calc(orientation);
            int cameraOrientation = getCameraOrientation();
            Log.e(TAG, "orientation = " + cameraOrientation);
            Camera.Parameters param = camera.getParameters();
            switch (result) {
                case 90:
                    param.setRotation(90);
                    break;
                case 180:
                    param.setRotation(180);
                    break;
                case 270:
                    param.setRotation(270);
                    break;
                default:
                    param.setRotation(0);
                    break;
            }
            camera.setParameters(param);
        }
    }

    //录像
    public void recordDirection(int orientation, Camera camera) {
        Log.e("TAG", "Direction》》");
        if (camera != null) {
            int result = calc(orientation);
            int cameraOrientation = getCameraOrientation();
            Log.e(TAG, "orientation = " + cameraOrientation);
            switch (result) {
                case 90:
                    mMediaRecorder.setOrientationHint(90);
                    break;
                case 180:
                    mMediaRecorder.setOrientationHint(180);
                    break;
                case 270:
                    mMediaRecorder.setOrientationHint(270);
                    break;
                default:
                    mMediaRecorder.setOrientationHint(0);
                    break;
            }
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
        }
    }

    @Override
    public void onSmallWindowClick() {
//        addBigWindow();
    }


    /***
     * 摄像头自动聚焦部分
     ****/
    private enum MODE {
        NONE, FOCUSING, FOCUSED, FOCUSFAIL

    }

    private static class ThreadHandler extends Handler {
        private WeakReference<CameraService> mReference;

        public ThreadHandler(WeakReference<CameraService> reference, Looper looper) {
            super(looper);
            mReference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CameraService service = mReference.get();
            if (service == null) {
                return;
            }
            switch (msg.what) {
                case Values.WRITE_GPS_DATA:
                    service.writeGpsInfo();
                    break;
            }
        }
    }
    class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: "+ intent.getAction());
            switch (intent.getAction()) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    if (!SystemUtil.isWifiNetworkConnected(context)) {
                        mReceiveStartRecordFromClient = false;
                    }
                    break;
                case Values.ACTION_RELEASE_CAMERA:
                    if (ZZXConfig.isP5C_HXJ ||ZZXConfig.isP5C_HXJ_TKY){
                        isOccupy = true;
                    }
                case Intent.ACTION_SHUTDOWN:
                    minWindow();
                    Settings.System.putInt(getContentResolver(), CAMERA_ID, mCameraId);
                    Settings.System.putInt(getContentResolver(), RECORDING_STATUES, mIsRecording ? 1 : 0);
                    releaseRecorder();
                    releaseCamera();
                    checkNeedClearIntervalOrTimerCaptureMode();// 如果在间隔拍照 释放间隔拍照
                    sendReleaseStatus();//释放成功广播
                    if (ZZXConfig.isP5C_SPXZ){
                        String extra = intent.getStringExtra("origin");
                        sendReleaseSuccess(extra);//发送广播告诉第三方释放成功
                    }
                    break;
                case Values.ACTION_RESTART_CAMERA:
                    Log.i("ACTION_RESTART_CAMERA", "ACTION_RESTART_CAMERA");
                    // 这里如果有多个值，就一次这样取值即可。
                    if (ZZXConfig.isP5C_HXJ || ZZXConfig.isP5C_HXJ_TKY){
                        isOccupy = false;
                    }
                    int camera_id = Settings.System.getInt(getContentResolver(), "camera_id", 0);// 取摄像头的id，用于判断是哪个摄像头
                    int autoVideo = Settings.System.getInt(getContentResolver(), "isAutoVideo", 0); // 取是否自动打开录像标志，0——不打开，1——默认打开
                    // 把获取到的状态传进方法里面去就行了。
                    // 但是这里方法没有参数，所以需要重载一个方法，既不影响原来的业务，还可以完成现有的功能
                    restartRecord(camera_id, autoVideo, context);
                    break;

                case Values.ACTION_UVC_CONNECT:
                    //   Settings.System.putString(getContentResolver(), Values.ACTION_UVC_CONNECT_STATE, "1");
                    Values.ACTION_UVC_CONNECT_STATE = true;
                    minWindow();
                    break;
                case Values.ACTION_UVC_DISCONNECT:
//                    Settings.System.putString(getContentResolver(), Values.ACTION_UVC_CONNECT_STATE, "0");
                    Values.ACTION_UVC_CONNECT_STATE = false;
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    //获得ConnectivityManager对象
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    //获取ConnectivityManager对象对应的NetworkInfo对象
                    //获取WIFI连接的信息
                    NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    //获取移动数据连接的信息
                    NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                        if (Settings.System.getInt(getContentResolver(),"ZZXAutomatic",0)==1){
                            mUploadFileUtil.upFile();
                        }
                    } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                        if (ZZXConfig.is4GC_SDJW || ZZXConfig.is4GC_SY){
                            Log.e(TAG, "onReceive: "+"连接热点");
//                            if (mLolaLiveUtil==null)
//                                Log.e(TAG, "onReceive: " +LolaLiveUtil.isRelease());
//                            mLolaLiveUtil = new LolaLiveUtil(CameraService.this);
                            if (mLolaLiveSDKUtil==null)
                            initLolaLiveSDK();
                        }

                        if (Settings.System.getInt(getContentResolver(),"ZZXAutomatic",0)==1){
                            mUploadFileUtil.upFile();
                        }
                    } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                        if (Settings.System.getInt(getContentResolver(),"ZZXAutomatic",0)==1){
                            mUploadFileUtil.upFile();
                        }
                    } else {
                        if (mUploadFileUtil.mFtpUpload != null) {
                            mUploadFileUtil.mFtpUpload.release();
                        }
                        if (mLolaLiveSDKUtil!=null){
                            mLolaLiveSDKUtil.stop();
                            mLolaLiveSDKUtil=null;
                        }
                    }
                    break;
                case Values.ACTION_VIDEO_CALL_END:
                    Log.e(TAG, "onReceive: "+Values.ACTION_VIDEO_CALL_END );
                    if (ZZXConfig.isP5C_HXJ || ZZXConfig.isP5C_HXJ_TKY){
                        isOccupy = false;
                    }
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);
                    switch (wifiState){
                        case WifiManager.WIFI_STATE_ENABLED:
                            //已打开
                            Log.i(TAG, "mUuid: "+mUuid);
                            WifiConfiguration wifiConfiguration = mWifiAdmin.CreateWifiInfo(mUuid,"66668888"+imei,3);
                            mWifiAdmin.addNetwork(wifiConfiguration);
                            break;
                        case WifiManager.WIFI_STATE_ENABLING:
                            //打开中
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            //已关闭
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                            //关闭中
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            //未知
                            break;
                    }
                    break;
            }
        }
    }


    public void sendReleaseStatus(){
        Intent intent = new Intent(Values.ACTION_RELEASE_CAMERA_STATUS);
        intent.putExtra(Values.RELEASE_STATUS,true);
        sendBroadcast(intent);
    }
    public void sendReleaseSuccess(String value){
        Intent intent= new Intent();
        intent.setAction("release_success");
        intent.putExtra("origin",value);
        sendBroadcast(intent);
    }
    public static void sendMsgToUploadHandler(int what, @Nullable Object obj) {
        Message msg = mUploadFileHandler.obtainMessage();
        msg.obj = obj;
        msg.what = what;
        mUploadFileHandler.sendMessage(msg);
    }

    private boolean isUploading ;
    /**
     * 这里为了方便单独创建一个Handler，该Handler与主线程Looper相关联,和
     *
     * @see EventHandler 一样都是运行在主线程中的Looper中
     */
    private final class UploadFileHandler extends Handler {
        private WeakReference<CameraService> mWk;
        private CameraService service;

        UploadFileHandler(WeakReference<CameraService> wk) {
            mWk = wk;
        }

        @Override
        public void handleMessage(Message msg) {
            service = mWk.get();
            if (service == null)
                return;
            FTPUploadInfo info = (FTPUploadInfo) msg.obj;
            String[] values = getStatusValues(msg.what, info);
            mUploadFileUtil.saveUploadStatusToSetting(values);
            if (values[0] != null) {
//                service.mTvUploadFile.setText(values[0]);
            }
            if (values[1] != null) {
//                service.mTvUploadStatus.setText(values[1]);
            }
        }

        /**
         * 根据状态码和文件上传对象中的信息，获取用于显示给客户查看的状态信息
         *
         * @param statusCode FTP文件上传状态码
         * @param info       上传文件对象
         * @return 字符串数组
         * <p>
         * values[0]: 文件名
         * values[1]: 该文件对应的状态
         * 注:为null时，表示该信息不需要显示
         * </p>
         */
        private String[] getStatusValues(@FTPUploadUtil.UploadStatusCode int statusCode, @Nullable FTPUploadInfo info) {
            String[] values = new String[2];
            switch (statusCode) {
                case FTPUploadUtil.UPLOAD_STATUS_DEFAULT:
                    values[0] = mUploadFileUtil.getSimpleUploadFileName(info);
                    values[1] = "等待上传";
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "等待上传");
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_READY:
                    values[0] = mUploadFileUtil.getSimpleUploadFileName(info);
                    values[1] = "准备上传";
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "准备上传");
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_START:
                    values[0] = mUploadFileUtil.getSimpleUploadFileName(info);
                    values[1] = "开始上传";
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "开始上传");

//                    showUpLoadDialog();
//                    filename.setText(getUploadFileName(info));
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_TRANSFERRED:
                    //前面的名字已经设置了，这里没有必要再设置，而且该方法调用的频率比较高
                    //values[0]=getSimpleUploadFileName(info);
                    isUploading = true;
                    mHandler.sendEmptyMessageDelayed(Values.UPLOADERROR,10*60*1000);
                    values[0] = null;
                    values[1] = mUploadFileUtil.getUploadRate(info);

                    if (mUploadFileUtil.getUploadRate(info) == null) {
                        break;
                    }
                    Double progress = Double.parseDouble(mUploadFileUtil.getUploadRate(info).replace("%", ""));
                    int progressInt = progress.intValue();
                    if (mUpLoadLoadShowed) {
                        addBigWindow();
                        if (schedue != null) {
                            filename.setText(mUploadFileUtil.getUploadFileName(info));
                            schedue.setText(mUploadFileUtil.getUploadRate(info));
                        }

                        if (progressBar != null) {
                            progressBar.setProgress(progressInt);
                        }
                    } else {
                        addSmallWindow();
                        mWindowSmall.setUpload(mUploadFileUtil.getUploadRate(info));
                    }
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_COMPLETED:
                    values[0] = mUploadFileUtil.getSimpleUploadFileName(info);
                    values[1] = "上传完成";
                    if (mUpLoadLoadShowed) {
                        dismissUpLoadDialog();
                    } else {
                        dismisssmallUpLoad();
                    }
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "上传完成");
                    mUploadFileUtil.sendUploadFile(info);
                    //此方法为客户端请求上传文件完成后把文件名字发送给客户端
                    if (ProtocolUtils.mCenterUpload){
                        ProtocolUtils.reportFileUpload(info.getFileLocalName());
                    }
                    if (ZZXConfig.isP5C_RZXFD){
                        DeleteFileUtil.delete(info.getFileLocalPath()+info.getFileLocalName());
                        LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "上传完成  删除");
                    }
                    if (ZZXConfig.isP5XD_SXJJ){
                        FileUtils.put(info.getFileLocalPath()+info.getFileLocalName());
                    }
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_ABORTED:
                    values[0] = mUploadFileUtil.getSimpleUploadFileName(info);
                    values[1] = "上传暂停";
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "上传暂停");
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_FAILED:
                    values[0] = mUploadFileUtil.getSimpleUploadFileName(info);
                    values[1] = "上传失败";
                    if (mUpLoadLoadShowed) {
                        dismissUpLoadDialog();
                    } else {
                        dismisssmallUpLoad();
                    }
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "上传失败");
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_FILE_NO_FIND:
                    values[0] = mUploadFileUtil.getSimpleUploadFileName(info);
                    values[1] = "找不到文件";
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "找不到文件");
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_NO_UPLOAD_FILE:
                    values[0] = "无文件上传";
                    values[1] = null;
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "无文件上传");
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_NO_NETWORK:
                    values[0] = null;
                    values[1] = "网络不可用";
                    if (mUpLoadLoadShowed) {
                        dismissUpLoadDialog();
                    } else {
                        dismisssmallUpLoad();
                    }
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "网络不可用");
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_LOGIN_FAILED:
                    values[0] = null;
                    values[1] = "退出登陆";
                    if (mUpLoadLoadShowed) {
                        dismissUpLoadDialog();
                    } else {
                        dismisssmallUpLoad();
                    }
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "登录失败，请检查IP地址用户名密码");
                    Toast.makeText(CameraService.this,"登录失败，请检查IP地址用户名密码！",Toast.LENGTH_LONG).show();
//                    mUploadFileUtil.mFtpUpload.release();
//                    mUploadFileUtil.mFtpUpload =null;
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_LOGIN_SUCCESS:
                    values[0] = null;
                    values[1] = "登录成功";
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "登录成功");
                    return values;
                case FTPUploadUtil.UPLOAD_STATUS_ERROR:
                    values[0] = mUploadFileUtil.getSimpleUploadFileName(info);
                    values[1] = "上传出错";
                    if (mUpLoadLoadShowed) {
                        dismissUpLoadDialog();
                    } else {
                        dismisssmallUpLoad();
                    }
                    LogUtil.e(TAG, mUploadFileUtil.getSimpleUploadFileName(info) + "上传出错");
                    return values;
            }
            values[0] = null;
            values[1] = null;
            return values;
        }
    }

    class EventHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
//            Values.LOG_W(TAG, "EventHandler.what = " + msg.what + "; msg.arg1 = " + msg.arg1);
            switch (msg.what) {
                case Values.PIC_MODE_INTERVAL:
                    EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.TAKE_PICTURE);
                    Message msg1 = mHandler.obtainMessage();
                    msg1.copyFrom(msg);
                    mHandler.sendMessageDelayed(msg1, msg1.arg1 * 1000);
                    break;
                case Values.PIC_MODE_QUICK:
                    setQuickSerialMode(msg.arg1);
                    EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.TAKE_PICTURE);
                    break;
                case Values.TAKE_PICTURE:
                    clearIntervalOrTimerCaptureMode();
                    EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.TAKE_PICTURE);
                    break;
                case Values.DISMISS_WINDOW:
                    if (mSecondPopWindow.isShowing())
                        mSecondPopWindow.dismiss();
                    if (mPopWindow.isShowing())
                        mPopWindow.dismiss();
                    break;
                case Values.BACK_RECORD_STOP:
                    //将删除文件放到主线程,利用阻塞来防止录像没停止又启动.
                    stopRecordAsMedia();
                    EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.BACK_STOP_PRE_MODE_RECORD);
                    break;
                case Values.MAIN_STOP_RECORD:
                    /**若为此事件则表明是人工打开录像然后分段录像才自动停止,此时设置{@link #mPreRecordMode}为false,这样重新录像时能设置分段的{@link android.media.MediaRecorder#setMaxDuration(int)}.
                     * */
                    recordSection();
                    break;
                case Values.HANDLER_MAIN_START_RECORD:
                    startRecord();
                    break;
                case Values.HANDLER_MAIN_STOP_RECORD:
                    stopRecordAsMain();
                    break;
                case Values.HANDLER_MAIN_STOP_LASER:
                    turnofflaser();
                    break;
                case Values.HANDLER_MAIN_OPEN_CLICK:
                    mBtnCaptureRecord.setClickable(true);
                    break;
                case Values.HANDLER_MAIN_STOP_RECORD_AS_USER:
                    stopRecordAsUserButton();
                    break;
                case Values.TALK_DIALOG:
//                    MainActivity.mProtocolUtils.parserCenterAudio((String[]) eventMessage.getMsg(), BaseProtocol.SECOND);
                    mBurstSound.stop(mDidiStreamID);
                    vib.cancel();
                    dismissTalkDialog();
                    break;
                case Values.WRITER_CLOSE:
                    mHandler.removeMessages(Values.WRITER_CLOSE);
                    LogUtil.close();
                    break;
                case Values.ZOOM_GONE:
                    mHandler.removeMessages(Values.ZOOM_GONE);
                    mIvZoom.setVisibility(View.GONE);
                    break;
                case Values.ACCELEROMETER_SENSOR:
                    mHandler.removeMessages(Values.ACCELEROMETER_SENSOR);
                    mImportantFileSign = true;
                    stopRecord();
                    startRecordAsUser();
                    break;
                case Values.UPLOADERROR:
                    if (isUploading){
                        return;
                    }else {
                        LogUtil.e("zzx","10分钟未上传，关闭网络重新打开");
                        if (SystemUtil.isNetworkConnected(CameraService.this)){
                            if (SystemUtil.isWifiNetworkConnected(CameraService.this)){
                                SystemUtil.closeWifi(CameraService.this);
                                SystemUtil.openWifi(CameraService.this);
                            }
                            if (SystemUtil.isMobileNetworkAvailable(CameraService.this)){
                                SystemUtil.setMobileDataState(CameraService.this,false);
                                SystemUtil.setMobileDataState(CameraService.this,true);
                            }
                        }
                    }
                    isUploading =false;
                    break;
                case Values.START_RECORD_LOGCAT:
                    mHandler.removeMessages(Values.START_RECORD_LOGCAT);
                    Settings.System.putInt(getContentResolver(),
                            Values.ACTION_SCREEN_NEVER, 1);//修改屏幕永不休眠
                    mBtnCaptureRecord.setBackgroundResource(R.drawable.btn_record_stop);
                    mBtnCaptureRecord.setId(R.drawable.btn_record_stop);
                    initRecorderAndRecord();
                    int time = mRecordManager.getRecorderTime();
                    /**处於预录模式下再启动分段录像也是需要通过Handler来延时发送停止事件.
                     * */
                    if (mPreRecordMode && time != 0) {
                        mHandler.sendEmptyMessageDelayed(Values.MAIN_STOP_RECORD, time);
                    }
                    disableCaptureOrRecordSetting();
//                    sendLogcatRecord(true);
                    break;
                case Values.RELEASE_CAMERA:
                    if (!mPreRecordMode && !mIsRecording && !mNeedReportVideo && !mIntervalOrTimerMode && !mContinuousCaptureMode && !isScreen){
                        sendBraod(Values.ACTION_RELEASE_CAMERA);
                    }
                    break;
                case Values.IS_HOME:
                    mHandler.removeMessages(Values.IS_HOME);
                    Log.i(TAG, "isHome = "+isTranslation());
                    if (isTranslation()){
                        Settings.System.putInt(getContentResolver(),
                                Values.ACTION_SCREEN_NEVER, 1);
                    }else {
                        Settings.System.putInt(getContentResolver(),
                                Values.ACTION_SCREEN_NEVER, 0);
                    }
                    mHandler.sendEmptyMessageDelayed(Values.IS_HOME,10*1000);
                    break;
                case Values.COUNT_DOWN:
                    int value = msg.arg1;
                    mTvCountTime.setVisibility(View.VISIBLE);
                    mTvCountTime.setText(String.valueOf(value--));
                    if (value >= 0){
                        Message message = Message.obtain();
                        message.what = Values.COUNT_DOWN;
                        message.arg1 = value;
                        sendMessageDelayed(message,1000);
                    }
                    break;
            }
            super.dispatchMessage(msg);
        }
    }

    class ReportCheckChanged implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

        }
    }

    class AudioReportTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {
            while (true) {
                boolean isCancel = isCancelled();
                if (!isCancel) {
                    if (mAudioEncoder != null) {
                        try {
                            mAudioEncoder.startReport();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * 拍照模式一级菜单监听器
     */
    class PicModeListClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mPicModeRootAdapter.setCheck(position, true);
            /**
             * 初始化拍照模式二级菜单
             * */
            if (mPicModeChildView == null) {
                mPicModeChildView = (ListView) ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_popup_window_container, null);
                mPicModeChildAdapter = new CaptureRecordPopListAdapter(CameraService.this, mRes.getTextArray(R.array.array_quick_series_mode), R.array.array_quick_series_mode_value, R.array.array_quick_series_mode_icon, true, false);
                mPicModeChildView.setAdapter(mPicModeChildAdapter);
                PicModeChildItemClickListener mPicModeChildItemListener = new PicModeChildItemClickListener();
                mPicModeChildView.setOnItemClickListener(mPicModeChildItemListener);
            }
            mPicMode = position;
            mSPEditor.putInt(Values.PIC_MODE, position).commit();
            SharedPreferences sp = RemotePreferences.getRemotePreferences(CameraService.this);
            int yOffset = 0;

            switch (position) {
                case Values.PIC_MODE_SIGNAL:
                    mIvMode.setBackgroundResource(R.drawable.mode_single);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPopWindow.dismiss();
                        }
                    }, 200);
                    return;
                case Values.PIC_MODE_QUICK:
                    mPicModeChildView.setBackgroundResource(R.drawable.bg_down);
                    mPicModeChildAdapter.setData(mRes.getTextArray(R.array.array_quick_series_mode), R.array.array_quick_series_mode_icon, R.array.array_quick_series_mode_value);
                    mPicModeChildAdapter.setCheck(sp.getInt(Values.PIC_MODE_QUICK_POSITION, 0), true);
                    yOffset = 35;
//                    yOffset = -15;
                    break;
                case Values.PIC_MODE_INTERVAL:
                    mPicModeChildView.setBackgroundResource(R.drawable.bg_down);
                    mPicModeChildAdapter.setData(mRes.getTextArray(R.array.array_interval_mode), R.array.array_interval_mode_icon, R.array.array_interval_mode_value);
                    mPicModeChildAdapter.setCheck(findPicModeChildItemIndex(sp.getString(Values.PIC_MODE_INTERVAL_VALUE, Values.PIC_MODE_INTERVAL_DEFAULT_VALUE), R.array.array_interval_mode_value), true);
                    yOffset = 65;
//                  yOffset = -45;
                    break;
                case Values.PIC_MODE_TIMER:
                    mPicModeChildView.setBackgroundResource(R.drawable.bg_hor_middle);
                    mPicModeChildAdapter.setData(mRes.getTextArray(R.array.array_timer_mode), R.array.array_timer_mode_icon, R.array.array_timer_mode_value);
                    mPicModeChildAdapter.setCheck(sp.getInt(Values.PIC_MODE_TIMER_POSITION, 0), true);
                    yOffset = 60;
//                    yOffset = -70;
                    break;
            }
            showPopupWindow(mSecondPopWindow, mPicModeChildView, mBtnMode, 245, yOffset);
        }
    }

    /**
     * 拍照模式的二级菜单监听器.
     */
    class PicModeChildItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CaptureRecordPopListAdapter adapter = (CaptureRecordPopListAdapter) parent.getAdapter();
            String value = adapter.getValue(position);
            adapter.setCheck(position, true);
            mPicModeRootAdapter.setItemIcon(mPicMode, adapter.getItemIcon(position));
            mIvMode.setBackground(adapter.getItemIcon(position));
            switch (mPicMode) {
                case Values.PIC_MODE_QUICK:
                    mSPEditor.putString(Values.PIC_MODE_QUICK_VALUE, value);
                    mSPEditor.putInt(Values.PIC_MODE_QUICK_POSITION, position);
                    break;
                case Values.PIC_MODE_INTERVAL:
                    mSPEditor.putString(Values.PIC_MODE_INTERVAL_VALUE, value);
                    mSPEditor.putInt(Values.PIC_MODE_INTERVAL_POSITION, position);
                    break;
                case Values.PIC_MODE_TIMER:
                    mSPEditor.putString(Values.PIC_MODE_TIMER_VALUE, value);
                    mSPEditor.putInt(Values.PIC_MODE_TIMER_POSITION, position);
                    break;
            }
            mSPEditor.commit();
            mHandler.sendEmptyMessageDelayed(Values.DISMISS_WINDOW, 200);
        }
    }

    /**
     * 拍照及录像分辨率设置监听器
     *
     * @see #isCapture 判断设置的是拍照还是录像.
     */
    class PicResolutionItemListener implements AdapterView.OnItemClickListener {
        private boolean isCapture = true;

        PicResolutionItemListener(boolean isCapture) {
            this.isCapture = isCapture;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CaptureRecordPopListAdapter adapter = (CaptureRecordPopListAdapter) parent.getAdapter();
            adapter.setCheck(position, true);
            String ratio = adapter.getItem(position).toString();
            String[] resolution = ratio.split("x");
            Values.LOG_I(TAG, "width x height = " + resolution[0] + "x" + resolution[1]);
            if (isCapture) {
                mSPEditor.putString(getKey(R.string.key_ratio_picture), ratio).apply();
            } else {
                mSPEditor.putString(getKey(R.string.key_ratio_record), ratio).apply();
            }
            mHandler.sendEmptyMessageDelayed(Values.DISMISS_WINDOW, 200);
            refreshRatioBtn();
        }
    }

    private String picPath;
    private class StoreContinuousPicThread extends Thread {
        @Override
        public void run() {
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RESTART_PREVIEW);
            for (byte[] data : mDataList) {
                writeData(data);
                picPath = mPicPath.replace(Values.FILE_SUFFIX_PIC,"_"+FileUtils.filesize(mPicPath)+Values.FILE_SUFFIX_PIC);
                File file = new File(mPicPath);
                file.renameTo(new File(picPath));
                if (Settings.System.getInt(getContentResolver(),"ZZXEncryption",0)==1){
                    mEncryptionUtil.addUnencry(picPath,Values.FILE_SUFFIX_PIC);
                }
                if (SystemUtil.isNetworkConnected(CameraService.this) && Settings.System.getInt(getContentResolver(),"ZZXAutomatic",0)==1 && picPath != null) {
                    if (mUploadFileUtil.isSetFTP())
                    mUploadFileUtil.uploadVideoFile(FTPUploadInfo.getFTPUploadInfo(CameraService.this, picPath));
                }
                sendBroadcastSP(Values.SPXZ_TAKE_PICTURE,picPath);
                mPicPath = picPath;
            }
            insertDatabases();
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.IMG_THUMBNAIL);
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.ENABLE_SOME_VIEW);
            Settings.System.putString(getContentResolver(),"case_number","");
            mDataList.clear();
            super.run();
        }
    }

    /**
     * 响应屏幕单击和双击反应,来做全屏及退出全屏操作
     * 单击:  若已全屏则自动对焦,否则进入全屏模式.
     * 双击:  同上反之.
     */
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mModeFullScreen) {
                quitFullScreenMode();
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!mModeFullScreen) {
                enterFullScreenMode();
            }
            return true;
        }
    }

    class RealTimeTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {
            while (!isCancelled()) {
                if (mDeque == null || mDeque.isEmpty()) {
                    continue;
                }
                if (mVideoEncoder != null && !mDeque.isEmpty())
                    mVideoEncoder.encode(mDeque.poll());
            }
            return null;
        }
    }
    private boolean isKeyPic =false;
    private class CameraReceiver extends BroadcastReceiver {
        private final int LONG_PRESSED = 0;
        private final int LONG_PRESSED_UP = 1;
        private final int SHUTTER = 2;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: " + action);
            switch (action) {
                case Values.ACTION_UEVENT_CAPTURE:
                    if (!mStorageNotEnough) {
                        if (!Values.ACTION_UVC_CONNECT_STATE) {
                            int event = intent.getIntExtra(Values.U_EVENT_LONG_PRESSED_STATE, 0);
                            switch (event) {
                                case LONG_PRESSED:
                                    if (MainActivity.mProtocolUtils != null && !MainActivity.mProtocolUtils.mAudioRequesting
                                            && !MainActivity.mProtocolUtils.isAudioPassing() && MainActivity.mProtocolUtils.isSocketCreated()) {
                                        MainActivity.mProtocolUtils.requestAudio();
                                        if (isVibrator(context)) {
                                            VibratorManager.vibrate(context, 200);
                                        }
                                    }
                                    break;
                                case LONG_PRESSED_UP:
                                    break;
                                case SHUTTER:
//                                    if (getUsbCameraShow(CameraService.this)) {
//                                        return;
//                                    }
//                                    takePictureByAction();
                                    EventPhonos();
                                    break;
                            }
                        }
                    }
                    break;
                case Values.COM_KEY_CODE_RECORD:
                case Values.ACTION_UEVENT_RECORD:
                    EventRecord(intent);
                    break;
                case Values.PHOTOS:
                    EventPhonos();
                    break;
                case Values.RED_CUT:
                    setIrCutEnable(!isIrCutOpened());
                    break;
                case Values.ACTION_WIFI_SCAN_FINISHED:
                    if (isExternalStorageEnabled()) {
                        checkCameraNeedRestart();
                        mImportantFileSign = false;
                        mVideoLongPressNum = 0;
                        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RECORD);
                    }
                    break;
                case Values.ACTION_UEVENT_MIC:
                    EventMic(intent, context);
                    break;
                case Values.LOGCAT_MIC:
                    boolean recordState = intent.getBooleanExtra("zzx_state", false);
                    if (recordState) {//判断是否开始录像
                        if (isVideoReecord) {
                            mHandler.sendEmptyMessageDelayed(Values.HANDLER_MAIN_STOP_RECORD_AS_USER, 1000);
                            // stopRecordAsUserButton();
                        }
                    }
                    break;
                case Values.ACTION_RECORD_PREFERENCE_NOTIFY:
                    if (!mIsRecording) {
                        initRecordFromRecordMode();
                    }
                    break;
                case Values.ACTION_SYSTEM_KEY_HOME:
                    if (intent.getBooleanExtra(Values.U_EVENT_LONG_PRESSED_STATE, false)) {
                        sendSOS(context);
                    }
                    break;
                case Values.ACTION_SYSTEM_KEY_BACK:
                    if (!intent.getBooleanExtra(Values.U_EVENT_LONG_PRESSED_STATE, false)) {
                        if (isWindowMax()) {
                            checkCameraNeedIDLE();
                            minWindow();
                        }
                    }
                    break;
                case Intent.ACTION_TIME_TICK:
                    if (!mCameraIDLEMode && !mIsWindowMax && !mPreRecordMode && !mIsRecording && !mNeedReportVideo && !mIntervalOrTimerMode && !mContinuousCaptureMode && !mUserRecording) {
                        if ((++mIDLECount) >= Values.IDLE_MAX_COUNT) {
                            cameraIDLEMode();
                        }
                    }
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    Log.e("zzxzdm", "ACTION_SCREEN_OFF");
                    isScreen = false;
                    //       checkCameraNeedIDLE();
//                    releaseRecorder();
//                    releaseCamera();
//                    releaseIrCut();
//                    checkNeedClearIntervalOrTimerCaptureMode();// 如果在间隔拍照 释放间隔拍照
                    checkCameraNeedIDLE();
                    int openCamera = Settings.System.getInt(context.getContentResolver(), Values.OPEN_CAMERA_TYPE, Values.OPEN_MAIN_CAMERA);
                    if (openCamera != Values.OPEN_MAIN_CAMERA) {
                        releaseCamera();
                    }
                    break;
                case Intent.ACTION_BATTERY_LOW:
                    //  Toast.makeText(getApplicationContext(),"ACTION_BATTERY_LOW",Toast.LENGTH_SHORT).show();
                    int battery = Settings.System.getInt(getContentResolver(), "battery", 0);
                    if (ZZXConfig.isSongJian) {

                        Toast.makeText(getApplicationContext(),battery+"",Toast.LENGTH_LONG).show();
                        if (battery <= 8){
                            mBurstSound.play(mDidianSoundID, 1.0f, 1.0f, 0, 20, 1.0f);
                        }
                    } else {
                        if (battery <=15)
                        mBurstSound.play(mDidianSoundID, 1.0f, 1.0f, 0, 6, 1.0f);
                    }
//                    LogUtil.e("电量"," :"+battery);
                    notifyLogcatBattery();
                    break;
                case Intent.ACTION_SCREEN_ON:
                    Log.e("zzxzdm", "ACTION_SCREEN_ON");
                    if (mIsWindowMax) {
                        int openCameraType = Settings.System.getInt(context.getContentResolver(), Values.OPEN_CAMERA_TYPE, Values.OPEN_MAIN_CAMERA);
                        if (openCameraType == Values.OPEN_MAIN_CAMERA)
                            checkCameraNeedRestart();
                    }
                    OpenIrCut();
                    isScreen = true;
                    break;
                case Values.ACTION_SCREEN_KEEP_ON:
                    if (intent.getBooleanExtra(Values.EXTRA_SCREEN_KEEP_ON, false)) {
                        keepScreenOn();
                    } else {
                        notKeepScreenOn();
                    }
                    break;
                case Values.ACTION_SYSTEM_KEY_MENU:
                    if (intent.getBooleanExtra(Values.U_EVENT_LONG_PRESSED_STATE, false) && !mNeedReportVideo
                            && MainActivity.mProtocolUtils != null
                            && !MainActivity.mProtocolUtils.mVideoRequesting
                            && MainActivity.mProtocolUtils.isSocketCreated()) {
                        MainActivity.mProtocolUtils.requestVideo();
                        if (isVibrator(context)) {
                            VibratorManager.vibrate(context, 200);
                        }
                    }
                    break;
                case Values.ACTION_SOS://镭射灯的事件
//                case Values.ACTION_PPT:
                    Log.e("zzx", "sos");
//                    if (!isVideoReecord || !isSoundReecord){
//                        controllDevice(1, LIGHT_COLOR_YELLOW);
//                        new Thread(){
//                            @Override
//                            public void run() {
//                                super.run();
//                                try {
//                                    Thread.sleep(5*1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                controllDevice(1, LIGHT_COLOR_GREEN_NO_FLASH);
//                            }
//                        }.start();
//                    }
                    List<String> sosList = SOSInfoUtils.getSOSNumList(context);
                    if (sosList != null && sosList.size() > 0) {
                        sendSOSMsg();
                    }
                    sendSOS(context);
                    break;
                case Values.ACTION_MIC_RECORD:
                    isSoundReecord = intent.getBooleanExtra("record_state", false);
                    break;
                case Values.SERVER_ACTION_MIN_WINDOW:
                    minWindow();
                    break;
                case Values.SERVER_ACTION_MAX_WINDOW:
                    maxWindow();
                    break;
                case Values.ACTION_CAMERA_DARK:
                    Log.d("zzx", "ACTION_CAMERA_DARK");
                    setIrCutEnable(true);
                    break;
                case Values.ACTION_CAMERA_LIGHT:
                    Log.d("zzx", "ACTION_CAMERA_LIGHT");
                    setIrCutEnable(false);
                    break;
                case Values.BLE_ACTION:
                    String extra = intent.getStringExtra(Values.BLE_STATUS);
                    switch (extra) {
                        case BLE_STATUS_PIC:
                            if (mLastTime != 0) {
                                mTimeSpan = System.currentTimeMillis() - mLastTime;
                            }
                            mLastTime = System.currentTimeMillis();
                            if (mTimeSpan > 2000 || mTimeSpan == 0) {
                                if (!Values.ACTION_UVC_CONNECT_STATE) {
                                    takePictureByAction();
                                }
                            }
                            break;
                        case BLE_STATUS_VON:
                        case BLE_STATUS_VOU:
                            //   Log.e("key>>>>1","mLastTime:"+mLastTime+"===mTimeSpan:"+mTimeSpan);
                            if (mLastTime != 0) {
                                mTimeSpan = System.currentTimeMillis() - mLastTime;
                                //      Log.e("key>>>>2","mLastTime:"+mLastTime+"===mTimeSpan:"+mTimeSpan);
                            }
                            //   Log.e("key>>>>3","mLastTime:"+mLastTime+"===mTimeSpan:"+mTimeSpan);
                            mLastTime = System.currentTimeMillis();
                            if (mTimeSpan > 2000 || mTimeSpan == 0) {
                                //               Log.e("key>>>>4","mLastTime:"+mLastTime+"===mTimeSpan:"+mTimeSpan);
                                if (!Values.ACTION_UVC_CONNECT_STATE) {
                                    if (!mReceiveStartRecordFromClient && isExternalStorageEnabled()) {
                                        checkCameraNeedRestart();
                                        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RECORD);
                                    }
                                    if (isSoundReecord ) {//一键切换功能，如果正在录音开启录像，停止录音
                                        startRecordService(false);
                                    }
                                }
                            }
                            break;
                    }
                    break;
                case Values.ACTION_SOUND:
                    String path = intent.getStringExtra("path");
                    String soundPath = path.replace(Values.FILE_SUFFIX_AUDIO,"_"+FileUtils.filesize(path)+Values.FILE_SUFFIX_AUDIO);
                    File file = new File(path);
                    file.renameTo(new File(soundPath));
                    if (Settings.System.getInt(getContentResolver(),"ZZXEncryption",0)==1){//是否加密
                        mEncryptionUtil.addUnencry(soundPath,Values.FILE_SUFFIX_AUDIO);
//                        String encryptingSoundName = path.replace(Values.FILE_SUFFIX_AUDIO,Values.ENCRYPTION + Values.FILE_SUFFIX_AUDIO);
//                        AESTask aesTask = aesSasynManage.createAndExecuteEncryptTask(path,encryptingSoundName,"bad123456");
                    }
                    if (SystemUtil.isNetworkConnected(CameraService.this) && Settings.System.getInt(getContentResolver(),"ZZXAutomatic",0)==1){//是否自动上传
                        if (mUploadFileUtil.isSetFTP()){
                            mUploadFileUtil.uploadVideoFile(FTPUploadInfo.getFTPUploadInfo(CameraService.this, soundPath));
                        }
                    }

                    break;
                case Values.FILE_UPLOAD:
                    if (SystemUtil.isNetworkConnected(CameraService.this)){
                        mUploadFileUtil.upFile();
                    }
                    break;
                case Values.ACTION_RECEIVER_UVC_CAMERA:
                    boolean state = intent.getBooleanExtra(Values.STATUS, false);
                    boolean isUsbUpload = intent.getBooleanExtra(Values.IS_UPLOAD, false);
                    //usb摄像头最大化，则主摄像头界面最小化
                    if (state) {
                        minWindow();
                    }
                    //usb摄像头最小化，则主摄像头界面最大化
                    else {
                        maxWindow();
                        //从外置式摄像头切换到主摄像头(前摄像头)
                        if (isUsbUpload && mOnCameraTypeChangeListener != null) {
                            mOnCameraTypeChangeListener.cameraTypeChange(mFrontCamera ? Values.OPEN_FRONT_CAMERA : Values.OPEN_MAIN_CAMERA);
                        }
                    }
                    break;
                case Values.ACTION_USB_CAMERA_STATUES:
                    int event3 = intent.getIntExtra(Values.USB_CAMERA_STATUES, 0);
                    switch (event3) {
                        case Values.USB_STATUES_CONNECT:
                            mBtnUsbCameraSwitcher.setVisibility(View.VISIBLE);
                            break;
                        case Values.USB_STATUES_DISCONNECT:
                            mBtnUsbCameraSwitcher.setVisibility(View.GONE);
                            sendUVCCamera();
                            maxWindow();
                            Settings.System.putInt(getContentResolver(), Values.OPEN_CAMERA_TYPE, Values.OPEN_MAIN_CAMERA);
                            break;
                    }
                    break;
                case Values.ACTION_EVENT_RECORD:
                    getEventSetting(intent, ConfigXMLParser.CFG_UEVENT_RECORD, RECORD, context);
                    break;
                case Values.ACTION_EVENT_SWITCH:
                    getEventSetting(intent, ConfigXMLParser.CFG_UEVENT_SWITCH, SWITCH, context);
                    break;
                case Values.ACTION_EVENT_PHONOS:
                    getEventSetting(intent, ConfigXMLParser.CFG_UEVENT_PHONOS, PHONOS, context);
                    break;
                case Values.ACTION_EVENT_SUBTRACT:
                    getEventSetting(intent, ConfigXMLParser.CFG_UEVENT_SUBTRACT, MIC, context);
                    break;
                case Values.ACTION_EVENT_ADD:
                    getEventSetting(intent, ConfigXMLParser.CFG_UEVENT_ADD, LED, context);
                    break;
                case Values.ACTION_PPT:
                    if (!CheckUtils.IsContinuousClick(500)){
                        return;
                    }
                    Log.e(TAG, "onReceive: "+ Values.ACTION_PPT);
//                    Intent intent1 = new Intent(Values.ZFY_MEDIA_UPLOADED, Uri.fromFile(new File("/storage/sdcard0/police/pic/20200413/000000_1_20200413_084732.jpg")));
//                    sendBroadcast(intent1);
                    if (ZZXConfig.isP5C_SPXZ){
                        Intent intent3 = new Intent();
                        intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent3.setClassName(Values.PACKAGE_NAME_VIDEO_ASSIST,Values.CLASS_NAME_VIDEO_ASSIST);
                        start(context,intent3);
                    }
                    if (ZZXConfig.is4G5_BAD){
                        EventLed();
                    }
                    break;
                case Values.ACTION_DROP:
                    boolean States1 = intent.getBooleanExtra("zzx_state",false);
                    if (States1){
                        mDidiStreamID = mBurstSound.play(mDidiSoundID, 1.0f, 1.0f, 0, 5, 1.0f);
                        if (!mStorageNotEnough && isVideoReecord){
                            mHandler.sendEmptyMessage(Values.ACCELEROMETER_SENSOR);
                        }
                    }
                    break;
                case Values.ZZX_ACTION_BATSW:
                    boolean States = intent.getBooleanExtra("zzx_state",false);
                    if (States) {
                        ZZXConfig.isPullout = false;
                        lightvalue = Settings.System.getInt(getContentResolver(), "ZZXBrightness",255);
                        if (SystemUtil.getAirplaneMode(context)){
                            SystemUtil.setAirplaneModeOn(context,false);
                            SystemUtil.setBrightness(lightvalue);
                            if (GPSstate){
                                mLowPowerUtil.openGPSSettings();//只打开GPS 未打开北斗
                            }
                            sendBroadcast(new Intent(Values.ACTION_RESTART_CAMERA));
                            sendBraod("Battery");
                        }
                    }else {
                        ZZXConfig.isPullout = true;
                        GPSstate =mLowPowerUtil.getGPsState();
                        Settings.System.putInt(getContentResolver(),"ZZXBrightness",mLowPowerUtil.getScreenBrightness(context));
                        mLowPowerUtil.setPowerMode(true);
                        sendBroadcast(new Intent(Values.ACTION_RELEASE_CAMERA));
                        if (isSoundReecord){
                            startRecordService(false);
                        }
                    }
                    break;
                case Values.ZFY_MEDIA_UPLOADED:
                    String uploadfile = intent.getStringExtra("file_path");
                    Toast.makeText(context,new File(uploadfile).getName()+"上传成功",Toast.LENGTH_SHORT).show();
                    break;
                case Values.ZFY_MEDIA_DELETED:
                    String deletedfile = intent.getStringExtra("file_path");
                    Toast.makeText(context,new File(deletedfile).getName()+"删除成功",Toast.LENGTH_SHORT).show();
                    break;
                case Values.ACTION_VIDEO_FPS:
                    sendBroadcast(new Intent(Values.ACTION_RELEASE_CAMERA));
//                    sendBroadcast(new Intent(Values.ACTION_RESTART_CAMERA));
                    break;
                case Values.ZZX_FACE_RECOGNITION:
                    boolean isOpenFace = intent.getBooleanExtra("zzx_face",false);
                    break;
                case Values.ACTION_Distance_induction:
                    Log.e(TAG, "onReceive: " +"ACTION_Distance_induction");
                    boolean isOpenDistance = intent.getBooleanExtra("zzx_distance",false);
                    if (isOpenDistance){
                        initProximitySensor();
                    }else {
                        if (mProximitySensor!=null){
                            Log.e(TAG, "onReceive: " +(mProximitySensor!=null));
                            mProximitySensor.onDestroy();
                            mProximitySensor=null;
                        }
                    }
                    break;
                case Values.ACTION_RECORD_DEFAULT_VALUE:
                    int resolution = Settings.System.getInt(getContentResolver(),"resolution",-1);
                    switch (resolution){
                        case 0:
                            mBtnResolution.setBackgroundResource(R.drawable.ratio_record_640_480);
                            break;
                        case 1:
                            mBtnResolution.setBackgroundResource(R.drawable.ratio_record_1280_720);
                            break;
                        case 2:
                            mBtnResolution.setBackgroundResource(R.drawable.ratio_record_1920_1080);
                            break;
                    }
                    break;
                case Values.ACTION_PR:
                    boolean isOpen = intent.getBooleanExtra("zzx_state",false);
                    Log.i(TAG, "onReceive: "+isOpen);
                    setIrCutEnable(!isOpen);
                    break;

            }
        }


    }
    private void sendBraod(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }
    private void start(final Context context , final Intent intent){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    context.startActivity(intent);

                } catch (Exception e) {
                    TTSToast.showToast(context, R.string.no_class_find);
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private final static int RECORD = 1;
    private final static int SWITCH = 2;
    private final static int PHONOS = 3;
    private final static int MIC = 4;
    private final static int LED = 5;

    private void getEventSetting(Intent intent, String name, int def, Context context) {
        def = Settings.System.getInt(getContentResolver(), name, def);
        switch (def) {
            case RECORD:
                EventRecord(intent);
                break;
            case SWITCH:
                EventSwitch();
                break;
            case PHONOS:
                EventPhonos();
                break;
            case MIC:
                EventMic(intent, context);
                break;
            case LED:
                EventLed();
                break;
        }
    }
    //按键录像
    private void EventRecord(Intent intent){
        if (!CheckUtils.IsContinuousClick(500)){
            return;
        }
        if (ZZXConfig.isPullout){
            Toast.makeText(CameraService.this,R.string.power_saving_mode,Toast.LENGTH_SHORT).show();
            return;
        }
        if (ZZXConfig.isP5C_HXJ || ZZXConfig.isP5C_HXJ_TKY){
            if (isOccupy){
                Toast.makeText(CameraService.this,R.string.camera_occupy,Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (getUsbCameraShow(CameraService.this)) {
            return;
        }
        if (isContinuousPic){
            return;
        }
        if (!mStorageNotEnough) {
            if (!Values.ACTION_UVC_CONNECT_STATE) {
                if (!mReceiveStartRecordFromClient && isExternalStorageEnabled()) {
                    checkCameraNeedRestart();
                    mVideoLongPress = intent.getBooleanExtra(Values.U_EVENT_LONG_PRESSED_STATE, false);
                    EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.RECORD);
                }
                if (isSoundReecord ) {//一键切换功能，如果正在录音开启录像，停止录音
                    startRecordService(false);

                }

            }
        }
    }
    //按键拍照
    private void EventPhonos(){
        if (!CheckUtils.IsContinuousClick(500)){
            return;
        }
        if (ZZXConfig.isPullout){
            Toast.makeText(CameraService.this,R.string.power_saving_mode,Toast.LENGTH_SHORT).show();
            return;
        }
        if (ZZXConfig.isP5C_HXJ || ZZXConfig.isP5C_HXJ_TKY){
            if (isOccupy){
                Toast.makeText(CameraService.this,R.string.camera_occupy,Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (getUsbCameraShow(CameraService.this)) {
            return;
        }
        takePictureByAction();
    }
    //按键转换
    private void EventSwitch() {
        if (getUsbCameraShow(this)){
            if (!CheckUtils.IsContinuousClick(500)){
                return;
            }
            if(isKeyPic){
                return;
            }
            if (!ZZXConfig.is4GA_TEST){
                if (mIsRecording) {
                    mUserRecording = false;
                    stopRecord();
                }
            }

            /**
             * 1:需要在releaseRealTimeVideo方法前执行
             * 2:通过判断实时上传对象是否为空来确定在切换到外置摄像头时，是否需要重新发送实时上传命令
             * 3.收到外置摄像头开启预览成功后的广播后，发送实时上传命令，只发一次{@link ACTION_UVC_START_PREVIEW_OK}
             * */
            if (mRealTimeTask != null) {
                mIsRepeatSend = true;
            }
            releaseRealTimeVideo(false);
            if (!checkQR()) {
                if (!ZZXConfig.is4GA_TEST){
                    stopRecordAsUserButton();
                }
            }
            Settings.System.putInt(getContentResolver(), Values.OPEN_CAMERA_TYPE, Values.OPEN_USB_CAMERA);
            /**切换为usb摄像头*/
            minWindow();
            if (!ZZXConfig.is4GA_TEST){
                releaseCamera();
            }
            Intent toUVCCamera = new Intent();
            toUVCCamera.setAction(Values.ACTION_ASTERN);
            toUVCCamera.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            toUVCCamera.putExtra(Values.INTENT_TYPE, Values.INTENT_TYPE_VALUE_BUTTON_REVERSE);
            toUVCCamera.putExtra(Values.STATUS, true);
            sendBroadcast(toUVCCamera);
        }else {
            switchCamera();
        }

    }
    //按键录音
    private void EventMic(Intent intent, Context context){
        if (!CheckUtils.IsContinuousClick(500)){
            return;
        }
        if (ZZXConfig.isPullout){
            Toast.makeText(CameraService.this,R.string.power_saving_mode,Toast.LENGTH_SHORT).show();
            return;
        }
        if (getUsbCameraShow(context)) {
            return;
        }
        if (!isExternalStorageEnabled()) {
            return;
        }
        if (!Values.ACTION_UVC_CONNECT_STATE){
            notifyRrecord(Values.LOGCAT_MIC, Values.ZZX_STATE, isSoundReecord);
            if (isVideoReecord) {
                mHandler.sendEmptyMessageDelayed(Values.HANDLER_MAIN_STOP_RECORD_AS_USER, 300);
                // stopRecordAsUserButton();
            }
            boolean longPress = intent.getBooleanExtra(Values.U_EVENT_LONG_PRESSED_STATE, false);
            if (!longPress) {
                if (isExternalStorageEnabled()) {
                    //         mHandler.sendEmptyMessageDelayed(Values.HANDLER_MAIN_STOP_MEDIO,500);
                    updateMicRgbState(intent);
                    startRecordService(false);
                }
            } else {
                if (ZZXConfig.SOSstatus == 0) {
                    List<String> sosList = SOSInfoUtils.getSOSNumList(context);
                    if (sosList != null && sosList.size() > 0) {
                        sendSOSMsg();
                    }
                    sendSOS(context);
                } else if (ZZXConfig.SOSstatus == 1) {
                    Intent intent2 = new Intent();
                    intent2.setAction(Values.ACTION_CALL_SOS_CONTACT);
                    context.sendBroadcast(intent2);
                }
            }

//                    if (isVideoReecord) {
//                        stopRecordAsUserButton();
//                    }
            FastModel.getInstance().setMic(isSoundReecord);
            Log.e("zzx", "mic");
        }
    }
    //按键补光灯
    private void EventLed(){
        //补光灯
        if (isFlashOpened()) {
            setflashEnable(false);
        } else {
            setflashEnable(true);
        }
//        if (!isFlashOpened()) {
//            turnonflash();
//        } else {
//            turnoffflash();
//        }
    }
    private void sendUVCCamera() {
        Intent intent = new Intent();
        intent.setAction(Values.ACTION_ASTERN);
        intent.putExtra(Values.STATUS, false);
        sendBroadcast(intent);
    }
    private void sendBroadcastSP(String action,String path){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("file_path",path);
        sendBroadcast(intent);
    }

    class SOSReportRunnable implements Runnable {
        private VehicleSOSReport mReport;

        public SOSReportRunnable(String uid) {
            VehicleStatus status = new VehicleStatus();
            ProtocolUtils.getVehicleStatus(status, false);
            mReport = new VehicleSOSReport(status.getValue());
            mReport.mVehicleStatus = status;
            mReport.mUID = uid;
            mReport.mSOSNumber = 0;
            mReport.mSOSSource = "key_press";
            mReport.mSOSInfo = "SOS";
        }

        @Override
        public void run() {
            mReport.increase();
            if (MainActivity.mProtocolUtils != null && MainActivity.mProtocolUtils.isSocketCreated()) {
                MainActivity.mProtocolUtils.sendSOS(mReport);
            }
            mHandler.postDelayed(this, 15 * 1000);
        }
    }
    public interface OnCameraTypeChangeListener {
        void cameraTypeChange(@Values.CameraType int type);
    }

    private static OnCameraTypeChangeListener mOnCameraTypeChangeListener;

    public static void setOnCameraTypeChangeListener(OnCameraTypeChangeListener listener) {
        mOnCameraTypeChangeListener = listener;
    }
    /**
     * 在切换摄像头之前，如果正在实时上传，则切换摄像头成功后重新发送实时上传命令
     */
    private void repeatSendRealTimeCommand(@Values.CameraType int type) {
        if (mIsRepeatSend && mOnCameraTypeChangeListener != null) {
            mOnCameraTypeChangeListener.cameraTypeChange(type);
            mIsRepeatSend = false;
        }
    }
    public static final String USB_CAMERA_LINK_STATE = "usb_camera_link_state";
    public static final int USB_CAMERA_LINKED = 1;
    public static final int USB_CAMERA_NO_LINK = 0;
    public static final String CAMERA_SERVICE_WINDOW_SIZE = "camera_service_window_size";
    public static final int WINDOW_SIZE_MIN = 0;
    public static final int WINDOW_SIZE_MAX = 1;

    public static boolean getUsbCameraShow(Context context) {
        //usb摄像头开启的时候，录像和拍照的操作在Usb摄像头应用中操作
        /*return (Settings.System.getInt(context.getContentResolver(),
                USB_CAMERA_LINK_STATE, USB_CAMERA_NO_LINK) == USB_CAMERA_LINKED)
                && (Settings.System.getInt(context.getContentResolver(),
                CAMERA_SERVICE_WINDOW_SIZE, WINDOW_SIZE_MIN) == WINDOW_SIZE_MAX);*/
        /*return (Settings.System.getInt(context.getContentResolver(),
                USB_CAMERA_LINK_STATE, USB_CAMERA_NO_LINK) == USB_CAMERA_LINKED)
                && (Settings.System.getInt(context.getContentResolver(),
                Values.OPEN_CAMERA_TYPE, Values.OPEN_MAIN_CAMERA) == Values.OPEN_USB_CAMERA);*/

        return (Settings.System.getInt(context.getContentResolver(),
                Values.OPEN_CAMERA_TYPE,
                Values.OPEN_MAIN_CAMERA) ==
                Values.OPEN_USB_CAMERA);
    }
    public static boolean getVideoformat(Context context){
        return Settings.System.getInt(context.getContentResolver(),"video_format",-1)==1;
    }
}
