package com.zzx.police.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zdm on 2016/8/3 0003.
 * 功能：log打印
 */
public class LogUtil {
    private static SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[yy-MM-dd hh:mm:ss]: ", Locale.getDefault());
    private static String DEFAULT_PATH = Environment.getExternalStorageDirectory() + File.separator + "zzx_log";
    private static Writer mWriter;
    private static boolean DISPLAY_TIME = false;

    private LogUtil() {
    }

    /**
     * 初始化日志写入工具
     *
     * @param isDisplayTime 是否显示具体时间(精确到毫秒)
     */
    public static void init(boolean isDisplayTime) {
        if (mWriter == null) {
            createFile();
        }
        DISPLAY_TIME = isDisplayTime;
    }

    /**
     * 创建文件
     */
    private static void createFile() {
        try {
            File dir = new File(DEFAULT_PATH);
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdirs();
            }
            String fileName = fileNameFormat.format(new Date());
            String defaultName = dir.getAbsolutePath() + File.separator + fileName + ".txt";
            /**注意 FileWriter 只能在已经存在的目录下创建文件*/
            mWriter = new BufferedWriter(new FileWriter(defaultName), 2048);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        if (mWriter != null) {
            try {
                mWriter.close();
                mWriter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final int TYPE_VERBOSE = 0;
    private static final int TYPE_DEBUG = 1;
    private static final int TYPE_INFO = 2;
    private static final int TYPE_WARN = 3;
    private static final int TYPE_ERROR = 4;
    private static final int TYPE_ASSERT = 5;

    /**
     * 将日志内容写入文本
     */
    private static void writeText(String TAG, String msg, int type) {
        try {
            if (mWriter == null)
                createFile();
            //如果调用createFile()方法后还是为null
            //有可能是设备连上电脑打开u盘模式,此时不记录日志
            if(mWriter==null){
                return;
            }
            mWriter.write(simpleDateFormat.format(new Date()));
            switch (type) {
                case TYPE_VERBOSE:
                    mWriter.write("V/" + TAG + ": ");
                    break;
                case TYPE_DEBUG:
                    mWriter.write("D/" + TAG + ": ");
                    break;
                case TYPE_INFO:
                    mWriter.write("I/" + TAG + ": ");
                    break;
                case TYPE_WARN:
                    mWriter.write("W/" + TAG + ": ");
                    break;
                case TYPE_ERROR:
                    mWriter.write("E/" + TAG + ": ");
                    break;
                case TYPE_ASSERT:
                    mWriter.write("A/" + TAG + ": ");
                    break;
            }
            if (DISPLAY_TIME) {
                mWriter.write(" " + System.currentTimeMillis() + " " + msg + "");
            } else {
                mWriter.write(msg + "");
            }
            mWriter.write("\n");
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void v(String TAG, String msg) {
        writeText(TAG, msg, TYPE_VERBOSE);
    }

    public static void d(String TAG, String msg) {
        writeText(TAG, msg, TYPE_DEBUG);
    }

    public static void i(String TAG, String msg) {
        writeText(TAG, msg, TYPE_INFO);
    }

    public static void w(String TAG, String msg) {
        writeText(TAG, msg, TYPE_WARN);
    }

    public static void e(String TAG, String msg) {
        writeText(TAG, msg, TYPE_ERROR);
    }
}
