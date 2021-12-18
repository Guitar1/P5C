package com.zzx.police.data;

import com.zzx.police.utils.ByteIntConvert;

import java.nio.ByteBuffer;



/**@author Tomy
 * Created by Tomy on 2015/7/25.
 */
public class BaseCmd {
    public static final int BASE_CMD_SIZE = 40;
    public static final byte BEAT_3 = '}';
    public static final byte BEAT_2 = 'C';
    public static final byte BEAT_1 = 'M';
    public static final byte BEAT_0 = '{';
    public byte[] signature = new byte[] {'{', 'M', 'C', '}'};
    public byte[] signatureSmall = new byte[] {'}', 'C', 'M', '{'};
    public int mId;
    public int mCmd;
    public int mMinorCmd;
    public int mParam;
    public int mFlag;
    public int mReserved;
    public int mCmdSize;
    public int mExtendCount = 0;
    public int mDataLen;

    /**转换为大端存储.
     * */
    public byte[] getBytesBig() {
        ByteBuffer buffer = ByteBuffer.allocate(40);
        buffer.put(signature);
        buffer.put(ByteIntConvert.intToByteArrayBig(mId));
        buffer.put(ByteIntConvert.intToByteArrayBig(mCmd));
        buffer.put(ByteIntConvert.intToByteArrayBig(mMinorCmd));
        buffer.put(ByteIntConvert.intToByteArrayBig(mParam));
        buffer.put(ByteIntConvert.intToByteArrayBig(mFlag));
        buffer.put(ByteIntConvert.intToByteArrayBig(mReserved));
        buffer.put(ByteIntConvert.intToByteArrayBig(mCmdSize));
        buffer.put(ByteIntConvert.intToByteArrayBig(mExtendCount));
        buffer.put(ByteIntConvert.intToByteArrayBig(mDataLen));
        return buffer.array();
    }

    /**转换为小端存储.
     * */
    public byte[] getBytesSmall() {
        ByteBuffer buffer = ByteBuffer.allocate(40);
        buffer.put(signature);
        buffer.put(ByteIntConvert.intToByteArraySmall(mId));
        buffer.put(ByteIntConvert.intToByteArraySmall(mCmd));
        buffer.put(ByteIntConvert.intToByteArraySmall(mMinorCmd));
        buffer.put(ByteIntConvert.intToByteArraySmall(mParam));
        buffer.put(ByteIntConvert.intToByteArraySmall(mFlag));
        buffer.put(ByteIntConvert.intToByteArraySmall(mReserved));
        buffer.put(ByteIntConvert.intToByteArraySmall(mCmdSize));
        buffer.put(ByteIntConvert.intToByteArraySmall(mExtendCount));
        buffer.put(ByteIntConvert.intToByteArraySmall(mDataLen));
        return buffer.array();
    }

    @Override
    public String toString() {
        return mId + "," + mCmd + "," + mMinorCmd + "," + mParam + "," + mFlag + "," + mReserved + "," + mCmdSize + "," + mExtendCount + "," + mDataLen + ";";
    }
}
