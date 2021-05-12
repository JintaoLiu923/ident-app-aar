package com.hachi.publishplugin.activity.ras14443;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.hachi.publishplugin.activity.GlobelHttpFunc;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.activity.base.BasePlugin4Cert;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.interfaces.service.ICertService;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

/**
 * 发证组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class Ras14443CertPlugin extends BasePlugin4Cert implements ICertService, BasePlugin.DetailListener {
    private static final String TAG = "Ras14443CertPlugin";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static String ndef = "";//密文NDEF
    private static Boolean isPswTag = false;//标签类型，false为RAS，true为14443或15693
    private static String config_rk = "";
    private static String config_wk = "";
    private static String config_itsp = "";
    private static int config_intsel;
    private static String config_rasIdEncrypt = "";
    private static int err_num = 0;
    private static String newPsw = "";

    @Override
    public ResultBean cert(Context context, Tag tag, String key, String EToken, String mobile, String password, String newPassword) {
        mContext = context;
        sResultBean = new ResultBean();

        //读取标签Uid
        uid = GlobelRasFunc.readUid14443(tag);
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);

        getDetailRequest(uid, null, null, EToken, this);

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
        }

        //获取标志位信息
        String r_Write_begin = certDecode.substring(0, 2);
        String r_Write_over = certDecode.substring(2, 4);
        String w_Write_begin = certEncrypt.substring(0, 2);
        String w_Write_over = certEncrypt.substring(2, 4);

        //根据UID判断类型
        if (isPswTag != null && !isPswTag) {
            /*--------------------------------------------RAS标签--------------------------------------------*/
            //密码认证
            NFCA nfca1 = new NFCA(tag);
            byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

            //验证密码
            if (!nfca1.authenticate(pwd)) {
                nfca1.close();
//                    result.setUid(uid);
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                return sResultBean;
            }

//            if (!TextUtils.isEmpty(newPsw)) {
//                byte[] newPswByte = EncryptUtils.hexString2Bytes(newPsw);
////                newPswByte[0] = OperationUtil.stringToByte(newPsw.substring(0, 2));
////                newPswByte[1] = OperationUtil.stringToByte(newPsw.substring(2, 4));
////                newPswByte[2] = OperationUtil.stringToByte(newPsw.substring(4, 6));
////                newPswByte[3] = OperationUtil.stringToByte(newPsw.substring(6));
//                boolean flag1 = nfca1.writePage(43, newPswByte);
//                if (!flag1) {
//                    nfca1.close();
//                    sResultBean.setErrno(TagErrorEnum.PWD_WRITE_FAILED.getCode());
////                        result.setUid(uid);
//                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写密码, Constant.FAIL);
//                    return sResultBean;
//                }
//                LogUtil.d(TAG, "14443密码写入成功");
//            }

