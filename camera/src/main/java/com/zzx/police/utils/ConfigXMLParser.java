package com.zzx.police.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.zzx.police.MainActivity;
import com.zzx.police.ZZXConfig;
import com.zzx.police.adapter.GridViewPagerAdapter;
import com.zzx.police.apn.ApnConfig;
import com.zzx.police.apn.ApnInfo;
import com.zzx.police.data.Values;
import com.zzx.police.services.CameraService;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Tomy
 *         配置文件解析.
 *         Created by Tomy on 2014/9/26.
 */
public class ConfigXMLParser {
    private static final String TAG = "ConfigXMLParser";
    private static final String CFG_DEVICE_NUM="ZZXDeviceNum";
    private final String FREQUENCY  ="Frequency";
    public static final String CFG_OPERATING = "ZZXOperating";
    private static final String AUTO_TIME = "ZZXAutoTime";
    private Context mContext;
    private final String CFG_TALK_BACK = "TalkBack";
    private final String CFG_SUB_RATE = "SubRate";
    private final String CFG_GPS = "GPS";
    private final String CFG_QUALITY = "ReportQuality";
    private final String CFG_USB_AUTO = "UsbAuto";
    private final String CFG_PRESSED = "PressScreenOff";
    private final String CFG_DSJ = "DSJ";
    private final String CFG_DATA_SET = "DataSet";
    private final String CFG_MID = "Mid";
    private final String CFG_TIME = "AutoTime";

    private final String CFG_IP = "IP";
    private final String CFG_PORT = "Port";
    private final String CFG_USER = "User";
    private final String CFG_PWD = "Pwd";
    private final String CFG_DEVICE = "Device";
    private final String CFG_MODEL = "Model";

    private final String CFG_FTP_IP = "FtpIP";
    private final String CFG_FTP_PORT = "FtpPort";
    private final String CFG_FTP_USER = "FtpUser";
    private final String CFG_FTP_PWD = "FtpPwd";
    private final String CFG_LANGUAGE = "Language";

    private final String CFG_TIME_MODE = "TimeMode";
    private final String CFG_TIME_SET = "TimeSet";
    private final String CFG_File_SCREEN_ORIENTATION_SET = "FileSet";

    private final String CFG_APN_NAME = "ZZXApnName";
    private final String CFG_APN_APN = "ZZXApnApn";
    private final String CFG_APN_MCC = "ZZXApnMcc";
    private final String CFG_APN_MNC = "ZZXApnMnc";
    private final String CFG_APN_CURRENT = "ZZXApnCurrent";
    private final String CFG_APN_TYPE = "ZZXApnType";
    private final String CFG_APN_PROTOCOL = "ZZXApnProtocol";
    private final String CFG_APN_ROAMING = "ZZXApnRoaming";

    private final String VERSION_MID = "ZZXVersionMid";
    private final String MODEL_NEED_PRE = "ZZXNeedPre";
    private final String SCREEN_SHIELD = "ZZXScreenShield";

    private final String CFG_ENABLED_PWD = "PwdEnabled";
    private final String CFG_ENABLED_USER = "UserEnabled";
    private final String CFG_ENABLED_SERVICE = "ServiceEnabled";
    private final String CFG_ENABLED_REPORT = "ReportEnabled";
    private final String CFG_ENABLED_GPS = "ZZXGPSEnabled";
    private final String CFG_ENABLED_WIFI = "ZZXWifiEnabled";
    private final String CFG_ENABLED_BT = "ZZXBTEnabled";
    public static final String CFG_APN_SETTING = "ZZXApnSetting";
    public static final String CFG_ENABLED_TEXT_GPS = "ZZXGpsTestEnabled";
    public static final String CFG_ENABLED_SERVER_SETTING = "ZZXServerSettingEnabled";
    public static final String CFG_ENABLED_NETWORD_APN_SETTING = "ZZXNetWorkApnSettingEnabled";

