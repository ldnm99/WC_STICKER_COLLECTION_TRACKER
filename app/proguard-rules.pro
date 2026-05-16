# ============================================================
# WC2026Stickers ProGuard / R8 rules
# ============================================================

# --- Kotlin ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlin.Metadata { *; }

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- Hilt / Dagger ---
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* <fields>;
    @javax.inject.* <init>(...);
    @dagger.* <fields>;
    @dagger.* <init>(...);
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# --- Gson (backup/restore serialization) ---
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep backup data model classes so Gson can serialize/deserialize them correctly
-keep class com.example.wc2026stickers.data.backup.** { *; }

# --- Jetpack Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- AndroidX Lifecycle ---
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keepclassmembers class androidx.lifecycle.** { *; }
-keep class androidx.lifecycle.DefaultLifecycleObserver

# --- AndroidX Navigation ---
-keepnames class androidx.navigation.** { *; }

# --- General Android ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
