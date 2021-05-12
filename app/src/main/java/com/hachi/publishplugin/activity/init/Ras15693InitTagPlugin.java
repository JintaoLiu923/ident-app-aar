package com.hachi.publishplugin.activity.init;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.ApiNfcTag;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.bean.TbRasCfg;
import com.hachi.publishplugin.bean.TbRasCfgNokey;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.InterpolationUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * F8023 电子锁下盖 标签入库
 */
public class Ras15693InitTagPlugin extends BasePlugin4Tag {
    private ApiNfcTag mApiNfcTag;
    private TbRasCfgNokey mTbRasCfgNokey;
    private TbRasCfg mTagCfg;
    private static final String TAG = "Ras15693PutStoragePlugin";
    protected Map<String, String> mHeader = new HashMap<>();
    private static byte readKey = 0x00, writeKe = 0x00, customItsp = 0x40, customIntSel = 0x00; //可设定的参数值
    byte[] rdmITSP = new byte[]{0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40};
    private byte[] mNewPassword;

    public ResultBean initTag(Context context, Tag tag, String key, String EToken, int tagType, int batchId) {
        mContext = context;
        sResultBean = new ResultBean();
        mApiNfcTag = new ApiNfcTag();
        mTbRasCfgNokey = new TbRasCfgNokey();
        mTagCfg = new TbRasCfg();

        //读取UID
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
        mTbRasCfgNokey.setUid(uid);
        sResultBean.setUid(uid);

        //获取随机数
        byte[] random = nfc.getRandom();
        if (random == null || random.length == 0) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            return sResultBean;
        }

        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.i(TAG, "random --> " + randomStr);

        SharedPreferences sharedPreferences = initResourceSP(mContext);

        //获取配置信息
        OkHttp okHttp = new OkHttp(context);
        okHttp.getFromInternetById(Constant.DETAIL_URL + "?uid=" + uid + "&random=" + randomStr + "&password=0000", EToken);

        boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);

        //若flag为false，表示未获取到数据，直接返回错误
        if (!flag) {
            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            nfc.close();
            return sResultBean;
        }

        String responseData = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "查询数据 --> " + responseData);
        TagBean tagBean = new ParseJson().Json2Bean(responseData, TagBean.class);

        if (tagBean.getErrmsg().equals("no db") || tagBean.getData() == null || tagBean.getData().getCfg() == null || TextUtils.isEmpty(tagBean.getData().getCfg().getPassword())) {
            byte[] pwd = InterpolationUtils.getVerifyPwd(new byte[]{0x00, 0x00}, random, customIntSel, customItsp);
            LogUtil.d(TAG, "pwd --> " + OperationUtil.bytesToHexString(pwd));
            if (!nfc.verifyPwd(pwd)) {
                LogUtil.d(TAG, "密码验证失败...");
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                return sResultBean;
            }
            LogUtil.d(TAG, "密码验证成功...");

            mNewPassword = new byte[]{0x00, 0x00, 0x00, 0x00};
        } else {
            byte[] pwd = EncryptUtils.hexString2Bytes(tagBean.getData().getCfg().getPassword());

            LogUtil.i(TAG, "温控-->随机数:" + Arrays.toString(random));

            //验证密码，若验证不通过，则直接返回错误
            if (!nfc.verifyPwd(pwd)) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                return sResultBean;
            }

            mNewPassword = EncryptUtils.hexString2Bytes(tagBean.getData().getNewPsw());
        }

        String newPsw = OperationUtil.bytesToHexString(mNewPassword);

        if (!TextUtils.isEmpty(newPsw)) {
            //写密码
            byte[] randomHexPwd = EncryptUtils.hexString2Bytes(newPsw);
            //
            if (!nfc.setPWD(randomHexPwd)) {
                LogUtil.d(TAG, "密码写入失败...");
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写密码, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.WRITE_PASSWORD_FAIL.getCode());
                return sResultBean;
            }

            byte[] rwkey = EncryptUtils.hexString2Bytes(newPsw);
            //写rk、wk
            if (!nfc.setRwKey(rwkey)) {
                LogUtil.d(TAG, "rk、wk写入失败...");
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写密码, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.WRITE_RWKEY_FAIL.getCode());
                return sResultBean;
            }

//            //TODO 写itsp
            byte[] itsp1 = new byte[4];
            itsp1[0] = rdmITSP[0];
            itsp1[1] = rdmITSP[1];
            itsp1[2] = rdmITSP[2];
            itsp1[3] = rdmITSP[3];
            if (!nfc.writeConfigBlock(itsp1, (byte) 0x01)) {
                LogUtil.d(TAG, "写itsp1失败");
            }
            byte[] itsp2 = new byte[4];
            itsp2[0] = rdmITSP[4];
            itsp2[1] = rdmITSP[5];
            itsp2[2] = rdmITSP[6];
            itsp2[3] = rdmITSP[7];
            if (!nfc.writeConfigBlock(itsp2, (byte) 0x02)) {
                LogUtil.d(TAG, "写itsp2失败");
            }
        }

        //读取状态位
        LogUtil.i(TAG, "验证读取状态位开始");
        String pad = nfc.readOneBlock((byte) 0x80);
        LogUtil.i(TAG, "验证读取状态位开始:" + pad);
        if (TextUtils.isEmpty(pad)) {
            nfc.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
            return sResultBean;
        }
        pad = pad.toUpperCase();
        LogUtil.d(TAG, "pad --> " + pad);

        mTagCfg.setPassword("0000");
//        mTagCfg.setRk("00");
//        mTagCfg.setWk("00");
        mTagCfg.setAccess(true);
        mTagCfg.setAflmt("00");
        mTagCfg.setAflmtx("00");
        mTagCfg.setAuth0("20");
        mTagCfg.setIntsel(0);
//        mTagCfg.setItsp(OperationUtil.bytesToHexString(rdmITSP));
        mTagCfg.setItsp("40-40-40-40-40-40-40-40");
        mTagCfg.setOflag(pad);
        mTagCfg.setPad(pad);
        mTagCfg.setRk(OperationUtil.byteToString(readKey));
        mTagCfg.setWk(OperationUtil.byteToString(writeKe));
        mTagCfg.setUid(uid);

        mTagCfg.setTagType(tagType);

        if (tagType == 2) {
            mTagCfg.setIsTempTag(true);
        }

        if (tagType == 3) {
            mTagCfg.setIsV5state(true);
        }

//        mTagCfg.setTagType(2);
        mTagCfg.setBatchId(batchId);

        //新证书校验
        mApiNfcTag.setTagCfg(mTagCfg);
        LogUtil.d(TAG, "log-->" + JSON.toJSONString(mApiNfcTag));
        Gson gson = new Gson();
        String jsonData = gson.toJson(mApiNfcTag);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);

        postRequest(Constant.CFG_SAVE_URL, requestBody, initHeader(EToken), new RequestListener() {
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
                sResultBean.setErrno(TagErrorEnum.INIT_TAG_SUCCESS.getCode());
            }
        });

        return sResultBean;
    }
}
