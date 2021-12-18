package com.zzx.police.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;

import com.zzx.police.R;
import com.zzx.police.data.StoragePathConfig;
import com.zzx.police.data.Values;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    private static final String TAG = "CommonUtils: ";
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat dfRecorder = new SimpleDateFormat(
			"yyyy/MM/dd hh:mm:ss");
	public static String formatData2Str(long time) {
		return df.format(new Date(time));
	}

	/**
	 * 2012-12-7
	 * 
	 * @return
	 */
	public static boolean isSDExists(Context context) {
		long availableMemorySize = getAvailableMemorySize(StoragePathConfig.getStorageRootPath(context));
		return availableMemorySize > 0;

	}

	public static boolean isExternalStorageAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 2013-1-4
	 * 
	 * @param intent
	 * @param context
	 * @return
	 */
	public static boolean checkAppInstalled(Intent intent, Context context) {
		PackageManager manager = context.getPackageManager();

		List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);
		if (list == null || list.size() < 1)
			return false;
		return true;
	}

	/**
	 * 
	 * @param context
	 * @param className
	 * @return
	 */
	public static boolean isServiceRunning(Context context, String className) {

		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;

	}

	public static boolean isActivityRunning(Context context, String cls) {
		boolean isRunning = false;
		ActivityManager manager = (ActivityManager) getService(context,
				Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfos = manager
				.getRunningTasks(20);
		for (ActivityManager.RunningTaskInfo info : taskInfos) {
			if (info.baseActivity != null) {
				if (cls.equals(info.baseActivity.getClassName())) {
					isRunning = true;
				}
			}
		}
		return isRunning;
	}

	public static boolean isActivityRunningByPkg(Context context, String pkg) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		return cn.getPackageName().equals(pkg);
	}

	public static Object getService(Context context, String serviceName) {
		return context.getSystemService(serviceName);
	}

	public static boolean isMobileNumberValid(String phoneNumber) {
		

		boolean isValid = false;

		String expression = "((^((\\+86)|(86))?(13|15|18)[0-9]{9}$))";
		CharSequence inputStr = phoneNumber;

		Pattern pattern = Pattern.compile(expression);

		Matcher matcher = pattern.matcher(inputStr);

		if (matcher.matches()) {
			isValid = true;
		}

		return isValid;
	}

	/* phone number check function by xieshengxin 2013.05.10 */
	public static boolean isPhoneNumberValid(String phoneNumber) {
		boolean isValid = false;

		String expression = "(^(0?\\d{2,3}\\d{6,8})$)";
		CharSequence inputStr = phoneNumber;

		Pattern pattern = Pattern.compile(expression);

		Matcher matcher = pattern.matcher(inputStr);

		if (matcher.matches()) {
			isValid = true;
		}

		return isValid;
	}

	public static boolean checkNetworkState(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			Values.LOG_E(TAG, context.getString(R.string.network_not_connected));
			return false;
		}
		return true;
	}

	public static boolean checkSIMCardState(Context context) {
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int state = manager.getSimState();
		if (state == TelephonyManager.SIM_STATE_UNKNOWN
				|| state == TelephonyManager.SIM_STATE_ABSENT) {
            Values.LOG_E(TAG, context.getString(R.string.sim_not_work));
			return false;
		}
		return true;
	}
	public static long getAvailableMemorySize(String sdcardPath) {
		// String sdcard2 = "/mnt/sdcard2/";
		if (isExternalStorageAvailable()) {
			// File path =
			// Environment.getExternalStorageDirectory();//SDCard
			StatFs stat = new StatFs(sdcardPath);
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getBlockCount();
			// long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return -1;
		}
	}
	
	
	/**
	 * 描述：复制文件io操作
	 * @param source
	 * @param dest
	 */
	public static void copyFile(String source, String dest) {
		FileInputStream inFile = null;
		FileOutputStream outFile = null;
		try {
			File in = new File(source);
			File out = new File(dest);
			if(!out.getParentFile().exists()){
				out.getParentFile().mkdirs();
			}
			inFile = new FileInputStream(in);
			outFile = new FileOutputStream(out);
			byte[] buffer = new byte[1024];
			int i = 0;
			while ((i = inFile.read(buffer)) != -1) {
				outFile.write(buffer, 0, i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inFile.close();
				outFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
