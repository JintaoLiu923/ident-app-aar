package com.hachi.publishplugin.activity.rasF8023;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.hachi.publishplugin.activity.GlobelHttpFunc;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.IdentRasLogBean;
import com.hachi.publishplugin.bean.LockTagBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.ElecEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
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

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static android.content.Context.MODE_PRIVATE;

/**
 * F8023 电子锁下盖 标签验证 return出状态位pad、电量elec、控制状态control
 */
public class RasF8023VailPlugin extends BasePlugin {

    private static final String TAG = "RasF8023VailPlugin";
    protected static String newCert = "";//明文证书
    protected static Boolean hasCert = false;
    protected static Boolean isPswTag = false;
    protected static String sErrmsg;
    protected static String config_password = "";
    protected static String config_rasId = "";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static String ndef = "";//密文NDEF
    private static Boolean isTmpTag = false;
    private static Integer tagType = 0;
    private static String config_rasIdEncrypt = "";
    private static boolean isSendLog = true;

    public ResultBean vali(Context context, Tag tag, TagBean tagBean, String key, String EToken, String userName) {
        mContext = context;
        sResultBean = new ResultBean();
        mHeader = new HashMap<>();
        //读取UID
        NfcVTmp nfcv = new NfcVTmp(tag);
        if (nfcv == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        uid = nfcv.getUID();
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            nfcv.close();
            return sResultBean;
        }
        sResultBean.setUid(uid);
        //获取随机数
        byte[] random = nfcv.getRandom();
        if (random == null || random.length == 0) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            return sResultBean;
        }
        String randomStr = EncryptUtils.bytes2Hex(random);
        randomStr = randomStr.toUpperCase();
        LogUtil.i(TAG, "随机数 --> " + randomStr);

        SharedPreferences sharedPreferences1 = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences1.edit();
        edit.clear();
        edit.apply();

        //获取配置信息
        OkHttp okHttp = new OkHttp(context);

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
//            nfcv.writeStatus(OperationUtil.stringToByte(w_Write_begin), 14, 0); //qq a
//            if (random == null) {
//                nfcv.close();
//                sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
//                return sResultBean;
//            }

        //对密码和随机数进行异或运算
        byte[] xorResult = getXor(random, pwd);
        System.arraycopy(xorResult, 0, pwd, 0, xorResult.length);
        System.arraycopy(xorResult, 0, pwd, 2, xorResult.length);

        LogUtil.d(TAG, "pwd --> " + OperationUtil.bytesToHexString(pwd) + "xorResult --> " + OperationUtil.bytesToHexString(xorResult));

        if (!nfcv.verifyPwd(pwd)) {
            nfcv.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            return sResultBean;
        }

        //获取标签information
        byte[] infoRmation = nfcv.getInfoRmation();
        String strInfo = OperationUtil.bytesToHexString(infoRmation);

        LockTagBean lockData = new LockTagBean();
        //读取状态位
        String pad = nfcv.readOneBlock((byte) 0x40);
//                String pad = nfcv.readOneBlock((int) 0x40);
        LogUtil.i(TAG, "pad --> " + pad);
        if (!TextUtils.isEmpty(pad)) {
            pad = pad.toUpperCase();
            lockData.setRasPad(pad.substring(0, 2));
            sResultBean.setLockData(lockData);
        } else {
            nfcv.close();
//                    result.setUid(uid);
            sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
            return sResultBean;
        }
        LogUtil.i(TAG, "读取状态位 --> " + pad);

        //读取电量
        String elec = nfcv.readOneBlock((int) 0x41);
        elec = elec.substring(0, 2);
        byte finalElec = OperationUtil.stringToByte(elec);
        int matchElec = ElecEnum.match(finalElec);
        if (!TextUtils.isEmpty(elec)) {
            elec = elec.toUpperCase();
            lockData.setPower(matchElec);
            sResultBean.setLockData(lockData);
        } else {
            nfcv.close();
//                    result.setUid(uid);
            sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
            return sResultBean;
        }
        LogUtil.i(TAG, "读取电量 --> " + matchElec);


        //读取控制状态
        String control = nfcv.readOneBlock((int) 0x43);
        control = control.substring(0, 2);
        byte finalControl = OperationUtil.stringToByte(control);
//                String pad = nfcv.readOneBlock((int) 0x40);
        LogUtil.i(TAG, "control --> " + finalControl);
        if (!TextUtils.isEmpty(control)) {
            control = control.toUpperCase();

            if (finalControl == 0x00) {
                lockData.setStatus(0);
            } else if (finalControl == 0x55) {
                lockData.setStatus(1);
            }
//                dataBean.setControl(control);
            sResultBean.setLockData(lockData);
        } else {
            nfcv.close();
//                    result.setUid(uid);
            sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
            return sResultBean;
        }
        LogUtil.i(TAG, "读取控制状态 --> " + control);

