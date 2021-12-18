package com.zzx.police;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.location.locationApplaction;
import com.zzx.police.activity.PasswordActivity;
import com.zzx.police.apn.ApnConfig;
import com.zzx.police.apn.ApnInfo;
import com.zzx.police.data.ApkFileInfo;
import com.zzx.police.data.FTPDownLoadInfo;
import com.zzx.police.data.IntentInfo;
import com.zzx.police.data.StoragePathConfig;
import com.zzx.police.data.Values;
import com.zzx.police.fragment.CheckFragment;
import com.zzx.police.fragment.LauncherFragment;
import com.zzx.police.fragment.LoginFragment;
import com.zzx.police.fragment.P5XSZLJPasswordFragment;
import com.zzx.police.fragment.PasswordFragment;
import com.zzx.police.manager.RecordManager;
import com.zzx.police.manager.UsbMassManager;
import com.zzx.police.services.CameraService;
import com.zzx.police.services.FTPDownService;
import com.zzx.police.services.FTPService;
import com.zzx.police.services.HttpServer;
import com.zzx.police.utils.ConfigXMLParser;
import com.zzx.police.utils.DeleteFileUtil;
import com.zzx.police.utils.DeviceUtils;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.utils.FileUtil;
import com.zzx.police.utils.FileUtils;
import com.zzx.police.utils.GpsConverter;
import com.zzx.police.utils.LogUtil;
import com.zzx.police.utils.NetWorkUtil;
import com.zzx.police.utils.PackageUtils;
import com.zzx.police.utils.PcSocketNewUtils;
import com.zzx.police.utils.ProtocolUtils;
import com.zzx.police.utils.RemotePreferences;
import com.zzx.police.utils.SystemUtil;
import com.zzx.police.utils.SystemValuesUtils;
import com.zzx.police.utils.TTSToast;
import com.zzx.police.utils.XMLParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_DISCHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
import static android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
import static com.zzx.police.ZZXConfig.isShowDisk;
import static com.zzx.police.ZZXConfig.mVideoCut;
import static com.zzx.police.data.Values.SETTING_VEHICLE;
import static com.zzx.police.services.CameraService.LIGHT_COLOR_GREEN_NO_FLASH;
import static com.zzx.police.utils.ProtocolUtils.KEY_FTP_DOWN_INFO_LIST;


public class MainActivity extends Activity implements LocationListener {
    public static final String VERSION_MID = "ZZXVersionMid";
    public static final String VERSION_PRE = "ZZXVersionPre";
    /**
     * 判斷是否打開數據流量.
     **/
    public static final String MOBILE_DATA = "ZZXMobileData";
    /**
     * 判斷連接電腦后是否自動打開TF卡,不是則顯示密碼框.
     **/
    public static final String AUTO_CONNECT = "ZZXAutoConnect";
    /**
     * 表示视频切割的时间间隔.单位分钟.
     */
    public static final int CUT_VIDEO_DURATION_IN_MIN = 1;
    /**
     * 表示录像大于此时长才需要切割.
     */
    public static final int CUT_VIDEO_RECORD_TIME = CUT_VIDEO_DURATION_IN_MIN * 60;
    public static final String CLASS_SMS_MANAGER = "com.android.internal.telephony.SmsApplication";
    public static final String METHOD_SET_DEFAULT = "setDefaultApplication";
    private static final String TAG = "MainActivity: ";
    /**
     * 默认是否弹窗 -1 为弹窗设置密码, 1 为直接打开.
     **/
    private static final int AUTO_CONNECT_ENABLED = 1;
    private static final boolean DEVELOPER_MODE = true;
    private static final int MAIN_START_RECORD = 3;
    private static final int MAIN_SHUT_DOWN = 2;
    private static final int MAIN_OPEN_AIR_PLANE_MODE = 4;
    private static final int MAIN_CLOSE_AIR_PLANE_MODE = 5;
    /**
     * 判断是否使用新后台
     **/
    public static boolean mIsUseSDK = false;
    public static int battery = 0;
    public static ProtocolUtils mProtocolUtils = null;
    public static ZZXClient mZzxClient;
    public static Location mLocation = null;
    public static Resources mRes;
    public static boolean mConnected = false;
    public static boolean isStorageNotEnought = false;
    public static boolean DEBUG = true;
    public static boolean mUsbConnected = false; //usb 插入拔出状态

    static {
        if (ZZXConfig.isG726) {
            try {
                System.loadLibrary("g726_codec");
            } catch (Error e) {
                e.printStackTrace();
            }
        }
    }

    private final String POLICE_NUM = "police_num";
    private final String DEVICE_NUM = "device_num";
    private final String PRISONER_NUM = "prisoner_num";
    private final String DATE_TIME = "datetime";
    public int mStorageState = 0;
    //表明该应用是否是Launcher,若不是则返回键可结束自己.
    private BatteryReceiver mReceiver = null;
    private MediaReceiver mMediaReceiver;
    private LocationManager mLocManager = null;
    private Location mPreLocation;
    private Handler mHandler = null;
    private Handler mEventHandler = null;
    private Intent mEventIntent = null;
    private Intent mCameraIntent;
    private boolean mIsFirst = true;
    private boolean mMountFirst = true;
    private LoginRunnable mLoginRunnable;
    private Intent mFtpIntent;
    private boolean mLogUpdate = false;
    private ContentObserver mObserver;
    private ContentObserver mUSBObserver;
    private ProgressDialog mInstallProgressDialog;
    public static final int PACKAGE_DELETE = 5580;
    public static final int INSTALL_APK_FILE = 5576;
    public static final int READ_APK_INSTALL_LIST = 5575;
    private static final String FIRST_TIME = "ZZXInit";
    private XMLParser.InstallInfo mInstallInfo;
    /**
     * 代表Apk安装XML文件版本
     **/
    private static final String INSTALL_XML_VERSION = "zzxXmlVersion";
    /**
     * APK安装索引
     **/
    private int mInstallIndex = 0;
    /**
     * 卸载包名索引
     **/
    private int mUninstallIndex = 0;
    private PackageInstallObserver mInstallObserver;
    private PackageDeleteObserver mUninstallObserver;
    private List<PackageInfo> mInstallPackageList;

    // public static LogUtil mLogUtil;
    //计算Back键次数,两次后退出.
    private int mBackCount = 0;
    private Intent mHttpServer;
    private HandlerThread mHandlerThread;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager mPowerManager;
    private boolean mWakeLocked = false;
    private boolean mIsStop = false;
    private boolean mNeedShowPassword = false;
    private boolean mPasswordShowed = false;
    private Handler mMainHandler;
    private int lastIRcut = 0;
    private int lastscreenMode = 0;
    private RecordManager mRecordManager;
    private PcSocketNewUtils mPcSocketNew;
    private boolean isshow = false;
    private UsbMassManager mUsbMassManager;
    private long mStartOpenUsb = 0;
    private int mReopenCount = 0;

