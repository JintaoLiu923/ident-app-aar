package com.hachi.publishplugin.bean;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class IdentRasLogBean implements Serializable {
    private String rasId;
    private Integer type;
    private String action;
    private Integer status;
    private String result;
    private String comment;
    private BigDecimal lng;
    private BigDecimal lat;
    private Integer createBy;
    private String createByName;
    private String uid;
}