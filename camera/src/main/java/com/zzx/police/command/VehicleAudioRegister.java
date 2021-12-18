package com.zzx.police.command;


/**
 * Created by Administrator on 2017/11/16 0016.
 */

public class VehicleAudioRegister extends BaseProtocol {
    public static final String COMMAND = "V0";

    public String mCenterCommand = "";
    public String mCenterTime   = "";
    public int mReplyMode   = 0;
    public String mReplyResult = "";
    public String mUserName     = "";
    public String mSessionId    = "";


    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mCenterCommand + COMMA + mCenterTime + COMMA
                + mReplyMode + COMMA + mReplyResult + COMMA + mUserName + COMMA + mSessionId  + SUFFIX;

    }
}
