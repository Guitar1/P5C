package com.zzx.police.manager;

import android.content.Context;
import android.provider.Settings;

import com.zzx.police.R;
import com.zzx.police.ZZXConfig;
import com.zzx.police.utils.RemotePreferences;

/**@author Tomy
 * Created by Tomy on 2014/6/30.
 */
public class RecordManager {
    private Context mContext = null;

    public RecordManager(Context mContext) {
        this.mContext = mContext;
    }

    public boolean needSound() {
        return RemotePreferences.getRemotePreferences(mContext).getBoolean(mContext.getString(R.string.key_record_sound), true);
    }

    public boolean needBootRecord() {
        return RemotePreferences.getRemotePreferences(mContext).getBoolean(mContext.getString(R.string.key_record_boot), false);
    }


    public boolean isAutoRecord() {
        return RemotePreferences.getRemotePreferences(mContext).getBoolean(mContext.getString(R.string.key_record_auto), true);
    }

    public boolean isLoopRecord() {
        return RemotePreferences.getRemotePreferences(mContext).getBoolean(mContext.getString(R.string.key_record_loop), true);
    }

    public int getRecorderTime() {
        int time = Integer.parseInt(RemotePreferences.getRemotePreferences(mContext).getString(mContext.getString(R.string.key_record_section), "10"));
        if (ZZXConfig.is4G5_GJDW){
            time = Integer.parseInt(RemotePreferences.getRemotePreferences(mContext).getString(mContext.getString(R.string.key_record_section), "120"));
        }
        return time * 60 * 1000;
    }
}
