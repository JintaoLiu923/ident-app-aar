package com.hachi.publishplugin.activity.base;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

public class TagNfcaPlugin extends BasePlugin implements BasePlugin.DetailListener {
    private static final String TAG = "Tag14443";
    protected static Boolean isPswTag = false;//标签类型，false为RAS，true为14443或15693
    protected static String config_password = "";
    protected static String config_rasId = "";
    protected static String newCert = "";//明文证书
    protected static Boolean hasCert = false;
    protected static Boolean isTmpTag = false;
    private static String sErrmsg;
    public NFCA mNfc;

    public TagNfcaPlugin(Tag tag, Context context, String EToken, TagNfcaListener listener) {
        sResultBean = new ResultBean();
        //读取标签Uid
        uid = GlobelRasFunc.readUid14443(tag);
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            listener.error(sResultBean);
        }
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);

        //获取配置信息
        getDetailRequest(uid, null, null, EToken, this);

        if (sResultBean.getErrno() != 0) {
            listener.error(sResultBean);
            return;
        }

        byte[] pwd = new byte[4];

        if (hasCert != null && hasCert) {
            pwd[0] = OperationUtil.stringToByte(config_password.substring(0, 2));
            pwd[1] = OperationUtil.stringToByte(config_password.substring(2, 4));
            pwd[2] = OperationUtil.stringToByte(config_password.substring(4, 6));
            pwd[3] = OperationUtil.stringToByte(config_password.substring(6));
        } else {
            pwd[0] = (byte) 0xFF;
            pwd[1] = (byte) 0xFF;
            pwd[2] = (byte) 0xFF;
            pwd[3] = (byte) 0xFF;
        }

        //密码认证
        mNfc = new NFCA(tag);

        if (!mNfc.authenticate(pwd)) {
            mNfc.close();
//                    result.setUid(uid);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            listener.error(sResultBean);
            return;
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
    }

    public interface TagNfcaListener {
        void error(ResultBean resultBean);

        void success(ResultBean resultBean);
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
