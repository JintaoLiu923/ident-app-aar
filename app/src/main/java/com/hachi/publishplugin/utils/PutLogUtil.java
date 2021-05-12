package com.hachi.publishplugin.utils;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.google.gson.Gson;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.IdentRasLogBean;
import com.hachi.publishplugin.bean.TmpBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.internet.OkHttp;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PutLogUtil {
    private static final String TAG = "PutLogUtil";
    private static Map<String, Object> map = new HashMap<>();
    protected static Map<String, String> mHeader = new HashMap<>();

    /**
     * 发送错误日志等...
     *
     * @param uid         标签uid
     * @param EToken
     * @param actTypeEnum
     */
    public static void putResult(Context context, String uid, String EToken, ActTypeEnum actTypeEnum, String result) {
        mHeader = new HashMap<>();

        IdentLogReqBean mIdentLogReqBean = new IdentLogReqBean();
        IdentRasLogBean mLog = new IdentRasLogBean();
        mLog.setUid(uid);
        mLog.setAction(actTypeEnum.getName());
        mLog.setResult(result);
        mIdentLogReqBean.setLog(mLog);
//        GlobelHttpFunc.sendLog(map, EToken);

        Gson gson = new Gson();
        String jsonLog = gson.toJson(mIdentLogReqBean);
        LogUtil.i(TAG, "json数据 --> " + jsonLog);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonLog);

        mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
        mHeader.put(Constant.IDENT_TOKEN, EToken);
//
        OkHttp okHttp = new OkHttp(context);
        okHttp.sendLog(Constant.LOG_ADD_URL, requestBody, mHeader);
    }

    public static void putLocationLog(Context context, String uid, String EToken, ActTypeEnum actTypeEnum, AMapLocation aMapLocation, String error) {
        mHeader = new HashMap<>();

        IdentLogReqBean identLogReqBean = new IdentLogReqBean();
        IdentRasLogBean log = new IdentRasLogBean();
        log.setUid(uid);
        log.setAction(actTypeEnum.getName());
        log.setResult(error);
        log.setLat(BigDecimal.valueOf(aMapLocation.getLatitude()));
        log.setLng(BigDecimal.valueOf(aMapLocation.getLongitude()));
        identLogReqBean.setLog(log);
        Gson gson = new Gson();
        String jsonLog = gson.toJson(identLogReqBean);
        LogUtil.i(TAG, "json数据 --> " + jsonLog);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonLog);

        mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
        mHeader.put(Constant.IDENT_TOKEN, EToken);
//
        OkHttp okHttp = new OkHttp(context);
        okHttp.sendLog(Constant.LOG_ADD_URL, requestBody, mHeader);

//        mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
//        mHeader.put(Constant.IDENT_TOKEN, EToken);

//        OkHttp okHttp = new OkHttp(context);
//        okHttp.sendLog(SERVER_URL + "app/ras/log/add?uid=" + uid + "&lng=" + aMapLocation.getLongitude() + "&lan=" + aMapLocation.getLatitude()
//                + "&action=" + actTypeEnum.getName() + "result=" + error, mHeader);

    }

    public static void putTempLog(Context context, String uid, String EToken, ActTypeEnum actTypeEnum, TmpBean tempList) {
        mHeader = new HashMap<>();
        LogUtil.i(TAG, "发送温度日志");

        IdentLogReqBean identLogReqBean = new IdentLogReqBean();
        IdentRasLogBean log = new IdentRasLogBean();
        log.setUid(uid);
        log.setAction(actTypeEnum.getName());
        identLogReqBean.setLog(log);
        identLogReqBean.setTemp(tempList);

        Gson gson = new Gson();
        String jsonLog = gson.toJson(identLogReqBean);
        LogUtil.i(TAG, "json数据 --> " + jsonLog);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonLog);
//
        mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
        mHeader.put(Constant.IDENT_TOKEN, EToken);

        OkHttp okHttp = new OkHttp(context);
        okHttp.sendLog(Constant.LOG_ADD_URL, requestBody, mHeader);
//
//        OkHttp okHttp = new OkHttp(context);
//        okHttp.sendLog(SERVER_URL + "app/ras/log/add", requestBody, mHeader);
//        OkHttp okHttp = new OkHttp(context);
//        okHttp.sendLog(SERVER_URL + "app/ras/log/add?uid=" + uid + "&action=" + actTypeEnum.getName() + "tempList=" + tempList, mHeader);
////        GlobelHttpFunc.sendLog(map, EToken);
    }
}
