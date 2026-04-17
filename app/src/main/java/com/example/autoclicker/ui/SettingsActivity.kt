package com.example.autoclicker.ui

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.autoclicker.R
import com.example.autoclicker.util.PermissionHelper
import timber.log.Timber

/**
 * 설정 액티비티 - 권한 설정을 관리합니다.
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SettingsActivity"
    }

    private lateinit var btnBack: ImageButton
    private lateinit var btnAccessibilitySettings: Button
    private lateinit var btnBatteryOptimization: Button
    private lateinit var btnOverlaySettings: Button
    private lateinit var tvAccessibilityStatus: TextView
    private lateinit var tvBatteryOptimizationStatus: TextView
    private lateinit var tvOverlayStatus: TextView
    private lateinit var tvAppVersion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        setupListeners()
        updatePermissionStatus()
    }

    /**
     * 뷰를 초기화합니다.
     */
    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        btnAccessibilitySettings = findViewById(R.id.btn_accessibility_settings)
        btnBatteryOptimization = findViewById(R.id.btn_battery_optimization)
        btnOverlaySettings = findViewById(R.id.btn_overlay_settings)
        tvAccessibilityStatus = findViewById(R.id.tv_accessibility_status)
        tvBatteryOptimizationStatus = findViewById(R.id.tv_battery_optimization_status)
        tvOverlayStatus = findViewById(R.id.tv_overlay_status)
        tvAppVersion = findViewById(R.id.tv_app_version)

        // 앱 버전 설정
        tvAppVersion.text = "1.0.0"
    }

    /**
     * 리스너를 설정합니다.
     */
    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnAccessibilitySettings.setOnClickListener {
            Timber.tag(TAG).d("Accessibility settings button clicked")
            PermissionHelper.navigateToAccessibilitySettings(this)
        }

        btnBatteryOptimization.setOnClickListener {
            Timber.tag(TAG).d("Battery optimization button clicked")
            PermissionHelper.requestBatteryOptimizationExemption(this)
        }

        btnOverlaySettings.setOnClickListener {
            Timber.tag(TAG).d("Overlay settings button clicked")
            PermissionHelper.navigateToOverlaySettings(this)
        }
    }

    /**
     * 권한 상태를 업데이트합니다.
     */
    private fun updatePermissionStatus() {
        try {
            // AccessibilityService 상태
            val isAccessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(this)
            tvAccessibilityStatus.text = if (isAccessibilityEnabled) "활성화" else "비활성화"
            tvAccessibilityStatus.setTextColor(
                if (isAccessibilityEnabled) getColor(R.color.success) else getColor(R.color.error)
            )

            // 배터리 최적화 상태
            val isBatteryOptimizationExempted = PermissionHelper.isBatteryOptimizationExempted(this)
            tvBatteryOptimizationStatus.text = if (isBatteryOptimizationExempted) "제외됨" else "최적화 중"
            tvBatteryOptimizationStatus.setTextColor(
                if (isBatteryOptimizationExempted) getColor(R.color.success) else getColor(R.color.error)
            )

            // 오버레이 권한 상태
            val canDrawOverlays = PermissionHelper.canDrawOverlays(this)
            tvOverlayStatus.text = if (canDrawOverlays) "활성화" else "비활성화"
            tvOverlayStatus.setTextColor(
                if (canDrawOverlays) getColor(R.color.success) else getColor(R.color.error)
            )

            Timber.tag(TAG).d("Permission status updated")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error updating permission status")
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }
}
