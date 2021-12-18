package com.zzx.police.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Administrator on 2018/8/1 0001.
 */

public class AESUtils {

    /**
     * 加密一组byte[] 数组
     * @param src 待加密数组
     * @param dataLen 需要加密的数据长度
     * @param key    KEY
     * @return
     * @throws Exception
     */
    public static byte[] encode(byte[] src,int dataLen,String key) throws Exception{
        return Operation(src,dataLen, key, Cipher.ENCRYPT_MODE);
    }
    /**
     * 加密一组byte[] 数组
     * @param src 待加密数组
     * @param key KEY
     * @return
     * @throws Exception
     */
    public static byte[] encode(byte[] src,String key) throws Exception{
        return Operation(src,src.length, key, Cipher.ENCRYPT_MODE);
    }




    /**
     * 解密一组byte[] 数组
     * @param src 待解密数组
     * @param dataLen 需要解密的数据长度
     * @param key    KEY
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] src,int dataLen,String key) throws Exception{
        return Operation(src,dataLen, key, Cipher.DECRYPT_MODE);
    }
    /**
     * 解密操作
     * @param src 待解密数组
     * @param key   KEY
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] src,String key) throws Exception{
        return Operation(src,src.length, key, Cipher.DECRYPT_MODE);
    }

    /**
     * 加密或者解密一组数据
     * @param src   想加密或解密的原始数据
     * @param dataLen  指定想加密或解密的原始数据长度。
     * @param key   KEY
     * @param mode  加密或者解密模式。
     *              <code>ENCRYPT_MODE</code>or <code>DECRYPT_MODE</code>
     * @return  加密或者解密后的结果
     * @throws Exception
     */
    private static byte[] Operation(byte[] src,int dataLen,String key,int mode) throws Exception{
        byte[] raw=cheackKey(key);
        SecretKeySpec keySpec=new SecretKeySpec(raw, "AES");
        Cipher cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(mode, keySpec);
        byte[] encrypted=cipher.doFinal(src,0,dataLen);
        return encrypted;
    }

    /**
     * 检查key值
     * @param key 原始key值
     * @return  key的byte[]数组
     * @throws UnsupportedEncodingException
     */
    private static  byte[] cheackKey(String key) throws UnsupportedEncodingException {
        if (key==null) {
            throw new RuntimeException("key不能为空");
        }
        if (key.length()>16) {
            throw new RuntimeException("Key长度必须小于16位字符");
        }
        byte[] raw = key.getBytes("utf-8");
        if(key.length()<16) {
            byte[] keyBytes = new byte[16];
            Arrays.fill(keyBytes, (byte) 0x0);
            System.arraycopy(raw, 0, keyBytes, 0, raw.length);
            return keyBytes;
        }
        else {
            return raw;
        }
    }
}
