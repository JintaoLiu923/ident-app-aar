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
import com.hachi.publishplugin.activity.base.TagNfcvPlugin;
import com.hachi.publishplugin.activity.ras15693.Ras15693CertPlugin;
import com.hachi.publishplugin.activity.rasF8023.RasF8023CertPlugin;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.JudgeTagTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.enums.channel.CertChannelEnum;
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
public class CertPlugin extends BasePlugin4Tag {

    private static final String TAG = "CertPlugin";


    //    private static String certEncrypt = "";//密文证书
//    private static String certDecode = "";
//    private static String ndef = "";//密文NDEF
//    private static Boolean isPswTag = false;//标签类型，false为RAS，true为14443或15693
//    private static String config_rk = "";
//    private static String config_wk = "";
//    private static String config_itsp = "";
//    private static int config_intsel;
//    private static String config_password = "";
//    private static String config_rasId = "";
//    private static String config_rasIdEncrypt = "";
//    private static String newCert = "";//明文证书
//    private static Boolean hasCert = false;
//    private static int err_num = 0;

    public static ResultBean rasTagActive(Context context, Tag tag, String key, String EToken, String mobile, String password) {
        return rasTagActive(context, tag, key, EToken, mobile, password, null, null, null);
    }

    public static ResultBean rasTagActive(Context context, Tag tag, String key, String EToken, String mobile, String password, IdentLogReqBean log) {
        return rasTagActive(context, tag, key, EToken, mobile, password, null, null, log);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @DoNotRename
    public static ResultBean rasTagActive(Context context, Tag tag, String key, String EToken, String mobile, String password, String newPassword, Boolean isSavePswd, IdentLogReqBean log) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始发证流程");

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
            CertChannelEnum matchChannel = CertChannelEnum.match(channelName);

            if (matchChannel != null) {
                sResultBean = matchChannel.mICertService.cert(context, tag, key, mRasToken, mobile, password, newPassword);
                if (log != null) {
                    log.getLog().setAction(ActTypeEnum.ACT_TYPE_发证操作.getName());
                    log.getLog().setLat(GlobelRasFunc.latitude);
                    log.getLog().setLng(GlobelRasFunc.longitude);
                    sendLog(log, context, sResultBean);
                }
            }
        } else if (sIsNfcV) {
            TagNfcvPlugin tagNfcv = new TagNfcvPlugin(tag, mContext, EToken, false,newPassword ,new TagNfcvPlugin.TagNfcvListener() {
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

            //tagType为3表示标签类型为F8023
            if (tagType == 3) {
                sResultBean = new RasF8023CertPlugin().cert(context, tag, mTagBean, key, mRasToken, mobile, password, newPassword);
            } else {
                sResultBean = new Ras15693CertPlugin().cert(context, tag, mTagBean, key, mRasToken, mobile, password, newPassword, true);
            }

            if (log != null) {
                log.getLog().setAction(ActTypeEnum.ACT_TYPE_发证操作.getName());
                log.getLog().setLat(GlobelRasFunc.latitude);
                log.getLog().setLng(GlobelRasFunc.longitude);
                sendLog(log, context, sResultBean);
            }
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
