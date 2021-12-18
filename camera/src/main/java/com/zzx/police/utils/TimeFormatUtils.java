package com.zzx.police.utils;

/**@author Tomy
 * Created by Tomy on 2016/5/3.
 */
public class TimeFormatUtils {
    /**
     * 把秒数解析成格式为00:00:00的时间.
     * */
    public static String secToHour(int second) {
        StringBuilder builder = new StringBuilder();
        int sec = second % 60;
        int min = second / 60;
        int hour = min / 60;
        min = min % 60;
        builder.append(hour > 10 ? hour : "0" + hour);
        builder.append(":");
        builder.append(min > 10 ? min : "0" + min);
        builder.append(":");
        builder.append(sec > 10 ? sec : "0" + sec);
        return builder.toString();
    }
}
