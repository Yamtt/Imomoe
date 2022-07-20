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

#-------------------------实体类数据，不能混淆，忽略了Serializable接口和Parcelable接口
-keep class * implements java.io.Serializable { *;}
-keep class * implements android.os.Parcelable { *;}

#-------------------------Flurry SDK
# Required to preserve the Flurry SDK
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepclasseswithmembers class * {
   public <init>(android.content.Context, android.util.AttributeSet, int);
}
# Google Play Services library
-keep class * extends java.util.ListResourceBundle {
protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}

#-------------------------EventBus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#-------------------------Aria
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}
-keep class **$$DownloadListenerProxy{ *; }
-keep class **$$UploadListenerProxy{ *; }
-keep class **$$DownloadGroupListenerProxy{ *; }
-keep class **$$DGSubListenerProxy{ *; }
-keepclasseswithmembernames class * {
    @Download.* <methods>;
    @Upload.* <methods>;
    @DownloadGroup.* <methods>;
}

#-------------------------okhttp
-keep class okhttp3.internal.publicsuffix.PublicSuffixDatabase

#-------------------------gsyvideoplayer播放器
-keep class com.shuyu.gsyvideoplayer.video.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
# 不混淆AnimeVideoPlayer，否则SimplePlayActivity横屏后布局是未横屏的
-keep class com.skyd.imomoe.view.component.player.AnimeVideoPlayer { *; }
-keep class com.skyd.imomoe.view.component.player.DanmakuVideoPlayer { *; }

# 自定义数据源接口不应被混淆
-keep class com.skyd.imomoe.model.interfaces.** { *; }
# 与自定义数据源相关的类不应该被混淆
-keep class com.skyd.imomoe.adsapi.** { *; }
-keep class com.skyd.imomoe.util.Util { *; }
-keep class com.skyd.imomoe.bean.** { *; }
-keep class com.skyd.imomoe.config.** { *; }
-keep class com.skyd.imomoe.route.** { *; }
-keep class com.skyd.imomoe.model.util.** { *; }
-keep class com.skyd.imomoe.util.html.source.** { *; }
-keep class com.skyd.imomoe.util.eventbus.** { *; }
-keep class com.skyd.imomoe.util.ToastKt { *; }
-keep class com.skyd.imomoe.net.RetrofitManager { *; }
-keep class com.skyd.imomoe.net.RetrofitManager$Companion { *; }
-keep class retrofit2.** { *; }
# 与自定义数据源相关的库不应该被混淆
-keep class com.google.gson.** { *; }
-keep class org.jsoup.** { *; }
-keep class org.greenrobot.eventbus.** { *; }
# kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#-------------------------Retrofit2
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

#-------------------------RxJava
-dontwarn java.util.concurrent.Flow*

#-------------------------cling
-dontwarn org.eclipse.jetty.**
-dontwarn org.fourthline.cling.**
-dontwarn org.seamless.**
-keep class org.fourthline.cling.** { *; }
-keepattributes Annotation
-keep class org.xmlpull.v1.* { *; }
-dontwarn org.xmlpull.v1.**
-dontwarn javax.xml.namespace.**

#for media render state machine
#-keep class org.seamless.statemachine.** {;}
#-keepclassmembers class * implements org.fourthline.cling.support.avtransport.impl.state.AbstractState {;}

#-------------------------AkDanmaku v1.0.3
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*
-dontwarn com.badlogic.gdx.graphics.g2d.freetype.FreetypeBuild
-keep class com.kuaishou.akdanmaku.ecs.system.ActionSystem { *; }
-keep class com.kuaishou.akdanmaku.ecs.system.DanmakuSystem { *; }
-keep class com.kuaishou.akdanmaku.ecs.system.DataSystem { *; }
-keep class com.kuaishou.akdanmaku.ecs.system.RenderSystem { *; }
-keep class com.kuaishou.akdanmaku.ecs.system.layout.LayoutSystem { *; }
# Required if using Gdx-Controllers extension
-keep class com.badlogic.gdx.controllers.android.AndroidControllers

#-------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.AppCompatActivity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends android.app.Application
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * implements java.lang.annotation.Annotation { *;}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}
-ignorewarnings
#-----------------------------