# EliteG ProGuard Configuration
# Optimized for production builds

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

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
-keepattributes LineNumberTable,SourceFile

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all classes in our main package
-keep class com.dnagda.eliteG.** { *; }

# Keep all utility classes and their methods
-keep class com.dnagda.eliteG.utils.** { *; }

# Keep ADB command execution classes (critical for functionality)
-keep class com.dnagda.eliteG.ExecuteADBCommands { *; }
-keep class com.dnagda.eliteG.ExecuteADBCommands$* { *; }

# Keep settings manager and game app classes
-keep class com.dnagda.eliteG.SettingsManager { *; }
-keep class com.dnagda.eliteG.GameApp { *; }
-keep class com.dnagda.eliteG.GameAppManager { *; }

# Keep constants class
-keep class com.dnagda.eliteG.utils.Constants { *; }

# AndroidX and Material Design
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Keep all View classes and their methods for UI functionality
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# Keep ViewBinding classes
-keep class * extends androidx.viewbinding.ViewBinding {
    public static * inflate(...);
    public static * bind(...);
}

# Keep custom drawable wrapper
-keep class com.dnagda.eliteG.WrappedDrawable { *; }

# Keep Activity and Service classes
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable implementations
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Performance optimizations
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# Remove logging in release builds (except errors)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remove our custom debug logging in release
-assumenosideeffects class com.dnagda.eliteG.utils.Logger {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Advanced optimizations
-repackageclasses 'eliteg'
-flattenpackagehierarchy 'eliteg'

# Remove unused resources
-keep class **.R
-keep class **.R$* { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep annotations
-keepattributes *Annotation*
-keep @interface *

# Keep runtime visible annotations
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# Security: Obfuscate sensitive method names but keep functionality
-keepclassmembers class com.dnagda.eliteG.ExecuteADBCommands {
    public static boolean hasADBPermissions();
    public static boolean execute(...);
    public static *** executeWithResult(...);
}
