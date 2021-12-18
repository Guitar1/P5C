package com.zzx.police.manager;

import android.content.Context;
import android.os.Vibrator;

/**@author Tomy
 * Created by Tomy on 2014-12-03.
 */
public class VibratorManager {
    public static void vibrate(Context context, int millisecond) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(millisecond);
    }
}
