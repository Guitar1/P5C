package com.zzx.police.data;

import android.os.Environment;
import android.support.annotation.IntDef;
import android.util.Log;

import com.zzx.police.MainActivity;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Tomy
 *         Created by Tomy on 2014/6/14.
 */
public class Values {

    public static final String BOARD = "ro.product.board";
    public static final String MODEL = "ro.product.model";
    public static final String INSTALL_FILE_LIST_PAT = "/system/etc/apk/installList.xml";
    public static final String ANIMATOR_PROPERTY_ROTATION = "rotation";
    public static final String PREFERENCE_NAME = "com_zzx_police";
    private static final String MAIN_TAG = "Police";
    /**
     * 自动应答模式及手动应答模式
     **/
    public static final int MODE_AUTO = 0;
    public static final int MODE_MANUAL = 1;
    public static final String BOOT_COUNT = "boot_count";
    public static final int GPS_REPORT_SET = 0;
    public static final int GPS_REMOVE = -1;
    public static final String KEY_HEARD_BEAT_INTERVAL = "heard_interval";

    public static final String BUS_EVENT_SOCKET_CONNECT = "event_socket_connect";
    public static final String BUS_EVENT_FTP = "bus_event_ftp";
    public static final String BUS_EVENT_MAIN_ACTIVITY = "event_main_activity";

    public static final String OPEN_CAMERA_TYPE="open_camera_type";
    public static final int OPEN_MAIN_CAMERA=0;
    public static final int OPEN_USB_CAMERA=1;
    public static final int OPEN_FRONT_CAMERA=2;
    @IntDef({OPEN_MAIN_CAMERA,OPEN_USB_CAMERA,OPEN_FRONT_CAMERA})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraType{

    }
    /**
     * 静态int变量
     **/
    public static final int SOCKET_CONNECTED = 0x01;
    public static final int SOCKET_RECONNECTION = 0x02;
    public static final int SERVICE_REGISTER_SUCCESS = 0x03;
    public static final int SERVICE_REGISTER_FAILED = 0x04;
    public static final int VIDEO_REGISTER_SUCCESS = 0x05;
    public static final int VIDEO_REGISTER_FAILED = 0x06;
    public static final int VIDEO_REPORT_STOP = 0x07;
    public static final int REPLACE_LOGIN_FRAGMENT = 0x08;
    public static final int TAKE_PICTURE = 0x09;
    public static final int DISMISS_WINDOW = 0x0A;
    public static final int START_UPLOAD = 0x0B;
    public static final int REPLACE_CAMERA_SETTING_FRAGMENT = 0x0C;
    public static final int BACK_START_RECORD = 0x0D;
    public static final int BACK_STOP_RECORD = 0x0E;
    public static final int RECORD = 0x0F;
    public static final int MSG_BIND_PREFERENCES = 0x11;
    public static final int VIDEO_THUMBNAIL = 0x12;
    public static final int IMG_THUMBNAIL = 0x13;
    public static final int MAIN_START_RECORD = 0x14;
    public static final int MAIN_STOP_RECORD = 0x15;
    public static final int BACK_RECORD_STOP = 0x16;
    public static final int BACK_DELETE_PRE_VIDEO_FILE = 0x17;
    public static final int HANDLER_MAIN_START_RECORD = 0x18;
    public static final int ENABLE_SOME_VIEW = 0x19;
    public static final int STORAGE_NOT_ENOUGH = 0x1A;
    public static final int STORAGE_OK = 0x1B;
    public static final int DOWNLOAD_EPO = 0x1C;
    public static final int CHECK_NEED_UPLOAD_LOG = 0x1D;
    public static final int AUDIO_REPORT_REGISTER_SUCCESS = 0x1E;
    public static final int AUDIO_REGISTER_FAILED = 0x1F;
    public static final int AUDIO_REPORT_STOP = 0x20;
    public static final int MSG_READ_BARCODE = 0x21;
    public static final int START_CAMERA_SERVICE = 0x22;
    public static final int AUDIO_RECEIVE_SUCCESS = 0x23;
    public static final int REPLACE_LAUNCHER_FRAGMENT = 0x24;
    public static final int REPLACE_PASSWORD_FRAGMENT = 0x25;
    public static final int RESTART_PREVIEW = 0x26;
    public static final int ENABLE_CAPTURE_BUTTON = 0x27;
    public static final int BACK_STOP_PRE_MODE_RECORD = 0x28;
    public static final int HANDLER_MAIN_STOP_RECORD = 0x29;
    public static final int CUT_VIDEO = 0x2A;
    public static final int STORE_DATA_BASE = 0x2B;
    public static final int FTP_LOGIN_FAILED = 0x2C;
    public static final int FTP_LOGIN_SUCCESS = 0x2D;
    public static final int VIDEO_CUT_MAIN = 0x2E;
    public static final int RECEIVE_START_RECORD = 0x2F;
    public static final int REPLACE_CHECK_FRAGMENT = 0x30;
    public static final int RECEIVE_STOP_RECORD = 0x31;
    public static final int RECEIVE_HTTP_UPLOAD = 0x32;
    public static final int WRITE_GPS_DATA = 0x33;
    public static final int CHECK_GPS_CHANGED = 0x34;
    public static final int CONFIG_FINISEDH = 0x35;
    public static final int HANDLER_MAIN_STOP_LASER = 0x36;
    public static final int HANDLER_MAIN_OPEN_CLICK = 0x37;
    public static final int SHOW_DIALOG_CHOOSE_POWER_SAVE_MODE = 0x40;

