package com.hachi.publishplugin.interfaces.service;

import android.content.Context;
import android.nfc.Tag;

import com.hachi.publishplugin.bean.ResultBean;

/**
 * 验证接口
 */
public interface IReadTagService {
    ResultBean readTag(Context context, Tag tag, String key, String EToken, int position);
}
