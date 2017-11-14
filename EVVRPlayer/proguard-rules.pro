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
#得图全景播放器
-keep class org.simple.** { *; }
-keep interface org.simple.** { *; }

-keep class org.opencv.** {
    <fields>;
    <methods>;
}

-keep class com.google.** {
    <fields>;
    <methods>;
}

-keep class com.adobe.** {
    <fields>;
    <methods>;
}

-keep class org.apache.** {
    <fields>;
    <methods>;
}

-keep class com.player.data.** {
    <fields>;
    !private <methods>;
}

-keep class com.player.util.** {
    <fields>;
    !private <methods>;
}

-keep class com.player.panoplayer.** {
    <fields>;
    !private <methods>;
}

-keep class com.player.renderer.** {
    <fields>;
    !private <methods>;
}

-keep class com.player.calibration.** {
    <fields>;
    <methods>;
}

-keep class com.player.configuration.machine.** {
    <fields>;
    <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclasseswithmembers class * {
    void onClick*(...);
}

-keepclasseswithmembers class * {
    *** *Callback(...);
}

-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

-keep class org.apache.http.** {
    <fields>;
    <methods>;
}

-keep class com.nostra13.universalimageloader.** {
    <fields>;
    <methods>;
}

-keep class aurelienribon.tweenengine.** {
    <fields>;
    <methods>;
}

-keep class cz.msebera.** {
    <fields>;
    <methods>;
}

-keep class tv.danmaku.ijk.media.player.** {
    <fields>;
    <methods>;
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

-dontwarn com.**

-keep class com.easyvaas.sdk.vrplayer.** {
    <fields>;
    !private <methods>;
}

-keepattributes Exceptions,InnerClasses,...
-keep class com.easyvaas.sdk.vrplayer.**.A{
    *;
    static *;
}