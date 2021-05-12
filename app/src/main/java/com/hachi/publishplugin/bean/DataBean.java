package com.hachi.publishplugin.bean;

import lombok.Data;

@Data
public class DataBean {
    private String uid;
    private String certEncrypt;
    private String certDecode;
    private String ndef;
    private CfgBean cfg;
    private Boolean isPswTag;
    private Boolean isTmpTag;
    private Integer tagType;
    private String newCert;
    private Boolean hasCert;
    private Boolean isOpened;
    private Boolean isV5state;
    private String rasId;
    private String rasIdEncrypt;
    private String tokenExpire;
    private String token;
    private String newPsw;
}