    public static final int FINISH_PASSWORD = 0x41;
    public static final int HANDLER_MAIN_STOP_RECORD_AS_USER = 0x42;
    public static final int LAUNCHER_FRAGMENT = 0x43;
    public static final int VIDEO_STOP_RECORD = 0x44;
    public static final int MAIN_ONLY_USB = 0x45;
    public static final int TALK_SHOW_DIALOG = 0x46;
    public static final int TALK_DISMISS_DIALOG = 0x47;
    public static final int TALK_DIALOG = 0X50;
    public static final int GPS_DATA = 0X55;
    public static final int OPEN_USB = 0x56;
    public static final int CHECK_OPEN_USB = 0x57;
    public static final int CHECK_NEED_OPEN_USB = 0x58;
    public static final int CONFIG_START = 0x59;
    public static final int CONFIG_ERROR = 0x60;
    public static final int WRITER_CLOSE = 0x61;
    public static final int ZOOM_GONE = 0x62;
    public static final int ACCELEROMETER_SENSOR = 0x63;
    public static final int UPLOADERROR = 0x69;
    public static final int START_RECORD_LOGCAT = 0x70;
    public static final int RELEASE_CAMERA = 0x71;
    public static final int IS_HOME = 0x72;
    public static final int COUNT_DOWN = 0x73;
    public static final String CLASS_SYSTEM_PROPERTIES = "android.os.SystemProperties";
    public static final String METHOD_GET = "get";
    public static final String ACTION_FILE_UPLOAD_FINISH = "action_FileUploadFinish";
    public static final String UPLOAD_FILE_NAME = "upload_file_name";
    public static final String ACTION_FILE_UPLOAD_LENGTH = "action_FileUploadLength";
    public static final String UPLOAD_FILE_LENGTH = "upload_file_length";
    public static final String ACTION_FILE_UPLOAD_FAILED = "action_FileUploadFailed";
    /**
     * Camera Service 静态变量
     **/
    public static final String BUS_EVENT_CAMERA = "bus_event_camera";

    public static final String ACTION_MAX_WINDOW = "action_Max_window";
    public static final String ACTION_MIN_WINDOW = "action_Min_window";
    public static final String SERVER_ACTION_MAX_WINDOW = "zzx_action_Max_window";
    public static final String SERVER_ACTION_MIN_WINDOW = "zzx_action_Min_window";
    public static final String ACTION_RELEASE_CAMERA = "action_Release_Camera";
    public static final String ACTION_RESTART_CAMERA = "action_Restart_Camera";
    public static final String ACTION_RELEASE_CAMERA_STATUS = "action_Release_Camera_Status";
    public static final String RELEASE_STATUS =  "Release_Status";

    public static final String PREFERENCE_KEY_RECORD_SOUND = "record_sound";
    public static final String PREFERENCE_KEY_RECORD_BOOT = "record_boot";
    public static final String PREFERENCE_KEY_RECORD_LOOP = "record_loop";
    public static final String PREFERENCE_KEY_RECORD_SECTION = "record_section";
    public static final String PREFERENCE_KEY_RECORD_PRE = "record_pre";
    public static final String PREFERENCE_KEY_RECORD_DELAY = "record_delay";
    public static final String PREFERENCE_KEY_RECORD_CIRCLE = "record_circle";
    public static final int SOUND_MODE_DEFAULT_OPEN = 1;
    public static final int MODE_DEFAULT_CLOSE = 0;
    public static final String PREFERENCE_KEY_RECORD_AUTO = "record_auto";
    public static final String CAMERA_ID="comera_id";

    public static final int PIC_MODE_SIGNAL = 0;
    public static final int PIC_MODE_QUICK = 1;
    public static final int PIC_MODE_INTERVAL = 2;
    public static final int PIC_MODE_TIMER = 3;
    public static final String PIC_MODE = "mode_take_picture";
    public static final String PIC_MODE_QUICK_POSITION = "pic_mode_quick_position";
    public static final String PIC_MODE_INTERVAL_POSITION = "pic_mode_interval_position";
    public static final String PIC_MODE_TIMER_POSITION = "pic_mode_timer_position";
    public static final String PIC_MODE_QUICK_VALUE = "key_mode_quick_series";
    public static final String PIC_MODE_INTERVAL_VALUE = "key_mode_interval";
    public static final String PIC_MODE_TIMER_VALUE = "key_mode_timer";
    public static final String PIC_MODE_QUICK_DEFAULT_VALUE = "5";
    public static final String PIC_MODE_INTERVAL_DEFAULT_VALUE = "2";
    public static final String PIC_MODE_TIMER_DEFAULT_VALUE = "5";

    public static final String FILE_NAME_DATA_FORMAT = "yyyyMMdd_HHmmss";
    public static final String FILE_NAME_DATA_FORMATS = "yyyyMMdd_HHmmssSSS";
    public static final String RECORD_DATE_FORMAT = "HH:mm:ss";
    public static final String INI_PATH = Environment.getExternalStorageDirectory() + "/bad.ini";
    public static final String ENCRYPTION = "_encryption";
    public static final String FILE_UPLOAD = "zzx_file_upload";
    public static final String ACTION_SOUND = "action_sound";
    public static final int FILE_TYPE_VID = 0;
    public static final int FILE_TYPE_IMG = 1;
    public static final int FILE_TYPE_SND = 2;
    public static final String BUS_EVENT_FTP_UPLOAD_FINISH = "bus_event_ftp_upload_finish";
    public static final String BUS_EVENT_START_LAUNCHER = "bus_event_start_launcher";
    public static final String BUS_EVENT_SEND_SYSTEM_BROADCAST = "bus_event_system_cast";

    public static final String ACTION_APK = "ApkFragment";
    public static final int SEARCH_APK_START = 0x02;
    public static final int SEARCH_APK_FINISHED = 0x03;

