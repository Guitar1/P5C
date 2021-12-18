package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/9/18.
 */
public class VehicleRequestVideoOrAudio extends BaseProtocol {
    public VehicleRequestVideoOrAudio(int type) {
        mRequestType = type;
    }
    public static final String COMMAND = "V114";
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;

    private int mRequestType = 0;

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mRequestType + SUFFIX;
    }
}
