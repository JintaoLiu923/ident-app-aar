package com.hachi.publishplugin.activity.lock;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.activity.lock.test.RasF8023VailTestPlugin;
import com.hachi.publishplugin.activity.ras15693.Ras15693ValiPlugin;
import com.hachi.publishplugin.bean.DataBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

import lombok.SneakyThrows;


/**
 * 验证组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class LockValiPlugin extends BasePlugin4Tag {
    private static final String TAG = "LockValiPlugin";

    @SneakyThrows
    @DoNotRename
    public static ResultBean lockTagVali(Context context, Tag tag, String key, String EToken, String userName) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始验证流程");

        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }

        //初始化定位
        initLocation(context, key);

        //判断标签类型
        sIsNfcA = false;
        sIsNfcV = false;

        //判断标签类型，为NFCA或者NFCV

        initTechType(tag);

        if (sIsNfcA) {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        } else if (sIsNfcV) {

            NfcVTmp nfc = new NfcVTmp(tag);

            if (nfc == null) {
                nfc.close();
                sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
                return sResultBean;
            }

            uid = nfc.getUID();
            if (TextUtils.isEmpty(uid)) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
                return sResultBean;
            }

            LogUtil.i(TAG, "标签id --> " + uid);

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

            sResultBean.setUid(uid);

            nfc.close();

            getDetailRequest(uid, randomStr, null, EToken, new DetailListener() {
                @Override
                public void error(TagBean tagBean) {
                    sResultBean.setErrno(tagBean.getErrno());
                    sResultBean.setErrmsg(tagBean.getErrmsg());
                }

                @Override
                public void success(TagBean tagBean) {
                    mTagBean = tagBean;
                }
            });

            if (sResultBean.getErrno() != 0) {
                nfc.close();
                return sResultBean;
            }

            tagType = mTagBean.getData().getTagType();

            if (tagType == 3) {
//                sResultBean = new RasF8023VailPlugin().vali(context, tag, mTagBean, key, EToken, userName);
                sResultBean.setTime(mTagBean.getData().getRasId());
                sResultBean = new RasF8023VailTestPlugin().vali(context, tag, key, EToken, userName);
//                sResultBean
//            } else if (tagType == 1 || tagType == 0) {
            } else {
                sResultBean = new Ras15693ValiPlugin().vali(context, tag, mTagBean, key, EToken, userName, null, null);
            }

//            else {
//                sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
//            }

            DataBean dataBean = new DataBean();
            dataBean.setTagType(tagType);
            sResultBean.setData(dataBean);
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        }

//        sActTypeEnum = ActTypeEnum.ACT_TYPE_写NDEF;
//        //设置定位监听
//        mlocationClient.setLocationListener(mLocationListener);
//        mlocationClient.startLocation();
        return sResultBean;
    }

}
