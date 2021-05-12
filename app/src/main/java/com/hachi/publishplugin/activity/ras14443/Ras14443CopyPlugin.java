package com.hachi.publishplugin.activity.ras14443;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.hachi.publishplugin.activity.base.BasePlugin;
import com.hachi.publishplugin.activity.base.TagNfcaPlugin;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.interfaces.service.ICopyService;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.OperationUtil;

/**
 * 发证组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class Ras14443CopyPlugin extends BasePlugin implements ICopyService {
    private static final String TAG = "Ras14443CopyPlugin";

    @Override
    public ResultBean copy(Context context, Tag tag, String key, String EToken) {
        mContext = context;
        sResultBean = new ResultBean();

        TagNfcaPlugin tagNfcv = new TagNfcaPlugin(tag, mContext, EToken, new TagNfcaPlugin.TagNfcaListener() {
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

        String bytesStr = null;
        for (int i = 0; i < 12; i++) {
            byte[] bytes = tagNfcv.mNfc.readPages(i * 4);
            bytesStr += OperationUtil.bytesToHexString(bytes);
//            LogUtil.d(TAG,"byte --> "+bytesStr);
        }

        if (TextUtils.isEmpty(bytesStr)) {
            tagNfcv.mNfc.close();
            sResultBean.setErrno(TagErrorEnum.READ_TAG_FAIL.getCode());
            sResultBean.setErrmsg(TagErrorEnum.READ_TAG_FAIL.getDescription());
            return sResultBean;
        }

        tagNfcv.mNfc.close();
        sResultBean.setTagData(bytesStr.substring(4));
        sResultBean.setErrno(TagErrorEnum.READ_TAG_SUCCESS.getCode());
        sResultBean.setErrmsg(TagErrorEnum.READ_TAG_SUCCESS.getDescription());
        return sResultBean;
    }

}
