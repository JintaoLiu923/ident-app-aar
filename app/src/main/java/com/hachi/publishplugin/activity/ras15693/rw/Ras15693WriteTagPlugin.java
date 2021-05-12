package com.hachi.publishplugin.activity.ras15693.rw;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.PutLogUtil;

import lombok.Getter;
import lombok.Setter;

public class Ras15693WriteTagPlugin extends BasePlugin implements BasePlugin.DetailListener {
    private static final String TAG = "Ras15693WriteTagPlugin";
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
    private TagBean mTagBean;

    public static ResultBean writeTag(Context context, Tag tag, String key, String EToken, int position, String content, String writeType) {
        mContext = context;
        sResultBean = new ResultBean();

        //读取UID
        NfcVTmp nfc = new NfcVTmp(tag);
        if (nfc == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }

        uid = nfc.getUID();
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            nfc.close();
            return sResultBean;
        }
        sResultBean.setUid(uid);

        //获取随机数
        byte[] random = nfc.getRandom();

        if (random == null || random.length == 0) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            return sResultBean;
        }

        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.i(TAG, "随机数 --> " + randomStr);

        //获取配置信息
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
        boolean writeResult;

        if (!TextUtils.isEmpty(writeType)) {
            if (writeType.equals("writeByte")) {
                writeResult = nfc.writeByte(ByteEnum.match(position), EncryptUtils.hexString2Bytes(content));
            } else {
                writeResult = nfc.writeBlock(position, EncryptUtils.hexString2Bytes(content));
            }
            if (!writeResult) {
                sResultBean.setErrno(TagErrorEnum.WRITE_TAG_FAIL.getCode());
                sResultBean.setErrmsg(TagErrorEnum.WRITE_TAG_FAIL.getDescription());
                return sResultBean;
            }
        }

        sResultBean.setErrno(TagErrorEnum.WRITE_TAG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.WRITE_TAG_SUCCESS.getDescription());

        return sResultBean;
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

    @Override
    public void error(TagBean tagBean) {
        sResultBean.setErrno(tagBean.getErrno());
        sResultBean.setErrmsg(tagBean.getErrmsg());
    }

    @Override
    public void success(TagBean tagBean) {
        initData(tagBean);
    }

    enum ByteEnum {
        OFMSEL(0, "OFMSEL", (byte) 0x00),
        AUTH0(1, "AUTH0", (byte) 0x01),
        MODE(2, "MODE", (byte) 0x02),
        AFLMT1(3, "AFLMT1", (byte) 0x03);

        @Getter
        @Setter
        private int position;
        @Getter
        @Setter
        private String name;
        @Getter
        @Setter
        private byte byteId;

        ByteEnum(int position, String name, byte byteId) {
            this.position = position;
            this.name = name;
            this.byteId = byteId;
        }

        public static byte match(int position) {
            ByteEnum[] values = ByteEnum.values();
            for (ByteEnum value : values) {
                if (value.getPosition() == position) {
                    return value.getByteId();
                }
            }
            return 0;
        }
    }
}
