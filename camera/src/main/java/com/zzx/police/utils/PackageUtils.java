package com.zzx.police.utils;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;
import java.lang.reflect.Method;

/**@author DonkeyTomy
 * Created by DonkeyTomy on 2016/4/11.
 */
public class PackageUtils {
    public static boolean isPackageInstalled(Context context, String pkgName) {
        PackageManager packageManager = context.getPackageManager();
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

    /**
     * Flag parameter for {@link #installPackage} to indicate that you want to replace an already
    * installed package, if one exists.
     * */
    public static final int INSTALL_REPLACE_EXISTING = 0x00000002;
    /**安装Apk文件,判断是否已安装该包名以及是否要覆盖安装.
     * 安装完后observer会收到安装完毕结果.安装完一个再安装下一个.
     * */
    public static void installPackage(IPackageInstallObserver observer, Context context, String apkPath, boolean replace, String pkgName) {
        File apkFile = new File(apkPath);
        if (!apkFile.exists() || apkFile.isDirectory()) {
            return;
        }
        PackageManager manager = context.getPackageManager();
        boolean exists = isPackageInstalled(context, pkgName);
        if (!replace && exists) {
            return;
        }
        try {
            Method installMethod = PackageManager.class.getMethod("installPackage", Uri.class, IPackageInstallObserver.class, Integer.TYPE, String.class);
            Uri file = Uri.fromFile(apkFile);
            installMethod.invoke(manager, file, observer, INSTALL_REPLACE_EXISTING, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Flag parameter for {@link #uninstallPackage} to indicate that you want the
     * package deleted for all users.
     *
     */
    public static final int DELETE_ALL_USERS = 0x00000002;

    public static void uninstallPackage(Context context, IPackageDeleteObserver observer, String pkgName) {
        PackageManager mManager = context.getPackageManager();
        try {
            Class packageClass  = mManager.getClass();
            Method[] methods = packageClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePackage")) {
                    method.setAccessible(true);
                    method.invoke(mManager, pkgName, observer, DELETE_ALL_USERS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
