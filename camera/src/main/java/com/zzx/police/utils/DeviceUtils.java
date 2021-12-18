package com.zzx.police.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.zzx.police.ZZXConfig;
import com.zzx.police.data.ServiceInfo;
import com.zzx.police.data.Values;

/**@author Tomy
 * Created by Tomy on 2014/10/23.
 */
public class DeviceUtils {
    private static final String MODEL_NEED_PRE  = "ZZXNeedPre";
	public static final String CFG_ENABLED_PRISON 	= "ZZXPrisonEnabled";
	public static boolean isPrisonEnabled(Context context) {
		return Settings.System.getInt(context.getContentResolver(), CFG_ENABLED_PRISON, -1) == 1;
	}
    public static String getPoliceNum(Context context) {
		String num = Settings.System.getString(context.getContentResolver(),
				Values.POLICE_NUMBER);
		if (num == null || num.equals(""))
			num = isPrisonEnabled(context) ? Values.POLICE_DEFAULT_NUM_8 : Values.POLICE_DEFAULT_NUM;
		return num;
    }

    public static String getUserNum(Context context) {
        String num = Settings.System.getString(context.getContentResolver(),
                Values.USER_NUMBER);
        if (num == null || num.equals(""))
            num = Values.USER_DEFAULT_NUM;
        return num;
    }

    public static String getDeviceNum(Context context) {
        String num = Settings.System.getString(context.getContentResolver(), Values.DEVICE_NUMBER);
        String modelName = getModelName(context);
        if (num == null || num.equals(""))
            num = Values.DEVICE_DEFAULT_ONLY_NUM;
        if (Settings.System.getInt(context.getContentResolver(), MODEL_NEED_PRE, 1) == 1) {
            return Values.DEVICE_PRE + modelName + "-" + num;
        } else {
            return modelName + "-" + num;
        }
    }



	public static String getPrisonerNum(Context context) {
		String num = Settings.System.getString(context.getContentResolver(),
				Values.PRISONER_NUMBER);
		if (num == null || num.equals(""))
			num = Values.PRISONER_DEFAULT_NUM;
		return num;
	}

	public static boolean writePrisonerNum(Context context, String prisonerNum) {
		return !(prisonerNum == null || !prisonerNum.matches("[A-Za-z0-9]{10}"))
				&& Settings.System.putString(context.getContentResolver(),
				Values.PRISONER_NUMBER, prisonerNum);
	}
    public static String getModelName(Context context) {
        String model = Settings.System.getString(context.getContentResolver(), Values.MODEL_NAME);
        if (model == null || model.equals("")) {
            return Values.MODEL_DEFAULT_NAME;
        }
        return model;
    }

    public static String getSerialName(Context context) {
        String model = Settings.System.getString(context.getContentResolver(), ConfigXMLParser.CFG_SETTING_SERIAL);
        if (model == null || model.equals("")) {
            return Values.MODEL_DEFAULT_SERIAL;
        }
        return model;
    }
    public static boolean writePoliceNum(Context context, String policeNum) {
		if (policeNum == null) {
			return false;
		}
		if (isPrisonEnabled(context)) {
			if (!policeNum.matches("[A-Za-z0-9]{8}")) {
				return false;
			}
		} else if (ZZXConfig.isP5X_SZLJ){
            if (!policeNum.matches("[A-Za-z0-9]{5}")) {
                return false;
            }
		}else {
            if (!policeNum.matches("[A-Za-z0-9]{6}")) {
                return false;
            }
        }

		return Settings.System.putString(context.getContentResolver(),
				Values.POLICE_NUMBER, policeNum);
    }

  public static String getIDCodeName(Context context) {
        String model = Settings.System.getString(context.getContentResolver(), ConfigXMLParser.CFG_SETTING_IDCode);
        if (model == null || model.equals("")) {
            return Values.MODEL_DEFAULT_IDCODE;
        }
        return model;
    }


    public static boolean writeDeviceNum(Context context, String deviceNum) {
        return !(deviceNum == null || !deviceNum.matches("[A-Za-z0-9]{5}")) && Settings.System.putString(context.getContentResolver(), Values.DEVICE_NUMBER,  deviceNum);
    }

    public static ServiceInfo getServiceInfo(Context mContext) {
        ServiceInfo info = new ServiceInfo();
        ContentResolver resolver = mContext.getContentResolver();
        String serviceIP = Settings.System.getString(resolver, Values.SERVICE_IP_ADDRESS);
        String portStr = Settings.System.getString(resolver, Values.SERVICE_IP_PORT);
        if (serviceIP != null && portStr != null && !serviceIP.equals("") && !portStr.equals("")) {
            try {
                int port = Integer.parseInt(portStr);
                info.mServiceIp = serviceIP;
                info.mPort = port;
            } catch (Exception e) {
                return null;
            }
        } else {
            XMLParser parser = XMLParser.getInstance();
            info = parser.parserXMLFile("/etc/service.xml");
            parser.release();
        }
        return info;
    }

    public static boolean checkHasService(Context context) {
        ServiceInfo info = DeviceUtils.getServiceInfo(context);
        return info != null;
    }
}
