package com.hachi.publishplugin.bean;

import lombok.Data;

@Data
public class LockCheckBean {

    private int errno;
    private DataBean data;
    private String errmsg;

    @Data
    public static class DataBean {
        private boolean ifPadsMatch;
        private boolean ifPadChanged;
        private boolean ifSubPadChanged;
        private int type;

    }
}
