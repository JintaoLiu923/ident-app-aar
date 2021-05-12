package com.hachi.publishplugin.enums;


import lombok.Getter;
import lombok.Setter;

/**
 * NFC标签操作类型
 */
public enum MusicTypeEnum {

    ACT_TYPE_SUCCESS (0  , "success",  "music.mp3"          ),
    ACT_TYPE_FAIL    (1  , "fail",     "music_fail.mp3"     ),
    ACT_TYPE_OTHER   (99 , "other",    "music_other.mp3"    );

    @Getter
    @Setter
    private int id;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String desc;

    MusicTypeEnum(int id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }
}
