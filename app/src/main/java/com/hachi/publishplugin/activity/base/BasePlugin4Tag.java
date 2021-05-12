package com.hachi.publishplugin.activity.base;

import android.nfc.Tag;

import com.hachi.publishplugin.constant.Constant;

public class BasePlugin4Tag extends BasePlugin {
    private static final String TAG = "BasePlugin4Tag";
    protected static boolean sIsNfcA = false;
    protected static boolean sIsNfcV = false;

    /**
     * 判断标签类型 是NFCA还是NFCV
     *
     * @param tag 标签
     */
    protected static void initTechType(Tag tag) {
        String[] techList = tag.getTechList();
        for (String tech : techList) {
            if (tech.contains(Constant.NFCA)) {
                sIsNfcA = true;
                break;
            } else if (tech.contains(Constant.NFCV)) {
                sIsNfcV = true;
                break;
            }
        }
    }
//    protected int getTagType(Context context, int uid,String EToken){
//
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
//        SharedPreferences.Editor edit = sharedPreferences.edit();
//        edit.clear();
//        edit.apply();
//
//        //获取配置信息
//        OkHttp okHttp = new OkHttp(context);
//        okHttp.getFromInternetById(Constant.DETAIL_URL + "?uid=" + uid, EToken);
//        boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);
//
//        //若flag为false，表示未获取到数据，直接返回错误
//        if (!flag) {
//            sResultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
//            return sResultBean;
//        }
//
//        String responseData = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
//        LogUtil.i(TAG, "查询数据:" + responseData);
//        TagBean tagBean = new ParseJson().Json2Bean(responseData, TagBean.class);
//        if (tagBean.getErrmsg().equals("no db")) {
//            sResultBean.setErrno(TagErrorEnum.IVALID_TAG.getCode());
//            return sResultBean;
//        } else if (tagBean.getErrno() == 501) {
//            SharedPreferences sharedPreferences1 = mContext.getSharedPreferences("token", MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences1.edit();
//            editor.clear();
//            editor.apply();
//            sResultBean.setErrno(TagErrorEnum.UNLOGIN.getCode());
//            return sResultBean;
//        } else if (tagBean.getErrno() != 0) {
//            sResultBean.setErrno(tagBean.getErrno());
//            sResultBean.setErrmsg(tagBean.getErrmsg());
//            return sResultBean;
//        }
//        int tagType = tagBean.getData().getTagType();
//    }
}
