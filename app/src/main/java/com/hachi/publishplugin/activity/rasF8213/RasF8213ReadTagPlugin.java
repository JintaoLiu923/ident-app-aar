package com.hachi.publishplugin.activity.rasF8213;

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
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

public class RasF8213ReadTagPlugin extends BasePlugin implements IReadTagService, BasePlugin.DetailListener {
    private static final String TAG = "RasF8213CertPlugin";
    private static String config_password = "";
    private static Boolean hasCert = false;

    @Override
    public ResultBean readTag(Context context, Tag tag, String key, String EToken, int position) {
        mContext = context;
        sResultBean = new ResultBean();

        LogUtil.i(TAG, "开始发证流程");

        //读取标签Uid
        uid = GlobelRasFunc.readUidF8213(tag);
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);

        getDetailRequest(uid, null, null, EToken, this);

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
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
//        pwd[0] = OperationUtil.stringToByte(config_password.substring(0, 2));
//        pwd[1] = OperationUtil.stringToByte(config_password.substring(2, 4));
//        pwd[2] = OperationUtil.stringToByte(config_password.substring(4, 6));
//        pwd[3] = OperationUtil.stringToByte(config_password.substring(6));
        //密码认证
        NFCA nfc = new NFCA(tag);
//        byte[] pwd_old = nfc.readPages(0x2B);
//        LogUtil.d(TAG,"psw -->"+pwd_old);
        if (!nfc.authenticate(pwd)) {
            nfc.close();
//                    result.setUid(uid);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            return sResultBean;
        }

        byte[] bytes = nfc.readPages(position);
        String readContent = OperationUtil.bytesToHexString(bytes).substring(0, 8);

        if (TextUtils.isEmpty(readContent)) {
            sResultBean.setErrno(TagErrorEnum.READ_TAG_FAIL.getCode());
            sResultBean.setErrmsg(TagErrorEnum.READ_TAG_FAIL.getDescription());
            return sResultBean;
        }

        sResultBean.setErrno(TagErrorEnum.READ_TAG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.READ_TAG_SUCCESS.getDescription());

        sResultBean.setReadTagContent(readContent);
        return sResultBean;
        //}
    }

    private void initData(TagBean tagBean) {
        config_password = tagBean.getData().getCfg().getPassword();
        hasCert = tagBean.getData().getHasCert();
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