    public static final String ACTION_UVC_DISCONNECT = "action_uvc_disconnect";//外置摄像头断开
    public static final String ACTION_UVC_CONNECT = "action_uvc_connect";//外置摄像头连接
    public static  boolean ACTION_UVC_CONNECT_STATE = false;//外置摄像头连接
    /**
     * 应用包名,类名
     **/
    public static final String PACKAGE_NAME_FILE_MANAGER = "com.mediatek.filemanager";
    public static final String CLASS_NAME_FILE_MANAGER = "com.mediatek.filemanager.FileManagerOperationActivity";

    public static final String PACKAGE_NAME_FILE_MANAGER_NEW = "com.byandy.files";
    public static final String CLASS_NAME_FILE_MANAGER_NEW = "com.byandy.files.MainActivity";

    public static final String PACKAGE_NAME_SOUND_RECORD = "com.zzx.soundrecord";
    public static final String CLASS_NAME_SOUND_RECORD = "com.zzx.soundrecord.MainActivity";

    /*public static final String PACKAGE_NAME_TALK_BACK = "com.danale.ipc";
    public static final String CLASS_NAME_TALK_BACK = "com.danale.ipc.activity.SplashActivity";*/
    /**
     * 原生对讲应用 -- 集群通
     **/
    public static final String PACKAGE_NAME_TALK_BACK_JQT = "com.zed3.sipua";
    public static final String CLASS_NAME_TALK_BACK = "com.zed3.sipua.ui.splash.SplashActivity";
    /**
     * 广州星柯蓝对讲应用 -- 易对讲
     **/
    public static final String PACKAGE_NAME_TALK_BACK_XKL = "com.example.weixinapp";
    public static final String CLASS_NAME_TALK_BACK_XKL = "com.example.weixinapp.LoginActivity";
    /**
     * 浙江力石对讲应用 -- 沃对讲
     **/
    public static final String PACKAGE_NAME_TALK_BACK_LS = "com.airtalkee";
    public static final String CLASS_NAME_TALK_BACK_LS = "com.airtalkee.activity.AccountActivity";

    /**
     * 酷对讲应用 -- CloudPTT
     **/
    public static final String PACKAGE_NAME_TALK_BACK_KU = "com.ptt";
    public static final String CLASS_NAME_TALK_BACK_KU = "com.ptt.ptt.ui.MainActivity";

    /**
     * 赛普乐对讲 -- PDDS
     **/
    public static final String PACKAGE_NAME_TALK_BACK_SPL = "com.bit.pmcrg.dispatchclient";
    public static final String CLASS_NAME_TALK_BACK_SPL = "com.bit.pmcrg.dispatchclient.ui.SplashActivity";
    /**
     * 讯对讲
     **/
    public static final String PACKAGE_NAME_TALK_BACK_XUN = "com.unionbroad.app";
    public static final String CLASS_NAME_TALK_BACK_XUN = "com.unionbroad.app.activity.LoginActivity";
    /**
     * 银川警情查询
     **/
    public static final String PACKAGE_NAME_TALK_BACK_YC_JQCX = "com.casesearch.mt.casesearch";
    public static final String CLASS_NAME_TALK_BACK_YC_JQCX = "com.casesearch.mt.casesearch.WelcomeActivity";


    /**
     * 银川安全通信
     **/
    public static final String PACKAGE_NAME_TALK_BACK_YC_AQTX = "com.krx.ydjw.aqtx";
    public static final String CLASS_NAME_TALK_BACK_YC_AQTX = "com.icld.mt.aqtx.views.WelcomeActivity";
    /**
     * 银川警力警情
     **/
    public static final String PACKAGE_NAME_TALK_BACK_YC_JLJQ = "com.krx.ydjw.jljq";
    public static final String CLASS_NAME_TALK_BACK_YC_JLJQ = "com.icld.mt.jljq.views.WelcomeActivity";
    /**
     * 银川移动核查
     **/
    public static final String PACKAGE_NAME_TALK_BACK_YC_YDHC = "com.krx.ydjw.ydhc";
    public static final String CLASS_NAME_TALK_BACK_YC_YDHC = "com.icld.mt.ydhc.views.WelcomeActivity";

    /**
     * 保定 警务对讲
     **/
    public static final String PACKAGE_NAME_TALK_BACK_BD_JWDJ = "com.ruiruisdudio.voiceptt";
    public static final String CLASS_NAME_TALK_BACK_BD_JWDJ = "com.ruiruistudio.voiceptt.app.ServerList";

    /**
     * 人保视频定损
     **/
    public static final String PACKAGE_NAME_TALK_BACK_POC_PMCC = "com.airtalkee";
    public static final String CLASS_NAME_TALK_BACK_POC_PMCC = "com.airtalkee.activity.AccountActivity";

    /**
     * h9
     */
    public static final String PACKAGE_NAME_TALK_BACK_H9 = "";

    public static final String PACKAGE_NAME_CAL = "com.android.calculator2";
    public static final String CLASS_NAME_CAL = "com.android.calculator2.Calculator";
    /**
     * 时钟
     **/
    public static final String PACKAGE_NAME_CLOCK = "com.android.deskclock";
    public static final String CLASS_NAME_CLOCK = "com.android.deskclock.DeskClock";

    public static final String PACKAGE_NAME_MAP_BD = "com.baidu.BaiduMap";
    public static final String CLASS_NAME_MAP_BD = "com.baidu.baidumaps.WelcomeScreen";

    public static final String PACKAGE_NAME_TU_MAP = "com.mapbar.android.mapbarmap";
    public static final String CLASS_NAME_TU_MAP = "com.mapbar.android.mapbarmap.MapViewActivity";

    /*public static final String PACKAGE_NAME_CALL = "com.zzxcomm.phone";
    public static final String CLASS_NAME_CALL = "com.zzxcomm.phone.MainActivity";*/

    public static final String PACKAGE_NAME_CALL = "com.android.dialer";
    public static final String CLASS_NAME_CALL = "com.android.dialer.DialtactsActivity";

