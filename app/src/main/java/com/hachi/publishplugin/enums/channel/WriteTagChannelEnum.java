package com.hachi.publishplugin.enums.channel;

import com.hachi.publishplugin.activity.ras14443.rw.Ras14443WriteTagPlugin;
import com.hachi.publishplugin.activity.rasF8213.RasF8213WriteTagPlugin;
import com.hachi.publishplugin.interfaces.service.IWriteTagService;

import lombok.Getter;
import lombok.Setter;

/**
 * 验证枚举类
 */
public enum WriteTagChannelEnum {
    TAG_14443(1, "14443", new Ras14443WriteTagPlugin()),
    TAG_F8213(2, "F8213", new RasF8213WriteTagPlugin()),
    ;

    @Getter
    @Setter
    public int mChannelNum;
    @Getter
    @Setter
    public String mChannelName;
    @Getter
    @Setter
    public IWriteTagService mWriteTagService;

    WriteTagChannelEnum(int ChannelNum, String channelName, IWriteTagService writeTagService) {
        this.mChannelNum = ChannelNum;
        this.mChannelName = channelName;
        this.mWriteTagService = writeTagService;
    }

    public static WriteTagChannelEnum match(String channelName) {
        WriteTagChannelEnum[] values = WriteTagChannelEnum.values();
        for (WriteTagChannelEnum value : values) {
            String name = value.mChannelName;
            if (name != null && name.equals(channelName)) {
                return value;
            }
        }
        return null;
    }
}
