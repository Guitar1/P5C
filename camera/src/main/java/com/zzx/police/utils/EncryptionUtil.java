package com.zzx.police.utils;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.zzx.police.ZZXConfig;
import com.zzx.police.data.FTPUploadInfo;
import com.zzx.police.data.Values;
import com.zzx.police.services.CameraService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xxq on 2019/7/23 1928.
 */
public class EncryptionUtil {
    private static AESSasynManage aesSasynManage;
    private static Context mContext;
    private static String mDuration;
    public static final String ROOT_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separatorChar + "police" + File.separatorChar;
    private static UploadFileUtil mUploadFileUtil;
    public EncryptionUtil(Context context){
        mContext = context;
        mUploadFileUtil = new UploadFileUtil(context);
    }

    /**
     * 对所有文件加密
     */
    public static void taskFiles(){
        String taskFile = Settings.System.getString(mContext.getContentResolver(),"taskFile");
        if (taskFile == null ){
            return;
        }
        DeleteFileUtil.delete(taskFile);
        getUnencryptedFile(new File(ROOT_PATH));
        for (int i = 0; i < unencryptedfilepath.size(); i++) {
            if (unencryptedfilepath.get(i).contains("mp4") ){
                mDuration = FileUtils.getRingDuring(unencryptedfilepath.get(i));
                addUnencry(unencryptedfilepath.get(i), Values.FILE_SUFFIX_VID);
            }
            if (unencryptedfilepath.get(i).contains("avi") ){
                mDuration = FileUtils.getRingDuring(unencryptedfilepath.get(i));
                addUnencry(unencryptedfilepath.get(i), Values.FILE_SUFFIX_VID_AVI);
            }
            if (unencryptedfilepath.get(i).contains("mp3")){
                mDuration = FileUtils.getRingDuring(unencryptedfilepath.get(i));
                addUnencry(unencryptedfilepath.get(i), Values.FILE_SUFFIX_AUDIO);
            }

        }
        unencryptedfilepath.clear();
    }
    //只对关机未来得及加密的文件加密
    public static void taskFile(){
        String taskFile = Settings.System.getString(mContext.getContentResolver(),"taskFile");
        if (taskFile == null) {
            return;
        }
        DeleteFileUtil.delete(taskFile);
        mDuration = FileUtils.getRingDuring(taskFile);
        if (Settings.System.getInt(mContext.getContentResolver(),"ZZXEncryption",0)==1){
            if (taskFile.contains("mp4") ){
                mDuration = FileUtils.getRingDuring(taskFile);
                addUnencry(taskFile, Values.FILE_SUFFIX_VID);
            }
            if (taskFile.contains("avi") ){
                mDuration = FileUtils.getRingDuring(taskFile);
                addUnencry(taskFile, Values.FILE_SUFFIX_VID_AVI);
            }
            if (taskFile.contains("mp3")){
                mDuration = FileUtils.getRingDuring(taskFile);
                addUnencry(taskFile, Values.FILE_SUFFIX_AUDIO);
            }
        }
    }
    private static void getaesSaynManage(){
        if (aesSasynManage==null){
            aesSasynManage = AESSasynManage.getInstance();//获取单例对象
            aesSasynManage.setReadFileLen(128*1024);//设置加密或解密时每次IO操作的字节数。默认4096
            aesSasynManage.setMaxTskCount(3);//设置最大的任务数，默认为5
        }
    }

    private static void setListener(){
        if (aesSasynManage!=null){
            aesSasynManage.setTaskStatusChangeListener(new AESSasynManage.TaskStatusChangeListener() {
                @Override
                public void onTaskStatusChange(AESTask task) {
                    switch (task.getStatus()){
                        case AESTask.WAIT_s:
                            Log.d("123","任务："+task.toString()+"当前状态为等待。。");
                            break;
                        case AESTask.DECRYPT_s:
                            Log.d("123","任务："+task.toString()+"当前状态为解密。。");
                            break;
                        case AESTask.ENCRYPT_s:
                            Log.d("123","任务："+task.toString()+"当前状态为加密。。");
                            Settings.System.putString(mContext.getContentResolver(),"taskFile",task.getDecPath());
                            break;
                        case AESTask.FINISH_s:
                            Log.d("123","任务："+task.toString()+"当前状态为完成。。");
                            Log.d("123", "onTaskStatusChange: "+task.getDecPath());
                            DeleteFileUtil.delete(task.getSrcPath());
                            String decPath = task.getDecPath();
                            String uploadFileName = decPath.replace(Values.ENCRYPTION,"_"+String.valueOf(FileUtils.filesize(decPath))+ Values.ENCRYPTION);
                            Settings.System.putString(mContext.getContentResolver(),"taskFile","");
//                            LogUtil.e("加密后文件路径",uploadFileName);
//                            File file = new File(task.getDecPath());
//                            file.renameTo(new File(uploadFileName));
                            if(SystemUtil.isNetworkConnected(mContext) &&(Settings.System.getInt(mContext.getContentResolver(),"ZZXAutomatic",0)==1)){
                                Log.d("123", "uploadFileName: "+decPath);
                                if (mUploadFileUtil.isSetFTP()){
                                    mUploadFileUtil.uploadVideoFile(FTPUploadInfo.getFTPUploadInfo(mContext, decPath));
                                }

                            }
                            break;
                        case AESTask.FAIL_s:
                            Log.d("123","任务："+task.toString()+"当前状态为失败。。");
                            break;
                    }
                }
            });
        }
    }
    /**
     *
     * @param fileName 文件路径
     * @param target    文件后缀
     */
    public static void addUnencry(String fileName,String target){
        getaesSaynManage();
        setListener();
        String encryptionName = fileName.replace(target, Values.ENCRYPTION + target);
        AESTask aesTask = aesSasynManage.createAndExecuteEncryptTask(fileName, encryptionName, "bad123456");
    }

    static List<String> unencryptedfilepath = new ArrayList<>();
    /**
     * 获取未加密的文件
     * @param file
     */
    public static void getUnencryptedFile(File file) {
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File child : listFiles) {
                String name = child.getName();
                if (child.isDirectory()) {
                    getUnencryptedFile(new File(file.toString() + "/" + name));
                } else {
                    if (!name.contains("start") && !name.contains("encryption") && !name.equals("txt")) {
                        unencryptedfilepath.add(child.getPath());
                    }
                }
            }
        } else {
            unencryptedfilepath.clear();
            return;
        }
    }
}
