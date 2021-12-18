package com.zzx.police.utils;

/**@author Tomy
 * Created by Tomy on 2015/7/25.
 */
public class ByteIntConvert {
    /**int转byte数组.(小端存储)
     * */
    public static byte[] intToByteArraySmall(int value) {
        byte[] result = new byte[4];
        int offset = 0;
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (value >> offset & 0xff);
            offset += 8;
        }
        return result;
    }

    /**int转byte数组(大端存储)
     * */
    public static byte[] intToByteArrayBig(int value) {
        byte[] result = new byte[4];
        int offset = 0;
        for (int i = 3; i >= 0; i--) {
            result[i] = (byte) (value >> offset & 0xff);
            offset += 8;
        }
        return result;
    }

    /**byte转int.(大端存储)
     * */
    public static int byteArrayToIntBig(byte[] value) {
        return (value[3] & 0xff) | ((value[2] << 8) & 0xff00)
                | ((value[1] << 16) & 0xff0000) | ((value[0] << 24) & 0xff000000);
    }

    /**byte转int.(小端存储)
     * */
    public static int byteArrayToIntSmall(byte[] value) {
        return (value[0] & 0xff) | ((value[1] << 8) & 0xff00)
                | ((value[2] << 16) & 0xff0000) | ((value[3] << 24) & 0xff000000);
    }
}
