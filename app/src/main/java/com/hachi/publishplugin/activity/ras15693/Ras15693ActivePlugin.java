package com.hachi.publishplugin.activity.ras15693;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.GlobelHttpFunc;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.bean.TempConfigBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.enums.TagTypeEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * 温控发证
 */
public class Ras15693ActivePlugin {
    private static final String TAG = "Ras15693ActivePlugin";
    private static String uid;
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static String ndef = "";//密文NDEF
    private static Boolean isPwdTag = false;
    private static Boolean isTmpTag = false;
    private static Integer tagType = 0;
    private static String config_password = "";
    private static String config_rasId = "";
    private static String config_rasIdEncrypt = "";
    private static String newCert = "";//明文证书
    private static Boolean hasCert = false;

    private static Map<String, Object> map = new HashMap<>();

    public static ResultBean start(Context context, Tag tag, String EToken) {
        ResultBean resultBean = new ResultBean();
        //读取UID
        NfcVTmp nfcv = new NfcVTmp(tag);
        if (nfcv == null) {
            resultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return resultBean;
        }
        uid = nfcv.getUID();
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context,null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            resultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            nfcv.close();
            return resultBean;
        }
        uid = uid.toUpperCase();
        LogUtil.i(TAG, "温度配置-->UID:" + uid);
        resultBean.setUid(uid);
        //获取随机数
        byte[] random = nfcv.getRandom();
        if (random == null || random.length == 0) {
            resultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            return resultBean;
        }
        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.i(TAG, "温度配置--->随机数:" + randomStr);

        SharedPreferences sharedPreferences1 = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        SharedPreferences.Editor edit1 = sharedPreferences1.edit();
        edit1.clear();
        edit1.apply();

        //获取配置信息
        OkHttp okHttp = new OkHttp(context);
        okHttp.getFromInternetById(Constant.SERVER_URL_V2 + "app/ras/detail/v2?uid=" + uid + "&random=" + randomStr, EToken);
        boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);

        if (flag1) {
            String detailData = sharedPreferences1.getString(Constant.RESPONSE_DATA, "");
            LogUtil.i(TAG, "温度配置-->Detail接口返回数据:" + detailData);
            TagBean tagBean = new ParseJson().Json2Bean(detailData, TagBean.class);
            if (tagBean.getErrmsg().equals("no db")) {
                resultBean.setErrno(TagErrorEnum.IVALID_TAG.getCode());
                return resultBean;
            } else if (tagBean.getErrno() == 501) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.DATA, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                resultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
                return resultBean;
            } else if (tagBean.getErrno() != 0) {
                resultBean.setErrno(tagBean.getErrno());
                resultBean.setErrmsg(tagBean.getErrmsg());
                return resultBean;
            }
            //解析
            initData(tagBean);

            TempConfigBean tempConfigBean = new TempConfigBean();
            if (isPwdTag != null && isTmpTag != null && !isPwdTag && (isTmpTag || (tagType != null && tagType == TagTypeEnum.TAGE_TYPE_15693温控.getId()))) {
                tempConfigBean = new TempConfigBean();
                tempConfigBean.setTintn(tagBean.getData().getCfg().getTintn());
                tempConfigBean.setTintx(tagBean.getData().getCfg().getTintx());
                tempConfigBean.setTmax(tagBean.getData().getCfg().getTmax());
                tempConfigBean.setTmin(tagBean.getData().getCfg().getTmin());
                LogUtil.i(TAG, "读取tempConfig:" + tempConfigBean.toString());
            }
            if (hasCert != null) {
                LogUtil.i(TAG, "读取Config:"
                        + " password:" + config_password
                        + " newCert:" + newCert
                        + " hasCert:" + hasCert
                        + " rasId:" + config_rasId);
            }

            //获取标志位信息
            String r_Read_begin = certDecode.substring(0, 2);
            String r_Read_over = certDecode.substring(2, 4);
            String w_Write_begin = certEncrypt.substring(0, 2);
            String w_Write_over = certEncrypt.substring(2, 4);

            if (isPwdTag != null && !isPwdTag) {
                //密码认证
                byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

                LogUtil.i(TAG, "温控--->随机数:" + Arrays.toString(random));
                if (random == null) {
                    nfcv.close();
                    resultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
                    return resultBean;
                }
                if (!nfcv.verifyPwd(pwd)) {
                    nfcv.close();
                    PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                    resultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                    return resultBean;
                }

                //todo 写入NDEF
                boolean ndefFlag = nfcv.writeTmpNedf(EncryptUtils.hexString2Bytes(ndef));
                if (!ndefFlag) {
                    resultBean.setErrno(TagErrorEnum.NDEF_WRITE_FAILED.getCode());
                    PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_写NDEF, Constant.FAIL);
                    return resultBean;
                }

                //读取状态位
                String pad = nfcv.readOneBlock((int) 0x80);
                LogUtil.i(TAG, "温控--->pad:" + pad);
                if (!pad.equals("")) {
                    pad = pad.toUpperCase();
                } else {
                    nfcv.close();
//                    result.setUid(uid);
                    resultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
                    PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
                    return resultBean;
                }
                LogUtil.i(TAG, "读取状态位:" + pad);

                //没有发证信息才写入时间戳
                if (hasCert != null && !hasCert) {
                    //写入时间戳
                    boolean rasIdFlag = false;
                    for (int i = 0; i < 3; i++) {
                        rasIdFlag = nfcv.writeRasId2Tmp(config_rasIdEncrypt, 11);
                        if (rasIdFlag) {
                            break;
                        }
                    }
                    if (!rasIdFlag) {
                        nfcv.close();
                        resultBean.setErrno(TagErrorEnum.BIZ_WRITE_FAILED.getCode());
                        PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_写时间戳, Constant.FAIL);
                        return resultBean;
                    }
                    LogUtil.i(TAG, "温控--->写入时间戳结束");
                }

                //写入证书
                nfcv.writeStatus(OperationUtil.stringToByte(w_Write_begin), 14, 0); //qq a
                boolean writeFlag = false;
                for (int i = 0; i < 3; i++) {
                    writeFlag = nfcv.writeCert(certEncrypt.substring(4), certDecode.substring(4), OperationUtil.stringToByte(w_Write_begin)
                            , OperationUtil.stringToByte(w_Write_over), 13, 14);
                    if (writeFlag) {
                        break;
                    }
                }
                if (!writeFlag) {
                    nfcv.close();
                    resultBean.setErrno(TagErrorEnum.CERT_WRITE_FAILED.getCode());
                    PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_写证书, Constant.FAIL);
                    return resultBean;
                }
                nfcv.writeStatus(OperationUtil.stringToByte(w_Write_over), 14, 0); //qq a

                //写入温度配置参数
                if (isTmpTag != null && isTmpTag) {

                    //读取KB配置
                    byte[] configPage = EncryptUtils.hexString2Bytes(nfcv.readOneBlock(0x16));//qq todo 后续修正0x16 为 0x1E
                    if (configPage == null) {
                        resultBean.setErrno(TagErrorEnum.TEMP_READ_KB_FAILED.getCode());
                        PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_读温控KB参数, Constant.FAIL);
                        return resultBean;
                    }
                    LogUtil.i(TAG, "读取0x1Eh:" + EncryptUtils.bytes2Hex(configPage));
                    //1Eh 0,1 温度系数 K K = -1 * (16h_Byte3 * 256 + 16h_Byte2) / 10000.0;
                    //1Eh 2,3 温度系数 B  B = (16h_Byte1 * 256 + 16h_Byte0) / 100.0;
                    double tmpk01 = -1 * ((configPage[3] & 0xFF) * 256 + (configPage[2] & 0xFF)) / 10000.0d;
                    double tmpb01 = ((configPage[1] & 0xFF) * 256 + (configPage[0] & 0xFF)) / 100.0d;

                    //芯片工作模式
                    boolean b = nfcv.writeByte((byte) 0x02, new byte[]{0x00, 0x00, 0x00, 0x00});//qq todo 后续修正0x16 为 0x1E,暂时关闭
                    if (!b) {
                        resultBean.setErrno(TagErrorEnum.CHIPM_WRITE_FAILED.getCode());
                        PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_写芯片工作模式, Constant.FAIL);
                        return resultBean;
                    }

                    LogUtil.i(TAG, "tmpk01:" + tmpk01);
                    LogUtil.i(TAG, "tmpb01:" + tmpb01);

                    //写入KB配置
