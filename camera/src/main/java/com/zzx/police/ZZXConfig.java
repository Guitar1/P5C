package com.zzx.police;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.zzx.police.data.Values;
import com.zzx.police.utils.LogUtil;
import com.zzx.police.utils.SystemUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zdm on 2016/12/27 0027.
 * 功能：主要存储整个软件的设置变量
 */
public class ZZXConfig {
    public static boolean IsBaidu = true;
    private static Context mContext;
    private static String zzxBoard ;
    private static String zzxModel ;
    public static boolean isAirplaneMode = false; //是否插上usb开启飞行模式
    /**
     * 0:为发送短信  1：为拨打电话
     **/
    public static int SOSstatus = 1;
    public static boolean isOpenPassActivity = false;  //是否打开输入密码的页面
    public static boolean isG726 = false;
    public static boolean IsCheckExternalStorage = true;//是否对内存进行判断，ture 是判断，false是不判断
    public static boolean IsShowWaterMark = true; //是否显示水印 true 是显示，false是不显示
    public static boolean isShowDisk = false; //是否打开磁盘模式（不打开同时不现实输入密码页面）
    /****
     * =====送检修改==== true为送检版本。false为非送检版本
     ****/
    public static boolean isSongJian = false;
    public static boolean iSMaxWindow = isSongJian;  //开机的时候要不要进入预览\ true为送检版本。false为非送检版本
    /****=====送检修改==== true为送检版本。false为非送检版本  ****/
    public static boolean mVideoCut = isSongJian;//是否打开分段录像    true为送检版本。false为非送检版本
    /*****攀枝花版本设置  true为攀枝花版本。false为非攀枝花版本****/
    public static boolean isPanZhiHua = false;
    public static boolean IsReSetAirPlaneMode = isPanZhiHua;//开机的时候是否重启飞行模式
    public static boolean IsSetDefultAPN = isPanZhiHua;//开机的时候是否设置默认的apn
    /*****攀枝花版本设置  true为攀枝花版本。false为非攀枝花版本****/

    public static boolean IsRotate = true;   //设置录像拍照镜像旋转方向
    /****新增版本 巴西客户版本设置    true为巴西客户版本。false为非巴西客户版本****/
    public static boolean IsBrazilVerion = false;
    public static  boolean isOperatingSystem = false;//是否显示操作系统
    public static boolean isShowFrequency = true;//是否显示主频
    public static boolean isPullout = false;

    public static final String ZZXAPPEnabled = "ZZXAPPEnabled";//应用软件是否可见
    public static final String ZZXPoneEnabled = "ZZXPoneEnabled"; //电话是否可见
    public static final String ZZXWXEnabled = "ZZXWXEnabled"; //微信是否可见
    public static final String ZZXMapEnabled = "ZZXMapEnabled"; //地图是否可见
    public static final String ZZXTalkEnabled = "ZZXTalkEnabled"; //对讲是否可见
    public static final String ZZXTieWuTongEnabled = "ZZXTieWuTongEnabled"; //铁务通是否可见
    public static final String ZZXTranslationEnabled = "ZZXTranslationEnabled";//智能翻译是否可见
    public static final String ZZXVOS = "ZZXVOS";// 山东通亚vos.apk

    public static boolean isP5C = false;//P5C
    /**
     * 4GA
     */
    public static boolean is4GA = false;//4GA
    public static boolean is4GC = false;//4GC
    public static boolean is4G5 = false;//4G5
    public static boolean isP5X = false;//P5X
    public static boolean isP80 = false;//P80
    /**
     * 4G5_BAD
     */
    public static boolean is4G5_BAD = false;
    /**
     * P5C_Eng
     */
    public static boolean isEng = false;

    /**
     * P5X济南客运段
     */
    public static boolean isP5XJNKYD = false;
    /**
     *4GA  河南电网
     */
    public static boolean is4GA_HNDW = false;

    /**
     * P5C_日照消防支队
     */
    public static boolean isP5C_RZXFD = false;

    /**
     * P5X_山西交警
     */
    public static boolean isP5XD_SXJJ = false;

    /**
     * P5C_郑州瑞尼
     */
    public static boolean isP5C_SPXZ = false;
    /**
     * 4GA_送检
     */
    public static boolean is4GA_TEST = false;
    /**
     * 4GA_成都合能
     */
    public static boolean is4GA_CDHN = false;
    /**
     * 4GA_GTX固特讯
     */
    public static boolean is4GA_GTX = false;
    /**
     * 4GA_SDTY山东通亚
     */
    public static boolean is4GA_SDTY = false;


