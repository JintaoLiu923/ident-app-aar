package com.hachi.publishplugin.activity.ras14443;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.hachi.publishplugin.activity.GlobelHttpFunc;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.activity.base.BasePlugin4Vail;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.interfaces.service.IValiService;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 * 验证组件
 */
public class Ras14443ValiPlugin extends BasePlugin4Vail implements IValiService, BasePlugin.DetailListener {
    private static final String TAG = "Ras14443ValiPlugin";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static Boolean sIsV5state;

    @DoNotRename
    @Override
    public ResultBean vali(Context context, Tag tag, String key, String EToken, IdentLogReqBean log) {
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

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        OkHttp okHttp = new OkHttp(context);

        //获取配置信息
        getDetailRequest(uid, null, null, EToken, this);

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
        }

        //获取标志位信息
        String r_Read_begin = certDecode.substring(0, 2);
        String r_Read_over = certDecode.substring(2, 4);
        String w_Write_begin = certEncrypt.substring(0, 2);
        String w_Write_over = certEncrypt.substring(2, 4);

        if (isPswTag != null && !isPswTag) {
            NFCA nfc = new NFCA(tag);
            byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

            //验证密码
            if (!nfc.authenticate(pwd)) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                return sResultBean;
            }

            //读取标志位
            String status = nfc.readStatus();

            if (status.equals(r_Read_begin)) {
                //表示上次写入断开
                //直接写入新证书
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, "ok");
                boolean certFlag = false;
                for (int i = 0; i < 3; i++) {
                    certFlag = nfc.writeCert(certEncrypt, certDecode);
                    if (certFlag) {
                        break;
                    }
                }
                if (!certFlag) {
                    nfc.close();
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                    sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
                    return sResultBean;
                }

                //读取状态位
                LogUtil.i(TAG, "验证读取状态位开始");
                String pad = nfc.analysisTag();
                LogUtil.i(TAG, "验证读取状态位开始:" + pad);
                if (TextUtils.isEmpty(pad)) {
                    nfc.close();
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
                    sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
                    return sResultBean;
                }
                pad = pad.toUpperCase();

                SharedPreferences sharedPreferences2 = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
                SharedPreferences.Editor edit2 = sharedPreferences2.edit();
                edit2.clear();
                edit2.apply();

                //新证书校验
                LogUtil.i(TAG, "读取状态位:" + pad);
                okHttp.getFromInternetById(Constant.VERIFY_URL + "?uid=" + uid
                        + "&cert3=" + newCert + "&pad=" + pad, EToken);
                LogUtil.i(TAG, "验证比对请求结束");

                boolean flag2 = sharedPreferences2.getBoolean(Constant.FLAG, false);
                //flag2如果为false，没获取到数据
                if (!flag2) {
                    sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                }

                //修改标志位
                nfc.writeStatus(OperationUtil.stringToByte(w_Write_over));
                //拿到ras_id
                String res = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
                LogUtil.i(TAG, "证书比对结果:" + res);
                TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);
                nfc.close();

                //如果Error不为0直接返回错误
                if (tagBean1.getErrno() != 0) {
                    sResultBean.setTime(config_rasId);
                    sResultBean.setErrno(tagBean1.getErrno());
                    sResultBean.setErrmsg(tagBean1.getErrmsg());
                    return sResultBean;
                }

                String rasId = tagBean1.getData().getRasId();

                //确认数据
                LogUtil.i(TAG, "验证确认新证书开始");
                sResultBean = GlobelHttpFunc.sendConfirm(context, uid, "", newCert, EToken);
                if (sResultBean.getErrno() == 0) {
                    sResultBean.setTime(rasId);
                }

