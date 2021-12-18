package com.zzx.police.data;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.zzx.police.ZZXConfig;

import java.io.File;

/**
 * Created by zdm on 2018/10/30 0030.
 * 功能：
 */
public class StoragePathConfig {
    public static final String DEFULT_ROOT_PATH = "/DCIM";

    public static String getStorageRootPath(Context context) {
        String rootPath =  "/police";
        return rootPath;
    }

    public static boolean setStorageRootPath(Context context, @NonNull String rootPath) {
        return Settings.System.putString(context.getContentResolver(), "zzx_storage_root_path", rootPath);
    }

    public static String getPicDirPath(Context context) {
        return Environment.getExternalStorageDirectory() + getStorageRootPath(context) + File.separatorChar + "pic";
    }

    public static String getVideoDirPath(Context context) {
        return Environment.getExternalStorageDirectory() + getStorageRootPath(context) + File.separatorChar + "video";
    }

    public static String getSoundDirPath(Context context) {
        return Environment.getExternalStorageDirectory() + getStorageRootPath(context) + File.separatorChar + "sound";
    }

    public static String getLogDirPath(Context context) {
        return Environment.getExternalStorageDirectory() + getStorageRootPath(context) + File.separatorChar + "log";
    }
    public static String getTextDirPath(Context context) {
        return Environment.getExternalStorageDirectory() + getStorageRootPath(context) + File.separatorChar + "text";
    }


}
