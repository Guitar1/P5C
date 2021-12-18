package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 14-3-19.
 */
public class VehicleFiles extends BaseProtocol {
    public static final String COMMAND = "V0";
    public String mCenterCommand = "";
    public String mCenterTime   = "";
    public int mReplyMode   = 0;
    public String mReplyResult = "";
    public String mStartTime    = "";
    public String mEndTime      = "";
    private String mHistoryList = "";

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mCenterCommand + COMMA + mCenterTime + COMMA
                + mReplyMode + COMMA + mReplyResult + COMMA + mHistoryList + SUFFIX;
    }


}