package com.zzx.police.utils;

/**
 * Created by Administrator on 2018/8/8 0008.
 */

public class AESTask {
    public static final int WAIT_s=0;//等待状态
    public static final int ENCRYPT_s=1;//加密状态
    public static final int DECRYPT_s=2;//解密状态
    public static final int FINISH_s=3;//完成状态
    public static final int FAIL_s=4;//失败状态
    //原文件路径
    private String srcPath;
    //结果路径
    private String decPath;
    //密码
    private String pwd;
    //加密或者解密
    private int mode;
    //id,用于任务的唯一标识
    private int id;
    /**
     * 状态
     */
    private int status;
    private static int taskId=0;
    public  AESTask(String srcPath, String decPath, String pwd, int mode) {
        this.srcPath=srcPath;
        this.decPath=decPath;
        this.pwd=pwd;
        this.mode=mode;
        synchronized (AESTask.class) {
            id = taskId++;
        }
        this.status=WAIT_s;
    }


    public String getSrcPath() {
        return srcPath;
    }
    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }
    public String getDecPath() {
        return decPath;
    }
    public void setDecPath(String decPath) {
        this.decPath = decPath;
    }
    public String getPwd() {
        return pwd;
    }
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    public int getMode() {
        return mode;
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
    public int getId() {
        return id;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public static int getTaskId() {
        return taskId;
    }
    public static void setTaskId(int taskId) {
        AESTask.taskId = taskId;
    }

    @Override
    public String toString() {
        return "AESTask{" +
                "id=" + id +
                ", status=" + status +
                '}';
    }
}
