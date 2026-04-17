package com.example.autoclicker.engine

import android.graphics.Bitmap
import android.media.projection.MediaProjection
import com.example.autoclicker.manager.ScreenCaptureManager
import com.example.autoclicker.service.AutoClickerAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 매크로 실행을 관리하는 엔진입니다.
 */
class MacroEngine(
    private val imageMatcher: ImageMatcher,
    private val screenCaptureManager: ScreenCaptureManager,
    private val accessibilityService: AutoClickerAccessibilityService?
) {

    companion object {
        private const val TAG = "MacroEngine"
    }

    private var macroJob: Job? = null
    private var isRunning = false
    private var currentConfiguration: MacroConfiguration? = null
    private var resolutionScaler: ResolutionScaler? = null
    private var macroProgress: MacroProgress? = null

    /**
     * 매크로 설정을 지정합니다.
     */
    fun setMacroConfiguration(config: MacroConfiguration) {
        currentConfiguration = config

        // 해상도 스케일러 생성
        resolutionScaler = ResolutionScaler(
            config.captureWidth,
            config.captureHeight,
            screenCaptureManager.getScreenWidth(),
            screenCaptureManager.getScreenHeight()
        )

        Timber.tag(TAG).d("Macro configuration set: ${config.name}")
    }

    /**
     * 매크로 실행을 시작합니다.
     */
    fun startMacro(
        mediaProjection: MediaProjection,
        onProgress: (MacroProgress) -> Unit
    ) {
        if (isRunning) {
            Timber.tag(TAG).w("Macro is already running")
            return
        }

        if (currentConfiguration == null) {
            Timber.tag(TAG).e("Macro configuration not set")
            return
        }

        try {
            isRunning = true
            macroProgress = MacroProgress(
                macroName = currentConfiguration!!.name,
                isRunning = true,
                totalExecutions = 0,
                successfulMatches = 0,
                failedMatches = 0,
                lastMatchTime = 0L
            )

            // 화면 캡처 시작
            screenCaptureManager.startCapture(mediaProjection) { screenshot ->
                if (isRunning) {
                    processMacroFrame(screenshot, onProgress)
                }
            }

            Timber.tag(TAG).d("Macro started: ${currentConfiguration!!.name}")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error starting macro")
            isRunning = false
        }
    }

    /**
     * 매크로 실행을 중지합니다.
     */
    fun stopMacro() {
        if (!isRunning) {
            return
        }

        try {
            isRunning = false
            macroJob?.cancel()
            screenCaptureManager.stopCapture()

            Timber.tag(TAG).d("Macro stopped: ${currentConfiguration?.name}")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error stopping macro")
        }
    }

    /**
     * 매크로 프레임을 처리합니다.
     */
    private fun processMacroFrame(screenshot: Bitmap, onProgress: (MacroProgress) -> Unit) {
        val config = currentConfiguration ?: return
        val progress = macroProgress ?: return

        try {
            // 이미지 매칭 수행
            val matchResult = imageMatcher.findTemplate(
                screenshot,
                config.targetImage,
                config.threshold.toDouble()
            )

            if (matchResult != null) {
                // 좌표 변환
                val scaler = resolutionScaler
                val (transformedX, transformedY) = if (scaler != null) {
                    scaler.transformCoordinate(matchResult.centerX, matchResult.centerY)
                } else {
                    Pair(matchResult.centerX, matchResult.centerY)
                }

                // 클릭 실행
                if (accessibilityService != null) {
                    when (config.actionType) {
                        ActionType.CLICK -> accessibilityService.performClick(transformedX.toFloat(), transformedY.toFloat())
                        ActionType.LONG_PRESS -> accessibilityService.performLongPress(transformedX.toFloat(), transformedY.toFloat())
                        ActionType.DOUBLE_CLICK -> accessibilityService.performDoubleClick(transformedX.toFloat(), transformedY.toFloat())
                    }
                }

                progress.successfulMatches++
                progress.lastMatchTime = System.currentTimeMillis()

                Timber.tag(TAG).d("Match found and action executed at ($transformedX, $transformedY)")

            } else {
                progress.failedMatches++
            }

            progress.totalExecutions++

            // 진행 상황 콜백
            onProgress(progress.copy())

            // 클릭 간격 대기
            Thread.sleep(config.clickInterval.toLong())

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error processing macro frame")
        }
    }

    /**
     * 매크로 실행 상태를 반환합니다.
     */
    fun isRunning(): Boolean = isRunning

    /**
     * 현재 진행 상황을 반환합니다.
     */
    fun getProgress(): MacroProgress? = macroProgress

    /**
     * 매크로 설정을 나타내는 데이터 클래스입니다.
     */
    data class MacroConfiguration(
        val name: String,
        val targetImage: Bitmap,
        val threshold: Float,
        val clickInterval: Int,
        val maxRepetitions: Int,
        val actionType: ActionType,
        val captureWidth: Int,
        val captureHeight: Int
    )

    /**
     * 매크로 진행 상황을 나타내는 데이터 클래스입니다.
     */
    data class MacroProgress(
        val macroName: String,
        val isRunning: Boolean,
        val totalExecutions: Int,
        val successfulMatches: Int,
        val failedMatches: Int,
        val lastMatchTime: Long
    )

    /**
     * 액션 타입을 나타내는 열거형입니다.
     */
    enum class ActionType {
        CLICK,
        LONG_PRESS,
        DOUBLE_CLICK
    }
}
