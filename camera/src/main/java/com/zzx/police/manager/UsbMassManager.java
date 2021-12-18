package com.zzx.police.manager;

import android.content.Context;
import android.os.storage.StorageManager;

import java.lang.reflect.Method;

/**
 * @author Tomy
 *         Created by Tomy on 2014/4/4.
 */
public class UsbMassManager {
    public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
    public static final String USB_CONNECTED = "connected";
    public static final String USB_MASS_FUNCTION = "mass_storage";
    public static final String USB_ADB = "adb";
    private StorageManager mStorageManager = null;
    private Class<?> mStorageClass = null;
    private Method mIsMassEnabled = null;
    public static final String mEnableMethod = "enableUsbMassStorage";
    public static final String mDisableMethod = "disableUsbMassStorage";
    public static final String mIsMassConnectedMethod = "isUsbMassStorageConnected";
    public static final String mIsMassEnabledMethod = "isUsbMassStorageEnabled";

    public UsbMassManager(Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        mStorageClass = mStorageManager.getClass();
    }

    public boolean isMassEnabled() {
        Boolean isEnabled = false;
        try {
            if (mIsMassEnabled == null)
                mIsMassEnabled = mStorageClass.getMethod(mIsMassEnabledMethod, (Class[]) null);
            isEnabled = (Boolean) mIsMassEnabled.invoke(mStorageManager, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isEnabled;
    }

    public boolean isMassStorageConnected() {
        Boolean isConnected = false;
        try {
            Method isMassStorageConnected = mStorageClass.getMethod(mIsMassConnectedMethod, (Class[]) null);
            isConnected = (Boolean) isMassStorageConnected.invoke(mStorageManager, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    public void enableUsbMass() {
        if (isMassEnabled()) {
            return;
        }
        try {
            Method enable = mStorageClass.getMethod(mEnableMethod);
            enable.invoke(mStorageManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disableUsbMass() {
        if (!isMassEnabled()) {
            return;
        }
        try {
            Method disable = mStorageClass.getMethod(mDisableMethod, (Class[]) null);
            disable.invoke(mStorageManager, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}