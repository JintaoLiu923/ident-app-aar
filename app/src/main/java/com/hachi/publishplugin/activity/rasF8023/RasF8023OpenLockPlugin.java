package com.hachi.publishplugin.activity.rasF8023;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.interfaces.service.IValiService;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.Arrays;


public class RasF8023OpenLockPlugin extends BasePlugin implements IValiService {

    private static final String TAG = "RasF8023Plugin";
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

    @Override
    public ResultBean vali(Context context, Tag tag, String key, String EToken, IdentLogReqBean log) throws InterruptedException {
        mContext = context;
        sResultBean = new ResultBean();
        //读取UID
        NfcVTmp nfcv = new NfcVTmp(tag);
        if (nfcv == null) {
            nfcv.close();
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }

        uid = nfcv.getUID();
        if (TextUtils.isEmpty(uid)) {
            nfcv.close();
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        sResultBean.setUid(uid);

        //获取随机数
        LogUtil.d(TAG, "读取随机数...");
        byte[] random = nfcv.getRandom();
        if (random == null || random.length == 0) {
            nfcv.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            return sResultBean;
        }

        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.d(TAG, "随机数为-->" + randomStr);

        getDetailRequest(uid, randomStr, null, EToken, new DetailListener() {
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
            nfcv.close();
            return sResultBean;
        }

        //密码认证
        byte[] pwd = EncryptUtils.hexString2Bytes(config_password);

//        byte[] pwd = {0x00, 0x00, 0x00, 0x00};
        LogUtil.i(TAG, "随机数:" + Arrays.toString(random));

        //对密码和随机数进行异或运算
        byte[] xorResult = getXor(random, pwd);
        System.arraycopy(xorResult, 0, pwd, 0, xorResult.length);
        System.arraycopy(xorResult, 0, pwd, 2, xorResult.length);
        LogUtil.d(TAG, "验证密码...");
        if (!nfcv.verifyPwd(pwd)) {
            nfcv.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            return sResultBean;
        }

        LogUtil.d(TAG, "密码验证成功...");
//        byte[] write00 = new byte[]{0x00, 0x00, 0x00, 0x00};
////        if (!nfcv.writebyte((byte) 0x43, data)) {
////            LogUtil.d(TAG, "开锁失败");
////        }
//        if (!nfcv.writeBlock((byte) 0x43, write00)) {
//            LogUtil.d(TAG, "关锁失败");
//            sResultBean.setErrno(TagErrorEnum.OPEN_LOCK_FAIL.getCode());
//            return sResultBean;
//        }
//        String read = nfcv.readOneBlock(0x43);
//        LogUtil.d(TAG, "第一次 0x43 --> " + read);

        byte[] writeOpen = new byte[]{0x55, 0x00, 0x00, 0x00};

//        if (!nfcv.writebyte((byte) 0x43, data)) {
//            LogUtil.d(TAG, "开锁失败");
//        }

        //线程休眠100ms
        Thread.sleep(200);

        LogUtil.d(TAG, "电子锁开锁...");
        if (!nfcv.writeBlock((byte) 0x43, writeOpen)) {
            nfcv.close();
            LogUtil.d(TAG, "开锁失败...");
            sResultBean.setErrno(TagErrorEnum.OPEN_LOCK_FAIL.getCode());
            return sResultBean;
        }

        nfcv.close();
        sResultBean.setErrno(TagErrorEnum.OPEN_LOCK_SUCCESS.getCode());
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
    }
}
