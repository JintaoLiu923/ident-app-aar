package com.hachi.publishplugin.activity.lock;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;

import com.alibaba.fastjson.JSONObject;
import com.allatori.annotations.DoNotRename;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.IdentRasLogBean;
import com.hachi.publishplugin.bean.LockCheckBean;
import com.hachi.publishplugin.bean.LockTagBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 关锁
 */
public class CheckLockPlugin extends BasePlugin {
    private final static String TAG = "CloseLockPlugin";
    private static Boolean ifPadsMatch;
    private static Boolean ifPadChanged;
    private static Boolean ifSubPadChanged;
    private static OkHttp sOkHttp;
    private static Gson sGson;

    @DoNotRename
    public static ResultBean checkLock(Context context, Tag tag, LockTagBean topTag, LockTagBean bottomTag, String key, String EToken, String userName) {
        mContext = context;
        sResultBean = new ResultBean();
        mHeader = new HashMap<>();
        mParams = new HashMap<>();

        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }

        //初始化定位
        initLocation(context, GlobelRasFunc.mMapKey);

        //鉴权
        if (!checkLogin(EToken, Constant.username, Constant.password)) {
            return sResultBean;
        }

        SharedPreferences sharedPreferences = initResourceSP(context);

        sOkHttp = new OkHttp(context);

        initParams(topTag, bottomTag);

        sGson = new Gson();
        String jsonData = sGson.toJson(mParams);
        LogUtil.i(TAG, "json数据 --> " + jsonData);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
//        LogUtil.i(TAG, "requestBody --> " + requestBody.toString());

        sOkHttp.postFromInternet(Constant.LOCK_CHECK_URL, requestBody, initHeader(EToken));

        boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);

        if (!flag) {
            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return sResultBean;
        }

        String result = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
        LogUtil.d(TAG, "result --> " + result);
        LockCheckBean lockCheckBean = new ParseJson().Json2Bean(result, LockCheckBean.class);
        if (lockCheckBean.getErrno() != 0) {
            sResultBean.setErrno(lockCheckBean.getErrno());
            sResultBean.setErrmsg(lockCheckBean.getErrmsg());
//            sendErrorLog(bottomTag, userName, okHttp);
            return sResultBean;
        }

        initData(lockCheckBean);

        mRasId = bottomTag.getRasId();
        mUserName = userName;
        mPower = bottomTag.getPower();


        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();

        sOkHttp.getFromInternetById(Constant.LOCK_DETAIL_URL + "?platformId=401&rasId=" + topTag.getRasId(), EToken);

        boolean flag1 = sharedPreferences.getBoolean(Constant.FLAG, false);
        if (!flag1) {
            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return sResultBean;
        }

        String result1 = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
        LogUtil.d(TAG, "result --> " + result1);

        try {
            if (result1 != null) {
                JSONObject jsonObject = JSONObject.parseObject(result1);
                JSONObject data = (JSONObject) jsonObject.get("data");
                if (data != null) {
                    String updateDate = (String) data.get("updateDate");
                    LogUtil.d(TAG, "电子锁关闭时间 --> " + updateDate);
                    LockTagBean lockTagBean = new LockTagBean();
                    lockTagBean.setCloseTime(updateDate);
                    sResultBean.setLockData(lockTagBean);
                }
            }
        } catch (Exception e) {

        }

        if (!ifPadsMatch) {
            sResultBean.setErrno(TagErrorEnum.LOCK_MATCH_FAIL.getCode());
            sendLog(bottomTag, TagErrorEnum.LOCK_MATCH_FAIL.getDescription());
            return sResultBean;
        }

        if (ifPadChanged) {
            sResultBean.setErrno(TagErrorEnum.LOCK_PAD_CHANGE.getCode());
            sendLog(bottomTag, TagErrorEnum.LOCK_PAD_CHANGE.getDescription());
            return sResultBean;
        }

        if (ifSubPadChanged) {
            sResultBean.setErrno(TagErrorEnum.LOCK_SUB_PAD_CHANGE.getCode());
            sendLog(bottomTag, TagErrorEnum.LOCK_SUB_PAD_CHANGE.getDescription());
            return sResultBean;
        }

       /* ResultBean resultBean1 = new UpdateLockPlugin().updateLock(context, topTag, bottomTag, 0, EToken);
        if (resultBean1.getErrno() != 208) {
            sResultBean.setErrmsg(resultBean1.getErrmsg());
            return sResultBean;
        }*/

        sendLog(bottomTag, "正常");

        sResultBean.setErrno(TagErrorEnum.LOCK_STATUS_NORMAL.getCode());
        return sResultBean;
    }


    private static void initParams(LockTagBean topTag, LockTagBean bottomTag) {
        mParams.put(Constant.RAS_ID, topTag.getRasId());
        mParams.put(Constant.PAD, topTag.getRasPad());
        mParams.put(Constant.SUB_RAS_ID, bottomTag.getRasId());
        mParams.put(Constant.SUB_PAD, bottomTag.getRasPad());
    }

    private static void sendLog(LockTagBean bottomTag, String state) {
        Map<String, String> header = new HashMap<>();
        IdentLogReqBean mIdentLogReqBean = new IdentLogReqBean();
        IdentRasLogBean mLog = new IdentRasLogBean();
        mLog.setCreateBy(1);
        mLog.setCreateByName(mUserName);
        mLog.setRasId(mRasId);
        mLog.setAction(ActTypeEnum.ACT_TYPE_电子锁操作_检测.getName());
        mLog.setResult(sResultBean.getErrno() + "");

        if (mlocationClient != null) {
            mlocationClient.startLocation();
        }
        mLog.setLat(GlobelRasFunc.latitude);
        mLog.setLng(GlobelRasFunc.longitude);
        mLog.setComment("{\"content\":null,\"master\":\"" + mUserName + "\",\"name\":\"\",\"sn\":null,\"type\":3,\"power\":" + bottomTag.getPower() + ",\"state\":\"" + state + "\"}");
        mIdentLogReqBean.setLog(mLog);

        header.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
        header.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);

//        Gson gson = new Gson();
        String jsonLog = sGson.toJson(mIdentLogReqBean);
        LogUtil.i(TAG, "json数据 --> " + jsonLog);
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonLog);
//        LogUtil.i(TAG, "requestBody --> " + requestBody1.toString());
//        OkHttp okHttp = new OkHttp(context);
        sOkHttp.sendLog(Constant.LOG_ADD_URL, requestBody1, header);
    }

    private static void initData(LockCheckBean lockCheckBean) {
        ifPadsMatch = lockCheckBean.getData().isIfPadsMatch();
        ifPadChanged = lockCheckBean.getData().isIfPadChanged();
        ifSubPadChanged = lockCheckBean.getData().isIfSubPadChanged();
    }

}
