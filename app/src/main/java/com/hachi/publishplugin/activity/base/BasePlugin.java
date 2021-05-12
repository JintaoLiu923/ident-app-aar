package com.hachi.publishplugin.activity.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.hachi.publishplugin.activity.GlobelRasFunc;
import com.hachi.publishplugin.activity.InitPlugin;
import com.hachi.publishplugin.activity.LocationActivity;
import com.hachi.publishplugin.bean.IdentLogReqBean;
import com.hachi.publishplugin.bean.IdentRasLogBean;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.ActTypeEnum;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;
import com.hachi.publishplugin.utils.PermissionUtils;
import com.hachi.publishplugin.utils.PutLogUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static android.content.Context.MODE_PRIVATE;
import static com.hachi.publishplugin.utils.OperationUtil.binaryToByte;
import static com.hachi.publishplugin.utils.OperationUtil.byteToBinary;
import static com.hachi.publishplugin.utils.Xor.reverseArray;

public class BasePlugin {
    private static final String TAG = "BasePlugin";

    //上下文环境
    protected static Context mContext;
    protected static Map<String, Object> map = new HashMap<>();
    public static ResultBean sResultBean = new ResultBean();
    //请求头
    protected static Map<String, String> mHeader = new HashMap<>();
    //经纬度
    public static BigDecimal latitude;
    public static BigDecimal longitude;
    //操作类型
    protected static ActTypeEnum sActTypeEnum;
    //标签uid
    protected static String uid = "";
    //时间戳
    protected static String mRasId = "";
    //标签类型 0:未知 1:15693标签 2:15693温控标签 3:F8023标签 10:F8213标签 20:14443标签
    public static int tagType;
    //管理员token
    protected static String token;
    //identToken
    protected static String mRasToken;
    //声明mlocationClient对象
    protected static AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    protected static AMapLocationClientOption mLocationOption = null;
    //用户名
    protected static String mUserName;
    //用户密码
    protected static String mPassword;
    //电量
    protected static int mPower;
    //日志Bean
    protected static IdentLogReqBean mIdentLogReqBean = new IdentLogReqBean();
    //溯源日志Bean
    protected static IdentRasLogBean mLog = new IdentRasLogBean();
    //Post放body参数
    protected static HashMap<Object, Object> mParams;

    public static TagBean mTagBean;

    /**
     * 初始化定位
     *
     * @param context 上下文环境
     */
    public static void initLocation(Context context, String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        LocationActivity.init(context, key);
        AMapLocationClient.setApiKey(key);
        mlocationClient = new AMapLocationClient(context);

        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();

        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //单次定位
        mLocationOption.setOnceLocation(true);
        //设置定位间隔,单位毫秒,默认为2000ms
        //mLocationOption.setInterval(2000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        //mlocationClient.stopLocation();
    }

    /**
     * 定义定位监听器
     */
    protected static AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(aMapLocation.getTime());
                    df.format(date);//定位时间
                    LogUtil.i(TAG, "Uid --> " + uid);
                    LogUtil.i(TAG, "经度:" + aMapLocation.getLongitude()
                            + ", 纬度:" + aMapLocation.getLatitude()
                            + ", 城市:" + aMapLocation.getCity()
                            + ", 地区:" + aMapLocation.getDistrict());
                    mlocationClient.stopLocation();
                    mlocationClient.onDestroy();
                    latitude = BigDecimal.valueOf(aMapLocation.getLatitude());
                    longitude = BigDecimal.valueOf(aMapLocation.getLongitude());
                    PutLogUtil.putLocationLog(mContext, uid, mRasToken, sActTypeEnum
                            , aMapLocation, sResultBean.getErrno() + "");
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    LogUtil.logLocationError(TAG, aMapLocation);
                }
            }
        }
    };


    protected static AMapLocationListener mLockListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    LogUtil.i(TAG, "经度:" + aMapLocation.getLongitude()
                            + ", 纬度:" + aMapLocation.getLatitude()
                            + ", 城市:" + aMapLocation.getCity()
                            + ", 地区:" + aMapLocation.getDistrict());
                    mIdentLogReqBean = new IdentLogReqBean();
                    mLog = new IdentRasLogBean();
                    mLog.setCreateBy(1);
                    mLog.setCreateByName(mUserName);
                    mLog.setRasId(mRasId);
                    mLog.setAction(sActTypeEnum.getName());
                    mLog.setResult(sResultBean.getErrno() + "");
                    mlocationClient.startLocation();
                    mLog.setLat(BigDecimal.valueOf(aMapLocation.getLatitude()));
                    mLog.setLng(BigDecimal.valueOf(aMapLocation.getLongitude()));
                    mLog.setComment("{\"content\":null,\"master\":\"" + mUserName + "\",\"name\":\"\",\"sn\":null,\"type\":3,\"power\":" + mPower + "}");
                    mIdentLogReqBean.setLog(mLog);

                    mHeader.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
                    Gson gson = new Gson();
                    String jsonLog = gson.toJson(mIdentLogReqBean);
                    LogUtil.i(TAG, "json数据 --> " + jsonLog);
                    RequestBody requestBody1 = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonLog);
