package com.hachi.publishplugin.bean;

import lombok.Data;

@Data
public class LockBindResultBean {

    private int errno;
    private int data;
    private String errmsg;
}
