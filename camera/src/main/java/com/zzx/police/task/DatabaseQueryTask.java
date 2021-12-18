package com.zzx.police.task;

import android.content.Context;
import android.os.AsyncTask;

import com.zzx.police.MainActivity;
import com.zzx.police.command.BaseProtocol;
import com.zzx.police.data.FileInfo;
import com.zzx.police.data.StoragePathConfig;
import com.zzx.police.data.Values;
import com.zzx.police.utils.DatabaseUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2014/7/10.
 */
public class DatabaseQueryTask extends AsyncTask<String, Integer, Boolean> {
    private static final String TAG = DatabaseUtils.TAG;
    private Context mContext;
    private String mCenterTime;

    public DatabaseQueryTask(Context mContext, String centerTime) {
        this.mContext = mContext;
        this.mCenterTime = centerTime;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Values.LOG_I(TAG, "type = " + params[0] + "; createDate = " + params[1] + "; startTime = " + params[2] + "; endTime = " + params[3]);
        int type = Integer.parseInt(params[0]);
        String createDate;
        if (params[1].length() == 6) {
            createDate = "20" + params[1];
        } else {
            createDate = params[1];
        }
        String startTime  = params[2];
        String endTime    = params[3];
        List<FileInfo> list = DatabaseUtils.queryHistory(mContext, params[0], createDate, startTime, endTime, null);
        if (list == null || list.isEmpty()) {
            return false;
        }
        List<Integer> positionList = new ArrayList<>();
        Values.LOG_I(TAG, "list.size = " + list.size());
        List<Long> sizeList  = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            FileInfo info = list.get(i);
            File file;
            String dirPath = null;
            switch (type) {
                case Values.FILE_TYPE_VID:
                    dirPath = StoragePathConfig.getVideoDirPath(mContext);
                    break;
                case Values.FILE_TYPE_SND:
                    dirPath = StoragePathConfig.getSoundDirPath(mContext);
                    break;
                case Values.FILE_TYPE_IMG:
                    dirPath = StoragePathConfig.getPicDirPath(mContext);
                    break;
            }
            dirPath += File.separator + info.mCreateDate;
            file = new File(dirPath, info.mFileName);
            Values.LOG_I(TAG, "file = " + file.getName());
            if (!file.exists()) {
                Values.LOG_W(TAG, " NOT exists! position = " + i);
                positionList.add(i);
            } else {
                sizeList.add(file.length());
            }
        }
        if (!positionList.isEmpty()) {
            String[] deleteList = new String[positionList.size()];
            for (int i = positionList.size() - 1; i >= 0; i--) {
                int position = positionList.get(i);
                Values.LOG_I(TAG, "deletePosition = " + position);
                deleteList[i] = list.get(position).mFileName;
                list.remove(position);
            }
            DatabaseUtils.deleteHistory(mContext, deleteList);
        }
        positionList.clear();
        String info = getHistoryInfo(list, sizeList);
        MainActivity.mProtocolUtils.reportHistoryListInfo(mCenterTime, info);
        return null;
    }

    private String getHistoryInfo(List<FileInfo> list, List<Long> sizeList) {
        int totalCount  = list.size();
        StringBuilder sizeBuilder   = new StringBuilder();
        StringBuilder startBuilder  = new StringBuilder();
        StringBuilder endBuilder    = new StringBuilder();
        StringBuilder nameBuilder   = new StringBuilder();
        StringBuilder typeBuilder   = new StringBuilder();
        StringBuilder roadBuilder   = new StringBuilder();
        for (int i = 0; i < totalCount; i++) {
            FileInfo info = list.get(i);
            sizeBuilder.append(sizeList.get(i) + BaseProtocol.COMMA);
            startBuilder.append(info.mStartTime + BaseProtocol.COMMA);
            endBuilder.append(info.mEndTime + BaseProtocol.COMMA);
            nameBuilder.append(info.mFileName + "|");
            typeBuilder.append("1,");
            roadBuilder.append("0001,");
        }
        String info = totalCount + BaseProtocol.COMMA + 0 + BaseProtocol.COMMA + totalCount + BaseProtocol.COMMA + sizeBuilder.toString()
                + startBuilder.toString() + endBuilder.toString() + typeBuilder.toString() + roadBuilder.toString()
                + nameBuilder.toString();
        Values.LOG_I(TAG, "historyInfo = " + info);
        return info;
    }

    private void queryFile(String dirPath, String createDate, long startTime, long endTime) {
        File file = new File(dirPath, createDate);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }
        String[] list = file.list(new Filter(startTime, endTime));
        for (String name : list) {
            Values.LOG_I(TAG, name);
        }
    }

    class Filter implements FilenameFilter {
        long startTime  = 0;
        long endTime    = 0;

        Filter(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        public boolean accept(File dir, String filename) {
            String time = filename.split("_")[3];
            long startTime = Long.parseLong(time.split("\\.")[0]);
            return startTime >= this.startTime && startTime <= this.endTime;
        }
    }
}
