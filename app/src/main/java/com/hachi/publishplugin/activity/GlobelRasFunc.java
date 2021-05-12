package com.hachi.publishplugin.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.hachi.publishplugin.R;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.utils.AppendUtil;
import com.hachi.publishplugin.utils.NFCV;
import com.hachi.publishplugin.utils.ServiceUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

/**
 * 日志组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class GlobelRasFunc {
    private static final String TAG = "GlobelRasFunc";

    private static boolean ifMusic = true;
    private static boolean isInit = false;
    private static int err_num;
    private static SoundPool sp;                   //声明SoundPool的引用
    private static HashMap<Integer, Integer> hm;    //声明HashMap存放声音
    private static int currStreamId;               //当前正在播放的声音id
    public static BigDecimal latitude;
    public static BigDecimal longitude;
    public static String regex = "(.{2})";
    public static double log;
    public static String mMapKey;
    public static boolean isRasIdModeOpen = false;

    public static void disableMusic() {
        ifMusic = false;
    }

    public static void enableMusic() {
        ifMusic = true;
    }

    public static void setIsRasIdModeOpen(boolean isRasIdModeOpen) {
        GlobelRasFunc.isRasIdModeOpen = isRasIdModeOpen;
    }

    public static boolean getIsRasIdModeOpen() {
        return isRasIdModeOpen;
    }

    public static ResultBean init(Context content, String key) {
        ResultBean resultBean = new ResultBean();
        try {
            mMapKey = key;
            if (!GlobelRasFunc.initSoundPool(content)) {
                resultBean.setErrno(TagErrorEnum.CHECK_MUSIC_FAIL.getCode());
                resultBean.setErrmsg(TagErrorEnum.CHECK_MUSIC_FAIL.getDescription());
                return resultBean;
            }
            String checkLogin = InitPlugin.checkLogin(content, Constant.username, Constant.password);
            if (TextUtils.isEmpty(checkLogin)) {
                resultBean.setErrno(TagErrorEnum.CHECK_LOGIN_FAIL.getCode());
                resultBean.setErrmsg(TagErrorEnum.CHECK_LOGIN_FAIL.getDescription());
                return resultBean;
            }
            if (!InitPlugin.initPos(content, key)) {
                resultBean.setErrno(TagErrorEnum.CHECK_LOCATION_FAIL.getCode());
                resultBean.setErrmsg(TagErrorEnum.CHECK_LOCATION_FAIL.getDescription());
                return resultBean;
            }
            if (!ServiceUtil.checkLocationService(content)) {
                resultBean.setErrno(TagErrorEnum.CHECK_LOCATION_FAIL.getCode());
                resultBean.setErrmsg(TagErrorEnum.CHECK_LOCATION_FAIL.getDescription());
                return resultBean;
            }
            if (!ServiceUtil.checkNetworkService(content)) {
                resultBean.setErrno(TagErrorEnum.CHECK_NET_FAIL.getCode());
                resultBean.setErrmsg(TagErrorEnum.CHECK_NET_FAIL.getDescription());
                return resultBean;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hm != null) {
            isInit = true;
        }
        resultBean.setErrno(TagErrorEnum.INIT_SUCCESS.getCode());
        resultBean.setErrmsg(TagErrorEnum.INIT_SUCCESS.getDescription());
        return resultBean;
    }

    public static void musicPlay(Context context, Integer type) {
        if (!ifMusic) {
            return;
        }
        switch (type) {
            case 0:
                break;
            case 1:
                GlobelRasFunc.playSound(context, 1, 0);
                break;
            case 2:
                break;
            default:
                break;
        }
    }

    //初始化声音池
    public static boolean initSoundPool(Context context) {
        sp = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);     //创建SoundPool对象
        hm = new HashMap<>();
        //加载声音文件，并设置为1号声音放入哈希
        hm.put(1, sp.load(context, R.raw.music, 1));
        return true;
    }

    public static void playSound(Context context, int sound, int loop) {        //播放声音的方法
        try {
            if (!isInit) {
                if (!TextUtils.isEmpty(mMapKey)) {
                    init(context, mMapKey);
                }
            }
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);  //获取AudioManager的引用
            float streamVolumeCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);      //获取当前音量
            float streamVolumeMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);       //获取系统最大音量
            float volume = streamVolumeCurrent / streamVolumeMax;                           //计算得到播放音量
            currStreamId = sp.play(hm.get(sound), volume, volume, 1, loop, 1.0f);
        } catch (Exception e) {

        }
    }

    /**
     * 读15693标签的UID
     */
    public static String readUid15693(Tag tag) {
        NFCV NFCV = new NFCV(tag);
        String uid = NFCV.getUID();
        NFCV.close();
        return uid.toUpperCase();
    }

    /**
     * 14443普通标签写入
     */
    public static boolean write14443tag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        Ndef ndef = Ndef.get(tag);
        try {
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ndef != null) {
                    ndef.close();
                }
            } catch (Exception e) {
                return true;
            }
        }
    }

    /**
     * F8213普通标签写入
     */
    public static boolean writeF8213Ndef(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        Ndef ndef = Ndef.get(tag);
        try {
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ndef != null) {
                    ndef.close();
                }
            } catch (Exception e) {
                return true;
            }
        }
    }

    /**
     * 读取14443标签UID
     */
    public static String readUid14443(Tag tag) {
        StringBuilder result = new StringBuilder();//uid
        String temp;
        StringBuilder uid = new StringBuilder();
        if (tag != null) {
            NfcA nfcA = NfcA.get(tag);
            try {
                nfcA.connect();
                byte[] payload = nfcA.transceive(new byte[]{0x30, 0});
                for (int j = 0; j < 8; j++) {
                    temp = Integer.toHexString(payload[j] & 0xFF);
                    if (temp.length() == 1) {
                        temp = "0" + temp;
                    }
                    result.append(temp);
                }

                /* 得到uid*/
//                for (int i = 0; i < 6; i++) {
//                    char a = result.charAt(i);
//                    uid.append(a);
//                }
//                for (int i = 8; i < 16; i++) {
//                    char a = result.charAt(i);
//                    uid.append(a);
//                }

                uid = AppendUtil.append(result, 0, 6);
                uid = uid.append(AppendUtil.append(result, 8, 16));
                uid = new StringBuilder(uid.toString().replaceAll(regex, "$1-"));
                uid = new StringBuilder(uid.substring(0, uid.length() - 1));
                return uid.toString().toUpperCase();
            } catch (IOException e) {
//                e.printStackTrace();
                return null;
            } finally {
                try {
                    nfcA.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                    //连接失败关闭时空指针
                    return uid.toString();
                }
            }
        } else {
            return null;
        }

    }

    /**
     * 读取F8213标签UID
     */
    public static String readUidF8213(Tag tag) {
        StringBuilder result = new StringBuilder();//uid
        String temp;
        StringBuilder uid = new StringBuilder();
        if (tag != null) {
            NfcA nfcA = NfcA.get(tag);
            try {
                nfcA.connect();
                byte[] payload = nfcA.transceive(new byte[]{0x30, 0});
                for (int j = 0; j < 8; j++) {
                    temp = Integer.toHexString(payload[j] & 0xFF);
                    if (temp.length() == 1) {
                        temp = "0" + temp;
                    }
                    result.append(temp);
                }
//                for (int i = 0; i < 16; i++) {
//                    char a = result.charAt(i);
//                    uid.append(a);
//                }
                uid = AppendUtil.append(result, 0, 16);
                uid = new StringBuilder(uid.toString().replaceAll(regex, "$1-"));
                uid = new StringBuilder(uid.substring(0, uid.length() - 1));
                return uid.toString().toUpperCase();
            } catch (IOException e) {
//                e.printStackTrace();
                return null;
            } finally {
                try {
                    nfcA.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                    //连接失败关闭时空指针
                    return uid.toString();
                }
            }
        } else {
            return null;
        }
    }

    /**
     * 读取标签UID
     */
    public static String readUid(Tag tag) {
        StringBuilder result = new StringBuilder();//uid
        String temp;
        StringBuilder uid = new StringBuilder();
        if (tag != null) {
            NfcA nfcA = NfcA.get(tag);
            try {
                nfcA.connect();
                byte[] payload = nfcA.transceive(new byte[]{0x30, 0});
                for (int j = 0; j < 8; j++) {
                    temp = Integer.toHexString(payload[j] & 0xFF);
                    if (temp.length() == 1) {
                        temp = "0" + temp;
                    }
                    result.append(temp);
                }
//                for (int i = 0; i < 16; i++) {
//                    char a = result.charAt(i);
//                    uid.append(a);
//                }
                uid = AppendUtil.append(result, 0, 16);
                uid = new StringBuilder(uid.toString().replaceAll(regex, "$1-"));
                uid = new StringBuilder(uid.substring(0, uid.length() - 1));
                return uid.toString().toUpperCase();
            } catch (IOException e) {
//                e.printStackTrace();
                return null;
            } finally {
                try {
                    nfcA.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                    //连接失败关闭时空指针
                    return uid.toString();
                }
            }
        } else {
            return null;
        }
    }
}
