# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontshrink

#-keep class com.hachi.publishplugin.activity.CertificatePlugin
-keep class com.hachi.publishplugin.bean.ResultBean{*;}
#-keep class com.hachi.publishplugin.bean.Resources{*;}
#-keep class com.hachi.publishplugin.bean.RAS14443{*;}
#-keep class com.hachi.publishplugin.bean.RASNormalBean{*;}
-keep class com.hachi.publishplugin.activity.CertPlugin
-keep class com.hachi.publishplugin.activity.ValiPlugin
-keep class com.hachi.publishplugin.bean.TagBean{*;}
#-keep class com.hachi.publishplugin.bean.TagBean.DataBean{*;}
-keep class com.hachi.publishplugin.bean.VerifyBean{*;}
#-keep class com.hachi.publishplugin.gson.ParseJson{*;}
#-keep class com.hachi.publishplugin.Api
#-keep class com.hachi.publishplugin.JniTools{*;}
#-assumenosideeffects class android.util.Log{
#    public static *** v(...);
#    public static *** i(...);
#    public static *** d(...);
#    public static *** w(...);
#    public static *** e(...);
#}

#手动启用support keep注解
#http://tools.android.com/tech-docs/support-annotations
#-dontskipnonpubliclibraryclassmembers
#-printconfiguration
-keep,allowobfuscation @interface android.support.annotation.Keep

 -keep @android.support.annotation.Keep class *
 -keepclassmembers class * {
     @android.support.annotation.Keep *;
 }
 -keepattributes SourceFile,LineNumberTable


 # This is a configuration file for ProGuard.
 # http://proguard.sourceforge.net/index.html#manual/usage.html
 #
 # This file is no longer maintained and is not used by new (2.2+) versions of the
 # Android plugin for Gradle. Instead, the Android plugin for Gradle generates the
 # default rules at build time and stores them in the build directory.

 # Optimizations: If you don't want to optimize, use the
 # proguard-android.txt configuration file instead of this one, which
 # turns off the optimization flags.  Adding optimization introduces
 # certain risks, since for example not all optimizations performed by
 # ProGuard works on all versions of Dalvik.  The following flags turn
 # off various optimizations known to have issues, but the list may not
 # be complete or up to date. (The "arithmetic" optimization can be
 # used if you are only targeting Android 2.0 or later.)  Make sure you
 # test thoroughly if you go this route.
 -optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
 -optimizationpasses 5
 #-allowaccessmodification
 -dontpreverify

 # The remainder of this file is identical to the non-optimized version
 # of the Proguard configuration file (except that the other file has
 # flags to turn off optimization).

 -dontusemixedcaseclassnames
 -dontskipnonpubliclibraryclasses
 -verbose

 -keepattributes *Annotation*
 -keep public class com.google.vending.licensing.ILicensingService
 -keep public class com.android.vending.licensing.ILicensingService

 # For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
 -keepclasseswithmembernames class * {
     native <methods>;
 }

 # keep setters in Views so that animations can still work.
 # see http://proguard.sourceforge.net/manual/examples.html#beans
 -keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
 }

 # We want to keep methods in Activity that could be used in the XML attribute onClick
 -keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
 }

 # For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
 -keepclassmembers enum * {
     public static **[] values();
     public static ** valueOf(java.lang.String);
 }

 -keepclassmembers class * implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator CREATOR;
 }

 -keepclassmembers class **.R$* {
     public static <fields>;
 }

 # The support library contains references to newer platform versions.
 # Don't warn about those in case this app is linking against an older
 # platform version.  We know about them, and they are safe.
 -dontwarn android.support.**

 # Understand the @Keep support annotation.
 -keep class android.support.annotation.Keep

 -keep @android.support.annotation.Keep class * {*;}

 -keepclasseswithmembers class * {
     @android.support.annotation.Keep <methods>;
 }

 -keepclasseswithmembers class * {
     @android.support.annotation.Keep <fields>;
 }

 -keepclasseswithmembers class * {
     @android.support.annotation.Keep <init>(...);
 }




