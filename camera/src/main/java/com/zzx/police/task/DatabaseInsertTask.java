package com.zzx.police.task;

import android.content.Context;
import android.os.AsyncTask;

import com.zzx.police.data.FileInfo;
import com.zzx.police.data.Values;
import com.zzx.police.utils.DatabaseUtils;

import java.io.File;

/**@author Tomy
 * Created by Tomy on 2014/7/10.
 */
public class DatabaseInsertTask extends AsyncTask<String, Integer, Boolean> {
    private static final String TAG = "DatabaseTask: ";
    private Context mContext;

    public DatabaseInsertTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        FileInfo info = new FileInfo();
        File file = new File(params[0]);
        String fileName = file.getName();
        String[] fileInfo = fileName.split("_");
        info.mFileType  = Integer.parseInt(fileInfo[1]);
        info.mCreateDate    = fileInfo[2];
        info.mStartTime = Long.parseLong(fileInfo[3].split("\\.")[0]);
        info.mEndTime   = Long.parseLong(params[1]);
        info.mFileName  = fileName;
        Values.LOG_I(TAG, info);
        return DatabaseUtils.insertHistory(mContext, info);
    }
}
