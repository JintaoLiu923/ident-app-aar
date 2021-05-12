package com.hachi.publishplugin.activity.data;

public interface DataSource {
    void getDetail(String uid, String random, String newPassword, String EToken, DataCallback dataCallback);

    interface DataCallback {

        void success(Object obj);

        void error(Integer code, String toastMessage);
    }
}
