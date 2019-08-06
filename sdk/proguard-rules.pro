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


###############################################优化处理#########################################

#关闭压缩；
#-dontshrink
#关闭优化
#-dontoptimize
#关闭混淆
#-dontobfuscate



#声明第三方jar包,不用管第三方jar包中的.so文件(如果有)
#-libraryjars libs/nanohttpd-2.3.1.jar
#-libraryjars libs/Java-WebSocket-1.4.0.jar
#-libraryjars libs/junit-4.12.jar
#-libraryjars libs/disklrucache-2.0.2.jar
#-libraryjars libs/fastjson-1.2.58.jar
#-libraryjars libs/okhttp-3.14.2.jar







####################################对于一些基本指令的添加#########################################

#压缩级别0-7，Android一般为5(对代码迭代优化的次数)
-optimizationpasses 5


# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames

# 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses

# 这句话能够使我们的项目混淆后产生映射文件
# 包含有类名->混淆后类名的映射关系
-verbose

# 不做预校验，preverify是proguard的四个步骤之一，Android不需要preverify，去掉这一步能够加快混淆速度。
-dontpreverify

# 添加支持的jar(引入libs下的所有jar包)
#-libraryjars libs(*.jar;)

# 保留Annotation不混淆
#-keepattributes *Annotation*
#-keep class * extends java.lang.annotation.Annotation {*;}

# 避免混淆泛型
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 保持反射不被混淆
-keepattributes EnclosingMethod

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*







############################Android开发中一些需要保留的公共部分#########################################

# 保留我们使用的四大组件，自定义的Application等等这些类不被混淆
# 因为这些子类都有可能被外部调用
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Appliction
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class * extends android.view.View
#-keep public class com.android.vending.licensing.ILicensingService


# 保留support下的所有类及其内部类
-keep class android.support.** {*;}

# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**

# 保留R下面的资源
#-keep class **.R$* {*;}

# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留在Activity中的方法参数是view的方法，
# 这样以来我们在layout中写的onClick就不会被影响
#-keepclassmembers class * extends android.app.Activity{
#    public void *(android.view.View);
#}

# 保留枚举类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留我们自定义控件（继承自View）不被混淆
#-keep public class * extends android.view.View{
#    *** get*();
#    void set*(***);
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}

# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable序列化的类不被混淆
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# webView处理，项目中没有使用到webView忽略即可
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
#}
#-keepclassmembers class * extends android.webkit.webViewClient {
#    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
#    public boolean *(android.webkit.WebView, java.lang.String);
#}
#-keepclassmembers class * extends android.webkit.webViewClient {
#    public void *(android.webkit.webView, jav.lang.String);
#}

# 移除Log类打印各个等级日志的代码，打正式包的时候可以做为禁log使用，这里可以作为禁止log打印的功能使用
# 记得proguard-android.txt中一定不要加-dontoptimize才起作用，此处只可以看到err
# 另外的一种实现方案是通过BuildConfig.DEBUG的变量来控制
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
}


########################################第三方依赖库########################################
# FastJson
# https://github.com/alibaba/fastjson
-dontwarn com.alibaba.fastjson.**
-keepattributes Signature
-keepattributes *Annotation*

# okhttp 3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


# Logger
-dontwarn com.orhanobut.logger.**
-keep class com.orhanobut.logger.** { *; }
-keep interface com.orhanobut.logger.** { *; }


#Java-WebSocket
-dontwarn org.java_websocket.**
-keep class org.java_websocket.** { *; }
-keep interface org.java_websocket.** { *; }


#disklrucache
-dontwarn com.jakewharton.disklrucache.**
-keep class com.jakewharton.disklrucache.** { *; }


#org.webrtc
-dontwarn org.webrtc.**
-dontwarn org.chromium.build.**
-keep class org.webrtc.** { *; }
-keep class org.chromium.build.** { *; }


#androidasync
-dontwarn com.koushikdutta.async.**
-keep class com.koushikdutta.async.** { *; }


#nanohttpd
-dontwarn fi.iki.elonen.**
-keep class fi.iki.elonen.** { *; }


#junit
-dontwarn junit.**
-dontwarn org.junit.**
-keep class junit.** { *; }
-keep interface junit.** { *; }
-keep class org.junit.** { *; }
-keep interface org.junit.** { *; }

########################################项目中个性化处理#########################################

#保持了类P2pEngine里面所有public修饰的成员和方法，以及类名
-keepclasseswithmembers class com.cdnbye.sdk.P2pEngine {
    public *;
}

#保持了类P2pConfig 里面public修饰的方法 和 类名不变
-keepclasseswithmembers class com.cdnbye.sdk.P2pConfig {
    public <methods>;
}

#不混淆内部类，需要用$修饰，否则可能出错
#不混淆内部类Builder以及里面的全部public 成员或者方法
#Builder 是 P2pConfig 类中的内部类
-keep class com.cdnbye.sdk.P2pConfig$Builder{
     public <fields>;
     public <methods>;
}


#保持了类ChannelIdCallback 里面public修饰的方法 和 类名不变
-keepclasseswithmembers class com.cdnbye.sdk.ChannelIdCallback {
    public <methods>;
}

#保持了类P2pStatisticsListener 里面public修饰的方法 和 类名不变
-keepclasseswithmembers class com.cdnbye.sdk.P2pStatisticsListener {
    public <methods>;
}






#不混淆内部类，需要用$修饰，否则可能出错
#不混淆内部类Builder以及里面的全部public 成员或者方法
#-keep class com.cdnbye.sdk.m3u8.data.*$Builder{
#      public <fields>;
#      public <methods>;
#}

#不混淆内部类，需要用$修饰，否则可能出错
#不混淆内部类Builder以及里面的全部public 成员或者方法
#ParsingMode,PlaylistWriter 这俩个类
#-keep class com.cdnbye.sdk.m3u8.*$Builder{
#     public <fields>;
#     public <methods>;
#}


########################################反射相关的类和方法处理#########################################

