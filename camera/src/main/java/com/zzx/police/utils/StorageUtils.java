package com.zzx.police.utils;

import android.content.Context;
import android.os.storage.StorageManager;

import java.lang.reflect.Method;

/**@author Tomy
 * Created by Tomy on 2014/8/20.
 */
public class StorageUtils {
    private static final String METHOD_GET_VOLUME_LIST = "getVolumePaths";

    public static StorageManager getInstance(Context context) {
        return (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
    }

    public static boolean isExternalStorageMonted(Context context) {
        Class storage = StorageManager.class;
        try {
            Method getVolumeList = storage.getMethod(METHOD_GET_VOLUME_LIST, (Class[]) null);
            getVolumeList.setAccessible(true);
            String[] objects = (String[]) getVolumeList.invoke(getInstance(context), (Object[]) null);
            if (objects.length >= 2) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
