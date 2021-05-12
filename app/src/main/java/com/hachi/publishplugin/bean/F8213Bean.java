package com.hachi.publishplugin.bean;

import lombok.Data;

@Data
public class F8213Bean {
    private int errno;
    private DataBean data;
    private String errmsg;
    @Data
    public static class DataBean {
        private String uid;
        private CfgBean cfg;
        private boolean isPswTag;
        private String newCert;
        private String rasId;
        private boolean hasCert;
        private boolean isOpen;
        @Data
        public static class CfgBean {
            private int id;
            private String password;
            private int state;
            private int platformId;
        }
    }
}
