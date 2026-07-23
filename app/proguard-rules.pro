# ============================================================
# ProGuard Rules for OrderFlow
# These rules prevent the release build minifier from
# removing or renaming classes that are needed at runtime.
# ============================================================

# Keep Firebase model classes (Firestore uses reflection to map documents to POJOs)
-keep class com.orderflow.data.model.** { *; }

# Keep Firebase Auth classes
-keepattributes Signature
-keepattributes *Annotation*

# Keep Firestore — it uses reflection internally
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Gson model serialization (used for backup/restore)
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum values (used in Resource<T> status, MessageLog status)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep custom Exception subclasses for proper crash reporting
-keep public class * extends java.lang.Exception

# Lottie
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# Shimmer
-keep class com.facebook.shimmer.** { *; }

# Remove all Log calls in release to prevent log leaks
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
