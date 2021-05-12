package com.hachi.publishplugin.bean;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TbRasCfg {
    private Long id;
    private String uid;
    private String rk;
    private String wk;
    private String itsp;
    private String aflmt;
    private String aflmtx;
    private String oflag;
    private Boolean access;
    private String auth0;
    private Integer intsel;
    private String pad;
    private String remarks;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private Short state;
    private Integer platformType;
    private Integer platformId;
    private Integer tenantId;
    private Integer groupId;
    private String tmpk;
    private String tmpb;
    private String tmin;
    private String tmax;
    private String tintx;
    private String tintn;
    private String wstate;
    private Boolean isTempTag;
    private Integer tagType;
    private Boolean isV5state;
    private Integer batchId;
    private String ndef;
    private String password;
}