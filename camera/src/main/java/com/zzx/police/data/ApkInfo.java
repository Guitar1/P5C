package com.zzx.police.data;

/**@author Tomy
 * Created by Tomy on 2015-05-08.
 */
public class ApkInfo {
    public ApkInfo() {
        mPackageName    = "";
        mActivityName   = "";
        mApkName        = "";
    }
    public void clear() {
        mPackageName    = "";
        mActivityName   = "";
        mApkName        = "";
    }
    public String mApkName;

    @Override
    public String toString() {
        return "ApkInfo{" +
                "mApkName='" + mApkName + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
                ", mActivityName='" + mActivityName + '\'' +
                '}';
    }

    public String mPackageName;
    public String mActivityName;
}
