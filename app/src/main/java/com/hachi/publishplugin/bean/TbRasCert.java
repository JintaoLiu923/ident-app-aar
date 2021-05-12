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
public class TbRasCert {
    private Long id;

    private String uid;

    private String cert1;

    private String cert2;

    private String cert3;

    private String rasId;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    private String status;

    private String state;

    private Integer platformType;

    private Integer platformId;

    private Integer tenantId;

    private Integer groupId;

    private String cert;

}