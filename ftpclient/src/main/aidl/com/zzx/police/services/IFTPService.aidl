// IFTPService.aidl
package com.zzx.police.services;

// Declare any non-default types here with import statements

interface IFTPService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
    void uploadFile(in String filePath, in String remoteDir);
}