//                    LogUtil.i(TAG, "requestBody --> " + requestBody1.toString());
                    OkHttp okHttp = new OkHttp(mContext);
                    okHttp.sendLog(Constant.LOG_ADD_URL, requestBody1, mHeader);
                    mlocationClient.stopLocation();
                    mlocationClient.onDestroy();
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    LogUtil.logLocationError(TAG, aMapLocation);
                }
            }
        }
    };

    /**
     * 获取标签详情
     *
     * @param uid            标签uid
     * @param random         随机数
     * @param newPassword    标签新密码
     * @param EToken
     * @param detailListener
     */
    protected static void getDetailRequest(String uid, String random, String newPassword, String EToken, DetailListener detailListener) {
        SharedPreferences sharedPreferences = initResourceSP(mContext);

        OkHttp okHttp = new OkHttp(mContext);

        String url = Constant.DETAIL_URL + "?uid=" + uid;
        url += TextUtils.isEmpty(random) ? "" : "&random=" + random;
        url += TextUtils.isEmpty(newPassword) ? "" : "&password=" + newPassword;

        okHttp.getFromInternetById(url, EToken);

        boolean flag = sharedPreferences.getBoolean(Constant.FLAG, false);

        //若flag为false，表示未获取到数据，直接返回错误
        if (!flag) {
            TagBean tagBean = new TagBean();
            tagBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            tagBean.setErrmsg(TagErrorEnum.REQ_FAILED.getDescription());
            detailListener.error(tagBean);
            return;
        }

        String responseData = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "查询数据 --> " + responseData);
        TagBean tagBean = new ParseJson().Json2Bean(responseData, TagBean.class);
        if (tagBean.getErrmsg().equals("no db")) {
            tagBean.setErrmsg(TagErrorEnum.IVALID_TAG.getDescription());
            detailListener.error(tagBean);
        } else if (tagBean.getErrno() != 0) {
            detailListener.error(tagBean);
            if (tagBean.getErrno() == 501) {
                SharedPreferences sharedPreferences1 = mContext.getSharedPreferences("token", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.clear();
                editor.apply();
                sResultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            }
        } else {
            detailListener.success(tagBean);
        }
    }


    /**
     * 请求网络数据
     *
     * @param url             请求链接
     * @param requestBody     post方法body
     * @param header          请求头
     * @param requestListener
     */
    public static void postRequest(String url, RequestBody requestBody, Map<String, String> header, RequestListener requestListener) {
        SharedPreferences sharedPreferences = initResourceSP(mContext);

        OkHttp okHttp = new OkHttp(mContext);
        okHttp.postFromInternet(url, requestBody, header);

        boolean flag1 = sharedPreferences.getBoolean(Constant.FLAG, false);
        if (!flag1) {
            requestListener.requestFail();
            return;
        }

        String responseData = sharedPreferences.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "查询数据 --> " + responseData);
        ResultBean resultBean = new ParseJson().Json2Bean(responseData, ResultBean.class);

        if (resultBean.getErrno() != 0) {
            requestListener.error(resultBean);
        } else {
            requestListener.success(resultBean);
        }
    }

    /**
     * 获取详情回调接口
     */
    public interface DetailListener {
        //请求错误
        void error(TagBean tagBean);

        //请求成功
        void success(TagBean tagBean);
    }

    /**
     * 请求网络回调接口
     */
    public interface RequestListener {
        //网络请求失败
        void requestFail();

        //请求错误
        void error(ResultBean tagBean);

        //请求成功
        void success(ResultBean tagBean);
    }

    /**
     * 鉴权
     *
     * @param EToken
     */
    public static boolean checkLogin(String EToken, String mobile, String password) {
        if (TextUtils.isEmpty(EToken)) {
            mRasToken = InitPlugin.checkLogin(mContext, mobile, password);
            EToken = mRasToken;
            if (mRasToken == null) {
                sResultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
                return false;
            }
            EToken = mRasToken;
        } else {
            mRasToken = EToken;
        }
        return true;
    }

    /**
     * 检查定位权限
     *
     * @return
     */
    public static boolean checkLocation() {
        if (!PermissionUtils.checkLocationPermission(mContext)) {
            sResultBean.setErrno(TagErrorEnum.PERMISSION_LOCATION_DENY.getCode());
            return false;
        }
        return true;
    }

    /**
     * NfcA标签获取uid
     *
     * @param tag
     * @return
     */
    protected static boolean checkNfcAUid(Tag tag) {
        uid = GlobelRasFunc.readUid(tag);
        if (TextUtils.isEmpty(uid)) {
            sResultBean.setErrno(TagErrorEnum.UID_GET_FAILED.getCode());
            sResultBean.setErrmsg(TagErrorEnum.UID_GET_FAILED.getDescription());
            return false;
        }
        return true;
    }

    /**
     * init请求头Header
     *
     * @param EToken IdentToken
     */
    protected static Map<String, String> initHeader(String EToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
        header.put(Constant.IDENT_TOKEN, EToken);
        header.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
        return header;
    }

    protected static Map<String, String> initAdminHeader(String EToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE);
        header.put(Constant.IDENT_ADMIN_TOKEN, EToken);
        header.put(Constant.COOKIE, "JSESSIONID =" + EToken);
        header.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
        return header;
    }

    protected static SharedPreferences initResourceSP(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();
        return sharedPreferences;
    }

    public static byte[] getOFLAGS(byte[] pad,byte readKey, byte customItsp) {
        if (pad != null) {
            byte[] ipDecodedOFLAGS = ipDecode(pad, readKey, customItsp);         //提取焊盘状态
            return ipDecodedOFLAGS;
        }
        return null;
    }

    public static byte[] ipDecode(byte[] Data, byte readKey, byte customItsp)
    {
        byte[] deIpData = DeInterpolation(customItsp, Data);
        byte[] decodedData = readDecode(deIpData, readKey);
        return decodedData;
    }

    public static byte[] DeInterpolation(byte ITSP, byte[] Data)
    {
        //将itsp转化为int类型的偏移量
        int[] itsp = new int[4];
        for (int j = 3; j > -1; j--)  //ITSP[]=[0,1,2,3], itsp[]=[3,2,1,0]
        {
            itsp[j] = (int)((ITSP & (0x03 << (j * 2))) >> (j * 2));
            if ((itsp[j] == 0) && (j == 3))
            {
                //itsp[3] = 1;
//                //RASLog.record(BasicUsageTool.getTime() +" "+ " ITSP数据不合规，首位为0。已置为1");
//                RASLog.record(BasicUsageTool.getTime() +" "+ " ITSP数据不合规，首位为0。");
            }
            else if ((itsp[j] == 0) && (j != 3))
                itsp[j] = itsp[j + 1];
        }
        byte[] reversedData = reverseArray(Data);
        int[] newData = byteToBinary(reversedData);
        int[] reverseData = reverseArray(newData);
        int flag = itsp[3];
        //去插值
        int i = 0;
        int count = 0;  //原文计数，应有16位
        int[] dataAIp = new int[16];
        while (count < 16)
        {
            if (i != flag)
            {
                dataAIp[count] = reverseData[i];
                //RASLog.record(BasicUsageTool.getTime() +" "+ String.Join("", dataAIp.ToArray()));
                count++;
            }
            else
                flag += itsp[3 - ((i + 1 - count) % 4)] + 1;
            i++;
        }
        //将去插值的int数组转化成byte数组
        int[] reversedDataAIp = reverseArray(dataAIp);
        byte[] byteAfterIp = binaryToByte(reversedDataAIp);
        return byteAfterIp;
    }

    public static byte[] readDecode(byte[] singlePageData, byte readKey)
    {
        if ((singlePageData.length != 4) && (singlePageData.length != 2))  //判断页数据是否有效
        {
            return null;
        }
        byte[] dataDecoded = new byte[singlePageData.length];
        for (int i = 0; i < singlePageData.length; i++)
        {
            dataDecoded[i] = (byte)(singlePageData[i] ^ readKey);
        }
        return dataDecoded;
    }
}
