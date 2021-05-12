package com.hachi.publishplugin.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    private static SimpleDateFormat simpleDateFormat;
    private static Date date;

    public static String getTime(String format) {
        simpleDateFormat = new SimpleDateFormat(format);
        return get(simpleDateFormat);
    }

    private static String get(SimpleDateFormat simpleDateFormat) {
        date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        return time;
    }
}
