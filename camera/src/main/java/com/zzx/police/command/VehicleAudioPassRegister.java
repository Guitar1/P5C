package com.zzx.police.command;

/**@author Tomy
 * Created by Tomy on 2014/8/11.
 */
public class VehicleAudioPassRegister extends BaseProtocol {
    public static final String COMMAND  = "V5";
    public static final int TYPE_TALK_RECEIVE       = 0;
    public static final int TYPE_TALK_REPORT        = 1;
    public static final int TYPE_MONITOR            = 2;
    public static final int TYPE_CENTER_BROADCAST   = 3;
    public String mVersion  = BaseProtocol.VERSION;
    public String mDeviceType   = BaseProtocol.DEVICE_TYPE;
    public String mServiceIp    = "";
    public String mUUID = "";
    public String mCenterCmd    = "";
    public int mRegisterType    = 0;
    public String mSoundMode    = "";
    public String mChannelCount = "";
    public String mSampleRate   = "";
    public String mBiteSize = "";
    public String mAudioFormat  = "";
    public String mFrameSize    = "";
    public String mAudioSource  = "";
    public String mPassNumber   = "";
    public String mUserName     = "";
    public String mSessionId    = "";
    public String mCityId       = BaseProtocol.CITY_ID;
    public String mCarId        = BaseProtocol.CAR_ID;
    public String  mReciveTime = "";
    public String mCallType = "";

    @Override
    protected String getCommandSuffix() {
        return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mReciveTime + COMMA + mVehicleStatus.getValue() + mVersion + COMMA + mUserName + COMMA + mSessionId
                + COMMA+ mCenterCmd + COMMA + mCallType + SUFFIX;
        /*return mDeviceSerialNum + COMMA + mWorkstationSerialNum + COMMA + COMMAND + COMMA
                + mVehicleStatus.getValue() + mVersion + COMMA + mDeviceType + COMMA
                + mServiceIp + COMMA + mUUID + COMMA + mCenterCmd + COMMA + mRegisterType + COMMA
                + mSoundMode + COMMA + mChannelCount + COMMA + mSampleRate + COMMA + mBiteSize + COMMA
                + mAudioFormat + COMMA + mFrameSize + COMMA + mAudioSource + COMMA + mPassNumber + mCityId + COMMA + mCarId + SUFFIX;*/
    }
}
