package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/6/18.
 */
public class VehicleVideoAuidoRegister extends BaseProtocol {
    public static final String COMMAND = "V0";
    public String mVersion = BaseProtocol.VERSION;
    public String mDeviceType   = BaseProtocol.DEVICE_TYPE;
    public String mServiceAddress = "";
    public String mSessionId    = "";
    public String mSessionCmd   = "";
    public String mPassNum  = "";
    public String mCityId   = BaseProtocol.CITY_ID;
    public String mCarId    = BaseProtocol.CAR_ID;

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mVersion + COMMA + mDeviceType + COMMA
                + mServiceAddress + COMMA + mSessionId + COMMA + mSessionCmd + COMMA + mPassNum + COMMA
                + mCityId + COMMA + mCarId + SUFFIX;
    }
}