    public void checkDebugLog() {
        DEBUG = Settings.System.getInt(getContentResolver(), "zzxDebug", -1) == 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBusUtils.registerEvent(Values.BUS_EVENT_RESET_SOCKET, this);
        EventBusUtils.registerEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, this);
        EventBusUtils.registerEvent(Values.BUS_EVENT_SOCKET_CONNECT, this);
        EventBusUtils.registerEvent(Values.BUS_EVENT_MAIN_ACTIVITY, this);
        setContentView(R.layout.activity_main);
        ZZXConfig zzxConfig = new ZZXConfig(getApplicationContext());
        if (SystemUtil.gpsIsOpen(this)){
            SystemUtil.writeToDevice(1,"/sys/devices/platform/zzx-misc/beidou_stats");
        }else {
            SystemUtil.writeToDevice(0,"/sys/devices/platform/zzx-misc/beidou_stats");
        }
        clearData();
        mIsUseSDK = Settings.System.getInt(getContentResolver(), ConfigXMLParser.CFG_ENABLED_SDK, -1) == 1;
        String userName = DeviceUtils.getPoliceNum(this);
        //    mVideoCut = Settings.System.getInt(getContentResolver(), ConfigXMLParser.CFG_ENABLED_VIDEO_CUT, -1) == 1;
        Values.LOG_E(TAG, "onCreate().userName = " + userName + "; mVideoCut = " + mVideoCut);
        checkDebugLog();
        initReceiver();
        mRes = getResources();
        mHandlerThread = new HandlerThread("io_handler");
        mHandlerThread.start();
        mEventHandler = new EventHandler();
        mHandler = new AsyncHandler(new WeakReference<>(this), mHandlerThread.getLooper());
        //mHandler.sendEmptyMessageDelayed(Values.CHECK_GPS_CHANGED, 5000);
        mMainHandler = new MainHandler();
        mObserver = new ServiceObserver(mHandler);
        try {
            getContentResolver().registerContentObserver(Settings.System.getUriFor(Values.SERVICE_IP_ADDRESS), true, mObserver);
            getContentResolver().registerContentObserver(Settings.System.getUriFor(Values.POLICE_NUMBER), true, mObserver);
            if (mVideoCut) {
                mUSBObserver = new USBObserver(mHandler);
                getContentResolver().registerContentObserver(Settings.System.getUriFor(Values.VIDEO_CUT), true, mUSBObserver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // setDefaultSms();
        sendMessage(new StorageRunnable(), 0);
//        try {
//            Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
//            Settings.Secure.putInt(getContentResolver(), LocationManager.GPS_PROVIDER, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        mLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, 0, this);
        TTSToast.init(getApplicationContext());
        if (mEventIntent == null) {
            mEventIntent = new Intent();
        }
        if (checkQRWifi()) {
            startHttpServer();
        }
        checkMobileData();
        if (!ZZXConfig.isP5X_SZLJ){
            startCameraService();
        }
//        startGpsServer();
        //  mEventHandler.sendEmptyMessageDelayed(1, 1500);
        checkEpoNeedDownload();
        mEventIntent.setAction(Values.ACTION_LOGIN_STATE).putExtra("state", 0);
        sendBroadcast(mEventIntent);
        if (!PcSocketNewUtils.isPcSocketCreated())
            startPcSocket();
        if (savedInstanceState == null) {
            if (ZZXConfig.isP5X_SZLJ){
                getFragmentManager().beginTransaction().add(R.id.container, Fragment.instantiate(this, P5XSZLJPasswordFragment.class.getName())).commit();
            }else {
                getFragmentManager().beginTransaction().add(R.id.container, Fragment.instantiate(this, LauncherFragment.class.getName())).commit();
            }
        }
        if (!userName.equals(Values.POLICE_DEFAULT_NUM)) {
            EventBusUtils.postEvent(Values.BUS_EVENT_RESET_SOCKET, Values.SOCKET_RECONNECTION);
        }
//        	 SetMaxVolume();
        //  SetLaserStatus();
//        SetAutoIrCut();
        InitFactoryReset();
//        showPasswordFragment();
        if (ZZXConfig.IsReSetAirPlaneMode) {
            mEventHandler.sendEmptyMessageDelayed(MAIN_OPEN_AIR_PLANE_MODE, 150 * 1000);
            //   mEventHandler.sendEmptyMessageDelayed(MAIN_CLOSE_AIR_PLANE_MODE, 5000);
            //  new ReSetNetWorkThread().start();
        }
        if (ZZXConfig.IsSetDefultAPN) {
            try {
                ApnInfo apnInfo = new ApnInfo();
                apnInfo.setApn("public.vpdn");
                apnInfo.setName("public.vpdn");
                apnInfo.setUser("jjzd001@pzhsgajtjczd.vpdn.sc");
                apnInfo.setPassword("123456");
                apnInfo.setAuthenticationtype(1);
                apnInfo.setType("default");
                apnInfo.setMcc("460");
                apnInfo.setMnc("11");
                apnInfo.setProtocol("IPV4V6");
                apnInfo.setRoaming("IPV4V6");
                ApnConfig apnConfig = new ApnConfig(getApplicationContext());
                apnConfig.setApnUse(apnInfo);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (ZZXConfig.isP5XJNKYD){
            Settings.System.putInt(getContentResolver(),"ZZXIsHideLcons",1);
            Settings.System.putInt(getContentResolver(),CameraService.REPORT_QUALITY,1);//实时视频默认高
            try{
                ApnInfo apnInfo= new ApnInfo();
                apnInfo.setName("unim2m.njm2mapn");
                apnInfo.setApn("unim2m.njm2mapn");
                apnInfo.setMcc("460");
                apnInfo.setMnc("06");
                apnInfo.setCurrent("1");
                apnInfo.setType("default");
                ApnConfig apnConfig = new ApnConfig(getApplicationContext());
                apnConfig.setApnUse(apnInfo);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (ZZXConfig.is4GA_TEST){
            //0为固定帧率30帧，1为动态帧率
            writeNumber("0", "/sys/devices/platform/zzx-misc/camera_fps_mode");
        }
        mEventHandler.sendEmptyMessageDelayed(MAIN_START_RECORD, 5000);//检测是否有开机录像广播
        if (ZZXConfig.IsBrazilVerion) {
            ZZXConfig.isOpenPassActivity = false;
        }
        if (ZZXConfig.isOpenPassActivity) {
            if (!(Settings.System.getInt(getContentResolver(), SETTING_VEHICLE, 0) == 1)) {//车载模式下不用输入密码
                StartPassWordActivity();
            }
        }
        checkNeedStartFtpDownService();
        deleteaiwinn();
        SystemUtil systemUtil =new SystemUtil(this);
        LogUtil.init(true);
        if (ZZXConfig.is4GA_CDHN || ZZXConfig.isP5C_CDHN || ZZXConfig.isP5X_CDHN){
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: " );
        if (ZZXConfig.is4GA_CDHN || ZZXConfig.isP5C_CDHN || ZZXConfig.isP5X_CDHN){
            moveTaskToBack(true);
        }
    }

    @SuppressWarnings("all")
    private void checkNeedStartFtpDownService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LinkedHashMap<String, FTPDownLoadInfo> linkMap = FTPDownLoadInfo.getLinkHashMapFromFiles(MainActivity.this, FTPDownLoadInfo.SAVE_FILE_NAME);
                if (linkMap != null && linkMap.size() != 0) {
                    ArrayList<FTPDownLoadInfo> list = new ArrayList<>();
                    Iterator<Map.Entry<String, FTPDownLoadInfo>> iterator = linkMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        list.add(iterator.next().getValue());
                    }
                    Intent intent = new Intent(MainActivity.this, FTPDownService.class);
                    intent.putParcelableArrayListExtra(KEY_FTP_DOWN_INFO_LIST, list);
                    startService(intent);
                }
            }
        }).start();
    }
    private void startRecord() {
        if (mRecordManager.needBootRecord() || Settings.System.getInt(getContentResolver(), SETTING_VEHICLE, 0) == 1) {
            Toast.makeText(getApplicationContext(), "a", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction("zzx_action_record");
            this.sendBroadcast(intent);
            //      Intent.ACTION_BATTERY_CHANGED
            Intent MaxwindowIntent = new Intent();
            MaxwindowIntent.setAction("zzx_action_Max_window");
            this.sendBroadcast(MaxwindowIntent);
        }
    }

    private void StartPassWordActivity() {
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
    }

    private void SetAutoIrCut() {
        try {
            lastIRcut = Settings.System.getInt(getApplicationContext().getContentResolver(), Values.AUTO_IrCut);
            lastscreenMode = Settings.System.getInt(getApplicationContext().getContentResolver(), Values.AUTO_LIGHT);
            controllDevice(lastIRcut, Values.IrCutfilePath);
            controllDevice(lastscreenMode, Values.BrightfilePath);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void SetLaserStatus() {
        SharedPreferences preferences = this.getSharedPreferences("ledstate", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("laserState", true);
        editor.commit();
    }

    /**
     * 验证Usb是否连接后自动打开.
     */
    private void checkUsbAutoOpen() {
        if (Settings.System.getInt(getContentResolver(), AUTO_CONNECT, -2) == -2) {
            boolean usbAutoOpen = SystemUtil.getSystemProperties(SystemUtil.PROPERTIES_FIELD_USB_AUTO).equals("1");
            Settings.System.putInt(getContentResolver(), AUTO_CONNECT, usbAutoOpen ? 1 : -1);
        }
        if (Settings.System.getInt(getContentResolver(), CameraService.RECORD_RESTART, -2) == -2) {
            boolean reportRecord = SystemUtil.getSystemProperties(SystemUtil.PROPERTIES_FIELD_RECORD_REPORT).equals("1");
            Settings.System.putInt(getContentResolver(), CameraService.RECORD_RESTART, reportRecord ? 1 : -1);
        }
    }

    private void parserConfig() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                initConfig();
            }
        }.start();
    }

    /**
     * 读取TF卡内的配置文件初始化功能配置.
     */
    private void initConfig() {

        String versionPre = Settings.System.getString(getContentResolver(), VERSION_PRE);
        if (versionPre == null || versionPre.equals("")) {
            Settings.System.putString(getContentResolver(), VERSION_PRE, SystemUtil.getSystemProperties(SystemUtil.PROPERTIES_FIELD_VERSION_PRE));
        }
        String versionMid = Settings.System.getString(getContentResolver(), VERSION_MID);
        if (versionMid == null || versionMid.equals("")) {
            Settings.System.putString(getContentResolver(), VERSION_MID, SystemUtil.getSystemProperties(SystemUtil.PROPERTIES_FIELD_VERSION_MID));
        }

        File file = new File(Values.FILE_CONFIG_INIT);
        if (!file.exists() || file.isDirectory())
            return;
        ConfigXMLParser parser = new ConfigXMLParser(this);
        parser.parserXMLFile(Values.FILE_CONFIG_INIT);
    }

    /**
     * 判断是否打开移动网络
     */
    private void checkMobileData() {
        if (Settings.System.getInt(getContentResolver(), MOBILE_DATA, 1) != 1) {
            SystemUtil.setMobileData(this, false);
        }
    }

    private boolean checkLauncher() {
        return SystemUtil.getDefaultLauncherPackage(this).equals("com.zzx.police");
    }

    /**
     * 为警务通增加判断是否使用扫描二维码即可判断为是否是警务通应用,是则开机录像.
     */
    private boolean checkQRWifi() {
        return Settings.System.getInt(getContentResolver(), CameraService.WIFI_SCAN, CameraService.WIFI_SCAN_DISABLED) == CameraService.WIFI_SCAN_ENABLED;
    }

    private void setDefaultSms() {
        try {
            Class<?> smsClass = Class.forName(CLASS_SMS_MANAGER);
            Method method = smsClass.getMethod(METHOD_SET_DEFAULT, String.class, Context.class);
            method.invoke(null, getPackageName(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startHttpServer() {
        if (mHttpServer == null)
            mHttpServer = new Intent();
        mHttpServer.setClass(this, HttpServer.class);
        startService(mHttpServer);
    }


    private void checkEpoNeedDownload() {
          /*if (!epoFileExist()) {
              EventBusUtils.postEvent(Values.BUS_EVENT_RESET_SOCKET, Values.DOWNLOAD_EPO);
          }*/
        EventBusUtils.postEvent(Values.BUS_EVENT_RESET_SOCKET, Values.CHECK_NEED_UPLOAD_LOG);
    }

    private void startDownloadEpoService() {
        try {
            Intent epoIntent = new Intent();
            epoIntent.setClassName("com.zzx.setting", "com.zzx.setting.epo.EpoDownloadService");
            epoIntent.setAction(Values.ACTION_START_DOWN);
            epoIntent.putExtra(Values.REMOTE_PATH, Values.REMOTE_PATH_VALUES);
            epoIntent.putExtra(Values.FILENAME, Values.FILENAME_VALUES);
            epoIntent.putExtra(Values.LOCAL_PATH, Values.LOCAL_PATH_VALUES);
            epoIntent.putExtra(Values.EPO_FTP_HOST, Values.EPO_FTP_HOST_VALUE);
            epoIntent.putExtra(Values.EPO_FTP_PASSWORD, Values.EPO_FTP_PASSWORD_VALUE);
            epoIntent.putExtra(Values.EPO_FTP_USER_NAME, Values.EPO_FTP_USER_NAME_VALUE);
            epoIntent.putExtra(Values.EPO_FTP_PORT, Values.EPO_FTP_PORT_VALUE);
            startService(epoIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean epoFileExist() {
        File epoFile = new File(Values.EPO_PATH);
        return epoFile.exists();
    }

    private void initReceiver() {
        IntentFilter batteryIntent = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryIntent.addAction(UsbMassManager.ACTION_USB_STATE);
        //        batteryIntent.addAction(MainApplication.ACTION_REPORT_BARCODE);
        batteryIntent.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        batteryIntent.addAction(Values.ACTION_SETTING_OPEN_USB);
        mReceiver = new BatteryReceiver();
        registerReceiver(mReceiver, batteryIntent);
        // IntentFilter filter1 = new IntentFilter("zzx_test");
        // registerReceiver(mReceiver, filter1);
        mMediaReceiver = new MediaReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mMediaReceiver, filter);
    }

    private void startPcSocket() {
        //   new PcSocketUtils(this).startSocket();
        this.mPcSocketNew = new PcSocketNewUtils(this);
    }

    private void writeInfo() {
        String SerialNum = DeviceUtils.getSerialName(MainActivity.this);
        String IDcodeNum = DeviceUtils.getIDCodeName(MainActivity.this);
        String deviceNum = DeviceUtils.getDeviceNum(MainActivity.this);
        String policeNum = DeviceUtils.getPoliceNum(MainActivity.this);
        String userNum = DeviceUtils.getUserNum(MainActivity.this);
        String modelNum = DeviceUtils.getModelName(MainActivity.this);
        if (ZZXConfig.IsShowWaterMark) {
            if (ZZXConfig.isP5X_SZLJ){
                writeNumber(deviceNum + "  " + userNum+"  " + policeNum , Values.PATH_POLICE_NUMBER);
            }else {
                writeNumber(deviceNum + "  " + policeNum + " " + SerialNum, Values.PATH_POLICE_NUMBER);
            }

        } else {
            writeNumber(" ", Values.PATH_POLICE_NUMBER);
        }
    }

    private void writeinitInfo(String path, String text) {
        File paramFile = new File(path);
        if (!paramFile.exists()) {
            FileWriter writer = null;
            try {
                writer = new FileWriter(path, false);
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

    private void checkNeedUpdateInit() {
        boolean isCheck;
        if (ZZXConfig.IsCheckExternalStorage) {
            isCheck = Environment.isExternalStorageRemovable() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } else {
            isCheck = true;
        }

        if (isCheck) {
            String policeNum = DeviceUtils.getPoliceNum(MainActivity.this);
            String deviceNum = DeviceUtils.getDeviceNum(MainActivity.this);
            String text = "police_num=" + policeNum + "\ndevice_num=" + deviceNum;
                writeinitInfo(Values.INI_PATH, text);
        }
    }

    private void writeGpsInfo() {

        String gpsInfo = "Lon: 00; Lat: 0";
        if (ZZXConfig.IsBaidu) {
            mLocation = locationApplaction.getLoation();
        }
        if (mLocation != null) {
            double longitude = mLocation.getLongitude();
            double latitude = mLocation.getLatitude();
            // gpsInfo = "Lon: " + GpsConverter.getGpsString(longitude) + "; " + "Lat: " + GpsConverter.getGpsString(latitude);
            gpsInfo = "Lon: " + GpsConverter.convertGPS(longitude + "", "000.000000", 6) + "; " + "Lat: " + GpsConverter.convertGPS(latitude + "", "00.000000", 6);
        }
//        if (mLocation != null){
//            sendAEPMessage(this,mLocation);
//        }
        if (ZZXConfig.IsShowWaterMark ) {
            writeNumber(gpsInfo, Values.PATH_GPS_INFO);
        } else {
            writeNumber(" ", Values.PATH_GPS_INFO);
        }
        if (ZZXConfig.isP5C_HLN){
            writeNumber(" ", Values.PATH_GPS_INFO);
        }

    }

    private static void sendAEPMessage(final Context context, Location mLocation) {
        if (mLocation != null) {
            SystemValuesUtils.setSystemLongitude((float) mLocation.getLongitude(), context);
            SystemValuesUtils.setSystemLatitude((float) mLocation.getLatitude(), context);
            SystemValuesUtils.setSystemAltitude((float) mLocation.getAltitude(), context);
            SystemValuesUtils.setSystemSpeed((int) mLocation.getSpeed(), context);

            File file = new File("/sys/bus/platform/devices/battery/Auxadc_Test");
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                byte[] bytes = new byte[fileInputStream.available()];
                fileInputStream.read(bytes);
                String text = new String(bytes);
                float Voltage=Float.parseFloat(text)/1000.00f;
                SystemValuesUtils.setSystemVoltage(Voltage,context);
            } catch (Exception e) {
                e.printStackTrace();
            }


            Intent intent = new Intent();
            intent.setAction("zzx_action_aep_data_upload");
            intent.setComponent(new ComponentName
                    ("com.zzx.aephttp",
                            "com.zzx.aephttp.AepReceiver"));
            context.sendBroadcast(intent);

            new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent intent2 = new Intent();
                    intent2.setAction("zzx_action_aep_signal_upload");
                    intent2.setComponent(new ComponentName
                            ("com.zzx.aephttp",
                                    "com.zzx.aephttp.AepReceiver"));
                    context.sendBroadcast(intent2);
                }
            }.start();

        }
    }
    private void writeNumber(String num, final String path) {
        File file = new File(path);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = num.getBytes();
            outputStream.write(buffer, 0, buffer.length);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert outputStream != null;
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!ZZXConfig.IsBaidu)
            mLocation = location;
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

    private void DelayedCloseDevice() {
        //停止录像5秒后关闭设备
        mEventHandler.sendEmptyMessageDelayed(MAIN_SHUT_DOWN, 30 * 1000);
    }

    private void NotifatyStopRecord() {
        //通知停止录像
        EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.VIDEO_STOP_RECORD);
    }

    private void ShutDown() {
        try {
//            Toast.makeText(getApplication(), "2", Toast.LENGTH_SHORT).show();
            //获得ServiceManager类
            Class<?> ServiceManager = Class
                    .forName("android.os.ServiceManager");

            //获得ServiceManager的getService方法
            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);

            //调用getService获取RemoteService
            Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);

            //获得IPowerManager.Stub类
            Class<?> cStub = Class
                    .forName("android.os.IPowerManager$Stub");
            //获得asInterface方法
            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
            //调用asInterface方法获取IPowerManager对象
            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
            //获得shutdown()方法
            Method shutdown = oIPowerManager.getClass().getMethod("shutdown", boolean.class, boolean.class);
            //调用shutdown()方法
            shutdown.invoke(oIPowerManager, false, true);

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    private void dismissPasswordFragment() {
//        TTSToast.showToast(this, "mPasswordShowed = " + mPasswordShowed);
        try {
            if (mPasswordShowed) {
                if (isShowDisk) {
                    getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPasswordShowed = false;
    }

    /**
     * 判断是否自动打开USB.
     */
    private void checkNeedOpenUsb() {
        Log.e(TAG, "checkNeedOpenUsb: " + Settings.System.getInt(getContentResolver(), AUTO_CONNECT, 0));
//        if (ZZXConfig.is4GC || ZZXConfig.isP5X_SZLJ){
        if ( ZZXConfig.isP5X_SZLJ){
            Settings.System.putInt(getContentResolver(),AUTO_CONNECT,-1);
            isShowDisk = true;
        }
        //需要弹出密码框，修改下面的判断值以及  isShowDisk 默认值
        if (Settings.System.getInt(getContentResolver(), AUTO_CONNECT, 1) == 1) {
            if (CameraService.mRecordTime <= CUT_VIDEO_RECORD_TIME && Settings.System.getInt(this.getContentResolver(), Values.VIDEO_CUT, 0) == 0) {
//                openUsb();
                mMainHandler.removeMessages(Values.OPEN_USB);
                mMainHandler.sendEmptyMessageDelayed(Values.OPEN_USB, 5000);
            }
            Values.LOG_E(TAG, "USB " + "just open");
        } else {
            if (mIsStop) {
                goToHome();
                mNeedShowPassword = true;
            } else {
                showPasswordFragment();
            }
        }
    }

    private void showPasswordFragment() {
        int i = Settings.System.getInt(getContentResolver(),"showPassword",0);
        try {
            mPasswordShowed = true;
            if (isShowDisk) {
                if (i!=1){
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, Fragment.instantiate(this, PasswordFragment.class.getName()))
                            .addToBackStack(PasswordFragment.class.getName())
                            .commit();
                    Settings.System.putInt(getContentResolver(),"showPassword",1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(homeIntent);
    }

    public void setAirplaneModeOn(boolean enabling) {
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                    enabling ? 1 : 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra("state", enabling);
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getAirplaneMode() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }


    private void readH9Init() {
        readInitFile(Values.INI_PATH, "H9Init");
    }

    private void readInitFile(String path, String name) {
        BufferedReader reader = null;
        try {
            File file = new File(path);
            if (!file.exists() || file.isDirectory()) {
                return;
            }
            reader = new BufferedReader(new FileReader(file));
            String msg = "";
            String temp;
            while ((temp = reader.readLine()) != null) {
                msg=msg+temp+"\r\n";
            }
            Settings.System.putString(getContentResolver(), name, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void readInit() {
        BufferedReader reader = null;
        try {
            File file = new File(Values.INI_PATH);
            if (!file.exists() || file.isDirectory()) {
                return;
            }
            reader = new BufferedReader(new FileReader(file));
            String msg;
            while ((msg = reader.readLine()) != null) {
                if (msg.equals("") || msg.equals("\n")) {
                    continue;
                }
                String[] results = msg.split("=");
                if (results.length < 2) {
                    continue;
                }
                String data = results[1];
                switch (results[0]) {
                    case POLICE_NUM:
                        DeviceUtils.writePoliceNum(this, data);
                        break;
                    case DEVICE_NUM:
//                        Settings.System.putString(getContentResolver(), Values.DEVICE_NUMBER, data.substring(data.length() - 5));
                        DeviceUtils.writeDeviceNum(this, data.substring(data.length() - 5));
                        break;
                    case DATE_TIME:
                        writeTime(data);
                        break;
                    case PRISONER_NUM:
                        DeviceUtils.writePrisonerNum(this, data);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeTime(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss", Locale.getDefault());
            SystemClock.setCurrentTimeMillis(format.parse(time).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onlyOpenUsb() {
        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORAGE_NOT_ENOUGH);
        Intent mStorageIntent = new Intent(Values.ACTION_STORAGE_STATE);
        mStorageIntent.putExtra("state", -1);
        sendBroadcast(mStorageIntent);
        UsbMassManager massManager = new UsbMassManager(this);
        massManager.enableUsbMass();
    }

    private void openUsb() {
        if (isShowDisk) {
            onlyOpenUsb();
        }
    }

    private void deleteAndroidDir() {
        File file = new File("/storage/sdcard0/Android");
        deleteFile(file);
//          file = new File("/storage/sdcard1/Android");
//          deleteFile(file);
    }

    private void deleteFile(File file) {
        if (!file.exists())
            return;
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList == null) {
                file.delete();
                return;
            }
            if (fileList.length == 0) {
                file.delete();
                return;
            }
            for (File file1 : fileList) {
                deleteFile(file1);
            }
            file.delete();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsStop = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsStop = false;
        if (mNeedShowPassword) {
            showPasswordFragment();
        }
        mNeedShowPassword = false;
    }

    @Override
    protected void onDestroy() {
        Values.LOG_I(TAG, "onDestroy()");
        TTSToast.release();
        mHandlerThread.quit();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mMediaReceiver);
        if (mHttpServer != null)
            stopService(mHttpServer);
          /*if (mUSBObserver != null) {
              getContentResolver().unregisterContentObserver(mUSBObserver);
          }*/
        if (mProtocolUtils != null) {
            mProtocolUtils.release();
            mProtocolUtils = null;
        }
        if (mCameraIntent != null)
            stopService(mCameraIntent);
        mHandler = null;
        mRes = null;
        EventBusUtils.unregisterEvent(Values.BUS_EVENT_RESET_SOCKET, this);
        EventBusUtils.unregisterEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, this);
        EventBusUtils.unregisterEvent(Values.BUS_EVENT_SOCKET_CONNECT, this);
        EventBusUtils.unregisterEvent(Values.BUS_EVENT_MAIN_ACTIVITY, this);
        EventBusUtils.release();
        mLocManager.removeUpdates(this);
        mLocManager = null;
        getContentResolver().unregisterContentObserver(mObserver);
        if (mPcSocketNew != null) {
            mPcSocketNew.Close();
        }
        super.onDestroy();
    }

    public void onEvent(Integer what) {
        switch (what) {
            case Values.REPLACE_LOGIN_FRAGMENT:
                getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(this, LoginFragment.class.getName())).commit();
                break;
            case Values.REPLACE_PASSWORD_FRAGMENT:
                getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(this, PasswordFragment.class.getName())).commit();
                break;
            case Values.REPLACE_CAMERA_SETTING_FRAGMENT:
                //                getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(this, PictureModeSettingFragment.class.getName())).addToBackStack(null).commit();
                break;
            case Values.START_CAMERA_SERVICE:
//                  startCameraService();
                break;
            case Values.REPLACE_LAUNCHER_FRAGMENT:
                getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(this, LauncherFragment.class.getName())).commit();
                break;
            case Values.REPLACE_CHECK_FRAGMENT:
                getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(this, CheckFragment.class.getName())).addToBackStack(null).commit();
                break;
        }
    }

    public void onEventMainThread(Integer what) {
        switch (what) {
            case Values.SERVICE_REGISTER_SUCCESS:
                writeInfo();
                break;
            case Values.CONFIG_FINISEDH:
                dismissInstallDialog();
                TTSToast.showToast(this, R.string.config_finished);
                finish();
                break;
            case Values.CONFIG_START:
//				  TTSToast.showToast(this, R.string.config_start, Toast.LENGTH_LONG);
                showProgressDialog(false, R.string.config_start);
                break;
            case Values.CONFIG_ERROR:
                showProgressDialog(true, R.string.config_error);
                break;
            case Values.MAIN_ONLY_USB:
                Log.d("zzx", "open_usb");
                if (mUsbConnected) {
                    Log.d("zzx", "open_usb_finish");
                    onlyOpenUsb();
                }
                break;
        }
    }

    public void onEventBackgroundThread(Integer what) {
        switch (what) {
            case Values.SOCKET_CONNECTED:
                if (mProtocolUtils == null) {
                    return;
                }
                MainActivity.mProtocolUtils.startReceive();
                String policeNum = DeviceUtils.getPoliceNum(this);
                MainActivity.mProtocolUtils.registerService(policeNum, "");
                break;
            case Values.SOCKET_RECONNECTION:
                if (mProtocolUtils != null) {
                    mProtocolUtils.release();
                    mProtocolUtils = null;
                }
                releaseWakeLock();
                if (mLoginRunnable == null)
                    mLoginRunnable = new LoginRunnable();
                if (mHandler != null)
                    mHandler.removeCallbacks(mLoginRunnable);
                sendMessage(mLoginRunnable, 8 * 1000);
                break;
            case Values.DOWNLOAD_EPO:
                sendMessage(new EpoDownloadRunnable(), 3 * 1000);
                break;
            case Values.CHECK_NEED_UPLOAD_LOG:
                checkNeedUploadLog();
                break;
        }
    }

    private void checkNeedUploadLog() {
        String policeNumber = DeviceUtils.getPoliceNum(MainActivity.this);
        if (policeNumber.equals(Values.POLICE_DEFAULT_NUM)) {
            return;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SimpleDateFormat format = new SimpleDateFormat(Values.DATE_FORMAT_CHECK_UPLOAD_LOG, Locale.getDefault());
        String day = format.format(System.currentTimeMillis());
        int currentDay = Integer.parseInt(day);
        int lastDay = sp.getInt(Values.LAST_UPLOAD_LOG_DAY, 0);
        if (lastDay == 0) {
            sp.edit().putInt(Values.LAST_UPLOAD_LOG_DAY, currentDay).apply();
        } else {
            if (currentDay > lastDay) {
                sendMessage(new LogUploadRunnable(String.valueOf(lastDay)), 0);
                sp.edit().putInt(Values.LAST_UPLOAD_LOG_DAY, currentDay).apply();
            }
        }
    }

    private void uploadLog(String currentDay) {
        String policeNumber = DeviceUtils.getPoliceNum(MainActivity.this);
        File file = new File(StoragePathConfig.getLogDirPath(this), policeNumber + "_3_" + currentDay + ".txt");
        if (file.exists()) {
            if (mFtpIntent == null) {
                mFtpIntent = new Intent();
                mFtpIntent.setClass(MainActivity.this, FTPService.class);
                mFtpIntent.setAction(Values.ACTION_UPLOAD_LOG);
            }
            mFtpIntent.putExtra(Values.UPLOAD_FILE_NAME, file.getAbsolutePath());
            mLogUpdate = true;
            startService(mFtpIntent);
        }
    }

    private void acquireWakeLock() {
        Values.LOG_W(TAG, "MainActivity.acquireWakeLock");
        if (mWakeLock == null) {
            if (mPowerManager == null) {
                mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
            }
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        }
        if (!mWakeLocked) {
            mWakeLocked = true;
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        Values.LOG_W(TAG, "MainActivity.releaseWakeLock");
        if (mWakeLock != null && mWakeLocked) {
            mWakeLock.release();
            mWakeLock = null;
        }
        mWakeLocked = false;
    }

    private void sendMessage(int what, int delay) {
        sendMessage(what, null, null, delay);
    }

    private void sendMessage(Message msg, int delay) {
        sendMessage(0, msg, null, delay);
    }

    private void sendMessage(Runnable runnable, int delay) {
        sendMessage(0, null, runnable, delay);
    }

    private void sendMessage(int what, Message msg, Runnable runnable, int delay) {
        if (mHandler == null)
            return;
        if (msg != null) {
            mHandler.sendMessageDelayed(msg, delay);
        } else if (runnable != null) {
            mHandler.postDelayed(runnable, delay);
        } else {
            mHandler.sendEmptyMessageDelayed(what, delay);
        }
    }
    private void removeCallbacks(Runnable runnable){
        if (mHandler == null)
            return;
        if (runnable !=null ){
            mHandler.removeCallbacks(runnable);
        }

    }

    public void onEventBackgroundThread(IntentInfo intentInfo) {
        if (mEventIntent == null) {
            mEventIntent = new Intent();
        }
        if (intentInfo.mAction.equals(Values.ACTION_UPLOAD_STATE)) {
            if (intentInfo.mState == 0 && mLogUpdate) {
                stopService(mFtpIntent);
            }
        }
        mEventIntent.setAction(intentInfo.mAction).putExtra("state", intentInfo.mState);
        Values.LOG_E(TAG, "mAction = " + intentInfo.mAction + "; state = " + intentInfo.mState);
        sendBroadcast(mEventIntent);
    }

    private void startCameraService() {
        if (mCameraIntent == null) {
            mCameraIntent = new Intent();
            mCameraIntent.setClass(this, CameraService.class);
        }
        if (ZZXConfig.is4GA_CDHN || ZZXConfig.isP5C_CDHN || ZZXConfig.isP5X_CDHN){
            mCameraIntent.setAction(Values.ACTION_MIN_WINDOW);
        }else {
            mCameraIntent.setAction(Values.ACTION_MAX_WINDOW);
        }

        mCameraIntent.putExtra(Values.CAMERA_SERVICE_EXTRA_STRING, Values.EXTRA_RECORD);
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startService(mCameraIntent);
            }
        }.start();
    }

    private void clearData() {
        SharedPreferences sp = RemotePreferences.getRemotePreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Values.PIC_MODE).remove(Values.PIC_MODE_QUICK_POSITION).remove(Values.PIC_MODE_QUICK_VALUE)
                .remove(Values.PIC_MODE_INTERVAL_POSITION).remove(Values.PIC_MODE_INTERVAL_VALUE).remove(Values.PIC_MODE_TIMER_POSITION).remove(Values.PIC_MODE_TIMER_VALUE).apply();
    }

    @Override
    public void onBackPressed() {
        Values.LOG_I(TAG, "onBackPressed()");
        if (ZZXConfig.is4GA_CDHN || ZZXConfig.isP5C_CDHN || ZZXConfig.isP5X_CDHN){
            moveTaskToBack(true);
            return;
        }

        if (CameraService.isWindowMax()) {
            minCamera();
            return;
        }
        if (checkLauncher()) {
            getFragmentManager().popBackStackImmediate();
        } else {
            if (getFragmentManager().popBackStackImmediate()) {
                return;
            }
            if (++mBackCount >= 2) {
                super.onBackPressed();
            } else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBackCount = 0;
                    }
                }, 3000);
                TTSToast.showToast(this, R.string.click_one_more_time);
            }
        }
    }

    private void minCamera() {
        if (mCameraIntent == null) {
            mCameraIntent = new Intent();
            mCameraIntent.setClass(this, CameraService.class);
        }
        mCameraIntent.setAction(Values.ACTION_MIN_WINDOW);
        startService(mCameraIntent);
    }

    public void controllDevice(int state, String filePath) {
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

    private void SetMaxVolume() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), AudioManager.FLAG_ALLOW_RINGER_MODES);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FLAG_ALLOW_RINGER_MODES);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_ALLOW_RINGER_MODES);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_ALLOW_RINGER_MODES);
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_ALLOW_RINGER_MODES);
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), AudioManager.FLAG_ALLOW_RINGER_MODES);
        mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF), AudioManager.FLAG_ALLOW_RINGER_MODES);

    }

