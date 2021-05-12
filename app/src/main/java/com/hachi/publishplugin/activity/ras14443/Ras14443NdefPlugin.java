package com.hachi.publishplugin.activity.ras14443;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.bean.DataBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.EncryptUtils;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.NFCA;
import com.hachi.publishplugin.utils.OperationUtil;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;


/**
 * NDEF自定义组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class Ras14443NdefPlugin extends BasePlugin4Tag {
    private static final String TAG = "Ras14443NdefPlugin";
    private static String sNdefEncrypt;

    @DoNotRename
    public static ResultBean rasTagWriteNdef(Context context, Tag tag, String key, String EToken, String mobile, String password, String ndef, boolean isStr) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始NDEF自定义流程");

        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }

        //初始化定位
        initLocation(context, key);

        //鉴权
        if (!checkLogin(EToken, mobile, password)) {
            return sResultBean;
        }

        //获取标签Uid
        uid = GlobelRasFunc.readUid14443(tag);
        String sUid = uid.substring(0, 2);
        if (sUid.equals("53")) {
            uid = GlobelRasFunc.readUidF8213(tag);
        }

        if (TextUtils.isEmpty(uid)) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            return sResultBean;
        }
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);
        OkHttp okHttp = new OkHttp(context);

        SharedPreferences sharedPreferences1 = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences1.edit();
        edit.clear();
        edit.apply();

        okHttp.getFromInternetById(Constant.DETAIL_URL + "?uid=" + uid + "&ndef=" + ndef, EToken);
        boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);
        ResultBean resultBean = new ResultBean();

        if (!flag1) {
            resultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return resultBean;
        }

        String detailData = sharedPreferences1.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "温度配置-->Detail接口返回数据:" + detailData);
        TagBean tagBean = new ParseJson().Json2Bean(detailData, TagBean.class);
        if (tagBean.getErrmsg().equals("no db")) {
            resultBean.setErrno(TagErrorEnum.IVALID_TAG.getCode());
            return resultBean;
        } else if (tagBean.getErrno() == 501) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.DATA, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            resultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return resultBean;
        } else if (tagBean.getErrno() != 0) {
            resultBean.setErrno(tagBean.getErrno());
            resultBean.setErrmsg(tagBean.getErrmsg());
            return resultBean;
        }

        //解析数据
        initData(tagBean);

        NFCA nfca = new NFCA(tag);
//        ndef = "";
        //写入NDEF

        byte[] ndefMessage = EncryptUtils.hexString2Bytes(sNdefEncrypt);
        LogUtil.d(TAG, "ndefMessage --> " + Arrays.toString(ndefMessage));

        boolean ndefFlag = false;
        for (int i = 0; i < 3; i++) {
            ndefFlag = nfca.writeNDEF(EncryptUtils.hexString2Bytes(ndef));
            if (!ndefFlag) {
                ndefFlag = false;
                break;
            }
        }

        if (!ndefFlag) {
            sResultBean.setErrno(TagErrorEnum.NDEF_WRITE_FAILED.getCode());
            PutLogUtil.putResult(context, uid, EToken, ActTypeEnum.ACT_TYPE_写NDEF, Constant.FAIL);
            return resultBean;
        }

        sResultBean.setErrno(TagErrorEnum.SUCCESS.getCode());
        return sResultBean;
    }

    @DoNotRename
    public static ResultBean rasTagReadNdef(Context context, Tag tag, String key, String EToken, String mobile, String password) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始NDEF读取流程");

        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }

        //初始化定位
        initLocation(context, key);

        //鉴权
        if (!checkLogin(EToken, mobile, password)) {
            return sResultBean;
        }

        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始发证流程");

        //读取标签Uid
        uid = GlobelRasFunc.readUid14443(tag);
        if (TextUtils.isEmpty(uid)) {
            PutLogUtil.putResult(context, null, EToken, ActTypeEnum.ACT_TYPE_读uid, Constant.FAIL);
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            return sResultBean;
        }
        LogUtil.i(TAG, "标签id --> " + uid);
        sResultBean.setUid(uid);

        NFCA nfca = new NFCA(tag);
        byte[] bytes1 = nfca.readPages(3);
        String str1 = OperationUtil.bytesToHexString(bytes1);
        byte[] bytes2 = nfca.readPages(7);
        String str2 = OperationUtil.bytesToHexString(bytes2);
        byte[] bytes3 = nfca.readPages(11);
        String str3 = OperationUtil.bytesToHexString(bytes3);

        DataBean dataBean = new DataBean();
        dataBean.setNdef(str1 + str2 + str3);
        sResultBean.setData(dataBean);
        sActTypeEnum = ActTypeEnum.ACT_TYPE_写NDEF;
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }

        GlobelRasFunc.musicPlay(context, 1);
        return sResultBean;
    }

    /**
     * 解析获取到的数据
     *
     * @param tagBean
     */
    private static void initData(TagBean tagBean) {
        //解析
        sNdefEncrypt = tagBean.getData().getNdef();
    }
}
