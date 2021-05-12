package com.hachi.publishplugin.activity.lock;

import android.content.Context;
import android.content.SharedPreferences;

import com.allatori.annotations.DoNotRename;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.LockBindResultBean;
import com.hachi.publishplugin.bean.LockTagBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 上传上下盖状态位
 */
public class UploadLockPadPlugin extends BasePlugin {
    private final static String TAG = "UploadLockPadPlugin";

    @DoNotRename
    public static ResultBean uploadPad(Context context, LockTagBean topTag, LockTagBean bottomTag, String key, String EToken) {
        mContext = context;
        sResultBean = new ResultBean();
        mHeader = new HashMap<>();
        mParams = new HashMap<>();

        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }

        //初始化定位
        initLocation(context, key);

        SharedPreferences sharedPreferences = initResourceSP(context);

        OkHttp okHttp = new OkHttp(context);

        initParams(topTag, bottomTag);

        Gson gson = new Gson();
        String jsonData = gson.toJson(mParams);
        LogUtil.i(TAG, "json数据 --> " + jsonData);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
//        LogUtil.i(TAG, "requestBody --> " + requestBody.toString());


//        LogUtil.d(TAG, mHeader.toString());

        //添加电子锁状态
        okHttp.postFromInternet(Constant.PADS_SAVE_URL, requestBody, initHeader(EToken));

        boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);
        if (!flag) {
            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return sResultBean;
        }

        String result = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
        LogUtil.d(TAG, "result --> " + result);
        LockBindResultBean tagBean1 = new ParseJson().Json2Bean(result, LockBindResultBean.class);
        if (tagBean1.getErrno() != 0) {
            sResultBean.setErrno(tagBean1.getErrno());
            sResultBean.setErrmsg(tagBean1.getErrmsg());
            return sResultBean;
        }

        sResultBean.setErrno(TagErrorEnum.UPLOAD_LOCK_PAD_SUCCESS.getCode());

        return sResultBean;
    }


    private static void initParams(LockTagBean topTag, LockTagBean bottomTag) {
        mParams.put(Constant.PAD, topTag.getRasPad());
        mParams.put(Constant.RAS_ID, topTag.getRasId());
        mParams.put(Constant.SUB_PAD, bottomTag.getRasPad());
        mParams.put(Constant.SUB_RAS_ID, bottomTag.getRasId());
    }
}