    /**
     * 5G_liangjian
     */
    public static boolean is5G_liangjian = false;
    /**
     * P5C_HXJ 华脉智联_铁务通
     */
    public static boolean isP5C_HXJ = false;
    /**
     * P5C_HXJ_TKY 华脉智联_铁科院
     */
    public static boolean isP5C_HXJ_TKY = false;
    /**
     * P5C_LJ_HLZL 亮见—汇绿智联
     */
    public static boolean isP5C_LJ_HLZL = false;
    /**
     * P5X_SZLJ 深圳亮见
     */
    public static boolean isP5X_SZLJ = false;
    /**
     * P5C_HLN 火灵鸟
     */
    public static boolean isP5C_HLN = false;
    /**
     * 4GA_GJDW 国家电网
     */
    public static boolean is4GA_GJDW = false;
    /**
     * 4G5_GJDW 国家电网
     */
    public static boolean is4G5_GJDW = false;
    /**
     * 4GA_LJ 亮见
     */
    public static boolean is4GA_LJ = false;
    /**
     * P5C_FBSJ  防爆电池
     */
    public static boolean isP5C_FBSJ = false;
    /**
     * P5C_成都合能
     */
    public static boolean isP5C_CDHN = false;
    /**
     * P5X_成都合能
     */
    public static boolean isP5X_CDHN = false;
    /**
     * P5X_liangjian
     */
    public static boolean isP5X_liangjian = false;
    /**
     * P5C_东华志高
     */
    public static boolean isP5C_DHZG = false;
    /**
     * 4GC_时代经纬
     */
    public static boolean is4GC_SDJW = false;
    /**
     * 4GC_亮见
     */
    public static boolean is4GC_SY = false;
    /**
     * 4GC_俄文
     */
    public static boolean is4GC_NINE = false;
    /**
     * 4G5_upgrade
     */
    public static boolean is4G5_upgrade = false;
    /**
     * P5C_Translate智能翻译
     */
    public static boolean isP5C_Translate = false;
    /**
     * 4GA_HNYC 河南烟草
     */
    public static boolean is4GA_HNYC = false;

