package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/10/27.
 */
public class VehicleReceiveAudio extends BaseProtocol {
    public final String COMMAND = "V22";
    public String mSessionId = "";
    public int mSessionType = 4;

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mSessionId + COMMA + mSessionType + SUFFIX;
    }
}
