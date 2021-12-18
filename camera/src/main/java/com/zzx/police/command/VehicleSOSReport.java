package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/9/16.
 */
public class VehicleSOSReport extends BaseProtocol {
    public static final String COMMAND  = "V60";
    private String mFirstStatusTmp;
    private String mFirstStatus = "";
    public String mUID;
    public int mCount = 0;
    public int mSOSNumber = 0;
    public String mSOSSource;
    public String mSOSInfo;
    public VehicleSOSReport(String firstStatus) {
        mFirstStatusTmp = firstStatus;
    }

    public void increase() {
        mCount++;
        if (mCount == 2) {
            mFirstStatus = mFirstStatusTmp;
        }
    }

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + COMMA + mFirstStatus + COMMA + mUID + COMMA
                + mCount + COMMA + mSOSNumber + COMMA + mSOSSource + COMMA + mSOSInfo + SUFFIX;
    }
}