    public static final String PACKAGE_NAME_CALL_NEW = "phone.zzx.com.h9_dialer";
    public static final String CLASS_NAME_CALL_NEW = "phone.zzx.com.h9_dialer.activity.MainActivity";

    public static final String PACKAGE_NAME_SETTING = "com.zzx.setting";
    public static final String CLASS_NAME_SETTING = "com.zzx.setting.MainActivity";


    public static final String PACKAGE_NAME_SETTING_NEW = "myproject.zzx.myset";
    public static final String CLASS_NAME_SETTING_NEW = "myproject.zzx.myset.MainActivity";

    //电信天翼对讲
    public static final String PACKAGE_NAME_DX_CALL = "cn.com.ctsi.android.qchat";
    public static final String CLASS_NAME_DX_CALL = "cn.com.ctsi.android.qchat.activity.AgreementActivity";

    //讯飞输入法
    public static final String PACKAGE_NAME_XUNFEI = "com.iflytek.inputmethod";
    //谷歌输入法
    public static final String PACKAGE_NAME_GOODGLE = "com.google.android.inputmethod.latin";
    //谷歌拼音输入法
    public static final String PACKAGE_NAME_GOODGLE_PINYIN = "com.google.android.inputmethod.pinyin";
    //海高思
    public static final String PACKAGE_NAME_HGSPPT = "com.hgs.ptt";
    /**
     * 北斗导航设置
     */
    public static final String PACKAGE_NAME_LOACATION  = "com.zzx.locations";
    public static final String CLASS_NAME_LOACATION = "com.zzx.locations.GpsService";
    /**
     * 文件上传FTP服务器IP
     */

    public static final String FTP_IP = "ftp_ip";
    /**
     * 文件上传FTP服务器Port
     */
    public static final String FTP_SET_PORT = "ftp_port";
    /**
     * FTP文件上传账号密码
     */
    public static final String FTP_NAME = "ftp_name";
    public static final String FTP_PASS = "ftp_password";
    /**
     * 语言开关设置
     */
    public static final String LANGUAGE_SETTING = "language_setting";
    public static final String BUS_EVENT_RESET_SOCKET = "bus_event_reset_socket";

    /**
     * add by tomy @20140923 for modifying ServiceIp and ServicePort.
     */
    public static final String SERVICE_IP_ADDRESS = "service_ip_address";
    public static final String SERVICE_IP_PORT = "service_ip_port";

    public static final String File_SCREEN_ORIENTATION = "file_screen_orientation";

    /**
     * 密码页面相关设置
     **/
    public static final String BUS_EVENT_FINISH_PASSWORD = "bus_event_finish_password";

    /**
     * 密码页面相关设置
     **/
    public static final String BUS_EVENT_LAUNCHER_FRAGMENT = "bus_event_launcher_fragment";

    //车载模式
    public static final String SETTING_VEHICLE = "vehicle";

    /**
     * 数据库
     */
    public static final String DATABASE_NAME = "history_database";
    public static final String TABLE_KEY_ID = "_id";
    public static final String TABLE_FILE_TYPE = "type";
    public static final String TABLE_FILE_NAME = "name";
    public static final String TABLE_FILE_PATH = "path";
    public static final String TABLE_START_TIME = "startTime";
    public static final String TABLE_END_TIME = "endTime";
    public static final String TABLE_NAME = "history_table";
    public static final String TABLE_CREATE_DATE = "createDate";
    public static final String TABLE_REMOTE_DIR_NAME = "dir";
    public static final String FRAGMENT_KEY = "Fragment_key";
    public static final int FRAGMENT_SETTING_PICTURE = 0x0;
    public static final int FRAGMENT_SETTING_RECORD = 0x1;

    public static final String ACTION_LOGIN_STATE = "loginState";
    public static final String ACTION_MONITOR_STATE = "monitorState";
    public static final String ACTION_UPLOAD_STATE = "uploadState";
    public static final String DEVICE_PASSWORD = "device_password";
    public static final String DEVICE_SUPER_PASSWORD = "device_super_password";
    public static final String KEY_CAPTURE_SOUND = "key_sound_take_picture";
    public static final String ACTION_START_RECORD = "cameraRecord";
    public static final String ACTION_STORAGE_STATE = "sdcardState";
    public static final String CAMERA_SERVICE_EXTRA_STRING = "extra_string";
    public static final String EXTRA_TAKE_PICTURE = "take_picture";
    public static final String EXTRA_RECORD = "record";
    public static final String RATIO_CAPTURE_DEFAULT_VALUE = "2560x1440";
    public static final String RATIO_RECORD_DEFAULT_VALUE = "1280x720";
    public static final String RATIO_RECORD_HNYC_VALUE = "1920x1080";
    public static final String JINAN_RECORD_DEFAULT_VALUE = "864x480";
    public static final String ACTION_RECORD_DEFAULT_VALUE = "zzx_action_record_default_value";

    public static final String ACTION_UEVENT_CAPTURE = "zzx_action_capture";
    public static final String ACTION_UEVENT_RECORD = "zzx_action_record";
    public static final String ACTION_UEVENT_MIC = "zzx_action_mic";
    public static final String ACTION_WIFI_SCAN_FINISHED = "zzx_wifi_scan";
    public static final String EXTRA_CAMERA_UEVENT = "zzx_state";
    public static final String ACTION_REGISTER_FAILED = "action_Register_failed";
    public static final String ACTION_CALL_SOS_CONTACT = "action_call_sos_contact";
    //拔插电池广播
    public static final String ZZX_ACTION_BATSW = "zzx_action_batsw";
    public static final int ZZX_BAT_IN = 1; //插
    public static final int ZZX_BAT_OUT = 0;//拔