    static class AsyncHandler extends Handler {
        private WeakReference<MainActivity> mReference;

        AsyncHandler(WeakReference<MainActivity> reference, Looper looper) {
            super(looper);
            mReference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case Values.CHECK_GPS_CHANGED:
                    if (activity.mPreLocation == MainActivity.mLocation) {
                        MainActivity.mLocation = null;
                    } else {
                        activity.mPreLocation = MainActivity.mLocation;
                    }
                    activity.mHandler.sendEmptyMessageDelayed(Values.CHECK_GPS_CHANGED, 5000);
                    break;
                case INSTALL_APK_FILE:
                    int index;
                    index = ++activity.mInstallIndex;
                    activity.installApkFile(index);
                    break;
                case READ_APK_INSTALL_LIST:
                    activity.readInstallList();
                    break;
            }
        }
    }

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
            if (uri.equals(Settings.System.getUriFor(Values.SERVICE_IP_ADDRESS))) {
                /**接收服务器地址更改事件,若当前已有警号则发送重新连接事件.
                 * */
                if (!DeviceUtils.getPoliceNum(MainActivity.this).equals(Values.POLICE_DEFAULT_NUM))
                    EventBusUtils.postEvent(Values.BUS_EVENT_RESET_SOCKET, Values.SOCKET_RECONNECTION);
            } else if (uri.equals(Settings.System.getUriFor(Values.POLICE_NUMBER))) {
                EventBusUtils.postEvent(Values.BUS_EVENT_RESET_SOCKET, Values.SOCKET_RECONNECTION);
            }
        }
    }

    class USBObserver extends ContentObserver {
        private int mCut = 0;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public USBObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            try {
                if (uri.equals(Settings.System.getUriFor(Values.VIDEO_CUT))) {
                    mCut = Settings.System.getInt(MainActivity.this.getContentResolver(), Values.VIDEO_CUT, -1);
                }
                if (mCut == 0 && mConnected) {
                    openUsb();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class StorageRunnable implements Runnable {

        @Override
        public void run() {
            if (ZZXConfig.is4GA_CDHN || ZZXConfig.isP5X_SZLJ || ZZXConfig.isP5C_CDHN || ZZXConfig.isP5X_CDHN || ZZXConfig.is4G5 || ZZXConfig.isP80){
                writeInfo();
            }else {
                if (mIsFirst) {
                    mIsFirst = false;
                    writeInfo();
                }
            }
            if (!ZZXConfig.is4G5_BAD &&!ZZXConfig.isP5C_HLN)
            writeGpsInfo();
            if (mEventIntent == null) {
                mEventIntent = new Intent();
            }
            boolean isCheck;
            if (ZZXConfig.IsCheckExternalStorage) {
                isCheck = Environment.isExternalStorageRemovable() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            } else {
                isCheck = true;
            }
            if (isCheck) {
                if (mMountFirst) {
                    checkNeedUpdateInit();
                    mMountFirst = false;
                }
                mStorageState = SystemUtil.getFreeStoragePercent();
            } else {
                mStorageState = 0;
            }
            if (mStorageState <= 1) {
                if (mStorageState == 0) {
                    isStorageNotEnought = false;//认为是sd挂载而不是内存不足
                }
                EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORAGE_NOT_ENOUGH);
            } else {
                if (mStorageState < 10) {
                    LogUtil.e("zzx", "mStorageState= " + mStorageState + "LOOP_RECORD" +
                            Settings.System.getInt(getContentResolver(),Values.LOOP_RECORD,-1));
                    if (Settings.System.getInt(getContentResolver(), Values.LOOP_RECORD, -1) == 1) {
                        LogUtil.e("zzx", "LOOP_RECORD= " + Settings.System.getInt(getContentResolver(), Values.LOOP_RECORD, 0));
                        if (ZZXConfig.isP5XD_SXJJ){
                            FileUtils.deleteFile();
                        }else {
                            FileUtils.deleteLastVideoFile();//循环录像删除最早的一个文件
                            FileUtils.deleteLastSoundFile();//循环录音删除最早的一个文件
                            FileUtils.deleteLastPicFile();//循环照片删除最早的一个文件
                            isStorageNotEnought = false;
                            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORAGE_OK);
                        }
                    } else {
                        isStorageNotEnought = true;
                        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORAGE_NOT_ENOUGH);
                    }
                } else {
                    isStorageNotEnought = false;
                    EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORAGE_OK);
                }
            }
            mEventIntent.setAction(Values.ACTION_STORAGE_STATE).putExtra("state", mStorageState);
            Values.LOG_E(TAG, "action = " + Values.ACTION_STORAGE_STATE + "; state = " + mStorageState);
            sendBroadcast(mEventIntent);
            //   checkNeedUpdateInit();
            sendMessage(this, 3 * 1000);
        }
    }

    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "onReceive: "+ action);
