package com.hachi.publishplugin.bean;

import com.hachi.publishplugin.enums.TagErrorEnum;

import lombok.Data;

@Data

public class ResultBean {
    private String uid; //标签UID
    private String time;  //时间戳
    private int errno;  //错误码
    private String errmsg; //错误内容
    private String logData; //溯源日志
    private String readTagContent;  //读取到的标签内容
    private DataBean data;
    private LockTagBean lockData;
    private TagBean tagBean;
    private String info;
    private String tagData;
    private String tagConfig;

    public void setErrno(int errno) {
        this.errno = errno;
        this.errmsg = TagErrorEnum.getDescription(errno);
    }

    @Override
    public String toString() {
        return "ResultBean{" +
                "uid='" + uid + '\'' +
                ", time='" + time + '\'' +
                ", errno=" + errno +
                ", errmsg='" + errmsg + '\'' +
                '}';
    }
}
