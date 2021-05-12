package com.hachi.publishplugin.utils;

import android.util.Log;

import com.amap.api.location.AMapLocation;

public class LogUtil {
    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int NOTHING = 6;
    private static final int level = NOTHING; //ljt debug VERBOSE

    public static void v(String tag, String msg) {
        msg = "Debug：" + msg;
        if (level <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        msg = "Debug：" + msg;
        if (level <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        msg = "Debug：" + msg;
        if (level <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        msg = "Debug：" + msg;
        if (level <= WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        msg = "Debug：" + msg;
        if (level <= ERROR) {
            Log.e(tag, msg);
        }
    }

    /**
     * 打印location位置信息日志
     *
     * @param tag
     * @param aMapLocation
     */
    public static void logLocation(String tag, AMapLocation aMapLocation) {
        LogUtil.i(tag, "经度:" + aMapLocation.getLongitude()
                + " 纬度:" + aMapLocation.getLatitude()
                + " 城市:" + aMapLocation.getCity()
                + " 地区:" + aMapLocation.getDistrict());
    }

    /**
     * 打印location错误日志
     *
     * @param tag
     * @param aMapLocation
     */
    public static void logLocationError(String tag, AMapLocation aMapLocation) {
        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
        LogUtil.e(tag, "location Error, ErrCode:" + aMapLocation.getErrorCode()
                + ", errInfo:" + aMapLocation.getErrorInfo());
    }
}