//            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//            if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
//                if (!isshow) {
//                    //     EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.SHOW_DIALOG_CHOOSE_POWER_SAVE_MODE);
//                    isshow = !isshow;
//                }
//            } else {
//                isshow = false;
//                if (CameraService.isOpenPSM) {
//                    CameraService.closePowerSaveMode(getApplicationContext());
//                    CameraService.isOpenPSM = false;
//                }
//            }
//            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
//                    status == BatteryManager.BATTERY_STATUS_FULL || status == -1;//-1为默认值
//
//            //  int chargePlug1 = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
//            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
//
//
//            if (isCharging) {
//                if (usbCharge) {
//                    //usb状态
//                } else if (acCharge) {
//                    //ac充电状态
//
//                }
//            } else {
//                //车载模式逻辑
//                if (Settings.System.getInt(getContentResolver(), SETTING_VEHICLE, 0) == 1) {
//                    //当如果是车载情况下。如果usb断开的话，停止录像，设备关机。
//                    //    Toast.makeText(getApplication(), R.string.shutdown, Toast.LENGTH_SHORT).show();
//                    NotifatyStopRecord();
//                    DelayedCloseDevice();
//
//                }
//            }
            //   TTSToast.showToast(context, "status"+status);
//              Values.LOG_I(TAG, "action = " + action);
            switch (action) {

                case Intent.ACTION_BATTERY_CHANGED:
                    battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 1);
