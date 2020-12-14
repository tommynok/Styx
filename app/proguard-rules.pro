-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-keep class com.jamal2367.styx.settings.** { *; }

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class com.jamal2367.styx.reading.*

-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keep class com.jamal2367.styx.view.StyxView$StyxChromeClient {
    void openFileChooser(android.webkit.ValueCallback);
    void openFileChooser(android.webkit.ValueCallback, java.lang.String);
    void openFileChooser(android.webkit.ValueCallback, java.lang.String, java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class org.jsoup.** {
    public *;
}

-dontwarn android.support.**
-dontwarn com.jamal2367.styx.**
-dontwarn net.i2p.crypto.CertUtil
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.apache.http.conn.ssl.DefaultHostnameVerifier
-dontwarn org.apache.http.HttpHost
-dontwarn org.conscrypt.**
-dontwarn javax.annotation.**
