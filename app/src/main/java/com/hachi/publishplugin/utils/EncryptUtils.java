package com.hachi.publishplugin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;



public final class EncryptUtils {

    private EncryptUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }



    public static String encrypt(String strSrc, String encName) {
        MessageDigest md = null;
        String strDes = null;

        byte[] bt = strSrc.getBytes();
        try {
            if (encName == null || encName.isEmpty()) {
                encName = "SHA-256";
            }
            md = MessageDigest.getInstance(encName);            md.update(bt);
            strDes = bytes2Hex(md.digest());         } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }


    public static String encryptMD2ToString(String data) {
        return encryptMD2ToString(data.getBytes());
    }


    public static String encryptMD2ToString(byte[] data) {
        return bytes2HexString(encryptMD2(data));
    }


    public static byte[] encryptMD2(byte[] data) {
        return hashTemplate(data, "MD2");
    }


    public static String encryptMD5ToString(String data) {
        return encryptMD5ToString(data.getBytes());
    }


    public static String encryptMD5ToString(String data, String salt) {
        return bytes2HexString(encryptMD5((data + salt).getBytes()));
    }


    public static String encryptMD5ToString(byte[] data) {
        return bytes2HexString(encryptMD5(data));
    }


    public static String encryptMD5ToString(byte[] data, byte[] salt) {
        if (data == null || salt == null) return null;
        byte[] dataSalt = new byte[data.length + salt.length];
        System.arraycopy(data, 0, dataSalt, 0, data.length);
        System.arraycopy(salt, 0, dataSalt, data.length, salt.length);
        return bytes2HexString(encryptMD5(dataSalt));
    }


    public static byte[] encryptMD5(byte[] data) {
        return hashTemplate(data, "MD5");
    }


    public static String encryptMD5File2String(String filePath) {
        File file = isSpace(filePath) ? null : new File(filePath);
        return encryptMD5File2String(file);
    }


    public static byte[] encryptMD5File(String filePath) {
        File file = isSpace(filePath) ? null : new File(filePath);
        return encryptMD5File(file);
    }


    public static String encryptMD5File2String(File file) {
        return bytes2HexString(encryptMD5File(file));
    }


    public static byte[] encryptMD5File(File file) {
        if (file == null) return null;
        FileInputStream fis = null;
        DigestInputStream digestInputStream;
        try {
            fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            digestInputStream = new DigestInputStream(fis, md);
            byte[] buffer = new byte[256 * 1024];
            while (true) {
                if (!(digestInputStream.read(buffer) > 0)) break;
            }
            md = digestInputStream.getMessageDigest();
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        } finally {
//            CloseUtils.closeIO(fis);
        }
    }


    public static String encryptSHA1ToString(String data) {
        return encryptSHA1ToString(data.getBytes());
    }


    public static String encryptSHA1ToString(byte[] data) {
        return bytes2HexString(encryptSHA1(data));
    }


    public static byte[] encryptSHA1(byte[] data) {
        return hashTemplate(data, "SHA1");
    }


    public static String encryptSHA224ToString(String data) {
        return encryptSHA224ToString(data.getBytes());
    }


    public static String encryptSHA224ToString(byte[] data) {
        return bytes2HexString(encryptSHA224(data));
    }


    public static byte[] encryptSHA224(byte[] data) {
        return hashTemplate(data, "SHA224");
    }


    public static String encryptSHA256ToString(String data) {
        return encryptSHA256ToString(data.getBytes());
    }


    public static String encryptSHA256ToString(byte[] data) {
        return bytes2HexString(encryptSHA256(data));
    }


    public static byte[] encryptSHA256(byte[] data) {
        return hashTemplate(data, "SHA256");
    }


    public static String encryptSHA384ToString(String data) {
        return encryptSHA384ToString(data.getBytes());
    }


    public static String encryptSHA384ToString(byte[] data) {
        return bytes2HexString(encryptSHA384(data));
    }


    public static byte[] encryptSHA384(byte[] data) {
        return hashTemplate(data, "SHA384");
    }


    public static String encryptSHA512ToString(String data) {
        return encryptSHA512ToString(data.getBytes());
    }


    public static String encryptSHA512ToString(byte[] data) {
        return bytes2HexString(encryptSHA512(data));
    }


    public static byte[] encryptSHA512(byte[] data) {
        return hashTemplate(data, "SHA512");
    }


    private static byte[] hashTemplate(byte[] data, String algorithm) {
        if (data == null || data.length <= 0) return null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String encryptHmacMD5ToString(String data, String key) {
        return encryptHmacMD5ToString(data.getBytes(), key.getBytes());
    }


    public static String encryptHmacMD5ToString(byte[] data, byte[] key) {
        return bytes2HexString(encryptHmacMD5(data, key));
    }


    public static byte[] encryptHmacMD5(byte[] data, byte[] key) {
        return hmacTemplate(data, key, "HmacMD5");
    }


    public static String encryptHmacSHA1ToString(String data, String key) {
        return encryptHmacSHA1ToString(data.getBytes(), key.getBytes());
    }


    public static String encryptHmacSHA1ToString(byte[] data, byte[] key) {
        return bytes2HexString(encryptHmacSHA1(data, key));
    }


    public static byte[] encryptHmacSHA1(byte[] data, byte[] key) {
        return hmacTemplate(data, key, "HmacSHA1");
    }


    public static String encryptHmacSHA224ToString(String data, String key) {
        return encryptHmacSHA224ToString(data.getBytes(), key.getBytes());
    }


    public static String encryptHmacSHA224ToString(byte[] data, byte[] key) {
        return bytes2HexString(encryptHmacSHA224(data, key));
    }


    public static byte[] encryptHmacSHA224(byte[] data, byte[] key) {
        return hmacTemplate(data, key, "HmacSHA224");
    }


    public static String encryptHmacSHA256ToString(String data, String key) {
        return encryptHmacSHA256ToString(data.getBytes(), key.getBytes());
    }


    public static String encryptHmacSHA256ToString(byte[] data, byte[] key) {
        return bytes2HexString(encryptHmacSHA256(data, key));
    }


    public static byte[] encryptHmacSHA256(byte[] data, byte[] key) {
        return hmacTemplate(data, key, "HmacSHA256");
    }


    public static String encryptHmacSHA384ToString(String data, String key) {
        return encryptHmacSHA384ToString(data.getBytes(), key.getBytes());
    }


    public static String encryptHmacSHA384ToString(byte[] data, byte[] key) {
        return bytes2HexString(encryptHmacSHA384(data, key));
    }


    public static byte[] encryptHmacSHA384(byte[] data, byte[] key) {
        return hmacTemplate(data, key, "HmacSHA384");
    }


    public static String encryptHmacSHA512ToString(String data, String key) {
        return encryptHmacSHA512ToString(data.getBytes(), key.getBytes());
    }


    public static String encryptHmacSHA512ToString(byte[] data, byte[] key) {
        return bytes2HexString(encryptHmacSHA512(data, key));
    }


    public static byte[] encryptHmacSHA512(byte[] data, byte[] key) {
        return hmacTemplate(data, key, "HmacSHA512");
    }


    private static byte[] hmacTemplate(byte[] data, byte[] key, String algorithm) {
        if (data == null || data.length == 0 || key == null || key.length == 0) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }



    public static String DES_Transformation = "DES/ECB/NoPadding";
    private static final String DES_Algorithm = "DES";

//
//    public static byte[] encryptDES2Base64(byte[] data, byte[] key) {
//        return base64Encode(encryptDES(data, key));
//    }


    public static String encryptDES2HexString(byte[] data, byte[] key) {
        return bytes2HexString(encryptDES(data, key));
    }


    public static byte[] encryptDES(byte[] data, byte[] key) {
        return desTemplate(data, key, DES_Algorithm, DES_Transformation, true);
    }


//    public static byte[] decryptBase64DES(byte[] data, byte[] key) {
//        return decryptDES(base64Decode(data), key);
//    }


    public static byte[] decryptHexStringDES(String data, byte[] key) {
        return decryptDES(hexString2Bytes(data), key);
    }


    public static byte[] decryptDES(byte[] data, byte[] key) {
        return desTemplate(data, key, DES_Algorithm, DES_Transformation, false);
    }



    public static String TripleDES_Transformation = "DESede/ECB/NoPadding";
    private static final String TripleDES_Algorithm = "DESede";



//    public static byte[] encrypt3DES2Base64(byte[] data, byte[] key) {
//        return base64Encode(encrypt3DES(data, key));
//    }


    public static String encrypt3DES2HexString(byte[] data, byte[] key) {
        return bytes2HexString(encrypt3DES(data, key));
    }


    public static byte[] encrypt3DES(byte[] data, byte[] key) {
        return desTemplate(data, key, TripleDES_Algorithm, TripleDES_Transformation, true);
    }


//    public static byte[] decryptBase64_3DES(byte[] data, byte[] key) {
//        return decrypt3DES(base64Decode(data), key);
//    }


    public static byte[] decryptHexString3DES(String data, byte[] key) {
        return decrypt3DES(hexString2Bytes(data), key);
    }


    public static byte[] decrypt3DES(byte[] data, byte[] key) {
        return desTemplate(data, key, TripleDES_Algorithm, TripleDES_Transformation, false);
    }



    public static String AES_Transformation = "AES/ECB/NoPadding";
    private static final String AES_Algorithm = "AES";



//    public static byte[] encryptAES2Base64(byte[] data, byte[] key) {
//        return base64Encode(encryptAES(data, key));
//    }


    public static String encryptAES2HexString(byte[] data, byte[] key) {
        return bytes2HexString(encryptAES(data, key));
    }


    public static byte[] encryptAES(byte[] data, byte[] key) {
        return desTemplate(data, key, AES_Algorithm, AES_Transformation, true);
    }


//    public static byte[] decryptBase64AES(byte[] data, byte[] key) {
//        return decryptAES(base64Decode(data), key);
//    }


    public static byte[] decryptHexStringAES(String data, byte[] key) {
        return decryptAES(hexString2Bytes(data), key);
    }


    public static byte[] decryptAES(byte[] data, byte[] key) {
        return desTemplate(data, key, AES_Algorithm, AES_Transformation, false);
    }


    public static byte[] desTemplate(byte[] data, byte[] key, String algorithm, String transformation, boolean isEncrypt) {
        if (data == null || data.length == 0 || key == null || key.length == 0) return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            Cipher cipher = Cipher.getInstance(transformation);
            SecureRandom random = new SecureRandom();
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, random);
            return cipher.doFinal(data);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    private static String bytes2HexString(byte[] bytes) {
        if (bytes == null) return null;
        int len = bytes.length;
        if (len <= 0) return null;
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
        }
        return new String(ret);
    }



    public static byte[] hexString2Bytes(String hexString) {
        if (isSpace(hexString)) return null;
        int len = hexString.length();
        if (len % 2 != 0) {
            hexString = "0" + hexString;
            len = len + 1;
        }
        char[] hexBytes = hexString.toUpperCase().toCharArray();
        byte[] ret = new byte[len >> 1];
        for (int i = 0; i < len; i += 2) {
            ret[i >> 1] = (byte) (hex2Dec(hexBytes[i]) << 4 | hex2Dec(hexBytes[i + 1]));
        }
        return ret;
    }


    private static int hex2Dec(char hexChar) {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;
        } else {
            throw new IllegalArgumentException();
        }
    }


//    private static byte[] base64Encode(byte[] input) {
//        return Base64.encode(input, Base64.NO_WRAP);
//        Base64.decodeBase64(authorization.split(" ")[1])
//    }
//
//
//    private static byte[] base64Decode(byte[] input) {
//        return Base64.decode(input, Base64.NO_WRAP);
//    }

    private static boolean isSpace(String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
