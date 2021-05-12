package com.hachi.publishplugin.enums;

import lombok.Getter;
import lombok.Setter;

/**
 * 电子锁电量枚举类
 */
public enum ElecEnum {
    ELEC_00H((byte) 0x00, 0),
    ELEC_01H((byte) 0x01, 5),
    ELEC_02H((byte) 0x02, 15),
    ELEC_03H((byte) 0x03, 30),
    ELEC_04H((byte) 0x04, 45),
    ELEC_05H((byte) 0x05, 60),
    ELEC_06H((byte) 0x06, 75),
    ELEC_07H((byte) 0x07, 85),
    ELEC_08H((byte) 0x08, 95),
    ELEC_09H((byte) 0x09, 100);

    @Getter
    @Setter
    public byte mElecCode;
    @Getter
    @Setter
    public int mElecValue;

    ElecEnum(byte elecCode, int elecValue) {
        this.mElecCode = elecCode;
        mElecValue = elecValue;
    }

    public static int match(byte elecCode) {
        ElecEnum[] values = ElecEnum.values();
        for (ElecEnum value : values) {
            byte code = value.mElecCode;
            if (code == elecCode) {
                return value.mElecValue;
            }
        }
        return 0;
    }
}
