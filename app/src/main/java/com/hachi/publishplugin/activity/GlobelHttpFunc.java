package com.hachi.publishplugin.activity;

import android.content.Context;
import android.content.SharedPreferences;

import com.allatori.annotations.DoNotRename;
import com.allatori.annotations.StringEncryption;
import com.allatori.annotations.StringEncryptionType;
import com.hachi.publishplugin.bean.ResultBean;
import com.hachi.publishplugin.bean.TagBean;
import com.hachi.publishplugin.constant.Constant;
import com.hachi.publishplugin.enums.TagErrorEnum;
import com.hachi.publishplugin.gson.ParseJson;
import com.hachi.publishplugin.internet.OkHttp;
import com.hachi.publishplugin.utils.LogUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 * 日志组件
 */
@DoNotRename
@StringEncryption(StringEncryption.MAXIMUM)
@StringEncryptionType(StringEncryptionType.STRONG)
public class GlobelHttpFunc {
    private static final String TAG = "GlobelHttpFunc";
    private static boolean ifMusic = true;
    private static int err_num;

    /**
     * confirm有状态位的标签信息
     */
    public static ResultBean sendConfirm(Context context, String uid, String pad, String newCert, String EToken) {
        OkHttp okHttp = new OkHttp(context);
        okHttp.getFromInternetById(Constant.CONFIRM_URL + "?uid=" + uid + "&pad=" + pad + "&cert3=" + newCert, EToken);
//        okHttp.getFromInternetById("http://192.168.3.20:8189/app/ras/confirm/v2?uid=" + uid + "&pad=" + pad + "&cert3=" + newCert, EToken);

        SharedPreferences sharedPreferences1 = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);
        ResultBean resultBean = new ResultBean();
        resultBean.setUid(uid);
        if (!flag1) {
            resultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return resultBean;
        }

        String detailData = sharedPreferences1.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "res data --> " + detailData);

        //TagBean tagBean = new ParseJson().Json2TagBean(detailData);
        TagBean tagBean = new ParseJson().Json2Bean(detailData, TagBean.class);
        if (tagBean.getErrmsg().equals("no db")) {
            resultBean.setErrno(TagErrorEnum.IVALID_TAG.getCode());
            return resultBean;
        } else if (tagBean.getErrno() == 501) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.DATA, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            resultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return resultBean;
        } else if (tagBean.getErrno() != 0) {
            resultBean.setErrno(tagBean.getErrno());
            resultBean.setErrmsg(tagBean.getErrmsg());
            return resultBean;
        }
        resultBean.setErrno(TagErrorEnum.SUCCESS.getCode());
        return resultBean;
    }

    /**
     * confirm有状态位的标签信息
     */
    public static ResultBean sendConfirm2(Context context, String rasId, String newCert, String EToken) {
        OkHttp okHttp = new OkHttp(context);
        okHttp.getFromInternetById(Constant.CONFIRM_URL + "?rasId=" + rasId + "&cert3=" + newCert, EToken);
//        okHttp.getFromInternetById("http://192.168.3.20:8189/app/ras/confirm/v2?uid=" + uid + "&pad=" + pad + "&cert3=" + newCert, EToken);

        SharedPreferences sharedPreferences1 = context.getSharedPreferences(Constant.RESOURCES, MODE_PRIVATE);
        boolean flag1 = sharedPreferences1.getBoolean(Constant.FLAG, false);
        ResultBean resultBean = new ResultBean();
        resultBean.setTime(rasId);
        if (!flag1) {
            resultBean.setErrno(TagErrorEnum.REQ_FAILED.getCode());
            return resultBean;
        }

        String detailData = sharedPreferences1.getString(Constant.RESPONSE_DATA, "");
        LogUtil.i(TAG, "res data --> " + detailData);

        //TagBean tagBean = new ParseJson().Json2TagBean(detailData);
        TagBean tagBean = new ParseJson().Json2Bean(detailData, TagBean.class);
        if (tagBean.getErrmsg().equals("no db")) {
            resultBean.setErrno(TagErrorEnum.IVALID_TAG.getCode());
            return resultBean;
        } else if (tagBean.getErrno() == 501) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.DATA, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            resultBean.setErrno(TagErrorEnum.UN_LOGIN.getCode());
            return resultBean;
        } else if (tagBean.getErrno() != 0) {
            resultBean.setErrno(tagBean.getErrno());
            resultBean.setErrmsg(tagBean.getErrmsg());
            return resultBean;
        }
        resultBean.setErrno(TagErrorEnum.SUCCESS.getCode());
        return resultBean;
    }
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Request original = chain.request();
//                        Request request = original.newBuilder()
//                                .header(Constant.IDENT_TOKEN, token)
//                                .method(original.method(), original.body())
//                                .build();
//                        return chain.proceed(request);
//                    }
//                }).build();
//
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(Constant.SERVER_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .client(okHttpClient)
//                .build();
//
//        retrofit.create(Api.class)
//                .confirm(uid, newCert, pad)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<TagBean>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(TagBean tagBean) {
//                        LogUtil.i(TAG, "结束:" + tagBean.getErrmsg());
//                        if (tagBean.getErrno() == 0) {
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        LogUtil.i(TAG, "更新失败:" + e.toString());
//                        err_num++;
//                        if (err_num < 3) {
//                            GlobelHttpFunc.sendData(uid, pad, newCert, token);
//                        }
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }
}
