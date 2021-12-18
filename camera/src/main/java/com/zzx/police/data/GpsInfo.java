package com.zzx.police.data;


/**@author Tomy
 * Created by Tomy on 2015/12/31 0031.
 */
public class GpsInfo {
    private BaseCmd mBaseCmd;
    public GpsInfo(int policeNum, int length) {
        mBaseCmd = new BaseCmd();
        mBaseCmd.mId = policeNum;
        mBaseCmd.mMinorCmd = CmdEnum.DATA;
        mBaseCmd.mCmd = CmdEnum.CMD_GPS;
        mBaseCmd.mCmdSize = BaseCmd.BASE_CMD_SIZE;
        mBaseCmd.mDataLen = length;
    }

    public byte[] getByte() {
        return mBaseCmd.getBytesSmall();
    }
}
