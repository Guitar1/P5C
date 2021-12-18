package com.zzx.police.autofocus;

/**
 * Created by wanderFly on 2017/6/30.
 */

public interface IServiceLifecycle {
    void onCreate();
    void onStartCommand();
    void onDestroy();
}
