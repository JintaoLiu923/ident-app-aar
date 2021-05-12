package com.hachi.publishplugin.activity;

import android.content.Context;
import android.nfc.Tag;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.activity.ras15693.Ras15693ConfigTmpPlugin;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.bean.TempConfigBean;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.LogUtil;


/**
 * 配置温控组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class ConfigTmpPlugin extends BasePlugin {
    private static final String TAG = "ConfigTmpPlugin";

    @DoNotRename
    public static ResultBean rasTagCfgTemp(Context context, Tag tag, String key, String EToken,
                                           String mobile, String password, TempConfigBean tempConfigBean) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始配置流程");
        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }
        //初始化定位
        initLocation(context, key);

        //鉴权
        if (!checkLogin(EToken, mobile, password)) {
            return sResultBean;
        }

        /*---------------------------------温控-----------------------------------*/
        sResultBean = Ras15693ConfigTmpPlugin.tempConfig(context, tag, mRasToken, tempConfigBean);

        sActTypeEnum = ActTypeEnum.ACT_TYPE_写温控参数;
        //设置监听器
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }
        GlobelRasFunc.musicPlay(context, 1);
        return sResultBean;
    }


    @DoNotRename
    public static ResultBean rasTagOpenTempLog(Context context, Tag tag, String key,
                                               String EToken, String mobile, String password, boolean open) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "停止测温流程");
        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }
        //初始化定位
        initLocation(context, key);
        //鉴权
        if (!checkLogin(EToken, mobile, password)) {
            return sResultBean;
        }
        /*---------------------------------温控-----------------------------------*/
        sResultBean = Ras15693ConfigTmpPlugin.startOrStop15693Temp(context, tag, EToken, open);
        //初始化定位

        sActTypeEnum = ActTypeEnum.ACT_TYPE_开启或停止温控;
        //设置定位监听器
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }
        GlobelRasFunc.musicPlay(context, 1);
        return sResultBean;
    }

    public static ResultBean rasTagReadTemps(Context context, Tag tag, String key, String EToken, String mobile, String password, IdentLogReqBean log) {
        return rasTagReadTemps(context, tag, key, EToken, mobile, password, log, null);
    }

    @DoNotRename
    public static ResultBean rasTagReadTemps(Context context, Tag tag, String key, String EToken, String mobile, String password, IdentLogReqBean log, TagBean tagBean) {
        mContext = context;
        LogUtil.i(TAG, "开始读取温控流程");

        if (log == null || log.getTemp() == null) {
            sResultBean.setErrno(TagErrorEnum.INVALID_PARAM.getCode());
            return sResultBean;
        }

        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }

        //初始化定位
        initLocation(context, key);

        //鉴权
        if (!checkLogin(EToken, mobile, password)) {
            return sResultBean;
        }

        /*---------------------------------温控-----------------------------------*/
        sResultBean = Ras15693ConfigTmpPlugin.readTemps(context, tag, EToken, log.getTemp(), tagBean);

        uid = log.getTemp().getUid();

        //设置定位监听
        ResultBean finalResultBean = sResultBean;
        sActTypeEnum = ActTypeEnum.ACT_TYPE_读取温度列表;

//        log.getLog().setCreateBy(1);
//        log.getLog().setCreateByName(mobile);
//        log.getTemp().setCreateBy(1);
//        log.getTemp().setCreateByName(mobile);
        log.getLog().setRasId(log.getTemp().getRasId());
        log.getLog().setAction(ActTypeEnum.ACT_TYPE_读取温度列表.getName());
        log.getLog().setResult(finalResultBean.getErrno() + "");
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }
        log.getLog().setLat(GlobelRasFunc.latitude);
        log.getLog().setLng(GlobelRasFunc.longitude);
//        log.getLog().setComment("{\"content\":null,\"master\":" + mobile + ",\"name\":\"\",\"sn\":null,\"type\":6}");
//        log.getLog().setComment("{\"content\":null,\"master\":\"" + mobile + "\",\"name\":\"\",\"sn\":null,\"type\":6}");
        GlobelRasFunc.musicPlay(context, 1);

        return sResultBean;
    }
}
