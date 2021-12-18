package com.zzx.phone;

import android.app.Application;

import com.zzx.phone.view.PhoneView;

/**@author Tomy
 * Created by Tomy on 2014/6/24.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        new PhoneView(this).setUpListener();
        super.onCreate();
    }
}
