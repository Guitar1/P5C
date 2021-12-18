package com.zzx.police.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.zzx.police.data.FTPUploadInfo;
import com.zzx.police.data.Values;
import com.zzx.police.services.CameraService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadFileUtil {
    private static String TAG = "UploadFileUtil";

    private static Context mContext;
    public FTPUploadUtil mFtpUpload;
    static List<String> filepaths = new ArrayList<>();
    private static final String KEY_FTP_UPLOAD_FILE = "key_ftp_upload_file";
    private static final String KEY_FTP_UPLOAD_STATUS = "key_ftp_upload_status";
    public UploadFileUtil(Context context){
        mContext = context;
    }
    public void uploadVideoFile(FTPUploadInfo info) {
        if (mFtpUpload == null) {
            mFtpUpload = new FTPUploadUtil(mContext, mStatusListener);
        }
        if (info == null) {
            String[] values = new String[]{"配置错误!", "无法上传"};
            saveUploadStatusToSetting(values);
            Toast.makeText(mContext, "没有文件可上传", Toast.LENGTH_SHORT).show();
        } else {
            mFtpUpload.addFileToBuffer(info);
        }
    }

    /**
     * 判断FTP ip  端口是否为空
     * @return
     */
    public boolean isSetFTP(){
        ContentResolver resolver =mContext .getContentResolver();
        String user = Settings.System.getString(resolver, Values.FTP_NAME);
        String pass = Settings.System.getString(resolver, Values.FTP_PASS);
        String ip = Settings.System.getString(resolver, Values.FTP_IP);
        String port = Settings.System.getString(resolver, Values.FTP_SET_PORT);
        if (ip == null || ip.equals("") || port == null || port.equals("") || user == null || user.equals("") || pass == null || pass.equals("")) {
            Toast.makeText(mContext, "获取FTP配置信息失败!! 文件无法上传", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static FTPUploadUtil.FTPUploadStatusListener mStatusListener = new FTPUploadUtil.FTPUploadStatusListener() {
        /**
         * 上传状态更新接口方法
         *
         * @param statusCode 状态码
         * @param info       FTP文件上传对象
         *                   为null:此次没有获取到文件上传对象,如断开连接、登录等操作;
         *                   不为null:此时已经从缓冲队列中获取到上传对象,
         */
        @Override
        public void statusUpdate(@FTPUploadUtil.UploadStatusCode int statusCode, @Nullable FTPUploadInfo info) {
            CameraService.sendMsgToUploadHandler(statusCode, info);
        }

        /**
         * 其它错误
         *
         * @param info      文件上传对象
         * @param errorInfo 错误日志
         */
        @Override
        public void error(@Nullable FTPUploadInfo info, String errorInfo) {
            LogUtil.e(TAG, "error: 文件名:" + (info == null ? "" : info.getFileLocalName()) + " 错误信息:" + errorInfo);
            CameraService.sendMsgToUploadHandler(FTPUploadUtil.UPLOAD_STATUS_ERROR, info);
        }
    };

    public void saveUploadStatusToSetting(String[] values) {
        if (values[0] != null)
            Settings.System.putString(mContext.getContentResolver(), KEY_FTP_UPLOAD_FILE, values[0]);
        if (values[1] != null)
            Settings.System.putString(mContext.getContentResolver(), KEY_FTP_UPLOAD_STATUS, values[1]);
    }

    public void sendUploadFile(FTPUploadInfo info) {
        Intent intent = new Intent();
        intent.setAction("send_upload");
        if (info!=null)
        intent.putExtra("upload_filename", info.getFileLocalPath() + info.getFileLocalName());
        mContext.sendBroadcast(intent);
    }

    /**
     * 获取该文件的简单名字(只取名字的一部分)
     */
    public String getSimpleUploadFileName(FTPUploadInfo info) {
        if (info == null)
            return null;
        String name = info.getFileLocalName();
        String[] values = name.split("_");
        return values[values.length - 1];
    }

    /**
     * 获取该文件的简单名字(只取名字的一部分)
     */
    public String getUploadFileName(FTPUploadInfo info) {
        if (info == null)
            return null;
        String name = info.getFileLocalName();
        return name;
    }

    /**
     * 获取上传百分比
     */
    public String getUploadRate(FTPUploadInfo info) {
        if (info == null)
            return "";
        String content;
        if (info.getProgress() == 0) {
            content = 0 + "%";
        } else if (info.getProgress() == 1) {
            content = 100 + "%";
        } else {
            int tempValue = Math.round(info.getProgress() * 10000);
            if (tempValue % 10 == 0) {
                content = (float) tempValue / 100 + "0%";
            } else {
                content = (float) tempValue / 100 + "%";
            }
        }
        return content;
    }
    //文件上传
    public void upFile() {
        if (!isSetFTP()){
            return;
        }
        getFileList(new File(Values.ROOT_PATH));
//        LogUtil.e(TAG, "需要上传文件个数: "+filepaths.size() );
        for (int i = 0; i < filepaths.size(); i++) {
            uploadVideoFile(FTPUploadInfo.getFTPUploadInfo(mContext, filepaths.get(i)));
        }
        filepaths.clear();
    }
    /**
     * 获取文件
     *
     * @param file
     * @return
     */
    public void getFileList(File file) {
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File child : listFiles) {
                String name = child.getName();
                if (child.isDirectory()) {
                    if (child.list().length == 0) {
                        DeleteFileUtil.delete(child.getPath());
                    }
                    getFileList(new File(file.toString() + "/" + name));
                } else {
                    if ( !name.contains("start") && FileUtils.filesize(child.getPath()) > 0 ) {
                        filepaths.add(child.getPath());
                    }else if (FileUtils.filesize(child.getPath())==0){
                        DeleteFileUtil.delete(child.getPath());
                    }
                }
            }
        } else {
            filepaths.clear();
            return;
        }
    }
}
