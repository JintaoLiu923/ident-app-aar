package com.hachi.publishplugin.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InterpolationUtils {
//    public static byte[] getPassword(byte[] PWD, byte WK, byte itsp) {
//        //密码由设定决定
//        //与WK异或
//        byte[] encodedpwdByteData = new byte[]{(byte) (PWD[0] ^ WK), (byte) (PWD[1] ^ WK)};
//        encodedpwdByteData = Xor.reverseArray(encodedpwdByteData);
//        byte[] ipencodedData = enInterpolation(itsp, encodedpwdByteData, generateRandom(2));
//        return ipencodedData;
//    }

    //密码认证函数，需要在确定标签准备之后
    public static byte[] getVerifyPwd(byte[] PWD, byte[] randNum, byte INTSEL, byte itsp) {
        if (randNum == null) {
            return null;
        }
        //密码由设定决定
        byte[] encodedpwdByteData = new byte[]{(byte) (PWD[0] ^ randNum[0]), (byte) (PWD[1] ^ randNum[1])};
        encodedpwdByteData = Xor.reverseArray(encodedpwdByteData);   //异或过后将倒转过来的结果用来插值
        byte[] toINT = new byte[]{randNum[0], INTSEL}; //用来插值的材料
        byte[] ipencodedData = enInterpolation(itsp, encodedpwdByteData, toINT);
//        boolean result = ReaderCSharp.TagReaderManager.sharedManager().validatePassword(UID, ipencodedData, pwdId);
        return ipencodedData;
    }

    //加插值运算函数
    public static byte[] enInterpolation(byte ITSP, byte[] Data, byte[] Rand) {
        //将itsp转化为int类型的偏移量
        int[] itsp = new int[4];
        for (int j = 3; j > -1; j--) {
            itsp[j] = (int) ((ITSP & (0x03 << (j * 2))) >> (j * 2));
            if ((itsp[j] == 0) && (j == 3)) {
                // itsp[3] = 1;

            } else if ((itsp[j] == 0) && (j != 3)) {
                itsp[j] = itsp[j + 1];
            }
        }
        int[] newData = OperationUtil.byteToBinary(Data);
        int[] newRAND = OperationUtil.byteToBinary(Rand);
        int flag = itsp[3];
        //转换成List<int>集合
        int[] reverseData = Xor.reverseArray(newData);  //第一次反转

        List<Integer> list = new ArrayList<Integer>();

        for (int reverseDatum : reverseData) {
            list.add(reverseDatum);
        }

        //插入
        int i = 0;
        for (; (flag - i < 16) && (list.size() < 32); ) {
            list.add(flag, newRAND[i]);
            i++;
            flag = flag + itsp[3 - (i % 4)] + 1;
        }

        for (; list.size() < 32; ) {
            list.add(newRAND[i]);
            i++;
        }

        //从List<int>集合，再转换成数组
        int[] dataAIp = new int[list.size()];
        for (int j = 0; j < list.size(); j++) {
            dataAIp[j] = list.get(j);
        }

        int[] reverseDataAIp = Xor.reverseArray(dataAIp);
        //将做好插值的int数组转化成byte数组
        byte[] byteAfterIp = OperationUtil.binaryToByte(reverseDataAIp);
        byte[] reversedByteAfterIp = Xor.reverseArray(byteAfterIp);
        return reversedByteAfterIp;
    }
    //生成随机Byte数
    public static byte[] generateRandom(int figure) {
        byte[] randomByte = new byte[figure];
        Random rdm = new Random();
        rdm.nextBytes(randomByte);
        if (randomByte != null) {
//            LogUtil.d(TAG, "生成的随机数 --> " + OperationUtil.bytesToHexString(randomByte));
            return randomByte;
        }
        return null;
    }

}
