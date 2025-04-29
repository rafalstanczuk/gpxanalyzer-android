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

# Keep OkHttp Platform classes
-keep class org.bouncycastle.jsse.** { *; }
-keep class org.conscrypt.** { *; }
-keep class org.openjsse.** { *; }

# Keep Joda-Time conversion annotations
-keep class org.joda.convert.** { *; }
-keep class org.joda.time.** { *; }

# Keep internal Java classes
-dontwarn sun.**
-dontwarn com.android.org.conscrypt.**
-dontwarn org.apache.harmony.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.conscrypt.**

# Keep all classes that might be used by OkHttp
-keepclassmembers class * implements javax.net.ssl.SSLSocketFactory {
    *;
}
-keepclassmembers class * implements javax.net.ssl.HostnameVerifier {
    *;
}