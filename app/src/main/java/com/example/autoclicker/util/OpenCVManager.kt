package com.example.autoclicker.util

import android.content.Context
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import timber.log.Timber

/**
 * OpenCV 라이브러리를 초기화하고 관리하는 클래스입니다.
 */
object OpenCVManager {

    private const val TAG = "OpenCVManager"
    private var isInitialized = false

    /**
     * OpenCV를 초기화합니다.
     */
    fun initialize(context: Context): Boolean {
        return try {
            if (isInitialized) {
                return true
            }

            // OpenCV 라이브러리 로드
            System.loadLibrary("opencv_java4")
            
            Timber.tag(TAG).d("OpenCV version: ${Core.VERSION}")
            Timber.tag(TAG).d("OpenCV Core.NATIVE_LIBRARY_NAME: ${Core.NATIVE_LIBRARY_NAME}")

            isInitialized = true
            Timber.tag(TAG).d("OpenCV initialized successfully")
            true

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error initializing OpenCV")
            false
        }
    }

    /**
     * OpenCV 초기화 상태를 반환합니다.
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * 이미지를 그레이스케일로 변환합니다.
     */
    fun toGrayscale(mat: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        return gray
    }

    /**
     * 이미지를 리사이즈합니다.
     */
    fun resize(mat: Mat, width: Int, height: Int): Mat {
        val resized = Mat()
        val size = org.opencv.core.Size(width.toDouble(), height.toDouble())
        Imgproc.resize(mat, resized, size)
        return resized
    }

    /**
     * 이미지에 가우시안 블러를 적용합니다.
     */
    fun gaussianBlur(mat: Mat, kernelSize: Int = 5): Mat {
        val blurred = Mat()
        val size = org.opencv.core.Size(kernelSize.toDouble(), kernelSize.toDouble())
        Imgproc.GaussianBlur(mat, blurred, size, 0.0)
        return blurred
    }

    /**
     * 이미지 히스토그램을 균등화합니다.
     */
    fun equalizeHistogram(mat: Mat): Mat {
        val equalized = Mat()
        Imgproc.equalizeHist(mat, equalized)
        return equalized
    }

    /**
     * 이미지의 밝기와 대비를 조정합니다.
     */
    fun adjustBrightnessContrast(mat: Mat, brightness: Double = 0.0, contrast: Double = 1.0): Mat {
        val adjusted = Mat()
        mat.convertTo(adjusted, -1, contrast, brightness)
        return adjusted
    }

    /**
     * Canny 엣지 감지를 수행합니다.
     */
    fun cannyEdgeDetection(mat: Mat, threshold1: Double = 100.0, threshold2: Double = 200.0): Mat {
        val edges = Mat()
        Imgproc.Canny(mat, edges, threshold1, threshold2)
        return edges
    }

    /**
     * 이미지에 직사각형을 그립니다.
     */
    fun drawRectangle(mat: Mat, x: Int, y: Int, width: Int, height: Int, color: Scalar = Scalar(0.0, 255.0, 0.0), thickness: Int = 2) {
        val pt1 = org.opencv.core.Point(x.toDouble(), y.toDouble())
        val pt2 = org.opencv.core.Point((x + width).toDouble(), (y + height).toDouble())
        Imgproc.rectangle(mat, pt1, pt2, color, thickness)
    }

    /**
     * 이미지에 원을 그립니다.
     */
    fun drawCircle(mat: Mat, centerX: Int, centerY: Int, radius: Int, color: Scalar = Scalar(0.0, 255.0, 0.0), thickness: Int = 2) {
        val center = org.opencv.core.Point(centerX.toDouble(), centerY.toDouble())
        Imgproc.circle(mat, center, radius, color, thickness)
    }

    /**
     * 이미지에 텍스트를 그립니다.
     */
    fun putText(mat: Mat, text: String, x: Int, y: Int, fontScale: Double = 1.0, color: Scalar = Scalar(0.0, 255.0, 0.0), thickness: Int = 2) {
        val org = org.opencv.core.Point(x.toDouble(), y.toDouble())
        Imgproc.putText(mat, text, org, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, color, thickness)
    }
}
