package com.hachi.publishplugin.internet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.hachi.publishplugin.bean.LatLanAddress;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.utils.LogUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttp {
    private static final String TAG = "OkHttp";
    public final int POSTOK = 0x2017;
    public final int GETOK = 0x2020;
    public final int GETIMGOK = 0x2030;
    public final int WRANG = 0x22;
    public final int EXCEPTION = 0x30;
    private final int SUC = 0x40;
    private Handler handler;
    private static Context mContext;
    private static SharedPreferences sharedPreferences;

    public OkHttp(Context context, Handler handler) {
        this.mContext = context;
        this.handler = handler;
    }

    public OkHttp(Context context) {
        this.mContext = context;
    }

    /**
     * 通过标签id查询对应的记录
     *
     * @param path
     */
    public void getFromInternetById(final String path) {

        LogUtil.i(TAG, "reqUrl --> " + path);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = OkHttp.mContext.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE)
                            .url(path)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            LogUtil.i(TAG, "查询完成，开始写入缓存");
                            editor.putBoolean(Constant.FLAG, true);
                            editor.putString(Constant.RESPONSE_DATA, response.body().string());
                            editor.commit();
                            LogUtil.i(TAG, "查询完成，写入缓存完毕");
                        } else {
                            editor.putBoolean(Constant.FLAG, false);
                            editor.commit();
                            Log.i(TAG, "网络请求失败(" + response.code() + ")");
                        }
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解码
     *
     * @param path       路径
     * @param adminToken 管理员token
     */
    public void decodePad(final String path, String adminToken) {
        LogUtil.i(TAG, "reqUrl --> " + path);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = OkHttp.mContext.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE)
                            .addHeader("Cookie", "JSESSIONID=" + adminToken)
                            .url(path)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
