package com.zzx.police.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2018/8/1 0001.
 */
public class FileAesUtil {
    /**
     * 文件AES加密方法
     * @param srcPath  待加密文件路径(加密之前的路径)
     * @param decPath  生成的加密文件路径(加密之后的路径)
     * @param pwd      KEY
     * @param buffLen  一次IO操作，操作的数据量，比如4096，则代表函数每次从文件中读取4096byte数据进行加密。
     *                 ①：该数字越大，消耗的内存越大
     *                 ②：该数字越大，加密的次数就会越小，整体加密时间越短
     *                 ③：该值不同，加密结果不同。所以解密时的buffLen和加密时的buffLen必须一致
     * @return
     * @throws IOException
     * @Note   该方法是个阻塞方法，建议在子线程中调用
     */
    public  static boolean fileEncrypt(String srcPath, String decPath, String pwd,int buffLen) throws IOException {
        FileInputStream fis=null;
        FileOutputStream fos=null;
        try {
            //打开待加密文件,如果不存在，抛出异常
            File fil1 = new File(srcPath);
            if(!fil1.exists())
            {
                throw new RuntimeException("文件："+srcPath+"不存在");
            }
            //创建加密结果文件,如果之前已经存在，先删除掉。
            File fil2 = new File(decPath);
            if(fil2.exists())
            {
                fil2.delete();
            }
            fil2.createNewFile();

            fis = new FileInputStream(fil1);
            fos = new FileOutputStream(fil2);
            byte[] buf = new byte[buffLen];
            int len = -1;
            //读取待加密文件
            while((len = fis.read(buf,0,buffLen)) != -1){
              //对读取的数字进行加密，然后存入 加密结果文件
              byte[] results=  AESUtils.encode(buf,len,pwd);//加密数据
              if((results!=null)&&(results.length!=0))
              {
                  fos.write(results);
              }
            }
            //释放资源
            fos.flush();
            fis.close();
            fos.close();
            return true;
        } catch (Exception e) {
            if(fis!=null)
            {
                fis.close();
            }
            if(fos!=null)
            {
                fos.close();
            }
        }
        return false;
    }

    /**
     * 文件AES解密方法
     * @param srcPath  待解密文件路径(解密之前的路径)
     * @param decPath  生成的解密文件路径(解密之后的路径)
     * @param pwd      KEY
     * @param buffLen  一次IO操作，操作的数据量，比如4096，则代表函数每次从文件中读取4096byte数据进行解密。
     *                 ①：该数字越大，消耗的内存越大
     *                 ②：该数字越大，解密的次数就会越小，整体解密时间越短
     *                 ③：该值不同，解密结果不同。所以解密时的buffLen和加密时的buffLen必须一致
     * @return
     * @throws IOException
     * @Note   该方法是个阻塞方法，建议在子线程中调用
     */
    public static boolean fileDecrypt(String srcPath, String decPath, String pwd,int buffLen) throws IOException {
        FileInputStream fis=null;
        FileOutputStream fos=null;
        /*一次解密需要的数据长度
        因为加密时，一组数据的加密结果会比源数据多16byte。
        所以，如果我们想一次得到buffLen的源数据，则需要解析（buffLen+16）长度的数据*/
        int decryptDataLen=buffLen+16;
        try {
            //打开待解密文件,如果不存在，抛出异常
            File fil1 = new File(srcPath);
            if(!fil1.exists())
            {
                throw new RuntimeException("文件："+srcPath+"不存在");
            }
            //创建解密结果文件,如果之前已经存在，先删除掉。
            File fil2 = new File(decPath);
            if(fil2.exists())
            {
                fil2.delete();
            }
            fil2.createNewFile();
            fis = new FileInputStream(fil1);
            fos = new FileOutputStream(fil2);
            byte[] buf = new byte[decryptDataLen];
            int len = -1;
            //读取待解密文件
            while((len = fis.read(buf,0,decryptDataLen)) != -1){
                //对读取的数字进行解密，然后存入 加密结果文件
                byte[] results=  AESUtils.decrypt(buf,len,pwd);//解密数据
                if((results!=null)&&(results.length!=0))
                {
                    fos.write(results);
                }
            }
            fos.flush();
            fis.close();
            fos.close();
            return true;
        } catch (Exception e) {
            if(fis!=null)
            {
                fis.close();
            }
            if(fos!=null)
            {
                fos.close();
            }
        }
        return false;
    }
}
