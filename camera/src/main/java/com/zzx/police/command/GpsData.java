package com.zzx.police.command;


/**@author Tomy
 * Created by Tomy on 2015/12/31 0031.
 */
public class GpsData {
    public String mPoliceNumber;
    public String mLongitude;
    public String mLatitude;
    public String mSpeed;
    public int mDirection = 0;
    public String mDateTime;

    @Override
    public String toString() {
        return mPoliceNumber + "," + mLongitude + "," + mLatitude + ","
                + mSpeed + "," + mDirection + "," + mDateTime;
    }
}