//            boolean flag1 = nfca1.writePage(43, pwd);
//            if (!flag1) {
//                nfca1.close();
//                sResultBean.setErrno(TagErrorEnum.PWD_WRITE_FAILED.getCode());
////                        result.setUid(uid);
//                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写密码, Constant.FAIL);
//                return sResultBean;
//            }
//            LogUtil.d(TAG, "14443密码写入成功");


            LogUtil.i(TAG, "发证读取状态位信息开始");

            //读取状态位
            String pad = nfca1.analysisTag();
            if (TextUtils.isEmpty(pad)) {
                nfca1.close();
//                    result.setUid(uid);
                sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
                return sResultBean;
            }

            pad = pad.toUpperCase();
            LogUtil.i(TAG, "读取状态位:" + pad);
            LogUtil.i(TAG, "发证读取状态位信息结束");

            //没有发证信息才写入时间戳
            nfca1.writeStatus(OperationUtil.stringToByte(w_Write_begin)); //qq a
            if (hasCert != null && !hasCert) {
                //写入时间戳
                boolean rasIdFlag = false;
                for (int i = 0; i < 3; i++) {
                    rasIdFlag = nfca1.writeRasId(config_rasIdEncrypt);
                    if (rasIdFlag) {
                        break;
                    }
                }

                if (!rasIdFlag) {
                    nfca1.close();
                    sResultBean.setErrno(TagErrorEnum.BIZ_WRITE_FAILED.getCode());
//                        result.setUid(uid);
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写时间戳, Constant.FAIL);
                    return sResultBean;
                }
            }

            //写入证书
            boolean certFlag = false;
            for (int i = 0; i < 3; i++) {
                certFlag = nfca1.writeCert(certEncrypt, certDecode);
                if (certFlag) {
                    break;
                }
            }

            nfca1.writePage(0x0D, new byte[]{0x5F, 0x5F, 0x5F, 0x5F});

            if (!certFlag) {
                nfca1.close();
                sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
//                    result.setUid(uid);
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                return sResultBean;
            }

            nfca1.writeStatus(OperationUtil.stringToByte(w_Write_over)); //qq a

            //写入NDEF
            byte[] ndefMessage = EncryptUtils.hexString2Bytes(ndef);

            boolean ndefFlag = false;
            for (int i = 0; i < 3; i++) {
                ndefFlag = nfca1.writeNDEF(ndefMessage);
                if (ndefFlag) {
                    break;
                }
            }

            if (!ndefFlag) {
                nfca1.close();
//                    result.setUid(uid);
                sResultBean.setErrno(TagErrorEnum.NDEF_WRITE_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写NDEF, Constant.FAIL);
                return sResultBean;
            }

            nfca1.close();
            //上传数据
            LogUtil.i(TAG, "发证确认上传信息开始");
            //同步请求

            sResultBean = GlobelHttpFunc.sendConfirm(context, uid, "", newCert, EToken);
            if (sResultBean.getErrno() == 0) {
                sResultBean.setErrno(TagErrorEnum.CERT_SUCCESS.getCode());
                sResultBean.setTime(config_rasId);
            }
//                GlobelHttpFunc.sendData(uid, pad, newCert, EToken);

