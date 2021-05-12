package com.hachi.publishplugin.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

//100以内的是RAS相关读写操作，100～200以内的是RAS相关业务节点操作（配置，发证，验证，二维码验证），200~299:物流相关操作


public enum ActTypeEnum {

    ACT_TYPE_读uid(0, "rUid", "读uid"),
    ACT_TYPE_密码认证(2, "rPsw", "密码认证"),
    ACT_TYPE_写密码(3, "wPsw", "写密码"),
    ACT_TYPE_读证书(4, "rCert", "读证书"),
    ACT_TYPE_写证书(5, "wCert", "写证书"),
    ACT_TYPE_读随机数(6, "rRandom", "15693读随机数"),
    ACT_TYPE_写时间戳(9, "wRasId", "写时间戳"),
    ACT_TYPE_读状态位(10, "rPad", "读状态位"),
    ACT_TYPE_读NDEF(12, "rNdef", "读NDEF"),
    ACT_TYPE_写NDEF(13, "wNdef", "写NDEF"),
    ACT_TYPE_读标志位(14, "rFlag", "读标志位"),
    ACT_TYPE_写标志位(15, "wFlag", "写标志位"),
    ACT_TYPE_供电开启(16, "rADC", "供电开启"),
    ACT_TYPE_写温控参数(17, "wTemp", "写温控参数"),
    ACT_TYPE_读温控KB参数(18, "rTempKB", "读温控KB参数"),
    ACT_TYPE_写芯片工作模式(19, "wChipM", "写芯片工作模式"),
    ACT_TYPE_读取温度列表(20, "rTemps", "读取温度列表"),
    ACT_TYPE_开启或停止温控(21, "wTempCrl", "开启或停止温控"),
    ACT_TYPE_写RWK(22, "wRwk", "写rkwk"),

    ACT_TYPE_写开锁(31, "wLockOpen", "开锁"),
    ACT_TYPE_读锁状态(32, "rLockStatus", "读锁状态"),


    ACT_TYPE_业务操作(100, "apiRas", "RAS操作"),
    ACT_TYPE_配置操作(101, "apiRasCfg", "RAS配置"),
    ACT_TYPE_发证操作(102, "apiRasCert", "RAS发证"),
    ACT_TYPE_验证操作(103, "apiRasVerify", "RAS验证"),
    ACT_TYPE_RAS二维码绑定(104, "apiRasBindQr", "RAS二维码绑定"),
    ACT_TYPE_RAS标签初始化(105, "apiRasPutStorage", "标签初始化"),


    ACT_TYPE_电子锁操作_开锁(110, "apiLockOpen", "电子锁开锁"),
    ACT_TYPE_电子锁操作_关锁(111, "apiLockClose", "电子锁关锁"),
    ACT_TYPE_电子锁操作_读锁状态(112, "apiLockStatus", "读电子锁状态"),
    ACT_TYPE_电子锁操作_检测(113, "apiLockCheck", "电子锁检测"),

    ACT_TYPE_电子锁操作_电量不足(121, "ePowerNotEnough", "电量不足"),
    ACT_TYPE_电子锁操作_电量告警(122, "ePowerWarning", "电量告警"),
    ACT_TYPE_电子锁操作_关锁异常(123, "eLockCloseError", "关锁异常"),

    ACT_TYPE_业务其他操作(199, "apiRasOther", "业务其他操作"),

    ACT_TYPE_物流操作(200, "apiLgs", "物流操作"),
    ACT_TYPE_物流操作_储存凭证(207, "apiLgsSaveProof", "储存凭证"),
    ACT_TYPE_物流操作_已取货(208, "apiLgsTaken", "已取货"),
    ACT_TYPE_物流操作_已送达(209, "apiLgsArrived", "已送达"),
    ACT_TYPE_物流操作_已签收(210, "apiLgsDelivered", "已签收"),
    ACT_TYPE_物流操作_已拒收(214, "apiLgsRejected", "已拒收"),
    ACT_TYPE_物流操作_已退回(215, "apiLgsReturned", "已退回"),


    ACT_TYPE_物流其他操作(299, "apiLgsOther", "物流其他操作"),
    ;
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String name;

    public String getValue() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private String desc;

    ActTypeEnum(int id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    public static List<String> getTraceActions() {
        return new ArrayList<>(Arrays.asList("apiRasCert", "apiLgsTaken", "apiLgsArrived", "apiLgsDelivered", "apiLgsRejected", "apiLgsReturned", "apiRasVerify"));
    }

    public static List<String> getLockTraceActions() {
        return new ArrayList<>(Arrays.asList("apiLockOpen", "apiLockClose", "apiLockCheck"));
    }

    public static ActTypeEnum getValueById(int id) {
        ActTypeEnum[] actTypeEnums = values();
        for (ActTypeEnum actTypeEnum : actTypeEnums) {
            if (actTypeEnum.getId() == id) {
                return actTypeEnum;
            }
        }
        return null;
    }

    public static ActTypeEnum getValueByName(String actName) {
        ActTypeEnum[] actTypeEnums = values();
        for (ActTypeEnum actTypeEnum : actTypeEnums) {
            if (actTypeEnum.getName().equals(actName)) {
                return actTypeEnum;
            }
        }
        return null;
    }
}
