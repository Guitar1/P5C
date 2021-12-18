package com.zzx.police.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.zzx.police.data.StoragePathConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**@author zdm
 * Created by zdm on 2016/12/20.
 */
public class FileUtil {



    public static List<File> sortDirTime(String path, boolean dec) {
        return sortDirTime(new File(path), dec);
    }

    /**@param dec if true 则递减排序,反之递增.
     * */
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

    /**@param dec if true 则递减排序,反之递增.根据名字去排序，名字必须全数字
     * */
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
     * */
    public  static void deleteLastVideoFile(Context context) {
        boolean isDelete = false;
        try {
            File file;
            String path = Environment.getExternalStorageDirectory() + StoragePathConfig.getStorageRootPath(context) + File.separator + "video" + File.separator;
            file = new File(path);
            if (file.exists() && file.isDirectory()) {
                List<File> list = FileUtil.sortDirName(file, true);//目录按名字顺序排序
                if (list != null && list.size() > 0) {
                    for (File video : list) {
                        File[] isnull = video.listFiles();//判断文件夹下面是不是没有文件
                        if (isnull.length > 0) {
                            List<File> list2 = FileUtil.sortDirTime(video, true);//文件按时间顺序排序
                            if (list2 != null && list2.size() > 0) {
                                for (File video2 : list2) {
                                    if (!isDelete) {
//                                         LogUtil.e(  "zzx", "file_name= " +video2.toString());
                                        deleteFile(false, video2.toString());//删除一个文件
                                        isDelete=true;
                                    }
                                }
                            }
                        } else if (isnull.length == 0) {
                            if (!isDelete) {
                                deleteFile(true, video.toString());//删除空目录
                                isDelete=true;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public  static boolean deleteFile(boolean isDir, String filePath) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String cmd = isDir ? "rm -rf " + filePath : "rm " + filePath;
            Process process = runtime.exec(cmd);
            process.waitFor();
            process.destroy();
            Log.d("zzx", "删除成功--->" + filePath);
//             LogUtil.e(  "zzx", "删除成功--->" + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
