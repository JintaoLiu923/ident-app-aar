package com.hachi.publishplugin.enums.channel;

import com.hachi.publishplugin.interfaces.service.IPutStorageService;

import lombok.Getter;
import lombok.Setter;

/**
 * 标签入库枚举类
 */
public enum PutStorageChannelEnum {

//    TAG_15693(1, "常规标签", new RasF8023InitPlugin()),
//    TAG_15693_TEMP(2, "15693温控标签", new RasF8023InitPlugin()),
//    TAG_15693_V5(3, "15693V5管角标签", new RasF8023InitPlugin()),
//    TAG_F8023(4, "F8023标签", new RasF8023InitPlugin()),
    ;
    @Getter
    @Setter
    public int mChannelNum;
    @Getter
    @Setter
    public String mChannelName;
    @Getter
    @Setter
    public IPutStorageService mPutStorageService;

    PutStorageChannelEnum(int ChannelNum, String channelName, IPutStorageService iPutStorageService) {
        this.mChannelNum = ChannelNum;
        this.mChannelName = channelName;
        this.mPutStorageService = iPutStorageService;
    }

    public static PutStorageChannelEnum match(String channelName) {
        PutStorageChannelEnum[] values = PutStorageChannelEnum.values();
        for (PutStorageChannelEnum value : values) {
            String name = value.mChannelName;
            if (name != null && name.equals(channelName)) {
                return value;
            }
        }
        return null;
    }
}