//                if (tagBean1.getData().isOpened()) {
//                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
//                } else {
//                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
//                }

                sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());

                nfc.close();
                return sResultBean;
            } else if (status.equals(r_Read_over)) {
                //上次写入完毕
                LogUtil.i(TAG, "验证读取证书开始");

                //读取证书
                String cert = nfc.readCert();
                if (TextUtils.isEmpty(cert)) {
                    nfc.close();
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读证书, Constant.FAIL);
                    sResultBean.setErrno(TagErrorEnum.CERT_READ_FAILED.getCode());
                    return sResultBean;
                }
                LogUtil.i(TAG, "验证读取证书结束:" + cert);
                //读取状态位
                LogUtil.i(TAG, "验证读取状态位开始");
                String pad = nfc.analysisTag();
                LogUtil.i(TAG, "验证读取状态位:" + pad);
                if (TextUtils.isEmpty(pad)) {
                    nfc.close();
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
                    sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
                    return sResultBean;
                }
                pad = pad.toUpperCase();

                SharedPreferences sharedPreferences1 = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
                SharedPreferences.Editor edit1 = sharedPreferences1.edit();
                edit1.clear();
                edit1.apply();

                //验证数据
                LogUtil.i(TAG, "验证比对请求开始");
                okHttp.getFromInternetById(Constant.VERIFY_URL + "?uid=" + uid
                        + "&cert3=" + cert + "&pad=" + pad, EToken);

                boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);
                //flag1为false，表示没有获取到数据
                if (!flag1) {
                    sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                }
                //拿到ras_id
                String res = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
                LogUtil.i(TAG, "证书比对结果:" + res);
                TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);

                //如果Error不为0则，则直接返回错误
                if (tagBean1.getErrno() != 0) {
                    sResultBean.setTime(config_rasId);
                    sResultBean.setErrno(tagBean1.getErrno());
                    sResultBean.setErrmsg(tagBean1.getErrmsg());
                    return sResultBean;
                }

                String rasId = tagBean1.getData().getRasId();
                LogUtil.i(TAG, "验证写入新证书开始");
                //修改标志位
                nfc.writeStatus(OperationUtil.stringToByte(w_Write_begin));
                //写入新证书
                boolean certFlag = false;
                for (int i = 0; i < 3; i++) {
                    certFlag = nfc.writeCert(certEncrypt, certDecode);
                    if (certFlag) {
                        break;
                    }
                }
                if (!certFlag) {
                    nfc.close();
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                    sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
                    return sResultBean;
                }
                //
                nfc.writeStatus(OperationUtil.stringToByte(w_Write_over));
                nfc.close();
                //确认数据
                LogUtil.i(TAG, "验证确认新证书开始");
                sResultBean = GlobelHttpFunc.sendConfirm(context, uid, "", newCert, EToken);
                if (sResultBean.getErrno() == 0) {
                    sResultBean.setTime(rasId);
                }

