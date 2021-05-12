package com.hachi.publishplugin.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.internet.OkHttp;


public class DecodeUtils {

    private static final String TAG = "DecodeUtils";

    public static String getDecode(Context context,final String path, String adminToken) {

        OkHttp okHttp=new OkHttp(context);
        okHttp.decodePad(path,adminToken);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
        boolean flag = sharedPreferences.getBoolean("flag", false);

        if (!flag) {
            return "网络请求失败";
        }

        String responseData = sharedPreferences.getString("responseData", "");

        LogUtil.d(TAG, "res --> " + responseData);
        JSONObject jsonObject = JSONObject.parseObject(responseData);
        String data = jsonObject.getString("data");

        return data;
    }
}