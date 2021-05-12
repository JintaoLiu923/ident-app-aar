package com.hachi.publishplugin.activity.rasF8213;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.GlobelHttpFunc;
import com.hachi.publishplugin.activity.GlobelRasFunc;
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

public class RasF8213ValiPlugin2 extends BasePlugin4Vail implements IValiService {
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

        byte[] pwd = new byte[4];
        pwd[0] = (byte) 0xFF;
        pwd[1] = (byte) 0xFF;
        pwd[2] = (byte) 0xFF;
        pwd[3] = (byte) 0xFF;

        //密码认证
        NFCA nfc = new NFCA(tag);
//        byte[] pwd_old = nfc.readPages(0x2B);
//        LogUtil.d(TAG,"psw -->"+pwd_old);

        byte[] bytesRasId = nfc.readPages(25);
        if (bytesRasId == null) {

            return sResultBean;
        }

        String str = OperationUtil.bytesToHexString(bytesRasId);
        String rasId = str.substring(0, 16);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();

        //获取配置信息
        OkHttp okHttp = new OkHttp(context);
        okHttp.getFromInternetById(Constant.DETAIL_URL + "?rasId=" + rasId, EToken);

        boolean flag = sharedPreferences.getBoolean(Constant.FLAG, true);

        if (!flag) {
            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return sResultBean;
        }

        String responseData = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "查询数据:" + responseData);
        TagBean tagBean = new ParseJson().Json2Bean(responseData, TagBean.class);

        if (tagBean.getErrmsg().equals("no db")) {
            sResultBean.setErrno(TagErrorEnum.IVALID_TAG.getCode());
            return sResultBean;
        } else if (tagBean.getErrno() == 501) {
            SharedPreferences sharedPreferences1 = mContext.getSharedPreferences("token", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences1.edit();
            editor.clear();
            editor.apply();
            sResultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return sResultBean;
        } else if (tagBean.getErrno() != 0) {
            sResultBean.setErrno(tagBean.getErrno());
            sResultBean.setErrmsg(tagBean.getErrmsg());
            return sResultBean;
        }

        //解析数据
        initData(tagBean);

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
//                result.setUid(uid);
                return sResultBean;
            }

            //新证书校验
            LogUtil.i(TAG, "验证比对请求开始");
            SharedPreferences sharedPreferences1 = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
            SharedPreferences.Editor edit1 = sharedPreferences1.edit();
            edit1.clear();
            edit1.apply();
            okHttp.getFromInternetById(Constant.VERIFY_URL + "?rasId=" + rasId + "&cert3=" + newCert, EToken);

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
                ResultBean resultBean = GlobelHttpFunc.sendConfirm2(context, rasId, newCert, EToken);
                if (resultBean.getErrno() != 0) {
                    nfc.close();
                    sResultBean.setErrno(resultBean.getErrno());
                    sResultBean.setErrmsg(resultBean.getErrmsg());
                    return sResultBean;
                }
                if (sResultBean.getErrno() == 0) {
                    sResultBean.setTime(config_rasId);
                }
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
                return sResultBean;
            }

            LogUtil.i(TAG, "验证读取证书结束");
            //验证数据
            LogUtil.i(TAG, "验证比对请求开始");

            SharedPreferences sharedPreferences1 = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
            SharedPreferences.Editor edit1 = sharedPreferences1.edit();
            edit1.clear();
            edit1.apply();

            okHttp.getFromInternetById(Constant.VERIFY_URL + "?rasId=" + rasId + "&cert3=" + cert, EToken);

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
                    return sResultBean;
                }

                nfc.writeStatus((byte) 0x00);
                nfc.close();
                //确认数据
                LogUtil.i(TAG, "验证确认新证书开始");

                ResultBean resultBean = GlobelHttpFunc.sendConfirm2(context, rasId, newCert, EToken);
                if (resultBean.getErrno() != 0) {
                    nfc.close();
                    sResultBean.setErrno(resultBean.getErrno());
                    sResultBean.setErrmsg(resultBean.getErrmsg());
                    return sResultBean;
                }
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
}
