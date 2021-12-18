package com.zzx.police.data;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.widget.Toast;

import com.zzx.police.utils.DeviceUtils;
import com.zzx.police.utils.FTPUploadUtil;

/**
 * Created by wanderFly on 2018/1/16.
 * FTP文件信息上传包装类
 */

public class FTPUploadInfo {
    private String ftpAccount;      //账号
    private String ftpPwd;          //密码
    private String ftpIp;           //ip地址
    private String fileLocalPath;   //本地文件路径        例:文件路径:/zx/
    private String fileLocalName;   //本地文件名字        例:文件名字:Launcher3.apk
    private String saveRemotePath;  //文件服务器上保存路径  例:保存路径:/byandaftp/000010/ (FTP客户端所能获取到最上层目录开始 现在已经改为登录FTP服务器开始，获取到的当前服务器目录)
    private float progress = 0;     //文件上传进度
    private int ftpPort = 21;       //端口号 默认为 21
    private int status = FTPUploadUtil.UPLOAD_STATUS_DEFAULT;//文件上传状态值


    public FTPUploadInfo(String ftpAccount,
                         String ftpPwd,
                         String ftpIp,
                         String fileLocalPath,/*在这里传入的文件路径不应该包含文件名*/
                         String fileLocalName,
                         String saveRemotePath,
                         int ftpPort) {
        this.ftpAccount = ftpAccount;
        this.ftpPwd = ftpPwd;
        this.ftpIp = ftpIp;
        this.fileLocalPath = fileLocalPath;
        this.fileLocalName = fileLocalName;
        this.saveRemotePath = saveRemotePath;
        this.ftpPort = ftpPort;
    }

    @Override
    public String toString() {
        return "------------"
                + "\n" + "FTP账号: " + ftpAccount
                + "\n" + "FTP密码: " + ftpPwd
                + "\n" + "FTP地址: " + ftpIp
                + "\n" + "FTP端口: " + ftpPort
                + "\n" + "文件路径:" + fileLocalPath
                + "\n" + "文件名字:" + fileLocalName
                + "\n" + "保存路径:" + saveRemotePath
                + "\n" + "上传进度:" + progress
                + "\n" + "上传状态:" + status;
    }

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

    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
    }

    public String getFileLocalName() {
        return fileLocalName;
    }

    public void setFileLocalName(String fileLocalName) {
        this.fileLocalName = fileLocalName;
    }

    public String getSaveRemotePath() {
        return saveRemotePath;
    }

    public void setSaveRemotePath(String saveRemotePath) {
        this.saveRemotePath = saveRemotePath;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@FTPUploadUtil.UploadStatusCode int status) {
        this.status = status;
    }


    /**
     * 根据包含名字的路径获取FTP文件上传对象
     *
     * @param context     上下文
     * @param pathAndName 包含文件名的路径
     * @return 文件上传对象
     */
    public static FTPUploadInfo getFTPUploadInfo(Context context, String pathAndName) {
        String[] values = getFileNameAndPath(pathAndName);
        return values == null ? null : getFTPUploadInfo(context, values[0], values[1]);
    }

    /**
     * 获取文件路径和文件名
     *
     * @param pathAndName 包含文件名字的路径字符串
     * @return 字符串数组   values[0]:文件路径  values[1]:文件名
     */
    public static String[] getFileNameAndPath(String pathAndName) {
        if (pathAndName == null || pathAndName.equals(""))
            return null;
        String[] values = new String[2];
        String[] dirs = pathAndName.split("/");
        values[1] = dirs[dirs.length - 1];
        //values[1] = "000010_0_20180118_172103.mp4";
        values[0] = "";
        for (int i = 0; i < dirs.length - 1; i++) {
            if (!dirs[i].equals(""))
                values[0] += "/" + dirs[i];
        }
        values[0] += "/";
        return values;
    }

    /**
     * 获取FTP文件上传对象
     *
     * @param context       上下文
     * @param localFilepath 文件路径
     * @param localFileName 文件名
     * @return FTP文件上传对象
     */
    public static FTPUploadInfo getFTPUploadInfo(Context context, String localFilepath, String localFileName) {
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        String user = Settings.System.getString(resolver, Values.FTP_NAME);
        String pass = Settings.System.getString(resolver, Values.FTP_PASS);
        String ip = Settings.System.getString(resolver, Values.FTP_IP);
        String port = Settings.System.getString(resolver, Values.FTP_SET_PORT);

        //String user = "byandaftp";
        //String pass = "123456";
        //String ip = "120.77.153.207";
        //String ip = "120.55.162.188";
        //String port = "21";
        if (ip == null || ip.equals("") || port == null || port.equals("") || user == null || user.equals("") || pass == null || pass.equals("")) {
//            Toast.makeText(context.getApplicationContext(), "获取FTP配置信息失败!! 文件无法上传", Toast.LENGTH_SHORT).show();
            return null;
        }
        int iPort = Integer.parseInt(port);
        //String saveRemotePath = "/" + user +"/"+ DeviceUtils.getPoliceNum(context.getApplicationContext()) + "/";
        //String saveRemotePath = "/" + user + "/nice/too/meet/you/" + DeviceUtils.getPoliceNum(context.getApplicationContext()) + "/";
        //String saveRemotePath = "/" + user + "/" + DeviceUtils.getPoliceNum(context.getApplicationContext()) + "/";
        //String saveRemotePath = "/" +"home/"+ user + "/555555/";
        //String saveRemotePath = "/" + user + DeviceUtils.getPoliceNum(context.getApplicationContext());
        String saveRemotePath = "/" + DeviceUtils.getPoliceNum(context.getApplicationContext()) + "/";
        //saveRemotePath = "/999999/";
        return new FTPUploadInfo(user,
                pass,
                ip,
                localFilepath,
                localFileName,
                saveRemotePath,
                iPort
        );
    }
}
