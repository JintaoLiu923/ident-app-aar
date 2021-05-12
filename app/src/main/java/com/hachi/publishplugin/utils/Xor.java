package com.hachi.publishplugin.utils;

import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Xor {

    public static byte[] writeEncode(byte[] singlePageData, byte writeKey) {
        byte[] dataEncoded = new byte[singlePageData.length];
        if ((singlePageData.length != 4) && (singlePageData.length != 2))//判断页数据是否有效
        {
            Log.i("加密", "出错");
            return null;
        } else {
            for (int i = 0; i < singlePageData.length; i++) {
                dataEncoded[i] = (byte) (singlePageData[i] ^ writeKey);
            }
            if (dataEncoded.length == singlePageData.length) {
                return dataEncoded;
            } else {
                Log.i("加密", "加密失败");
                return null;
            }
        }
    }

    public byte[] readDecode(byte[] singlePageData, byte readKey) {
        //其实wirteEncode和readDecode的功能是一样的，只是一个用WK,一个RK
        byte[] dataDecoded = new byte[singlePageData.length];
        for (int i = 0; i < singlePageData.length; i++) {
            dataDecoded[i] = (byte) (singlePageData[i] ^ readKey);
        }
        if (dataDecoded.length == singlePageData.length) {
            return dataDecoded;
        } else {
            return null;
        }
    }

    /**
     * 去插值
     */
    public static byte[] deInterpolation(byte ITSP, byte[] data) {
        int[] itsp = new int[4];
        for (int j = 3; j > -1; j--) {
            itsp[j] = (int) ((ITSP & (0x03 << (j * 2))) >> (j * 2));
            if ((itsp[j] == 0) && (j == 3)) {
                Log.e("EnInterpolation", "ITSP数据不合规，首位为0");
            } else if ((itsp[j] == 0) && (j != 3)) {
                itsp[j] = itsp[j + 1];
            }
        }
        byte[] reversedData = reverseArray(data);
        int[] newData = OperationUtil.byteToBinary(reversedData);
        int[] reverseData = reverseArray(newData);
        int flag = itsp[3];
        int i = 0;
        int count = 0;
        int[] dataAip = new int[16];
        for (; count < 16; ) {
            if (i != flag) {
                dataAip[count] = reverseData[i];
                count++;
            } else {
                flag = flag + itsp[3 - ((i + 1 - count) % 4)] + 1;
            }
            i++;
        }
        int[] reversedDataAip = reverseArray(dataAip);
        byte[] byteAfterIp = OperationUtil.binaryToByte(reversedDataAip);
        return byteAfterIp;
    }

    //以下为工具函数
    //byte数组转2进制数组
    public int[] byteToBinary(byte[] HEX) {
        int length = HEX.length * 8;
        int[] BinaryArray = new int[length];
        int iv = 0x80;
        for (int i = 0; i < length; i++) {
            if (i % 8 == 0) {
                iv = 0x80;
            }
            if ((HEX[i / 8] & iv) == 0) {
                BinaryArray[i] = 0;
            } else {
                BinaryArray[i] = 1;
            }
            iv = iv >> 1;
        }
        return BinaryArray;
    }

    //2进制数组转byte数组
    public byte[] binaryToByte(int[] BIN) {
        int length = BIN.length / 8;
        byte[] ByteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            ByteArray[i] = (byte) ((8 * BIN[i * 8] + 4 * BIN[i * 8 + 1] + 2 * BIN[i * 8 + 2] + BIN[i * 8 + 3]) * 16 + (8 * BIN[i * 8 + 4] + 4 * BIN[i * 8 + 5] + 2 * BIN[i * 8 + 6] + BIN[i * 8 + 7]));
        }
        return ByteArray;
    }

    //反转数组顺序
    public static int[] reverseArray(int[] inputArray) {
        int length = inputArray.length;
        int[] reversedArray = new int[length];
        for (int i = 0; i < length; i++) {
            reversedArray[i] = inputArray[length - i - 1];
        }
        //Console.WriteLine(String.Join("", reversedArray.ToArray()));
        return reversedArray;
    }

    //反转数组顺序
    public static byte[] reverseArray(byte[] inputArray) {
        int length = inputArray.length;
        byte[] reversedArray = new byte[length];
        for (int i = 0; i < length; i++) {
            reversedArray[i] = inputArray[length - i - 1];
        }
        //Console.WriteLine(String.Join("", reversedArray.ToArray()));
        return reversedArray;
    }

    public static byte[] interpolation(byte ITSP, byte[] Data, byte[] RAND) {
        //将itsp转化为int类型的偏移量
        int[] itsp = new int[4];
        for (int j = 3; j > -1; j--) {
            itsp[j] = (int) ((ITSP & (0x03 << (j * 2))) >> (j * 2));
            if ((itsp[j] == 0) && (j == 3)) {
                Log.e("EnInterpolation", "ITSP数据不合规，首位为0");
            } else if ((itsp[j] == 0) && (j != 3)) {
                itsp[j] = itsp[j + 1];
            }
        }
        for (int it : itsp) {
//            Log.i("Debug","ITSP十进制数值:"+it);
        }
//        for (int i=0;i<RAND.length;i++){
//            if (RAND[i]<0){
//                int str=256+RAND[i];
//                RAND[i]=(byte)str;
//            }
//        }
        int[] newData = OperationUtil.byteToBinary(Data);
        int[] newRAND = OperationUtil.byteToBinary(RAND);
        StringBuffer stringBuffer = new StringBuffer();
        for (int i : newData) {
            stringBuffer.append(i);
        }
//        Log.i("Debug","Data转为二进制数组:"+stringBuffer.toString());
        StringBuffer stringBuffer1 = new StringBuffer();
        for (int i : newRAND) {
            stringBuffer1.append(i);
        }
//        Log.i("Debug","RAND转为二进制数组:"+stringBuffer1.toString());
        int flag = itsp[3];
        //转换为list集合
        int[] reverseData = reverseArray(newData);//第一次反转
        List<Integer> list = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list = Arrays.stream(reverseData).boxed().collect(Collectors.toList());
        }

        StringBuffer stringBuffer2 = new StringBuffer();
        for (Integer in : list) {
            stringBuffer2.append(in);
        }
