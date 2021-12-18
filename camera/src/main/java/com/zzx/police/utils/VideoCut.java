package com.zzx.police.utils;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.zzx.police.ZZXConfig;
import com.zzx.police.data.StoragePathConfig;
import com.zzx.police.data.Values;

import java.io.File;

/**@author Tomy
 * Created by Tomy on 2014-12-25.
 */
public class VideoCut {
    private Context mContext;

    public VideoCut(Context mContext) {
        this.mContext = mContext;
    }

    public void cut(final String dir, final int time, final int recordTime, final String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                LogUtil.e("zzx"," recordTime:    "+recordTime);
                int min = recordTime / (60 * time);
                int sec = recordTime % (60 * time);
                if (sec >10){
                    min++;
                }
                Process process =  null;
                Log.e("zzx","min"+min+" sec"+sec+" fileName"+fileName);
//                LogUtil.e("zzx","min"+min+" sec"+sec+" fileName"+fileName);
                try {
                    int startTime   = Integer.parseInt(fileName.substring(18, 24));
                    String startName    = fileName.substring(0, 18);
                    Runtime runtime = Runtime.getRuntime();
                    String path = StoragePathConfig.getVideoDirPath(mContext)+ File.separator + dir;
                    Log.e("zzx","startTime"+startTime+" startName"+startName);
//                    LogUtil.e("zzx","startTime"+startTime+" startName"+startName);
                    File cmdDir = new File(path);
                    String cmd;
                    for (int i = 0; i < min; i++) {
                        String name = String.format("%06d", startTime + time * i * 100);
                        String tmp  = String.format("%02d", i * time);
                        if (i == (min-1)){
//                            LogUtil.e("zzx","i -> "+i+"sec: "+sec);
                            if (sec <= 10){
                                cmd = "ffmpeg -ss 00:" + tmp + ":00 -i " + fileName + " -vcodec copy -acodec copy -t 00:0" + time + ":0" + sec +" "+ startName + name + ".mp4";
                            }else {
                                cmd = "ffmpeg -ss 00:" + tmp + ":00 -i " + fileName + " -vcodec copy -acodec copy -t 00:0" + time + ":01 "+ startName + name + ".mp4";
                            }
                        }else {
                            cmd = "ffmpeg -ss 00:" + tmp + ":00 -i " + fileName + " -vcodec copy -acodec copy -t 00:0" + time + ":01 " + startName + name + ".mp4";
                        }
                        Log.e("zzx","cmd -> "+cmd);
//                        LogUtil.e("zzx","cmd -> "+cmd);
                        process = runtime.exec(cmd, null, cmdDir);
                        process.waitFor();
                        process.destroy();
                    }
                    Log.e("zzx","VideoCut finished");
                    cmd = "rm " + fileName;
                    process = runtime.exec(cmd, null, cmdDir);
                    process.waitFor();
                    Settings.System.putInt(mContext.getContentResolver(), Values.VIDEO_CUT, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("zzx",e.toString());
                } finally {
                    try {
                        if (process != null) {
                            process.destroy();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }){}.start();

    }
}