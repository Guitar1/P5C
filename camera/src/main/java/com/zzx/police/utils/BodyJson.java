package com.zzx.police.utils;


public class BodyJson {
    private int type;
    private byte[] stream;
    public BodyJson() {

    }
    public BodyJson(int type, byte[] stream) {
        this.type = type;
        this.stream = stream;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getStream() {
        return stream;
    }

    public void setStream(byte[] stream) {
        this.stream = stream;
    }

}
