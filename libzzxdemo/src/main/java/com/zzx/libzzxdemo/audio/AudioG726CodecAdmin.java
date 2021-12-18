package com.zzx.libzzxdemo.audio;

import com.zzx.police.utils.AudioEncoder;

import java.util.Arrays;

class AudioG726CodecAdmin
        implements AudioInterface.Decode<byte[]>, AudioInterface.Encode<byte[]> {

    private byte[] mData;
    private AudioEncoder mAudioEncode;

    AudioG726CodecAdmin(boolean encode) {
        if (encode) {
            mAudioEncode = new AudioEncoder();
        }
        mData = new byte[1024];
    }

    @Override
    public void start() {

    }

    @Override
    public void release() {

    }

    @Override
    public byte[] encode(byte[] data) {
        int len = mAudioEncode.encode(data, data.length, mData, mData.length);
        if (len > 0) {
            return Arrays.copyOfRange(mData, 0, len);
        }
        return new byte[0];
    }

    @Override
    public byte[] decode(byte[] data) {
        if (data.length * 8 > mData.length) {
            mData = new byte[data.length * 8];
        }
        int len = AudioEncoder.g726_decode(data, data.length, mData, mData.length);
        if (len > 0) {
            return Arrays.copyOfRange(mData, 0, len);
        }
        return new byte[0];
    }

}
