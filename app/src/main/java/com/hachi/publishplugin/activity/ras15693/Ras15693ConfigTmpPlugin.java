package com.hachi.publishplugin.activity.ras15693;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.bean.TempConfigBean;
import com.hachi.publishplugin.bean.TmpBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.enums.TagTypeEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 温控标签-测温参数配置
 * TMIN--超限模式,温度最小值
 * TMAX--超限模式,温度最大值
 * TINTX--超限模式,测温范围外时间间隔，单位：分
 * TINTN--超限模式，测温范围内时间间隔，单位：分
 * TMPK--温度系数K
 * TMPB--温度系数B
 */
public class Ras15693ConfigTmpPlugin extends BasePlugin4Tag {
    private static String TAG = "Ras15693ConfigTmpPlugin";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static Boolean isPswTag = false;
    private static Boolean isTmpTag = false;
    private static Integer tagType = 0;
    private static String config_password = "";
    private static String config_rasId = "";
    private static String newCert = "";//明文证书
    private static Boolean hasCert = false;
    private static TagBean mTagBean;

    public static ResultBean tempConfig(Context context, Tag tag, String EToken, TempConfigBean tempConfigBean) {
        mContext = context;
        sResultBean = new ResultBean();

        if (TextUtils.isEmpty(EToken)) {
            //未授权
            sResultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return sResultBean;
        }
        //判断标签类型
        sIsNfcV = false;
        initTechType(tag);

        if (!sIsNfcV) {
            sResultBean.setErrno(TagErrorEnum.INVALI_NFC_TYPE.getCode());
            return sResultBean;
        }

        //读取UID
        NfcVTmp nfcv = new NfcVTmp(tag);
        if (nfcv == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }

        uid = nfcv.getUID();
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            nfcv.close();
            return sResultBean;
        }
        uid = uid.toUpperCase();
        LogUtil.i(TAG, "温度配置-->UID:" + uid);
        sResultBean.setUid(uid);

        //获取随机数
        byte[] random = nfcv.getRandom();
        String randomStr;
        if (random == null || random.length == 0) {
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            return sResultBean;
        } else {
            randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        }

        LogUtil.i(TAG, "温度配置--->随机数:" + randomStr);

