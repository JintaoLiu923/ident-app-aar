package com.hachi.publishplugin.gson;


import com.google.gson.Gson;
import com.hachi.publishplugin.bean.F8213Bean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.utils.LogUtil;

/**
 * 工具类：将json字符串转换为对应的Bean类
 */

public class ParseJson {
    private static final String TAG = "ParseJson";

    public TagBean Json2TagBean(String json) {
        LogUtil.i(TAG, "开始解析");
        Gson gson = new Gson();
        TagBean resources = gson.fromJson(json, TagBean.class);
        return resources;
    }

    public F8213Bean Json2F8213Bean(String json) {
        LogUtil.i(TAG, "开始解析");
        Gson gson = new Gson();
        F8213Bean resources = gson.fromJson(json, F8213Bean.class);
        return resources;
    }

    /**
     * 将json字符串转换为对应的Bean类
     *
     * @param json json字符串
     * @param bean bean类
     * @param <T>  bean类泛型
     * @return
     */
    public <T> T Json2Bean(String json, Class<T> bean) {
        Gson gson = new Gson();
        T resources = gson.fromJson(json, bean);
//        LogUtil.i(TAG,  "解析json数据 --> "+resources.toString());
        return resources;
    }
}
