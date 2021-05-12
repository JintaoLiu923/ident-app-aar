package com.hachi.publishplugin.activity.tracing;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.ValiPlugin;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.DataBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class TracingSourcePlugin extends BasePlugin4Tag {
    private static final String TAG = "getTracingSource";
    private static Integer mTagType;
    private static Map<String, String> mHeader = new HashMap<>();

    @DoNotRename
    public static ResultBean getTracingSource(Context context, Tag tag, String key, String EToken, String mobile, String password) throws InterruptedException {
        ResultBean result = ValiPlugin.rasTagVerify(context, tag, GlobelRasFunc.mMapKey, EToken, mobile, password);
        OkHttp okHttp = new OkHttp(context);
        if (result != null) {
            if (result.getData() != null && result.getData().getTagType() != null) {
                mTagType = result.getData().getTagType();
            }
            String rasId = "";
            if (!TextUtils.isEmpty(result.getTime())) {
                rasId = result.getTime();
                mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);

                SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.clear();
                edit.apply();

                //查询溯源日志
                okHttp.getFromInternetById(Constant.LOG_LIST + "?rasId=" + rasId);

                String responseData = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
                boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);

                //若flag为false，表示未获取到数据，直接返回错误
                if (!flag) {
                    sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                    return sResultBean;
                }

                LogUtil.i(TAG, "查询数据:" + responseData);

                DataBean dataBean = new DataBean();
                dataBean.setTagType(tagType);
                sResultBean.setTime(rasId);
                sResultBean.setData(dataBean);
//                sResultBean.setLogData(logBean.getData());
                sResultBean.setLogData(responseData);
            } else {
                sResultBean.setErrno(result.getErrno());
                sResultBean.setErrmsg(result.getErrmsg());
            }
        }
        return sResultBean;
    }

    /**
     * 根据uid获取溯源日志
     *
     * @param context 上下文
     * @param rasId   时间戳
     * @param key     高德地图mapkey
     * @param EToken
     * @return
     * @throws InterruptedException
     */

    @DoNotRename
    public static ResultBean getTracingSourceByRasId(Context context, String rasId, String key, String EToken) throws InterruptedException {
        ResultBean result = new ResultBean();
        OkHttp okHttp = new OkHttp(context);
        if (!TextUtils.isEmpty(rasId)) {
            if (result.getData() != null && result.getData().getTagType() != null) {
                mTagType = result.getData().getTagType();
            }
            mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);

            SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.clear();
            edit.apply();

            //查询溯源日志
            okHttp.getFromInternetById(Constant.LOG_LIST + "?rasId=" + rasId);

            String responseData = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
            boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);

            //若flag为false，表示未获取到数据，直接返回错误
            if (!flag) {
                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                return sResultBean;
            }

            LogUtil.i(TAG, "查询数据 --> " + responseData);

            DataBean dataBean = new DataBean();
            dataBean.setTagType(tagType);
            sResultBean.setTime(rasId);
            sResultBean.setData(dataBean);
//                sResultBean.setLogData(logBean.getData());
            sResultBean.setLogData(responseData);
        } else {
            sResultBean.setErrno(result.getErrno());
            sResultBean.setErrmsg(result.getErrmsg());
        }

        return sResultBean;
    }
}