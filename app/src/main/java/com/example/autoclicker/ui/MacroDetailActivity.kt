package com.example.autoclicker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.autoclicker.R
import com.example.autoclicker.service.MacroExecutionService
import timber.log.Timber

/**
 * 매크로 상세 액티비티 - 매크로를 편집하고 실행합니다.
 */
class MacroDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MacroDetailActivity"
        const val EXTRA_MACRO_ID = "macro_id"
    }

    private lateinit var ivTargetImage: ImageView
    private lateinit var tvThreshold: TextView
    private lateinit var tvClickInterval: TextView
    private lateinit var tvMaxRepetitions: TextView
    private lateinit var tvExecutionStats: TextView
    private lateinit var btnStart: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvMacroName: TextView

    private var macroId: String? = null
    private var isMacroRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_macro_detail)

        macroId = intent.getStringExtra(EXTRA_MACRO_ID)

        initializeViews()
        setupListeners()
        loadMacroDetails()
    }

    /**
     * 뷰를 초기화합니다.
     */
    private fun initializeViews() {
        ivTargetImage = findViewById(R.id.iv_target_image)
        tvThreshold = findViewById(R.id.tv_threshold)
        tvClickInterval = findViewById(R.id.tv_click_interval)
        tvMaxRepetitions = findViewById(R.id.tv_max_repetitions)
        tvExecutionStats = findViewById(R.id.tv_execution_stats)
        btnStart = findViewById(R.id.btn_start)
        btnEdit = findViewById(R.id.btn_edit)
        btnDelete = findViewById(R.id.btn_delete)
        btnBack = findViewById(R.id.btn_back)
        tvMacroName = findViewById(R.id.tv_macro_name)
    }

    /**
     * 리스너를 설정합니다.
     */
    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnStart.setOnClickListener {
            if (isMacroRunning) {
                stopMacro()
            } else {
                startMacro()
            }
        }

        btnEdit.setOnClickListener {
            // TODO: 매크로 편집 화면으로 이동
            Timber.tag(TAG).d("Edit macro clicked")
        }

        btnDelete.setOnClickListener {
            deleteMacro()
        }
    }

    /**
     * 매크로 상세 정보를 로드합니다.
     */
    private fun loadMacroDetails() {
        try {
            Timber.tag(TAG).d("Loading macro details: $macroId")

            // TODO: 데이터베이스에서 매크로 정보 로드
            // val macro = MacroRepository.getMacroById(macroId)
            // tvMacroName.text = macro.name
            // ivTargetImage.setImageBitmap(macro.targetImage)
            // tvThreshold.text = String.format("%.2f", macro.threshold)
            // tvClickInterval.text = "${macro.clickInterval}ms"
            // tvMaxRepetitions.text = "${macro.maxRepetitions}"

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading macro details")
        }
    }

    /**
     * 매크로를 시작합니다.
     */
    private fun startMacro() {
        try {
            Timber.tag(TAG).d("Starting macro: $macroId")

            val intent = Intent(this, MacroExecutionService::class.java).apply {
                action = MacroExecutionService.ACTION_START_MACRO
                putExtra(MacroExecutionService.EXTRA_MACRO_ID, macroId)
                putExtra(MacroExecutionService.EXTRA_MACRO_NAME, tvMacroName.text.toString())
            }

            startService(intent)

            isMacroRunning = true
            btnStart.text = "중지"

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error starting macro")
        }
    }

    /**
     * 매크로를 중지합니다.
     */
    private fun stopMacro() {
        try {
            Timber.tag(TAG).d("Stopping macro: $macroId")

            val intent = Intent(this, MacroExecutionService::class.java).apply {
                action = MacroExecutionService.ACTION_STOP_MACRO
            }

            startService(intent)

            isMacroRunning = false
            btnStart.text = "시작"

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error stopping macro")
        }
    }

    /**
     * 매크로를 삭제합니다.
     */
    private fun deleteMacro() {
        try {
            Timber.tag(TAG).d("Deleting macro: $macroId")

            // TODO: 데이터베이스에서 매크로 삭제
            // MacroRepository.deleteMacro(macroId)

            finish()

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error deleting macro")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMacroRunning) {
            stopMacro()
        }
    }
}
