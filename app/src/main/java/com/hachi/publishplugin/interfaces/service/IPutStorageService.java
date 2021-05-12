package com.hachi.publishplugin.interfaces.service;

import android.content.Context;
import android.nfc.Tag;

import com.hachi.publishplugin.bean.ResultBean;

/**
 * 标签入库接口
 */
public interface IPutStorageService {
    ResultBean initTag(Context context, Tag tag, String key, String EToken);
}
