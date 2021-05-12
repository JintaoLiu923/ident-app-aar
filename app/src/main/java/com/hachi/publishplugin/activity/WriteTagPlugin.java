package com.hachi.publishplugin.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.nfc.Tag;
import android.os.Build;

import com.allatori.annotations.DoNotRename;
import com.hachi.publishplugin.activity.base.BasePlugin4Tag;
import com.hachi.publishplugin.activity.ras15693.rw.Ras15693WriteTagPlugin;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.enums.JudgeTagTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.enums.channel.WriteTagChannelEnum;
import com.hachi.publishplugin.utils.LogUtil;

public class WriteTagPlugin extends BasePlugin4Tag {
    private static final String TAG = "WriteTagPlugin";

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @DoNotRename
    public static ResultBean writeTag(Context context, Tag tag, String key, String EToken, int position, String content, String writeType) {
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

//        //鉴权
//        if (!checkLogin(EToken, mobile, password)) {
//            return sResultBean;
//        }

        sIsNfcA = false;
        sIsNfcV = false;
        //判断标签类型
        initTechType(tag);
        if (sIsNfcA) {
            if (!checkNfcAUid(tag)) {
                return sResultBean;
            }
            String sUid = uid.substring(0, 2);
            String channelName = JudgeTagTypeEnum.match(sUid);
            WriteTagChannelEnum matchChannel = WriteTagChannelEnum.match(channelName);
            if (matchChannel != null) {
                sResultBean = matchChannel.mWriteTagService.writeTag(context, tag, key, mRasToken, position, content);
            }
        } else if (sIsNfcV) {
            Ras15693WriteTagPlugin.writeTag(context, tag, key, EToken, position, content, writeType);
        } else {
            sResultBean.setErrno(TagErrorEnum.UN_SUPPORT.getCode());
        }

        if (sResultBean.getErrno() == 205) {
            GlobelRasFunc.musicPlay(context, 1);
        }
        return sResultBean;
    }
}
