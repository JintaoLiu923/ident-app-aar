package com.hachi.publishplugin.utils;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcV;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Objects;


/**
 * NfcV操作类
 * <p>
 *  * 用法
 *  * NfcV mNfcV = NfcV.get(tag);
 *  * mNfcV.connect();
 *  * NfcVUtil mNfcVutil = new NfcVUtil(mNfcV);
 *  * 取得UID
 *  * mNfcVutil.getUID();
 *  * 读取block在1位置的内容
 *  * mNfcVutil.readOneBlock(1);
 *  * 从位置7开始读2个block的内容
 *  * mNfcVutil.readBlocks(7, 2);
 *  * 取得block的个数
 *  * mNfcVutil.getBlockNumber();
 *  * 取得1个block的长度
 *  * mNfcVutil.getOneBlockSize();
 *  * 往位置1的block写内容
 *  * mNfcVutil.writeBlock(1, new byte[]{0, 0, 0, 0})
 *  
 */

public class NfcVTmp {
    private static final String TAG = "NfcVTmp";
    private NfcV mNfcV;
    /**
     * UID数组行式
     */
    private byte[] ID;
    private String UID;
    private String DSFID;
    private String AFI;
    /**
     * block的个数
     */
    private int blockNumber;
    /**
     * 一个block长度
     */
    private int oneBlockSize;
    /**
     * 信息
     */
    private byte[] information;

    /**
     * 初始化
     *
     * @throws IOException  
     */
    public NfcVTmp(Tag tag) {
        mNfcV = NfcV.get(tag);
        if (mNfcV == null) {
            return;
        }
        try {
            mNfcV.connect();
            ID = this.mNfcV.getTag().getId();
            byte[] uid = new byte[ID.length];
            int j = 0;
//            for (int i = ID.length - 1; i >= 0; i--) {
//                uid[j] = ID[i];
//                j++;
//            }

            for (int i = 0; i < ID.length; i++) {
                uid[j] = ID[i];
                j++;
            }

            this.UID = printHexString(uid);
            getInfoRmation();
        } catch (IOException e) {
            mNfcV = null;
        }
    }

    /**
     * 获取UID
     */
    public String getUID() {
        if (TextUtils.isEmpty(UID)) {
            return null;
        }
        String regex = "(.{2})";
        UID = UID.replaceAll(regex, "$1-");
        UID = UID.substring(0, UID.length() - 1).toUpperCase();
        return UID;
    }

    /**
     * 取得标签信息 
     */
    public byte[] getInfoRmation() {
        if (mNfcV == null) {
            return null;
        }
        byte[] cmd = new byte[10];
        cmd[0] = (byte) 0x22; // flag
        cmd[1] = (byte) 0x2B; // command
        System.arraycopy(ID, 0, cmd, 2, ID.length); // UID
        try {
            LogUtil.d(TAG, "getInfo --> " + OperationUtil.bytesToHexString(cmd));
            information = mNfcV.transceive(cmd);
            blockNumber = information[12];
            oneBlockSize = information[13];
            AFI = printHexString(new byte[]{information[11]});
            DSFID = printHexString(new byte[]{information[10]});
        } catch (IOException e) {
            return null;
        }
        return information;
    }

    public String getDSFID() {
        return DSFID;
    }

    public String getAFI() {
        return AFI;
    }

    public int getBlockNumber() {
        return blockNumber + 1;
    }

    public int getOneBlockSize() {
        return oneBlockSize + 1;
    }

