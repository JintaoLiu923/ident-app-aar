package com.hachi.publishplugin.bean;

import java.io.Serializable;

import lombok.Data;

@Data
public class IdentLogReqBean implements Serializable {
    private IdentRasLogBean log;
    private TmpBean temp;
}
