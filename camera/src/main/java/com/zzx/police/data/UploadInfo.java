package com.zzx.police.data;

/**@author Tomy
 * Created by Tomy on 2014/7/4.
 */
public class UploadInfo {
    public UploadInfo(String mFileName, String mRemoteDir) {
        this.mFileName = mFileName;
        this.mRemoteDir = mRemoteDir;
    }

    public UploadInfo() {
    }

    public String mFileName = null;
    public String mRemoteDir    = null;
}