    public final static String CFG_ENABLED_VIDEO_CUT = "ZZXCut";
    public final static String CFG_SETTING_SERIAL = "ZZXSerial";
    public final static String CFG_SETTING_IDCode = "ZZXIDCode";
    public static final String CFG_ENABLED_ID_CARD = "ZZXIdCard";            //身份证识别
    public static final String CFG_ENABLED_FENYANG = "ZZXFenYang";                //汾阳执法
    public final static String CFG_TALK_BACK_VISABLE = "ZZXTalkBackVisable";     //对讲
    public static final String CFG_DEFAULT_WIFI = "ZZXDefaultWifi";
    public static final String CFG_DEFAULT_USB_DEBUG = "ZZXUsbDebug";
    public static final String CFG_DEFAULT_BT = "ZZXDefaultBT";
    public static final String CFG_DEFAULT_DATA_ROAMING = "ZZXDefaultDataRoaming";
    public static final String CFG_UEVENT_RECORD = "ZZXRecord";
    public static final String CFG_UEVENT_PHONOS = "ZZXPhonos";
    public static final String CFG_UEVENT_SWITCH = "ZZXSwitch";
    public static final String CFG_UEVENT_SUBTRACT = "ZZXSubtract";
    public static final String CFG_UEVENT_ADD = "ZZXAdd";

    public static final String CFG_ENABLED_SDK = "ZZXSDKEnabled";
    private static SAXParser mSaxParser;
    private PackageManager mPkgManager;
    private ContentResolver mResolver;
    private PackageInstallObserver mInstallObserver;
    private PackageDeleteObserver mUninstallObserver;

    private boolean mInstalling = false;
    private boolean mUninstalling = false;
    private boolean mLastConfig = false;
    private String mFilePath;
    private SharedPreferences mPreferences;
    private final Object mObject = new Object();
    private ApnConfig apnConfig;
    private ApnInfo info;

