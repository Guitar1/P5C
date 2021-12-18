package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/6/18.
 */
public class VehicleFileUpload extends BaseProtocol {
    public static final String COMMAND = "V0";
    public String mCenterCommand = "";
    public String mCenterTime   = "";
    public String mFileName = "";
    public String mSuccess = "";
    public String mCause = "";


    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum +COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mCenterCommand + COMMA + mCenterTime
                + COMMA + mFileName + COMMA + mSuccess + COMMA + mCause + COMMA +SUFFIX;
    }
}
