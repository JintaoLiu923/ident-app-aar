package com.hachi.publishplugin.bean;

import lombok.Data;

@Data
public class LockDetailBean {

    private int errno;
    private DataBean data;
    private String errmsg;

    @Data
    public static class DataBean {
        private int id;
        private String rasId;
        private String subRasId;
        private String goodsSn;
        private String rasPad;
        private String subRasPad;
        private String updateDate;
        private int state;
        private int platformId;
        private int tenantId;
    }
}
