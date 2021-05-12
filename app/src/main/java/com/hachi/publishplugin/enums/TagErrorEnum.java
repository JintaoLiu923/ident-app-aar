package com.hachi.publishplugin.enums;

import lombok.Getter;

public enum TagErrorEnum {

    SUCCESS(0, "成功"),
    FAIL(-1, "失败"),
    VERIFY_TRUE_NOT_OPEN(200, "验证为真，标签未开启"),
    VERIFY_TRUE_OPEN(201, "验证为真，标签已开启"),
    CERT_SUCCESS(202, "发证成功"),
    CFG_SUCCESS(203, "配置成功"),
    VERIFY_SUCCESS(204, "验证为真"),
    INIT_TAG_SUCCESS(205, "初始化标签成功"),

    BIND_LOCK_TAG_SUCCESS(206, "上盖下盖绑定成功"),
    MATCH_LOCK_TAG_SUCCESS(207, "上盖下盖匹配成功"),
    UPDATE_LOCK_STATUS(208, "更新锁状态成功"),
    OPEN_LOCK_SUCCESS(209, "开锁成功"),
    CLOSE_LOCK_SUCCESS(210, "关锁成功"),
    UPLOAD_LOCK_PAD_SUCCESS(211, "上传锁状态成功"),

    READ_TAG_SUCCESS(220, "标签读取成功"),
    WRITE_TAG_SUCCESS(221, "标签写入成功"),
    DELETE_CERT_SUCCESS(222, "删除发证记录成功"),
    PUT_STORAGE_SUCCESS(223, "标签入库成功"),
    GET_CFG_SUCCESS(224, "获取配置成功"),

    INVALID_PARAM(401, "参数不对"),
    INVLID_PARAM_VALUE(402, "参数值不对"),
    UNAUTHORIZED(420, "未授权"),
    REQ_FAILED(500, "网络请求失败"),
    UN_LOGIN(501, "登录凭证失效"),
    SYS_INNER_ERROR(502, "系统内部错误"),
    UN_SUPPORT(503, "业务不支持"),
    UPDATE_UNSUCCESS(504, "更新数据已经失效"),
    UPDATE_FAIL(505, "更新数据失败"),
    ADD_FAIL(506, "新增数据失败"),
    UN_AUTHORITY(507, "无操作权限"),
    COMP_REG(508, "请提交企业认证申请"),
    PLS_REG(509, "请先注册后登录"),

    PWD_FAILED(601, "密码验证失败"),
    CERT_READ_FAILED(602, "证书读取失败"),
    CERT_WRITE_FAILED(603, "证书写入失败"),
    BIZ_WRITE_FAILED(604, "时间戳写入失败"),
    NDEF_WRITE_FAILED(605, "NDEF写入失败"),
    RANDOM_GET_FAILED(606, "15693随机数获取失败"),
    UID_GET_FAILED(607, "UID读取失败"),
    PWD_WRITE_FAILED(608, "14443普通密码写入失败"),
    PAD_READ_FAILED(609, "状态位读取失败"),
    FLAG_READ_FAILED(610, "标志位读取失败"),
    RESET_ADC_FAILED(611, "供电复位失败"),
    TEMP_READ_KB_FAILED(612, "读取温度系数失败"),
    CHIPM_WRITE_FAILED(613, "写芯片工作模式失败"),
    TEMP_READ_LIST_FAILED(614, "读取温度列表失败"),
    NDEF_READ_FAILED(615, "读取NDEF失败"),
    HAS_CERT(616, "标签已发证,请勿重复发证"),
    PERMISSION_LOCATION_DENY(617, "未开启定位权限"),
    TAG_INITIALIZED(618, "标签已初始化,请勿重复初始化"),
    WRITE_PASSWORD_FAIL(619, "密码写入失败"),
    WRITE_RWKEY_FAIL(620, "RwKey写入失败"),

    OPEN_LOCK_FAIL(621, "开锁失败"),
    LOCK_MATCH_FAIL(622, "上下盖状态不匹配"),
    LOCK_STATUS_ABNORMAL(623, "闭锁状态异常"),
    LOCK_STATUS_NORMAL(624, "闭锁状态正常"),
    LOCK_PAD_CHANGE(625, "上盖状态发生改变"),
    LOCK_SUB_PAD_CHANGE(626, "下盖状态发生改变"),

    FLAG_FAIL(640, "标志位数值异常"),
    READ_TAG_FAIL(641, "标签读取失败"),
    WRITE_TAG_FAIL(642, "标签写入失败"),

    INVALI_CFG_ERROR(798, "非法配置"),

    IVALID_TAG(701, "不是爱扽芯片"),
    VERIFY_FALSE(702, "验证为假"),
    NOT_SUPPORT_TEMP(703, "不支持温控功能"),
    INVALI_RAS_CERT_ERROR(704, "证书非法"),
    INVALI_RAS_PAD_ERROR(705, "状态非法"),
    INVALI_NFC_TYPE(798, "标签类型非法"),
    DEFAULT(799, "未知错误"),


    CHECK_LOGIN_FAIL(901, "初始化登录失败"),
    CHECK_NET_FAIL(902, "初始化网络失败"),
    CHECK_LOCATION_FAIL(903, "初始化定位失败"),
    CHECK_MUSIC_FAIL(904, "初始化音乐失败"),
    INIT_SUCCESS(905, "初始化成功"),

    ;

    @Getter
    private final int code;
    @Getter
    private final String description;

    TagErrorEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescription(int code) {
        return getTagError(code).description;
    }

    public static TagErrorEnum getTagError(int code) {
        for (TagErrorEnum error : TagErrorEnum.values()) {
            if (error.getCode() == code) {
                return error;
            }
        }
        return DEFAULT;
    }
}
