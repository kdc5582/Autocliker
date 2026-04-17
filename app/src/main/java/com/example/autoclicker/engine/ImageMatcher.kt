package com.example.autoclicker.engine

import android.graphics.Bitmap
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.core.MinMaxLocResult
import timber.log.Timber

/**
 * OpenCV를 사용하여 이미지 매칭을 수행하는 클래스입니다.
 */
class ImageMatcher {

    companion object {
        private const val TAG = "ImageMatcher"
    }

    /**
     * 템플릿 이미지를 스크린샷에서 찾습니다.
     */
    fun findTemplate(
        screenshot: Bitmap,
        template: Bitmap,
        threshold: Double = 0.8
    ): MatchResult? {
        return try {
            val screenshotMat = bitmapToMat(screenshot)
            val templateMat = bitmapToMat(template)

            // 그레이스케일로 변환
            val screenshotGray = Mat()
            val templateGray = Mat()
            Imgproc.cvtColor(screenshotMat, screenshotGray, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.cvtColor(templateMat, templateGray, Imgproc.COLOR_RGBA2GRAY)

            // 매칭 수행
            val result = Mat()
            Imgproc.matchTemplate(screenshotGray, templateGray, result, Imgproc.TM_CCOEFF_NORMED)

            // 최대값 찾기
            val minMaxLoc = Core.minMaxLoc(result)

            screenshotMat.release()
            templateMat.release()
            screenshotGray.release()
            templateGray.release()
            result.release()

            val confidence = minMaxLoc.maxVal
            if (confidence >= threshold) {
                val centerX = (minMaxLoc.maxLoc.x + templateMat.width() / 2).toInt()
                val centerY = (minMaxLoc.maxLoc.y + templateMat.height() / 2).toInt()

                Timber.tag(TAG).d("Template found at ($centerX, $centerY) with confidence: $confidence")

                MatchResult(
                    centerX = centerX,
                    centerY = centerY,
                    confidence = confidence,
                    scale = 1.0f
                )
            } else {
                Timber.tag(TAG).d("Template not found. Max confidence: $confidence (threshold: $threshold)")
                null
            }

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error finding template")
            null
        }
    }

    /**
     * 다중 스케일에서 템플릿을 찾습니다.
     */
    fun findTemplateMultiScale(
        screenshot: Bitmap,
        template: Bitmap,
        threshold: Double = 0.8,
        scaleRange: FloatArray = floatArrayOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f)
    ): MatchResult? {
        return try {
            var bestResult: MatchResult? = null
            var bestConfidence = threshold

            val screenshotMat = bitmapToMat(screenshot)
            val templateMat = bitmapToMat(template)

            // 그레이스케일로 변환
            val screenshotGray = Mat()
            Imgproc.cvtColor(screenshotMat, screenshotGray, Imgproc.COLOR_RGBA2GRAY)

            for (scale in scaleRange) {
                val scaledTemplate = Mat()
                val newWidth = (templateMat.width() * scale).toInt()
                val newHeight = (templateMat.height() * scale).toInt()

                if (newWidth > screenshotMat.width() || newHeight > screenshotMat.height()) {
                    continue
                }

                Imgproc.resize(templateMat, scaledTemplate, org.opencv.core.Size(newWidth.toDouble(), newHeight.toDouble()))

                val templateGray = Mat()
                Imgproc.cvtColor(scaledTemplate, templateGray, Imgproc.COLOR_RGBA2GRAY)

                val result = Mat()
                Imgproc.matchTemplate(screenshotGray, templateGray, result, Imgproc.TM_CCOEFF_NORMED)

                val minMaxLoc = Core.minMaxLoc(result)
                val confidence = minMaxLoc.maxVal

                if (confidence > bestConfidence) {
                    bestConfidence = confidence
                    val centerX = (minMaxLoc.maxLoc.x + scaledTemplate.width() / 2).toInt()
                    val centerY = (minMaxLoc.maxLoc.y + scaledTemplate.height() / 2).toInt()

                    bestResult = MatchResult(
                        centerX = centerX,
                        centerY = centerY,
                        confidence = confidence,
                        scale = scale
                    )
                }

                scaledTemplate.release()
                templateGray.release()
                result.release()
            }

            screenshotMat.release()
            templateMat.release()
            screenshotGray.release()

            if (bestResult != null) {
                Timber.tag(TAG).d("Template found at (${bestResult.centerX}, ${bestResult.centerY}) with scale: ${bestResult.scale} and confidence: ${bestResult.confidence}")
            } else {
                Timber.tag(TAG).d("Template not found in any scale")
            }

            bestResult

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error finding template with multi-scale")
            null
        }
    }

    /**
     * Bitmap을 Mat으로 변환합니다.
     */
    private fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        val temp = org.opencv.core.Mat()
        org.opencv.imgcodecs.Imgcodecs.imencode(".png", temp, temp)
        
        val pixelArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixelArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val matData = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        val buffer = matData.put(0, 0, pixelArray.map { it.toByte() }.toByteArray())

        return matData
    }

    /**
     * 매칭 결과를 나타내는 데이터 클래스입니다.
     */
    data class MatchResult(
        val centerX: Int,
        val centerY: Int,
        val confidence: Double,
        val scale: Float
    )
}
