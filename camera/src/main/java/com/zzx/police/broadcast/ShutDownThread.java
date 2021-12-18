package com.zzx.police.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zzx.police.MainActivity;
import com.zzx.police.ZZXConfig;
import com.zzx.police.data.Values;
import com.zzx.police.services.CameraService;
import com.zzx.police.utils.EventBusUtils;

/**@author Tomy
 * Created by Tomy on 2015-05-28.
 */
public class ShutDownThread extends BroadcastReceiver {
    Intent intent4;
    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        Log.e("onReceive: ", action);
        switch (action) {
            case Intent.ACTION_SHUTDOWN:
                Intent intent1 = new Intent(Values.ACTION_RELEASE_CAMERA);
                intent1.setClass(context, CameraService.class);
                context.startService(intent1);

                Intent intent2 = new Intent("stopRecord");
                context.sendBroadcast(intent2);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.START_CAMERA_SERVICE);
                ZZXConfig zzxConfig = new ZZXConfig(context);
                if (zzxConfig.is4GA_CDHN || ZZXConfig.isP5C_CDHN || ZZXConfig.isP5X_CDHN){
                    Intent intent3 = new Intent();
                    intent3.setClass(context, MainActivity.class);
                    intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent3);
                }
                intent4 = new Intent();
                intent4.setClassName(Values.PACKAGE_NAME_UVC,Values.CLASS_NAME_UVC);
                intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            mContext.startActivity(intent4);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();

                break;
        }
    }
}
