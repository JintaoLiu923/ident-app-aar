package com.hachi.publishplugin.utils;

import com.hachi.publishplugin.enums.JudgeTagTypeEnum;

import static com.hachi.publishplugin.enums.JudgeTagTypeEnum.*;

public class JudgeTagTypeUtil {
    public static JudgeTagTypeEnum getTagType(String uid){
        if (uid.startsWith("53")){
            return JudgeTagType_F8213;
        }else if (uid.startsWith("99")){
            return JudgeTagType_14443;
        }
        else if (uid.startsWith("1E")){
            return JudgeTagType_15693;
        }
        return null;
    }
}
