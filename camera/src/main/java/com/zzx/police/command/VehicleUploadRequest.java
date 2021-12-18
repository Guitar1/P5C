package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/7/2.
 */
public class VehicleUploadRequest extends BaseProtocol {
    public static final String COMMAND  = "V111";
    public static final int STATUS_REQUEST  = 1;
    public static final int STATUS_FINISH   = 2;

    public int mStatus  = 1;
    public String mFileName = "";
    public String mFileMD5  = "";
    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mStatus + COMMA + mFileName + COMMA
                + mFileMD5 + SUFFIX;
    }
}
