package com.zzx.police.utils;


public class HeaderJson {
    private String version;
    private int pid;
    private String uid;
    public HeaderJson() {

    }

    public HeaderJson(String version, int pid, String uid) {
        this.version = version;
        this.pid = pid;
        this.uid = uid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
