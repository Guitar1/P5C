package com.zzx.police.data;

/**@author Tomy
 * Created by Tomy on 2014/7/9.
 */
public class FileInfo {
    public String mFileName     = "";
    public long mStartTime      = 0;
    public long mEndTime        = 0;
    public int mFileType        = Values.FILE_TYPE_VID;
    public String mCreateDate   = "";
    public String mDirName      = "";
    public boolean mSuccess     = false;
    public String mFilePath = "";

    @Override
    public String toString() {
        return "FileInfo{" +
                "mFileName='" + mFileName + '\'' +
                ", mStartTime=" + mStartTime +
                ", mEndTime=" + mEndTime +
                ", mFileType=" + mFileType +
                ", mCreateDate='" + mCreateDate + '\'' +
                '}';
    }
}
