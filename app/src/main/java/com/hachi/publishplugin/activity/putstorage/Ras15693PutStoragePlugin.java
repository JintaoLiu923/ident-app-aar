package com.hachi.publishplugin.activity.putstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.ApiNfcTag;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TbRasCfg;
import com.hachi.publishplugin.bean.TbRasCfgNokey;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NfcVTmp;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;
import com.hachi.publishplugin.utils.Xor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * F8023 电子锁下盖 标签入库
 */
public class Ras15693PutStoragePlugin extends BasePlugin4Tag {
    private ApiNfcTag mApiNfcTag;
    private TbRasCfgNokey mTbRasCfgNokey;
    private TbRasCfg mTagCfg;
    private static final String TAG = "Ras15693PutStoragePlugin";
    protected Map<String, String> mHeader = new HashMap<>();
    private static byte readKey = 0x00, writeKe = 0x00, customItsp = 0x40, customIntSel = 0x00; //可设定的参数值
    byte[] rdmITSP = new byte[]{0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40};
    private byte[] mNewPassword;

    public ResultBean putStorage(Context context, Tag tag, String key, String EToken, int tagType, int batchId) {

        mContext = context;
        sResultBean = new ResultBean();
        mApiNfcTag = new ApiNfcTag();
        mTbRasCfgNokey = new TbRasCfgNokey();
        mTagCfg = new TbRasCfg();

        //读取UID
        NfcVTmp nfc = new NfcVTmp(tag);
        if (nfc == null) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
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
        mTbRasCfgNokey.setUid(uid);
        sResultBean.setUid(uid);

        //获取随机数
        byte[] random = nfc.getRandom();
        if (random == null || random.length == 0) {
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读随机数, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.RANDOM_GET_FAILED.getCode());
            return sResultBean;
        }

        String randomStr = EncryptUtils.bytes2Hex(random).toUpperCase();
        LogUtil.i(TAG, "random --> " + randomStr);

        SharedPreferences sharedPreferences = initResourceSP(mContext);

        byte[] pwd = getVerifyPwd(new byte[]{0x00, 0x00}, random, customIntSel, customItsp);
        LogUtil.d(TAG, "pwd --> " + OperationUtil.bytesToHexString(pwd));
        if (!nfc.verifyPwd(pwd)) {
            LogUtil.d(TAG, "密码验证失败...");
            nfc.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_密码认证, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PWD_FAILED.getCode());
            return sResultBean;
        }
        LogUtil.d(TAG, "密码验证成功...");

        mNewPassword = new byte[]{0x00, 0x00, 0x00, 0x00};

        //读取状态位
        LogUtil.i(TAG, "验证读取状态位开始");
        String pad = nfc.readOneBlock((byte) 0x80).substring(0, 2);
        LogUtil.i(TAG, "验证读取状态位开始:" + pad);
        if (TextUtils.isEmpty(pad)) {
            nfc.close();
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_读状态位, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.PAD_READ_FAILED.getCode());
            return sResultBean;
        }
        pad = pad.toUpperCase();
        LogUtil.d(TAG, "pad --> " + pad);

        mTagCfg.setPassword("0000");
//        mTagCfg.setRk("00");
//        mTagCfg.setWk("00");
        mTagCfg.setAccess(true);
        mTagCfg.setAflmt("00");
        mTagCfg.setAflmtx("00");
        mTagCfg.setAuth0("20");
        mTagCfg.setIntsel(0);
//        mTagCfg.setItsp(OperationUtil.bytesToHexString(rdmITSP));
        mTagCfg.setItsp("40-40-40-40-40-40-40-40");
        mTagCfg.setOflag(pad);
        mTagCfg.setPad(pad);
        mTagCfg.setRk(OperationUtil.byteToString(readKey));
        mTagCfg.setWk(OperationUtil.byteToString(writeKe));
        mTagCfg.setUid(uid);

        mTagCfg.setTagType(tagType);

        if (tagType == 2) {
            mTagCfg.setIsTempTag(true);
        }

        if (tagType == 3) {
            mTagCfg.setIsV5state(true);
        }

