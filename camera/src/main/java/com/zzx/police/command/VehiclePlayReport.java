package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/5/28.
 */
public class VehiclePlayReport extends BaseProtocol {
    public static final String COMMAND = "V32";
//    public VehicleStatus mVehicleStatus = new VehicleStatus();
    public int mPlayStatus = 0;
    public String mFileName = "";
    public String mSendTime = "";
    public int mPlayType = 0;
    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA + mSendTime + COMMA
                + mVehicleStatus.getValue() + mPlayStatus + COMMA + mFileName + COMMA
                + mPlayType + SUFFIX;
    }
}
