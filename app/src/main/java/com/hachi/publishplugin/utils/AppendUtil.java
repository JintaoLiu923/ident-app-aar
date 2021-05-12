package com.hachi.publishplugin.utils;

public class AppendUtil {
    public static StringBuilder append(StringBuilder appendStr, int beginIndex, int endIndex) {
        StringBuilder result = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            char a = appendStr.charAt(i);
            result.append(a);
        }
        return result;
    }
}
