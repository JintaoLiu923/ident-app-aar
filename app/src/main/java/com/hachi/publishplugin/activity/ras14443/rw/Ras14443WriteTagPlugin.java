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
import com.hachi.publishplugin.interfaces.service.IWriteTagService;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.PutLogUtil;

public class Ras14443WriteTagPlugin extends BasePlugin implements IWriteTagService, BasePlugin.DetailListener {
    private static final String TAG = "Ras15693WriteTagPlugin";
    protected static String sErrmsg;
    protected static String config_password = "";
    protected static String config_rasId = "";

    public ResultBean writeTag(Context context, Tag tag, String key, String EToken, int position, String content) {
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
        getDetailRequest(uid, null, null, EToken, this);

        NFCA nfc = new NFCA(tag);
        byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

        //验证密码
        if (!nfc.authenticate(pwd)) {
            nfc.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            return sResultBean;
        }

        boolean writePage = nfc.writePage(position, EncryptUtils.hexString2Bytes(content));

        if (!writePage) {
            sResultBean.setErrno(TagErrorEnum.WRITE_TAG_FAIL.getCode());
            sResultBean.setErrmsg(TagErrorEnum.WRITE_TAG_FAIL.getDescription());
            return sResultBean;
        }

        sResultBean.setErrno(TagErrorEnum.WRITE_TAG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.WRITE_TAG_SUCCESS.getDescription());
        return sResultBean;
    }

    private static void initData(TagBean tagBean) {
        config_password = tagBean.getData().getCfg().getPassword();
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
