package com.hachi.publishplugin.bean;
import java.io.Serializable;

import lombok.Data;

@Data
public class TagBean implements Serializable {
    private int errno;
    private DataBean data;
    private String errmsg;
}
