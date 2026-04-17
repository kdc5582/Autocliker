package com.example.autoclicker.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import timber.log.Timber
import java.nio.ByteBuffer

/**
 * MediaProjection을 사용하여 화면을 캡처하는 매니저입니다.
 */
class ScreenCaptureManager(private val context: Context) {

    companion object {
        private const val TAG = "ScreenCaptureManager"
    }

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0
    private var captureCallback: ((Bitmap) -> Unit)? = null

    /**
     * 화면 캡처를 시작합니다.
     */
    fun startCapture(
        mediaProjection: MediaProjection,
        callback: (Bitmap) -> Unit
    ): Boolean {
        return try {
            this.mediaProjection = mediaProjection
            this.captureCallback = callback

            // 화면 크기 및 밀도 가져오기
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getRealMetrics(metrics)

            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
            screenDensity = metrics.densityDpi

            Timber.tag(TAG).d("Screen dimensions: $screenWidth x $screenHeight, density: $screenDensity")

            // ImageReader 생성
            imageReader = ImageReader.newInstance(
                screenWidth,
                screenHeight,
                ImageFormat.RGB_565,
                2
            )

            imageReader?.setOnImageAvailableListener({ reader ->
                try {
                    val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                    val bitmap = imageToBitmap(image)
                    image.close()

                    if (bitmap != null) {
                        captureCallback?.invoke(bitmap)
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error processing image")
                }
            }, Handler(Looper.getMainLooper()))

            // VirtualDisplay 생성
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                android.media.projection.MediaProjection.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )

            Timber.tag(TAG).d("Screen capture started")
            true

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error starting screen capture")
            false
        }
    }

    /**
     * 화면 캡처를 중지합니다.
     */
    fun stopCapture() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null

            imageReader?.close()
            imageReader = null

            mediaProjection?.stop()
            mediaProjection = null

            captureCallback = null

            Timber.tag(TAG).d("Screen capture stopped")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error stopping screen capture")
        }
    }

    /**
     * Image를 Bitmap으로 변환합니다.
     */
    private fun imageToBitmap(image: Image): Bitmap? {
        return try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowPadding = planes[0].rowPadding
            val rowStride = planes[0].rowStride

            val width = image.width
            val height = image.height

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val pixelArray = IntArray(width * height)

            buffer.rewind()
            for (y in 0 until height) {
                var pixelIndex = y * width
                val lineOffset = y * rowStride
                for (x in 0 until width) {
                    val bufferIndex = lineOffset + x * pixelStride
                    val rgb565 = (buffer.get(bufferIndex).toInt() and 0xFF) or
                            ((buffer.get(bufferIndex + 1).toInt() and 0xFF) shl 8)
                    pixelArray[pixelIndex] = convertRGB565ToARGB8888(rgb565)
                    pixelIndex++
                }
            }

            bitmap.setPixels(pixelArray, 0, width, 0, 0, width, height)
            bitmap

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error converting image to bitmap")
            null
        }
    }

    /**
     * RGB565를 ARGB8888로 변환합니다.
     */
    private fun convertRGB565ToARGB8888(rgb565: Int): Int {
        val r = (rgb565 and 0xF800) shr 11
        val g = (rgb565 and 0x07E0) shr 5
        val b = rgb565 and 0x001F

        val r8 = (r shl 3) or (r shr 2)
        val g8 = (g shl 2) or (g shr 4)
        val b8 = (b shl 3) or (b shr 2)

        return (0xFF shl 24) or (r8 shl 16) or (g8 shl 8) or b8
    }

    /**
     * 화면 너비를 반환합니다.
     */
    fun getScreenWidth(): Int = screenWidth

    /**
     * 화면 높이를 반환합니다.
     */
    fun getScreenHeight(): Int = screenHeight

    /**
     * 화면 밀도를 반환합니다.
     */
    fun getScreenDensity(): Int = screenDensity

    /**
     * 캡처 중인지 여부를 반환합니다.
     */
    fun isCapturing(): Boolean = virtualDisplay != null
}