//        Log.i("Debug","DATA第一次反转:"+stringBuffer2.toString());
        //插入
        int i = 0;
        for (; (flag - i <= 16) && (list.size() < 32); ) {
            list.add(flag, newRAND[i]);
            i++;
            flag = flag + itsp[3 - (i % 4)] + 1;
        }
        StringBuffer stringBuffer3 = new StringBuffer();
        for (Integer in : list) {
            stringBuffer3.append(in);
        }
//        Log.i("Debug","插值:"+stringBuffer3.toString());

        for (; list.size() < 32; ) {
            list.add(newRAND[i]);
            i++;
        }


        //从list集合转换为数组
        int[] dataAIp = new int[10];
        int count = 0;
        for (Integer integer : list) {
            int valueOf = Integer.valueOf(integer);
            if (dataAIp.length == count) dataAIp = Arrays.copyOf(dataAIp, count * 2);
            dataAIp[count++] = valueOf;
        }
        dataAIp = Arrays.copyOfRange(dataAIp, 0, count);

        StringBuffer stringBuffer4 = new StringBuffer();
        for (int a : dataAIp) {
            stringBuffer4.append(a);
        }
//        Log.i("Debug","插值后转为int数组:"+stringBuffer4.toString());

        int[] reverseDataAIp = reverseArray(dataAIp);
        StringBuffer stringBuffer5 = new StringBuffer();
        for (int a : reverseDataAIp) {
            stringBuffer5.append(a);
        }
//        Log.i("Debug","int数组反转:"+stringBuffer5.toString());
        //将做好插值的int数组转化为byte数组
        byte[] byteAfterIp = OperationUtil.binaryToByte(reverseDataAIp);
        byte[] reversedByteAfterIp = reverseArray(byteAfterIp);
        for (byte by : reversedByteAfterIp) {
//            Log.i("Debug", String.valueOf(by));
        }
        int[] st = OperationUtil.byteToBinary(byteAfterIp);
        StringBuffer stringBuffer6 = new StringBuffer();
        for (int a : st) {
            stringBuffer6.append(a);
        }
//        Log.i("Debug","最终结果二进制:"+stringBuffer6.toString());
        return reversedByteAfterIp;
    }
}