//                        if (tagBean1.getData().isOpened()) {
//                            sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
//                        } else {
//                            sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
//                        }
                sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());

                nfc.close();
                return sResultBean;
            } else {
                nfc.close();
                sResultBean.setErrno(TagErrorEnum.FLAG_READ_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读标志位, Constant.FAIL);
                return sResultBean;
            }
        } else {
            /*----------------------------14443普通标签---------------------------------*/
            byte[] pwd = new byte[4];
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
            }
            //密码认证
            NFCA nfc = new NFCA(tag);
            if (!nfc.authenticate(pwd)) {
                nfc.close();
//                    result.setUid(uid);
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                return sResultBean;
            }

            //读取标志位
            String status = nfc.readStatus();
            if (status.equals("01")) {
                //表示上次写入断开
                //直接写入新证书
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, "ok");
                boolean certFlag = false;
                for (int i = 0; i < 3; i++) {
                    certFlag = nfc.writeCert(newCert);
                    if (certFlag) {
                        break;
                    }
                }
                if (!certFlag) {
                    nfc.close();
                    sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
//                        result.setUid(uid);
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                    return sResultBean;
                }
                SharedPreferences sharedPreferences1 = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
                SharedPreferences.Editor edit1 = sharedPreferences1.edit();
                edit1.clear();
                edit1.apply();

                //新证书校验
                LogUtil.i(TAG, "验证比对请求开始");
                okHttp.getFromInternetById(Constant.VERIFY_URL + "?uid=" + uid
                        + "&cert3=" + newCert, EToken);
                LogUtil.i(TAG, "验证比对请求结束");
                boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);
                if (!flag1) {
                    sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                    return sResultBean;
                }
                //修改标志位
                nfc.writeStatus((byte) 0x00);
                //拿到ras_id
                String res = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
                LogUtil.i(TAG, "证书比对结果:" + res);
                TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);
                sIsV5state = tagBean1.getData().getIsV5state();
                if (tagBean1.getErrno() != 0) {
                    sResultBean.setErrno(tagBean1.getErrno());
                    sResultBean.setErrmsg(tagBean1.getErrmsg());
                    return sResultBean;
                }
                String rasId = tagBean1.getData().getRasId();
                //确认数据
                LogUtil.i(TAG, "验证确认新证书开始");
                sResultBean = GlobelHttpFunc.sendConfirm(context, uid, "", newCert, EToken);
                if (sResultBean.getErrno() == 0) {
                    sResultBean.setTime(rasId);
                }
                if (sIsV5state != null &&
                        sIsV5state) {
                    if (tagBean1.getData().getIsOpened() != null &&
                            tagBean1.getData().getIsOpened()) {
                        sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
                    } else {
                        sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
                    }
                } else {
                    sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
                }
                nfc.close();
                return sResultBean;
            } else if (status.equals("00")) {
                //上次写入完毕
                LogUtil.i(TAG, "验证读取证书开始");
                //读取证书
                String cert = nfc.readCert();
                if (cert.equals("")) {
                    nfc.close();
//                        result.setUid(uid);
                    sResultBean.setErrno(TagErrorEnum.CERT_READ_FAILED.getCode());
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读证书, Constant.FAIL);
                    return sResultBean;
                }
                LogUtil.i(TAG, "验证读取证书结束");
                //验证数据
                LogUtil.i(TAG, "验证比对请求开始");
                SharedPreferences sharedPreferences1 = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
                SharedPreferences.Editor edit1 = sharedPreferences1.edit();
                edit1.clear();
                edit1.apply();

                okHttp.getFromInternetById(Constant.VERIFY_URL + "?uid=" + uid
                        + "&cert3=" + cert, EToken);
                LogUtil.i(TAG, "验证比对请求结束");
                boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);
                //flag为false，表示没有获取到数据
                if (!flag1) {
                    sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                    nfc.close();
                    return sResultBean;
                }

                //拿到ras_id
                String res = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
                LogUtil.i(TAG, "证书比对结果:" + res);
                TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);
                sIsV5state = tagBean1.getData().getIsV5state();
                if (tagBean1.getErrno() != 0) {
                    sResultBean.setErrno(tagBean1.getErrno());
                    sResultBean.setErrmsg(tagBean1.getErrmsg());
                    return sResultBean;
                }
                String rasId = tagBean1.getData().getRasId();
                LogUtil.i(TAG, "验证写入新证书开始");
                //修改标志位
                nfc.writeStatus((byte) 0x01);

                //写入新证书
                boolean certFlag = false;
                for (int i = 0; i < 3; i++) {
                    certFlag = nfc.writeCert(newCert);
                    if (certFlag) {
                        break;
                    }
                }
                LogUtil.i(TAG, "验证写入新证书结束");
                if (!certFlag) {
                    nfc.close();
                    sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
//                                result.setUid(uid);
                    PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                    return sResultBean;
                }
                //
                nfc.writeStatus((byte) 0x00);
                nfc.close();
                //确认数据
                LogUtil.i(TAG, "验证确认新证书开始");
                sResultBean = GlobelHttpFunc.sendConfirm(context, uid, "", newCert, EToken);
                if (sResultBean.getErrno() == 0) {
                    sResultBean.setTime(rasId);
                }

                if (sIsV5state != null && sIsV5state) {
                    if (tagBean1.getData().getIsOpened() != null &&
                            tagBean1.getData().getIsOpened()) {
                        sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
                    } else {
                        sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
                    }
                } else {
                    sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
                }
                nfc.close();
                return sResultBean;
            } else {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读标志位, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.FLAG_READ_FAILED.getCode());
                return sResultBean;
            }
        }

    }

    private void initData(TagBean tagBean) {
        certEncrypt = tagBean.getData().getCertEncrypt();
        certDecode = tagBean.getData().getCertDecode();
        isPswTag = tagBean.getData().getIsPswTag();

        config_password = tagBean.getData().getCfg().getPassword();
        config_rasId = tagBean.getData().getRasId();
        newCert = tagBean.getData().getNewCert();
        hasCert = tagBean.getData().getHasCert();
        sIsV5state = tagBean.getData().getIsV5state();

        if (hasCert != null) {

            LogUtil.i(TAG, "读取Config:"
                    + " password:" + config_password
                    + " newCert:" + newCert
                    + " hasCert:" + hasCert
                    + " rasId:" + config_rasId);
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
