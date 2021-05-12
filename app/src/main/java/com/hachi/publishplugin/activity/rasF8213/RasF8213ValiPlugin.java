package com.hachi.publishplugin.activity.rasF8213;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

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
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import static android.content.Context.MODE_PRIVATE;

public class RasF8213ValiPlugin extends BasePlugin4Vail implements IValiService, BasePlugin.DetailListener {
    private static final String TAG = "RasF8213ValiPlugin";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static String sErrmsg;
    protected static int mError;
    protected static String mErrMsg;

    @Override
    public ResultBean vali(Context context, Tag tag, String key, String EToken, IdentLogReqBean log) {
        mContext = context;
        sResultBean = new ResultBean();

        //读取UID
        uid = GlobelRasFunc.readUidF8213(tag);
        LogUtil.d(TAG, "标签Id --> " + uid);
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        sResultBean.setUid(uid);


        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();

        //获取配置信息
        OkHttp okHttp = new OkHttp(context);
        getDetailRequest(uid, "", null, EToken, this);

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
        }

        byte[] pwd = new byte[4];
        
        if (hasCert != null && hasCert) {
            pwd[0] = OperationUtil.stringToByte(config_password.substring(0, 2));
            pwd[1] = OperationUtil.stringToByte(config_password.substring(2, 4));
            pwd[2] = OperationUtil.stringToByte(config_password.substring(4, 6));
            pwd[3] = OperationUtil.stringToByte(config_password.substring(6));
        } else {
            pwd[0] = (byte) 0xFF;
            pwd[1] = (byte) 0xFF;
            pwd[2] = (byte) 0xFF;
            pwd[3] = (byte) 0xFF;
        }
//        pwd[0] = OperationUtil.stringToByte(config_password.substring(0, 2));
//        pwd[1] = OperationUtil.stringToByte(config_password.substring(2, 4));
//        pwd[2] = OperationUtil.stringToByte(config_password.substring(4, 6));
//        pwd[3] = OperationUtil.stringToByte(config_password.substring(6));
        //密码认证
        NFCA nfc = new NFCA(tag);
//        byte[] pwd_old = nfc.readPages(0x2B);
//        LogUtil.d(TAG,"psw -->"+pwd_old);
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

            byte[] bytes = nfc.readPages(43);
            LogUtil.d(TAG, "密码为 -->" + OperationUtil.bytesToHexString(bytes));
            if (!certFlag) {
                nfc.close();
                sResultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
//                result.setUid(uid);
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                return sResultBean;
            }
            //新证书校验
            LogUtil.i(TAG, "验证比对请求开始");
            SharedPreferences sharedPreferences1 = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
            SharedPreferences.Editor edit1 = sharedPreferences1.edit();
            edit1.clear();
            edit1.apply();
            okHttp.getFromInternetById(Constant.VERIFY_URL + "?uid=" + uid
                    + "&cert3=" + newCert, EToken);
            LogUtil.i(TAG, "验证比对请求结束");
            boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);

            if (!flag1) {
                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            }

            //修改标志位
            nfc.writeStatus((byte) 0x00);
            //拿到ras_id
            String res = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
            LogUtil.i(TAG, "证书比对结果:" + res);
//                TagBean tagBean1 = new ParseJson().Json2TagBean(res);
            TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);

            if (tagBean1.getErrno() == 0) {
                config_rasId = tagBean1.getData().getRasId();
                //确认数据
                LogUtil.i(TAG, "验证确认新证书开始");
                sResultBean = GlobelHttpFunc.sendConfirm(context, uid, "", newCert, EToken);
                if (sResultBean.getErrno() == 0) {
                    sResultBean.setTime(config_rasId);
                }
//                    if (tagBean1.getData().isOpened()) {
//                        sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
//                    } else {
//                        sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
//                    }
                sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
            } else {
                sResultBean.setErrno(tagBean1.getErrno());
                sResultBean.setErrmsg(tagBean1.getErrmsg());
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

            byte[] bytes = nfc.readPages(43);
            LogUtil.d(TAG, "密码为 -->" + OperationUtil.bytesToHexString(bytes));
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
            if (!flag1) {
                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                nfc.close();
                return sResultBean;
            }

            //拿到ras_id
            String res = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
            LogUtil.i(TAG, "证书比对结果:" + res);
            TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);
            if (tagBean1.getErrno() == 0) {
                config_rasId = tagBean1.getData().getRasId();
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
                sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
                sResultBean.setTime(config_rasId);
            } else {
                sResultBean.setErrno(tagBean1.getErrno());
                sResultBean.setErrmsg(tagBean1.getErrmsg());
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

    private void initData(TagBean tagBean) {
        isPswTag = tagBean.getData().getIsPswTag();
        sErrmsg = tagBean.getErrmsg();
        config_password = tagBean.getData().getCfg().getPassword();
        newCert = tagBean.getData().getNewCert();
        hasCert = tagBean.getData().getHasCert();
        if (hasCert != null) {
            LogUtil.i(TAG, "message:" + sErrmsg
                    + " password:" + config_password
                    + " newCert:" + newCert
                    + " hasCert:" + hasCert);
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