//                if (!tempConfig.getTmpk().isEmpty() && !tempConfig.getTmpb().isEmpty()) {
                    //需要将TMPK和TMPB处理成tmpk1,tmpk0和tmpb1,tmpb0
//                    nfcv.writeConfigBlock()
//                }

                    //写入TMAX,TMIN,TINTX,TINTN配置
                    if (!TextUtils.isEmpty(tempConfigBean.getTintn()) && !TextUtils.isEmpty(tempConfigBean.getTintx()) &&
                            !TextUtils.isEmpty(tempConfigBean.getTmin()) && !TextUtils.isEmpty(tempConfigBean.getTmax())) {
                        //TMAX,TMIN,TINTX,TINTN
                        byte[] bytes = new byte[4];
                        byte TMIN = (byte) ((Float.parseFloat(tempConfigBean.getTmin()) - tmpb01) / tmpk01);
                        byte TMAX = (byte) ((Float.parseFloat(tempConfigBean.getTmax()) - tmpb01) / tmpk01);

                        bytes[0] = TMAX;
                        bytes[1] = TMIN;
                        bytes[2] = Byte.parseByte(tempConfigBean.getTintx());
                        bytes[3] = Byte.parseByte(tempConfigBean.getTintn());
//                    String tmax_tmin_tintx_tintn = tempConfig.getTmax()+tempConfig.getTmin()+tempConfig.getTintx()+tempConfig.getTintn();
//                    byte[] bytes = OperationUtil.stringToBytes(tmax_tmin_tintx_tintn, tmax_tmin_tintx_tintn.length());

                        LogUtil.i(TAG, "温控--->TMAX,TMIN,TINTX,TINTN:" + Arrays.toString(bytes));
                        nfcv.writeConfigBlock(bytes, (byte) 0x04);
                    }
                }

                if (!nfcv.ResetAdc()) {
                    nfcv.close();
                    PutLogUtil.putResult(context,uid, EToken, ActTypeEnum.ACT_TYPE_供电开启, Constant.FAIL);
                    resultBean.setErrno(TagErrorEnum.RESET_ADC_FAILED.getCode());
                    return resultBean;
                }

                nfcv.close();
                //上传数据
                LogUtil.i(TAG, "温控--->发证确认上传信息开始");
                resultBean = GlobelHttpFunc.sendConfirm(context, uid, pad, newCert, EToken);
                if (resultBean.getErrno() == 0) {
                    resultBean.setErrno(TagErrorEnum.CERT_SUCCESS.getCode());
                    resultBean.setTime(config_rasId);
                }
                return resultBean;

            } else {
                resultBean.setErrno(TagErrorEnum.IVALID_TAG.getCode());
                return resultBean;
            }
        } else {
            resultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return resultBean;
        }
    }

    private static void initData(TagBean tagBean) {
        certEncrypt = tagBean.getData().getCertEncrypt();
        certDecode = tagBean.getData().getCertDecode();
        isPwdTag = tagBean.getData().getIsPswTag();
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
