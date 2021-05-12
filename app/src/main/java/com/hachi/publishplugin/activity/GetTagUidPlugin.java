package com.hachi.publishplugin.activity;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.enums.JudgeTagTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.NfcVTmp;

public class GetTagUidPlugin extends BasePlugin4Tag {
    public static ResultBean getUid(Context context, Tag tag) {
        mContext = context;
        sResultBean = new ResultBean();
        //检查定位权限
        if (!checkLocation()) {
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
            if (channelName.equals("14443")) {
                //读取标签Uid
                uid = GlobelRasFunc.readUid14443(tag);
            } else if (channelName.equals("F8213")) {
                //读取UID
                uid = GlobelRasFunc.readUidF8213(tag);
            }

        } else if (sIsNfcV) {
            NfcVTmp nfc = new NfcVTmp(tag);
            if (nfc == null) {
                nfc.close();
                sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
                return sResultBean;
            }

            uid = nfc.getUID();

            nfc.close();
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        }

        if (TextUtils.isEmpty(uid)) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }

        sResultBean.setUid(uid);
        return sResultBean;
    }
}
