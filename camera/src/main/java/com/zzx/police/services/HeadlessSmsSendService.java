package com.zzx.police.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**@author Tomy
 * Created by Tomy on 2015-04-20.
 */
public class HeadlessSmsSendService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
