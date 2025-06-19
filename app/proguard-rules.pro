# EliteG - Android Game Booster ProGuard Rules
# Optimized for performance and security

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep generic signatures for reflection
-keepattributes Signature

# Keep inner classes
-keepattributes InnerClasses,EnclosingMethod

# Application classes - keep main components
-keep public class com.dnagda.eliteG.MainActivity { *; }
-keep public class com.dnagda.eliteG.SplashScreen { *; }
-keep public class com.dnagda.eliteG.ADBInstructionsActivity { *; }

# Keep model classes
-keep class com.dnagda.eliteG.GameApp { *; }
-keep class com.dnagda.eliteG.WrappedDrawable { *; }

# Keep manager classes with their public methods
-keep class com.dnagda.eliteG.SettingsManager {
    public <methods>;
}
-keep class com.dnagda.eliteG.GameAppManager {
    public <methods>;
}
-keep class com.dnagda.eliteG.ExecuteADBCommands {
    public <methods>;
}

# AndroidX and Support Library
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Material Design Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep activity lifecycle methods
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Optimization settings
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove unused resources
-dontwarn org.xmlpull.v1.**
-dontwarn org.kxml2.io.**
-dontwarn android.content.res.**

# Keep crash reporting
-keepattributes SourceFile,LineNumberTable