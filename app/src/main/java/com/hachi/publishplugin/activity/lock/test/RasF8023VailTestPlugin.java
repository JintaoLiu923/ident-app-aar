package com.hachi.publishplugin.activity.lock.test;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.LockTagBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.ElecEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.Arrays;
import java.util.HashMap;

/**
 * F8023 电子锁下盖 标签验证 return出状态位pad、电量elec、控制状态control
 */
public class RasF8023VailTestPlugin extends BasePlugin {

    private static final String TAG = "RasF8023VailTestPlugin";
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
    private static boolean isSendLog = false;

    public ResultBean vali(Context context, Tag tag, String key, String EToken, String userName) throws InterruptedException {
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
        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.i(TAG, "随机数:" + randomStr);

//            if (isPswTag != null && !isPswTag) {
        //密码认证

        byte[] pwd = {0x00, 0x00, 0x00, 0x00};
        LogUtil.i(TAG, "温控--->随机数:" + Arrays.toString(random));
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

        if (!nfcv.verifyPwd(pwd)) {
            nfcv.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            LogUtil.d(TAG, "密码验证失败！");
            return sResultBean;
        }

        LogUtil.d(TAG, "密码验证成功！");
        LockTagBean lockData = new LockTagBean();
        Thread.sleep(500);
        //读取状态位
        String pad = nfcv.readOneBlock((byte) 0x40).substring(0, 2);
//                String pad = nfcv.readOneBlock((int) 0x40);
        LogUtil.i(TAG, "pad:" + pad);
        if (!TextUtils.isEmpty(pad)) {
            pad = pad.toUpperCase();
            lockData.setRasPad(pad);
            sResultBean.setLockData(lockData);
        } else {
            nfcv.close();
//                    result.setUid(uid);
            sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
            return sResultBean;
        }
        LogUtil.i(TAG, "读取状态位:" + pad);

        //读取电量
        Thread.sleep(500);
        String elec = nfcv.readOneBlock((byte) 0x41);
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

//        //读取控制状态
//        String control = nfcv.readOneBlock((int) 0x43);
//        control = control.substring(0, 2);
//        byte finalControl = OperationUtil.stringToByte(control);
////                String pad = nfcv.readOneBlock((int) 0x40);
//        LogUtil.i(TAG, "control --> " + finalControl);
//        if (!TextUtils.isEmpty(control)) {
//            control = control.toUpperCase();
//
//            if (finalControl == 0x00) {
//                lockData.setStatus(0);
//            } else if (finalControl == 0x55) {
//                lockData.setStatus(1);
//            }
////                dataBean.setControl(control);
//            sResultBean.setLockData(lockData);
//        } else {
//            nfcv.close();
////                    result.setUid(uid);
//            sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
//            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
//            return sResultBean;
//        }
//        LogUtil.i(TAG, "读取控制状态 --> " + control);
        sResultBean.setErrmsg("读取成功");
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
