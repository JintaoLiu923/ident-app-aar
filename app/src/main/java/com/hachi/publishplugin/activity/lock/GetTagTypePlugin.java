package com.hachi.publishplugin.activity.lock;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.DataBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

import static android.content.Context.MODE_PRIVATE;

public class GetTagTypePlugin extends BasePlugin4Tag {
    private static final String TAG = "GetTagTypePlugin";

    @DoNotRename
    public static ResultBean getTagType(Context context, Tag tag, String key, String EToken) {
        mContext = context;
        sResultBean = new ResultBean();
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
                sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
                return sResultBean;
            }

            uid = nfc.getUID();
            if (TextUtils.isEmpty(uid)) {
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
                nfc.close();
                return sResultBean;
            }

            LogUtil.i(TAG, "标签id --> " + uid);
            sResultBean.setUid(uid);

            SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.clear();
            edit.apply();

            nfc.close();

            getDetailRequest(uid, null, null, EToken, new DetailListener() {
                @Override
                public void error(TagBean tagBean) {
                    sResultBean.setErrno(tagBean.getErrno());
                    sResultBean.setErrmsg(tagBean.getErrmsg());
                }

                @Override
                public void success(TagBean tagBean) {
                    tagType = tagBean.getData().getTagType();
                }
            });

            if (sResultBean.getErrno() != 0) {
                return sResultBean;
            }

            DataBean dataBean = new DataBean();
            dataBean.setTagType(tagType);
            sResultBean.setData(dataBean);
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        }
        sActTypeEnum = ActTypeEnum.ACT_TYPE_业务操作;
        //设置定位监听
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }

        GlobelRasFunc.musicPlay(context, 1);
        return sResultBean;
    }
}
