package com.hachi.publishplugin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONException;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.internet.OkHttp;

import java.io.IOException;


public class LocationUtile {

    private static final String TAG = "LocationUtile";

    /**
     *根据经纬度获取省市区
     * @param lng
     * @param lat
     * @return
     */
    public static String getCoordinate(Context context, String lng, String lat) throws IOException, JSONException {
        if(TextUtils.isEmpty(lng)||TextUtils.isEmpty(lat)){
            return "扫码用户未开启定位";
        }
        OkHttp okHttp=new OkHttp(context);
        okHttp.queryAddress(Double.parseDouble(lng),Double.parseDouble(lat));

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
        boolean flag = sharedPreferences.getBoolean("flag", false);

        if (!flag) {
            return "网络请求失败";
        }
        String responseData = sharedPreferences.getString("responseData", "");
        return responseData;
    }
}