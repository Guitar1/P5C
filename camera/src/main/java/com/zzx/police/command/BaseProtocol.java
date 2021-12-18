package com.zzx.police.command;


/**@author Tomy
 * Created by Tomy on 14-3-18.
 */
public abstract class BaseProtocol {
    public static final String TAG = "BaseProtocol: ";
    public static final String SUCCESS  = "1";
    public static final String SECOND   = "2";
    public static final String THIRD    = "3";
    public static final String FAILED   = "0";
    public static final String SUFFIX   = "#";
    /** 协议指令关键字 **/
    public static final String CENTER_REPLY             = "C0";
    public static final String CENTER_HEARD_BEAT_SET    = "C3";
    /** 中心发起设置GPS间隔指令  */
    public static final String CENTER_GPS_SET           = "C30";
    public static final String CENTER_DOWNLOAD_MEDIA    = "C31";
    /**  */
    public static final String CENTER_REAL_TIME_VIDEO   = "C34";
    public static final String CENTER_SHUT_DOWN         = "C36";
    public static final String CENTER_SET_PARAMETER     = "C37";
    public static final String CENTER_FTP_DOWNLOAD      = "C38";
    public static final String CENTER_GET_LIST_INFO     = "C39";
    public static final String CENTER_DOWNLOAD_LIST_TXT = "C40";
    /** 中心发起历史查询请求 */
    public static final String CENTER_HISTORY_QUERY     = "C110";
    public static final String CENTER_MESSAGE     = "C111";
    /**
     * 连接对讲服务器
     * */
    public static final String CENTER_TALK_BACK_SERVER  = "C101";
    /** 中心发起对讲请求 */
    public static final String CENTER_TALK_BACK         = "C130";
    /** 新的对讲请求*/
    public static final String CENTER_NEW_TALK_BACK     = "C136";
    /**视音频同传**/
    public static final String CENTER_VIDEO_AUDIO       = "C137";
    /** 中心告诉设备有文件需要下载到设备上*/
    public static final String CENTER_FILE_DOWNLOAD     = "C215";
    /** 中心发起语音监控请求 */
    public static final String CENTER_MONITOR           = "C131";
    /** 中心请求上传历史视频记录 */
    public static final String CENTER_HISTORY_UPLOAD    = "C500";
    /** 中心下发文件上传FTP配置信息 */
    public static final String CENTER_FTP_INFO          = "C600";
    public static final String CENTER_REMOTE_CONTROL    = "C601";
    /**中心下发获取文件列表**/
    public static final String CENTER_FILE_LIST         = "C216";
    /**中心下发需要上传文件**/
    public static final String CENTER_FILE              = "C217";

    public static final String CENTER_TALK_BACK_SERVER_NEW  = "C666";

    public static final String CENTER_RTMP  = "C360";
    /**中心下发指令控制录音录像拍照**/
    public static final String CENTER_TYPE = "C334";

    public static final String COMMA = ",";
    public static final String SERVICE_IP  = "219.136.187.242";
//    public static final String SERVICE_IP  = "123.129.222.131";
    public static final int SERVICE_PORT   = 6025;
    public static final String PREFIX = "99dc";
    public static final String HEAD_BEAT = "90dc";
    public static final String VERSION  = "0.0.1.11";
    public static final String DEVICE_TYPE = "0600";
    public static final String CITY_ID  = "0755";
    public static final String CAR_ID = "";

    protected String mCommandLen = "";
    public static String mDeviceSerialNum   = "";
    public String mWorkstationSerialNum = "";

    public VehicleStatus mVehicleStatus = new VehicleStatus();

    protected String mCommandSuffix = null;
    private String getCommandLen() {
        mCommandSuffix = getCommandSuffix();
        mCommandLen = String.format("%04d", mCommandSuffix.length());
        return mCommandLen;
    }
    protected abstract String getCommandSuffix();
    public String getCommand() {
        getCommandLen();
        return BaseProtocol.PREFIX + mCommandLen + COMMA + mCommandSuffix;
    }
}