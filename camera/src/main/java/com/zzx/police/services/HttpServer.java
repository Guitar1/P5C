package com.zzx.police.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zzx.police.utils.XMLUtils;

/**@author Tomy
 * Created by Tomy on 2015-06-20.
 */
public class HttpServer extends Service {
    private WebThread mWebThread;

    @Override
    public void onCreate() {
        super.onCreate();
        initHttpServer();
    }

    private void initHttpServer() {
        if (mWebThread == null) {
            mWebThread = new WebThread(8086, this);
            mWebThread.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebThread != null) {
            mWebThread.release();
            mWebThread.mIsLoop = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
