package com.hachi.publishplugin.constant;

/**
 * Created by pengllrn on 2019/1/4.
 * 系统常量池
 */

public class Constant {
    public static final String DATA = "data";
    public static final String RESPONSE_DATA = "responseData";
    public static final String RESOURCES = "Resources";
    public static final String PLATFORM_ID = "platformId";
    public static final String PLATFORM_ID_VALUE = "401";
    public static final String NFCA = "NfcA";
    public static final String NFCV = "NfcV";
    public static final String START_CERT = "";
    public static final String FAIL = "fail";
    public static final String FLAG = "flag";
    public static final String IDENT_TOKEN = "IDent-Token";
    public static final String IDENT_ADMIN_TOKEN = "IDent-Admin-Token";
    public static final String TOKEN = "Token";
    public static final String YISHOP_TOKEN = "YiShop-Token";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String COOKIE = "Cookie";
    public static final String APPLICATION_JSON = "application/json";
    public static final String MAPKEY = "1459a8e9c7adb479204fb99d6292844f";
    public static final String RAS_ID = "rasId";
    public static final String SUB_RAS_ID = "subRasId";
    public static final String PAD = "pad";
    public static final String SUB_PAD = "subPad";
    public static final String POWER = "power";
    public static final String STATUS = "status";
    //用户名
    public static final String username = "admin123";
    //密码
    public static final String password = "admin123";
    public static final String USER_ID = "userId";
    public static final String PASSWORD = "password";
    public static String MAP_WEB_SERVER_KEY = "6a3ce091a8d459bc62caa4aaaaccaa10";

    /**
     * 爱扽验证URL
     */

    public static final String SERVER_URL = "https://service1.ident.cn/apis/";
    //v2.0 ident服务器
    public static final String SERVER_URL_V2 = "https://service1.ident.cn/apis2/";
    //    public static final String SERVER_URL_V2 = "https://srv1.identw.com/apis2/";
    //应用层服务器
    public static final String SERVER_URL_NEWBIE = "https://newbie3d.com/apis/";

    //路由
    private static final String ADMIN_URL = SERVER_URL_V2 + "ident.admin/admin/";
    private static final String BIZ_URL = SERVER_URL_V2 + "ident.app4biz/app4biz/";
    private static final String LCK_URL = SERVER_URL_V2 + "ident.app4lck/app4lck/";
    private static final String LOG_URL = SERVER_URL_V2 + "ident.app4log/app4log/";
    private static final String QR_URL = SERVER_URL_V2 + "ident.qr/qr/";
//    private static final String LOG_URL = "https://newbie3d.com/apis/app/";

    private static final String APP_URL = SERVER_URL_V2 + "ident.app/app/";

    //    public static final String SERVER_URL = "http://192.168.3.140:8181/";
    public static final String SERVER_URL_1 = "https://newbie3d.com/apis/app/ras";

    private static final String APP_RAS_URL_V2 = "app/ras/";

    private static final String BIZ_LOG_URL = "ras/biz/log/";
    private static final String BIZ_LOCK_URL = "ras/biz/lock/";
    private static final String LOCK_URL = "app4biz/ras/biz/lock/";

    //用于查询最新版本
    public static final String QUERY_VERSION_URL = SERVER_URL_NEWBIE + "app/comm/query/app/version";

    //根据坐标查询具体地址
    public static final String QUERY_ADDRESS = "https://restapi.amap.com/v3/geocode/regeo";

    //标签入库
    public static final String CFG_SAVE_URL = APP_URL + "ras/cfg/save";
    public static final String ADMIN_CFG_SAVE_URL = ADMIN_URL + "ras/cfg/save";
    public static final String ADMIN_CFG_DETAIL = ADMIN_URL + "ras/cfg/detail";

    //修改标签密码
    public static final String CFG_UPDATE_PASSWORD = APP_URL + "ras/cfg/update/password";

    /************************ 标签验证 ************************/
    public static final String CONFIRM_URL = APP_URL + "ras/confirm/v3";
    //    public static final String CONFIRM_URL = "http://192.168.3.140:8181/app/ras/confirm/v3";
    //获取标签详情接口
    public static final String DETAIL_URL = APP_URL + "ras/detail/v3";
//            public static final String DETAIL_URL = "http://192.168.3.140:8189/app/ras/detail/v3";
    public static final String VERIFY_URL = APP_URL + "ras/verify/v3";
//    public static final String VERIFY_URL = "http://192.168.3.140:8181/app/ras/verify/v3";

    /************************ 电子锁 ************************/
    //绑定电子锁上盖下盖
    public static final String LOCK_BIND_URL = LCK_URL + BIZ_LOCK_URL + "bind";
    //检测电子锁上盖下盖匹配
    public static final String LOCK_CHECK_URL = LCK_URL + BIZ_LOCK_URL + "check";
    //获取电子锁上盖下盖匹配详情
    public static final String LOCK_DETAIL_URL = LCK_URL + BIZ_LOCK_URL + "detail";
    //更新电子锁上盖下盖
    public static final String LOCK_UPDATE_URL = LCK_URL + BIZ_LOCK_URL + "update";
    //增加电子锁状态
    public static final String PADS_SAVE_URL = LCK_URL + BIZ_LOCK_URL + "pads/save";
    //状态位详情
    public static final String PADS_DETAIL_URL = LCK_URL + BIZ_LOCK_URL + "pads/detail";
    //状态位解码
    //    public static final String DECODE_URL = "https://service1.ident.cn/apis2/ident.admin/admin/ras/decode";
    public static final String DECODE_URL = "https://service1.ident.cn/apis/app/ras/decode";

    /************************ 二维码 ************************/
    //绑定标签与二维码
    public static final String BIND_QR_URL = "https://newbie3d.com/apis/app/ras/biz/bind/qr";
    //芯码合一制作
    public static final String CREATE_QR_URL = BIZ_URL + "ras/biz/create/qr";

    public static final String CREATE_QRID_URL = QR_URL + "create/qrid";
    //https://service1.ident.cn/apis2/ident.qr/qr/create/qrid

    /************************ 日志 ************************/
    //用户添加温度日志
    public static final String LOG_ADD_URL = LOG_URL + BIZ_LOG_URL + "add/v2";
    //添加应用层日志
    public static final String LOG_ADD_URL_NEWBIE = "http://newbie3d.com/apis/app/ras/biz/log/add/v2";
    //用户查询温度列表
    public static final String TEMP_LIST_URL = LOG_URL + BIZ_LOG_URL + "temp/list";
    //用于查询温度详情
    public static final String TEMP_DETAIL_URL = LOG_URL + BIZ_LOG_URL + "temp/detail";
    //查询日志记录
    public static final String LOG_LIST_URL = LOG_URL + BIZ_LOG_URL + "list/v2";

    public static final String LOG_LIST = LOG_URL + BIZ_LOG_URL + "list/v2";
    //查询应用层日志
    public static final String LOG_LIST_NEWBIE = "https://newbie3d.com/apis/app/ras/biz/log/list/v2";

    /************************ 鉴权 ************************/
    //管理员登录
    public static final String ADMIN_LOGIN_URL = ADMIN_URL + "auth/login";

    public static final String CERT_DELETE_URL = ADMIN_URL + "ras/cert/delete";
    //用于用户登录
    public static final String USER_LOGIN_URL = APP_URL + "auth/userLogin";

    public static final String USER_LOGIN_URL_NEWBIE = "http://newbie3d.com:8181/app/auth/userLogin";
    //刷新Token
    public static final String REFRESH_TOKEN_URL = APP_URL + "auth/refreshToken";


