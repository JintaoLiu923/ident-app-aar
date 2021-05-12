package com.hachi.publishplugin.activity.init;

import android.annotation.TargetApi;
import android.content.Context;
import android.nfc.Tag;
import android.os.Build;

import com.allatori.annotations.DoNotRename;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.activity.rasF8023.RasF8023InitTagPlugin;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.LogUtil;

/**
 * 标签初始化
 */
public class InitTagPlugin extends BasePlugin4Tag {
    private static final String TAG = "PutStoragePlugin";

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @DoNotRename
    public static ResultBean init(Context context, Tag tag, String key, String EToken, int tagType, int batchId) {
        mContext = context;
        sResultBean = new ResultBean();
        LogUtil.i(TAG, "开始发证流程");
        //检查定位权限
        if (!checkLocation()) {
            return sResultBean;
        }

        //初始化定位
        initLocation(context, key);

        mRasToken = EToken;

        sIsNfcA = false;
        sIsNfcV = false;
        //判断标签类型
        initTechType(tag);
        if (sIsNfcA) {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        } else if (sIsNfcV) {
            if (tagType == 4) {
                sResultBean = new RasF8023InitTagPlugin().initTag(context, tag, key, EToken);
            } else {
                sResultBean = new Ras15693InitTagPlugin().initTag(context, tag, key, EToken, tagType, batchId);
            }
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        }

        sActTypeEnum = ActTypeEnum.ACT_TYPE_RAS标签初始化;
        //设置定位监听器
        if (mlocationClient != null) {
            mlocationClient.setLocationListener(mLocationListener);
            mlocationClient.startLocation();
        }

        if (sResultBean.getErrno() == 205) {
            GlobelRasFunc.musicPlay(context, 1);
        }
        return sResultBean;
    }
}