    //瑞尼
    public static final String ZFY_MEDIA_UPLOADED = "zfy_media_uploaded";//文件上传成功广播
    public static final String ZFY_MEDIA_DELETED = "zfy_media_deleted";//文件删除广播

    //设置帧率的广播
    public static final String ACTION_VIDEO_FPS = "video_fps";
    /**
     * 蓝牙遥控操作
     */
    public static final String ACTION_EVENT_RECORD ="action_event_record";//录像
    public static final String ACTION_EVENT_SWITCH ="action_event_switch";//切换摄像头
    public static final String ACTION_EVENT_PHONOS ="action_event_phonos";//拍照
    public static final String ACTION_EVENT_SUBTRACT ="action_event_subtract";//录音
    public static final String ACTION_EVENT_ADD ="action_event_add";//灯

    public static final String PHOTOS = "com.zzxkeycode.photos";
    public static final String RED_CUT = "com.zzxkeycode.redCut";
    public static final String COM_KEY_CODE_RECORD = "com.zzxkeycode.record";
    /**
     * 融合指挥对讲
     */
    public static final String PACKAGE_NAME_TALK_BACK_RONG_HE = "com.ids.idtma";
    public static final String CLASS_NAME_TALK_BACK_RONG_HE = "com.ids.idtma.IdtStartPage";
    /**
     * 身份证识别
     */
    public static final String PACKAGE_NAME_ID_CARD = "com.eidlink.readcarddemo7";
    public static final String CLASS_NAME_ID_CARD = "com.eidlink.readcarddemo7.acvitity.IDRecognitionActivity";
    /**
     * UVC
     */
    public static final String PACKAGE_NAME_UVC = "com.serenegiant.usbcameratest3";
    public static final String CLASS_NAME_UVC = "com.serenegiant.usbcameratest3.HomeActivity";

    /***
     * ========logcat========
     ***/
    public static final String LOGCAT_PHOTOS = "com_logcat_photos";
    public static final String LOGCAT_RECORD = "com_logcat_record";
    public static final String LOGCAT_MIC = "com_logcat_mic";
    public static final String LOGCAT_USER_ADMIN = "com_logcat_user_admin";
    public static final String LOGCAT_ROOT_ADMIN = "com_logcat_root_admin";
    public static final String LOGCAT_BATTERY_LOW = "com_logcat_battery_low";
    public static final String LOGCAT_USER_ADMIN_EXTRA = "com_logcat_user_admin_extra";
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_ID = "extra_id";

    ////////////////////////sos//////////////////////////////////////
    public static final String ACTION_SOS = "zzx_action_sos";
    public static final String ACTION_PPT = "zzx_action_ppt";
    public static final String ZZX_STATE = "zzx_state";
    public static final int KEY_PRESSED_UP = 3;
    //Key event: Long pressed. Pressed about 2s will send this event.
    public static final int LONG_PRESSED_KEY = 2;
    //Key event: Pressed Down.
    public static final int KEY_PRESSED_DOWN = 1;
    //Key event: Short Press.
    public static final int SHUTTER_KEY = 0;
    ////////////////////////sos//////////////////////////////////////
    public static final String ACTION_RECORD_PREFERENCE_NOTIFY = "action_RecordPreferenceNotify";
    public static final String SETTING_YUYINBOBAO = "yuyinbobao";
    public static final String ACTION_MIC_RECORD = "zzx_action_record_mic";
    public static final String ACTION_VIDEO_RECORD = "zzx_action_record_video";
    public static final String ACTION_CAPTURE = "takePicture";
    public static final String ACTION_UPLOAD_FILE = "uploadFile";
    public static final String EXTRA_UPLOAD_FILE = "fileName";
    public static final String POLICE_NUMBER = "police_number";
    public static final String USER_NUMBER = "user_number";
    public static final String DEVICE_NUMBER = "device_number";
    public static final String DEVICE_PRE = "DSJ-";
    public static final String MODEL_NAME = "model_name";
    public static final String MODEL_DEFAULT_NAME = "00";
    public static final String MODEL_DEFAULT_SERIAL = "0000000";
//    public static final String MODEL_DEFAULT_IDCODE = "DSJ-KRSH9A1";
    public static final String MODEL_DEFAULT_IDCODE = "ABCDE";
    public static final String TABLE_NAME_POLICE_INFO = "table_police_info";

    ///   public static final String FILE_SUFFIX_IMPORTANT = "_ZY";//2882长按录像的后缀
    public static final String FILE_SUFFIX_IMPORTANT = "_IMP";//2080.2835长按录像的后缀
    public static final String FILE_SUFFIX_SRT_GPS = ".GPS";
//public static final String FILE_SUFFIX_SRT_GPS = ".log";
    public static final String FILE_SUFFIX_VID = ".mp4";
    public static final String FILE_SUFFIX_IMPORTANT_VID = "IMP.mp4";
    public static final String FILE_SUFFIX_PIC = ".jpg";
    public static final String FILE_SUFFIX_AUDIO = ".mp3";
    public static final String FILE_SUFFIX_VID_AVI = ".avi";
    public static final String FILE_SUFFIX_IMPORTANT_VID_AVI = "IMP.avi";

    public static final String ACTION_SYSTEM_KEY_BACK = "zzx_action_back";
    public static final String ACTION_SYSTEM_KEY_HOME = "zzx_action_home";
    public static final String ACTION_SYSTEM_KEY_MENU = "zzx_action_menu";
    public static final String ACTION_CAMERA_DARK = "zzx_action_camera_dark";  //摄像头暗的时候
    public static final String ACTION_CAMERA_LIGHT = "zzx_action_camera_light";  //摄像头亮的时候
    public static final int IDLE_MAX_COUNT = 3;

