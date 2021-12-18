package com.zzx.police.utils;

import android.os.SystemClock;

/**
 * Created by zdm on 2018/11/23 0023.
 * 功能：
 */
public class CheckUtils {
    private static final long DEFULT_TIME_DURATION = 2000;
    private static long mLasttime = 0;

    public  boolean IsContinuousClick() {
        if (mLasttime != 0) {
            long duration = System.currentTimeMillis() - mLasttime;
            if (duration < DEFULT_TIME_DURATION) {
                return false;
            } else {
                mLasttime = System.currentTimeMillis();
            }
        } else {
            mLasttime = System.currentTimeMillis();
        }
        return true;
    }

    public static boolean IsContinuousClick(long durationtime) {
        if (mLasttime != 0) {
            long duration = SystemClock.elapsedRealtime() - mLasttime;
            if (duration < durationtime) {
                return false;
            } else {
                mLasttime = SystemClock.elapsedRealtime();
            }
        } else {
            mLasttime = SystemClock.elapsedRealtime();
        }
        return true;
    }

}
