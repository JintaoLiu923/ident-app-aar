package com.hachi.publishplugin.enums.channel;

import com.hachi.publishplugin.activity.ras14443.Ras14443CopyPlugin;
import com.hachi.publishplugin.activity.rasF8213.RasF8213CopyPlugin;
import com.hachi.publishplugin.interfaces.service.ICopyService;

import lombok.Getter;
import lombok.Setter;

/**
 * 发证枚举类
 */
public enum CopyChannelEnum {

    TAG_14443(1, "14443", new Ras14443CopyPlugin()),
    TAG_F8213(3, "F8213", new RasF8213CopyPlugin()),
    ;

    @Getter
    @Setter
    public int mChannelNum;
    @Getter
    @Setter
    public String mChannelName;
    @Getter
    @Setter
    public ICopyService mICopyService;

    CopyChannelEnum(int ChannelNum, String channelName, ICopyService copyService) {
        this.mChannelNum = ChannelNum;
        this.mChannelName = channelName;
        this.mICopyService = copyService;
    }

    public static CopyChannelEnum match(String channelName) {
        CopyChannelEnum[] values = CopyChannelEnum.values();
        for (CopyChannelEnum value : values) {
            String name = value.mChannelName;
            if (name != null && name.equals(channelName)) {
                return value;
            }
        }
        return null;
    }
}
