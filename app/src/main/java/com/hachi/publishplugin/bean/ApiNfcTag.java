package com.hachi.publishplugin.bean;

import lombok.Data;


@Data
public class ApiNfcTag  {
    private TbRasCert tagCert;
    private TbRasCfg tagCfg;
    private TbRasCfgNokey tagCfgNoKey;
}
