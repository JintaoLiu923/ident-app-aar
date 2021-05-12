package com.hachi.publishplugin.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.JudgeTagTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;

import static android.content.Context.MODE_PRIVATE;

public class TagConfigPlugin extends BasePlugin4Tag {

    private static final String TAG = "ClearCertPlugin";

    @SneakyThrows
    public static ResultBean getConfig(Context context, Tag tag, String EToken) {
        mContext = context;
        sResultBean = new ResultBean();
        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }
        //初始化定位
        initLocation(context, GlobelRasFunc.mMapKey);

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
                sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
                return sResultBean;
            }

            uid = nfc.getUID();
            nfc.close();
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        }

        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, uid, mRasToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }

        Map<String, String> header = new HashMap<>();
        header.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
        header.put("Cookie", "JSESSIONID =" + EToken);
        header.put("IDent-Admin-Token", EToken);
        header.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
        LogUtil.d(TAG, "header --> " + header.toString());

        OkHttp okHttp = new OkHttp(mContext);

        okHttp.getFromInternet(Constant.ADMIN_CFG_DETAIL + "?uid=" + uid, header);

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);

        if (!flag) {
            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return sResultBean;
        }

        String responseData = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "查询数据 --> " + responseData);
        sResultBean.setTagConfig(responseData);

        sResultBean.setErrno(TagErrorEnum.GET_CFG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.GET_CFG_SUCCESS.getDescription());

        GlobelRasFunc.musicPlay(context, 1);
        return sResultBean;
    }
}