//                if (!hasCert) {
//                    result.setTime(config_rasId);
//                }
            return sResultBean;
        } else {
            /*--------------------------14443普通标签------------------------*/
            byte[] pwd = new byte[4];
            byte[] newPwd = new byte[4];
            //写入证书、时间戳
            if (hasCert != null && hasCert) {
                pwd[0] = OperationUtil.stringToByte(config_password.substring(0, 2));
                pwd[1] = OperationUtil.stringToByte(config_password.substring(3, 5));
                pwd[2] = OperationUtil.stringToByte(config_password.substring(6, 8));
                pwd[3] = OperationUtil.stringToByte(config_password.substring(9));
            } else {
                pwd[0] = (byte) 0xFF;
                pwd[1] = (byte) 0xFF;
                pwd[2] = (byte) 0xFF;
                pwd[3] = (byte) 0xFF;
                newPwd[0] = OperationUtil.stringToByte(config_password.substring(0, 2));
                newPwd[1] = OperationUtil.stringToByte(config_password.substring(3, 5));
                newPwd[2] = OperationUtil.stringToByte(config_password.substring(6, 8));
                newPwd[3] = OperationUtil.stringToByte(config_password.substring(9));
            }

            LogUtil.i(TAG, "14443密码 --> " + OperationUtil.bytesToHexString(pwd));

            //写入NDEF
            NdefRecord ndefRecord = NFCA.createUriRecord1(Constant.NDEF_URI);
            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});

            boolean result1 = GlobelRasFunc.write14443tag(ndefMessage, tag);
            if (result1) {
                LogUtil.i(TAG, "aid写入成功...");
            } else {
                LogUtil.i(TAG, "aid写入失败...");
                sResultBean.setErrno(TagErrorEnum.NDEF_WRITE_FAILED.getCode());
//                    result.setUid(uid);
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写NDEF, Constant.FAIL);
                return sResultBean;
            }

            //密码认证
            NFCA nfca1 = new NFCA(tag);
            if (!nfca1.authenticate(pwd)) {
                nfca1.close();
//                    result.setUid(uid);
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                return sResultBean;
            }

            //写入证书
            nfca1.writeStatus((byte) 0x01); //qq a
            boolean certFlag = false;
            for (int i = 0; i < 3; i++) {
                certFlag = nfca1.writeCert(newCert);
                if (certFlag) {
                    break;
                }
            }

            if (!certFlag) {
                nfca1.close();
                sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
//                    result.setUid(uid);
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                return sResultBean;
            }

            //没有发证信息才写入时间戳
            if (hasCert != null && !hasCert) {
                //写入时间戳
                boolean rasIdFlag = false;
                for (int i = 0; i < 3; i++) {
                    rasIdFlag = nfca1.writeRasId(config_rasId);
                    if (rasIdFlag) {
                        break;
                    }
                }

                if (!rasIdFlag) {
                    nfca1.close();
                    sResultBean.setErrno(TagErrorEnum.BIZ_WRITE_FAILED.getCode());
//                        result.setUid(uid);
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写时间戳, Constant.FAIL);
                    return sResultBean;
                }
            }

            //写入新密码
            if (hasCert != null && !hasCert) {
                boolean flag1 = nfca1.writePage(43, newPwd);
                if (!flag1) {
                    nfca1.close();
                    sResultBean.setErrno(TagErrorEnum.PWD_WRITE_FAILED.getCode());
//                        result.setUid(uid);
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写密码, Constant.FAIL);
                    return sResultBean;
                }
            }

            nfca1.writeStatus((byte) 0x00); //qq a
            //确认上传
            sResultBean = GlobelHttpFunc.sendConfirm(context, uid, "", newCert, EToken);

            if (sResultBean.getErrno() == 0) {
                if (TextUtils.isEmpty(config_rasId)) {
                    sResultBean.setErrno(TagErrorEnum.HAS_CERT.getCode());
                    return sResultBean;
                }
                sResultBean.setErrno(TagErrorEnum.CERT_SUCCESS.getCode());
                sResultBean.setTime(config_rasId);
            }
            return sResultBean;
        }
    }

    private void initData(TagBean tagBean) {
        certEncrypt = tagBean.getData().getCertEncrypt();
        certDecode = tagBean.getData().getCertDecode();
        config_rasIdEncrypt = tagBean.getData().getRasIdEncrypt();
        ndef = tagBean.getData().getNdef();
        isPswTag = tagBean.getData().getIsPswTag();

        config_password = tagBean.getData().getCfg().getPassword();
        config_rk = tagBean.getData().getCfg().getRk();
        config_wk = tagBean.getData().getCfg().getWk();
        config_itsp = tagBean.getData().getCfg().getItsp();
        config_intsel = tagBean.getData().getCfg().getIntsel();
        config_rasId = tagBean.getData().getRasId();
        newCert = tagBean.getData().getNewCert();
        hasCert = tagBean.getData().getHasCert();
        newPsw = tagBean.getData().getNewPsw();
        if (isPswTag != null && hasCert != null) {
            LogUtil.i(TAG, "读取Config: RK" + config_rk
                    + " WK:" + config_wk
                    + " ITSP:" + config_itsp
                    + " INTSEL:" + config_intsel
                    + " password:" + config_password
                    + " newCert:" + newCert
                    + " hasCert:" + hasCert
                    + " rasId:" + config_rasId
                    + " certEcrypt:" + certEncrypt
                    + " certDecode:" + certDecode
                    + " ndef:" + ndef
                    + " isPswTag:" + isPswTag);
        }
    }

    @Override
    public void error(TagBean tagBean) {
        sResultBean.setErrno(tagBean.getErrno());
        sResultBean.setErrmsg(tagBean.getErrmsg());
    }

    @Override
    public void success(TagBean tagBean) {
        initData(tagBean);
    }
}
