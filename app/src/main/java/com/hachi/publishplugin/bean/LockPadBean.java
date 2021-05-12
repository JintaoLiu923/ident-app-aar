package com.hachi.publishplugin.bean;

import lombok.Data;

@Data
public class LockPadBean {
    private String pad;
    private String rasId;
    private String subPad;
    private String subRasId;

    public LockPadBean(String pad, String rasId, String subPad, String subRasId) {
        this.pad = pad;
        this.rasId = rasId;
        this.subPad = subPad;
        this.subRasId = subRasId;
    }
}
