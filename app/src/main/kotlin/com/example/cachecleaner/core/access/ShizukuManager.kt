package com.example.cachecleaner.core.access

import android.content.Context
import android.content.pm.PackageManager
import com.example.cachecleaner.domain.model.AccessStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

object ShizukuManager {

    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const val REQUEST_CODE = 4001

    private val _accessStatus = MutableStateFlow(AccessStatus.SERVICE_NOT_RUNNING)
    val accessStatus: StateFlow<AccessStatus> = _accessStatus.asStateFlow()

    private var isListenersRegistered = false

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        updateStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _accessStatus.value = AccessStatus.SERVICE_NOT_RUNNING
    }

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    _accessStatus.value = AccessStatus.AVAILABLE
                } else {
                    _accessStatus.value = AccessStatus.PERMISSION_REQUIRED
                }
            }
        }

    fun init(context: Context) {
        if (!isListenersRegistered) {
            try {
                Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
                Shizuku.addBinderDeadListener(binderDeadListener)
                Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
                isListenersRegistered = true
            } catch (e: Exception) {
                // Shizuku provider not yet available
            }
        }
        updateStatus(context)
    }

    fun updateStatus(context: Context? = null) {
        if (context != null && !isShizukuInstalled(context)) {
            _accessStatus.value = AccessStatus.NOT_INSTALLED
            return
        }

        val pingSuccess = try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }

        if (!pingSuccess) {
            _accessStatus.value = AccessStatus.SERVICE_NOT_RUNNING
            return
        }

        val permissionGranted = try {
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Throwable) {
            false
        }

        _accessStatus.value = if (permissionGranted) {
            AccessStatus.AVAILABLE
        } else {
            AccessStatus.PERMISSION_REQUIRED
        }
    }

    fun requestPermission() {
        if (!Shizuku.pingBinder()) return
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(REQUEST_CODE)
            } else {
                _accessStatus.value = AccessStatus.AVAILABLE
            }
        } catch (e: Exception) {
            _accessStatus.value = AccessStatus.PERMISSION_REQUIRED
        }
    }

    fun getUid(): Int {
        return try {
            if (Shizuku.pingBinder()) Shizuku.getUid() else -1
        } catch (e: Exception) {
            -1
        }
    }

    fun isShizukuInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
