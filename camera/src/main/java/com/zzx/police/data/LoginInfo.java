package com.zzx.police.data;

import android.util.Log;

import java.nio.ByteBuffer;

/**@author Tomy
 * Created by Tomy on 2015/7/28.
 */
public class LoginInfo {
    private static final String TAG = "LoginInfo";
    public BaseCmd mBaseCmd;
    private String mUser;
    private String mPwd;
    public static final int CMD_SIZE = 40;

    public LoginInfo(String user, String pwd) {
        if (user == null || user.equals("") || pwd == null) {
            throw new NullPointerException("user,pwd can not be NULL!");
        }
        mBaseCmd = new BaseCmd();
        mBaseCmd.mCmdSize = CMD_SIZE;
        mBaseCmd.mCmd   = CmdEnum.CMD_Login;
        mBaseCmd.mMinorCmd = 0x22;
        mUser   = user;
        mPwd    = pwd;
        mBaseCmd.mDataLen = mUser.length();
    }

    public byte[] getByte() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(CMD_SIZE + mUser.length());
        byteBuffer.put(mBaseCmd.getBytesSmall());
        byteBuffer.put(mUser.getBytes());
//        byteBuffer.put(mPwd.getBytes());
        Log.d(TAG, mBaseCmd.toString());
        return byteBuffer.array();
    }
}
