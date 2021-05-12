package com.hachi.publishplugin.bean;

import java.util.List;

import lombok.Data;

@Data
public class LatLanAddress {
    private List<RegeocodesBean> regeocodes;

    @Data
    public static class RegeocodesBean {
        private String formatted_address;
    }
}