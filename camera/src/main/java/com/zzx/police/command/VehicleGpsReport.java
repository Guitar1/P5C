package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 14-3-18.
 */
public class VehicleGpsReport extends BaseProtocol {
    private static final String TAG = "VehicleGpsReport: ";
    public final String COMMAND = "V30";
    public String mDriverMark = "1";
//    public VehicleStatus mVehicleStatus = new VehicleStatus();

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mDriverMark + SUFFIX;
    }

}
