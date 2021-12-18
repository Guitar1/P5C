package com.zzx.police.data;


import com.zzx.police.utils.HexCodeConverter;

import java.nio.ByteBuffer;

/**@author Tomy
 * Created by Tomy on 2016/3/21.
 */
public class DataHeader {
    public byte[] mHeader = new byte[] {'{', 'M', 'C', '}'};
    public int mFrameType;
    public long mStreamType;
    public long mFrameLen;
    public long mPts;

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put(mHeader);
        buffer.put(HexCodeConverter.intToByte(mFrameType));
        buffer.put(HexCodeConverter.longToByte(mPts));
        return buffer.array();
    }
}