    /**
     * NDEF
     */
    public static final String NDEF_URI = "d156000139://\033\001\000\001\000\001\000\001\000\001\000";
    /**
     * 存在状态位的软标签uid
     */
    public static final String[] UIDARRAY = {
            "040ce50a685f81", "0406e40a685f81", "047be00a685f80", "0482e00a685f80", "048ae00a685f80",
            "0493e00a685f80",
            "049ce00a685f80", "04a3e00a685f80", "04aae00a685f80", "04b2e00a685f80",
            "04b9e00a685f80", "04c0e00a685f80", "04c8e00a685f80", "04d1e00a685f80",
            "04d7e10a685f80", "04dfe10a685f80", "04e6e10a685f80", "04eee10a685f80",
            "04f5e10a685f80", "04fce10a685f80", "0404e20a685f81", "040ae30a685f81",
            "0410e40a685f81", "0418e40a685f81", "041fe40a685f81", "0426e40a685f81",
            "042ee40a685f81", "0428e50a685f81", "0474e00a685f80", "046de00a685f80",
            "0464e00a685f80", "045de00a685f80", "0441e00a685f80", "0421e50a685f81",
            "047be10a685f80", "0474e10a685f80", "046ce10a685f80", "0463e10a685f80",
            "045ce10a685f80", "0455e10a685f80", "044ee10a685f80", "0447e10a685f80",
            "0440e10a685f80", "0446e20a685f80", "044de20a685f80", "0456e20a685f80",
            "045de20a685f80", "0464e20a685f80", "046de20a685f80", "0474e20a685f80",
            "047be20a685f80", "0482e20a685f80", "0489e20a685f80", "0490e20a685f80",
            "0497e20a685f80", "04a0e20a685f80", "04a7e20a685f80", "04afe20a685f80",
            "04b6e20a685f80", "04bde20a685f80", "04c5e20a685f80", "04cfe20a685f80",
            "04d7e30a685f80", "04dee30a685f80", "04e6e30a685f80", "04ede30a685f80",
            "04f4e30a685f80", "04fbe30a685f80", "0404e40a685f81", "0409e60a685f81",
            "0410e60a685f81", "0417e60a685f81", "041ee60a685f81", "0425e60a685f81",
            "042ce60a685f81", "042de70a685f81", "0426e70a685f81", "041fe70a685f81",
            "0418e70a685f81", "0411e70a685f81", "040ae70a685f81", "0404e60a685f81",
            "0404e70a685f81", "040ae80a685f81", "0411e80a685f81", "0418e80a685f81",
            "041fe80a685f81", "0427e80a685f81", "042ee80a685f81", "0433e60a685f81",
            "0437e50a685f81", "043ee50a685f81", "0445e50a685f81", "044ce50a685f81",
            "0453e50a685f81", "045ae50a685f81", "0461e50a685f81", "045be60a685f81",
            "0454e60a685f81", "044de60a685f81", "0446e60a685f81", "043ee60a685f81",
            "0437e60a685f81", "0438e70a685f81", "043fe70a685f81", "0446e70a685f81",
            "044de70a685f81", "0455e70a685f81", "045ce70a685f81", "0463e60a685f81",
            "045de80a685f81", "0455e80a685f81", "044ee80a685f81", "0447e80a685f81",
            "0440e80a685f81", "0439e80a685f81", "0433e70a685f81", "0434e90a685f81",
            "043be90a685f81", "0442e90a685f81", "044ae90a685f81", "0452e90a685f81",
            "045ae90a685f81", "0463e90a685f81", "0459ea0a685f81", "0452ea0a685f81",
            "044bea0a685f81", "0443ea0a685f81", "043cea0a685f81", "0435ea0a685f81",
            "042ee90a685f81", "0427e90a685f81", "041fe90a685f81", "0418e90a685f81",
            "0411e90a685f81", "040ae90a685f81", "0403e80a685f81", "04fee60a685f80",
            "04f9e40a685f80", "04f2e40a685f80", "04eae40a685f80", "04e2e40a685f80",
            "04dbe40a685f80", "04d4e40a685f80", "04cee30a685f80", "04c5e30a685f80",
            "04bde30a685f80", "04b6e30a685f80", "04afe30a685f80", "04a8e30a685f80",
            "04a0e30a685f80", "0497e30a685f80", "0490e30a685f80", "0489e30a685f80",
            "0482e30a685f80", "047be30a685f80", "0473e30a685f80", "046ce30a685f80",
            "0463e30a685f80", "045be30a685f80", "0454e30a685f80", "044de30a685f80",
            "0445e30a685f80", "043ee20a685f80", "0444e40a685f80", "044ce40a685f80",
            "0455e40a685f80", "045ce40a685f80", "0463e40a685f80", "0463e50a685f80",
            "045ce50a685f80", "0454e50a685f80", "044de50a685f80", "0446e50a685f80",
            "043ce60a685f80", "0447e60a685f80", "044fe60a685f80", "0457e60a685f80",
            "045ee60a685f80", "0465e60a685f80", "0464e70a685f80", "045de70a685f80",
            "0456e70a685f80", "044fe70a685f80", "0447e70a685f80", "0446e90a685f80",
            "044ee80a685f80", "0457e80a685f80", "045ee80a685f80", "0465e80a685f80",
            "046ae60a685f80", "0470e40a685f80", "0478e40a685f80", "047fe40a685f80",
            "0465de0a685f80", "045ede0a685f80", "0456de0a685f80", "044fde0a685f80",
            "0447de0a685f80", "0440de0a685f80", "0438dd0a685f80", "042fda0a685f80",
            "0494de0a685f80", "049dde0a685f80", "04a4de0a685f80", "04abde0a685f80",
            "04b2de0a685f80", "04b9de0a685f80", "04c0de0a685f80", "04c7de0a685f80",
            "04d0de0a685f80", "04d8de0a685f80", "04dedf0a685f80", "04e5df0a685f80",
            "04ecdf0a685f80", "04f3df0a685f80", "04fbdf0a685f80", "0404df0a685f81",
            "0409e10a685f81", "0410e10a685f81", "0417e10a685f81", "041fe10a685f81",
            "0426e10a685f81", "042de10a685f81", "0437e10a685f81", "043ce30a685f81",
            "0443e30a685f81", "044ae30a685f81", "0451e30a685f81", "0458e30a685f81",
            "0460e30a685f81", "045ce40a685f81", "0455e40a685f81", "044ee40a685f81",
            "0445e40a685f81", "043ee40a685f81", "0437e40a685f81", "0432e20a685f81",
            "0429e20a685f81", "0422e20a685f81", "041be20a685f81", "0414e20a685f81", "040de20a685f81",
            "042de30a685f81", "0426e30a685f81", "041fe30a685f81", "0418e30a685f81", "0411e30a685f81",
            "040be20a685f81", "0405e10a685f81", "04fde00a685f80", "04f6e00a685f80", "04efe00a685f80",
            "04e8e00a685f80", "04e1e00a685f80", "04dae00a685f80", "04d4df0a685f80", "04cbdf0a685f80",
            "04c4df0a685f80", "04bddf0a685f80", "04b6df0a685f80", "04afdf0a685f80", "04a8df0a685f80",
            "04a1df0a685f80", "0498df0a685f80", "0491df0a685f80", "048adf0a685f80", "0483df0a685f80",
            "047bdf0a685f80", "0445e80a685f80", "046ddf0a685f80", "0464df0a685f80", "045cdf0a685f80",
            "0454df0a685f80", "044cdf0a685f80", "0445df0a685f80", "043edf0a685f80", "0442e00a685f80",
            "0449e00a685f80", "0450e00a685f80", "0457e00a685f80", "045ee00a685f80", "0465e00a685f80",
            "046ee00a685f80", "0475e00a685f80", "047ce00a685f80", "0483e00a685f80", "048be00a685f80",
            "0494e00a685f80", "049de00a685f80", "04a4e00a685f80", "04abe00a685f80", "04b3e00a685f80",
            "04bae00a685f80", "04c1e00a685f80", "04c9e00a685f80", "04d2e00a685f80", "04d9e10a685f80",
            "04e0e10a685f80", "04e7e10a685f80", "04efe10a685f80", "04f6e10a685f80", "04fde10a685f80",
            "0405e20a685f81", "040ae40a685f81", "0411e40a685f81", "0419e40a685f81", "0420e40a685f81",
            "0427e40a685f81", "042ee50a685f81", "0427e50a685f81", "0420e50a685f81", "0419e50a685f81",
            "0412e50a685f81", "040be50a685f81", "0405e40a685f81", "0400e20a685f81", "04f7e20a685f80",
            "04f0e20a685f80", "04e9e20a685f80", "04e2e20a685f80", "04dbe20a685f80", "04d5e10a685f80",
            "04cbe10a685f80", "04c4e10a685f80", "04bde10a685f80", "04b6e10a685f80", "04afe10a685f80",
            "04a8e10a685f80", "04a1e10a685f80", "0497e10a685f80", "0490e10a685f80", "0488e10a685f80",
            "0481e10a685f80", "047ae10a685f80", "0473e10a685f80", "046be10a685f80", "0462e10a685f80",
            "045be10a685f80", "0454e10a685f80", "044de10a685f80", "0446e10a685f80", "0440e20a685f80",
            "0447e20a685f80", "044ee20a685f80", "0457e20a685f80", "045ee20a685f80", "0465e20a685f80",
            "046ee20a685f80", "0475e20a685f80", "047ce20a685f80", "0483e20a685f80", "048ae20a685f80",
            "0491e20a685f80", "0498e20a685f80", "04a1e20a685f80", "04a8e20a685f80", "04b0e20a685f80",
            "04b7e20a685f80", "04bfe20a685f80", "04c6e20a685f80", "04d0e20a685f80", "04d8e30a685f80",
            "04dfe30a685f80", "04e7e30a685f80", "04eee30a685f80", "04f5e30a685f80", "04fce30a685f80",
            "0404e50a685f81", "040ae60a685f81", "0411e60a685f81", "0418e60a685f81", "041fe60a685f81",
            "0426e60a685f81", "042de60a685f81", "042ce70a685f81", "0425e70a685f81", "041ee70a685f81",
            "0417e70a685f81", "0410e70a685f81", "0409e70a685f81", "0403e60a685f81", "0405e70a685f81",
            "040be80a685f81", "0412e80a685f81", "0419e80a685f81", "0420e80a685f81", "0428e80a685f81",
            "042fe80a685f81", "0434e60a685f81", "0438e50a685f81", "043fe50a685f81", "0446e50a685f81",
            "044de50a685f81", "0454e50a685f81", "045be50a685f81", "0461e60a685f81", "045ae60a685f81",
            "0453e60a685f81", "044ce60a685f81", "0445e60a685f81", "043de60a685f81", "0436e60a685f81",
            "0439e70a685f81", "0440e70a685f81", "0447e70a685f81", "044ee70a685f81", "0456e70a685f81",
            "045de70a685f81", "0463e70a685f81", "045ce80a685f81", "0454e80a685f81", "044de80a685f81",
            "0446e80a685f81", "043fe80a685f81", "0438e80a685f81", "0432e70a685f81", "0435e90a685f81",
            "043ce90a685f81", "0443e90a685f81", "044be90a685f81", "0453e90a685f81", "045be90a685f81",
            "0461eb0a685f81", "0458ea0a685f81", "0451ea0a685f81", "044aea0a685f81", "0442ea0a685f81",
            "043bea0a685f81", "0434ea0a685f81", "042de90a685f81", "0425e90a685f81", "041ee90a685f81",
            "0417e90a685f81", "0410e90a685f81", "0409e90a685f81", "0402e80a685f81", "04fde60a685f80",
            "04f8e40a685f80", "04f1e40a685f80", "04e9e40a685f80", "04e1e40a685f80", "04dae40a685f80",
            "04d3e40a685f80", "04cbe30a685f80", "04c4e30a685f80", "04bce30a685f80", "04b5e30a685f80",
            "04aee30a685f80", "04a7e30a685f80", "049fe30a685f80", "0496e30a685f80", "048fe30a685f80",
            "0488e30a685f80", "0481e30a685f80", "047ae30a685f80", "0472e30a685f80", "046be30a685f80",
            "0462e30a685f80", "045ae30a685f80", "0453e30a685f80", "044ce30a685f80", "0444e30a685f80",
            "043de40a685f80", "0445e40a685f80", "044de40a685f80", "0456e40a685f80", "045de40a685f80",
            "0464e40a685f80", "0462e50a685f80", "045be50a685f80", "0453e50a685f80", "044ce50a685f80",
            "0445e50a685f80", "043fe70a685f80", "0448e60a685f80", "0451e60a685f80", "0458e60a685f80",
            "045fe60a685f80", "0466e60a685f80", "0463e70a685f80", "045ce70a685f80", "0455e70a685f80",
            "044ee70a685f80", "0447e80a685f80", "0447e90a685f80", "044fe80a685f80", "0458e80a685f80",
            "045fe80a685f80", "0466e80a685f80", "046be60a685f80", "0471e40a685f80", "0479e40a685f80",
            "046ce50a685f80", "0467de0a685f80", "0460de0a685f80", "0458de0a685f80", "0451de0a685f80",
            "0449de0a685f80", "0442de0a685f80", "043ade0a685f80", "0433db0a685f80", "0433df0a685f80",
            "0499de0a685f80", "04a2de0a685f80", "04a9de0a685f80", "04b0de0a685f80", "04b7de0a685f80",
            "04bede0a685f80", "04c5de0a685f80", "04cede0a685f80", "04d5de0a685f80", "04dcdf0a685f80",
            "04e3df0a685f80", "04eadf0a685f80", "04f1df0a685f80", "04f9df0a685f80", "0402df0a685f81",
            "0408e00a685f81", "040ee10a685f81", "0415e10a685f81", "041ce10a685f81", "0424e10a685f81",
            "042be10a685f81", "0435e10a685f81", "043ae30a685f81", "0441e30a685f81", "0448e30a685f81",
            "044fe30a685f81", "0456e30a685f81", "045ee30a685f81", "045ee40a685f81", "0457e40a685f81",
            "0450e40a685f81", "0449e40a685f81", "0440e40a685f81", "0439e40a685f81", "0434e20a685f81",
            "042be20a685f81", "0424e20a685f81", "041de20a685f81", "0416e20a685f81", "040fe20a685f81",
            "042fe30a685f81", "0428e30a685f81", "0421e30a685f81", "041ae30a685f81", "0413e30a685f81",
            "040ce30a685f81", "0407e10a685f81", "0401e00a685f81", "04f8e00a685f80", "04f1e00a685f80",
            "04eae00a685f80", "04e3e00a685f80", "04dce00a685f80", "04d6df0a685f80", "04cfdf0a685f80",
            "04c6df0a685f80", "04bfdf0a685f80", "04b8df0a685f80", "04b1df0a685f80", "04aadf0a685f80",
            "04a3df0a685f80", "049cdf0a685f80", "0493df0a685f80", "048cdf0a685f80", "0485df0a685f80",
            "047edf0a685f80", "0476df0a685f80", "046fdf0a685f80", "0467df0a685f80", "045edf0a685f80",
            "0456df0a685f80", "044edf0a685f80", "0447df0a685f80", "0440df0a685f80", "0440e00a685f80",
            "0447e00a685f80", "044ee00a685f80", "0455e00a685f80", "045ce00a685f80", "0463e00a685f80",
            "046ce00a685f80", "0473e00a685f80", "047ae00a685f80", "0481e00a685f80", "0489e00a685f80",
            "0492e00a685f80", "0499e00a685f80", "04a2e00a685f80", "04a9e00a685f80", "04b1e00a685f80",
            "04b8e00a685f80", "04bfe00a685f80", "04c7e00a685f80", "04d0e00a685f80", "04d6e10a685f80",
            "04dee10a685f80", "04e5e10a685f80", "04ede10a685f80", "04f4e10a685f80", "04fbe10a685f80",
            "0403e20a685f81", "0409e30a685f81", "040fe40a685f81", "0417e40a685f81", "041ee40a685f81",
            "0425e40a685f81", "042de40a685f81", "0429e50a685f81", "0422e50a685f81", "041be50a685f81",
            "0414e50a685f81", "040de50a685f81", "0407e40a685f81", "0402e20a685f81", "04f9e20a685f80",
            "04f2e20a685f80", "04ebe20a685f80", "04e4e20a685f80", "04dde20a685f80", "04d6e20a685f80",
            "04cfe10a685f80", "04c6e10a685f80", "04bfe10a685f80", "04b8e10a685f80", "04b1e10a685f80",
            "04aae10a685f80", "04a3e10a685f80", "0499e10a685f80", "0492e10a685f80", "048ae10a685f80",
            "0483e10a685f80", "047ce10a685f80", "0475e10a685f80", "046de10a685f80", "0464e10a685f80",
            "045de10a685f80", "0456e10a685f80", "044fe10a685f80", "0448e10a685f80", "0441e10a685f80",
            "0445e20a685f80", "044ce20a685f80", "0455e20a685f80", "045ce20a685f80", "0463e20a685f80",
            "046ce20a685f80", "0473e20a685f80", "047ae20a685f80", "0481e20a685f80", "0488e20a685f80",
            "048fe20a685f80", "0496e20a685f80", "049fe20a685f80", "04a6e20a685f80", "04aee20a685f80",
            "04b5e20a685f80", "04bce20a685f80", "04c4e20a685f80", "04cee20a685f80", "04d5e30a685f80",
            "04dde30a685f80", "04e5e30a685f80", "04ece30a685f80", "04f3e30a685f80", "04fae30a685f80",
            "0403e40a685f81", "0408e60a685f81", "040fe60a685f81", "0416e60a685f81", "041de60a685f81",
            "0424e60a685f81", "042be60a685f81", "042ee70a685f81", "0427e70a685f81", "0420e70a685f81",
            "0419e70a685f81", "0412e70a685f81", "040be70a685f81", "0405e60a685f81", "0403e70a685f81",
            "0409e80a685f81", "0410e80a685f81", "0417e80a685f81", "041ee80a685f81", "0426e80a685f81",
            "042de80a685f81", "0432e60a685f81", "0436e50a685f81", "043de50a685f81", "0444e50a685f81",
            "044be50a685f81", "0452e50a685f81", "0459e50a685f81", "0460e50a685f81", "045ce60a685f81",
            "0455e60a685f81", "044ee60a685f81", "0447e60a685f81", "0440e60a685f81", "0438e60a685f81",
            "0437e70a685f81", "043ee70a685f81", "0445e70a685f81", "044ce70a685f81", "0454e70a685f81",
            "045be70a685f81", "0462e60a685f81", "045fe80a685f81", "0456e80a685f81", "044fe80a685f81",
            "0448e80a685f81", "0441e80a685f81", "043ae80a685f81", "0434e70a685f81", "0433e90a685f81",
            "043ae90a685f81", "0441e90a685f81", "0449e90a685f81", "0451e90a685f81", "0459e90a685f81",
            "0461e90a685f81", "045aea0a685f81", "0453ea0a685f81", "044cea0a685f81", "0444ea0a685f81",
            "043dea0a685f81", "0436ea0a685f81", "042fe90a685f81", "0428e90a685f81", "0420e90a685f81",
            "0419e90a685f81", "0412e90a685f81", "040be90a685f81", "0405e80a685f81", "04ffe60a685f80",
            "04fae40a685f80", "04f3e40a685f80", "04ebe40a685f80", "04e3e40a685f80", "04dce40a685f80",
            "04d5e40a685f80", "04cfe30a685f80", "04c6e30a685f80", "04bee30a685f80", "04b7e30a685f80",
            "04b0e30a685f80", "04a9e30a685f80", "04a1e30a685f80", "0498e30a685f80", "0491e30a685f80",
            "048ae30a685f80", "0483e30a685f80", "047ce30a685f80", "0474e30a685f80", "046de30a685f80",
            "0464e30a685f80", "045ce30a685f80", "0455e30a685f80", "044ee30a685f80", "0446e30a685f80",
            "043ee30a685f80", "0443e40a685f80", "044be40a685f80", "0454e40a685f80", "045be40a685f80",
            "0462e40a685f80", "0464e50a685f80", "045de50a685f80", "0455e50a685f80", "044ee50a685f80",
            "0447e50a685f80", "043fe60a685f80", "0446e60a685f80", "044ee60a685f80", "0456e60a685f80",
            "045de60a685f80", "0464e60a685f80", "0465e70a685f80", "045ee70a685f80", "0457e70a685f80",
            "0450e70a685f80", "0448e70a685f80", "0445e90a685f80", "044de80a685f80", "0456e80a685f80",
            "045de80a685f80", "0464e80a685f80", "0469e60a685f80", "046ee40a685f80", "0476e40a685f80",
            "047ee40a685f80", "046bde0a685f80", "0462de0a685f80", "045bde0a685f80", "0453de0a685f80",
            "044bde0a685f80", "0444de0a685f80", "043dde0a685f80", "0434dc0a685f80", "0434df0a685f80",
            "0497de0a685f80", "04a0de0a685f80", "04a7de0a685f80", "04aede0a685f80", "04b5de0a685f80",
            "04bcde0a685f80", "04c3de0a685f80", "04cade0a685f80", "04d3de0a685f80", "04dadf0a685f80",
            "04e1df0a685f80", "04e8df0a685f80", "04efdf0a685f80", "04f6df0a685f80", "0400df0a685f81",
            "0406e00a685f81", "040ce10a685f81", "0413e10a685f81", "041ae10a685f81", "0422e10a685f81",
            "0429e10a685f81", "0433e10a685f81", "0438e30a685f81", "043fe30a685f81", "0446e30a685f81",
            "044de30a685f81", "0454e30a685f81", "045ce30a685f81", "0460e40a685f81", "0459e40a685f81",
            "0452e40a685f81", "044be40a685f81", "0442e40a685f81", "043be40a685f81", "0436e20a685f81",
            "042de20a685f81", "0426e20a685f81", "041fe20a685f81", "0418e20a685f81", "0411e20a685f81",
            "0433e30a685f81", "042ae30a685f81", "0423e30a685f81", "041ce30a685f81", "0415e30a685f81",
            "040ee30a685f81", "0408e20a685f81", "0403e00a685f81", "04fae00a685f80", "04f3e00a685f80",
            "04ece00a685f80", "04e5e00a685f80", "04dee00a685f80", "04d7e00a685f80", "04d1df0a685f80",
            "04c8df0a685f80", "04c1df0a685f80", "04badf0a685f80", "04b3df0a685f80", "04acdf0a685f80",
            "04a5df0a685f80", "049edf0a685f80", "0495df0a685f80", "048edf0a685f80", "0487df0a685f80",
            "0480df0a685f80", "0478df0a685f80", "0471df0a685f80", "046bdf0a685f80", "0461df0a685f80",
            "0458df0a685f80", "0450df0a685f80", "0449df0a685f80", "0442df0a685f80", "043ee00a685f80",
            "0445e00a685f80", "044ce00a685f80", "0453e00a685f80", "045ae00a685f80", "0461e00a685f80",
            "046ae00a685f80", "0471e00a685f80", "0478e00a685f80", "047fe00a685f80", "0486e00a685f80",
            "048ee00a685f80", "0497e00a685f80", "04a0e00a685f80", "04a7e00a685f80", "04aee00a685f80",
            "04b6e00a685f80", "04bde00a685f80", "04c4e00a685f80", "04cee00a685f80", "04d5e00a685f80",
            "04dce10a685f80", "04e3e10a685f80", "04ebe10a685f80", "04f2e10a685f80", "04f9e10a685f80",
            "0402e10a685f81", "0407e30a685f81", "040de40a685f81", "0414e40a685f81", "041ce40a685f81",
            "0423e40a685f81", "042be40a685f81", "042be50a685f81", "0424e50a685f81", "041de50a685f81",
            "0416e50a685f81", "040fe50a685f81", "0409e40a685f81", "0403e30a685f81", "04fbe20a685f80",
            "04f4e20a685f80", "04ede20a685f80", "04e6e20a685f80", "04dfe20a685f80", "04d8e20a685f80",
            "04d2e10a685f80", "04c8e10a685f80", "04c1e10a685f80", "04bae10a685f80", "04b3e10a685f80",
            "04ace10a685f80", "04a5e10a685f80", "049ee10a685f80", "0494e10a685f80", "048de10a685f80",
            "0485e10a685f80", "047ee10a685f80", "0477e10a685f80", "0470e10a685f80", "0466e10a685f80",
            "045fe10a685f80", "0458e10a685f80", "0451e10a685f80", "044ae10a685f80", "0443e10a685f80",
            "0443e20a685f80", "044ae20a685f80", "0452e20a685f80", "045ae20a685f80", "0461e20a685f80",
            "046ae20a685f80", "0471e20a685f80", "0478e20a685f80", "047fe20a685f80", "0486e20a685f80",
            "048de20a685f80", "0494e20a685f80", "049de20a685f80", "04a4e20a685f80", "04abe20a685f80",
            "04b3e20a685f80", "04bae20a685f80", "04c2e20a685f80", "04c9e20a685f80", "04d4e20a685f80",
            "04dbe30a685f80", "04e2e30a685f80", "04eae30a685f80", "04f1e30a685f80", "04f8e30a685f80",
            "0401e30a685f81", "0407e50a685f81", "040de60a685f81", "0414e60a685f81", "041be60a685f81",
            "0422e60a685f81", "0429e60a685f81", "042fe50a685f81", "0429e70a685f81", "0422e70a685f81",
            "041be70a685f81", "0414e70a685f81", "040de70a685f81", "0407e60a685f81", "0402e60a685f81",
            "0407e80a685f81", "040ee80a685f81", "0415e80a685f81", "041ce80a685f81", "0423e80a685f81",
            "042be80a685f81", "0430e60a685f81", "0435e40a685f81", "043be50a685f81", "0442e50a685f81",
            "0449e50a685f81", "0450e50a685f81", "0457e50a685f81", "045ee50a685f81", "045ee60a685f81",
            "0457e60a685f81", "0450e60a685f81", "0449e60a685f81", "0442e60a685f81", "043ae60a685f81",
            "0435e70a685f81", "043ce70a685f81", "0443e70a685f81", "044ae70a685f81", "0452e70a685f81",
            "0459e70a685f81", "0460e70a685f81", "0461e80a685f81", "0458e80a685f81", "0451e80a685f81",
            "044ae80a685f81", "0443e80a685f81", "043ce80a685f81", "0435e80a685f81", "0432e80a685f81",
            "0438e90a685f81", "043fe90a685f81", "0447e90a685f81", "044fe90a685f81", "0456e90a685f81",
            "045ee90a685f81", "045cea0a685f81", "0455ea0a685f81", "044eea0a685f81", "0446ea0a685f81",
            "043fea0a685f81", "0438ea0a685f81", "0432e90a685f81", "042ae90a685f81", "0422e90a685f81",
            "041be90a685f81", "0414e90a685f81", "040de90a685f81", "0406e90a685f81", "0401e60a685f81",
            "04fce40a685f80", "04f5e40a685f80", "04eee40a685f80", "04e6e40a685f80", "04dee40a685f80",
            "04d7e40a685f80", "04d1e30a685f80", "04c8e30a685f80", "04c1e30a685f80", "04b9e30a685f80",
            "04b2e30a685f80", "04abe30a685f80", "04a3e30a685f80", "049ce30a685f80", "0493e30a685f80",
            "048ce30a685f80", "0485e30a685f80", "047ee30a685f80", "0476e30a685f80", "046fe30a685f80",
            "0466e30a685f80", "045fe30a685f80", "0457e30a685f80", "0450e30a685f80", "0448e30a685f80",
            "0441e30a685f80", "043ee50a685f80", "0448e40a685f80", "0452e40a685f80", "0459e40a685f80",
            "0460e40a685f80", "0466e50a685f80", "045fe50a685f80", "0457e50a685f80", "0450e50a685f80",
            "0449e50a685f80", "0441e60a685f80", "0445e70a685f80", "044be60a685f80", "0454e60a685f80",
            "045be60a685f80", "0462e60a685f80", "0467e70a685f80", "0460e70a685f80", "0459e70a685f80",
            "0452e70a685f80", "044be70a685f80", "0444e80a685f80", "0449e80a685f80", "0454e80a685f80",
            "045be80a685f80", "0462e80a685f80", "0468e70a685f80", "046ce40a685f80", "0474e40a685f80",
            "047ce40a685f80", "046ede0a685f80", "0464de0a685f80", "045dde0a685f80", "0455de0a685f80",
            "044ede0a685f80", "0446de0a685f80", "043fde0a685f80", "0435dd0a685f80", "0435de0a685f80",
            "0495de0a685f80", "049ede0a685f80", "04a5de0a685f80", "04acde0a685f80", "04b3de0a685f80",
            "04bade0a685f80", "04c1de0a685f80", "04c8de0a685f80", "04d1de0a685f80", "04d8df0a685f80",
            "04dfdf0a685f80", "04e6df0a685f80", "04eddf0a685f80", "04f4df0a685f80", "04fcdf0a685f80",
            "0405df0a685f81", "040ae10a685f81", "0411e10a685f81", "0418e10a685f81", "0420e10a685f81",
            "0427e10a685f81", "042fe10a685f81", "0437e20a685f81", "043de30a685f81", "0444e30a685f81",
            "044be30a685f81", "0452e30a685f81", "0459e30a685f81", "0461e30a685f81", "045be40a685f81",
            "0454e40a685f81", "044de40a685f81", "0444e40a685f81", "043de40a685f81", "0437e30a685f81",
            "042fe20a685f81", "0428e20a685f81", "0421e20a685f81", "041ae20a685f81", "0413e20a685f81",
            "0435e30a685f81", "042ce30a685f81", "041ee30a685f81", "0417e30a685f81", "0410e30a685f81",
            "040ae20a685f81", "0404e10a685f81", "04fce00a685f80", "04f5e00a685f80", "04eee00a685f80",
            "04e7e00a685f80", "04e0e00a685f80", "04d9e00a685f80", "04d3df0a685f80", "04cadf0a685f80",
            "04c3df0a685f80", "04bcdf0a685f80", "04b5df0a685f80", "04aedf0a685f80", "04a7df0a685f80",
            "04a0df0a685f80", "0497df0a685f80", "0490df0a685f80", "0489df0a685f80", "0482df0a685f80",
            "047adf0a685f80", "0473df0a685f80", "046dde0a685f80", "0463df0a685f80", "045bdf0a685f80",
            "0453df0a685f80", "044bdf0a685f80", "0444df0a685f80", "043bdf0a685f80", "0443e00a685f80",
            "044ae00a685f80", "0451e00a685f80", "0458e00a685f80", "045fe00a685f80", "0466e00a685f80",
            "046fe00a685f80", "0476e00a685f80", "047de00a685f80", "0484e00a685f80", "048ce00a685f80",
            "0495e00a685f80", "049ee00a685f80", "04a5e00a685f80", "04ace00a685f80", "04b4e00a685f80",
            "04bbe00a685f80", "04c2e00a685f80", "04cae00a685f80", "04d3e00a685f80", "04dae10a685f80",
            "04e1e10a685f80", "04e8e10a685f80", "04f0e10a685f80", "04f7e10a685f80", "0400e10a685f81",
            "0406e20a685f81", "040be40a685f81", "0412e40a685f81", "041ae40a685f81", "0421e40a685f81",
            "0428e40a685f81", "042de50a685f81", "0426e50a685f81", "041fe50a685f81", "0418e50a685f81",
            "0411e50a685f81", "040ae50a685f81", "0405e30a685f81", "04fde20a685f80", "04f6e20a685f80",
            "04efe20a685f80", "04e8e20a685f80", "04e1e20a685f80", "04dae20a685f80", "04d4e10a685f80",
            "04cae10a685f80", "04c3e10a685f80", "04bce10a685f80", "04b5e10a685f80", "04aee10a685f80",
            "04a7e10a685f80", "04a0e10a685f80", "0496e10a685f80", "048fe10a685f80", "0487e10a685f80",
            "0480e10a685f80", "0479e10a685f80", "0472e10a685f80", "046ae10a685f80", "0461e10a685f80",
            "045ae10a685f80", "0453e10a685f80", "044ce10a685f80", "0445e10a685f80", "0441e20a685f80",
            "0448e20a685f80", "044fe20a685f80", "0458e20a685f80", "045fe20a685f80", "0466e20a685f80",
            "046fe20a685f80", "0476e20a685f80", "047de20a685f80", "0484e20a685f80", "048be20a685f80",
            "0492e20a685f80", "0499e20a685f80", "04a2e20a685f80", "04a9e20a685f80", "04b1e20a685f80",
            "04b8e20a685f80", "04c0e20a685f80", "04c7e20a685f80", "04d1e20a685f80", "04d9e30a685f80",
            "04e0e30a685f80", "04e8e30a685f80", "04efe30a685f80", "04f6e30a685f80", "04fde30a685f80",
            "0405e50a685f81", "040be60a685f81", "0412e60a685f81", "0419e60a685f81", "0420e60a685f81",
            "0427e60a685f81", "042ee60a685f81", "042be70a685f81", "0424e70a685f81", "041de70a685f81",
            "0416e70a685f81", "040fe70a685f81", "0408e70a685f81", "0403e50a685f81", "0473e40a685f80",
            "040ce80a685f81", "0413e80a685f81", "041ae80a685f81", "0421e80a685f81", "0429e80a685f81",
            "0430e80a685f81", "0434e50a685f81", "0439e50a685f81", "0440e50a685f81", "0447e50a685f81",
            "044ee50a685f81", "0455e50a685f81", "045ce50a685f81", "0460e60a685f81", "0459e60a685f81",
            "0452e60a685f81", "044be60a685f81", "0444e60a685f81", "043ce60a685f81", "0435e60a685f81",
            "0441e70a685f81", "0448e70a685f81", "0450e70a685f81", "0457e70a685f81", "045ee70a685f81",
            "0463e80a685f81", "045be80a685f81", "0453e80a685f81", "044ce80a685f81", "0445e80a685f81",
            "043ee80a685f81", "0437e80a685f81", "0431e70a685f81", "0436e90a685f81", "043de90a685f81",
            "0445e90a685f81", "044de90a685f81", "0454e90a685f81", "045ce90a685f81", "045feb0a685f81",
            "0457ea0a685f81", "0450ea0a685f81", "0448ea0a685f81", "0441ea0a685f81", "043aea0a685f81",
            "0433ea0a685f81", "042ce90a685f81", "0424e90a685f81", "041de90a685f81", "0416e90a685f81",
            "040fe90a685f81", "0408e90a685f81", "0401e80a685f81", "04fde50a685f80", "04f7e40a685f80",
            "04f0e40a685f80", "04e8e40a685f80", "04e0e40a685f80", "04d9e40a685f80", "04d3e30a685f80",
            "044ce70a685f80", "04c3e30a685f80", "04bbe30a685f80", "04b4e30a685f80", "04ade30a685f80",
            "04a5e30a685f80", "049ee30a685f80", "0495e30a685f80", "048ee30a685f80", "0487e30a685f80",
            "0480e30a685f80", "0479e30a685f80", "0471e30a685f80", "046ae30a685f80", "0461e30a685f80",
            "0459e30a685f80", "0452e30a685f80", "044be30a685f80", "0443e30a685f80", "043de30a685f80",
            "0446e40a685f80", "044fe40a685f80", "0457e40a685f80", "045ee40a685f80", "0465e40a685f80",
            "0461e50a685f80", "045ae50a685f80", "0452e50a685f80", "044be50a685f80", "0445e60a685f80",
            "0442e70a685f80", "0449e60a685f80", "0452e60a685f80", "0459e60a685f80", "0460e60a685f80",
            "0467e60a685f80", "0462e70a685f80", "045be70a685f80", "0454e70a685f80", "044de70a685f80",
            "0446e80a685f80", "0448e90a685f80", "0452e80a685f80", "0459e80a685f80", "0460e80a685f80",
            "0467e80a685f80", "046ce60a685f80", "0472e40a685f80", "047ae40a685f80", "046ade0a685f80",
            "0461de0a685f80", "045ade0a685f80", "0452de0a685f80", "044ade0a685f80", "0453e70a685f80",
            "043cde0a685f80", "0433dc0a685f80", "0435df0a685f80", "0498de0a685f80", "04a1de0a685f80",
            "04a8de0a685f80", "04afde0a685f80", "04b6de0a685f80", "04bdde0a685f80", "04c4de0a685f80",
            "04cbde0a685f80", "04d4de0a685f80", "04dbdf0a685f80", "04e2df0a685f80", "04e9df0a685f80",
            "04f0df0a685f80", "04f7df0a685f80", "0401df0a685f81", "0407e00a685f81", "040de10a685f81",
            "0414e10a685f81", "041be10a685f81", "0423e10a685f81", "042ae10a685f81", "0434e10a685f81",
            "0439e30a685f81", "0440e30a685f81", "0447e30a685f81", "044ee30a685f81", "0455e30a685f81",
            "045de30a685f81", "045fe40a685f81", "0458e40a685f81", "0451e40a685f81", "044ae40a685f81",
            "0441e40a685f81", "043ae40a685f81", "0435e20a685f81", "042ce20a685f81", "0425e20a685f81",
            "041ee20a685f81", "0417e20a685f81", "0410e20a685f81", "0432e30a685f81", "0429e30a685f81",
            "0422e30a685f81", "041be30a685f81", "0414e30a685f81", "040de30a685f81", "0407e20a685f81",
            "0402e00a685f81", "04f9e00a685f80", "04f2e00a685f80", "04ebe00a685f80", "04e4e00a685f80",
            "04dde00a685f80", "04d7df0a685f80", "04d0df0a685f80", "04c7df0a685f80", "04c0df0a685f80",
            "04b9df0a685f80", "04b2df0a685f80", "04abdf0a685f80", "04a4df0a685f80", "049ddf0a685f80",
            "0494df0a685f80", "048ddf0a685f80", "0486df0a685f80", "047fdf0a685f80", "0477df0a685f80",
            "0470df0a685f80", "046adf0a685f80", "045fdf0a685f80", "0457df0a685f80", "044fdf0a685f80",
            "0448df0a685f80", "0441df0a685f80", "043fe00a685f80", "0446e00a685f80", "044de00a685f80",
            "0454e00a685f80", "045be00a685f80", "0462e00a685f80", "046be00a685f80", "0472e00a685f80",
            "0479e00a685f80", "0480e00a685f80", "0488e00a685f80", "048fe00a685f80", "0498e00a685f80",
            "04a1e00a685f80", "04a8e00a685f80", "04afe00a685f80", "04b7e00a685f80", "04bee00a685f80",
            "04c6e00a685f80", "04cfe00a685f80", "04d6e00a685f80", "04dde10a685f80", "04e4e10a685f80",
            "04ece10a685f80", "04f3e10a685f80", "04fae10a685f80", "0403e10a685f81", "0408e30a685f81",
            "040ee40a685f81", "0415e40a685f81", "041de40a685f81", "0424e40a685f81", "042ce40a685f81",
            "042ae50a685f81", "0423e50a685f81", "041ce50a685f81", "0415e50a685f81", "040ee50a685f81",
            "0408e40a685f81", "0402e30a685f81", "04fae20a685f80", "04f3e20a685f80", "04ece20a685f80",
            "04e5e20a685f80", "04dee20a685f80", "04d7e20a685f80", "04d1e10a685f80", "04c7e10a685f80",
            "04c0e10a685f80", "04b9e10a685f80", "04b2e10a685f80", "04abe10a685f80", "04a4e10a685f80",
            "049de10a685f80", "0493e10a685f80", "048be10a685f80", "0484e10a685f80", "047de10a685f80",
            "0476e10a685f80", "046fe10a685f80", "0465e10a685f80", "045ee10a685f80", "0457e10a685f80",
            "0450e10a685f80", "0449e10a685f80", "0442e10a685f80", "0444e20a685f80", "044be20a685f80",
            "0453e20a685f80", "045be20a685f80", "0462e20a685f80", "046be20a685f80", "0472e20a685f80",
            "0479e20a685f80", "0480e20a685f80", "0487e20a685f80", "048ee20a685f80", "0495e20a685f80",
            "049ee20a685f80", "04a5e20a685f80", "04ace20a685f80", "04b4e20a685f80", "04bbe20a685f80",
            "04c3e20a685f80", "04cbe20a685f80", "04d4e30a685f80", "04dce30a685f80", "04e4e30a685f80",
            "04ebe30a685f80", "04f2e30a685f80", "04f9e30a685f80", "0402e40a685f81", "0408e50a685f81",
            "040ee60a685f81", "0415e60a685f81", "041ce60a685f81", "0423e60a685f81", "042ae60a685f81",
            "042fe70a685f81", "0428e70a685f81", "0421e70a685f81", "041ae70a685f81", "0413e70a685f81",
            "040ce70a685f81", "0406e60a685f81", "0402e70a685f81", "0408e80a685f81", "040fe80a685f81",
            "0416e80a685f81", "041de80a685f81", "0424e80a685f81", "042ce80a685f81", "0431e60a685f81",
            "0436e40a685f81", "043ce50a685f81", "0443e50a685f81", "044ae50a685f81", "0451e50a685f81",
            "0458e50a685f81", "045fe50a685f81", "045de60a685f81", "0456e60a685f81", "044fe60a685f81",
            "0448e60a685f81", "0441e60a685f81", "0439e60a685f81", "0436e70a685f81", "043de70a685f81",
            "0444e70a685f81", "044be70a685f81", "0453e70a685f81", "045ae70a685f81", "0461e70a685f81",
            "0460e80a685f81", "0457e80a685f81", "0450e80a685f81", "0449e80a685f81", "0442e80a685f81",
            "043be80a685f81", "0434e80a685f81", "0433e80a685f81", "0439e90a685f81", "0440e90a685f81",
            "0448e90a685f81", "0450e90a685f81", "0457e90a685f81", "0460e90a685f81", "045bea0a685f81",
            "0454ea0a685f81", "044dea0a685f81", "0445ea0a685f81", "043eea0a685f81", "0437ea0a685f81",
            "0430e90a685f81", "0429e90a685f81", "0421e90a685f81", "041ae90a685f81", "0413e90a685f81",
            "040ce90a685f81", "0405e90a685f81", "0400e60a685f81", "04fbe40a685f80", "04f4e40a685f80",
            "04ede40a685f80", "04e5e40a685f80", "04dde40a685f80", "04d6e40a685f80", "04d0e30a685f80",
            "04c7e30a685f80", "04bfe30a685f80", "04b8e30a685f80", "04b1e30a685f80", "04aae30a685f80",
            "04a2e30a685f80", "0499e30a685f80", "0492e30a685f80", "048be30a685f80", "0484e30a685f80",
            "047de30a685f80", "0475e30a685f80", "046ee30a685f80", "0465e30a685f80", "045ee30a685f80",
            "0456e30a685f80", "044fe30a685f80", "0447e30a685f80", "043fe30a685f80", "0442e50a685f80",
            "0449e40a685f80", "0453e40a685f80", "045ae40a685f80", "0461e40a685f80", "0465e50a685f80",
            "045ee50a685f80", "0456e50a685f80", "044fe50a685f80", "0448e50a685f80", "0440e60a685f80",
            "0446e70a685f80", "044de60a685f80", "0455e60a685f80", "045ce60a685f80", "0463e60a685f80",
            "0466e70a685f80", "045fe70a685f80", "0458e70a685f80", "0451e70a685f80", "0449e70a685f80",
            "0444e90a685f80", "044ae80a685f80", "0455e80a685f80", "045ce80a685f80", "0463e80a685f80",
            "0468e60a685f80", "046de40a685f80", "0475e40a685f80", "047de40a685f80", "046cde0a685f80",
            "0463de0a685f80", "045cde0a685f80", "0454de0a685f80", "044cde0a685f80", "0445de0a685f80",
            "043ede0a685f80", "0434dd0a685f80", "0434de0a685f80", "0496de0a685f80", "049fde0a685f80",
            "04a6de0a685f80", "04adde0a685f80", "04b4de0a685f80", "04bbde0a685f80", "04c2de0a685f80",
            "04c9de0a685f80", "04d2de0a685f80", "04d9df0a685f80", "04e0df0a685f80", "04e7df0a685f80",
            "04eedf0a685f80", "04f5df0a685f80", "04fddf0a685f80", "0405e00a685f81", "040be10a685f81",
            "0412e10a685f81", "0419e10a685f81", "0421e10a685f81", "0428e10a685f81", "0432e10a685f81",
            "0438e20a685f81", "043ee30a685f81", "0445e30a685f81", "044ce30a685f81", "0453e30a685f81",
            "045ae30a685f81", "0461e20a685f81", "045ae40a685f81", "0453e40a685f81", "044ce40a685f81",
            "0443e40a685f81", "043ce40a685f81", "0436e30a685f81", "042ee20a685f81", "0427e20a685f81",
            "0420e20a685f81", "0419e20a685f81", "0412e20a685f81", "0434e30a685f81", "042be30a685f81",
            "0424e30a685f81", "041de30a685f81", "0416e30a685f81", "040fe30a685f81", "0409e20a685f81",
            "0404e00a685f81", "04fbe00a685f80", "04f4e00a685f80", "04ede00a685f80", "04e6e00a685f80",
            "04dfe00a685f80", "04d8e00a685f80", "04d2df0a685f80", "04c9df0a685f80", "04c2df0a685f80",
            "04bbdf0a685f80", "04b4df0a685f80", "04addf0a685f80", "04a6df0a685f80", "049fdf0a685f80", "0496df0a685f80", "048fdf0a685f80", "0488df0a685f80", "0481df0a685f80", "0479df0a685f80", "0472df0a685f80", "046cdf0a685f80", "0462df0a685f80", "0459df0a685f80", "0452df0a685f80", "044adf0a685f80", "0443df0a685f80", "043ce00a685f80", "0444e00a685f80", "044be00a685f80", "0452e00a685f80", "0459e00a685f80", "0460e00a685f80", "0467e00a685f80", "0470e00a685f80", "0477e00a685f80", "047ee00a685f80", "0485e00a685f80", "048de00a685f80", "0496e00a685f80", "049fe00a685f80", "04a6e00a685f80", "04ade00a685f80", "04b5e00a685f80", "04bce00a685f80", "04c3e00a685f80", "04cbe00a685f80", "04d4e00a685f80", "04dbe10a685f80", "04e2e10a685f80", "04eae10a685f80", "04f1e10a685f80", "04f8e10a685f80", "0401e10a685f81", "0406e30a685f81", "040ce40a685f81", "0413e40a685f81", "041be40a685f81", "0422e40a685f81", "042ae40a685f81", "042ce50a685f81", "0425e50a685f81", "041ee50a685f81", "0417e50a685f81", "0410e50a685f81", "0409e50a685f81", "0404e30a685f81", "04fce20a685f80", "04f5e20a685f80", "04eee20a685f80", "04e7e20a685f80", "04e0e20a685f80", "04d9e20a685f80", "04d3e10a685f80", "04c9e10a685f80", "04c2e10a685f80", "04bbe10a685f80", "04b4e10a685f80", "04ade10a685f80", "04a6e10a685f80", "049fe10a685f80", "0495e10a685f80", "048ee10a685f80", "0486e10a685f80", "047fe10a685f80", "0478e10a685f80", "0471e10a685f80", "0467e10a685f80", "0460e10a685f80", "0459e10a685f80", "0452e10a685f80", "044be10a685f80", "0444e10a685f80", "0442e20a685f80", "0449e20a685f80", "0451e20a685f80", "0459e20a685f80", "0460e20a685f80", "0467e20a685f80", "0470e20a685f80", "0477e20a685f80", "047ee20a685f80", "0485e20a685f80", "048ce20a685f80", "0493e20a685f80", "049ce20a685f80", "04a3e20a685f80", "04aae20a685f80", "04b2e20a685f80", "04b9e20a685f80", "04c1e20a685f80", "04c8e20a685f80", "04d2e20a685f80", "04dae30a685f80", "04e1e30a685f80", "04e9e30a685f80", "04f0e30a685f80", "04f7e30a685f80", "0400e30a685f81", "0406e50a685f81", "040ce60a685f81", "0413e60a685f81", "041ae60a685f81", "0421e60a685f81", "0428e60a685f81", "042fe60a685f81", "042ae70a685f81", "0423e70a685f81", "041ce70a685f81", "0415e70a685f81", "040ee70a685f81", "0407e70a685f81", "0402e50a685f81", "0406e80a685f81", "040de80a685f81", "0414e80a685f81", "041be80a685f81", "0422e80a685f81", "042ae80a685f81", "0430e70a685f81", "0434e40a685f81", "043ae50a685f81", "0441e50a685f81", "0448e50a685f81", "044fe50a685f81", "0456e50a685f81", "045de50a685f81", "045fe60a685f81", "0458e60a685f81", "0451e60a685f81", "044ae60a685f81", "0443e60a685f81", "043be60a685f81", "0435e50a685f81", "043be70a685f81", "0442e70a685f81", "0449e70a685f81", "0451e70a685f81", "0458e70a685f81", "045fe70a685f81", "0462e80a685f81", "045ae80a685f81", "0452e80a685f81", "044be80a685f81", "0444e80a685f81", "043de80a685f81", "0436e80a685f81", "0431e80a685f81", "0437e90a685f81", "043ee90a685f81", "0446e90a685f81", "044ee90a685f81", "0455e90a685f81", "045de90a685f81", "045eea0a685f81", "0456ea0a685f81", "044fea0a685f81", "0447ea0a685f81", "0440ea0a685f81", "0439ea0a685f81", "0432ea0a685f81", "042be90a685f81", "0423e90a685f81", "041ce90a685f81", "0415e90a685f81", "040ee90a685f81", "0407e90a685f81", "0401e70a685f81", "04fce50a685f80", "04f6e40a685f80", "04efe40a685f80", "04e7e40a685f80", "04dfe40a685f80", "04d8e40a685f80", "04d2e30a685f80", "04c9e30a685f80", "04c2e30a685f80", "04bae30a685f80", "04b3e30a685f80", "04ace30a685f80", "04a4e30a685f80", "049de30a685f80", "0494e30a685f80", "048de30a685f80", "0486e30a685f80", "047fe30a685f80", "0477e30a685f80", "0470e30a685f80", "0467e30a685f80", "0460e30a685f80", "0458e30a685f80", "0451e30a685f80", "044ae30a685f80", "0442e30a685f80", "043de60a685f80", "0447e40a685f80", "0450e40a685f80", "0458e40a685f80", "045fe40a685f80", "0466e40a685f80", "0460e50a685f80", "0458e50a685f80", "0451e50a685f80", "044ae50a685f80", "0444e60a685f80", "0444e70a685f80", "044ae60a685f80", "0453e60a685f80", "045ae60a685f80", "0461e60a685f80", "0467e50a685f80", "0461e70a685f80", "045ae70a685f80", "0489c0b2953c80", "041ae50a685f81"};

}
