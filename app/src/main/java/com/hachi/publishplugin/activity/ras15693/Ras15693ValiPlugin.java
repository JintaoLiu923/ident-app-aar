package com.hachi.publishplugin.activity.ras15693;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.hachi.publishplugin.activity.ConfigTmpPlugin;
import com.hachi.publishplugin.activity.GlobelHttpFunc;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin4Vail;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.LockTagBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.bean.TmpBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 * 验证组件
 */
public class Ras15693ValiPlugin extends BasePlugin4Vail {
    private static final String TAG = "Ras15693ValiPlugin";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private String rasId;

    public ResultBean vali(Context context, Tag tag, TagBean tagBean, String key, String EToken, String mobile, String password, IdentLogReqBean log) {
        mContext = context;
        sResultBean = new ResultBean();

        //读取UID
        NfcVTmp nfc = new NfcVTmp(tag);

        if (nfc == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            nfc.close();
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
        sResultBean.setUid(uid);

        byte[] information = nfc.getInfoRmation();
        String strInfo = OperationUtil.bytesToHexString(information);

        if (!TextUtils.isEmpty(strInfo)) {
            sResultBean.setInfo(strInfo);
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
//        //获取配置信息
        OkHttp okHttp = new OkHttp(context);

        initData(tagBean);

        if (sResultBean.getErrno() != 0) {
            nfc.close();
            return sResultBean;
        }

        if (TextUtils.isEmpty(rasId)) {
            sResultBean.setTime(rasId);
        }

        //获取标志位信息
        String r_Read_begin = certDecode == null ? "" : certDecode.substring(0, 2);
        String r_Read_over = certDecode == null ? "" : certDecode.substring(2, 4);
        String w_Write_begin = certEncrypt == null ? "01" : certEncrypt.substring(0, 2);
        String w_Write_over = certEncrypt == null ? "00" : certEncrypt.substring(2, 4);

        if (isPswTag != null && isPswTag) {
            sResultBean.setErrno(TagErrorEnum.IVALID_TAG.getCode());
            nfc.close();
            return sResultBean;
        }

        //密码认证
        byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

        //验证密码，若验证不通过，则直接返回错误
        if (!nfc.verifyPwd(pwd)) {
            nfc.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            return sResultBean;
        }

        //读取标志位
        String status = nfc.readStatus(14);
        LogUtil.d(TAG, "status --> " + status);
        if (TextUtils.isEmpty(status) || TextUtils.isEmpty(r_Read_begin)) {
            nfc.close();
            sResultBean.setErrno(TagErrorEnum.FLAG_READ_FAILED.getCode());
            return sResultBean;
        }

        if (status.equals(r_Read_begin)) {
            //表示上次写入断开
            //直接写入新证书
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写证书, "ok");
            boolean certFlag = false;
            for (int i = 0; i < 3; i++) {
                certFlag = nfc.writeCert(certEncrypt.substring(4), certDecode.substring(4), OperationUtil.stringToByte(w_Write_begin)
                        , OperationUtil.stringToByte(w_Write_over), 13, 14);
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
            String pad = nfc.readOneBlock((int) 0x80);
            LogUtil.i(TAG, "验证读取状态位开始:" + pad);
            if (TextUtils.isEmpty(pad)) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
                return sResultBean;
            }
            pad = pad.toUpperCase();

            edit.clear();
            edit.apply();

            //对状态位进行解码
//            okHttp.getFromInternetById(Constant.DECODE_URL + "?uid=" + uid + "&encodeTxt=" + pad);
//            boolean flag3 = sharedPreferences.getBoolean(Constant.FLAG, false);
//            if (!flag3) {
//                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
//                nfc.close();
//                return sResultBean;
//            }
//            String res1 = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
//            LogUtil.d(TAG, "res --> " + res1);
//            JSONObject jsonObject = JSONObject.parseObject(res1);
//            String decodePad = jsonObject.getString("data");

            SharedPreferences sharedPreferences2 = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
            SharedPreferences.Editor edit2 = sharedPreferences2.edit();
            edit2.clear();
            edit2.apply();

            //新证书校验
            LogUtil.i(TAG, "读取状态位:" + pad);
            okHttp.getFromInternetById(Constant.VERIFY_URL + "?uid=" + uid
                    + "&cert3=" + newCert + "&pad=" + pad, EToken);

            LogUtil.i(TAG, "验证比对请求结束");

            boolean flag = sharedPreferences2.getBoolean(Constant.FLAG, false);
            if (!flag) {
                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                nfc.close();
                return sResultBean;
            }

            //修改标志位
            nfc.writeStatus(OperationUtil.stringToByte(w_Write_over), 14, 0);
            //拿到ras_id
            String res = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
            LogUtil.i(TAG, "证书比对结果:" + res);
            TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);

            nfc.close();

            if (!TextUtils.isEmpty(pad)) {
                LockTagBean lockTagBean = new LockTagBean();
                lockTagBean.setRasPad(pad.substring(0, 2));
                sResultBean.setLockData(lockTagBean);
            }

            if (tagBean1.getErrno() != 0) {
                nfc.close();
                sResultBean.setErrno(tagBean1.getErrno());
                sResultBean.setErrmsg(tagBean1.getErrmsg());
                return sResultBean;
            }

//            nfc.writeBytes(0x04,config_password)

            String rasId = tagBean1.getData().getRasId();
            //确认数据
            LogUtil.i(TAG, "验证确认新证书开始");
            sResultBean = GlobelHttpFunc.sendConfirm(context, uid, pad, newCert, EToken);
            if (sResultBean.getErrno() == 0) {
                sResultBean.setTime(config_rasId);
            }

            if (!TextUtils.isEmpty(strInfo)) {
                sResultBean.setInfo(strInfo);
            }

            edit2.clear();
            edit2.apply();

//            LockTagBean lockDataBean = new LockTagBean();
//            lockDataBean.setRasPad(pad.substring(0, 2));
//            lockDataBean.setDecodePad(decodePad);
//            sResultBean.setLockData(lockDataBean);

//            if (tagBean1.getData().getIsV5state() != null && tagBean1.getData().getIsV5state()) {
//                if (tagBean1.getData().getIsOpened() != null && tagBean1.getData().getIsOpened()) {
//                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
//                } else {
//                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
//                }
//            } else {
//                sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
//            }

            if (tagBean1.getData().getIsOpened() != null) {
                if (tagBean1.getData().getIsOpened()) {
                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
                } else {
                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
                }
            } else {
                sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
            }

            nfc.close();
            return sResultBean;
        } else if (status.equals(r_Read_over)) {
            //上次写入完毕
            LogUtil.i(TAG, "验证读取证书开始");
            //读取证书
            String cert = nfc.readOneBlock(13);
            if (TextUtils.isEmpty(cert)) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读证书, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.CERT_READ_FAILED.getCode());
                return sResultBean;
            }
            LogUtil.i(TAG, "验证读取证书结束:" + cert);

