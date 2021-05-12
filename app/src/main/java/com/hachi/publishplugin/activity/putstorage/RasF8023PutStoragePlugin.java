package com.hachi.publishplugin.activity.putstorage;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.ApiNfcTag;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TbRasCfg;
import com.hachi.publishplugin.bean.TbRasCfgNokey;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;
import com.hachi.publishplugin.utils.TimeUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * F8023 电子锁下盖 标签入库
 */
public class RasF8023PutStoragePlugin extends BasePlugin4Tag {
    private ApiNfcTag mApiNfcTag;
    private TbRasCfgNokey mTbRasCfgNokey;
    private TbRasCfg mTagCfg;
    private static final String TAG = "Ras15693PutStoragePlugin";
    protected Map<String, String> mHeader = new HashMap<>();

    public ResultBean putStorage(Context context, Tag tag, String key, String EToken) {
        try {
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
            LogUtil.i(TAG, "随机数:" + randomStr);

            byte[] pwd = {0x00, 0x00, 0x00, 0x00};

            //对密码和随机数进行异或运算
            byte[] xorResult = getXor(random, pwd);
            System.arraycopy(xorResult, 0, pwd, 0, xorResult.length);
            System.arraycopy(xorResult, 0, pwd, 2, xorResult.length);

            LogUtil.d(TAG, "密码验证...");
            if (!nfc.verifyPwd(pwd)) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                return sResultBean;
            }
            LogUtil.d(TAG, "密码验证成功...");

            //读取状态位
            String pad = nfc.readOneBlock((byte) 0x40).substring(0, 2);
            LogUtil.i(TAG, "状态位 --> " + pad);

            if (TextUtils.isEmpty(pad)) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
                return sResultBean;
            }

            pad = pad.toUpperCase();

            int date = Integer.parseInt(TimeUtil.getTime("yyyyMMdd"));

            //新证书校验
            mTbRasCfgNokey.setPad(pad);
            mTbRasCfgNokey.setBatchId(date);
            mTbRasCfgNokey.setPassword("00000000");
            mTbRasCfgNokey.setState((short) 0);
            mTbRasCfgNokey.setTagType(3);
            mApiNfcTag.setTagCfgNoKey(mTbRasCfgNokey);
            mApiNfcTag.setTagCfg(mTagCfg);

            LogUtil.d(TAG, "log-->" + JSON.toJSONString(mApiNfcTag));

//        byte[] bytesPwd = OperationUtil.stringToBytes(randomPwd, 8);
//        nfc.setPWD(bytesPwd);

            Gson gson = new Gson();
            String jsonData = gson.toJson(mApiNfcTag);
//        LogUtil.i(TAG, "json数据 --> " + jsonData);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);

            postRequest(Constant.ADMIN_CFG_SAVE_URL, requestBody, initHeader(EToken), new RequestListener() {
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
                    sResultBean.setErrno(TagErrorEnum.PUT_STORAGE_SUCCESS.getCode());
                }
            });
        } catch (Exception e) {

        }
        return sResultBean;
    }

    /**
     * 对密码和随机数进行异或运算
     *
     * @param random 随机数
     * @param pwd    密码
     * @return
     */
    private byte[] getXor(byte[] random, byte[] pwd) {
        byte[] xorResult = new byte[2];
        xorResult[0] = (byte) (random[0] | pwd[0]);
        xorResult[1] = (byte) (random[1] | pwd[1]);
        return xorResult;
    }
}
