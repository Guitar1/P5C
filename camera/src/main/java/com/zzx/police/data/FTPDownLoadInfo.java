package com.zzx.police.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zzx.police.utils.FTPDownUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by wanderFly on 2017/11/16.
 * FTP文件下载相关信息
 */

public class FTPDownLoadInfo implements Parcelable {
    private String ftpAccount; //账号
    private String ftpPwd;     //密码
    private String ftpIp;      //ip地址
    private String filePath;   //文件路径
    private String fileName;   //文件名字
    private String savePath;   //文件在客户端保存路径
    private float progress = 0;//文件下载进度
    private int ftpPort = 21;  //端口号 默认为 21
    private int status = FTPDownUtil.DOWN_STATUS_DEFAULT;//文件下载状态值

    public FTPDownLoadInfo(String ftpAccount,
                           String ftpPwd,
                           String ftpIp,
                           String filePath,
                           String fileName,
                           String savePath,
                           int ftpPort) {
        this.ftpAccount = ftpAccount;
        this.ftpPwd = ftpPwd;
        this.ftpIp = ftpIp;
        this.filePath = filePath;
        this.fileName = fileName;
        this.savePath = savePath;
        this.ftpPort = ftpPort;
    }


    protected FTPDownLoadInfo(Parcel in) {
        ftpAccount = in.readString();
        ftpPwd = in.readString();
        ftpIp = in.readString();
        filePath = in.readString();
        fileName = in.readString();
        savePath = in.readString();
        progress = in.readFloat();
        ftpPort = in.readInt();
        status = in.readInt();
    }

    public static final Creator<FTPDownLoadInfo> CREATOR = new Creator<FTPDownLoadInfo>() {
        @Override
        public FTPDownLoadInfo createFromParcel(Parcel in) {
            return new FTPDownLoadInfo(in);
        }

        @Override
        public FTPDownLoadInfo[] newArray(int size) {
            return new FTPDownLoadInfo[size];
        }
    };

    public String getFtpAccount() {
        return ftpAccount;
    }

    public void setFtpAccount(String ftpAccount) {
        this.ftpAccount = ftpAccount;
    }

    public String getFtpPwd() {
        return ftpPwd;
    }

    public void setFtpPwd(String ftpPwd) {
        this.ftpPwd = ftpPwd;
    }

    public String getFtpIp() {
        return ftpIp;
    }

    public void setFtpIp(String ftpIp) {
        this.ftpIp = ftpIp;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }


    /**
     * 正常的取值范围为 0~1
     */
    public float getProgress() {
        return progress;
    }

    /**
     * 正常的设置值范围为 0~1
     */
    public void setProgress(float progress) {
        this.progress = progress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@FTPDownUtil.StatusCode int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "------------"
                + "\n" + "FTP账号: " + ftpAccount
                + "\n" + "FTP密码: " + ftpPwd
                + "\n" + "FTP地址:" + ftpIp
                + "\n" + "FTP端口:" + ftpPort
                + "\n" + "文件路径:" + filePath
                + "\n" + "文件名字:" + fileName
                + "\n" + "保存路径:" + savePath
                + "\n" + "下载进度:" + progress
                + "\n" + "下载状态:" + status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ftpAccount);
        dest.writeString(ftpPwd);
        dest.writeString(ftpIp);
        dest.writeString(filePath);
        dest.writeString(fileName);
        dest.writeString(savePath);
        dest.writeFloat(progress);
        dest.writeInt(ftpPort);
        dest.writeInt(status);
    }

    public static final String SAVE_FILE_NAME = "FTPDownInfo";

    @IntDef({TYPE_ARRAY_LIST, TYPE_LINKED_HASH_MAP})
    @Retention(RetentionPolicy.SOURCE)
    @interface DataType {
    }

    static final int TYPE_ARRAY_LIST = 1;
    static final int TYPE_LINKED_HASH_MAP = 2;

    /**
     * 将FTPDownInfo对象转换为Json并保存到文件中
     *
     * @param context      上下文
     * @param list         数据列表集合
     * @param saveFileName 保存的文件名
     * @see #getArrayListFromFile(Context, String) 获取对应的对象集合
     */
    public static void saveToFile(Context context, ArrayList<FTPDownLoadInfo> list, @NonNull String saveFileName) {
        saveFile(context, saveFileName, list);
    }

    /**
     * 将FTPDownInfo对象转换为Json并保存到文件中
     *
     * @param context      上下文
     * @param map          map集合
     * @param saveFileName 保存的文件名
     * @see #getLinkHashMapFromFiles(Context, String) 获取对应的对象集合
     */
    public static void saveToFile(Context context, LinkedHashMap<String, FTPDownLoadInfo> map, @NonNull String saveFileName) {
        saveFile(context, saveFileName, map);
    }

    private static void saveFile(Context context, @NonNull String saveFileName, Object src) {
        Gson gson = new Gson();
        String jsonArrayString = gson.toJson(src);
        Writer writer = null;
        try {
            //如果文件已经存在先删除
            context.getApplicationContext().deleteFile(saveFileName);
            OutputStream out = context.getApplicationContext().openFileOutput(saveFileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(jsonArrayString);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从应用 /data/data/包名/files目录中获取文件,并解析成对象集合
     *
     * @param context 上下文
     * @return 返回对应的数组列表集合
     */
    public static ArrayList<FTPDownLoadInfo> getArrayListFromFile(Context context, @NonNull String saveFileName) {
        return getFormFile(context, saveFileName, TYPE_ARRAY_LIST);
    }

    /**
     * 从应用 /data/data/包名/files目录中获取文件,并解析成对象集合
     *
     * @param context 上下文
     * @return 返回对应的哈希链表集合
     */
    public static LinkedHashMap<String, FTPDownLoadInfo> getLinkHashMapFromFiles(Context context, @NonNull String saveFileName) {
        return getFormFile(context, saveFileName, TYPE_LINKED_HASH_MAP);
    }

    /**
     * 从应用 /data/data/包名/files目录中获取文件,并解析成对象集合
     *
     * @param context 上下文
     * @return 返回对象集合(目前仅仅定义了两种)
     */
    private static <T> T getFormFile(Context context, @NonNull String saveFileName, @DataType int type) {
        BufferedReader reader = null;
        try {
            InputStream input = context.getApplicationContext().openFileInput(saveFileName);
            reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            Gson gson = new Gson();
            switch (type) {
                case TYPE_ARRAY_LIST:
                    return gson.fromJson(builder.toString(), new TypeToken<ArrayList<FTPDownLoadInfo>>() {
                    }.getType());
                case TYPE_LINKED_HASH_MAP:
                    return gson.fromJson(builder.toString(), new TypeToken<LinkedHashMap<String, FTPDownLoadInfo>>() {
                    }.getType());
                default:
                    //return gson.fromJson(builder.toString(), new TypeToken<T>() {}.getType());
                    return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
