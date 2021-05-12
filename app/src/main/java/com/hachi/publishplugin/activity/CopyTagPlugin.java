package com.hachi.publishplugin.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.nfc.Tag;
import android.os.Build;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.activity.ras15693.rw.Ras15693ReadTagPlugin;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.JudgeTagTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.enums.channel.CopyChannelEnum;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 发证组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class CopyTagPlugin extends BasePlugin4Tag {

    private static final String TAG = "CertPlugin";

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @DoNotRename
    public static ResultBean copy(Context context, Tag tag, String key, String EToken) {
        mContext = context;
        sResultBean = new ResultBean();
        mRasToken = EToken;

        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }
        //初始化定位
        initLocation(context, key);

        sIsNfcA = false;
        sIsNfcV = false;

        //判断标签类型
        initTechType(tag);
        if (sIsNfcA) {
            //获取到Uid判断是否为空
            if (!checkNfcAUid(tag)) {
                return sResultBean;
            }
            String sUid = uid.substring(0, 2);
            String channelName = JudgeTagTypeEnum.match(sUid);
            CopyChannelEnum matchChannel = CopyChannelEnum.match(channelName);
            matchChannel.mICopyService.copy(context, tag, key, EToken);
        } else if (sIsNfcV) {
            sResultBean = Ras15693ReadTagPlugin.readTag(context, tag, key, mRasToken, 0, 129);
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        }

        sActTypeEnum = ActTypeEnum.ACT_TYPE_写NDEF;

        //设置定位监听器
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }

        if (sResultBean.getErrno() == 202) {
            GlobelRasFunc.musicPlay(context, 1);
        }

        return sResultBean;
    }

    private static void sendLog(IdentLogReqBean log, Context context, ResultBean resultBean) {
        if (log != null) {
            OkHttp okHttp = new OkHttp(context);
            log.getLog().setRasId(resultBean.getTime());
            Gson gson = new Gson();
            String jsonData = gson.toJson(log);
            LogUtil.i(TAG, "json数据 --> " + jsonData);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
            mHeader = new HashMap<>();
            mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
            mHeader.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
            okHttp.sendLog(Constant.LOG_ADD_URL, requestBody, mHeader);
        }
    }
}