    /**
     * 读取从begin开始end个block
     */
    public String readBlocks(int begin, int count) {
        //qq d 用户数据存储是23个块，第一个自己是存储块大小，第二个单块字节数目，第三个是版本号
//        if ((begin + count) > blockNumber) {
//            count = blockNumber - begin;
//        }
//        StringBuilder data = new StringBuilder();
//        for (int i = begin; i < count + begin; i++) {
//            data.append(readOneBlock(i));
//        }
//        return data.toString();

        if (mNfcV == null) {
            return null;
        }
        byte[] cmd = new byte[12];
        cmd[0] = (byte) 0x22;
        cmd[1] = (byte) 0x23;
        System.arraycopy(ID, 0, cmd, 2, ID.length); // UID
        cmd[10] = (byte) begin;
        cmd[11] = (byte) count;
        byte[] res;
        try {
            res = mNfcV.transceive(cmd);
            if (res[0] == 0x00) {
                byte[] block = new byte[res.length - 1];
                System.arraycopy(res, 1, block, 0, res.length - 1);
                return printHexString(block);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     *  读取一个位置在position的block
     */
    public String readOneBlock(int position) {
        if (mNfcV == null) {
            return null;
        }
        byte[] cmd = new byte[11];
        cmd[0] = (byte) 0x22;
        cmd[1] = (byte) 0x20;
        System.arraycopy(ID, 0, cmd, 2, ID.length); // UID
        cmd[10] = (byte) position;
        byte[] res;
        try {
            LogUtil.d(TAG, "readOneBlock:cmd -->" + printHexString(cmd));
            res = mNfcV.transceive(cmd);
            LogUtil.d(TAG, "readOneBlock:result -->" + printHexString(res));
            if (res[0] == 0x00) {
                byte[] block = new byte[res.length - 1];
                System.arraycopy(res, 1, block, 0, res.length - 1);
                return printHexString(block);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * 将byte[]转换成16进制字符串
     *  
     */
    private String printHexString(byte[] data) {
        StringBuilder s = new StringBuilder();
        for (byte datum : data) {
            String hex = Integer.toHexString(datum & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            s.append(hex);
        }
        return s.toString();
    }

    /**
     * 将数据写入到block
     *  
     */
    public boolean writeBlock(int position, byte[] data) {
        if (data == null || data.length != 4) {
            return false;
        }
        byte[] cmd = new byte[15];
        cmd[0] = (byte) 0x22;
        cmd[1] = (byte) 0x21;
        System.arraycopy(ID, 0, cmd, 2, ID.length); // UID
        //block
        cmd[10] = (byte) position;
        //value
        System.arraycopy(data, 0, cmd, 11, data.length);
//        LogUtil.d(TAG, "write Block :cmd -->" + printHexString(cmd));
        byte[] rsp;
        try {
            rsp = mNfcV.transceive(cmd);
            if (rsp[0] == 0x00) {

                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public void close() {
        try {
            if (mNfcV != null) {
                mNfcV.close();
            }
            mNfcV = null;
        } catch (IOException e) {
            mNfcV = null;
        }
    }


    /**
     * 温控标签--配置页信息录入
     * command:
     * 01 -- ITSP0,1,2,3
     * 02 -- ITSP4,5,6,7
     * 03 -- TMPK1,TMPK0,TMPB1,TMPB0
     * 04 -- TMAX,TMIN,TINTX,TINTN
     */

    public boolean writeConfigBlock(byte[] data, byte command) {
        byte cmd[] = new byte[16];
        cmd[0] = (byte) 0x22;
        cmd[1] = (byte) 0xBD;
        cmd[2] = (byte) 0x04;
        System.arraycopy(ID, 0, cmd, 3, ID.length); // UID
        //block
        cmd[11] = command;//Config ID
        //value
        System.arraycopy(data, 0, cmd, 12, data.length);

        byte[] rsp = new byte[0];
        try {
            LogUtil.d(TAG, "cmd -->" + EncryptUtils.bytes2Hex(cmd));
            rsp = mNfcV.transceive(cmd);
        } catch (IOException e) {
            return false;
        }
        //           StringBuilder sb=new StringBuilder();
        //           for (byte b:rsp){
        //               sb.append(b+" ");
        //           }
        //           LogUtil.i(TAG,"写入ITSP的返回值:"+sb.toString());
        LogUtil.i(TAG, "写入配置指令响应:" + EncryptUtils.bytes2Hex(rsp));
        if (rsp[0] == 0x00)
            return true;
        return false;
    }


    /**
     * 温控标签
     * Reset ADC
     *
     * @return
     */
    public boolean ResetAdc() {
        byte[] cmd = new byte[11];
        cmd[0] = (byte) 0x22;//flags
        cmd[1] = (byte) 0xb9;//command
        cmd[2] = (byte) 0x04;//ic mc
        System.arraycopy(ID, 0, cmd, 3, ID.length);
        StringBuffer sb1 = new StringBuffer();
        for (byte b : cmd) {
            sb1.append(b + " ");
        }
        LogUtil.i(TAG, "Reset ADC的请求格式为:" + sb1);
        byte[] rand = new byte[0];
        try {
            rand = mNfcV.transceive(cmd);
            StringBuffer sb = new StringBuffer();
            for (byte a : rand) {
                sb.append(a + " ");
            }
            LogUtil.i(TAG, "Reset ADC得到的结果格式为:" + sb);
            if (rand[0] == 0x00) {
                return true;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        }

        return false;
    }

    /**
     * 设置状态位映射
     *
     * @param data
     * @return
     */
    public boolean setOfmsel(byte[] data) {
        return writeByte((byte) 1, data);
    }

    /**
     * 设置密码保护区
     *
     * @param data
     * @return
     */
    public boolean setAuth0(byte[] data) {
        return writeByte((byte) 2, data);
    }

    /**
     * 0，ADC 工作在静默模式
     * 1，ADC 正常工作模式
     *
     * @param data
     * @return
     */
    public boolean setMode(byte[] data) {
        return writeByte((byte) 2, data);
    }

    /**
     * 设置失败认证次数
     *
     * @param data
     * @return
     */
    public boolean setAflmt1(byte[] data) {
        return writeByte((byte) 2, data);
    }

    /**
     * write byte
     */
    public boolean writeByte(byte byteid, byte[] data) {
        byte[] cmd = new byte[16];
        cmd[0] = (byte) 0x22;//flag
        cmd[1] = (byte) 0xB6;//command
        cmd[2] = (byte) 0x04;//IC MFG CODE
        System.arraycopy(ID, 0, cmd, 3, ID.length);
        cmd[11] = byteid;
        cmd[12] = data[0];
        cmd[13] = data[1];
        cmd[14] = data[2];
        cmd[15] = data[3];
        try {
            byte[] wb = mNfcV.transceive(cmd);
            StringBuffer sb = new StringBuffer();
            for (byte a : wb) {
                sb.append(a + " ");
            }
            LogUtil.d(TAG, "writebyte命令返回的格式为:" + sb);
            //响应码为0时成功
            if (wb[0] == 0x00)
                return true;
        } catch (IOException e) {
            LogUtil.d(TAG, "writeByte失败");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 15693普通芯片设置密码
     */
    public boolean setPWD(byte[] PWD) {
        LogUtil.i(TAG, "15693修改密码开始");
        if (PWD == null || PWD.length != 4 || mNfcV == null) {
            return false;
        }
        byte[] cmd = new byte[16];
        cmd[0] = 0x22;//flags
        cmd[1] = (byte) 0xB4;//cmd
        cmd[2] = 0x04;//IC MC Code
        System.arraycopy(ID, 0, cmd, 3, ID.length);
        cmd[11] = 0x01;
        cmd[12] = PWD[0];
        cmd[13] = PWD[1];
        cmd[14] = PWD[2];
        cmd[15] = PWD[3];
        LogUtil.d(TAG, "setPWD --> " + OperationUtil.bytesToHexString(cmd));
        try {
            byte[] result = mNfcV.transceive(cmd);
            StringBuilder sb = new StringBuilder();
            for (byte a : result) {
                sb.append(a).append(" ");
            }
            LogUtil.i(TAG, "15693密码修改结果:" + sb);
            if (result[0] == 0) {
                return true;
            }
        } catch (IOException e) {
            LogUtil.i(TAG, "15693密码修改失败");
            return false;
        }
        return false;
    }

    public boolean setRwKey(byte[] PWD) {
        if (PWD == null || PWD.length != 4 || mNfcV == null) {
            return false;
        }
        byte[] cmd = new byte[16];
        cmd[0] = 0x22;//flags
        cmd[1] = (byte) 0xB4;//cmd
        cmd[2] = 0x04;//IC MC Code
        System.arraycopy(ID, 0, cmd, 3, ID.length);
        cmd[11] = 0x02;
        cmd[12] = PWD[0];
        cmd[13] = PWD[1];
        cmd[14] = PWD[2];
        cmd[15] = PWD[3];
        LogUtil.d(TAG, "setRwKey --> " + OperationUtil.bytesToHexString(cmd));
        try {
            byte[] result = mNfcV.transceive(cmd);
            StringBuilder sb = new StringBuilder();
            for (byte a : result) {
                sb.append(a).append(" ");
            }
            LogUtil.i(TAG, "15693密码修改结果:" + sb);
            if (result[0] == 0) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * 锁定密码
     *
     * @param PWD
     * @return
     */
    public boolean lockPassword(byte[] PWD) {
        LogUtil.i(TAG, "15693锁定密码开始");
        if (PWD == null || PWD.length != 4 || mNfcV == null) {
            return false;
        }
        byte[] cmd = new byte[16];
        cmd[0] = 0x22;//flags
        cmd[1] = (byte) 0xB5;//cmd
        cmd[2] = 0x04;//IC MC Code
        System.arraycopy(ID, 0, cmd, 3, ID.length);
        cmd[11] = 0x01;
        cmd[12] = PWD[0];
        cmd[13] = PWD[1];
        cmd[14] = PWD[2];
        cmd[15] = PWD[3];
        LogUtil.d(TAG, "lockPassword --> " + OperationUtil.bytesToHexString(cmd));
        try {
            byte[] result = mNfcV.transceive(cmd);
            StringBuilder sb = new StringBuilder();
            for (byte a : result) {
                sb.append(a).append(" ");
            }
            LogUtil.i(TAG, "lockPassword result --> " + sb);
            if (result[0] == 0) {
                return true;
            }
        } catch (IOException e) {
            LogUtil.i(TAG, "lockPassword result --> fail");
            return false;
        }
        return false;
    }


    /**
     * 获取随机数
     */
    public byte[] getRandom() {
        if (ID == null || mNfcV == null) {
            return null;
        }
        byte[] cmd = new byte[11];
        cmd[0] = 0x22;//flags
        cmd[1] = (byte) 0xB2;//cmd
        cmd[2] = 0x04;//IC Code
        System.arraycopy(ID, 0, cmd, 3, ID.length);//UID

        LogUtil.d(TAG, "getRandom cmd -->" + OperationUtil.bytesToHexString(cmd));

        try {
            byte[] rsp = mNfcV.transceive(cmd);
            if (rsp[0] == 0x00) {
                byte[] block = new byte[rsp.length - 1];
                System.arraycopy(rsp, 1, block, 0, rsp.length - 1);
                return block;
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * 密钥认证
     */
    public boolean verifyPwd(byte[] PWD) {
        if (PWD == null || PWD.length != 4 || mNfcV == null) {
            return false;
        }
        byte[] cmd = new byte[16];
        cmd[0] = 0x22;//flags
        cmd[1] = (byte) 0xB3;//cmd
        cmd[2] = 0x04;//IC MC Code
        System.arraycopy(ID, 0, cmd, 3, ID.length);
        cmd[11] = 0x01;//PWD ID
        cmd[12] = PWD[0];
        cmd[13] = PWD[1];
        cmd[14] = PWD[2];
        cmd[15] = PWD[3];

        try {
//            LogUtil.d(TAG, "verifyPwd cmd -->" + OperationUtil.bytesToHexString(cmd));
            byte[] result = mNfcV.transceive(cmd);
            StringBuilder sb = new StringBuilder();
            for (byte a : result) {
                sb.append(a).append(" ");
            }
            LogUtil.i(TAG, "15693密码认证结果:" + sb.toString());
            if (result[0] == 0) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * 用随机数对密码进行异或
     */
    private byte[] xorPWD(byte[] pwd, byte[] random) {
        byte[] data = new byte[pwd.length];
        for (int i = 0; i < pwd.length; i++) {
            data[i] = (byte) (pwd[i] ^ random[i % 2]);
        }
        return data;
    }


    /**
     * 15693普通标签写入NDEF
     */
    public static boolean writeNdef15693(Tag tag, String ndefUri) {
        //写非NDEF格式的数据
        boolean isNdef = false;
        String[] techList = tag.getTechList();
        for (String tech : techList) {
            if (tech.contains("NdefFormatable")) {
                break;
            } else if (tech.contains("Ndef")) {
                isNdef = true;
                break;
            }
        }

        //写入NDEF
        NdefRecord ndefRecord = createUriRecord1(ndefUri);
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);
//        LogUtil.i("NDEF",ndefMessage);
        try {
            if (isNdef) {
                Ndef ndef = Ndef.get(tag);
                ndef.connect();
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
            } else {
                NdefFormatable ndef = NdefFormatable.get(tag);
                ndef.connect();
                ndef.format(ndefMessage);
                ndef.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean writeTmpNedf(byte[] data) {
        try {
            int pages = data.length / 4;
            if (data.length % 4 > 0) {
                pages++;
            }
            for (int i = 0; i < pages; ++i) {
                int index = i << 2;

                boolean flag = writeBlock(0 + i, new byte[]{
                        index >= data.length ? (byte) 0x00 : data[index],
                        index + 1 >= data.length ? (byte) 0x00 : data[index + 1],
                        index + 2 >= data.length ? (byte) 0x00 : data[index + 2],
                        index + 3 >= data.length ? (byte) 0x00 : data[index + 3]
                });
                if (!flag) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将Uri转成NdefRecord
     */
    private static NdefRecord createUriRecord1(String uriStr) {
        byte prefix = 0;
        for (Byte b : UriPrefix.URI_PREFIX_MAP.keySet()) {
            String prefixStr = Objects.requireNonNull(UriPrefix.URI_PREFIX_MAP.get(b)).toLowerCase();
            if ("".equals(prefixStr))
                continue;
            if (uriStr.toLowerCase().startsWith(prefixStr)) {
                prefix = b;
                uriStr = uriStr.substring(prefixStr.length());
                break;
            }
        }
        byte[] data = new byte[1 + uriStr.length()];
        data[0] = prefix;
        System.arraycopy(uriStr.getBytes(), 0, data, 1, uriStr.length());
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], data);
    }

    /**
     * write byte
     */
    public boolean writeBytes(byte byteid, byte[] data) {
        byte[] cmd = new byte[16];
        cmd[0] = (byte) 0x22;//flag
        cmd[1] = (byte) 0xb6;//command
        cmd[2] = (byte) 0x04;//IC MFG CODE
        System.arraycopy(ID, 0, cmd, 3, ID.length);
        cmd[11] = byteid;
        cmd[12] = data[0];
        cmd[13] = data[1];
        cmd[14] = data[2];
        cmd[15] = data[3];
        try {
            byte[] wb = mNfcV.transceive(cmd);
            StringBuffer sb = new StringBuffer();
            for (byte a : wb) {
                sb.append(a + " ");
            }
            LogUtil.i(TAG, "writebyte命令返回的格式为:" + sb);
            //响应码为0时成功
            if (wb[0] == 0x00)
                return true;
        } catch (IOException e) {
            LogUtil.i(TAG, "失败");
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 写证书
     */
    public boolean writeCert(String newCert, String decodeCert, byte flag_begin, byte flag_over, int certPosition, int flagPosition) {
        if (TextUtils.isEmpty(newCert)) {
            return false;
        }
        byte[] cert = OperationUtil.stringToBytes(newCert, newCert.length());
        for (byte b : cert) {
            LogUtil.i(TAG, "证书转字节:" + b);
        }
        if (!writeBlock(flagPosition, new byte[]{0x00, 0x00, 0x00, flag_begin})) {
            return false;
        }

        boolean flag = writeBlock(certPosition, cert);
        if (flag) {
            String read_cert = readOneBlock(certPosition);
            if (TextUtils.isEmpty(read_cert)) {
                return false;
            }
            LogUtil.i(TAG, "写入证书:readEncrypt:" + decodeCert + " read:" + read_cert);
            if (!writeBlock(flagPosition, new byte[]{0x00, 0x00, 0x00, flag_over})) {
                return false;
            }
            return decodeCert.equals(read_cert.toUpperCase());
        } else {
            return false;
        }
    }

    /**
     * 读证书
     */
    public String readCert() {
        return readOneBlock(22);
    }


//    /**
//     * 写入时间戳RasId
//     */
//    public boolean writeRasId(String config_rasId) {
//
//        String page1 = config_rasId.substring(0, 8);
//        String page2 = config_rasId.substring(8) + "000";
//        byte[] byte1 = OperationUtil.stringToBytes(page1, page1.length());
//        byte[] byte2 = OperationUtil.stringToBytes(page2, page2.length());
//        boolean flag1 = writeBlock(25, byte1);
//        if (!flag1) {
//            return false;
//        }
//        boolean flag2 = writeBlock(26, byte2);
//        if (!flag2) {
//            return false;
//        }
//        String read_page1 = readOneBlock(25);
//        String read_page2 = readOneBlock(26);
//        return read_page1.equals(page1) && read_page2.equals(page2);
//    }

    public boolean writeRasId2Tmp(String config_rasId, int position) {
        byte[] data = EncryptUtils.hexString2Bytes(config_rasId);
        int pages = data.length / 4;
        if (data.length % 4 > 0) {
            pages++;
        }
        for (int i = 0; i < pages; ++i) {
            int index = i << 2;
            boolean flag = writeBlock(position + i, new byte[]{
                    index >= data.length ? (byte) 0x00 : data[index],
                    index + 1 >= data.length ? (byte) 0x00 : data[index + 1],
                    index + 2 >= data.length ? (byte) 0x00 : data[index + 2],
                    index + 3 >= data.length ? (byte) 0x00 : data[index + 3]
            });
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    /**
     * 读标志位
     */
    public String readStatus(int position) {
        String status = readOneBlock(position);
        if (status == null) {
            return null;
        }
        return status.substring(6).toUpperCase();
    }

    public boolean writeStatus(byte code, int position, int bytePosition) {
        LogUtil.i(TAG, "执行写入标志位 code:" + OperationUtil.byteToString(code));
        byte[] data = new byte[4];
        if (bytePosition == 0) {
            data = new byte[]{0x00, 0x00, 0x00, code};
        } else if (bytePosition == 1) {
            data = new byte[]{0x00, 0x00, code, 0x00};
        }
        return writeBlock(position, data);
    }
}
