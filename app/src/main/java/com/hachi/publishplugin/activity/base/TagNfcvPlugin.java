package com.hachi.publishplugin.activity.base;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

public class TagNfcvPlugin extends BasePlugin implements BasePlugin.DetailListener {
    private static final String TAG = "Tag15693";
    protected static String newCert = "";//明文证书
    protected static Boolean hasCert = false;
    protected static Boolean isPswTag = false;
    protected static String sErrmsg;
    protected static String config_password = "";
    protected static String config_rasId = "";
    protected static String newPswd = "";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static String ndef = "";//密文NDEF
    private static Boolean isTmpTag = false;
    private static Integer tagType = 0;
    private static String config_rasIdEncrypt = "";

    public final NfcVTmp mNfc;


    public TagNfcvPlugin(Tag tag, Context context, String EToken, boolean verifyPwd,String newPassword, TagNfcvListener listener) {
        mNfc = new NfcVTmp(tag);
        sResultBean = new ResultBean();
        if (mNfc == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            listener.error(sResultBean);
        }
        uid = mNfc.getUID();
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            listener.error(sResultBean);
            mNfc.close();
            return;
        }
        sResultBean.setUid(uid);

        //获取随机数
        byte[] random = mNfc.getRandom();

        if (random == null || random.length == 0) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            listener.error(sResultBean);
            mNfc.close();
            return;
        }

        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.i(TAG, "随机数 --> " + randomStr);

        getDetailRequest(uid, randomStr, newPassword, EToken, this);

        if (sResultBean.getErrno() != 0) {
            mNfc.close();
            listener.error(sResultBean);
            return;
        }

        if (verifyPwd) {
            //密码认证
            byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

            //验证密码，若验证不通过，则直接返回错误
            if (!mNfc.verifyPwd(pwd)) {
                mNfc.close();
                PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
                sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
                listener.error(sResultBean);
                return;
            }
        }

        listener.success(sResultBean);
    }

    @Override
    public void error(TagBean tagBean) {
        sResultBean.setErrno(tagBean.getErrno());
        sResultBean.setErrmsg(tagBean.getErrmsg());
    }

    @Override
    public void success(TagBean tagBean) {
        initData(tagBean);
        sResultBean.setTagBean(tagBean);
    }

    public interface TagNfcvListener {
        void error(ResultBean resultBean);

        void success(ResultBean resultBean);
    }

    private static void initData(TagBean tagBean) {
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
        newPswd = tagBean.getData().getNewPsw();
    }
}