        //获取配置信息
        getDetailRequest(uid, randomStr, null, EToken, new DetailListener() {
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
            nfcv.close();
            return sResultBean;
        }

        if (isPswTag != null && isTmpTag != null && !isPswTag && (isTmpTag || (tagType != null && tagType == TagTypeEnum.TAGE_TYPE_15693温控.getId()))) {
            if (tempConfigBean == null) {
                tempConfigBean = new TempConfigBean();
                tempConfigBean.setTintn(mTagBean.getData().getCfg().getTintn());
                tempConfigBean.setTintx(mTagBean.getData().getCfg().getTintx());
                tempConfigBean.setTmax(mTagBean.getData().getCfg().getTmax());
                tempConfigBean.setTmin(mTagBean.getData().getCfg().getTmin());
            }
            LogUtil.i(TAG, "读取tempConfig --> " + tempConfigBean.toString());
            if (hasCert != null) {

                LogUtil.i(TAG, "读取Config:"
                        + " password:" + config_password
                        + " newCert:" + newCert
                        + " hasCert:" + hasCert
                        + " rasId:" + config_rasId);
            }

            //密码认证
            byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

            LogUtil.i(TAG, "温控--->随机数:" + Arrays.toString(random));
            if (random == null) {
                nfcv.close();
                sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
                return sResultBean;
            }

            //验证密码，若验证不成功，直接返回错误
            if (!nfcv.verifyPwd(pwd)) {
                nfcv.close();
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                return sResultBean;
            }

            //读取KB配置
            byte[] configPage = EncryptUtils.hexString2Bytes(nfcv.readOneBlock(0x1E));//qq todo 后续修正0x16 为 0x1E
            if (configPage == null) {
                sResultBean.setErrno(TagErrorEnum.TEMP_READ_KB_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读温控KB参数, Constant.FAIL);
                return sResultBean;
            }

            LogUtil.i(TAG, "读取0x1Eh --> " + EncryptUtils.bytes2Hex(configPage));
            //16h 2,3 温度系数 B  B = (16h_Byte1 * 256 + 16h_Byte0) / 100.0;
            // tmpk01: -1 * (bytes[3]*256+bytes[2])/ 10000.0,  //qq todo
            // tmpb01: (bytes[1]*256+bytes[0])/100.0,

            //1Eh 0,1 温度系数 K K = -1 * (16h_Byte1 * 256 + 16h_Byte0) / 10000.0;
            //1Eh 3 温度系数  温度系数 B  B = 1Eh_Byte3*10;
            //tmpk01: -1 * (bytes[1] * 256 + bytes[0]) / 10000.0,  //qq todo
            //tmpb01: bytes[3] * 10,
            float tmpk01 = -1 * ((configPage[1] & 0xFF) * 256 + (configPage[0] & 0xFF)) / 10000.0f;
//                float tmpb01 = ((configPage[1] & 0xFF) * 256 + (configPage[0] & 0xFF)) / 100.0f;
            float tmpb01 = (configPage[3] & 0xFF) * 10;
            LogUtil.i(TAG, "tmpk01:" + tmpk01 + "tmpb01:" + tmpb01 + "configPage[1]:" + configPage[1] + "configPage[0]:" + configPage[0]);

            //时钟缺陷，需要时钟补偿，读取时钟补偿系数，如果过要间隔1分钟的话，实际设置 60/（20h Byte3）
            configPage = EncryptUtils.hexString2Bytes(nfcv.readOneBlock(0x20));
            if (configPage == null) {
                sResultBean.setErrno(TagErrorEnum.TEMP_READ_KB_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读温控KB参数, Constant.FAIL);
                return sResultBean;
            }
            LogUtil.i(TAG, "读取0x20h --> " + EncryptUtils.bytes2Hex(configPage));
            float timeK = configPage[3] == 0 ? 4f : 60f / (float) (configPage[3] & 0xFF);

            //芯片工作模式
            boolean b = nfcv.writeByte((byte) 0x02, new byte[]{0x01, 0x01, 0x01, 0x01});//qq todo 后续修正0x16 为 0x1E
            if (!b) {
                sResultBean.setErrno(TagErrorEnum.CHIPM_WRITE_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写芯片工作模式, Constant.FAIL);
                return sResultBean;
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
                //记录了时间系数，如果过要间隔1分钟的话，实际设置 60/（20h Byte3）
                bytes[2] = (byte) (Float.parseFloat(tempConfigBean.getTintx()) * timeK);
                bytes[3] = (byte) (Float.parseFloat(tempConfigBean.getTintn()) * timeK);
                //String tmax_tmin_tintx_tintn = tempConfig.getTmax()+tempConfig.getTmin()+tempConfig.getTintx()+tempConfig.getTintn();
//                    byte[] bytes = OperationUtil.stringToBytes(tmax_tmin_tintx_tintn, tmax_tmin_tintx_tintn.length());

                LogUtil.i(TAG, "温控--->TMAX,TMIN,TINTX,TINTN:" + Arrays.toString(bytes));
                nfcv.writeConfigBlock(bytes, (byte) 0x04);
            }

            if (!nfcv.ResetAdc()) {
                nfcv.close();
                sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
                return sResultBean;
            }

            nfcv.close();
            //返回结果

            sResultBean.setErrno(TagErrorEnum.CFG_SUCCESS.getCode());
        } else {
            sResultBean.setErrno(TagErrorEnum.NOT_SUPPORT_TEMP.getCode());
        }
        return sResultBean;
    }

