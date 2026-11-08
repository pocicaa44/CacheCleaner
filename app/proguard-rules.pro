# Proguard rules for Cache Cleaner

# Keep HiddenApiBypass
-dontwarn dalvik.system.VMRuntime
-if class org.lsposed.hiddenapibypass.HiddenApiBypass
-keepclassmembers class org.lsposed.hiddenapibypass.Helper$* { *; }

# Keep Shizuku IPC
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }

# Keep AIDL generated interfaces and stubs
-keep class android.content.pm.IPackageDataObserver** { *; }
-keep interface android.content.pm.IPackageDataObserver** { *; }

# Keep Domain Models
-keep class com.example.cachecleaner.domain.model.** { *; }