    /**
     * 接收usb摄像头发来发广播
     */
    public static final String ACTION_RECEIVER_UVC_CAMERA = "ActionReceiverUVCCamra";
    public final static String STATUS = "status";
    public static final String IS_UPLOAD = "is_upload";
    public static final String ACTION_STOP_MAIN_CAMERA_RECORD = "action_stop_main_camera_record";
    public static final String ACTION_UVC_START_PREVIEW_OK="action_uvc_start_preview_ok";
    public static final String ACTION_USB_CAMERA_STATUES = "com.zzx.action.usbcamera.statues";
    public static final String USB_CAMERA_STATUES = "usb_statues";
    public static final int USB_STATUES_CONNECT = 0;
    public static final int USB_STATUES_DISCONNECT = 1;
    public static final int USB_STATUES_ATTACH = 2;
    public static final int USB_STATUES_DETTACH = 3;
    public static final int USB_STATUES_CANCLE = 4;
    public static final String USB_CAMERA_LINK_STATE = "usb_camera_link_state";
    public static final int USB_CAMERA_LINKED = 1;
    public static final int USB_CAMERA_NO_LINK = 0;
    public static final String CAMERA_SERVICE_WINDOW_SIZE = "camera_service_window_size";
    public static final int WINDOW_SIZE_MIN = 0;
    public static final int WINDOW_SIZE_MAX = 1;

    /**主摄像头反转到usb摄像头发送的广播*/
    public static final String ACTION_ASTERN = "ActionAstern";
    public final static String INTENT_TYPE = "intent_type";
    /**
     * 表明是在其它地方打开后置摄像头
     */
    public final static int INTENT_TYPE_VALUE_OTHER = 0;
    /**
     * 表明是通过点击"反转"按钮打开后置摄像头
     **/
    public final static int INTENT_TYPE_VALUE_BUTTON_REVERSE = 1;
    /**
     * 停止usb摄像头服务
     */
    public final static int INTENT_TYPE_VALUE_STOP_UVC_CAMERA_SERVICE = 2;

    public static final String ACTION_MASS_USB = "com.zzx.usbmass";

    public static final String PATH_POLICE_NUMBER = "/sys/devices/platform/zzx-misc/police_num_stats";
    public static final String PATH_GPS_INFO = "/sys/devices/platform/zzx-misc/gps_stats";
    public static final String POLICE_DEFAULT_NUM = "000000";
    public static final String PRISONER_NUMBER = "PrisonerNumber";
    public static final String POLICE_DEFAULT_NUM_8 = "00000000";
    public static final String PRISONER_DEFAULT_NUM = "0000000000";
    public static final String DEVICE_DEFAULT_NUM = "DSJ-00-00000";
    public static final String DEVICE_DEFAULT_ONLY_NUM = "00000";
    public static final String USER_DEFAULT_NUM = "00000000000";

    public static final String POLICE_NAME = "police_name";
    public static final String GOURP_NO = "gourp_no";
    public static final String GROUP_NAME = "group_name";

    public static final String ROOT_PATH = Environment.getExternalStorageDirectory() + "/police" + File.separatorChar;
    public static final String IMG_DIR_PATH = ROOT_PATH + "pic";
    public static final String VID_DIR_PATH = ROOT_PATH + "video";
    public static final String SND_DIR_PATH = ROOT_PATH + "sound";
    public static final String LOG_DIR_PATH = ROOT_PATH + "log";
    public static final String VIDEO_UNLOCK_SAVE_PATH = VID_DIR_PATH;

    public static final String UPDATE_LIGHT = "updateLight";



    /**********************************
     * EPO下载相关
     **************************************/
    public static final String ACTION_UPLOAD_CANCEL = "upload_cancel";
    public static final String EPO_PATH = "/data/misc/EPO.DAT";
    public static final String REMOTE_PATH = "remotepath";
    public static final String REMOTE_PATH_VALUES = "/home/zzx-001/rock_workspace/ad/apache-tomcat-6.0.24/apache-tomcat-6.0.24/webapps/OTAService/file/systemfiles/EPO";
    public static final String FILENAME = "remote_filename";
    public static final String LOCAL_PATH = "localpath";
    public static final String FILENAME_VALUES = "EPO.DAT";
    public static final String EPO_FTP_HOST = "ftpHost";
    public static final String EPO_FTP_PORT = "ftpPort";
    public static final String EPO_FTP_USER_NAME = "ftpUserName";
    public static final String EPO_FTP_PASSWORD = "ftpPassword";
    public static final String EPO_FTP_HOST_VALUE = "218.17.161.5";
    public static final int EPO_FTP_PORT_VALUE = 8002;
    public static final String EPO_FTP_USER_NAME_VALUE = "zzxcomm";
    public static final String EPO_FTP_PASSWORD_VALUE = "662211";
    public static final String LOCAL_PATH_VALUES = "/data/misc";
    public static final String ACTION_START_DOWN = "ActionStartDownload";
    /***************************************************************************/


    public static final String PROCESS_KILL_PATH = "/sys/devices/platform/zzx-misc/camera_debug";
    public static final String TAG_WAKE_LOCK = "Record_Wake";
    public static final String TAG_REAL_WAKE_LOCK = "Real_Wake";
    public static final String U_EVENT_LONG_PRESSED_STATE = "zzx_state";
    public static final String U_EVENT_LONG_PRESSED_UP = "long_pressed_up";
    public static final String ACTION_SCREEN_KEEP_ON = "action_screen_never";
    public static final String EXTRA_SCREEN_KEEP_ON = "screen_light_never";
    public static final String LAST_UPLOAD_LOG_DAY = "last_upload_log_day";
    public static final String DATE_FORMAT_CHECK_UPLOAD_LOG = "yyyyMMdd";
    public static final String ACTION_UPLOAD_LOG = "action_report_log";
    public static final String XML_SERVICE_PORT = "port";
    public static final String XML_SERVICE_IP = "ip";
    /**
     * 设备默认密码
     */
    public static final String DEVICE_DEFAULT_PASSWORD = "123456";
    /**
     * P5X_SZLJ深圳亮见固定密码
     */
    public static final String DEVICE_P5X_SZLJ_DEFAULT_PASSWORD = "abcdef";
    /**
     * 巴西版本默认密码
     * 管理员密码，超级密码相同均为 123456
     */
    public static final String DEVICE_DEFAULT_PASSWORD_BRAZIL ="123456";
    /**
     * 设备超级密码
     */
    public static final String DEVICE_DEFAULT_SUPER_PASSWORD = "777888";


