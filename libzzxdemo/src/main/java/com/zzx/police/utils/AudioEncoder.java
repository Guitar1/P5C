package com.zzx.police.utils;

//g726库的规定路径
public class AudioEncoder {

    static {
        try {
            System.loadLibrary("g726_codec");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int encode(byte[] input, int count, byte[] output, int outputSize){
        return g726_encode(input, count, output, outputSize);
    }


    private native int g726_encode(byte[] input, int count, byte[] output, int outputSize);

    public static native int g726_decode(byte[] input, int count, byte[] output, int outputSize);

}
