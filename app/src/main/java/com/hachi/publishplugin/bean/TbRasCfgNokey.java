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
public class TbRasCfgNokey {

    private Long id;

    private String uid;

    private String password;

    private String remarks;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private Short state;

    private Integer platformType;

    private Integer platformId;

    private Integer tenantId;

    private Integer groupId;

    private Integer tagType;

    private Integer batchId;

    private String ndef;

    private String pad;
}