//        mTagCfg.setTagType(2);
        mTagCfg.setBatchId(batchId);

        //新证书校验
        mApiNfcTag.setTagCfg(mTagCfg);
        LogUtil.d(TAG, "log-->" + JSON.toJSONString(mApiNfcTag));
        Gson gson = new Gson();
        String jsonData = gson.toJson(mApiNfcTag);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);

        postRequest(Constant.ADMIN_CFG_SAVE_URL, requestBody, initAdminHeader(EToken), new RequestListener() {
            @Override
            public void requestFail() {
                sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            }

            @Override
            public void error(ResultBean tagBean) {
                sResultBean.setErrno(tagBean.getErrno());
                sResultBean.setErrmsg(tagBean.getErrmsg());
            }

            @Override
            public void success(ResultBean tagBean) {
                sResultBean.setErrno(TagErrorEnum.PUT_STORAGE_SUCCESS.getCode());
            }
        });

        return sResultBean;
    }

    //生成随机Byte数
    public static byte[] generateRandom(int figure) {
        byte[] randomByte = new byte[figure];
        Random rdm = new Random();
        rdm.nextBytes(randomByte);
        if (randomByte != null) {
            LogUtil.d(TAG, "生成的随机数 --> " + OperationUtil.bytesToHexString(randomByte));
            return randomByte;
        }
        return null;
    }

    public static byte[] getPassword(byte[] PWD, byte WK, byte itsp) {
        //密码由设定决定
        //与WK异或
        byte[] encodedpwdByteData = new byte[]{(byte) (PWD[0] ^ WK), (byte) (PWD[1] ^ WK)};
        encodedpwdByteData = Xor.reverseArray(encodedpwdByteData);
        byte[] ipencodedData = enInterpolation(itsp, encodedpwdByteData, generateRandom(2));
        return ipencodedData;
    }

    //密码认证函数，需要在确定标签准备之后
    public static byte[] getVerifyPwd(byte[] PWD, byte[] randNum, byte INTSEL, byte itsp) {
        if (randNum == null) {
            return null;
        }
        //密码由设定决定
        byte[] encodedpwdByteData = new byte[]{(byte) (PWD[0] ^ randNum[0]), (byte) (PWD[1] ^ randNum[1])};
        encodedpwdByteData = Xor.reverseArray(encodedpwdByteData);   //异或过后将倒转过来的结果用来插值
        byte[] toINT = new byte[]{randNum[0], INTSEL}; //用来插值的材料
        byte[] ipencodedData = enInterpolation(itsp, encodedpwdByteData, toINT);
//        boolean result = ReaderCSharp.TagReaderManager.sharedManager().validatePassword(UID, ipencodedData, pwdId);
        return ipencodedData;
    }

    //加插值运算函数
    public static byte[] enInterpolation(byte ITSP, byte[] Data, byte[] Rand) {
        //将itsp转化为int类型的偏移量
        int[] itsp = new int[4];
        for (int j = 3; j > -1; j--) {
            itsp[j] = (int) ((ITSP & (0x03 << (j * 2))) >> (j * 2));
            if ((itsp[j] == 0) && (j == 3)) {
                // itsp[3] = 1;

            } else if ((itsp[j] == 0) && (j != 3)) {
                itsp[j] = itsp[j + 1];
            }
        }
        int[] newData = OperationUtil.byteToBinary(Data);
        int[] newRAND = OperationUtil.byteToBinary(Rand);
        int flag = itsp[3];
        //转换成List<int>集合
        int[] reverseData = Xor.reverseArray(newData);  //第一次反转

        List<Integer> list = new ArrayList<Integer>();

        for (int reverseDatum : reverseData) {
            list.add(reverseDatum);
        }

        //插入
        int i = 0;
        for (; (flag - i < 16) && (list.size() < 32); ) {
            list.add(flag, newRAND[i]);
            i++;
            flag = flag + itsp[3 - (i % 4)] + 1;
        }

        for (; list.size() < 32; ) {
            list.add(newRAND[i]);
            i++;
        }

        //从List<int>集合，再转换成数组
        int[] dataAIp = new int[list.size()];
        for (int j = 0; j < list.size(); j++) {
            dataAIp[j] = list.get(j);
        }

        int[] reverseDataAIp = Xor.reverseArray(dataAIp);
        //将做好插值的int数组转化成byte数组
        byte[] byteAfterIp = OperationUtil.binaryToByte(reverseDataAIp);
        byte[] reversedByteAfterIp = Xor.reverseArray(byteAfterIp);
        return reversedByteAfterIp;
    }
}
