package com.hachi.publishplugin.bean;

import lombok.Data;

@Data
public class VersionBean {


    private int errno;
    private DataBeanX data;
    private String errmsg;

    @Data
    public static class DataBeanX {

        private DataBean data;
        private String isCache;
        private String expireTime;

        @Data
        public static class DataBean {

            private int id;
            private String name;
            private String commKey;
            private AppVersionBean appVersion;

            @Data
            public static class AppVersionBean {

                private String id;
                private String remarks;
                private String updateDate;
                private String versionNo;
                private String updateContent;
                private String downloadUrl;
                private String forceUpdate;
                private String appFlag;
            }
        }
    }
}
