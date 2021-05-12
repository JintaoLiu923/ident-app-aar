package com.hachi.publishplugin.activity.lock;

import android.content.Context;
import android.nfc.Tag;

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
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 关锁
 */
public class CloseLockPlugin extends BasePlugin {
    private final static String TAG = "CloseLockPlugin";
    private static Boolean ifPadsMatch;
    private static Boolean ifPadChanged;
    private static Boolean ifSubPadChanged;

    @DoNotRename
    public static ResultBean closeLock(Context context, Tag tag, LockTagBean topTag, LockTagBean bottomTag, String key, String EToken, String userName) {
        mContext = context;
        sResultBean = new ResultBean();
        mHeader = new HashMap<>();

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

//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
//        SharedPreferences.Editor edit = sharedPreferences.edit();
//        edit.clear();
//        edit.apply();
//
//        OkHttp okHttp = new OkHttp(context);
//        HashMap<Object, Object> params = new HashMap<>();
//        params.put("rasId", topTag.getRasId());
//        params.put("pad", topTag.getRasPad());
//        params.put("subRasId", bottomTag.getRasId());
//        params.put("subPad", bottomTag.getRasPad());
//
//        Gson gson = new Gson();
//        String jsonData = gson.toJson(params);
//        LogUtil.i(TAG, "json数据 --> " + jsonData);
//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
//        LogUtil.i(TAG, "requestBody --> " + requestBody.toString());
//
//        mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
//        mHeader.put(Constant.IDENT_TOKEN, EToken);
//        mHeader.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
//
//        okHttp.postFromInternet(Constant.LOCK_CHECK_URL, requestBody, mHeader);
//
//        boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);
//        if (!flag) {
//            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
//            return sResultBean;
//        }
//
//        String result = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
//        LogUtil.d(TAG, "result --> " + result);
//        LockCheckBean lockCheckBean = new ParseJson().Json2Bean(result, LockCheckBean.class);
//        if (lockCheckBean.getErrno() != 0) {
//            sResultBean.setErrno(lockCheckBean.getErrno());
//            sResultBean.setErrmsg(lockCheckBean.getErrmsg());
//            sendErrorLog(bottomTag, userName, okHttp);
//            return sResultBean;
//        }
//
//        initData(lockCheckBean);
//
//        if (!ifPadsMatch) {
//            sResultBean.setErrno(TagErrorEnum.LOCK_MATCH_FAIL.getCode());
//            sendErrorLog(bottomTag, userName, okHttp);
//            return sResultBean;
//        }


        ResultBean resultBean1 = new UpdateLockPlugin().updateLock(context, topTag, bottomTag, 0, EToken);
        if (resultBean1.getErrno() != 208) {
            sResultBean.setErrmsg(resultBean1.getErrmsg());
            return sResultBean;
        }
        GlobelRasFunc.musicPlay(context, 1);

        mRasId = bottomTag.getRasId();
        mUserName = userName;
        mPower = bottomTag.getPower();

//        sActTypeEnum = ActTypeEnum.ACT_TYPE_电子锁操作_关锁;
//        //设置定位监听
//        mlocationClient.setLocationListener(mLockListener);
//        mlocationClient.startLocation();

        Map<String, String> header = new HashMap<>();

        String state = "正常关锁";
        IdentLogReqBean mIdentLogReqBean = new IdentLogReqBean();
        IdentRasLogBean mLog = new IdentRasLogBean();
        mLog.setCreateBy(1);
        mLog.setCreateByName(userName);
        mLog.setRasId(mRasId);
        mLog.setAction(ActTypeEnum.ACT_TYPE_电子锁操作_关锁.getName());
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

        Gson gson = new Gson();
        String jsonLog = gson.toJson(mIdentLogReqBean);
        LogUtil.i(TAG, "json数据 --> " + jsonLog);
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonLog);
//        LogUtil.i(TAG, "requestBody --> " + requestBody1.toString());
        OkHttp okHttp = new OkHttp(context);
        okHttp.sendLog(Constant.LOG_ADD_URL, requestBody1, header);

        sResultBean.setErrno(TagErrorEnum.CLOSE_LOCK_SUCCESS.getCode());
        return sResultBean;
    }


    private static void initData(LockCheckBean lockCheckBean) {
        ifPadsMatch = lockCheckBean.getData().isIfPadsMatch();
        ifPadChanged = lockCheckBean.getData().isIfPadChanged();
        ifSubPadChanged = lockCheckBean.getData().isIfSubPadChanged();
    }

}
