package com.zzx.police.utils;

import android.app.NotificationManager;
import android.content.Context;

import static android.app.Notification.Builder;

/**@author Tomy
 * Created by Tomy on 2014/7/10.
 */
public class NotificationUtils {
    private NotificationManager mNotifyManager = null;
    private Builder mNotifyBuilder  = null;

    public NotificationUtils(Context context) {
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyBuilder = new Builder(context);
    }

    public void sendNotification(String title, String msg, int iconId) {
        mNotifyBuilder.setAutoCancel(false);
        mNotifyBuilder.setOngoing(true);
    }
}
