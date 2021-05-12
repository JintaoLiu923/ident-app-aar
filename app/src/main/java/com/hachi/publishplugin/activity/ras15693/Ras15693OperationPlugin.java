package com.hachi.publishplugin.activity.ras15693;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.ApiNfcTag;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.bean.TbRasCfg;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Ras15693OperationPlugin extends BasePlugin {
    private static final String TAG = "Ras15693OperationPlugin";
    private static TagBean mTagBean;
    private static String newPswd = "";
    private static String config_password = "";
    private final NfcVTmp mNfcv;
    private final String mRandomStr;
    private static String mToken;
    private static String mNewPassword;

    public Ras15693OperationPlugin(Context context, Tag tag, String EToken, String newPassword) {
        mContext = context;
        mNfcv = new NfcVTmp(tag);
        //读取UID
        uid = mNfcv.getUID();
        byte[] random = mNfcv.getRandom();
        mRandomStr = OperationUtil.bytesToHexString(random);
        mToken = EToken;
        mNewPassword = newPassword;

        ResultBean resultBean = getTagDetail();
        LogUtil.d(TAG, "result --> " + resultBean);
        TagBean tagBean = resultBean.getTagBean();
        initData(tagBean);
    }

    public ResultBean getTagDetail() {

        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(mContext, null, mToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            mNfcv.close();
            return sResultBean;
        }
        sResultBean.setUid(uid);

        getDetailRequest(uid, mRandomStr, mNewPassword, mToken, new DetailListener() {
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

        sResultBean.setTagBean(mTagBean);
        return sResultBean;
    }

    public ResultBean verifyPassword(String EToken, String newPassword) {
        ResultBean resultBean = new ResultBean();

        byte[] passwordByte = EncryptUtils.hexString2Bytes(config_password);
        boolean verifyPwd = mNfcv.verifyPwd(passwordByte);
        if (!verifyPwd) {
            LogUtil.d(TAG, "密码验证失败!");
            return resultBean;
        }
        return resultBean;
    }

    public ResultBean changePassword(String EToken, String newPassword) {
        if (mNfcv == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }

        if (!TextUtils.isEmpty(newPassword)) {
            TbRasCfg tagCfg = new TbRasCfg();
            tagCfg.setPassword(newPassword);

            tagCfg.setUid(uid);

            ApiNfcTag apiNfcTag = new ApiNfcTag();

            //新证书校验
            apiNfcTag.setTagCfg(tagCfg);
            LogUtil.d(TAG, "log-->" + JSON.toJSONString(apiNfcTag));
//        byte[] bytesPwd = OperationUtil.stringToBytes(randomPwd, 8);
//        nfc.setPWD(bytesPwd);
            Gson gson = new Gson();
            String jsonData = gson.toJson(apiNfcTag);
//        LogUtil.i(TAG, "json数据 --> " + jsonData);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);


            mHeader = initHeader(EToken);
            mHeader.put(Constant.COOKIE, "JSESSIONID =" + EToken);

            postRequest(Constant.CFG_SAVE_URL, requestBody, mHeader, new RequestListener() {
                @Override
                public void requestFail() {
                    sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                }

                @Override
                public void error(ResultBean tagBean) {
                    sResultBean.setErrno(tagBean.getErrno());
                    sResultBean.setErrmsg(tagBean.getErrmsg());
                }

                @Override
                public void success(ResultBean tagBean) {

                }
            });

            if (!TextUtils.isEmpty(newPswd)) {
                byte[] randomHexPwd = EncryptUtils.hexString2Bytes(newPswd);
                //
                if (!mNfcv.setPWD(randomHexPwd)) {
                    mNfcv.close();
                    PutLogUtil.putResult(mContext, uid, EToken, ActTypeEnum.ACT_TYPE_写密码, Constant.FAIL);
                    sResultBean.setErrno(TagErrorEnum.WRITE_PASSWORD_FAIL.getCode());
                    return sResultBean;
                }
            }
        }
        return sResultBean;
    }

    public ResultBean changeRwKey(String EToken, String newPassword) {
        if (mNfcv == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }

        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(mContext, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            mNfcv.close();
            return sResultBean;
        }
        sResultBean.setUid(uid);

        if (sResultBean.getErrno() != 0) {
            mNfcv.close();
            return sResultBean;
        }

        //密码认证
        byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

        if (!TextUtils.isEmpty(newPassword)) {
            TbRasCfg tagCfg = new TbRasCfg();
            tagCfg.setPassword(newPassword);

            tagCfg.setUid(uid);

            ApiNfcTag apiNfcTag = new ApiNfcTag();

            //新证书校验
            apiNfcTag.setTagCfg(tagCfg);
            LogUtil.d(TAG, "log-->" + JSON.toJSONString(apiNfcTag));
//        byte[] bytesPwd = OperationUtil.stringToBytes(randomPwd, 8);
//        nfc.setPWD(bytesPwd);
            Gson gson = new Gson();
            String jsonData = gson.toJson(apiNfcTag);
//        LogUtil.i(TAG, "json数据 --> " + jsonData);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);


            mHeader = initHeader(EToken);
            mHeader.put(Constant.COOKIE, "JSESSIONID =" + EToken);

            postRequest(Constant.CFG_SAVE_URL, requestBody, mHeader, new RequestListener() {
                @Override
                public void requestFail() {
                    sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                }

                @Override
                public void error(ResultBean tagBean) {
                    sResultBean.setErrno(tagBean.getErrno());
                    sResultBean.setErrmsg(tagBean.getErrmsg());
                }

                @Override
                public void success(ResultBean tagBean) {

                }
            });

            if (!TextUtils.isEmpty(newPswd)) {
                byte[] randomHexPwd = EncryptUtils.hexString2Bytes(newPswd);
                //
                if (!mNfcv.setPWD(randomHexPwd)) {
                    mNfcv.close();
                    PutLogUtil.putResult(mContext, uid, EToken, ActTypeEnum.ACT_TYPE_写密码, Constant.FAIL);
                    sResultBean.setErrno(TagErrorEnum.WRITE_PASSWORD_FAIL.getCode());
                    return sResultBean;
                }
            }
        }
        return sResultBean;
    }

    private static void initData(TagBean tagBean) {
        newPswd = tagBean.getData().getNewPsw();
        config_password = tagBean.getData().getCfg().getPassword();
    }
}
