package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/5/20.
 */
public class VehicleListInfoReply extends BaseProtocol {
    public static final String COMMAND = "V39";
    public String mCenterTime   = "";
    public String mHasList = "";
    public String mListVersion  = "";

//    public VehicleStatus mVehicleStatus = new VehicleStatus();
    @Override
    protected String getCommandSuffix() {
        String suffix = mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mListVersion + COMMA + mCenterTime + COMMA + mVehicleStatus.getValue() + mHasList + SUFFIX;
        return suffix;
    }
}