//                      Values.LOG_I(TAG, "battery = " + battery);
                    handleBatteryStatus(intent);
                    break;
                case UsbMassManager.ACTION_USB_STATE:
                    mUsbConnected = intent.getBooleanExtra(
                            UsbMassManager.USB_CONNECTED, false);

                    boolean usbMassFunction = intent.getBooleanExtra(UsbMassManager.USB_MASS_FUNCTION, false);
                    UsbMassManager massManager = new UsbMassManager(MainActivity.this);
                    if (mUsbConnected && usbMassFunction && massManager.isMassStorageConnected()) {
                        mConnected = true;
                        boolean preAirplaneMode = getAirplaneMode();
                        if (!preAirplaneMode) {
                            if (ZZXConfig.isAirplaneMode) {
                                setAirplaneModeOn(true);
                            }
                        }
                        minCamera();

                        if (mVideoCut) {
                            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.VIDEO_CUT_MAIN);
                        }
                        mMainHandler.removeMessages(Values.CHECK_NEED_OPEN_USB);
                        mMainHandler.sendEmptyMessageDelayed(Values.CHECK_NEED_OPEN_USB, 1000);
                    } else {
                        if (ZZXConfig.isAirplaneMode) {
                            setAirplaneModeOn(false);
                        }
                        Settings.System.putInt(getContentResolver(),"showPassword",0);
                        mConnected = false;
                        dismissPasswordFragment();
                    }
                    break;
                case MainApplication.ACTION_REPORT_BARCODE:
                    String barcode = intent.getStringExtra(MainApplication.EXTRA_BARCODE);
                    break;
                case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                    minCamera();
                    break;
                case Values.ACTION_SETTING_OPEN_USB:
                    EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.MAIN_ONLY_USB);
                    break;
            }
        }
        private void handleBatteryStatus(Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BATTERY_STATUS_UNKNOWN);
            switch (status) {
                case BATTERY_STATUS_UNKNOWN:
                    Log.d(TAG, "handleBatteryStatus: status_unknown:状态未知");
                    break;
                case BATTERY_STATUS_CHARGING:
                    Log.d(TAG, "handleBatteryStatus: status_charging:充电中:");
                    break;
                case BATTERY_STATUS_DISCHARGING:
                    Log.d(TAG, "handleBatteryStatus: status_discharging:放电中:");
                    break;
                case BATTERY_STATUS_NOT_CHARGING:
                    Log.d(TAG, "handleBatteryStatus: status_not_charging:未充电");
                    break;
                case BATTERY_STATUS_FULL:
                    Log.d(TAG, "handleBatteryStatus: status_full:电池充满");
                    controllDevice(1, LIGHT_COLOR_GREEN_NO_FLASH);
                    break;

            }

        }
    }

    class MainHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case Values.CHECK_NEED_OPEN_USB:
                    checkNeedOpenUsb();
                    break;
                case Values.OPEN_USB:
                    Log.d("UsbMassManager", "MainHandler dispatchMessage: usbConnected:" + mUsbConnected);
                    if (mUsbConnected) {//如果设备在插入的状态下才会打开USb
                        if (CameraService.isVideoReecord)
                        sendRecord(Values.ACTION_RELEASE_CAMERA);
                        mStartOpenUsb = System.currentTimeMillis();
                        onlyOpenUsb();
                        Log.d("UsbMassManager", "open Usb ----打开次数:" + (mReopenCount + 1) + " ----");
                        removeMessages(Values.CHECK_OPEN_USB);
                        sendEmptyMessageDelayed(Values.CHECK_OPEN_USB, 500);
                    }
                    break;
                case Values.CHECK_OPEN_USB:
                    if (mUsbMassManager == null) {
                        mUsbMassManager = new UsbMassManager(MainActivity.this);
                    }
                    Log.d("UsbMassManager", "dispatchMessage: isMassEnabled():" + mUsbMassManager.isMassEnabled() + " sMassStorageConnected():" + mUsbMassManager.isMassStorageConnected());
                    sendEmptyMessageDelayed(Values.CHECK_OPEN_USB, 500);
                    if (mUsbMassManager.isMassEnabled()) {
                        Log.d("UsbMassManager", "dispatchMessage:------- 开启磁盘模式成功 用时:" + (System.currentTimeMillis() - mStartOpenUsb));
                        mReopenCount = 0;
                        removeMessages(Values.CHECK_OPEN_USB);
                    } else if ((System.currentTimeMillis() - mStartOpenUsb) > 8000) {
                        mReopenCount++;
                        if (mReopenCount <= 20) {
                            sendEmptyMessage(Values.OPEN_USB);
                        }
                        /**如果重新打开20次后，磁盘模式还是未打开成功，则放弃*/
                        else {
                            removeMessages(Values.CHECK_OPEN_USB);
                        }
                    }
                    break;
            }
            super.dispatchMessage(msg);
        }

    }
    private void sendRecord(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }
    private class MediaReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //2020.06.20 屏蔽的目的是因为通过采集站设置信息之后打开磁盘又重新还原配置
