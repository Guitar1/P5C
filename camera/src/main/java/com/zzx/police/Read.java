package com.zzx.police;

import android.content.Context;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;

/**@author Tomy
 * Created by Tomy on 2014/10/11.
 */
public class Read {
    public static void read() {
        int lid = 41;
        IBinder binder = null;
        Log.i("tomy", "barcode = start read");
        try {
            Class service = Class.forName("android.os.ServiceManager");
            Method method = service.getMethod("getService", String.class);
            binder = (IBinder) method.invoke(null, "NvRAMAgent");
        } catch (Exception e) {

        }
        NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
        while (lid-- >= 20) {
            Log.e("tomy", "lid = " + lid);
            try {
                byte[] barcode = agent.readFile(lid);
                Log.e("tomy", "barcode = " + Arrays.toString(barcode));
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error error) {
                error.printStackTrace();
            }
        }
    }

    public Read() {

    }

    public void readBarcode(Context context, Message msg) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method readBarcode = TelephonyManager.class.getMethod("getMobileRevisionAndIMEI", Integer.TYPE, Message.class);
            readBarcode.setAccessible(true);
            readBarcode.invoke(manager, 5, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object o = msg.obj;
    }
}