    public static String P5C = "P5C";
    public ZZXConfig(Context context){
        mContext = context;
        zzxBoard = SystemUtil.getSystemProperties(Values.BOARD);
        zzxModel = SystemUtil.getSystemProperties(Values.MODEL);
        ConfigList = new ArrayList<>();
        add("ZZXIsCheckExternalStorage", IsCheckExternalStorage);
        add("ZZXIsShowWaterMark", IsShowWaterMark);
        add("ZZXIsBaiDu", IsBaidu);
        add("ZZXOperatingSystem",isOperatingSystem);
        add("ZZXFrequency",isShowFrequency);
        add("ZZXISSongJian",isSongJian);
        LogUtil.e("zzxconfig", zzxBoard+zzxModel);

        switch (zzxBoard){
            case "P5C":
                isP5C = true;
                switch (zzxModel){
                    case "byanda"://百安达
                        addBySave("P5C",true);
                        break;
                    case "p5c_fycg"://汾阳城管（深圳图元）
                        addBySave("P5C_FYCG",true);
                        break;
                    case "p5c_rzxfd"://日照消防支队
                        isP5C_RZXFD = true;
                        addBySave("P5C_RZXFD",true);
                        break;
                    case "sd_gongan_tejing"://山东公安特警
                        addBySave("P5C_SDGATJ",true);
                        break;
                    case "shandong_weidun"://山东威盾
                        break;
                    case "taiyuan_zonghe_guanliju"://太原综合管理局
                        addBySave("P5C_TYZHGL",true);
                        break;
                    case "zhengzhou_ruini_spxz"://郑州瑞尼
                        isP5C_SPXZ = true;
                        addBySave("P5C_SPXZ",true);
                        break;
                    case "5G_liangjian"://亮见
                        is5G_liangjian = true;
                        addBySave("5G_liangjian",true);
                        break;
                    case "tiewutong":
                        isP5C_HXJ = true;
                        addBySave("P5C_HXJ",true);
                        addBySave(ZZXTieWuTongEnabled,true);
                        break;
                    case "tiekeyuan":
                        isP5C_HXJ_TKY = true;
                        addBySave("P5C_HXJ_TKY",true);
                        addBySave(ZZXTieWuTongEnabled,true);
                        break;
                    case "huilvzhilian":
                        isP5C_LJ_HLZL = true;
                        addBySave("P5C_LJ_HLZL",true);
                        break;
                    case "P5C_HLN":
                        isP5C_HLN = true;
                        addBySave("P5C_HLN",true);
                        addBySave(ZZXAPPEnabled,false);
                        break;
                    case "ruiqi":
                        break;
                    case "FBSJ":
                        isP5C_FBSJ = true;
                        addBySave("P5C_FBSJ",true);
                        break;
                    case "CDHN":
                        isP5C_CDHN = true;
                        addBySave("P5C_CDHN",true);
                        break;
                    case "Translate":
                        isP5C_Translate = true;
                        addBySave("P5C_Translate",true);
                        addBySave(ZZXTranslationEnabled,true);
                        break;
                    case "DHZG":
                        isP5C_DHZG = true;
                        addBySave("P5C_DHZG",true);
                        break;
                }
                break;
            case "P5X":
                switch (zzxModel){
                    case "byanda"://百安达
                        addBySave("P5X",true);
                        break;
                    case "p5x_jnkyd"://济南客运段
                        isP5XJNKYD = true;
                        addBySave("ZZXIsHideLcons",true);
                        addBySave("isplaysound",true);
                        addBySave(ZZXAPPEnabled,true);
                        break;
                    case "p5xd_sxjj"://山西交警
                        isP5XD_SXJJ = true;
                        addBySave("P5XD_SXJJ",true);
                        addBySave(Values.LOOP_RECORD,true);
                        addBySave("ZZXAutomatic",true);//默认打开自动上传
                        break;
                    case "shenzhen_liangjian":
                        isAirplaneMode=true;
                        isShowDisk = true;
                        isP5X_SZLJ = true;
                        addBySave("P5X_SZLJ",true);
                        break;
                    case "P5X_CDHN":
                        isP5X_CDHN = true;
                        addBySave("P5X_CDHN",true);
                        addBySave("isplaysound",true);
                        break;
                    case "P5X_liangjian":
                        isP5X_liangjian = true;
                        addBySave("P5X_liangjian",true);
                        addBySave("isplaysound",true);
                        break;
                }
                break;
            case "4GA":
                Log.i("GridVIewPagerAdapter", "zzxModel: "+zzxModel);
                is4GA = true;
                addBySave("zzxReportQuality",true);
                addBySave("isplaysound",true);
                switch (zzxModel){
                    case "BAD":
                    case "BYANDA_FB":
                        addBySave(ZZXPoneEnabled,true);
                        addBySave(ZZXMapEnabled,true);
                        addBySave(ZZXTalkEnabled,true);
                        addBySave(ZZXAPPEnabled,false);
                        break;
                    case "henan_dianwang":
                        is4GA_HNDW =true;
                        addBySave("4GA_HNDW",true);
                        addBySave(ZZXPoneEnabled,true);
                        addBySave(ZZXWXEnabled,true);
                        addBySave(ZZXMapEnabled,true);
                        addBySave(ZZXTalkEnabled,true);
                        break;
                    case "shanghai_songjian_test_25fps":
                        is4GA_TEST =true;
                        addBySave("4GA_TEST",true);
                        addBySave(ZZXPoneEnabled,true);
                        break;
                    case "chengdu_heneng":
                        is4GA_CDHN = true;
                        addBySave("4GA_CDHN",true);
                        break;
                    case "gutexun":
                    case "gutexun_2.0":
                        is4GA_GTX = true;
                        addBySave("4GA_GTX",true);
                        addBySave(ZZXPoneEnabled,true);
                        addBySave(ZZXMapEnabled,true);
                        addBySave(ZZXTalkEnabled,true);
                        addBySave(ZZXAPPEnabled,false);
                        break;
                    case "4GA_GJDW":
                        is4GA_GJDW = true;
                        addBySave("4GA_GJDW",true);
                        addBySave(ZZXPoneEnabled,true);
                        addBySave(ZZXMapEnabled,true);
                        addBySave(ZZXTalkEnabled,true);
                        addBySave(ZZXAPPEnabled,false);
                        break;
                    case "4GA_liangjian":
                        is4GA_LJ = true;
                        addBySave("4GA_LJ",true);
                        addBySave(ZZXPoneEnabled,true);
                        addBySave(ZZXMapEnabled,true);
                        addBySave(ZZXTalkEnabled,true);
                        addBySave(ZZXAPPEnabled,false);
                        break;
                    case "SDTY":
                        is4GA_SDTY = true;
                        addBySave("4GA_SDTY",true);
                        addBySave(ZZXPoneEnabled,true);
                        addBySave(ZZXMapEnabled,true);
                        addBySave(ZZXTalkEnabled,false);
                        addBySave(ZZXAPPEnabled,false);
                        addBySave(ZZXVOS,true);
                        break;
                    case "ZS_FB":
                        addBySave("4GA_ZSFB",true);
                        addBySave(ZZXPoneEnabled,true);
                        addBySave(ZZXMapEnabled,true);
                        addBySave(ZZXTalkEnabled,true);
                        addBySave(ZZXAPPEnabled,true);
                        break;
                    case "4GA_HNYC":
                        is4GA_HNYC = true;
                        addBySave("4GA_HNYC",true);
                        addBySave(ZZXPoneEnabled,true);
                        addBySave(ZZXMapEnabled,true);
                        addBySave(ZZXTalkEnabled,true);
                        addBySave(ZZXAPPEnabled,false);
                        addBySave("isplaysound",false);
                        break;
                }
                break;
            case "4GC":
                addBySave("zzxReportQuality",true);
                addBySave("isplaysound",true);
                addBySave(ZZXAPPEnabled,false);
                is4GC = true;
                switch (zzxModel){
                    case "4GC_BAD":
                        addBySave("BAD_4GC",true);
                        break;
                    case "4GC_SDJW":
                        is4GC_SDJW = true;
                        addBySave("4GC_SDJW",true);
                        addBySave("isplaysound",false);
                        break;
                    case "4GC_NINE":
                        is4GC_NINE = true;
                        addBySave("4GC_NINE",true);
                        break;
                    case "4GC_SY":
                        is4GC_SY = true;
                        addBySave("4GC_SY",true);
                        addBySave("isplaysound",false);
                        break;
                    case "4GC_liangjian":
                        addBySave("4GC_liangjian",true);
                        break;
                }
                break;
            case "4G5":
                is4G5 = true;
                addBySave("zzxReportQuality",true);
                addBySave(ZZXAPPEnabled,false);
                switch (zzxModel){
                    case "4G5_BAD":
                    case "byanda":
                        is4G5_BAD = true;
                        addBySave("BAD_4G5",true);
                        break;
                    case "4G5_GJDW":
                        is4G5_GJDW = true;
                        addBySave("4G5_GJDW",true);
                        break;
                    case "upgrade":
                        is4G5_upgrade = true;
                        addBySave("4G5_upgrade",true);
                        break;
                }
                break;
            case "P80":
                isP80 = true;
                addBySave("zzxReportQuality",true);
                addBySave(ZZXAPPEnabled,false);
                switch (zzxModel){
                    case "BAD":
                        addBySave("BAD_P80",true);
                        break;
                }
                break;
        }
    }
    public void ChangeValues(String name, boolean values){
        Log.e("ChangeValues: ", name + values);
        switch (name){
            case "ZZXIsCheckExternalStorage":
                IsCheckExternalStorage = values;
                break;
            case "ZZXIsShowWaterMark":
                IsShowWaterMark = values;
                break;
            case "ZZXIsBaiDu":
                IsBaidu = values;
                break;
            case "ZZXISSongJian":
                isSongJian = values;
                break;
            case "ZZXOperatingSystem":
                isOperatingSystem = values;
                break;
            case "ZZXFrequency":
                isShowFrequency = values;
                break;
        }
    }
    private int getInt(boolean values) {
        if (values) {
            return 1;
        } else return 0;
    }

    private List<String> ConfigList;

    private void add(String name, boolean values) {
        ConfigList.add(name);
        Log.e("add: ", name + Settings.System.getInt(mContext.getContentResolver(), name, getInt(values)) + "");
        ChangeValues(name, Settings.System.getInt(mContext.getContentResolver(), name, getInt(values)) == 1);
    }
    public int getCount() {
        return ConfigList.size();
    }

    public void ChangeValuesBySave(String name, boolean values) {
        ChangeValues(name, values);
        Settings.System.putInt(mContext.getContentResolver(), name, getInt(values));
    }

    public void addBySave(String name, boolean values) {
        Settings.System.putInt(mContext.getContentResolver(), name, getInt(values));
    }
    public String getList(int postion) {
        return ConfigList.get(postion);
    }
}
