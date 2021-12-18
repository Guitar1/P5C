package com.zzx.police.data;

/**@author Tomy
 * Created by Tomy on 2015/7/25.
 */
public class CmdEnum {
    public static final int CMD_Invalid   = 0x00;		//无效命令
    public static final int CMD_Login     = 0x01;		//登陆到服务器
    public static final int CMD_Heartbeat = 0x02;		//心跳包
    public static final int CMD_ACK       = 0x20;	//命令应答
    public static final int CMD_Video     = 0x03;		//视频数据
    public static final int CMD_GPS     = 5;		//音频数据
    public static final int CMD_Info      = 6;		//设备信息
    public static final int CMD_Capability= 7;		//设备能力
    public static final int CMD_LBS       = 8;		//位置信息
    public static final int CMD_StartLBS  = 9;		//开启位置信息
    public static final int CMD_StopLBS   = 10;		//停止位置信息
    public static final int CMD_StartVideo= 0x22;		//开始视频流
    public static final int CMD_StopVideo = 0x23;		//停止视频流
    public static final int CMD_StartAudio= 13;		//开始音频流
    public static final int CMD_StopAudio = 14;		//停止音频流
    public static final int CMD_StartTalk = 15;		//开始语音对讲
    public static final int CMD_StopTalk  = 16;		//停止语音对讲
    public static final int CMD_Custom    = 17;		//自定义命令
    public static final int CMD_Max       = 18;

    /** 代表此为数据包. **/
    public static final int DATA = 0x24;
}
