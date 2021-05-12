package com.hachi.publishplugin.activity.ras15693;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.activity.base.TagNfcvPlugin;
import com.hachi.publishplugin.bean.DataBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * NDEF
 */
public class Ras15693NdefPlugin extends BasePlugin {
    private static Context mContext;
    private static String TAG = "Debug";
    private static String uid;
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static String ndefEncrypt = "";
    private static Boolean isPswTag = false;
    private static Boolean isTmpTag = false;
    private static Integer tagType = 0;
    private static String config_password = "";
    private static String config_rasId = "";
    private static String newCert = "";//明文证书
    private static Boolean hasCert = false;
    private static Map<String, Object> map = new HashMap<>();

    public static ResultBean writeNdef(Context context, Tag tag, String EToken, String ndef, boolean isStr) {
        sResultBean = new ResultBean();
        mContext = context;
        if (TextUtils.isEmpty(EToken)) {
            //未授权
            sResultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return sResultBean;
        }

        TagNfcvPlugin tagNfcv = new TagNfcvPlugin(tag, mContext, EToken, true,null,new TagNfcvPlugin.TagNfcvListener() {
            @Override
            public void error(ResultBean resultBean) {
                sResultBean = resultBean;
                LogUtil.d(TAG, "error...");
            }

            @Override
            public void success(ResultBean resultBean) {
                sResultBean = resultBean;
                LogUtil.d(TAG, "success...");
            }
        });

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
        }

        LogUtil.d(TAG, "NDEF --> " + ndef);
        //todo 写入NDEF
        boolean ndefFlag = tagNfcv.mNfc.writeTmpNedf(EncryptUtils.hexString2Bytes(ndef));

//        boolean ndefFlag = nfcv.writeTmpNedf(EncryptUtils.hexString2Bytes(ndefEncrypt));
//        boolean ndefFlag = NfcVTmp.writeNdef15693(tag, ndef);
        if (!ndefFlag) {
            tagNfcv.mNfc.close();
            sResultBean.setErrno(TagErrorEnum.NDEF_WRITE_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写NDEF, Constant.FAIL);
            return sResultBean;
        }
        tagNfcv.mNfc.close();

        sResultBean.setErrno(TagErrorEnum.SUCCESS.getCode());
        return sResultBean;
    }

    public static ResultBean readNdef(Context context, Tag tag, String EToken) {
        ResultBean resultBean = new ResultBean();
        mContext = context;
        if (TextUtils.isEmpty(EToken)) {
            //未授权
            resultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return resultBean;
        }

        //读取UID
        NfcVTmp nfcv = new NfcVTmp(tag);
        if (nfcv == null) {
            resultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return resultBean;
        }
        uid = nfcv.getUID();
        if (TextUtils.isEmpty(uid)) {
            nfcv.close();
            resultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return resultBean;
        }
        uid = uid.toUpperCase();
        LogUtil.i(TAG, "温度Config-->标签id:" + uid);
        resultBean.setUid(uid);

        //todo 读取NDEF

        //读取NDEF
        String strR = nfcv.readBlocks(0x0, 10);
        if (strR == null) {
            resultBean.setErrno(TagErrorEnum.NDEF_READ_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读NDEF, Constant.FAIL);
            return resultBean;
        }

        DataBean dataBean = new DataBean();
        dataBean.setNdef(strR);
        resultBean.setData(dataBean);
//                result.getData().setNdef(strR);
        resultBean.setErrno(TagErrorEnum.SUCCESS.getCode());
        return resultBean;
    }
}

