package com.zzx.police.utils;

import android.media.MediaPlayer;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zzx.police.MainActivity;
import com.zzx.police.data.Values;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zdm on 2017/7/5 0005.
 * 功能：
 */

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 根据byte数组生成文件
     *
     * @param bytes 生成文件用到的byte数组
     */
    public static void createFileWithByte(byte[] bytes, String fileName) {
        // TODO Auto-generated method stub
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "zzx_video",
                fileName);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }


    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
    static FileOutputStream out = null;
    static File fileReceive;
    static String fileNameReceive;

    public static void saveToFile(byte[] data) {
        Log.e("ZZXZdm", "saveToFile: ------开始------");

        try {
            if (fileReceive == null) {
                fileNameReceive = getFileName();
                fileReceive = new File(fileNameReceive);
                out = new FileOutputStream(fileReceive);
            }
            out.write(data);
        } catch (Exception e) {
            Log.e("ZZXZdm", "saveToFile: ------保存文件出错------");
            e.printStackTrace();
        } finally {
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        Log.e("ZZXZdm", "saveToFile: ------结束------");
    }


    /**
     * 获取指定格式的时间字符串
     */
    private static String getFormatTime() {
        return mDateTimeFormat.format(new Date());

    }

    private static String getFileName() {
        return getDirectory() + File.separator + getFormatTime();
    }

    /**
     * 根据路径创建目录
     *
     * @return 该目录的路径
     */
    private static String getDirectory() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "zzxabc";
        File saveDir = new File(path);
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            /**
             * mkdirs()可以建立多级文件夹， mkdir()只会建立一级的文件夹， 如下：
             * new File("/tmp/one/two/three").mkdirs();
             * 执行后， 会建立tmp/one/two/three四级目录
             * new File("/tmp/one/two/three").mkdir();
             * 则不会建立任何目录， 因为找不到/tmp/one/two目录， 结果返回false
             **/
            saveDir.mkdirs();
        }
        return saveDir.getAbsolutePath();

    }


    public static List<File> sortDirTime(String path, boolean dec) {
        return sortDirTime(new File(path), dec);
    }

    /**
     * @param dec if true 则递减排序,反之递增.
     */
    public static List<File> sortDirTime(File dir, final boolean dec) {
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        Collections.addAll(list, files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                if (file1.lastModified() < file2.lastModified()) {
                    return dec ? -1 : 1;
                } else {
                    return dec ? 1 : -1;
                }
            }
        });
        return list;
    }


    public static List<File> sortDirName(String path, boolean dec) {
        return sortDirName(new File(path), dec);
    }

    /**
     * @param dec if true 则递减排序,反之递增.根据名字去排序，名字必须全数字
     */
    public static List<File> sortDirName(File dir, final boolean dec) {

        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        final List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        Collections.addAll(list, files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                int name1 = Integer.parseInt(file1.getName());
                int name2 = Integer.parseInt(file2.getName());
                if (name1 < name2) {
                    return dec ? -1 : 1;
                } else {
                    return dec ? 1 : -1;
                }
            }
        });
        return list;
    }

    /**
     * 自动删除最早的一个录像文件。
     */
    public static void deleteLastVideoFile() {
        boolean isDelete = false;
        try {
            File file;
            file = new File(Environment.getExternalStorageDirectory() + File.separator + "police" + File.separator + "video" + File.separator);
            if (file.exists() && file.isDirectory()) {
                List<File> list = sortDirName(file, true);//目录按名字顺序排序
                if (list != null && list.size() > 0) {
                    for (File video : list) {
                        File[] isnull = video.listFiles();//判断文件夹下面是不是没有文件
                        if (isnull.length > 0) {
                            List<File> list2 = sortDirTime(video, true);//文件按时间顺序排序
                            printFileLog(list2);
                            if (list2 != null && list2.size() > 0) {
                                for (File video2 : list2) {
                                    if (!isDelete) {
                                        //LogUtil.e("zzx", "file_name= " + video2.toString());
                                        deleteFile(false, video2.toString());//删除一个文件
                                        isDelete = true;
                                    }
                                }
                            }
                        } else if (isnull.length == 0) {
                            if (!isDelete) {
                                deleteFile(true, video.toString());//删除空目录
                                isDelete = true;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void deleteLastPicFile() {
        boolean isDelete = false;
        try {
            File file;
            file = new File(Environment.getExternalStorageDirectory() + File.separator + "police" + File.separator + "pic" + File.separator);
            if (file.exists() && file.isDirectory()) {
                List<File> list = sortDirName(file, true);//目录按名字顺序排序
                if (list != null && list.size() > 0) {
                    for (File video : list) {
                        File[] isnull = video.listFiles();//判断文件夹下面是不是没有文件
                        if (isnull.length > 0) {
                            List<File> list2 = sortDirTime(video, true);//文件按时间顺序排序
                            printFileLog(list2);
                            if (list2 != null && list2.size() > 0) {
                                for (File video2 : list2) {
                                    if (!isDelete) {
                                        //LogUtil.e("zzx", "file_name= " + video2.toString());
                                        deleteFile(false, video2.toString());//删除一个文件
                                        isDelete = true;
                                    }
                                }
                            }
                        } else if (isnull.length == 0) {
                            if (!isDelete) {
                                deleteFile(true, video.toString());//删除空目录
                                isDelete = true;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void deleteLastSoundFile() {
        boolean isDelete = false;
        try {
            File file;
            file = new File(Environment.getExternalStorageDirectory() + File.separator + "police" + File.separator + "sound" + File.separator);
            if (file.exists() && file.isDirectory()) {
                List<File> list = sortDirName(file, true);//目录按名字顺序排序
                if (list != null && list.size() > 0) {
                    for (File video : list) {
                        File[] isnull = video.listFiles();//判断文件夹下面是不是没有文件
                        if (isnull.length > 0) {
                            List<File> list2 = sortDirTime(video, true);//文件按时间顺序排序
                            printFileLog(list2);
                            if (list2 != null && list2.size() > 0) {
                                for (File video2 : list2) {
                                    if (!isDelete) {
                                        deleteFile(false, video2.toString());//删除一个文件
                                        isDelete = true;
                                    }
                                }
                            }
                        } else if (isnull.length == 0) {
                            if (!isDelete) {
                                deleteFile(true, video.toString());//删除空目录
                                isDelete = true;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void deleteLastFile() {
        boolean isDelete = false;
        try {
            File file;
            file = new File(Environment.getExternalStorageDirectory() + File.separator + "police" + File.separator );
            if (file.exists() && file.isDirectory()) {
                List<File> list = sortDirName(file, true);//目录按名字顺序排序
                if (list != null && list.size() > 0) {
                    for (File files : list) {
                        if (!files.isDirectory()){
                            File[] isnull = files.listFiles();//判断文件夹下面是不是没有文件
                            if (isnull.length > 0) {
                                List<File> list2 = sortDirTime(files, true);//文件按时间顺序排序
                                printFileLog(list2);
                                if (list2 != null && list2.size() > 0) {
                                    for (File video2 : list2) {
                                        if (!isDelete) {
                                            //LogUtil.e("zzx", "file_name= " + video2.toString());
                                            deleteFile(false, video2.toString());//删除一个文件
                                            isDelete = true;
                                        }
                                    }
                                }
                            } else if (isnull.length == 0) {
                                if (!isDelete) {
                                    deleteFile(true, files.toString());//删除空目录
                                    isDelete = true;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * 打印文件名字
     */
    public static void printFileLog(@Nullable List<File> files) {
        if (files == null) {
//            LogUtil.e(TAG, "printFileLog: files is null !!");
        } else {
            Log.d(TAG, "printFileLog: --------------start--------------");
            for (File f : files) {
//                LogUtil.d(TAG, "printFileLog: FileName:" + f.getName());
            }
            Log.d(TAG, "printFileLog: --------------end--------------");
        }
    }

    public static boolean deleteFile(boolean isDir, String filePath) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String cmd = isDir ? "rm -rf " + filePath : "rm " + filePath;
            Process process = runtime.exec(cmd);
            process.waitFor();
            process.destroy();
            //Log.d("zzx", "删除成功--->" + filePath);
//            LogUtil.e("zzx", isDir ? "--目录--" : "--文件--" + "删除成功--->" + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 文件大小
     * @param path
     * @return
     * @throws IOException
     */
    public static long filesize(String path) {
        long size = 0;
        File file = new File(path);
        if (file.exists()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                size = fileInputStream.available();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return size;
    }
    /**
     * 获取文件时长
     * @param mUri 文件路径
     * @return
     */

    public static String getRingDuring(final String mUri){
        String duration=null;
        MediaPlayer mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mUri);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        duration = String.valueOf(mMediaPlayer.getDuration());
        mMediaPlayer.release();
//        LogUtil.e("ryan","duration "+duration);
        return duration;
    }
    /**
     * 开机遍历文件是否有不完整（含有start）文件
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
                    if (name.contains("start")) {
                            DeleteFileUtil.delete(child.getPath());
                    }
                }
            }
        }
    }
    /**
     * 上传完成的文件放入集合中
     */
    private static List files = new ArrayList();
    public static void put(String filepath){
        if (files==null)
            files = new ArrayList();
        files.add(filepath);
    }
    public static int getSize(){
        if (files!=null){
            return files.size();
        }
        return 0;
    }
    public static void deleteFile(){
//        LogUtil.e(TAG,"deleteFile: "+files.size());
        boolean isDelete = false;
        if (files.size()>0){
            for (int i = 0; i < files.size(); i++) {
                if (!isDelete){
//                    LogUtil.e(TAG, "deleteFile: "+files.get(i).toString());
                    DeleteFileUtil.delete(files.get(i).toString());
                    files.remove(i);
                }
                isDelete = true;
            }
        }else {
            MainActivity.isStorageNotEnought =true;
            EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORAGE_NOT_ENOUGH);
        }
    }
}