//                        Log.i("Debug","方法执行");
                        if (response.isSuccessful()) {
                            LogUtil.i(TAG, "查询完成，开始写入缓存");
                            editor.putBoolean(Constant.FLAG, true);
                            editor.putString(Constant.RESPONSE_DATA, response.body().string());
                            editor.commit();
                            LogUtil.i(TAG, "查询完成，写入缓存完毕");
                        } else {
                            editor.putBoolean(Constant.FLAG, false);
                            editor.commit();
                            Log.i(TAG, "网络请求失败(" + response.code() + ")");
                        }
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getFromInternetById(final String path, final String token) {

        LogUtil.i(TAG, "reqUrl --> " + path);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = mContext.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    OkHttpClient client = new OkHttpClient();
                    String tokenKey;
                    if (path.contains("admin/ras/tst/")) {
                        tokenKey = "IDent-Admin-Token";
                    } else {
                        tokenKey = Constant.IDENT_TOKEN;
                    }
                    Request request = new Request.Builder()
                            .addHeader(tokenKey, token)
                            .addHeader(Constant.IDENT_TOKEN, token)
                            .addHeader(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE)
                            .url(path)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
//                        Log.i("Debug","方法执行");
                        if (response.isSuccessful()) {
                            LogUtil.i(TAG, "查询完成，开始写入缓存");
                            editor.putBoolean(Constant.FLAG, true);
                            editor.putString(Constant.RESPONSE_DATA, response.body().string());
                            editor.commit();
                            LogUtil.i(TAG, "查询完成，写入缓存完毕");
                        } else {
                            editor.putBoolean(Constant.FLAG, false);
                            editor.commit();
                            LogUtil.i(TAG, "网络请求失败(" + response.code() + ")");
                        }
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 向服务器添加标签数据
     *
     * @param path
     * @param requestBody
     */
    public void postTagFromInternet(final String path, final RequestBody requestBody) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(path)
                            .post(requestBody)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        LogUtil.i(TAG, "标签成功发送给服务器");
                    } else {
                        //如果发送失败该如何处理
                        LogUtil.i(TAG, "标签发送给服务器失败:" + response.body().string());
                    }
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void postFromInternet(final String path, final HashMap<Object, Object> params, CacheControl cacheControl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sharedPreferences = OkHttp.mContext.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String jsonData = gson.toJson(params);
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .addHeader(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE)
//                        .addHeader(Constant.IDENT_TOKEN, "0ujkxzw9gdh7sxbbfn8uvp5spk57c17i")
                        .addHeader("Cache-Control", "no-cache")
                        //.addHeader(CONTENT_TYPE, "application/json")
                        .url(path)
                        .post(requestBody)
                        .cacheControl(cacheControl)
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        //缓存
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constant.RESPONSE_DATA, response.body().string());
                        editor.commit();
                    } else {
                        LogUtil.i(TAG, "刷新-->查询数据失败!");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    /**
     * post方法发起网络请求
     *
     * @param path        请求url
     * @param requestBody body
     * @param header      请求头
     */
    public void postFromInternet(final String path, final RequestBody requestBody, Map<String, String> header) {
        LogUtil.i(TAG, "reqUrl --> " + path);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = OkHttp.mContext.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .headers(Headers.of(header))
                            .url(path)
                            .post(requestBody)
                            .build();
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        if (response.isSuccessful()) {
                            //缓存
                            LogUtil.i(TAG, "查询完成，开始写入缓存");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(Constant.FLAG, true);
                            editor.putString(Constant.RESPONSE_DATA, response.body().string());
//                        LogUtil.d(TAG,"res --> "+response.body().toString());
                            LogUtil.i(TAG, "查询完成，写入缓存完毕");
                            editor.commit();
                        } else {
                            LogUtil.i(TAG, "查询数据失败!");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                        }
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void getFromInternet(final String path,  Map<String, String> header) {

        LogUtil.i(TAG, "reqUrl --> " + path);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = mContext.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .headers(Headers.of(header))
                            .url(path)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
//                        Log.i("Debug","方法执行");
                        if (response.isSuccessful()) {
                            LogUtil.i(TAG, "查询完成，开始写入缓存");
                            editor.putBoolean(Constant.FLAG, true);
                            editor.putString(Constant.RESPONSE_DATA, response.body().string());
                            editor.commit();
                            LogUtil.i(TAG, "查询完成，写入缓存完毕");
                        } else {
                            editor.putBoolean(Constant.FLAG, false);
                            editor.commit();
                            LogUtil.i(TAG, "网络请求失败(" + response.code() + ")");
                        }
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void post(String path, RequestBody requestBody, Map<String, String> header, RequestCallBack requestCallBack) {
        LogUtil.i(TAG, "reqUrl --> " + path);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .headers(Headers.of(header))
                            .url(path)
                            .post(requestBody)
                            .build();
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        if (response.isSuccessful()) {
                            requestCallBack.success(response.body().string());
                        } else {
                            requestCallBack.error();
                        }
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface RequestCallBack {
        void error();

        void success(String response);
    }

    public void sendLog(final String path, final RequestBody requestBody, Map<String, String> header) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .headers(Headers.of(header))
                .url(path)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        //1.异步请求，通过接口回调告知用户 http 的异步执行结果
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i(TAG, "发送日志请求失败 --> " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    LogUtil.i(TAG, "发送日志请求成功！-->" + response.body().string());
                }
            }
        });
    }

    /**
     * 根据经纬度查询实际地址
     *
     * @param lng 经度
     * @param lan 纬度
     */
    public void queryAddress(final double lng, final double lan) {
        String path = Constant.QUERY_ADDRESS + "?output=json&location=" + lng + "," + lan + "&key=" + Constant.MAP_WEB_SERVER_KEY + "&radius=100&batch=true";
        LogUtil.i(TAG, "reqUrl --> " + path);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = mContext.getSharedPreferences(Constant.RESOURCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(path)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
//                        Log.i("Debug","方法执行");
                        if (response.isSuccessful()) {
                            LogUtil.i(TAG, "查询完成，开始写入缓存");
                            editor.putBoolean(Constant.FLAG, true);
                            LatLanAddress logisticsInfo = new ParseJson().Json2Bean(response.body().string(), LatLanAddress.class);
                            String formatted_address = logisticsInfo.getRegeocodes().get(0).getFormatted_address();
                            editor.putString(Constant.RESPONSE_DATA, formatted_address);
                            editor.commit();
                            LogUtil.i(TAG, "查询完成，写入缓存完毕 , res --> " + formatted_address);
                        } else {
                            editor.putBoolean(Constant.FLAG, false);
                            editor.commit();
                            LogUtil.i(TAG, "网络请求失败(" + response.code() + ") ");
                        }
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendLog(final String path, Map<String, String> header) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .headers(Headers.of(header))
                .url(path)
                .build();
        Call call = okHttpClient.newCall(request);
        //1.异步请求，通过接口回调告知用户 http 的异步执行结果
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i(TAG, "发送日志请求失败 --> " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    LogUtil.i(TAG, "发送日志请求成功！-->" + response.body().string());
                }
            }
        });
    }

    /**
     * 向服务器修改数据
     *
     * @param path
     * @param requestBody
     */
    public void updateTagFromInternet(final String path, final RequestBody requestBody) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(path)
                            .patch(requestBody)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
//                        Message message=new Message();
//                        message.what=POSTOK;
//                        message.obj=response.body().string();
//                        Log.i("Debug","Message:"+message);
//                        handler.sendMessage(message);
                        Log.i("Debug", "标签数据修改成功");
                    } else {
//                        Message message=new Message();
//                        message.what=WRANG;
//                        handler.sendMessage(message);
                        Log.i("Debug", "标签修改失败:" + response.body().string());
                    }
                    response.close();
                } catch (IOException e) {
//                    Message message=new Message();
//                    message.what=EXCEPTION;
//                    handler.sendMessage(message);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
