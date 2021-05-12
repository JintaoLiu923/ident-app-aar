package com.hachi.publishplugin.interfaces.service;

import android.content.Context;
import android.nfc.Tag;

import com.hachi.publishplugin.bean.ResultBean;

/**
 * 发证接口
 */
public interface ICertService {
    ResultBean cert(Context context, Tag tag, String key, String EToken, String mobile, String password,String newPassword);
}
