package com.hachi.publishplugin.activity.rasF8023;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.GlobelHttpFunc;
import com.hachi.publishplugin.activity.base.BasePlugin4Cert;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.interfaces.service.ICertService;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

/**
 * F8023 电子锁下盖 标签发证
 **/
public class RasF8023CertPlugin extends BasePlugin4Cert {

    private static final String TAG = "RasF8023CertPlugin";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static String ndef = "";//密文NDEF
    private static Boolean isTmpTag = false;
    private static Integer tagType = 0;
    private static String config_rasIdEncrypt = "";

    public ResultBean cert(Context context, Tag tag, TagBean tagBean, String key, String EToken, String mobile, String password, String newPassword) {
        mContext = context;
        sResultBean = new ResultBean();
        //读取UID
        NfcVTmp nfcv = new NfcVTmp(tag);
        if (nfcv == null) {
            nfcv.close();
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        uid = nfcv.getUID();
        if (TextUtils.isEmpty(uid)) {
            nfcv.close();
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            nfcv.close();
            return sResultBean;
        }
        sResultBean.setUid(uid);
        //获取随机数
        byte[] random = nfcv.getRandom();
        if (random == null || random.length == 0) {
            nfcv.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            return sResultBean;
        }

        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.i(TAG, "随机数 --> " + randomStr);

        initData(tagBean);

        if (sResultBean.getErrno() != 0) {
            nfcv.close();
            return sResultBean;
        }

        String w_Write_begin = certEncrypt == null ? "01" : certEncrypt.substring(0, 2);
        String w_Write_over = certEncrypt == null ? "00" : certEncrypt.substring(2, 4);
//            if (isPswTag != null && !isPswTag) {
        //密码认证
        byte[] pwd = EncryptUtils.hexString2Bytes(config_password);
//            byte[] pwd = {0x00, 0x00, 0x00, 0x00};
        LogUtil.i(TAG, "随机数 --> " + Arrays.toString(random));

        //对密码和随机数进行异或运算
        byte[] xorResult = getXor(random, pwd);
        System.arraycopy(xorResult, 0, pwd, 0, xorResult.length);
        System.arraycopy(xorResult, 0, pwd, 2, xorResult.length);

        if (!nfcv.verifyPwd(pwd)) {
            nfcv.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            return sResultBean;
        }

        //todo 写入NDEF
        boolean ndefFlag = nfcv.writeTmpNedf(EncryptUtils.hexString2Bytes(ndef));
        if (!ndefFlag) {
            sResultBean.setErrno(TagErrorEnum.NDEF_WRITE_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写NDEF, Constant.FAIL);
            return sResultBean;
        }

        //读取状态位
        String pad = nfcv.readOneBlock((int) 0x40);
        pad = pad.substring(0, 2);
//                String pad = nfcv.readOneBlock((int) 0x40);
        LogUtil.i(TAG, "pad -->" + pad);
        if (TextUtils.isEmpty(pad)) {
            nfcv.close();
//                    result.setUid(uid);
            sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
            return sResultBean;
        }
        pad = pad.toUpperCase();

        LogUtil.i(TAG, "读取状态位 --> " + pad);

//        //没有发证信息才写入时间戳
//        if (hasCert != null && !hasCert) {
//            //写入时间戳
//            boolean rasIdFlag = false;
//            if (!TextUtils.isEmpty(config_rasIdEncrypt)) {
//                for (int i = 0; i < 3; i++) {
//                    rasIdFlag = nfcv.writeRasId2Tmp(config_rasIdEncrypt, 11);
//                    if (rasIdFlag) {
//                        break;
//                    }
//                }
//            }
//            if (!rasIdFlag) {
//                nfcv.close();
//                sResultBean.setErrno(TagErrorEnum.BIZ_WRITE_FAILED.getCode());
//                PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_写时间戳, Constant.FAIL);
//                return sResultBean;
//            }
//            LogUtil.i(TAG, "写入时间戳结束");
//        }

//        //写入证书
//        nfcv.writeStatus(OperationUtil.stringToByte(w_Write_begin), 14, 0); //qq a
//        boolean writeFlag = false;
//        for (int i = 0; i < 3; i++) {
//            writeFlag = nfcv.writeCert(certEncrypt.substring(4), certDecode.substring(4), OperationUtil.stringToByte(w_Write_begin)
//                    , OperationUtil.stringToByte(w_Write_over), 13, 14);
//            if (writeFlag) {
//                break;
//            }
//        }
//        if (!writeFlag) {
//            nfcv.close();
//            sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
//            PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
//            return sResultBean;
//        }
//        nfcv.writeStatus(OperationUtil.stringToByte(w_Write_over), 14, 0); //qq a

        nfcv.close();
        //上传数据
        LogUtil.i(TAG, "温控--->发证确认上传信息开始");
        sResultBean = GlobelHttpFunc.sendConfirm(context, uid, pad, newCert, EToken);
        if (sResultBean.getErrno() == 0) {
            sResultBean.setErrno(TagErrorEnum.CERT_SUCCESS.getCode());
            sResultBean.setTime(config_rasId);
        }
        return sResultBean;
    }

    private byte[] getXor(byte[] random, byte[] pwd) {
        byte[] xorResult = new byte[2];
        xorResult[0] = (byte) (random[0] | pwd[0]);
        xorResult[1] = (byte) (random[1] | pwd[1]);
        return xorResult;
    }

    private void initData(TagBean tagBean) {
        certEncrypt = tagBean.getData().getCertEncrypt();
        certDecode = tagBean.getData().getCertDecode();
        isPswTag = tagBean.getData().getIsPswTag();
        isTmpTag = tagBean.getData().getIsTmpTag();
        tagType = tagBean.getData().getTagType();
        config_rasIdEncrypt = tagBean.getData().getRasIdEncrypt();
        ndef = tagBean.getData().getNdef();
        config_password = tagBean.getData().getCfg().getPassword();
        config_rasId = tagBean.getData().getRasId();
        newCert = tagBean.getData().getNewCert();
        hasCert = tagBean.getData().getHasCert();
    }
}
