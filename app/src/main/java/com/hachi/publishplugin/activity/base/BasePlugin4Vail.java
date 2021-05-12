package com.hachi.publishplugin.activity.base;

public class BasePlugin4Vail extends BasePlugin {
    protected static Boolean isPswTag = false;//标签类型，false为RAS，true为14443或15693
    protected static String config_password = "";
    protected static String config_rasId = "";
    protected static String newCert = "";//明文证书
    protected static Boolean hasCert = false;
    protected static Boolean isTmpTag = false;
    protected static Boolean isV5State = false;
}
