package com.zzx.police.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**@author Tomy
 * Created by Tomy on 2014/7/23.
 */
public class RemotePreferences {
    public static SharedPreferences getRemotePreferences(Context context) {
        /*try {
            Context remoteContext = context.createPackageContext(Values.PACKAGE_NAME_SETTING, Context.CONTEXT_IGNORE_SECURITY);
            return remoteContext.getSharedPreferences(Values.PACKAGE_NAME_SETTING + "_preferences", Context.MODE_MULTI_PROCESS);
        } catch (PackageM   anager.NameNotFoundException e) {
            e.printStackTrace();
        }*/
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
