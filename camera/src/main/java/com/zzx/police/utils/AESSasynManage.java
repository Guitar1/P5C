package com.zzx.police.utils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

/**
 * Created by Administrator on 2018/8/8 0008.
 */

public class AESSasynManage {
    //正在等待的任务集合
    private List<AESTask> waitAesTasks;
    //正在运行的任务集合
    private List<AESThread> runAesThreads;
    //    //暂停任务的集合 懒得做
//    private List<AESTask> pauseAesTasks;
    //加密和解密时一次读多少字节的数据
    private  int readFileLen=4096;
    //同时执行的任务数量,默认5
    private int maxTskCount=5;
    //状态修改监听
    private TaskStatusChangeListener taskStatusChangeListener;
   //单例----------------------------
    private static AESSasynManage instance;
    private AESSasynManage(){
        waitAesTasks =new ArrayList<>();
        runAesThreads=new ArrayList<>();
    }
    public static synchronized AESSasynManage getInstance(){
        if (instance == null) {
            instance = new AESSasynManage();
        }
        return instance;
    }
    //------------------------
    /**
     * 创建并且执行一个加密任务
     * @param srcPath 原文件路径
     * @param decPath 加密结果文件路径
     * @param pwd    密码
     * @return 任务对象
     */
    public AESTask createAndExecuteEncryptTask(String srcPath, String decPath, String pwd)
    {

        AESTask aesTask= new AESTask( srcPath,  decPath,  pwd, Cipher.ENCRYPT_MODE);
        executeAESTask(aesTask);
        return aesTask;
    }
    /**
     * 创建并且执行一个解密任务
     * @param srcPath 原文件路径
     * @param decPath 解密结果文件路径
     * @param pwd    密码
     * @return 任务对象
     */
    public AESTask createAndExecuteDecryptTask(String srcPath, String decPath, String pwd)
    {
        AESTask aesTask= new AESTask(srcPath,  decPath,  pwd, Cipher.DECRYPT_MODE);
        executeAESTask(aesTask);
        return aesTask;
    }

    /**
     * 执行一个指定任务
     * @param aesTask 指定的任务对象
     */
     public synchronized void  executeAESTask(AESTask aesTask)
    {
        waitAesTasks.add(aesTask);
        FindAesTaskRun();
    }
    /**
     * 设置最大任务数量
     * @param maxTskCount
     */
    public void  setMaxTskCount(int maxTskCount)
    {
        this.maxTskCount=maxTskCount;
    }

    /**
     * 设置加解密读文件的数据长度
     * @param readFileLen
     */
    public void setReadFileLen(int readFileLen) {
        this.readFileLen = readFileLen;
    }

    public void setTaskStatusChangeListener(TaskStatusChangeListener taskStatusChangeListener) {
        this.taskStatusChangeListener = taskStatusChangeListener;
    }

    /**
     * 寻找一个没有执行的任务进入执行状态
     */
    private void FindAesTaskRun()
    {
        if(waitAesTasks.size()==0)
        {
            return;
        }
        if(runAesThreads.size()>=maxTskCount)
        {
            return;
        }
        //将正在等待的任务加入正在执行的任务
        for(AESTask task:waitAesTasks)
        {
            if(task.getStatus()==AESTask.WAIT_s)
            {
                AESThread aesThread= new AESThread(task);
                runAesThreads.add(aesThread);
                if(runAesThreads.size()>=maxTskCount)
                {
                    break;
                }
            }
        }
        for(AESThread aesThread:runAesThreads)
        {
            if(aesThread.getState()== Thread.State.NEW)
            {
                waitAesTasks.remove(aesThread.getTask());
                aesThread.start();
            }
        }
    }
    private synchronized void changeAESTaskStatus(AESThread thread ,int newStatus)
    {
        AESTask task=thread.getTask();
        task.setStatus(newStatus);
        switch (newStatus)
        {
            case AESTask.WAIT_s:
                break;
            case AESTask.DECRYPT_s:
            case AESTask.ENCRYPT_s:

                break;
            case AESTask.FINISH_s:
            case AESTask.FAIL_s:
                runAesThreads.remove(thread);
                FindAesTaskRun();
                break;
        }
        if(taskStatusChangeListener !=null)
        {
            taskStatusChangeListener.onTaskStatusChange(task);
        }
    }

    private final class AESThread extends Thread{
        private AESTask task;
        public AESThread(AESTask task){
            this.task=task;
        }
        public AESTask getTask() {
            return task;
        }
        @Override
        public void run() {
            if(task!=null)
            {
                try {
                    if(task.getMode()==Cipher.ENCRYPT_MODE){
                        changeAESTaskStatus(AESThread.this,AESTask.ENCRYPT_s);//状态改为开始加密
                        FileAesUtil.fileEncrypt(task.getSrcPath(),task.getDecPath(),task.getPwd(),readFileLen);
                    }
                    else {
                        changeAESTaskStatus(AESThread.this,AESTask.DECRYPT_s);//状态改为开始解密
                        FileAesUtil.fileDecrypt(task.getSrcPath(),task.getDecPath(),task.getPwd(),readFileLen);
                    }
                    changeAESTaskStatus(AESThread.this,AESTask.FINISH_s);//状态改为完成
                } catch (IOException e) {
                    e.printStackTrace();
                    changeAESTaskStatus(AESThread.this,AESTask.FAIL_s);//状态改为失败
                }
            }
        }
    }
    public interface TaskStatusChangeListener {
        void onTaskStatusChange(AESTask task);
    }
}
