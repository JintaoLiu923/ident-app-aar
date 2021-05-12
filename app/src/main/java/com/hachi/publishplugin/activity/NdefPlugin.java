package com.hachi.publishplugin.activity;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.hachi.publishplugin.R;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.activity.ras14443.Ras14443NdefPlugin;
import com.hachi.publishplugin.activity.ras15693.Ras15693NdefPlugin;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.PermissionUtils;
import com.hachi.publishplugin.utils.PutLogUtil;


/**
 * NDEF自定义组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class NdefPlugin extends BasePlugin4Tag {
    private static final String TAG = "NdefPlugin";

    @DoNotRename
    public static ResultBean rasTagWriteNdef(Context context, Tag tag, String key, String EToken, String mobile, String password, String ndef, boolean isStr) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始NDEF自定义流程");
        //检查定位权限
        boolean locationFlag = PermissionUtils.checkLocationPermission(context);
        if (!locationFlag) {
            return sResultBean;
        }

        initLocation(context, key);

        //鉴权
        if (!checkLogin(EToken, mobile, password)) {
            return sResultBean;
        }
        //判断标签类型
        sIsNfcA = false;
        sIsNfcV = false;
        initTechType(tag);

        //读取UID
        if (sIsNfcA) {
            uid = GlobelRasFunc.readUid14443(tag);
            String sUid = uid.substring(0, 2);
            if (sUid.equals("53")) {
                uid = GlobelRasFunc.readUidF8213(tag);
            }
        } else {
            uid = GlobelRasFunc.readUid15693(tag);
        }
        if (TextUtils.isEmpty(uid)) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            return sResultBean;
        }
        uid = uid.toUpperCase();
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);

        if (sIsNfcV) {
            sResultBean = Ras15693NdefPlugin.writeNdef(context, tag, EToken, ndef, isStr);
        } else {
//            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
            sResultBean = Ras14443NdefPlugin.rasTagWriteNdef(context, tag, key, EToken, mobile, password, ndef, isStr);
        }

        //mlocationClient.stopLocation();
        sActTypeEnum = ActTypeEnum.ACT_TYPE_写NDEF;
        //设置定位监听
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }
        GlobelRasFunc.musicPlay(context, 1);
        return sResultBean;
    }

    @DoNotRename
    public static ResultBean rasTagReadNdef(Context context, Tag tag, String key, String EToken, String mobile, String password) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, context.getString(R.string.text_startNDEFRead));
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
        //判断标签类型
        sIsNfcA = false;
        sIsNfcV = false;
        //判断标签类型，为NFCA或者NFCV
        initTechType(tag);

        //读取UID
        if (sIsNfcA) {
            uid = GlobelRasFunc.readUid14443(tag);
            String sUid = uid.substring(0, 2);
            if (sUid.equals("53")) {
                uid = GlobelRasFunc.readUidF8213(tag);
            }
        } else {
            uid = GlobelRasFunc.readUid15693(tag);
        }
        if (TextUtils.isEmpty(uid)) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            return sResultBean;
        }
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);

        if (sIsNfcV) {
            sResultBean = Ras15693NdefPlugin.readNdef(context, tag, EToken);
        } else {
            //不支持
//            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
            sResultBean = Ras14443NdefPlugin.rasTagReadNdef(context, tag, key, EToken, mobile, password);
            return sResultBean;
        }
        //初始化定位

        sActTypeEnum = ActTypeEnum.ACT_TYPE_读NDEF;
        //设置定位监听
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }
        GlobelRasFunc.musicPlay(context, 1);
        return sResultBean;
    }

}
