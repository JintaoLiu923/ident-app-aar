package com.hachi.publishplugin.utils;

import java.util.Random;

/**
 * 运算类
 */
public class OperationUtil {

    /**
     * 字符串转数组
     *
     */
    public static byte[] stringToBytes(String pageStrData, int length) {
        if (pageStrData == null || length % 2 != 0) {
            return null;
        }
        char[] pageCharData = pageStrData.toCharArray();
        int byteLength = length / 2;
        byte[] pageByteData = new byte[byteLength];
        int high, low;
        if (!pageStrData.equals("")) {
            for (int i = 0; i < byteLength; i++) {
                //每两位提取出来转化为string格式至byte
                if (pageCharData[i * 2] >= '0' && pageCharData[i * 2] <= '9') {
                    high = pageCharData[i * 2] - '0';
                } else if (pageCharData[i * 2] >= 'A' && pageCharData[i * 2] <= 'F') {
                    high = pageCharData[i * 2] - 'A' + 10;
                } else if (pageCharData[i * 2] >= 'a' && pageCharData[i * 2] <= 'f') {
                    high = pageCharData[i * 2] - 'a' + 10;
                } else {
                    return null;
                }
                if (pageCharData[i * 2 + 1] >= '0' && pageCharData[i * 2 + 1] <= '9') {
                    low = pageCharData[i * 2 + 1] - '0';
                } else if (pageCharData[i * 2 + 1] >= 'A' && pageCharData[i * 2 + 1] <= 'F') {
                    low = pageCharData[i * 2 + 1] - 'A' + 10;
                } else if (pageCharData[i * 2 + 1] >= 'a' && pageCharData[i * 2 + 1] <= 'f') {
                    low = pageCharData[i * 2 + 1] - 'a' + 10;
                } else {
                    return null;
                }
                pageByteData[i] = (byte) (high * 16 + low);
            }
        }
        return pageByteData;
    }

    /**
     * byte数组转二进制数组
     */
    public static int[] byteToBinary(byte[] HEX) {
        int length = HEX.length * 8;
        int[] binaryArray = new int[length];
        int iv = 0x80;
        for (int i = 0; i < length; i++) {
            if (i % 8 == 0) {
                iv = 0x80;
            }
            if ((HEX[i / 8] & iv) == 0) {
                binaryArray[i] = 0;
            } else {
                binaryArray[i] = 1;
            }
            iv = iv >> 1;
        }

        return binaryArray;
    }

    /**
     * 二进制数组转成byte数组
     */
    public static byte[] binaryToByte(int[] BIN) {
        int length = BIN.length / 8;
        byte[] byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            byteArray[i] = (byte) ((8 * BIN[i * 8] + 4 * BIN[i * 8 + 1] + 2 * BIN[i * 8 + 2] + BIN[i * 8 + 3]) * 16
                    + (8 * BIN[i * 8 + 4] + 4 * BIN[i * 8 + 5] + 2 * BIN[i * 8 + 6] + BIN[i * 8 + 7]));
        }
        return byteArray;
    }

    /**
     * 十进制转十六进制字符串
     *
     */
    public static String intToHex(int number) {
        StringBuffer stringBuffer = new StringBuffer();
        String hexNumber;
        char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        while (number != 0) {
            stringBuffer = stringBuffer.append(b[number % 16]);
            number = number / 16;
        }
        hexNumber = stringBuffer.reverse().toString();
        return hexNumber;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase(); //qq a .toUpperCase() 十六进制文本字符串全部要求大写
    }


    /**
     * 十六进制字符串转byte
     *
     */
    public static byte stringToByte(String src) {
        return (byte) Integer.parseInt(src, 16);
    }


    /**
     * byte转十六进制字符串
     *
     */
    public static String byteToString(byte src) {
        return String.format("%02x", src & 0xff).toUpperCase();
    }

    /**
     * 生成二byte的随机数数组
     */
    public static byte[] getRandom() {
        //生成插值所需的随机数
        Random random = new Random();
        int ab = random.nextInt(239) + 16;
        int b = random.nextInt(239) + 16;
        String rand = OperationUtil.intToHex(ab);
        String rand2 = OperationUtil.intToHex(b);
        byte[] RAND = OperationUtil.stringToBytes(rand, rand.length());
        byte[] RAND2 = OperationUtil.stringToBytes(rand2, rand2.length());
        byte[] RAND3 = new byte[2];
        RAND3[0] = RAND[0];
        RAND3[1] = RAND2[0];
        return RAND3;
    }


    /**
     * 随机生成64到255的ITSP
     *
     */
    public static int createITSP() {
        int max = 191;
        Random random = new Random();
        return random.nextInt(max) + 64;
    }

    public static int createWK() {
        int max = 239;
        Random random = new Random();
        return random.nextInt(max) + 16;
    }

    public static byte createINTSEL() {
        //对应高三位000~111
        //对应十六进制字符串：0,20,40,60,80，A0,C0,E0
        byte[] nums = new byte[]{0, 32, 64, 96, (byte) 128, (byte) 160, (byte) 192, (byte) 224};
        Random random = new Random();
        int index = random.nextInt(8);
//        Log.i("Debug","INTSEL的索引:"+index);
        return nums[index];
    }

}
