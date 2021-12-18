package com.zzx.police.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.zzx.police.data.Values;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 13-12-26.
 */
public class SystemUtil {

    public static final String MOBILE = "MOBILE";
    public static final String PROPERTIES_FIELD_USB_AUTO = "zzx.usb_auto";
    public static final String PROPERTIES_FIELD_RECORD_REPORT = "zzx.record_report";
    public static final String PROPERTIES_FIELD_VERSION_PRE = "zzx.version_pre";
    public static final String PROPERTIES_FIELD_VERSION_MID = "zzx.version_mid";
    private static Activity mContext;
    public SystemUtil(Activity activity){
        this.mContext = activity;
    }

    public static void reboot(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot(null);
    }

    public static String getSystemProperties(String field) {
        String value = "";
        try {
            Class<?> classType = Class.forName(Values.CLASS_SYSTEM_PROPERTIES);
            Method getMethod = classType.getDeclaredMethod(Values.METHOD_GET, String.class);
            value = (String) getMethod.invoke(classType, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static List<ResolveInfo> queryLauncherActivity(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list;
        try {
            list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        } catch (Exception e) {
            e.printStackTrace();
            list = new ArrayList<>();
        }
        return list;
    }

    public static String getDefaultLauncherPackage(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info = context.getPackageManager().resolveActivity(intent, 0);
        if (info == null || info.activityInfo == null || info.activityInfo.packageName.equals("android")) {
            return "";
        } else {
            return info.activityInfo.packageName;
        }
    }

    public static int getLauncherActivityCount(Context context) {
        return queryLauncherActivity(context).size();
    }

    public static void setDefaultLauncher(Context context, String pkgName, String clsName) {
        PackageManager pm = context.getPackageManager();
        ComponentName name = new ComponentName(pkgName, clsName);
        IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
    }

    public static String getIMEI(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    public static String getIMSI(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
    }

    public static String getIP(Context context) {
        return "";
    }

    public static boolean isMobileNetworkAvailable(Context context) {
        if (isNetworkAvailable(context)) {
            int networkType = getNetworkType(context);
            if (ConnectivityManager.TYPE_MOBILE == networkType) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetwork(context);
        if (null != networkInfo) {
            if (networkInfo.isAvailable() && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWifiNetworkConnected(Context context) {
        if (isNetworkAvailable(context)) {
            int networkType = getNetworkType(context);
            if (ConnectivityManager.TYPE_WIFI == networkType) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo networkInfo = getActiveNetwork(context);
        if (null != networkInfo) {
            if (networkInfo.isAvailable() && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static NetworkInfo getActiveNetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo();
    }

    public static int getNetworkType(Context context) {
        NetworkInfo networkInfo = getActiveNetwork(context);
        if (networkInfo == null) {
            return -1;
        }
        return networkInfo.getType();
    }

    public static void openWifi(Context cxt){
        WifiManager wifiManager = (WifiManager) cxt.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
    }
    public static void closeWifi(Context cxt){
        WifiManager wifiManager = (WifiManager) cxt.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    /**
     * 判断是否有SIM 卡
     */
    public static boolean readSIMCard(Context cxt){
        TelephonyManager telephonyService = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telephonyService.getSimState();
        boolean result = true;
        switch (simState){
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false;
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }
    public static void setMobileData(Context context, boolean enabled) {
        if (context == null)
            return;
        /*if (isMobileDataEnabled(context)) {
            return;
        }*/
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class<?> connectClass;
        Method setMethod;
        try {
            connectClass = Class.forName(ConnectivityManager.class.getName());
            setMethod = connectClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMethod.setAccessible(true);
            setMethod.invoke(manager, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMobileDataEnabled(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Method getMobileDataEnabled = manager.getClass().getMethod("getMobileDataEnabled");
            return (boolean) getMobileDataEnabled.invoke(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setTimeOut(Context context, int sec) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, sec * 1000);
    }

    /**
     *@param msgId R.string.xxx. If this is set, param msg will be ignore.
     * */
    public static void makeToast(Context context, int msgId, String msg) {
        if (msgId != 0) {
            Toast.makeText(context, context.getString(msgId), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static long getFlashSize() {
        /** StatFs获取该路径文件标识在Unix文件系统中包含的所有信息 **/
        File dataDir = Environment.getDataDirectory();
        StatFs statFs = new StatFs(dataDir.getPath());
        long blockSize = statFs.getBlockSize();//文件系统中每个块的大小,单位是byte
        long blockCount = statFs.getBlockCount();//块的个数
        long freeSize = statFs.getFreeBlocks();//包括预留的,应用不可用的块的个数
        long availbleSize = statFs.getAvailableBlocks();//只包含应用可用的块的个数
        return blockSize * blockCount;
    }

    public static float getExternalStorageSize(boolean free) {
        long size;
        try {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(sdcardDir.getAbsolutePath());
            long blockSize = statFs.getBlockSize();//文件系统中每个块的大小,单位是byte
            long blockCount = statFs.getBlockCount();//块的个数
            long availableSize = statFs.getAvailableBlocks();//只包含应用可用的块的个数
            if (!free) {
                size = blockSize * blockCount;
            } else {
                size = blockSize * availableSize;
            }
            return size / 1024.0f / 1024.0f / 1024.0f;
        }catch (Exception e){
            e.printStackTrace();
        }
       return 0;
    }
    public static int getFreeStoragePercent() {
        try {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(sdcardDir.getAbsolutePath());
            float blockCount = statFs.getBlockCount();//块的个数
            float availableSize = statFs.getAvailableBlocks();//只包含应用可用的块的个数
            float percent = availableSize / blockCount;
            if (percent > 0.01)
                return (int) (percent * 100);
            if (percent <= 0.01 && percent > 0.005) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getDeleteStoragePercent() {
        try {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(sdcardDir.getAbsolutePath());
            float blockCount = statFs.getBlockCount();//块的个数
            float availableSize = statFs.getAvailableBlocks();//只包含应用可用的块的个数
            float percent = availableSize / blockCount;
            if (percent > 0.01)
                return (int) (percent * 100);
            if (percent <= 0.01 && percent > 0.005) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setMobileDataState(Context cxt, boolean mobileDataEnabled) {
        TelephonyManager telephonyService = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod)
            {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }
    /**
     * 设置飞行模式是否可以打开
     *
     * @param enabling true打开  false关闭
     */
    public static void setAirplaneModeOn(Context context,boolean enabling) {
        try {
            Settings.Global.putInt(context.getApplicationContext()
                            .getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                    enabling ? 1 : 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra("state", enabling);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean getAirplaneMode(Context context) {
        Log.e( "getAirplaneMode: ", Settings.Global.getInt(context.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON, 0)+"");
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }
    public static void setBrightness(int brightness) {
        if (brightness <= 11){
            brightness=15;
        }
        Intent intent = new Intent();
        intent.setAction(Values.UPDATE_LIGHT);
        intent.putExtra("lightRation", brightness);
        mContext.sendBroadcast(intent);
        try {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, brightness);
            WindowManager.LayoutParams lp = mContext.getWindow()
                    .getAttributes();
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
            mContext.getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean gpsIsOpen(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps) {
            return true;
        }
        return false;
    }
    /**
     * @param state    0 关闭，1打开
     * @param filePath ir_cut文件设备路径
     */
    public static void writeToDevice(int state, String filePath) {
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
    public static String getWifiHost(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return null;
        } else if (!wifiManager.isWifiEnabled()) {
            return null;
        } else {
            DhcpInfo info = wifiManager.getDhcpInfo();
            if (info == null) {
                return null;
            } else {
                int address = info.serverAddress;
                return (address & 255) + "." + (address >> 8 & 255) + "." + (address >> 16 & 255) + "." + (address >> 24 & 255);
            }
        }
    }
}
