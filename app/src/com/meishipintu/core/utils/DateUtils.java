package com.meishipintu.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/8/16.
 */
public class DateUtils {

    public static String getDateFormat(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return  format.format(new Date(Long.valueOf(timeStamp)*1000));
    }

    public static String getDateFormatSlash(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return  format.format(new Date(Long.valueOf(timeStamp)*1000));
    }

    public static String getTimePeriod(String startTimeStamp, String endTimeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        //网络获取时间戳是秒为单位，在java中以毫秒为单位，要先乘以1000
        return format.format(new Date(Long.valueOf(startTimeStamp)*1000)) + "——"
                + format.format(new Date(Long.valueOf(endTimeStamp)*1000));
    }

    public static String getTimePeriodWithSlash(String startTimeStamp, String endTimeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd");
        return format.format(new Date(Long.valueOf(startTimeStamp)*1000)) + "-"
                + format.format(new Date(Long.valueOf(endTimeStamp)*1000));
    }

    public static String[] getTimeArray(String timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-HH:mm");
        String times = sdf.format(new Date(Long.valueOf(timeStamp) * 1000));
        return times.split("-");
    }
}