        //新证书校验
        SharedPreferences sharedPreferences2 = mContext.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        SharedPreferences.Editor edit2 = sharedPreferences2.edit();
        edit2.clear();
        edit2.apply();

        nfcv.close();

        okHttp.getFromInternetById(Constant.VERIFY_URL + "?uid=" + uid
                + "&cert3=" + newCert + "&pad=" + pad, EToken);
        LogUtil.i(TAG, "验证比对请求结束");
        boolean flag2 = sharedPreferences2.getBoolean(Constant.FLAG, false);
        if (!flag2) {
            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return sResultBean;
        }

        //拿到ras_id
        String res = sharedPreferences2.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "证书比对结果:" + res);
        TagBean tagBean1 = new ParseJson().Json2Bean(res, TagBean.class);

        if (!TextUtils.isEmpty(strInfo)) {
            sResultBean.setInfo(strInfo);
        }
        if (tagBean1.getErrno() != 0) {
            sResultBean.setErrno(tagBean1.getErrno());
            sResultBean.setErrmsg(tagBean1.getErrmsg());
            return sResultBean;
        }

        String rasId = tagBean1.getData().getRasId();

        if (!TextUtils.isEmpty(rasId)) {
            sResultBean.setTime(rasId);
        }

        LogUtil.d(TAG, "isSendLog -->" + isSendLog);
        if (!isSendLog) {
            LogUtil.d(TAG, "距离上次发送电量警告不足10分钟");
        }

        if (matchElec <= 30 && isSendLog) {
            //设置定位监听
            if (mlocationClient != null) {
                mlocationClient.setLocationListener(mLocationListener);
                mlocationClient.startLocation();
            }
            Map<String, String> header = new HashMap<>();

            IdentLogReqBean identLogReqBean = new IdentLogReqBean();
            IdentRasLogBean identRasLog = new IdentRasLogBean();
            identRasLog.setCreateBy(1);
            identRasLog.setCreateByName(userName);
            identRasLog.setRasId(rasId);
            identRasLog.setAction(ActTypeEnum.ACT_TYPE_电子锁操作_电量告警.getName());
            identRasLog.setResult(sResultBean.getErrno() + "");

            identRasLog.setLat(GlobelRasFunc.latitude);
            identRasLog.setLng(GlobelRasFunc.longitude);
            identRasLog.setComment("{\"content\":null,\"master\":\"" + userName + "\",\"name\":\"\",\"sn\":null,\"type\":3,\"power\":" + matchElec + "}");
            identLogReqBean.setLog(identRasLog);

            header.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
            header.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);

            String jsonLog = new Gson().toJson(identLogReqBean);
            LogUtil.i(TAG, "json数据 --> " + jsonLog);
            RequestBody requestBody1 = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonLog);
            LogUtil.i(TAG, "requestBody --> " + requestBody1.toString());
            okHttp.sendLog(Constant.LOG_ADD_URL, requestBody1, header);
            isSendLog = false;
            CountDownTimer countDownTimer = new CountDownTimer(10 * 60 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    isSendLog = true;
                }
            }.start();
        }


        //确认数据
        LogUtil.i(TAG, "验证确认新证书开始");
        ResultBean resultBean = GlobelHttpFunc.sendConfirm(context, uid, pad, newCert, EToken);
        if (resultBean.getErrno() == 0) {
//            DataBean dataBean = new DataBean();
//            dataBean.setTagType(tagType);
//            sResultBean.setData(dataBean);
            sResultBean.setErrno(TagErrorEnum.VERIFY_SUCCESS.getCode());
        }
        return sResultBean;
    }

    /**
     * 对密码和随机数进行异或运算
     *
     * @param random 随机数
     * @param pwd    密码
     * @return
     */
    private byte[] getXor(byte[] random, byte[] pwd) {
        byte[] xorResult = new byte[2];
        xorResult[0] = (byte) (random[0] | pwd[0]);
        xorResult[1] = (byte) (random[1] | pwd[1]);
        return xorResult;
    }

    /**
     * 解析数据
     *
     * @param tagBean
     */
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
        tagType = tagBean.getData().getTagType();
    }
}
