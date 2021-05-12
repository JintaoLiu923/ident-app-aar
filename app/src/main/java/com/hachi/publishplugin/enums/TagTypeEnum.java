package com.hachi.publishplugin.enums;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * NFC标签类型
 */
public enum TagTypeEnum {
    //(0:常规功能  2:15693温控检测 3:15693第五管脚状态检测 // 1:15693标签 10:F8213标签 20:14443标签)
    DEFAULT(0, "常规功能", ""),
    TAGE_TYPE_15693常规(1, "15693常规", ""),
    TAGE_TYPE_15693温控(2, "15693温控", "温控"),
    TAGE_TYPE_15693状态(3, "15693状态", "第五管脚状态检测"),
    TAGE_TYPE_F8023常规(4, "F8023常规", "电子锁下盖"),
    TAGE_TYPE_F8213常规(10, "F8213常规", ""),
    TAGE_TYPE_14443常规(20, "14443常规", "");

    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String desc;

    TagTypeEnum(int id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    public static List<String> toList() {
        List<String> list = new ArrayList<>();
        for (TagTypeEnum item : TagTypeEnum.values()) {
            if (item.getId() < 10) {
                list.add(item.getName());
            }
        }
        return list;
    }

    public static int getTagType(String name) {
        for (TagTypeEnum value : TagTypeEnum.values()) {
            if (value.getName().equals(name)) {
                return value.getId();
            }
        }
        return 0;
    }
}
