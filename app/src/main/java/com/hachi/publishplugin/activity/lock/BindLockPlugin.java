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
 * 绑定电子锁上下盖
 */
public class BindLockPlugin extends BasePlugin {
    private final static String TAG = "BindLockPlugin";

    /**
     * 绑定电子锁上下盖
     *
     * @param context   上下文环境
     * @param topTag    上盖标签 LockTagBean
     * @param bottomTag 下盖标签 LockTagBean
     * @param key       高德地图Key
     * @param EToken    token
     * @return
     */
    @DoNotRename
    public static ResultBean bindLock(Context context, LockTagBean topTag, LockTagBean bottomTag, String key, String EToken) {
        mContext = context;
        sResultBean = new ResultBean();
        mHeader = new HashMap<>();
        mParams = new HashMap<>();

        SharedPreferences sharedPreferences = initResourceSP(context);

        OkHttp okHttp = new OkHttp(context);
        //上传上盖下盖Uid进行电子锁上盖下盖绑定,rasId:上盖标签Uid,subRasId:下盖标签Uid。

        //post方法body

        initParams(topTag, bottomTag);

        Gson gson = new Gson();
        String jsonData = gson.toJson(mParams);
        LogUtil.i(TAG, "json数据 --> " + jsonData);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
//        LogUtil.i(TAG, "requestBody --> " + requestBody.toString());

        //向服务器发起绑定电子锁上下盖请求
        okHttp.postFromInternet(Constant.LOCK_BIND_URL, requestBody, initHeader(EToken));

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

        //返回随机数绑定成功
        sResultBean.setErrno(TagErrorEnum.BIND_LOCK_TAG_SUCCESS.getCode());

        return sResultBean;
    }

    private static void initParams(LockTagBean topTag, LockTagBean bottomTag) {
        mParams.put("goodsSn", "333");
        mParams.put(Constant.RAS_ID, topTag.getRasId());
        mParams.put(Constant.PAD, topTag.getRasPad());
        mParams.put(Constant.SUB_RAS_ID, bottomTag.getRasId());
        mParams.put(Constant.SUB_PAD, bottomTag.getRasPad());
    }
}
