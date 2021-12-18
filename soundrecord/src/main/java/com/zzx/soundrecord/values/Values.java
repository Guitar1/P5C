package com.zzx.soundrecord.values;

import android.os.Environment;

import java.io.File;

public class Values {

	// public static final String SDCARD_PATH = "/sdcard"+ File.separatorChar;
	public static final String SDCARD_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separatorChar;
	public static final String FILE_NAME_DATE_FORMATE = "yyyyMMdd_HHmmss";
	public static final String SOUND_TEMP_PATH = SDCARD_PATH + "recordSound";
	public static final String FILE_FRONT = "_2_";
	public static final String FILE_CONST = "文件名:";
	public static final String SOUND_SAVE_PATH = SDCARD_PATH + "police/sound";
	public static final String IMAGEDIR = "/sdcard/Pictures/Screenshots";
	public static final String VIDEODIR = SDCARD_PATH + "OliveDownload";
	public static final String START_RECORD = "startRecord";
	public static final String STOP_RECORD = "stopRecord";
	public static final String SOUND_RECORDING = "soundRecording";
	public static final String MASK = "_IMP";
	public static final String PACKAGE_NAME_POLICE = "com.zzx.police";
	public static final String POLICE_NUMBER = "police_number";
	public static final String DEFAULT_NUMBER = "000000";
	public static final String DEFAULT_NUMBER_8 = "00000000";
	public static final String DEVICE_NUMBER = "device_number";
	public static final String DEVICE_NUM = "devicenum";
	public static final String DEVICE_DEFAULT_NUM = "00000";
	public static final String DEVICE_DEFAULT_NUM_4 = "0000";
	public static boolean isButtonKey = false;
	public static boolean isManual = false;//手动
	public static boolean isSubsection = false;
    public static final String LOGCAT_MIC = "com_logcat_mic";
    public static final String ZZX_STATE = "zzx_state";
}