    /**
     * 设备H9超级密码
     */
    public static final String DEVICE_H9_DEFAULT_SUPER_PASSWORD = "708718728";
    public static final String USB_CONNECTED = "usb_connected";
    public static final String VIDEO_CUT = "video_cut";
    public static final String PACKAGE_NAME_MAP_GOOGLE = "com.google.android.apps.maps";
    public static final String CLASS_NAME_MAP_GOOGLE = "com.google.android.maps.MapsActivity";
    /**
     * 微信包名类名
     */
    public static final String CLASS_NAME_WECHAT = "com.tencent.mm.ui.LauncherUI";
    public static final String PACKAGE_NAME_WECHAT = "com.tencent.mm";
    /**
     * 后台监控应用
     **/
    public static final String CLASS_NAME_X_PAI = "cn.com.xpai.XPAndroid";
    public static final String PACKAGE_NAME_X_PAI = "cn.com.xpai";
    /**
     * 武安行应用
     **/
    public static final String PACKAGE_NAME_RAIL_WAY = "com.jetsum";
    public static final String CLASS_NAME_RAIL_WAY = "com.jetsum.activity.TestActivity3";

    /**
     * 启动保定警务通对讲应用
     **/
    public static final String PACKAGE_NAME_TALK_BACK_QR = "com.ruiruisdudio.voiceptt";
    public static final String CLASS_NAME_TALK_BACK_QR = "com.ruiruistudio.voiceptt.app.ServerList";
    /**
     * 郑州瑞尼客户
     */
    public static final String PACKAGE_NAME_VIDEO_ASSIST = "cnnt.videoCall";
    public static final String CLASS_NAME_VIDEO_ASSIST = "cnnt.videoCall.VideoRoomActivity";
    /**
     * 华脉智联_铁务通
     */
    public static final String PACKAGE_NAME_UNION_BROAD = "com.unionbroad.app";
    public static final String CLASS_NAME_UNION_BROAD = "com.unionbroad.app.activity.LoginActivity";
    /**
     * 智能翻译机
     */
    public static final String PACKAGE_NAME_TRANSLATION = "com.yiyitong.translator";
    public static final String CLASS_NAME_TRANSLATION = "com.yiyitong.translator.MainActivity";
    /**
     *
     */
    public static final String PACKAGE_NAME_TALK_BAD = "com.bad.dsj";
    public static final String CLASS_NAME_TALK_BAD = "com.bad.dsj.module.login.LoginActivity";
    /**
     * 山东通亚vos
     */
    public static final String PACKAGE_NAME_VOS = "com.smartpower.callcenter";
    public static final String CLASS_NAME_VOS = "com.smartpower.callcenter.LoginActivity";
    //铁务通释放摄像头广播
    public static final String ACTION_VIDEO_CALL_END = "action_Video_Call_End";
    //郑州瑞尼接受广播
    public static final String SPXZ_TAKE_PICTURE = "take_picture";//拍照
    public static final String SPXZ_RECORD_VIDEO = "record_video";//录像
    public static final String SPXZ_RECORD_AUDIO = " record_audio";//录音

    //test 震动警报广播
    public static final String ACTION_DROP = "zzx_action_drop";
    //人脸识别
    public static final String ZZX_FACE_RECOGNITION = "zzx_face_recognition";
    //距离感应广播
    public static final String ACTION_Distance_induction = "action_distance_induction";
    //P80自动红外广播
    public static final String ACTION_PR = "zzx_pr_adc";
    /**
     * 启动保定警务通对讲传入警号
     **/
    public static final String QR_POLICE_NUMBER = "username";
    /**
     * 保存接收到警务通开始录像后所录的文件名
     **/
    public static final String HTTP_UPLOAD_PATH = "ZZXHttpRecordPath";
    public static final String AUTO_LIGHT = "auto_light_state";
    public static final String AUTO_IrCut = "auto_IrCut_state";
    public static final String BrightfilePath = "/sys/devices/platform/zzx-misc/bright_stats";
    public static final String IrCutfilePath = "/sys/devices/platform/zzx-misc/ircut_irled_stats";

    public static final String LOOP_RECORD = "ZZXLoopRecord";
    public static final String FREQUENCY  = "frequency";
    public static final String ACTION_SETTING_OPEN_USB = "zzx_event_openusb";
    /**
     * 写入串号，类似于出厂日期
     */
    public static String SERIAL_NUMBER = "zzx_serial_number";
    /**
     * 普通用户密码
     */
    public static String USER_PSW = "zzx_user_password";
    /**
     * 管理员用户密码
     */
    public static String ADMIN_PSW = "zzx_admin_password";
    /**
     * 超级管理员用户密码
     */
    public static String SUPER_PSW = "zzx_spuer_password";

    public static String IsOpenDialer = "is_open_dialer";


    public static String UserNameNow="username";
    public static String UserNubNow ="Num";
    public static String UserIDNow ="id";
    public static boolean isUserAdmin=false;
    public static boolean isRootAdmin=false;

    /*************************
     * 蓝牙肩灯
     ***********************************/
    //广播会在开启服务，有事件变化进行发出
    public static final String BLE_ACTION = "com.zzx.setting.ble";

