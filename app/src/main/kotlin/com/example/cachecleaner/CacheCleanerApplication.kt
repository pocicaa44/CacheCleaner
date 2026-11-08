package com.example.cachecleaner

import android.app.Application
import android.content.Context
import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass

class CacheCleanerApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                HiddenApiBypass.addHiddenApiExemptions("L")
            } catch (t: Throwable) {
                // Ignore if platform denies or fails
            }
        }
    }
}
