package com.example.autoclicker.engine

import timber.log.Timber

/**
 * 해상도 비율을 계산하고 좌표를 변환하는 클래스입니다.
 */
class ResolutionScaler(
    private val captureWidth: Int,
    private val captureHeight: Int,
    private val currentWidth: Int,
    private val currentHeight: Int
) {

    companion object {
        private const val TAG = "ResolutionScaler"
    }

    private val scaleX: Float = currentWidth.toFloat() / captureWidth.toFloat()
    private val scaleY: Float = currentHeight.toFloat() / captureHeight.toFloat()

    init {
        Timber.tag(TAG).d("ResolutionScaler initialized")
        Timber.tag(TAG).d("Capture resolution: ${captureWidth}x${captureHeight}")
        Timber.tag(TAG).d("Current resolution: ${currentWidth}x${currentHeight}")
        Timber.tag(TAG).d("Scale: scaleX=$scaleX, scaleY=$scaleY")
    }

    /**
     * 캡처 해상도의 좌표를 현재 해상도의 좌표로 변환합니다.
     */
    fun transformCoordinate(captureX: Int, captureY: Int): Pair<Int, Int> {
        val transformedX = (captureX * scaleX).toInt()
        val transformedY = (captureY * scaleY).toInt()

        // 화면 범위를 벗어나지 않도록 제한
        val clampedX = transformedX.coerceIn(0, currentWidth - 1)
        val clampedY = transformedY.coerceIn(0, currentHeight - 1)

        Timber.tag(TAG).d("Transform coordinate: ($captureX, $captureY) -> ($clampedX, $clampedY)")

        return Pair(clampedX, clampedY)
    }

    /**
     * 여러 좌표를 한 번에 변환합니다.
     */
    fun transformCoordinates(coordinates: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        return coordinates.map { (x, y) -> transformCoordinate(x, y) }
    }

    /**
     * 크기를 변환합니다.
     */
    fun transformSize(captureWidth: Int, captureHeight: Int): Pair<Int, Int> {
        val transformedWidth = (captureWidth * scaleX).toInt()
        val transformedHeight = (captureHeight * scaleY).toInt()

        Timber.tag(TAG).d("Transform size: (${captureWidth}, ${captureHeight}) -> ($transformedWidth, $transformedHeight)")

        return Pair(transformedWidth, transformedHeight)
    }

    /**
     * 스케일이 균일한지 확인합니다 (X와 Y 스케일이 같은지).
     */
    fun isScaleUniform(): Boolean {
        return kotlin.math.abs(scaleX - scaleY) < 0.01f
    }

    /**
     * X 스케일을 반환합니다.
     */
    fun getScaleX(): Float = scaleX

    /**
     * Y 스케일을 반환합니다.
     */
    fun getScaleY(): Float = scaleY

    /**
     * 평균 스케일을 반환합니다.
     */
    fun getAverageScale(): Float = (scaleX + scaleY) / 2f
}
