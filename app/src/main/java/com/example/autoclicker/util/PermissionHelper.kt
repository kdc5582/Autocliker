package com.example.autoclicker.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * 권한 확인 및 처리를 담당하는 유틸리티 클래스입니다.
 */
object PermissionHelper {

    private const val TAG = "PermissionHelper"

    /**
     * AccessibilityService가 활성화되어 있는지 확인합니다.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASKS)
            
            enabledServices.any { service ->
                service.id.contains("com.example.autoclicker")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error checking accessibility service")
            false
        }
    }

    /**
     * AccessibilityService 설정 페이지로 이동합니다.
     */
    fun navigateToAccessibilitySettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activity.startActivity(intent)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error navigating to accessibility settings")
        }
    }

    /**
     * 배터리 최적화에서 제외되어 있는지 확인합니다.
     */
    fun isBatteryOptimizationExempted(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error checking battery optimization exemption")
            false
        }
    }

    /**
     * 배터리 최적화 제외 설정 페이지로 이동합니다.
     */
    fun requestBatteryOptimizationExemption(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error requesting battery optimization exemption")
        }
    }

    /**
     * 오버레이 권한이 있는지 확인합니다.
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * 오버레이 권한 설정 페이지로 이동합니다.
     */
    fun navigateToOverlaySettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = android.net.Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error navigating to overlay settings")
        }
    }

    /**
     * 카메라 권한이 있는지 확인합니다.
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 저장소 읽기 권한이 있는지 확인합니다.
     */
    fun hasReadStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 저장소 쓰기 권한이 있는지 확인합니다.
     */
    fun hasWriteStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 필요한 모든 권한이 있는지 확인합니다.
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return hasCameraPermission(context) &&
                hasReadStoragePermission(context) &&
                hasWriteStoragePermission(context) &&
                isAccessibilityServiceEnabled(context) &&
                canDrawOverlays(context) &&
                isBatteryOptimizationExempted(context)
    }

    /**
     * 권한 상태를 확인하고 로그합니다.
     */
    fun checkAllPermissions(context: Context) {
        Timber.tag(TAG).d("=== Permission Status ===")
        Timber.tag(TAG).d("Camera: ${hasCameraPermission(context)}")
        Timber.tag(TAG).d("Read Storage: ${hasReadStoragePermission(context)}")
        Timber.tag(TAG).d("Write Storage: ${hasWriteStoragePermission(context)}")
        Timber.tag(TAG).d("Accessibility Service: ${isAccessibilityServiceEnabled(context)}")
        Timber.tag(TAG).d("Overlay: ${canDrawOverlays(context)}")
        Timber.tag(TAG).d("Battery Optimization Exempted: ${isBatteryOptimizationExempted(context)}")
        Timber.tag(TAG).d("========================")
    }
}
