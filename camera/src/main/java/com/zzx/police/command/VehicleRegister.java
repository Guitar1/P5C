package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 14-3-18.
 */
public class VehicleRegister extends BaseProtocol {

    private static final String TAG = "VehicleRegister: ";
    public static final String COMMAND = "V1";
    public String mProtocolVersion = "0.0.1.11";
    public String mDeviceType   = "0600";
    public int mBootCount       = 1;
    public int mConnectCount    = 1;
    public String mCityNum  = "0755";
    public String mPlateNum = "B888888";
    private String mServiceIp = "";
    private int mServicePort = 0;
//    public VehicleStatus mVehicleStatus = new VehicleStatus();
    public boolean IsG726 = true;
    private String G726 = "g726";
    private String AAC = "aac";

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mProtocolVersion + COMMA + mDeviceType + COMMA + SERVICE_IP + ":" + SERVICE_PORT + COMMA
                + mBootCount + COMMA + mConnectCount + COMMA + mCityNum + COMMA
                + mPlateNum + (IsG726 ? G726 : AAC) + SUFFIX;
    }

}