    //ble信息 intent.getStringExtra(BleValue.BLE_STATUS);
    public static final String BLE_STATUS = "ble status";

    //ble事件 拍照
    public static final String BLE_STATUS_PIC = "ble picture";

    //ble事件 开始录像
    public static final String BLE_STATUS_VON = "ble start video";

    //ble事件 停止录像
    public static final String BLE_STATUS_VOU = "ble stop video";

    //ble状态 连接
    public static final String BLE_STATUS_CON = "ble connect";

    //ble状态 未连接
    public static final String BLE_STATUS_DIS = "ble disconnect";

    //ble状态，代表在进行操作，主要是在通讯服务时告知，正在使用,不影响ble事件
    public static final String BLE_STATUS_ACT = "ble do action";
    /*************************蓝牙肩灯***********************************/


    public static final String ACTION_SCREEN_NEVER = "action_screen_never";

    /**
     * 初始化配置文件.
     */
    public static final String FILE_CONFIG_INIT = "/sdcard/zzxConfig.xml";

    public static void LOG_I(String TAG, Object msg) {
        if (MainActivity.DEBUG) {
            Log.i(MAIN_TAG, TAG + msg);
        }
    }

    public static void LOG_W(String TAG, Object msg) {
        if (MainActivity.DEBUG) {
            Log.w(MAIN_TAG, TAG + msg);
        }
    }

    public static void LOG_E(String TAG, Object msg) {
        if (MainActivity.DEBUG) {
            Log.e(MAIN_TAG, TAG + msg);
        }
    }

    public static void LOGD(String TAG, Object msg) {
        if (MainActivity.DEBUG) {
            Log.d(MAIN_TAG, TAG + msg);
        }
    }

    public static void LOG_V(String TAG, Object msg) {
        if (MainActivity.DEBUG) {
            Log.v(MAIN_TAG, TAG + msg);
        }
    }

    public static final String CAMERA = "preferred-preview-size-for-video=1280x720;zoom=0;hue=middle;fb-smooth-level-max=4;recording-hint=true;max-num-detected-faces-hw=15;video-stabilization=false;zoom-supported=true;fb-smooth-level=0;cap-mode=normal;fb-sharp=0;aflamp-mode-values=off,on,auto;contrast=middle;whitebalance=auto;scene-mode=auto;jpeg-quality=90;afeng-min-focus-step=0;preview-format-values=yuv420sp,yuv420p,yuv420i-yyuvyy-3plane;rotation=90;jpeg-thumbnail-quality=100;burst-num=1;preview-format=yuv420sp;metering-areas=(0,0,0,0,0);video-size-values=176x144,480x320,640x480,864x480,1280x720,1920x1080;preview-size=960x540;focal-length=3.5;iso-speed=auto;cap-mode-values=normal,face_beauty,continuousshot,smileshot,bestshot,evbracketshot,autorama;flash-mode-values=off,on,auto,red-eye,torch;preview-frame-rate-values=15,24,30;hue-values=low,middle,high;max-num-metering-areas=9;aflamp-mode=off;fb-sharp-max=4;preview-frame-rate=30;focus-mode-values=auto,macro,infinity,continuous-picture,continuous-video,manual,fullscan;jpeg-thumbnail-width=160;video-size=1280x720;scene-mode-values=auto,portrait,landscape,night,night-portrait,theatre,beach,snow,sunset,steadyphoto,fireworks,sports,party,candlelight,hdr;preview-fps-range-values=(5000,60000);fb-sharp-min=-4;jpeg-thumbnail-size-values=0x0,160x128,320x240;contrast-values=low,middle,high;zoom-ratios=100,114,132,151,174,200,229,263,303,348,400;preview-size-values=176x144,320x240,352x288,480x320,480x368,640x480,720x480,800x480,800x600,864x480,960x540,1280x720;edge-values=low,middle,high;preview-fps-range=5000,60000;auto-whitebalance-lock=false;min-exposure-compensation=-3;antibanding=auto;max-num-focus-areas=0;vertical-view-angle=42;fb-smooth-level-min=-4;horizontal-view-angle=55;fb-skin-color=0;brightness=middle;video-stabilization-supported=true;jpeg-thumbnail-height=128;brightness_value=-50;cam-mode=2;smooth-zoom-supported=true;capfname=/sdcard/DCIM/cap00;saturation-values=low,middle,high;zsd-mode=off;focus-mode=continuous-video;fb-skin-color-max=4;auto-whitebalance-lock-supported=true;fb-skin-color-min=-4;edge=middle;max-num-detected-faces-sw=0;picture-format-values=jpeg;iso-speed-values=auto,100,200,400,800,1600;max-exposure-compensation=3;video-snapshot-supported=true;exposure-compensation=0;exposure-compensation-step=1.0;brightness-values=low,middle,high;flash-mode=off;auto-exposure-lock=false;effect-values=none,mono,negative,sepia,aqua,whiteboard,blackboard;picture-size=3328x1872;max-zoom=10;effect=none;saturation=middle;whitebalance-values=auto,incandescent,fluorescent,warm-fluorescent,daylight,cloudy-daylight,twilight,shade;picture-format=jpeg;focus-distances=0.95,1.9,Infinity;zsd-mode-values=off;auto-exposure-lock-supported=true;afeng-max-focus-step=0;antibanding-values=off,50hz,60hz,auto;";
    //picture-size-values=320x240,640x480,1024x768,1280x720,1280x768,1280x960,1600x1200,2048x1536,2560x1440,2560x1920,3264x2448,3328x1872,2880x1728,3600x2160;

    public static final String H9INIT= "[ZFY]\n" +
            "\r\n" +
            "PolNo=123456\n" +
            "\r\n" +
            "DevNo=Dev123";

}