    public ConfigXMLParser(Context context) {

        mContext = context;
        mResolver = mContext.getContentResolver();
        mPkgManager = mContext.getPackageManager();
        if (mSaxParser == null) {
            try {
                mSaxParser = SAXParserFactory.newInstance().newSAXParser();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void parserXMLFile(String filePath) {
        mFilePath = filePath;
        File file = new File(filePath);
        if (!file.exists())
            return;
        try {
            mSaxParser.parse(new File(filePath), mHandler);
            EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.CONFIG_START);
        } catch (Exception e) {
            e.printStackTrace();
            EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.CONFIG_ERROR);
        }
    }

    public void release() {
        EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.CONFIG_FINISEDH);
        mSaxParser.reset();
        mContext = null;
        mResolver = null;
        mPkgManager = null;
        mSaxParser = null;
        File file = new File(mFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    private DefaultHandler mHandler = new XMLContentHandler();

    private class XMLContentHandler extends DefaultHandler {
        private String mFlag;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            mFlag = localName;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            mFlag = "";
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
            synchronized (mObject) {
                mLastConfig = true;
                if (!mInstalling && !mUninstalling) {
                    release();
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = String.valueOf(ch, start, length);
            mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            switch (mFlag) {
                case CFG_DATA_SET:
                    setConfigInt(MainActivity.MOBILE_DATA, value);
                    /*
                    int dataSet = Settings.System.getInt(mResolver, MainActivity.MOBILE_DATA, 1);
                    if (dataSet != Integer.parseInt(value)) {
                        SystemUtil.reboot(mContext);
                    }*/
                    break;
                case CFG_DSJ:
                    setConfigInt(MODEL_NEED_PRE, value);
                    break;
                case CFG_GPS:
                    setConfigInt(CameraService.GPS_SRT, value);
                    break;
                case CFG_MID:
                    Settings.System.putString(mResolver, VERSION_MID, value);
                    break;
                case CFG_PRESSED:
                    setConfigInt(SCREEN_SHIELD, value);
                    break;
                case CFG_USB_AUTO:
                    setConfigInt(MainActivity.AUTO_CONNECT, value);
                    break;
                case CFG_TALK_BACK:
                    int talkBack = Settings.System.getInt(mResolver, GridViewPagerAdapter.TALK_BACK_NEW, 3);
                    setConfigInt(GridViewPagerAdapter.TALK_BACK_NEW, value);
                    installTalkBack(talkBack, Integer.parseInt(value));
                    break;
                case CFG_SUB_RATE:
                    setConfigInt(CameraService.RECORD_RESTART, value);
                    break;
                case CFG_QUALITY:
                    setConfigInt(CameraService.REPORT_QUALITY, value);
                    break;
                case CFG_TIME:
                    setConfigInt(AUTO_TIME, value);
                    break;
                case CFG_IP:
                    setConfigString(Values.SERVICE_IP_ADDRESS, value);
                    break;
                case CFG_PORT:
                    setConfigString(Values.SERVICE_IP_PORT, value);
                    break;
                case CFG_USER:
                    setConfigString(Values.POLICE_NUMBER, value);
                    break;
                case CFG_PWD:
                    setConfigString(Values.ADMIN_PSW, value);
                    mPreferences.edit()
                            .putString(Values.ADMIN_PSW, value)
                            .apply();

                    break;
                case CFG_DEVICE:
                    setConfigString(Values.DEVICE_NUMBER, value);
                    break;
                case CFG_MODEL:
                    setConfigString(Values.MODEL_NAME, value);
                    break;
                case CFG_FTP_IP:
                    setConfigString(Values.FTP_IP, value);
                    break;
                case CFG_FTP_PORT:
                    setConfigString(Values.FTP_SET_PORT, value);
                    break;
                case CFG_FTP_USER:
                    setConfigString(Values.FTP_NAME, value);
                    break;
                case CFG_FTP_PWD:
                    setConfigString(Values.FTP_PASS, value);
                    break;
                case CFG_LANGUAGE:
                    setConfigInt(Values.LANGUAGE_SETTING, value);
                    break;
                case CFG_TIME_MODE://根据什么去校准时间
                    setTimeMode(value);
                    break;
                case CFG_TIME_SET://设置时间，格式为yyyy-mm-dd hh:mm:ss年.月.日.时.分.秒）
                    setTime(value);
                    break;
                case CFG_File_SCREEN_ORIENTATION_SET://设置文件管理器播放视屏的时候横竖屏  1为竖屏，0为横屏
                    setConfigInt(Values.File_SCREEN_ORIENTATION, value);
                    break;

                case CFG_SETTING_IDCode:
                case CFG_SETTING_SERIAL:
                    setConfigString(mFlag, value);
                    break;
                case CFG_ENABLED_GPS:
                case CFG_ENABLED_WIFI:
                case CFG_ENABLED_BT:
                case CFG_ENABLED_SDK:
                case CFG_APN_SETTING:
                case CFG_ENABLED_TEXT_GPS:
                case CFG_ENABLED_SERVER_SETTING:
                case CFG_ENABLED_NETWORD_APN_SETTING:
                case CFG_ENABLED_FENYANG:
                case CFG_UEVENT_RECORD:
                case CFG_UEVENT_PHONOS:
                case CFG_UEVENT_SWITCH:
                case CFG_UEVENT_SUBTRACT:
                case CFG_UEVENT_ADD:
                case Values.LOOP_RECORD:
                    setConfigInt(mFlag, value);
                    break;
                case CFG_ENABLED_VIDEO_CUT:
                    setConfigInt(CFG_ENABLED_VIDEO_CUT, value);
               //     ZZXConfig.mVideoCut = Integer.parseInt(value) == 1;
                    break;
                case CFG_TALK_BACK_VISABLE:
                    setConfigInt(CFG_TALK_BACK_VISABLE, value);
                    break;
                case CFG_ENABLED_ID_CARD:
                    setConfigInt(mFlag,value);
                    break;
                case CFG_DEFAULT_BT:
                    setConfigInt(mFlag, value);
                    setBTSetting();
                    break;
                case CFG_DEFAULT_WIFI:
                    setConfigInt(mFlag, value);
                    setWIFISetting();
                    break;
                case CFG_DEFAULT_DATA_ROAMING:
                    setConfigInt(mFlag, value);
                    setDataRoamingSetting();
                    //    Settings.Global.getInt(mResolver,)
                    break;
                case VERSION_MID:
                    setConfigString(mFlag, value);
                    break;
                case CFG_APN_APN:
                case CFG_APN_NAME:
                case CFG_APN_MCC:
                case CFG_APN_MNC:
                case CFG_APN_CURRENT:
                case CFG_APN_TYPE:
                case CFG_APN_PROTOCOL:
                case CFG_APN_ROAMING:
                    setConfigString(mFlag, value);
                    setAPNSetting(mFlag);
                    break;
                case CFG_DEVICE_NUM:
                    setConfigString(CFG_DEVICE_NUM,value);
                    break;
                case FREQUENCY:
                    setConfigString( Values.FREQUENCY,value);
                    break;
                case CFG_OPERATING:
                    setConfigString(CFG_OPERATING, value);
                    break;
            }
            ZZXConfig zzxConfig = new ZZXConfig(mContext);
            for (int i = 0; i < zzxConfig.getCount(); i++) {
                if (mFlag == zzxConfig.getList(i)) {
                    //  mlog.mgLog(5, "zzxzdm", "mFlag" + mFlag + "-->value:" + value + "-->i" + i);
//                    LogUtil.e("zzxzdm", "mFlag" + mFlag + "-->value:" + value + "-->i" + i);
                    zzxConfig.ChangeValuesBySave(mFlag, Integer.parseInt(value) == 1);
                }
            }
        }

        private void setTime(String time) {
            //    LogUtil lg=LogUtil.getInstance(Environment.getExternalStorageDirectory() + File.separator + "myLog.txt");
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                //   format.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                Date date = format.parse(time);
//                lg.mgLog(2, "zdm", "===========================");
//                lg.mgLog(2, "zdm", time);
//                lg.mgLog(2,"zdm",date.toString());
                SystemClock.setCurrentTimeMillis(date.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void setTimeMode(String value) {
            switch (value) {
                case "0":
                    setConfigInt(Settings.Global.AUTO_TIME, "1");
                    setConfigInt("auto_time_gps", "0");
                    break;//network
                case "1":
                    setConfigInt(Settings.Global.AUTO_TIME, "0");
                    setConfigInt("auto_time_gps", "1");
                    break;//gps
                case "2":
                    setConfigInt(Settings.Global.AUTO_TIME, "0");
                    setConfigInt("auto_time_gps", "0");
                    break;//close
            }
        }

        private void setConfigInt(String key, String value) {
            Settings.System.putInt(mResolver, key, Integer.parseInt(value));
        }

        private void setConfigString(String key, String value) {
//            TTSToast.showToast(mContext, mFlag + ":" + value);
            if (value.isEmpty()) {
                return;
            }
            Settings.System.putString(mResolver, key, value);
        }
    }

    private void setDataRoamingSetting() {
        if (Settings.System.getInt(mResolver, ConfigXMLParser.CFG_DEFAULT_DATA_ROAMING, 1) == 1) {
            Log.i(TAG, "zzx_setDataRoamingSetting_open");
            Settings.Global.putInt(mResolver, Settings.Global.DATA_ROAMING, 1);
        } else {
            Log.i(TAG, "zzx_setDataRoamingSetting_close");
            Settings.Global.putInt(mResolver, Settings.Global.DATA_ROAMING, 0);
        }
    }
    private WifiManager mManager = null;

    private void setWIFISetting() {
        mManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (Settings.System.getInt(mResolver, ConfigXMLParser.CFG_DEFAULT_WIFI, 0) == 1) {
            Log.i(TAG, "zzx_setWIFISetting_open");
            mManager.setWifiEnabled(true);
        } else {
            Log.i(TAG, "zzx_setWIFISetting_close");
            mManager.setWifiEnabled(false);
        }

    }

    private BluetoothAdapter mAdapter = null;

    private void setBTSetting() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Settings.System.getInt(mResolver, ConfigXMLParser.CFG_DEFAULT_BT, 0) == 1) {
            Log.i(TAG, "zzx_setBTSetting()_bt_open");
            mAdapter.enable();
        } else {
            Log.i(TAG, "zzx_setBTSetting()_bt_close");
            mAdapter.disable();
        }
    }

    private void setAPNSetting(String mFlag) {
        String name = Settings.System.getString(mResolver, CFG_APN_NAME);
        String apn = Settings.System.getString(mResolver, CFG_APN_APN);
        if (mFlag == CFG_APN_NAME && name != null && apn != null) {
            String roaming = Settings.System.getString(mResolver, CFG_APN_ROAMING);
            String mcc = Settings.System.getString(mResolver, CFG_APN_MCC);
            String mnc = Settings.System.getString(mResolver, CFG_APN_MNC);
            String current = Settings.System.getString(mResolver, CFG_APN_CURRENT);
            String type = Settings.System.getString(mResolver, CFG_APN_TYPE);
            String protocol = Settings.System.getString(mResolver, CFG_APN_PROTOCOL);
            info = new ApnInfo();
            info.setName(name);
            info.setApn(apn);
            if (mcc != null) {
                info.setMcc(mcc);
            }
            if (mnc != null) {
                info.setMnc(mnc);
            }
            if (type != null) {
                info.setType(type);
            }
            if (current != null) {
                info.setCurrent(current);
            }
            if (protocol != null) {
                info.setProtocol(protocol);
            }
            if (roaming != null) {
                info.setRoaming(roaming);
            }
            apnConfig = new ApnConfig(mContext);
            apnConfig.setApnUse(info);

        }
    }
    private void installTalkBack(int preTalkBack, int currentTalkBack) {
        Settings.System.putInt(mResolver, CameraService.WIFI_SCAN, currentTalkBack == GridViewPagerAdapter.TALK_BACK_BD_JWDJ ? 1 : 0);
        String prePkg = getTalkBackPkg(preTalkBack);
        if (currentTalkBack != preTalkBack) {
            if (isPackageInstalled(prePkg)) {
                if (mUninstallObserver == null) {
                    mUninstallObserver = new PackageDeleteObserver();
                }
                Log.w("ConfigXMLParser", "uninstallPackage0");
                mUninstalling = true;
                PackageUtils.uninstallPackage(mContext, mUninstallObserver, prePkg);
                Log.w("ConfigXMLParser", "uninstallPackage1");
            }
        }
        String newPkg = getTalkBackPkg(currentTalkBack);
        if (!isPackageInstalled(newPkg)) {
            String apkPath = getTalkBackApkPath(currentTalkBack);
            File apkFile = new File(apkPath);
            if (apkFile.exists() && !apkFile.isDirectory()) {
                Log.w("ConfigXMLParser", "installPackage0");
                mInstalling = true;
                installPackage(apkFile);
                Log.w("ConfigXMLParser", "installPackage1");
            }
        }
    }

    /**
     * Flag parameter for {@link #installPackage} to indicate that you want to replace an already
     * installed package, if one exists.
     */
    public static final int INSTALL_REPLACE_EXISTING = 0x00000002;

    private void installPackage(File apkFile) {
        if (!apkFile.exists()) {
            return;
        }
        try {
            Method installMethod = PackageManager.class.getMethod("installPackage", Uri.class, IPackageInstallObserver.class, Integer.TYPE, String.class);
            Uri file = Uri.fromFile(apkFile);
            if (mInstallObserver == null) {
                mInstallObserver = new PackageInstallObserver();
            }
            installMethod.invoke(mPkgManager, file, mInstallObserver, INSTALL_REPLACE_EXISTING, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void installPackage(String apkPath) {
        installPackage(new File(apkPath));
    }

    private void uninstallPackage(String pkg) {
        PackageUtils.uninstallPackage(mContext, mUninstallObserver, pkg);
    }

    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {

        @Override
        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            Log.w("ConfigXMLParser", packageName + " is removed");
            switch (packageName) {
                case Values.PACKAGE_NAME_TALK_BACK_YC_AQTX:
                    uninstallPackage(Values.PACKAGE_NAME_TALK_BACK_YC_JLJQ);
                    break;
                case Values.PACKAGE_NAME_TALK_BACK_YC_JLJQ:
                    uninstallPackage(Values.PACKAGE_NAME_TALK_BACK_YC_JQCX);
                    break;
                case Values.PACKAGE_NAME_TALK_BACK_YC_JQCX:
                    uninstallPackage(Values.PACKAGE_NAME_TALK_BACK_YC_YDHC);
                    break;
                default:
                    synchronized (mObject) {
                        mUninstalling = false;
                        if (mLastConfig && !mInstalling) {
                            release();
                        }
                    }
                    break;
            }
            /*mUninstalling = false;
            if (mLastConfig && !mInstalling) {
                release();
            }*/
        }
    }

    class PackageInstallObserver extends IPackageInstallObserver.Stub {

        @Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
            Log.w("ConfigXMLParser", packageName + " is Installed");
            switch (packageName) {
                case Values.PACKAGE_NAME_TALK_BACK_YC_AQTX:
                    installPackage(PATH_TALK_YC_JLJQ);
                    break;
                case Values.PACKAGE_NAME_TALK_BACK_YC_JLJQ:
                    installPackage(PATH_TALK_YC_JQCX);
                    break;
                case Values.PACKAGE_NAME_TALK_BACK_YC_JQCX:
                    installPackage(PATH_TALK_YC_YDHC);
                    break;
                default:
                    synchronized (mObject) {
                        mInstalling = false;
                        if (mLastConfig && !mUninstalling) {
                            release();
                        }
                    }
                    break;
            }
        }
    }

    private boolean isPackageInstalled(String pkgName) {
        boolean installed;
        try {
            PackageInfo info = mPkgManager.getPackageInfo(pkgName, 0);
            installed = info != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            installed = false;
        }
        return installed;
    }

    private String getTalkBackPkg(int position) {
        String pkg = null;
        switch (position) {
            case GridViewPagerAdapter.TALK_BACK_ORI_JQT:
                pkg = Values.PACKAGE_NAME_TALK_BACK_JQT;
                break;
            case GridViewPagerAdapter.TALK_BACK_XKL_YI:
                pkg = Values.PACKAGE_NAME_TALK_BACK_XKL;
                break;
            case GridViewPagerAdapter.TALK_BACK_LS_WO:
                pkg = Values.PACKAGE_NAME_TALK_BACK_LS;
                break;
            case GridViewPagerAdapter.TALK_BACK_CLOUD_KU:
                pkg = Values.PACKAGE_NAME_TALK_BACK_KU;
                break;
            case GridViewPagerAdapter.TALK_BACK_SPL:
                pkg = Values.PACKAGE_NAME_TALK_BACK_SPL;
                break;
            case GridViewPagerAdapter.TALK_BACK_YC:
                pkg = Values.PACKAGE_NAME_TALK_BACK_YC_AQTX;
                break;
            case GridViewPagerAdapter.TALK_BACK_BD_JWDJ:
                pkg = Values.PACKAGE_NAME_TALK_BACK_BD_JWDJ;
                break;
            case GridViewPagerAdapter.TALK_BACK_RONG_HE_ZHI_HUI:
                pkg = Values.PACKAGE_NAME_TALK_BACK_RONG_HE;
                break;
        }
        return pkg;
    }

    private String getTalkBackApkPath(int position) {
        String path = null;
        switch (position) {
            case GridViewPagerAdapter.TALK_BACK_ORI_JQT:
                path = PATH_TALK_JQT;
                break;
            case GridViewPagerAdapter.TALK_BACK_XKL_YI:
                path = PATH_TALK_YI;
                break;
            case GridViewPagerAdapter.TALK_BACK_LS_WO:
                path = PATH_TALK_LS_WO;
                break;
            case GridViewPagerAdapter.TALK_BACK_CLOUD_KU:
                path = PATH_TALK_KU;
                break;
            case GridViewPagerAdapter.TALK_BACK_SPL:
                path = PATH_TALK_SPL;
                break;
            case GridViewPagerAdapter.TALK_BACK_YC:
                path = PATH_TALK_YC_AQTX;
                break;
            case GridViewPagerAdapter.TALK_BACK_BD_JWDJ:
                path = PATH_TALK_BD_JWDJ;
                break;
            case GridViewPagerAdapter.TALK_BACK_RONG_HE_ZHI_HUI:
                path = PATH_TALK_RONG_HE_ZHI_HUI;
                break;
        }
        return path;
    }

    private final String PATH_TALK_JQT = "/system/etc/apk/PTT_jqt.apk";
    private final String PATH_TALK_YI = "/system/etc/apk/YiTalk.apk";
    private final String PATH_TALK_LS_WO = "/system/etc/apk/lishi_wo.apk";
    private final String PATH_TALK_KU = "/system/etc/apk/CloudPTT.apk";
    private final String PATH_TALK_SPL = "/system/etc/apk/spl.apk";
    private final String PATH_TALK_BD_JWDJ = "/system/etc/apk/BD_JWDJ.apk";
    private final String PATH_TALK_RONG_HE_ZHI_HUI = "/system/etc/apk/RongHeTalk.apk";
    private final String PATH_TALK_YC_AQTX = "/system/etc/apk/MT_AQTX.apk";
    private final String PATH_TALK_YC_JLJQ = "/system/etc/apk/MT_JLJQ.apk";
    private final String PATH_TALK_YC_JQCX = "/system/etc/apk/MT_JQCX.apk";
    private final String PATH_TALK_YC_YDHC = "/system/etc/apk/MT_YDHC.apk";
}
