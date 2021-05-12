package com.hachi.publishplugin.enums.channel;

import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.ras14443.Ras14443ValiPlugin;
import com.hachi.publishplugin.activity.rasF8213.RasF8213ValiPlugin;
import com.hachi.publishplugin.activity.rasF8213.RasF8213ValiPlugin2;
import com.hachi.publishplugin.interfaces.service.IValiService;

import lombok.Getter;
import lombok.Setter;

/**
 * 验证枚举类
 */
public enum VailChannelEnum {

    TAG_14443(1, "14443", new Ras14443ValiPlugin()),
    TAG_F8213(3, "F8213", new RasF8213ValiPlugin()),
    TAG_F8213_2(4, "F8213", new RasF8213ValiPlugin2());

    @Getter
    @Setter
    public int mChannelNum;
    @Getter
    @Setter
    public String mChannelName;
    @Getter
    @Setter
    public IValiService mValiService;

    VailChannelEnum(int ChannelNum, String channelName, IValiService valiService) {
        this.mChannelNum = ChannelNum;
        this.mChannelName = channelName;
        this.mValiService = valiService;
    }

    public static VailChannelEnum match(String channelName) {
        VailChannelEnum[] values = VailChannelEnum.values();
        for (VailChannelEnum value : values) {
            String name = value.mChannelName;
            if (name != null && name.equals(channelName)) {
                if (name.equals("F8213")){
                    if (GlobelRasFunc.getIsRasIdModeOpen()){
                        return TAG_F8213_2;
                    }
                }
                return value;
            }
        }
        return null;
    }
}
