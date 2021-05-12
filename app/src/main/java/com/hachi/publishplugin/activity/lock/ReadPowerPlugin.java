package com.hachi.publishplugin.activity.lock;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.activity.rasF8023.RasF8023VailPlugin;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;


public class ReadPowerPlugin extends BasePlugin4Tag {
    private static final String TAG = "ReadPowerPlugin";

    @DoNotRename
    public static ResultBean readPower(Context context, Tag tag, String key, String EToken, String userName) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始读电量流程");

        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }

        //初始化定位
        initLocation(context, key);

        //鉴权
        if (!checkLogin(EToken, Constant.username, Constant.password)) {
            return sResultBean;
        }

        //判断标签类型
        sIsNfcA = false;
        sIsNfcV = false;
        //判断标签类型，为NFCA或者NFCV
        initTechType(tag);
        if (sIsNfcA) {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
            return sResultBean;
        } else if (sIsNfcV) {
            NfcVTmp nfc = new NfcVTmp(tag);
            if (nfc == null) {
                sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
                return sResultBean;
            }

            uid = nfc.getUID();
            if (TextUtils.isEmpty(uid)) {
                PutLogUtil.putResult(context, uid, mRasToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
                nfc.close();
                return sResultBean;
            }

            //获取随机数
            byte[] random = nfc.getRandom();

            if (random == null || random.length == 0) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
                return sResultBean;
            }

            String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
            LogUtil.i(TAG, "随机数 --> " + randomStr);

            LogUtil.i(TAG, "标签id --> " + uid);
            sResultBean.setUid(uid);

            nfc.close();

            //获取配置信息
            getDetailRequest(uid, randomStr, null, EToken, new DetailListener() {
                @Override
                public void error(TagBean tagBean) {
                    sResultBean.setErrno(tagBean.getErrno());
                    sResultBean.setErrmsg(tagBean.getErrmsg());
                }

                @Override
                public void success(TagBean tagBean) {
                    //解析数据
                    mTagBean = tagBean;
                }
            });

            if (sResultBean.getErrno() != 0) {
                nfc.close();
                return sResultBean;
            }
            tagType = mTagBean.getData().getTagType();

            if (tagType == 3) {
                sResultBean = new RasF8023VailPlugin().vali(context, tag, mTagBean, key, mRasToken, userName);
            } else {
                sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
            }
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
            return sResultBean;
        }

        sActTypeEnum = ActTypeEnum.ACT_TYPE_读锁状态;
        //设置定位监听
        if (mlocationClient != null) {

            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }

        return sResultBean;
    }
}
