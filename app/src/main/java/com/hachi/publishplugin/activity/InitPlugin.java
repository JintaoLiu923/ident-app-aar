package com.hachi.publishplugin.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.allatori.annotations.DoNotRename;
import com.google.gson.Gson;
import com.hachi.publishplugin.R;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.bean.UserBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.utils.LogUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 初始化
 */
@DoNotRename
public class InitPlugin {
    public static final String LOGIN_INFO_PREF = "token";
    private static final String TAG = "InitPlugin";
    private static Context mContext;
    private static ResultBean sMResultBean;

    /**
     * 检查登录，保存Token
     *
     * @param context
     * @param mobile
     * @param password
     * @return
     */
    @DoNotRename
    public static String checkLogin(Context context, String mobile, String password) {

        //检查登录
//        LogUtil.i(TAG, "账号:" + mobile + " 密码:" + password);
        mContext = context;
        //检查本地缓存
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOGIN_INFO_PREF, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(Constant.IDENT_TOKEN, "");
        if (TextUtils.isEmpty(token)) {
            //登录
            UserBean userBean = new UserBean();
            userBean.setUsername(mobile);
            userBean.setPassword(password);
            userBean.setPlatformType(1);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new Gson();
                    String jsonData = gson.toJson(userBean);
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE)
                            .url(Constant.USER_LOGIN_URL)
                            .post(requestBody)
                            .build();
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        if (response.isSuccessful()) {
                            //解析响应数据
                            TagBean tagBean = new ParseJson().Json2Bean(response.body().string(), TagBean.class);
                            if (tagBean.getErrno() == 0) {
                                LogUtil.i(TAG, "登录-->errno:" + tagBean.getErrno()
                                        + " data-Token:" + tagBean.getData().getToken()
                                        + "data-tokenExpire:" + tagBean.getData().getTokenExpire()
                                        + "errmsg:" + tagBean.getErrmsg());
                                //缓存
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(Constant.IDENT_TOKEN, tagBean.getData().getToken());
                                editor.putString("tokenExpire", tagBean.getData().getTokenExpire());
                                editor.putString("mobile", mobile);
                                editor.putString("password", password);
                                editor.putInt("platformType", 1);
                                editor.apply();
                            } else {
                                LogUtil.i(TAG, "登录-->errno:" + tagBean.getErrno()
                                        + "errmsg:" + tagBean.getErrmsg());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String str = sharedPreferences.getString(Constant.IDENT_TOKEN, "");
            if (TextUtils.isEmpty(str)) {
                return null;
            } else {
                return str;
            }
        } else {
            //检查Token有效期
            String tokenExpire = sharedPreferences.getString("tokenExpire", "");
            //获取当前日期
            Date time = new Date();
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
            String hehe = dateFormat.format(now);
            try {
                now = dateFormat.parse(hehe);
                time = dateFormat.parse(tokenExpire);
                LogUtil.i(TAG, context.getString(R.string.text_NowTime) + now.getTime());
                LogUtil.i(TAG, context.getString(R.string.text_effectiveTime) + time.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (now.getTime() < time.getTime()) {
                //没过期就直接返回
                LogUtil.i(TAG, context.getString(R.string.text_tokenNotBeOverdue));
                return token;
            } else {
                //刷新Token
                UserBean userBean = new UserBean();
                userBean.setUsername(mobile);
                userBean.setPassword(password);
                userBean.setPlatformType(1);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        String jsonData = gson.toJson(userBean);
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder()
                                .addHeader(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE)
                                .addHeader(Constant.IDENT_TOKEN, token)
                                .url(Constant.REFRESH_TOKEN_URL)
                                .post(requestBody)
                                .build();
                        try {
                            Response response = okHttpClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                TagBean tagBean = new ParseJson().Json2Bean(response.body().string(), TagBean.class);
                                if (tagBean.getErrno() == 0) {
                                    LogUtil.i(TAG, "刷新-->errno:" + tagBean.getErrno()
                                            + " data-Token:" + tagBean.getData().getToken()
                                            + "data-tokenExpire:" + tagBean.getData().getTokenExpire()
                                            + "errmsg:" + tagBean.getErrmsg());
                                    //缓存
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(Constant.IDENT_TOKEN, tagBean.getData().getToken());
                                    editor.putString("tokenExpire", tagBean.getData().getTokenExpire());
                                    editor.putString("mobile", mobile);
                                    editor.putString("password", password);
                                    editor.putInt("platformType", 1);
                                    editor.apply();
                                } else {
                                    LogUtil.i(TAG, "刷新-->errno:" + tagBean.getErrno()
                                            + "errmsg:" + tagBean.getErrmsg());
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("error", tagBean.getErrno());
                                    editor.putString("errmsg", tagBean.getErrmsg());
                                    editor.clear();
                                    editor.apply();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String str = sharedPreferences.getString(Constant.IDENT_TOKEN, "");
                if (TextUtils.isEmpty(str) || str.equals(token)) {
                    return null;
                } else {
                    return str;
                }
            }
        }
    }
//    @DoNotRename
//    public static String checkLogin(Context context, String mobile, String password) {
//        //初始化位置信息
//        initPos(context);
//        //检查登录
//        LogUtil.i(TAG, "账号:" + mobile + " 密码:" + password);
//        mContext = context;
//        //检查本地缓存
//        SharedPreferences sharedPreferences = context.getSharedPreferences(LOGIN_INFO_PREF, Context.MODE_PRIVATE);
//        String token = sharedPreferences.getString(Constant.IDENT_TOKEN, "");
//        if (TextUtils.isEmpty(token)) {
//            //登录
//            UserBean userBean = new UserBean();
//            userBean.setUsername(mobile);
//            userBean.setPassword(password);
//            userBean.setPlatformType(1);
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Gson gson = new Gson();
//                    String jsonData = gson.toJson(userBean);
//                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
//                    OkHttpClient okHttpClient = new OkHttpClient();
//                    Request request = new Request.Builder()
//                            .addHeader(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE)
//                            .url(Constant.USER_LOGIN_URL)
//                            .post(requestBody)
//                            .build();
//                    try {
//                        Response response = okHttpClient.newCall(request).execute();
//                        if (response.isSuccessful()) {
//                            //解析响应数据
//                            TagBean tagBean = new ParseJson().Json2Bean(response.body().string(), TagBean.class);
//                            if (tagBean.getErrno() == 0) {
//                                LogUtil.i(TAG, "登录-->errno:" + tagBean.getErrno()
//                                        + " data-Token:" + tagBean.getData().getToken()
//                                        + "data-tokenExpire:" + tagBean.getData().getTokenExpire()
//                                        + "errmsg:" + tagBean.getErrmsg());
//                                //缓存
//                                SharedPreferences.Editor editor = sharedPreferences.edit();
//                                editor.putString(Constant.IDENT_TOKEN, tagBean.getData().getToken());
//                                editor.putString("tokenExpire", tagBean.getData().getTokenExpire());
//                                editor.putString("mobile", mobile);
//                                editor.putString("password", password);
//                                editor.putInt("platformType", 1);
//                                editor.apply();
//                            } else {
//                                LogUtil.i(TAG, "登录-->errno:" + tagBean.getErrno()
//                                        + "errmsg:" + tagBean.getErrmsg());
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            thread.start();
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            String str = sharedPreferences.getString(Constant.IDENT_TOKEN, "");
//            if (TextUtils.isEmpty(str)) {
//                return null;
//            } else {
//                return str;
//            }
//        } else {
//            //检查Token有效期
//            String tokenExpire = sharedPreferences.getString("tokenExpire", "");
//            //获取当前日期
//            Date time = new Date();
//            Date now = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
//            String hehe = dateFormat.format(now);
//            try {
//                now = dateFormat.parse(hehe);
//                time = dateFormat.parse(tokenExpire);
//                LogUtil.i(TAG, context.getString(R.string.text_NowTime) + now.getTime());
//                LogUtil.i(TAG, context.getString(R.string.text_effectiveTime) + time.getTime());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            if (now.getTime() < time.getTime()) {
//                //没过期就直接返回
//                LogUtil.i(TAG, context.getString(R.string.text_tokenNotBeOverdue));
//                return token;
//            } else {
//                //刷新Token
//                UserBean userBean = new UserBean();
//                userBean.setUsername(mobile);
//                userBean.setPassword(password);
//                userBean.setPlatformType(1);
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Gson gson = new Gson();
//                        String jsonData = gson.toJson(userBean);
//                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
//                        OkHttpClient okHttpClient = new OkHttpClient();
//                        Request request = new Request.Builder()
//                                .addHeader(Constant.PLATFORM_ID, Constant.PLATFORM_ID_VALUE)
//                                .addHeader(Constant.IDENT_TOKEN, token)
//                                .url(Constant.SERVER_URL + "app4biz/auth/refreshToken")
//                                .post(requestBody)
//                                .build();
//                        try {
//                            Response response = okHttpClient.newCall(request).execute();
//                            if (response.isSuccessful()) {
//                                TagBean tagBean = new ParseJson().Json2Bean(response.body().string(), TagBean.class);
//                                if (tagBean.getErrno() == 0) {
//                                    LogUtil.i(TAG, "刷新-->errno:" + tagBean.getErrno()
//                                            + " data-Token:" + tagBean.getData().getToken()
//                                            + "data-tokenExpire:" + tagBean.getData().getTokenExpire()
//                                            + "errmsg:" + tagBean.getErrmsg());
//                                    //缓存
//                                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                                    editor.putString(Constant.IDENT_TOKEN, tagBean.getData().getToken());
//                                    editor.putString("tokenExpire", tagBean.getData().getTokenExpire());
//                                    editor.putString("mobile", mobile);
//                                    editor.putString("password", password);
//                                    editor.putInt("platformType", 1);
//                                    editor.apply();
//                                } else {
//                                    LogUtil.i(TAG, "刷新-->errno:" + tagBean.getErrno()
//                                            + "errmsg:" + tagBean.getErrmsg());
//                                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                                    editor.putInt("error", tagBean.getErrno());
//                                    editor.putString("errmsg", tagBean.getErrmsg());
//                                    editor.clear();
//                                    editor.apply();
//                                }
//                            }
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                thread.start();
//                try {
//                    thread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                String str = sharedPreferences.getString(Constant.IDENT_TOKEN, "");
//                if (TextUtils.isEmpty(str) || str.equals(token)) {
//                    return null;
//                } else {
//                    return str;
//                }
//            }
//        }
//    }

    protected static boolean initPos(Context context, String key) {
        return LocationActivity.init(context, key);
    }
}
