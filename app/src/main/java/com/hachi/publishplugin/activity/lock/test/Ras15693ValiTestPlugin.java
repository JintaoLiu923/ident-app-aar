package com.hachi.publishplugin.activity.lock.test;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin4Vail;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.LockTagBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.InterpolationUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

/**
 * 验证组件
 */
public class Ras15693ValiTestPlugin extends BasePlugin4Vail {
    private static final String TAG = "Ras15693ValiPlugin";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private String rasId;
    private static byte readKey = 0x00, writeKe = 0x00, customItsp = 0x40, customIntSel = 0x00; //可设定的参数值
    byte[] rdmITSP = new byte[]{0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40};

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

        //获取随机数
        byte[] random = nfc.getRandom();
        if (random == null || random.length == 0) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            return sResultBean;
        }

        byte[] pwd = InterpolationUtils.getVerifyPwd(new byte[]{0x00, 0x00}, random, customIntSel, customItsp);
        LogUtil.d(TAG, "pwd --> " + OperationUtil.bytesToHexString(pwd));
        if (!nfc.verifyPwd(pwd)) {
            LogUtil.d(TAG, "密码验证失败...");
            nfc.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            return sResultBean;
        }
        LogUtil.d(TAG, "密码验证成功...");

        String pad = nfc.readOneBlock((byte) 0x80);
        LogUtil.i(TAG, "验证读取状态位开始:" + pad);

        byte[] oflags = getOFLAGS(EncryptUtils.hexString2Bytes(pad), (byte) 0x00, (byte) 0x40);
        String decodePad = OperationUtil.bytesToHexString(oflags).substring(0, 2);
        LogUtil.d(TAG, "oflags --> " + decodePad);

        if (!TextUtils.isEmpty(decodePad)) {
            LockTagBean lockDataBean = new LockTagBean();
            lockDataBean.setRasPad(decodePad);
            sResultBean.setLockData(lockDataBean);
        }

        sResultBean.setErrmsg("读取成功");
        return sResultBean;
    }
}