            //读取状态位
            LogUtil.i(TAG, "验证读取状态位开始");
            String pad = nfc.readOneBlock((byte) 0x80);
            LogUtil.i(TAG, "温控--->验证读取状态位:" + pad);
            if (TextUtils.isEmpty(pad)) {
                nfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
                return sResultBean;
            }
            pad = pad.toUpperCase();

            edit.clear();
            edit.apply();

//            //对状态位进行解码
//            okHttp.getFromInternetById(Constant.DECODE_URL + "?uid=" + uid + "&encodeTxt=" + pad);
//
//            boolean flag3 = sharedPreferences.getBoolean(Constant.FLAG, false);
//            if (!flag3) {
//                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
//                nfc.close();
//                return sResultBean;
//            }
//
//            String res1 = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
//            LogUtil.d(TAG, "res --> " + res1);
//            JSONObject jsonObject = JSONObject.parseObject(res1);
//            String decodePad = jsonObject.getString("data");

            //验证数据
            LogUtil.i(TAG, "验证比对请求开始");
            SharedPreferences sharedPreferences1 = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
            SharedPreferences.Editor edit1 = sharedPreferences1.edit();
            edit1.clear();
            edit1.apply();

            okHttp.getFromInternetById(Constant.VERIFY_URL + "?uid=" + uid
                    + "&cert3=" + cert + "&pad=" + pad, EToken);

            boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);
            if (!flag1) {
                nfc.close();
                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
                return sResultBean;
            }

            //拿到ras_id
            String res = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
            LogUtil.i(TAG, "证书比对结果:" + res);
            TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);

            if (!TextUtils.isEmpty(pad)) {
                LockTagBean lockDataBean = new LockTagBean();
                String subPad = pad.substring(0, 2);
                lockDataBean.setRasPad(subPad);
                sResultBean.setLockData(lockDataBean);
            }

            if (tagBean1.getErrno() != 0) {
                nfc.close();
                sResultBean.setTime(rasId);
                sResultBean.setErrno(tagBean1.getErrno());
                sResultBean.setErrmsg(tagBean1.getErrmsg());
                return sResultBean;
            }

            String rasId = null;
            if (tagBean1 != null && tagBean1.getData() != null) {
                rasId = tagBean1.getData().getRasId();
            }

            LogUtil.i(TAG, "验证写入新证书开始");
            //修改标志位
            nfc.writeStatus(OperationUtil.stringToByte(w_Write_begin), 14, 0);
            //写入新证书
            boolean certFlag = false;
            for (int i = 0; i < 3; i++) {
                certFlag = nfc.writeCert(
                        certEncrypt.substring(4),
                        certDecode.substring(4), OperationUtil.stringToByte(w_Write_begin)
                        , OperationUtil.stringToByte(w_Write_begin), 13, 14);
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
            nfc.writeStatus(OperationUtil.stringToByte(w_Write_over), 14, 0);
            nfc.close();
            //确认数据
            LogUtil.i(TAG, "验证确认新证书开始");
            sResultBean = GlobelHttpFunc.sendConfirm(context, uid, pad, newCert, EToken);
            if (sResultBean.getErrno() == 0) {
                sResultBean.setTime(config_rasId);
            }
            LogUtil.i(TAG, "验证确认新证书开始");


            if ((sResultBean.getErrno() == 000 || (sResultBean.getErrno() >= 200 && sResultBean.getErrno() < 300))
                    && isTmpTag) {
                ConfigTmpPlugin.rasTagReadTemps(context, tag, GlobelRasFunc.mMapKey, EToken, "", "", log, tagBean);
            }

            if (!TextUtils.isEmpty(rasId)) {
                sResultBean.setTime(rasId);
            }
            if (!TextUtils.isEmpty(strInfo)) {
                sResultBean.setInfo(strInfo);
            }

            edit.clear();
            edit.apply();

            if (!TextUtils.isEmpty(pad)) {
                LockTagBean lockTagBean = new LockTagBean();
                lockTagBean.setRasPad(pad.substring(0, 2));
                sResultBean.setLockData(lockTagBean);
            }

//            if (tagBean1.getData().getIsV5state() != null &&
//                    tagBean1.getData().getIsV5state()) {
//                if (tagBean1.getData().getIsOpened() != null &&
//                        tagBean1.getData().getIsOpened()) {
//                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
//                } else {
//                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
//                }
//            } else {
//                sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
//            }

            if (tagBean1.getData().getIsOpened() != null) {
                if (tagBean1.getData().getIsOpened()) {
                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_OPEN.getCode());
                } else {
                    sResultBean.setErrno(TagErrorEnum.VERIFY_TRUE_NOT_OPEN.getCode());
                }
            } else {
//                DataBean dataBean = new DataBean();
//                dataBean.setTagType(tagType);
//                sResultBean.setData(dataBean);
                sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
            }
            return sResultBean;
        } else {
            nfc.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读标志位, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.FLAG_FAIL.getCode());
            return sResultBean;
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
        isTmpTag = tagBean.getData().getIsTmpTag();
        isV5State = tagBean.getData().getIsV5state();
        rasId = tagBean.getData().getRasId();
        if (isPswTag != null) {
            LogUtil.i(TAG, "读取Config:"
                    + " password:" + config_password
                    + " newCert:" + newCert
                    + " hasCert:" + hasCert
                    + " rasId:" + config_rasId);
        }
    }

    @DoNotRename
    public static TmpBean showTmp(Tag detectedTag) {
        NfcVTmp nfc = new NfcVTmp(detectedTag);
        TmpBean tmpBean = new TmpBean();
        //读取参数
        byte[] temp = tmpRecord(nfc);

        if (temp == null) {
            LogUtil.i(TAG, "读取温度失败");
            return tmpBean;
        }
        LogUtil.i(TAG, "读取温度:" + OperationUtil.bytesToHexString(temp));
        float k = (float) -0.24134;
        float b = (float) 64.733;
        LogUtil.i(TAG, "温度K-B:" + k + " " + b);

        float nowTmp = k * (temp[0] & 0xff) + b;
        float highTmp = k * (temp[1] & 0xff) + b;
        float lowTmp = k * (temp[2] & 0xff) + b;
        nowTmp = (float) (Math.round(nowTmp * 100.0D) / 100L);
        highTmp = (float) (Math.round(highTmp * 100.0D) / 100L);
        lowTmp = (float) (Math.round(lowTmp * 100.0D) / 100L);
        LogUtil.i(TAG, "温度记录--->当前温度:" + nowTmp);
        LogUtil.i(TAG, "温度记录--->历史最高温度:" + highTmp);
        LogUtil.i(TAG, "温度记录--->历史最低温度:" + lowTmp);
        LogUtil.i(TAG, "温度记录--->循环次数:" + temp[3]);
        LogUtil.i(TAG, "温度记录--->指针:" + (temp[4] & 0xFF));
        //展示

        tmpBean.setNowtmp(nowTmp);
        tmpBean.setTmpmax(highTmp);
        tmpBean.setTmpmin(lowTmp);
        return tmpBean;
    }


    private static byte[] tmpRecord(NfcVTmp nfc) {
        byte[] configPage = EncryptUtils.hexString2Bytes(nfc.readOneBlock(0x25));

        if (!(configPage == null || configPage.equals(new byte[]{0x00, 0x00, 0x00, (byte) 0xff}))) {
            LogUtil.i(TAG, "读取0x25h:" + EncryptUtils.bytes2Hex(configPage));
            byte pnt = configPage[0];
            int pnt_int = pnt & 0xFF;
            LogUtil.i(TAG, "读取温度记录:pnt:" + pnt);
            byte cnt = configPage[1];
            if (pnt <= 184) {
                byte page = (byte) (0x26 + pnt_int / 4);
                byte frag = pnt_int == 0 ? pnt : (byte) ((pnt_int - 1) % 4);
                LogUtil.i(TAG, "读取温度记录:page:" + page);
                LogUtil.i(TAG, "读取温度记录:frag:" + frag);
                int tmppage = frag == 0x03 ? (page - 1) : page;
                LogUtil.i(TAG, "读取" + tmppage + "页温度");
                String str = nfc.readOneBlock(tmppage);
                if (str == null || str.isEmpty()) {
                    return new byte[5];
                }
                LogUtil.i(TAG, "读取当前温度页:" + str);
                return new byte[]{
                        EncryptUtils.hexString2Bytes(str)[frag]
                        , configPage[2], configPage[3], cnt, pnt
                };
            } else {
                return new byte[5];
            }
        } else {
            return new byte[5];
        }
    }
}