//            readInit();
            parserConfig();
        }
    }

    class EventHandler extends Handler {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 1:
                    startCameraService();
                    break;
                case MAIN_SHUT_DOWN:
//                    Toast.makeText(getApplication(), "0", Toast.LENGTH_SHORT).show();
                    Log.e("zzx", "shutdown");
                    Toast.makeText(getApplicationContext(), R.string.shutdown, Toast.LENGTH_SHORT).show();
                    ShutDown();
                    break;
                case MAIN_START_RECORD:
                    mRecordManager = new RecordManager(getApplicationContext());
                    startRecord();
                    break;

                case MAIN_OPEN_AIR_PLANE_MODE:
                    setAirplaneModeOn(true);
                    Log.e("zzx", "open_air");
                    mEventHandler.sendEmptyMessageDelayed(MAIN_CLOSE_AIR_PLANE_MODE, 5000);
                    break;
                case MAIN_CLOSE_AIR_PLANE_MODE:
                    setAirplaneModeOn(false);
                    Log.e("zzx", "cloes_air");
                    break;


            }


        }
    }

    class LogUploadRunnable implements Runnable {
        private String mDay;

        LogUploadRunnable(String day) {
            mDay = day;
        }

        @Override
        public void run() {
            if (!SystemUtil.isNetworkAvailable(MainActivity.this) || mProtocolUtils == null || !mProtocolUtils.isSocketCreated()) {
                sendMessage(this, 5 * 1000);
                return;
            }
            uploadLog(mDay);
        }
    }

    class LoginRunnable implements Runnable {
        @Override
        public void run() {
            String policeNum = DeviceUtils.getPoliceNum(MainActivity.this);
            if (!SystemUtil.isNetworkAvailable(MainActivity.this) || !DeviceUtils.checkHasService(MainActivity.this) || policeNum.equals(Values.POLICE_DEFAULT_NUM)) {
                sendMessage(this, 10 * 1000);
                return;
            }
              /*if (!SystemUtil.isNetworkAvailable(MainActivity.this)) {
                  sendMessage(this, 5 * 1000);
                  return;
              }*/
            acquireWakeLock();
            if (mIsUseSDK) {
                mZzxClient = new ZZXClient(MainActivity.this, policeNum, "admin");
            } else {
                if (mProtocolUtils != null) {
                    mProtocolUtils.createSocket();
                } else {
                    mProtocolUtils = new ProtocolUtils(getApplicationContext(), mHandler);
                }
            }
        }
    }

    private class EpoDownloadRunnable implements Runnable {
        @Override
        public void run() {
            if (!SystemUtil.isNetworkAvailable(MainActivity.this)) {
                sendMessage(this, 3 * 1000);
                return;
            }
            startDownloadEpoService();
            if (mHandler != null)
                mHandler.removeCallbacks(this);
        }
    }

    public class ReSetNetWorkThread extends Thread {
        @Override
        public void run() {
            super.run();
            Log.e("zzxzdm", "NetworkType1" + NetWorkUtil.getCurrentNetworkType(getApplicationContext()));
            while (!NetWorkUtil.getCurrentNetworkType(getApplicationContext()).equals("3G")) {
                Log.e("zzxzdm", "NetworkType2" + NetWorkUtil.getCurrentNetworkType(getApplicationContext()));
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("zzxzdm", "========no_3G");
            }
            mEventHandler.sendEmptyMessage(MAIN_OPEN_AIR_PLANE_MODE);
            mEventHandler.sendEmptyMessageDelayed(MAIN_CLOSE_AIR_PLANE_MODE, 5000);
            Log.e("zzxzdm", "========3g_stop");
        }
    }
    private void InitFactoryReset() {
        int first = Settings.System.getInt(getContentResolver(), FIRST_TIME, -1);
        if (first == -1) {
            /**检查应用是否安装**/
            mHandler.sendEmptyMessageDelayed(READ_APK_INSTALL_LIST, 2000);
            Settings.System.putInt(getContentResolver(), FIRST_TIME, 0);
        }
    }
    private void showProgressDialog(final boolean cancelable, final int stringId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInstallProgressDialog == null) {
                    mInstallProgressDialog = new ProgressDialog(MainActivity.this);
                    mInstallProgressDialog.setIndeterminate(true);
                    mInstallProgressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
                }
                mInstallProgressDialog.setCancelable(cancelable);
                mInstallProgressDialog.setMessage(getString(stringId));
                if (!mInstallProgressDialog.isShowing()) {
                    mInstallProgressDialog.show();
                }
            }
        });
    }
    private void showProgressDialog(final int stringId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInstallProgressDialog == null) {
                    mInstallProgressDialog = new ProgressDialog(MainActivity.this);
                    mInstallProgressDialog.setIndeterminate(true);
                    mInstallProgressDialog.setCancelable(false);
                    mInstallProgressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
                }
                mInstallProgressDialog.setMessage(getString(stringId));
                if (!mInstallProgressDialog.isShowing()) {
                    mInstallProgressDialog.show();
                }
            }
        });
    }
    private void dismissInstallDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInstallProgressDialog != null && mInstallProgressDialog.isShowing()) {
                    mInstallProgressDialog.dismiss();
                }
            }
        });
    }
    class PackageInstallObserver extends IPackageInstallObserver.Stub {

        @Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
//            SystemUtil.writeLog(MainActivity.this, "packageInstalled: " + packageName);
            mHandler.sendEmptyMessageDelayed(INSTALL_APK_FILE, 1000);
        }
    }
    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {

        @Override
        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            Values.LOG_W(TAG, packageName + " is Uninstalled " + returnCode);
            mHandler.sendEmptyMessageDelayed(PACKAGE_DELETE, 1000);
        }
    }
    public static final int INSTALL_REPLACE_EXISTING = 0x00000002;
    /**
     * 安装Apk文件,判断是否已安装该包名以及是否要覆盖安装.
     * 安装完后{@link #mInstallObserver}会收到安装完毕结果.安装完一个再安装下一个.
     */
    private void installPackage(String apkPath, boolean replace, String pkgName) {
//        SystemUtil.writeLog(this, "apkPath = " + apkPath + "; replace = " + replace + "; pkgName = " + pkgName);
        File apkFile = new File(apkPath);
        if (!apkFile.exists() || apkFile.isDirectory()) {
            mHandler.sendEmptyMessageDelayed(INSTALL_APK_FILE, 1000);
            return;
        }
        boolean isInstalled = isPackageInstalled(pkgName);
        Values.LOG_E(TAG, pkgName + " : isInstalled = " + isInstalled);
        PackageManager manager = getPackageManager();
        boolean exists = false;
        if (mInstallPackageList == null) {
            mInstallPackageList = manager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        }
        for (PackageInfo info : mInstallPackageList) {
            if (info.packageName.equals(pkgName)) {
                exists = true;
                break;
            }
        }
//        SystemUtil.writeLog(this, "startInstall! exists = " + exists);
        if (!replace && exists) {
            mHandler.sendEmptyMessageDelayed(INSTALL_APK_FILE, 1000);
            return;
        }
        try {
            Method installMethod = PackageManager.class.getMethod("installPackage", Uri.class, IPackageInstallObserver.class, Integer.TYPE, String.class);
            Uri file = Uri.fromFile(apkFile);
            if (mInstallObserver == null) {
                mInstallObserver = new PackageInstallObserver();
            }
            installMethod.invoke(manager, file, mInstallObserver, INSTALL_REPLACE_EXISTING, null);
        } catch (Exception e) {
            e.printStackTrace();
//            SystemUtil.writeLog(this, e.getMessage());
        }
//        SystemUtil.writeLog(this, "finishInstall!");
    }
    private boolean isPackageInstalled(String pkgName) {
        PackageManager packageManager = getPackageManager();
        boolean installed;
        try {
            PackageInfo info = packageManager.getPackageInfo(pkgName, 0);
            installed = info != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            installed = false;
        }
        return installed;
    }
    private void uninstallPackage(int index) {
        if (mInstallInfo.mUninstallList.size() > index) {
            Values.LOG_W(TAG, "index = " + index + "; uninstallPackage " + mInstallInfo.mUninstallList.get(index));
            uninstallPackage(mInstallInfo.mUninstallList.get(index));
        }
    }
    /**
     * =====================自动安装apk ==================
     **/
    private void uninstallPackage(String packageName) {
        boolean isInstalled = isPackageInstalled(packageName);
        Values.LOG_E(TAG, packageName + ": installed --> " + isInstalled);
        if (isInstalled) {
            if (mUninstallObserver == null) {
                mUninstallObserver = new PackageDeleteObserver();
            }
            PackageUtils.uninstallPackage(this, mUninstallObserver, packageName);
        } else {
            mHandler.sendEmptyMessageDelayed(PACKAGE_DELETE, 1000);
        }
    }
    /**
     * 读取安装应用的文件列表并解析.
     */
    private void readInstallList() {
        //    RingtoneManager.setActualDefaultRingtoneUri(MainActivity.this, RingtoneManager.TYPE_NOTIFICATION, null);
//
        File file = new File(Values.INSTALL_FILE_LIST_PAT);
        if (!file.exists() || file.isDirectory()) {
            return;
        }
        try {
            XMLParser parser = XMLParser.getInstance();
            mInstallInfo = parser.parserXMLFile(file);
            parser.release();
            if (mInstallInfo == null) {
                return;
            }
            if (checkNeedInstall()) {
                Values.LOG_E(TAG, " mInstallList.size = " + mInstallInfo.mInstallList.size());
                Values.LOG_E(TAG, " mUninstallList.size = " + mInstallInfo.mUninstallList.size());
                if (!mInstallInfo.mInstallList.isEmpty()) {
                    installApkFile(mInstallIndex);
                }
                if (!mInstallInfo.mUninstallList.isEmpty()) {
                    uninstallPackage(mUninstallIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 解析完毕逐个安装列表内指定文件,若安装完毕则释放资源.
     */
    private void installApkFile(int xmlListIndex) {
//        SystemUtil.writeLog(this, "xmlListIndex = " + xmlListIndex);
        showProgressDialog(R.string.updating);
        if (mInstallInfo.mInstallList.size() > xmlListIndex) {
            ApkFileInfo apkFileInfo = mInstallInfo.mInstallList.get(xmlListIndex);
            installPackage(apkFileInfo.mFilePath, apkFileInfo.mReplaceable, apkFileInfo.mPkgName);
        } else {
            releaseInstall();
        }
    }
    /**
     * 判断是否需要安装apk文件.
     * 1.  XML文件版本不同.
     * 2.  XML文件解析出的Apk文件大于0.
     */
    private boolean checkNeedInstall() {
//        return !mInstallInfo.mInstallList.isEmpty();
        int preVersion = Settings.System.getInt(getContentResolver(), INSTALL_XML_VERSION, -1);
//        SystemUtil.writeLog(this, "preVersion = " + preVersion + "; mVersion = " + mInstallInfo.mVersion + "; list.Size() = " + mInstallInfo.mInstallList.size());
        Values.LOG_E(TAG, "preVersion = " + preVersion + "; mVersion = " + mInstallInfo.mVersion + "; list.Size() = " + mInstallInfo.mInstallList.size());
        return preVersion != mInstallInfo.mVersion && !mInstallInfo.isEmpty();
    }
    private void releaseInstall() {
//        SystemUtil.writeLog(this, "releaseInstall");
        Settings.System.putInt(getContentResolver(), INSTALL_XML_VERSION, mInstallInfo.mVersion);
        if (mInstallPackageList != null) {
            mInstallPackageList.clear();
            mInstallPackageList = null;
        }
        if (mInstallInfo != null) {
            mInstallInfo.releaseInstall();
        }
        mInstallObserver = null;
        dismissInstallDialog();
    }
    public void deleteaiwinn(){
        if (!ZZXConfig.is4GA || !ZZXConfig.is4G5 || !ZZXConfig.is4GC || !ZZXConfig.isP80)
            if (ZZXConfig.IsCheckExternalStorage) {
                if (Environment.isExternalStorageRemovable() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    DeleteFileUtil.delete(Environment.getExternalStorageDirectory() + "/aiwinn/attendance/feature");
                }
            }
    }
}
