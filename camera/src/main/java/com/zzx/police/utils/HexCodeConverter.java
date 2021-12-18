package com.zzx.police.utils;

/**@author Tomy
 * Created by Tomy on 2014/10/29.
 */
public class HexCodeConverter {
    /** Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
            * @param src byte[] data
    * @return hex string
    */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv.toUpperCase() + " ");
        }
        return stringBuilder.toString();
    }
    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static  byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = b.length - 1; i >= 0; i--) {
            b[i] = Long.valueOf(temp & 0xff).byteValue();// 将最低位保存在最高位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = b.length - 1; i >= 0; i--) {
            b[i] = Integer.valueOf(temp & 0xff).byteValue();// 将最低位保存在最高位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    public static long byteToLong(byte[] value) {
        long temp = 0;
        for (int i = 0; i < value.length; i++) {
            temp = (temp | value[i]) << 8;
        }
        return temp;
    }
}
