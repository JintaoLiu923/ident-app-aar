package com.hachi.publishplugin.enums.channel;

import com.hachi.publishplugin.activity.ras14443.rw.Ras14443ReadTagPlugin;
import com.hachi.publishplugin.activity.rasF8213.RasF8213ReadTagPlugin;
import com.hachi.publishplugin.interfaces.service.IReadTagService;

import lombok.Getter;
import lombok.Setter;

/**
 * 验证枚举类
 */
public enum ReadTagChannelEnum {

    TAG_14443(1, "14443", new Ras14443ReadTagPlugin()),
    TAG_F8213(2, "F8213", new RasF8213ReadTagPlugin()),
    ;

    @Getter
    @Setter
    public int mChannelNum;
    @Getter
    @Setter
    public String mChannelName;
    @Getter
    @Setter
    public IReadTagService mReadTagService;

    ReadTagChannelEnum(int ChannelNum, String channelName, IReadTagService readTagService) {
        this.mChannelNum = ChannelNum;
        this.mChannelName = channelName;
        this.mReadTagService = readTagService;
    }

    public static ReadTagChannelEnum match(String channelName) {
        ReadTagChannelEnum[] values = ReadTagChannelEnum.values();
        for (ReadTagChannelEnum value : values) {
            String name = value.mChannelName;
            if (name != null && name.equals(channelName)) {
                return value;
            }
        }
        return null;
    }
}
