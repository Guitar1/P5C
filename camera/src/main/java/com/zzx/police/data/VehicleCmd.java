package com.zzx.police.data;

public class VehicleCmd {
    public String event = "" ;//案件号
    public String message = "";//事件消息
    public String operation = "";//操作样式 录音/录像/拍照
    public String state = "";//开关

    public VehicleCmd(String event,String message,String operation,String state){
        this.event = event;
        this.message = message;
        this.operation = operation;
        this.state = state;
    }
}
