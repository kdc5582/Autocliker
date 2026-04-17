package com.example.autoclicker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import timber.log.Timber

/**
 * AccessibilityService를 상속받아 자동 클릭 기능을 구현합니다.
 * 이 서비스는 시스템 수준에서 화면 터치 이벤트를 발생시킬 수 있습니다.
 */
class AutoClickerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoClickerService"
        private var instance: AutoClickerAccessibilityService? = null

        fun getInstance(): AutoClickerAccessibilityService? = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.tag(TAG).d("AccessibilityService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Timber.tag(TAG).d("AccessibilityService destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 접근성 이벤트 처리
    }

    override fun onInterrupt() {
        Timber.tag(TAG).d("AccessibilityService interrupted")
    }

    /**
     * 지정된 좌표에서 클릭 이벤트를 발생시킵니다.
     */
    fun performClick(x: Float, y: Float, duration: Long = 50): Boolean {
        return try {
            val path = Path().apply {
                moveTo(x, y)
            }

            val gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()

            val success = dispatchGesture(gestureDescription, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Timber.tag(TAG).d("Click gesture completed at ($x, $y)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Timber.tag(TAG).w("Click gesture cancelled at ($x, $y)")
                }
            }, null)

            success
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error performing click at ($x, $y)")
            false
        }
    }

    /**
     * 지정된 좌표에서 길게 누르기(Long Press) 이벤트를 발생시킵니다.
     */
    fun performLongPress(x: Float, y: Float, duration: Long = 500): Boolean {
        return try {
            val path = Path().apply {
                moveTo(x, y)
            }

            val gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()

            dispatchGesture(gestureDescription, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Timber.tag(TAG).d("Long press gesture completed at ($x, $y)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Timber.tag(TAG).w("Long press gesture cancelled at ($x, $y)")
                }
            }, null)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error performing long press at ($x, $y)")
            false
        }
    }

    /**
     * 드래그 제스처를 수행합니다.
     */
    fun performDrag(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        duration: Long = 300
    ): Boolean {
        return try {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }

            val gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()

            dispatchGesture(gestureDescription, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Timber.tag(TAG).d("Drag gesture completed from ($startX, $startY) to ($endX, $endY)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Timber.tag(TAG).w("Drag gesture cancelled")
                }
            }, null)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error performing drag")
            false
        }
    }

    /**
     * 더블 클릭 제스처를 수행합니다.
     */
    fun performDoubleClick(x: Float, y: Float, interval: Long = 100): Boolean {
        return try {
            val path = Path().apply {
                moveTo(x, y)
            }

            val gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                .addStroke(GestureDescription.StrokeDescription(path, interval + 50, 50))
                .build()

            dispatchGesture(gestureDescription, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Timber.tag(TAG).d("Double click gesture completed at ($x, $y)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Timber.tag(TAG).w("Double click gesture cancelled at ($x, $y)")
                }
            }, null)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error performing double click at ($x, $y)")
            false
        }
    }
}
