package com.hachi.publishplugin.bean;

import lombok.Data;

@Data
public class LockTagBean {
    //标签时间戳
    private String rasId;
    //标签状态位
    private String rasPad;
    //标签解码状态位
    private String decodePad;
    //电子锁电量
    private int power;
    //电子锁开关状态
    private int status;
    //电子锁关闭时间
    private String closeTime;
}
