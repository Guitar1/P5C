package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 14-3-19.
 */
public class VehicleCmdReply extends BaseProtocol {
    public static final String COMMAND = "V0";
    public String mCenterCommand = "";
    public String mCenterTime   = "";
    public int mReplyMode   = 0;
    public String mReplyResult = "";

    private String mHistoryList = "";

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mCenterCommand + COMMA + mCenterTime + COMMA
                + mReplyMode + COMMA + mReplyResult + COMMA + mHistoryList + SUFFIX;
    }

    public void setHistoryList(String historyList) {
        mHistoryList = historyList;
    }
    public void clearHistory() {
        mHistoryList = "";
    }

}