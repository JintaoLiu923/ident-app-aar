package com.hachi.publishplugin.activity.rasF8213;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.GlobelHttpFunc;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin4Cert;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.interfaces.service.ICertService;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

public class RasF8213CertPlugin extends BasePlugin4Cert implements ICertService {
    private static final String TAG = "RasF8213CertPlugin";

    @Override
    public ResultBean cert(Context context, Tag tag, String key, String EToken, String mobile, String password, String newPassword) {
        mContext = context;
        sResultBean = new ResultBean();

        LogUtil.i(TAG, "开始发证流程");

        //读取标签Uid
        uid = GlobelRasFunc.readUidF8213(tag);
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);

        getDetailRequest(uid, null, null, EToken, new DetailListener() {
            @Override
            public void error(TagBean tagBean) {
                sResultBean.setErrno(tagBean.getErrno());
                sResultBean.setErrmsg(tagBean.getErrmsg());
            }

            @Override
            public void success(TagBean tagBean) {
                //解析数据
                initData(tagBean);
            }
        });

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
        }

        if (isPswTag != null && isPswTag) {
                /*if (hasCert) {
                    result.setErrno(TagError.HAS_CERT.getCode());
                    map.put("uid", uid);
                    map.put("action", ActTypeEnum.ACT_TYPE_标签已发证.getName());
                    map.put("result", Constant.FAIL);
                    GlobelHttpFunc.sendLog(map, EToken);
                } else {*/
            byte[] pwd = new byte[]{
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
            };
            byte[] newPwd = new byte[4];
            //写入证书、时间戳
            if (TextUtils.isEmpty(config_password) || config_password.equals("0")) {
                sResultBean.setErrno(TagErrorEnum.DEFAULT.getCode());
                sResultBean.setErrmsg(TagErrorEnum.DEFAULT.getDescription());
                return sResultBean;
            } else {
                newPwd[0] = OperationUtil.stringToByte(config_password.substring(0, 2));
                newPwd[1] = OperationUtil.stringToByte(config_password.substring(2, 4));
                newPwd[2] = OperationUtil.stringToByte(config_password.substring(4, 6));
                newPwd[3] = OperationUtil.stringToByte(config_password.substring(6));
                LogUtil.i(TAG, "F8213密码:" + OperationUtil.bytesToHexString(newPwd));
            }
            //写入NDEF
            NdefRecord ndefRecord = NFCA.createUriRecord1(Constant.NDEF_URI);
            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});

            boolean result1 = GlobelRasFunc.writeF8213Ndef(ndefMessage, tag);

            if (result1) {
                LogUtil.i(TAG, "aid写入成功");
            } else {
                LogUtil.i(TAG, "aid写入失败");
                sResultBean.setErrno(TagErrorEnum.NDEF_WRITE_FAILED.getCode());
//                    result.setUid(uid);
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写NDEF, Constant.FAIL);
                return sResultBean;
            }
            //密码认证
            NFCA nfca1 = new NFCA(tag);

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
//                LogUtil.i(TAG, "certFlag -->" + certFlag);
                sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
//                    result.setUid(uid);
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                return sResultBean;
            }

//            boolean writeUid = nfca1.writePage(0, EncryptUtils.hexString2Bytes("00000000"));
//            if (!writeUid) {
//                LogUtil.d(TAG,"写UID失败...");
//            }

            //写入新密码
            if (!GlobelRasFunc.getIsRasIdModeOpen()) {
                boolean flag1;
                if (GlobelRasFunc.getIsRasIdModeOpen()) {
                    flag1 = nfca1.writePage(43, pwd);
                } else {
                    flag1 = nfca1.writePage(43, newPwd);
                }

                if (!flag1) {
                    nfca1.close();
                    sResultBean.setErrno(TagErrorEnum.PWD_WRITE_FAILED.getCode());
//                        result.setUid(uid);
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写密码, Constant.FAIL);
                    return sResultBean;
                }
            }

            nfca1.writeStatus((byte) 0x00); //qq a
            nfca1.close();
            //确认上传
            sResultBean = GlobelHttpFunc.sendConfirm(context, uid, "", newCert, EToken);
            if (sResultBean.getErrno() == 0) {
                sResultBean.setErrno(TagErrorEnum.CERT_SUCCESS.getCode());
                sResultBean.setTime(config_rasId);
            }
        }
        return sResultBean;
        //}
    }

    private void initData(TagBean tagBean) {
        isPswTag = tagBean.getData().getIsPswTag();
        sErrmsg = tagBean.getErrmsg();
        config_password = tagBean.getData().getCfg().getPassword();
        newCert = tagBean.getData().getNewCert();
        hasCert = tagBean.getData().getHasCert();
        config_rasId = tagBean.getData().getRasId();
        if (hasCert != null) {
            LogUtil.i(TAG, "message:" + sErrmsg
                    + " password:" + config_password
                    + " newCert:" + newCert
                    + " hasCert:" + hasCert);
        }
    }
}
