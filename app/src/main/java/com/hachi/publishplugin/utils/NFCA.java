package com.hachi.publishplugin.utils;

import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;

import static com.hachi.publishplugin.utils.OperationUtil.bytesToHexString;

/**
 * NFCA读写工具类
 */
public class NFCA {
    private static final String TAG = "NFCA";
    private NfcA nfcA;
    private byte[] _id;

    public NFCA(Tag tag) {
        nfcA = NfcA.get(tag);
        if (nfcA == null) {
            return;
        }
        try {
            nfcA.connect();
            byte[] id = tag.getId();
            _id = new byte[8];
            System.arraycopy(id, 0, _id, 0, 7);
            _id[_id.length - 1] = (byte) 0;
        } catch (Exception e) {
//            e.printStackTrace();
            nfcA = null;
        }
    }

    /**
     * RAS写入证书
     */
    public boolean writeCert(String certEncrypt, String certDecode) {
        if (TextUtils.isEmpty(certEncrypt) || TextUtils.isEmpty(certDecode)) {
            return false;
        }
        try {
            //格式转换
            byte[] cert = EncryptUtils.hexString2Bytes(certEncrypt);
            if (!writePage(0x11, new byte[]{0x00, 0x00, 0x00, cert[0]})) {
                return false;
            }

            boolean flag1 = writePage(0x10, new byte[]{cert[2], cert[3], cert[4], cert[5]});

            if (!flag1) {
                return false;
            }

            if (!writePage(0x11, new byte[]{0x00, 0x00, 0x00, cert[1]})) {
                return false;
            }

            //读取比对
            String cert_read = readCert();
            if (certDecode.substring(4).equals(cert_read)) {
                LogUtil.i(TAG, "证书写入成功");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LogUtil.i(TAG, "证书写入失败");
            return false;
        }
    }


    public boolean writeCert(String newCert) {
        if (TextUtils.isEmpty(newCert)) {
            return false;
        }
        try {
            //格式转换
            byte[] cert = EncryptUtils.hexString2Bytes(newCert);
            if (!writePage(0x11, new byte[]{0x00, 0x00, 0x00, 0x01})) {
                return false;
            }

            boolean flag1 = writePage(0x10, cert);

            if (!flag1) {
                return false;
            }

            if (!writePage(0x11, new byte[]{0x00, 0x00, 0x00, 0x00})) {
                return false;
            }

            //读取比对
            String cert_read = readCert();
            if (newCert.equals(cert_read)) {
                LogUtil.i(TAG, "证书写入成功");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LogUtil.i(TAG, "证书写入失败");
            return false;
        }
    }

    /**
     * RAS读证书
     */
    public String readCert() {
        try {
            byte[] data = readPages(0x10);
            if (data == null) {
                return "";
            }
            byte[] arr = new byte[]{data[0], data[1], data[2], data[3]};
            String cert = bytesToHexString(arr);
            LogUtil.i(TAG, "读取的Cert:" + cert);
            return cert;
        } catch (Exception e) {
            return "";
        }
    }

    public String analysisTag(byte rk, byte itsp) {
//        String padStr;
        if (nfcA == null) {
            return "";
        }
        try {
            byte[] transceive = nfcA.transceive(new byte[]{0x30, -128});
            Log.i(TAG, new String(transceive));


            byte[] data = Xor.deInterpolation(itsp, transceive);//去插值

            LogUtil.i(TAG, "输入的data:" + new String(data));

            byte[] data1 = Xor.writeEncode(data, rk);
            String padStr = Integer.toHexString((int) data1[1]);
            LogUtil.i(TAG, "PadStr --> " + padStr);


            return padStr;
//            Log.i("Debug", new String(transceive));
//            byte[] data = JniTools.DeInterpolation(itsp, transceive, transceive.length);//去插值
//            LogUtil.i("Debug", "输入的data:" + new String(data));
//            byte[] data1 = JniTools.readEncode(data, data.length, rk);
//            padStr = Integer.toHexString((int) data1[1]);
//            LogUtil.i("Debug", "PadStr:" + padStr);
//            return padStr;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * RAS写入时间戳
     */
    public boolean writeRasId(String config_rasId) {
        if (TextUtils.isEmpty(config_rasId)) {
            return false;
        }
        LogUtil.d(TAG,"config_rasId --> "+config_rasId);
        try {
            //处理biz
            byte[] data = EncryptUtils.hexString2Bytes(config_rasId);
            int pages = data.length / 4;
            if (data.length % 4 > 0) {
                pages++;
            }

            for (int i = 0; i < pages; ++i) {
                int index = i << 2;
                boolean flag = writePage(14 + i, new byte[]{
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
     * RAS读取状态位
     */
    public String analysisTag() {
//        String padStr;
        if (nfcA == null) {
            return "";
        }
        try {
            byte[] transceive = nfcA.transceive(new byte[]{0x30, -128});
            return EncryptUtils.bytes2Hex(transceive);
//            Log.i("Debug", new String(transceive));
//            byte[] data = JniTools.DeInterpolation(itsp, transceive, transceive.length);//去插值
//            LogUtil.i("Debug", "输入的data:" + new String(data));
//            byte[] data1 = JniTools.readEncode(data, data.length, rk);
//            padStr = Integer.toHexString((int) data1[1]);
//            LogUtil.i("Debug", "PadStr:" + padStr);
//            return padStr;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 读取标志位
     */
    public String readStatus() {
        LogUtil.i(TAG, "执行读取标志位");
        byte[] data = readPages(0x11);
        if (data == null) {
            return null;
        }
        String status = OperationUtil.byteToString(data[3]);
        LogUtil.i(TAG, "读取的标志位 --> " + status);
        return status;
    }

    /**
     * 写入标志位
     */
    public boolean writeStatus(byte code) {
        LogUtil.i(TAG, "执行写入标志位 code --> " + OperationUtil.byteToString(code));
        boolean flag = writePage(0x11, new byte[]{0x00, 0x00, 0x00, code});
        LogUtil.i(TAG, "执行写入标志位 return:--> " + flag);
        readStatus();
        return flag;
    }

    /**
     * 读取从index开始的4页数据
     */
    public byte[] readPages(int index) {
        if (nfcA == null) {
            return null;
        }
        byte[] command = new byte[2];
        command[0] = (byte) 0x30;
        command[1] = (byte) index;
        byte[] data;
        try {
            data = nfcA.transceive(command);
        } catch (IOException e) {
            return null;
        }
        if (data != null && data.length == 16) {
            LogUtil.d(TAG, "readPages " + index + ", data --> " + OperationUtil.bytesToHexString(data));
            return data;
        }
        return null;
    }

    /**
     * 读取从index开始的4页数据
     */
    public byte[] fastRead(int startIndex, int endIndex) {
        if (nfcA == null) {
            return null;
        }
        byte[] command = new byte[3];
        command[0] = (byte) 0x3A;
        command[1] = (byte) startIndex;
        command[2] = (byte) endIndex;
        byte[] data;
        try {
            data = nfcA.transceive(command);
        } catch (IOException e) {
            return null;
        }
        if (data != null) {
            return data;
        }
        return null;
    }

    /**
     * 向某页写入数据
     */
    public boolean writePage(int index, byte[] data) {
        if (data == null || data.length != 4 || nfcA == null) {
            return false;
        }
        byte[] command = new byte[6];
        command[0] = (byte) 0xA2;
        command[1] = (byte) index;
        command[2] = data[0];
        command[3] = data[1];
        command[4] = data[2];
        command[5] = data[3];
        try {
            byte[] response = nfcA.transceive(command);
            LogUtil.d(TAG, "writePage response --> " + OperationUtil.bytesToHexString(response) + ",index --> " + index);
            return response != null && response[0] == 10;
        } catch (IOException e) {
            return false;
        }
    }


    public boolean compWrite(int index, byte[] data) {
        if (data == null || data.length != 16 || nfcA == null) {
            return false;
        }
        byte[] command = new byte[2];
        command[0] = (byte) 0xA0;
        command[1] = (byte) index;

        byte[] command2 = new byte[16];
        System.arraycopy(data, 0, command2, 0, data.length); // UID
        try {
            byte[] response = nfcA.transceive(command);
            LogUtil.d(TAG, "compWrite response --> " + OperationUtil.bytesToHexString(response));
            byte[] response2 = nfcA.transceive(command2);
            LogUtil.d(TAG, "compWrite response --> " + OperationUtil.bytesToHexString(response));
            return response2 != null && response2[0] == 10;
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * 写NDEF数据
     */
    public boolean writeNDEF(byte[] data) {
        try {
//            NdefRecord ndefRecord = createUriRecord1(Constant.NDEF_URI);
//            byte[] payLoad = ndefRecord.getPayload();
//            StringBuilder s1 = new StringBuilder();
//            for (byte bc : payLoad) {
//                s1.append(String.format("%02X", bc));
//            }
//            LogUtil.i("Debug", "NDEF:" + s1.toString());
//            byte[] res = OperationUtil.stringToBytes(s1.toString(), s1.length());
//            LogUtil.i("Debug", "NDEF HEx:" + OperationUtil.bytesToHexString(res));
//            LogUtil.i("Debug", "写入NDEF用的WK：" + RK + "  RK:" + WK);
//            byte[] data = new byte[15 + res.length];
//            data[0] = (byte) 0xe1;//定义数据存储在数据区
//            data[1] = (byte) 0x10;//版本号
//            data[2] = (byte) 0x10;//数据区的存储器大小,1k
//            data[3] = (byte) 0x00;//表示在没有任何安全性的情况下授予读写权限
//            data[4] = (byte) 0x01;//
//            data[5] = (byte) 0x03;
//            data[6] = (byte) 0xa0;
//
//            data[7] = (byte) 0x0C;
//            data[8] = (byte) 0x34;
//            data[9] = (byte) 0x03;
//            data[10] = (byte) 0x1D;


//            data[11] = (byte) 0xD1;
//            data[12] = (byte) 0x01;   tnf
//            data[13] = (byte) 0x19;
//            data[14] = (byte) 0x55;  type
//            System.arraycopy(res, 0, data, 15, res.length);
            int pages = data.length / 4;
            if (data.length % 4 > 0) {
                pages++;
            }
//
//            StringBuilder s = new StringBuilder();
//            for (byte arr : data) {
//                s.append(arr).append(" ");
//            }
//            LogUtil.i("Debug","NDEF原文:"+s.toString());
//
//
//
//            // 与WK异或
//            for (int i = 4; i < data.length; i++) {
//                data[i] = (byte) (data[i] ^ WK);
//            }
//            LogUtil.i("Debug", "NDEF原文与WK异或:" + OperationUtil.bytesToHexString(data));
//
//            //与RK异或
//            for (int i = 4; i < data.length; i++) {
//                LogUtil.i("Debug", "执行RK异或" + RK + "data length:" + data.length);
//                data[i] = (byte) (data[i] ^ RK);
//            }
//            LogUtil.i("Debug", "NDEF原文与RK异或:" + OperationUtil.bytesToHexString(data));


            for (int i = 0; i < pages; ++i) {
                int index = i << 2;

                boolean flag = writePage(3 + i, new byte[]{
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

    public void close() {
        try {
            if (nfcA != null) {
                nfcA.close();
            }
            nfcA = null;
        } catch (IOException e) {
            nfcA = null;
        }
    }

    /**
     * 密码验证
     */
    public boolean authenticate(byte[] password) {
        if (password == null || password.length < 4 || nfcA == null) { //QQ A DUE TO bug c 长度大于4byte
            return false;
        }
        byte[] command = new byte[5];
        command[0] = (byte) 0x1B;
        command[1] = password[0];
        command[2] = password[1];
        command[3] = password[2];
        command[4] = password[3];
        try {

            byte[] response = nfcA.transceive(command);
            return response != null && response[0] == 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static NdefRecord createUriRecord1(String uriStr) {
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

}