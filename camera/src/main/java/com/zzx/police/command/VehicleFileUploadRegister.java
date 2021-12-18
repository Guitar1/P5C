package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/7/11.
 */
public class VehicleFileUploadRegister extends BaseProtocol {
    private static final String TAG = "VehicleFileUploadRegister: ";
    public static final String COMMAND = "V2";
    public int mFieldCount      = 11;
    public String mVersion      = BaseProtocol.VERSION;
    public String mDeviceType   = BaseProtocol.DEVICE_TYPE;
    public String mServiceAddress = "";
    public String mSessionId    = "";
    public String mSessionCmd   = "";
    public String mStreamType   = "";
    public String mPassNum      = "";
    public String mStartTime    = "";
    public String mEndTime      = "";
    public String mCityId       = BaseProtocol.CITY_ID;
    public String mCarId        = BaseProtocol.CAR_ID;

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mFieldCount + COMMA + mVersion + COMMA
                + mDeviceType + COMMA + mServiceAddress + COMMA + mSessionId + COMMA
                + mSessionCmd + COMMA + mStreamType + COMMA + mPassNum + COMMA
                + mStartTime + COMMA + mEndTime + COMMA + mCityId + COMMA + mCarId + SUFFIX;
    }
}
