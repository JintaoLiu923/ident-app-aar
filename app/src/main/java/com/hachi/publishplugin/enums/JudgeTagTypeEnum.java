package com.hachi.publishplugin.enums;

import android.text.TextUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 标签类型枚举类，根据标签Uid前两位判断标签类型
 */
public enum JudgeTagTypeEnum {
    JudgeTagType_14443(1, "14443", "99"),
    JudgeTagType_15693(2, "15693", "E0"),
    JudgeTagType_F8213(4, "F8213", "53"),
    ;

    @Getter
    @Setter
    private int mId;
    @Getter
    @Setter
    private String mName;
    @Getter
    @Setter
    private String mHead;

    JudgeTagTypeEnum(int id, String name, String head) {
        this.mId = id;
        this.mName = name;
        this.mHead = head;
    }

    public static String match(String head) {
        JudgeTagTypeEnum[] values = JudgeTagTypeEnum.values();
        for (JudgeTagTypeEnum value : values) {
            String headName = value.getMHead();
            if (!TextUtils.isEmpty(headName) && headName.equals(head)) {
                return value.getMName();
            }
        }
        return null;
    }
}
