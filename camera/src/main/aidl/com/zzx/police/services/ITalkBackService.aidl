// ITalkBackService.aidl
package com.zzx.police.services;

// Declare any non-default types here with import statements

interface ITalkBackService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
    boolean requestTalkBack();
    List<String> getChannelList();
    boolean selectChannel(String channel);
    void startTalkBack();
    void stopTalkBack();
}
