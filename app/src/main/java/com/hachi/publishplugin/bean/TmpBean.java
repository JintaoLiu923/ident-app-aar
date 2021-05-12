package com.hachi.publishplugin.bean;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class TmpBean implements Serializable {
    private int id;
    private String uid;
    private int logId;
    private String rasId;
    private String action;
    private String createDate;
    private Float tmpk01;       //1Eh 0,1 温度系数 K
    private Float tmpb01;       //1Eh 2,3 温度系数 B
    private Integer tmppnt;     //25h 0 循环记录温度的指针，记录本次循环的最后一个字节地址值
    private Integer tmpcnt;     //25h 1 温度循环记录次数计数器，记录温度地址每循环一次，该值加 1
    private Float tmpmax;       //25h 2 整个温度记录周期里发生的温度最大值
    private Float tmpmin;       //25h 3 整个温度记录周期里发生的温度最小值
    private Float tmin;         //1fh 0 超限模式，温度最小值
    private Float tmax;         //1fh 1 超限模式，温度最大值
    private Integer tintx;      //1fh 2 超限模式下，当温度在范围外的检测时间间隔，单位 1 分钟
    private Integer tintn;      //1fh 3 超限模式下，当温度在范围内的检测时间间隔，单位 1 分钟
    private List<Float> list;   //qq
    private Float nowtmp;
    private Integer createBy;
    private String createByName;
}