    private static void initData(TagBean tagBean) {
        mTagBean = tagBean;
        certEncrypt = tagBean.getData().getCertEncrypt();
        certDecode = tagBean.getData().getCertDecode();
        isPswTag = tagBean.getData().getIsPswTag();
        isTmpTag = tagBean.getData().getIsTmpTag();
        config_password = tagBean.getData().getCfg().getPassword();
        config_rasId = tagBean.getData().getRasId();
        newCert = tagBean.getData().getNewCert();
        hasCert = tagBean.getData().getHasCert();
    }


    public static ResultBean startOrStop15693Temp(Context context, Tag tag, String EToken, boolean open) {
        sResultBean = new ResultBean();
        mContext = context;
        if (TextUtils.isEmpty(EToken)) {
            //未授权
            sResultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return sResultBean;
        }
        sIsNfcV = false;
        //判断标签类型
        initTechType(tag);
        if (!sIsNfcV) {
            sResultBean.setErrno(TagErrorEnum.INVALI_NFC_TYPE.getCode());
            return sResultBean;
        }
        //读取UID
        NfcVTmp nfcv = new NfcVTmp(tag);
        if (nfcv == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        uid = nfcv.getUID();
        if (TextUtils.isEmpty(uid)) {
            nfcv.close();
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        uid = uid.toUpperCase();
        LogUtil.i(TAG, "温度Config-->标签id:" + uid);
        sResultBean.setUid(uid);

        //获取随机数
        byte[] random = nfcv.getRandom();
        String randomStr;
        if (random == null || random.length == 0) {
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            return sResultBean;
        } else {
            randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        }

        LogUtil.i(TAG, "温度配置--->随机数:" + randomStr);

        //获取配置信息
        getDetailRequest(uid, randomStr, null, EToken, new DetailListener() {
            @Override
            public void error(TagBean tagBean) {
                sResultBean.setErrno(tagBean.getErrno());
                sResultBean.setErrmsg(tagBean.getErrmsg());
            }

            @Override
            public void success(TagBean tagBean) {
                //解析数据
                initData2(tagBean);
            }
        });

        if (sResultBean.getErrno() != 0) {
            nfcv.close();
            return sResultBean;
        }


        if (isPswTag != null && isTmpTag != null && !isPswTag && (isTmpTag || (tagType != null && tagType == TagTypeEnum.TAGE_TYPE_15693温控.getId()))) {
            //密码认证
            byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

            LogUtil.i(TAG, "温控--->随机数:" + Arrays.toString(random));
            if (random == null) {
                nfcv.close();
                sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
                return sResultBean;
            }
            if (!nfcv.verifyPwd(pwd)) {
                nfcv.close();
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                return sResultBean;
            }

            //芯片工作模式
            byte data[] = open ? new byte[]{1, 1, 1, 1} : new byte[]{0, 0, 0, 0};
            boolean b = nfcv.writeByte((byte) 0x02, data);//qq todo 后续修正0x16 为 0x1E
            if (!b) {
                sResultBean.setErrno(TagErrorEnum.CHIPM_WRITE_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写芯片工作模式, Constant.FAIL);
                return sResultBean;
            }

            if (!nfcv.ResetAdc()) {
                nfcv.close();
                return sResultBean;
            }

            nfcv.close();
            //返回结果

            sResultBean.setErrno(TagErrorEnum.CFG_SUCCESS.getCode());
            return sResultBean;
        } else {
            sResultBean.setErrno(TagErrorEnum.NOT_SUPPORT_TEMP.getCode());
            return sResultBean;
        }
    }

    public static ResultBean readTemps(Context context, Tag tag, String EToken, TmpBean temp) {
        return readTemps(context, tag, EToken, temp, null);
    }

    public static ResultBean readTemps(Context context, Tag tag, String EToken, TmpBean temp, TagBean tagBean) {
        sResultBean = new ResultBean();
        map = new HashMap<>();
        mContext = context;

        if (TextUtils.isEmpty(EToken)) {
            //未授权
            sResultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return sResultBean;
        }
        //判断标签类型

        sIsNfcV = false;
        //判断标签类型
        initTechType(tag);

        if (!sIsNfcV) {
            sResultBean.setErrno(TagErrorEnum.INVALI_NFC_TYPE.getCode());
            return sResultBean;
        }

        //读取UID
        NfcVTmp nfcv = new NfcVTmp(tag);
        if (nfcv == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        uid = nfcv.getUID();

        if (TextUtils.isEmpty(uid)) {
            nfcv.close();
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        uid = uid.toUpperCase();
        LogUtil.i(TAG, "温度Config-->标签id:" + uid);
        sResultBean.setUid(uid);

        if (tagBean == null) {
            //获取随机数
            byte[] random = nfcv.getRandom();
            String randomStr;

            if (random == null || random.length == 0) {
                sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
                return sResultBean;
            } else {
                randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
            }
            LogUtil.i(TAG, "温度配置--->随机数:" + randomStr);

            //获取配置信息
            getDetailRequest(uid, randomStr, null, EToken, new DetailListener() {
                @Override
                public void error(TagBean tagBean) {
                    sResultBean.setErrno(tagBean.getErrno());
                    sResultBean.setErrmsg(tagBean.getErrmsg());
                }

                @Override
                public void success(TagBean tagBean) {
                    //解析数据
                    initData2(tagBean);
                }
            });

            if (sResultBean.getErrno() != 0) {
                nfcv.close();
                return sResultBean;
            }
        } else {
            initData2(tagBean);
        }

        if (isPswTag != null && isTmpTag != null && hasCert != null && !isPswTag && (isTmpTag || (tagType != null && tagType == TagTypeEnum.TAGE_TYPE_15693温控.getId()))) {
            LogUtil.i(TAG, "读取Config:"
                    + " password:" + config_password
                    + " newCert:" + newCert
                    + " hasCert:" + hasCert
                    + " rasId:" + config_rasId);

            //密码认证
            byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

            //验证密码，若验证不通过，直接返回错误
            if (!nfcv.verifyPwd(pwd)) {
                nfcv.close();
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                return sResultBean;
            }

            //读取KB配置
            byte[] configPage = EncryptUtils.hexString2Bytes(nfcv.readOneBlock(0x16));//qq todo 后续修正0x16 为 0x1E
            if (configPage == null) {
                sResultBean.setErrno(TagErrorEnum.TEMP_READ_KB_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读温控KB参数, Constant.FAIL);
                return sResultBean;
            }

            LogUtil.i(TAG, "读取0x1Eh:" + EncryptUtils.bytes2Hex(configPage));
            float tmpk01 = -1 * ((configPage[3] & 0xFF) * 256 + (configPage[2] & 0xFF)) / 10000.0f;
            float tmpb01 = ((configPage[1] & 0xFF) * 256 + (configPage[0] & 0xFF)) / 100.0f;
            LogUtil.i(TAG, "tmpk01:" + tmpk01 + "tmpb01:" + tmpb01 + "configPage[1]:" + configPage[1] + "configPage[0]:" + configPage[0]);

            temp.setUid(uid);
            temp.setRasId(config_rasId);
            temp.setTmpk01(tmpk01);
            temp.setTmpb01(tmpb01);

            //时钟缺陷，需要时钟补偿，读取时钟补偿系数，如果过要间隔1分钟的话，实际设置 60/（20h Byte3）
            configPage = EncryptUtils.hexString2Bytes(nfcv.readOneBlock(0x20));
            if (configPage == null) {
                sResultBean.setErrno(TagErrorEnum.TEMP_READ_KB_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读温控KB参数, Constant.FAIL);
                return sResultBean;
            }

            LogUtil.i(TAG, "读取0x20h:" + EncryptUtils.bytes2Hex(configPage));
            float timeK = configPage[3] == 0 ? 4f : 60f / (float) (configPage[3] & 0xFF);
            LogUtil.d(TAG, "timeK -->" + timeK);
            //读取TMAX,TMIN配置 //需要先密码认证
            configPage = EncryptUtils.hexString2Bytes(nfcv.readOneBlock(0x1F));
            if (configPage == null) {
                sResultBean.setErrno(TagErrorEnum.TEMP_READ_KB_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读温控KB参数, Constant.FAIL);
                return sResultBean;
            }
            LogUtil.i(TAG, "读取0x1Fh:" + EncryptUtils.bytes2Hex(configPage));
            float tmax = temp.getTmpk01() * (configPage[0] & 0xFF) + temp.getTmpb01();
            float tmin = temp.getTmpk01() * (configPage[1] & 0xFF) + temp.getTmpb01();
            temp.setTmax((float) (Math.round((tmax) * 10) / 10));
            temp.setTmin((float) (Math.round((tmin) * 10) / 10));
            temp.setTintx((int) Math.round((float) (configPage[2] & 0xFF) / timeK));
            temp.setTintn((int) Math.round(((float) (configPage[3] & 0xFF) / timeK)));

            //读取温度列表
            String strR = nfcv.readBlocks(0x25, 0x53 - 0x25 + 1);
            configPage = EncryptUtils.hexString2Bytes(strR);
            if (configPage == null) {
                sResultBean.setErrno(TagErrorEnum.TEMP_READ_LIST_FAILED.getCode());
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读取温度列表, Constant.FAIL);
                return sResultBean;
            }
            LogUtil.i(TAG, "读取0x25h:" + EncryptUtils.bytes2Hex(configPage));

            //25h configPage[0] 循环记录温度的指针，记录本次循环的最后一个字节地址值
            temp.setTmpmax((float) (Math.round((float) (tmpk01 * (configPage[3] & 0xFF) + tmpb01) * 100.0f) / 100.0f));
            temp.setTmpmin((float) (Math.round((float) (tmpk01 * (configPage[2] & 0xFF) + tmpb01) * 100.0f) / 100.0f));
            temp.setTmpcnt((int) configPage[1] & 0xFF);
            temp.setTmppnt((int) configPage[0] & 0xFF);
            for (int i = 0; i < (configPage[0] & 0xFF); i++) {
                temp.getList().add((Math.round((tmpk01 * (configPage[i + 4] & 0xFF) + tmpb01) * 100.0f) / 100.0f)); //temp = K * (ADC值) + B;
            }
            LogUtil.i(TAG, "temp:" + temp.toString());
            //鉴权
            if (!checkLogin(EToken, Constant.username, Constant.password)) {
                return sResultBean;
            }

            //TODO 发送温度日志
//            String json4temp = new Gson().toJson(temp);
//            map.put("uid", uid);
//            map.put("action", ActTypeEnum.ACT_TYPE_读取温度列表.getName());
//            map.put("tempList", json4temp + "");
//            GlobelHttpFunc.sendLog(map, EToken);

            PutLogUtil.putTempLog(context, uid, EToken, ActTypeEnum.ACT_TYPE_读取温度列表, temp);

            nfcv.close();
            //返回结果

            sResultBean.setErrno(TagErrorEnum.SUCCESS.getCode());
            return sResultBean;
        } else {
            sResultBean.setErrno(TagErrorEnum.NOT_SUPPORT_TEMP.getCode());
            return sResultBean;
        }
    }

    private static void initData2(TagBean tagBean) {
        certEncrypt = tagBean.getData().getCertEncrypt();
        certDecode = tagBean.getData().getCertDecode();
        isPswTag = tagBean.getData().getIsPswTag();
        isTmpTag = tagBean.getData().getIsTmpTag();
        tagType = tagBean.getData().getTagType();
        config_password = tagBean.getData().getCfg().getPassword();
        config_rasId = tagBean.getData().getRasId();
        newCert = tagBean.getData().getNewCert();
        hasCert = tagBean.getData().getHasCert();
    }
}

