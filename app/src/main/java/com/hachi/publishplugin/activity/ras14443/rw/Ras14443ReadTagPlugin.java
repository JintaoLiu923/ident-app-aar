package com.hachi.publishplugin.activity.ras14443.rw;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.interfaces.service.IReadTagService;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

public class Ras14443ReadTagPlugin extends BasePlugin implements IReadTagService, BasePlugin.DetailListener {
    private static final String TAG = "Ras14443ReadTagPlugin";
    protected static String newCert = "";//明文证书
    protected static Boolean hasCert = false;
    protected static Boolean isPswTag = false;
    protected static String sErrmsg;
    protected static String config_password = "";
    protected static String config_rasId = "";
    private static String certEncrypt = "";//密文证书
    private static String certDecode = "";
    private static String ndef = "";//密文NDEF
    private static String config_rk = "";
    private static String config_wk = "";
    private static String config_itsp = "";
    private static int config_intsel;

    public ResultBean readTag(Context context, Tag tag, String key, String EToken, int position) {
        mContext = context;
        sResultBean = new ResultBean();

        //读取标签Uid
        uid = GlobelRasFunc.readUid14443(tag);
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);

        //获取配置信息
        LogUtil.i(TAG, "发证配置信息请求开始");

        getDetailRequest(uid, null, null, EToken, this);

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
        }

        //密码认证
        NFCA nfca1 = new NFCA(tag);
        byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

        //验证密码
        if (!nfca1.authenticate(pwd)) {
            nfca1.close();
//                    result.setUid(uid);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            return sResultBean;
        }

        byte[] bytes = nfca1.readPages(position);
        String readContent = OperationUtil.bytesToHexString(bytes).substring(0, 8);

        if (TextUtils.isEmpty(readContent)) {
            sResultBean.setErrno(TagErrorEnum.READ_TAG_FAIL.getCode());
            sResultBean.setErrmsg(TagErrorEnum.READ_TAG_FAIL.getDescription());
            return sResultBean;
        }

        sResultBean.setReadTagContent(readContent);
        LogUtil.d(TAG, "readContent --> " + readContent);

        sResultBean.setErrno(TagErrorEnum.READ_TAG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.READ_TAG_SUCCESS.getDescription());

        return sResultBean;
    }

    private static void initData(TagBean tagBean) {
        certEncrypt = tagBean.getData().getCertEncrypt();
        certDecode = tagBean.getData().getCertDecode();
        ndef = tagBean.getData().getNdef();
        isPswTag = tagBean.getData().getIsPswTag();

        config_password = tagBean.getData().getCfg().getPassword();
        config_rk = tagBean.getData().getCfg().getRk();
        config_wk = tagBean.getData().getCfg().getWk();
        config_itsp = tagBean.getData().getCfg().getItsp();
        config_intsel = tagBean.getData().getCfg().getIntsel();
        config_rasId = tagBean.getData().getRasId();
        newCert = tagBean.getData().getNewCert();
        hasCert = tagBean.getData().getHasCert();

        if (isPswTag != null && hasCert != null) {
            LogUtil.i(TAG, "读取Config: RK" + config_rk
                    + " WK:" + config_wk
                    + " ITSP:" + config_itsp
                    + " INTSEL:" + config_intsel
                    + " password:" + config_password
                    + " newCert:" + newCert
                    + " hasCert:" + hasCert
                    + " rasId:" + config_rasId
                    + " certEcrypt:" + certEncrypt
                    + " certDecode:" + certDecode
                    + " ndef:" + ndef
                    + " isPswTag:" + isPswTag);
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
