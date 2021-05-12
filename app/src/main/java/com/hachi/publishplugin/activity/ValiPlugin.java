package com.hachi.publishplugin.activity;

import android.content.Context;
import android.nfc.Tag;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.activity.base.TagNfcvPlugin;
import com.hachi.publishplugin.activity.ras15693.Ras15693ValiPlugin;
import com.hachi.publishplugin.activity.rasF8023.RasF8023VailPlugin;
import com.hachi.publishplugin.bean.DataBean;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.JudgeTagTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.enums.channel.VailChannelEnum;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;


/**
 * 验证组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class ValiPlugin extends BasePlugin4Tag {
    private static final String TAG = "ValiPlugin";

    @DoNotRename
    public static ResultBean rasTagVerify(Context context, Tag tag, String key, String EToken, String mobile, String password) throws InterruptedException {
        return rasTagVerify(context, tag, key, EToken, mobile, password, null);
    }

    @DoNotRename
    public static ResultBean rasTagVerify(Context context, Tag tag, String key, String EToken, String mobile, String password, IdentLogReqBean log) throws InterruptedException {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始验证流程");
        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }
        //初始化定位
        initLocation(context, GlobelRasFunc.mMapKey);

        //鉴权
        if (!checkLogin(EToken, mobile, password)) {
            return sResultBean;
        }

        //判断标签类型
        sIsNfcA = false;
        sIsNfcV = false;
        //判断标签类型，为NFCA或者NFCV
        initTechType(tag);
        tagType = 0;
        if (sIsNfcA) {
            //获取标签uid，判断是否为空
            if (!checkNfcAUid(tag)) {
                return sResultBean;
            }
            String sUid = uid.substring(0, 2);
            String channelName = JudgeTagTypeEnum.match(sUid);
            VailChannelEnum matchChannel = VailChannelEnum.match(channelName);
            if (matchChannel != null) {
                sResultBean = matchChannel.mValiService.vali(context, tag, key, mRasToken, null);
                if (log != null) {
                    log.getLog().setAction(ActTypeEnum.ACT_TYPE_物流操作_已签收.getName());
                    log.getLog().setLat(GlobelRasFunc.latitude);
                    log.getLog().setLng(GlobelRasFunc.longitude);
                    sendLog(log, context, sResultBean);
                }
            }
        } else if (sIsNfcV) {
            TagNfcvPlugin tagNfcv = new TagNfcvPlugin(tag, mContext, EToken, false, null, new TagNfcvPlugin.TagNfcvListener() {
                @Override
                public void error(ResultBean resultBean) {
                    sResultBean = resultBean;
                    LogUtil.d(TAG, "error...");
                }

                @Override
                public void success(ResultBean resultBean) {
                    sResultBean = resultBean;
                    mTagBean = sResultBean.getTagBean();
                    LogUtil.d(TAG, "success...");
                }
            });

            if (sResultBean.getErrno() != 0) {
                return sResultBean;
            }

            tagNfcv.mNfc.close();
            tagType = mTagBean.getData().getTagType();

            if (tagType == 3) {
                sResultBean = new RasF8023VailPlugin().vali(context, tag, mTagBean, key, mRasToken, mobile);
            } else {
                sResultBean = new Ras15693ValiPlugin().vali(context, tag, mTagBean, key, mRasToken, mobile, password, log);
            }

            if (log != null) {
                log.getLog().setAction(ActTypeEnum.ACT_TYPE_物流操作_已签收.getName());
                log.getLog().setLat(GlobelRasFunc.latitude);
                log.getLog().setLng(GlobelRasFunc.longitude);
                sendLog(log, context, sResultBean);
            }
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        }

        DataBean dataBean = new DataBean();
        dataBean.setTagType(tagType);
        sResultBean.setData(dataBean);

        sActTypeEnum = ActTypeEnum.ACT_TYPE_写证书;
        //设置定位监听
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }
        GlobelRasFunc.musicPlay(context, 1);
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
