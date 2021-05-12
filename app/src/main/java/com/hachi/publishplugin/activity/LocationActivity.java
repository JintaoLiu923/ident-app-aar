package com.hachi.publishplugin.activity;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

import java.math.BigDecimal;

public class LocationActivity {
    //声明AMapLocationClientOption对象
    public static AMapLocationClientOption mLocationOption = null;

    //声明AMapLocationClient类对象
    public static AMapLocationClient mLocationClient = null;
    //声明定位回调监听器

    public static AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    double locationType = amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    GlobelRasFunc.latitude = new BigDecimal(amapLocation.getLatitude()).setScale(6, BigDecimal.ROUND_HALF_UP);
                    GlobelRasFunc.longitude = new BigDecimal(amapLocation.getLongitude()).setScale(6, BigDecimal.ROUND_HALF_UP);
//                    Log.e("Amap==经度：纬度", "locationType:"+locationType+",latitude:"+GlobelRasFunc.latitude+",longitude:"+GlobelRasFunc.longitude);
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
//                    Log.e("AmapError", "location Error, ErrCode:"
//                            + amapLocation.getErrorCode() + ", errInfo:"
//                            + amapLocation.getErrorInfo());
                }
            }
        }
    };

    public static boolean init(Context context, String key) {
        AMapLocationClient.setApiKey(key);
        //初始化定位
        mLocationClient = new AMapLocationClient(context);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        mLocationOption.setLocationMode(AMapLocationMode.Battery_Saving);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        //TODO 开启定位后一直打印:D/TelephonyManager: getAllCellInfo calling app is ,D/TelephonyManager: getCellLocation calling app is ??????
        mLocationClient.startLocation();
//        /**
//         * 获取一次定位
//         */

//        //该方法默认为false，true表示只定位一次
        mLocationOption.setOnceLocation(true);
        return true;
    }

    protected void onDestroy() {
        mLocationClient.stopLocation();
    }
}