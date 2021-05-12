package com.hachi.publishplugin.activity.lock;

import android.content.Context;

import com.allatori.annotations.DoNotRename;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.LockTagBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.LogUtil;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 更新锁状态
 */
public class UpdateLockPlugin extends BasePlugin {
    private final static String TAG = "UpdateLockPlugin";


    @DoNotRename
    public ResultBean updateLock(Context context, LockTagBean topTag, LockTagBean bottomTag, int status, String EToken) {
        mContext = context;
        sResultBean = new ResultBean();
        mHeader = new HashMap<>();
        mParams = new HashMap<>();

        String topRasUid = topTag.getRasId();
        String topRasPad = topTag.getRasPad();
        String bottomRasUid = bottomTag.getRasId();
        String bottomRasPad = bottomTag.getRasPad();
        int power = bottomTag.getPower();

        mParams = initParam(status, topRasUid, topRasPad, bottomRasUid, bottomRasPad, power);

        Gson gson = new Gson();
        String jsonData = gson.toJson(mParams);
        LogUtil.i(TAG, "json数据 --> " + jsonData);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
        LogUtil.i(TAG, "requestBody --> " + requestBody.toString());


        postRequest(Constant.LOCK_UPDATE_URL, requestBody, initHeader(EToken), new RequestListener() {
            @Override
            public void requestFail() {
                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            }

            @Override
            public void error(ResultBean tagBean) {
                sResultBean.setErrno(tagBean.getErrno());
                sResultBean.setErrmsg(tagBean.getErrmsg());
            }

            @Override
            public void success(ResultBean tagBean) {
                sResultBean.setErrno(TagErrorEnum.UPDATE_LOCK_STATUS.getCode());
            }
        });

        if (status == 1) {
            sActTypeEnum = ActTypeEnum.ACT_TYPE_电子锁操作_开锁;
        } else {
            sActTypeEnum = ActTypeEnum.ACT_TYPE_电子锁操作_关锁;
        }
        return sResultBean;
    }

    private HashMap<Object, Object> initParam(int status, String topRasUid, String topRasPad, String bottomRasUid, String bottomRasPad, int power) {
        HashMap<Object, Object> params = new HashMap<>();
        params.put(Constant.RAS_ID, topRasUid);
        params.put(Constant.SUB_RAS_ID, bottomRasUid);
        params.put(Constant.PAD, topRasPad);
        params.put(Constant.SUB_PAD, bottomRasPad);
        params.put(Constant.POWER, power);
        params.put(Constant.STATUS, status);
        return params;
    }

}
