package com.hachi.publishplugin.activity.ras15693.rw;

import android.content.Context;
import android.nfc.Tag;

import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.activity.base.TagNfcvPlugin;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.LogUtil;

public class Ras15693ReadTagPlugin extends BasePlugin {
    private static final String TAG = "Ras15693ReadTagPlugin";
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

    public static ResultBean readTag(Context context, Tag tag, String key, String EToken, int position) {
        mContext = context;
        sResultBean = new ResultBean();

        TagNfcvPlugin tagNfcv = new TagNfcvPlugin(tag, mContext, EToken, true, null, new TagNfcvPlugin.TagNfcvListener() {
            @Override
            public void error(ResultBean resultBean) {
                sResultBean = resultBean;
                LogUtil.d(TAG, "error...");
            }

            @Override
            public void success(ResultBean resultBean) {
                sResultBean = resultBean;
                LogUtil.d(TAG, "success...");
            }
        });

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
        }

        String readContent = tagNfcv.mNfc.readOneBlock(position);
        sResultBean.setReadTagContent(readContent);

        tagNfcv.mNfc.close();

        sResultBean.setErrno(TagErrorEnum.READ_TAG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.READ_TAG_SUCCESS.getDescription());

        return sResultBean;
    }

    public static ResultBean readTag(Context context, Tag tag, String key, String EToken, int startPosition, int endPosition) {
        mContext = context;
        sResultBean = new ResultBean();

        TagNfcvPlugin tagNfcv = new TagNfcvPlugin(tag, mContext, EToken, true, null, new TagNfcvPlugin.TagNfcvListener() {
            @Override
            public void error(ResultBean resultBean) {
                sResultBean = resultBean;
                LogUtil.d(TAG, "error...");
            }

            @Override
            public void success(ResultBean resultBean) {
                sResultBean = resultBean;
                LogUtil.d(TAG, "success...");
            }
        });

        if (sResultBean.getErrno() != 0) {
            return sResultBean;
        }

        String readContent1 = tagNfcv.mNfc.readBlocks(0x00, 0x16);
        String readContent2 = tagNfcv.mNfc.readBlocks(0x17, 0x24 - 0x17 + 1);
        String readContent3 = tagNfcv.mNfc.readBlocks(0x25, 0x53 - 0x25 + 1);
        LogUtil.d(TAG, "readContent --> " + readContent1 + ", readContent2 --> " + readContent2 + ", readContent3 --> " + readContent3);
        tagNfcv.mNfc.close();
        sResultBean.setTagData(readContent1 + readContent2 + readContent3);

        sResultBean.setErrno(TagErrorEnum.READ_TAG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.READ_TAG_SUCCESS.getDescription());
        return sResultBean;
    }
}
