package com.hachi.publishplugin.enums.channel;

import com.hachi.publishplugin.activity.ras14443.Ras14443CertPlugin;
import com.hachi.publishplugin.activity.rasF8213.RasF8213CertPlugin;
import com.hachi.publishplugin.interfaces.service.ICertService;

import lombok.Getter;
import lombok.Setter;

/**
 * 发证枚举类
 */
public enum CertChannelEnum {

    TAG_14443(1, "14443", new Ras14443CertPlugin()),
//    TAG_15693(2, "15693", new Ras15693CertPlugin()),
    TAG_F8213(3, "F8213", new RasF8213CertPlugin()),
    ;

    @Getter
    @Setter
    public int mChannelNum;
    @Getter
    @Setter
    public String mChannelName;
    @Getter
    @Setter
    public ICertService mICertService;

    CertChannelEnum(int ChannelNum, String channelName, ICertService certService) {
        this.mChannelNum = ChannelNum;
        this.mChannelName = channelName;
        this.mICertService = certService;
    }

    public static CertChannelEnum match(String channelName) {
        CertChannelEnum[] values = CertChannelEnum.values();
        for (CertChannelEnum value : values) {
            String name = value.mChannelName;
            if (name != null && name.equals(channelName)) {
                return value;
            }
        }
        return null;
    }
}
