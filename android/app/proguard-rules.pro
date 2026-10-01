# Arrow Escape ProGuard / R8 Configuration

# Preserve Keep annotations
-keepclassmembers class * {
    @androidx.annotation.Keep <methods>;
    @androidx.annotation.Keep <fields>;
}

# Preserve Game Data and Model Classes (prevents DataStore serialization issues)
-keep class com.arrowescape.game.model.** { *; }
-keep class com.arrowescape.game.data.** { *; }

# Google Mobile Ads SDK rules
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# AndroidX DataStore Preferences
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Jetpack Compose runtime
-keep class androidx.compose.runtime.** { *; }
