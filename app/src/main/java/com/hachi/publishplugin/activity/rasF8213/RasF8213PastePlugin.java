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
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.List;

public class RasF8213PastePlugin extends BasePlugin {
    private static final String TAG = "RasF8213CopyPlugin";
    protected static Boolean isPswTag = false;//标签类型，false为RAS，true为14443或15693
    protected static String config_password = "";
    protected static String config_rasId = "";
    protected static String newCert = "";//明文证书
    protected static Boolean hasCert = false;
    protected static Boolean isTmpTag = false;
    private static String sErrmsg;

    public ResultBean paste(Context context, Tag tag, String key, String EToken, List<String> tagData) {
        mContext = context;
        sResultBean = new ResultBean();

        //判断标签类型
        String[] techList = tag.getTechList();
        boolean isNfcA = false;
        for (String tech : techList) {
            if (tech.contains(Constant.NFCA)) {
                isNfcA = true;
                break;
            }
        }

        if (!isNfcA) {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
            sResultBean.setErrmsg(TagErrorEnum.UN_SUPPORT.getDescription());
            return sResultBean;
        }

        //读取UID
        uid = GlobelRasFunc.readUidF8213(tag);
        LogUtil.d(TAG, "标签Id --> " + uid);
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        sResultBean.setUid(uid);

        //获取配置信息
        getDetailRequest(uid, "", null, EToken, new DetailListener() {
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
            return sResultBean;
        }

        byte[] pwd = new byte[4];
        if (TextUtils.isEmpty(config_password) || config_password.equals("0")) {
            sResultBean.setErrno(TagErrorEnum.DEFAULT.getCode());
            sResultBean.setErrmsg(TagErrorEnum.DEFAULT.getDescription());
            return sResultBean;
        }

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
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            return sResultBean;
        }

//        byte[] bytes1 = EncryptUtils.hexString2Bytes(tagData.get(0) + tagData.get(1) + tagData.get(2) + tagData.get(3));
//        boolean bytes1Bol1 = nfc.compWrite(0x00, bytes1);

//        byte[] bytes1 = EncryptUtils.hexString2Bytes(tagData.get(3) + tagData.get(4) + tagData.get(5) + tagData.get(6));
//        boolean bytes1Bol1 = nfc.compWrite(0x03, bytes1);
//
//        byte[] bytes2 = EncryptUtils.hexString2Bytes(tagData.get(7) + tagData.get(8) + tagData.get(9) + tagData.get(10));
//        boolean bytes1Bol2 = nfc.compWrite(0x07, bytes2);
//
//        byte[] bytes3 = EncryptUtils.hexString2Bytes(tagData.get(11) + tagData.get(12) + tagData.get(13) + tagData.get(14));
//        boolean bytes4 = nfc.compWrite(0x11, bytes3);
//
//        byte[] bytes3 = EncryptUtils.hexString2Bytes(tagData.get(11) + tagData.get(12) + tagData.get(13) + tagData.get(14));
//        boolean bytes4 = nfc.compWrite(0x11, bytes3);

//        byte[] bytes3 = EncryptUtils.hexString2Bytes(tagData.get(22));
//        boolean bytes4 = nfc.compWrite(22, bytes3);

        for (int i = 0; i < tagData.size(); i++) {
            if (i != 43 && i != 0 && i != 1) {
                byte[] bytes = EncryptUtils.hexString2Bytes(tagData.get(i));
                boolean writeSuccess = nfc.writePage(i, bytes);
                if (!writeSuccess) {
                    nfc.close();
                    sResultBean.setErrno(TagErrorEnum.WRITE_TAG_FAIL.getCode());
                    sResultBean.setErrmsg(TagErrorEnum.WRITE_TAG_FAIL.getDescription());
                    return sResultBean;
                }
            }
        }
        nfc.close();
        sResultBean.setErrno(TagErrorEnum.WRITE_TAG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.WRITE_TAG_SUCCESS.getDescription());
        return sResultBean;
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
