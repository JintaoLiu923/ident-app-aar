package com.hachi.publishplugin.activity.ras15693;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.activity.base.BasePlugin4Vail;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

/**
 * 用于设置认证失败次数限制
 */
public class Ras15693AflmtPlugin extends BasePlugin4Vail implements BasePlugin.DetailListener {

    private static final String TAG = "Ras15693AflmtPlugin";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private String rasId;

    public ResultBean vali(Context context, Tag tag, String key, String EToken, IdentLogReqBean log) {
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
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            nfc.close();
            return sResultBean;
        }

        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.i(TAG, "随机数 --> " + randomStr);

        getDetailRequest(uid, randomStr, null, EToken, this);

        if (sResultBean.getErrno() != 0) {
            nfc.close();
            return sResultBean;
        }

        if (TextUtils.isEmpty(rasId)) {
            sResultBean.setTime(rasId);
        }

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

        ///设置密码认证失败次数限制
        if (!nfc.writeByte((byte) 0x21, new byte[]{0x00, 0x00, 0x00, 0x01})) {
            nfc.close();
        }

        return sResultBean;
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
