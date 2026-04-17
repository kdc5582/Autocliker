package com.example.autoclicker.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.example.autoclicker.R
import timber.log.Timber

/**
 * 오버레이 플로팅 버튼을 표시하는 서비스입니다.
 * 앱이 백그라운드에 있어도 사용자가 매크로를 제어할 수 있습니다.
 */
class FloatingButtonService : Service() {

    companion object {
        private const val TAG = "FloatingButtonService"
    }

    private var windowManager: WindowManager? = null
    private var floatingView: FrameLayout? = null
    private var isShowing = false

    private var lastX = 0f
    private var lastY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Timber.tag(TAG).d("FloatingButtonService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatingButton()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        hideFloatingButton()
        Timber.tag(TAG).d("FloatingButtonService destroyed")
    }

    /**
     * 플로팅 버튼을 표시합니다.
     */
    private fun showFloatingButton() {
        if (isShowing) {
            return
        }

        try {
            floatingView = FrameLayout(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }

            // 버튼 뷰 생성
            val buttonView = LayoutInflater.from(this).inflate(R.layout.floating_button_layout, null)
            floatingView?.addView(buttonView)

            val params = WindowManager.LayoutParams().apply {
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                width = 120
                height = 120
                x = 0
                y = 0
                gravity = Gravity.TOP or Gravity.START
            }

            windowManager?.addView(floatingView, params)
            isShowing = true

            // 버튼 클릭 리스너 설정
            setupButtonListeners(buttonView)

            Timber.tag(TAG).d("Floating button shown")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error showing floating button")
        }
    }

    /**
     * 플로팅 버튼을 숨깁니다.
     */
    private fun hideFloatingButton() {
        if (!isShowing || floatingView == null) {
            return
        }

        try {
            windowManager?.removeView(floatingView)
            floatingView = null
            isShowing = false

            Timber.tag(TAG).d("Floating button hidden")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error hiding floating button")
        }
    }

    /**
     * 버튼 리스너를 설정합니다.
     */
    private fun setupButtonListeners(view: View) {
        val startButton = view.findViewById<Button>(R.id.btn_floating_start)
        val stopButton = view.findViewById<Button>(R.id.btn_floating_stop)
        val statusText = view.findViewById<TextView>(R.id.tv_floating_status)

        startButton?.setOnClickListener {
            Timber.tag(TAG).d("Start button clicked")
            statusText?.text = "시작됨"
            // 매크로 시작 이벤트 처리
        }

        stopButton?.setOnClickListener {
            Timber.tag(TAG).d("Stop button clicked")
            statusText?.text = "중지됨"
            // 매크로 중지 이벤트 처리
        }

        // 드래그 기능 추가
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - lastTouchX
                    val dy = event.rawY - lastTouchY

                    val params = floatingView?.layoutParams as? WindowManager.LayoutParams
                    if (params != null) {
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                    }

                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    true
                }
                else -> false
            }
        }
    }
}
