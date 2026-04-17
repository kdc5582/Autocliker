package com.example.autoclicker.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.autoclicker.R
import com.google.android.material.slider.Slider
import timber.log.Timber

/**
 * 매크로 등록 액티비티 - 새로운 매크로를 등록합니다.
 */
class MacroRegistrationActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MacroRegistrationActivity"
        private const val REQUEST_IMAGE_PICK = 1001
    }

    private lateinit var etMacroName: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var ivTargetImagePreview: ImageView
    private lateinit var sliderThreshold: Slider
    private lateinit var tvThresholdValue: TextView
    private lateinit var etClickInterval: EditText
    private lateinit var etMaxRepetitions: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnBack: ImageButton

    private var selectedImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_macro_registration)

        initializeViews()
        setupListeners()
    }

    /**
     * 뷰를 초기화합니다.
     */
    private fun initializeViews() {
        etMacroName = findViewById(R.id.et_macro_name)
        btnSelectImage = findViewById(R.id.btn_select_image)
        ivTargetImagePreview = findViewById(R.id.iv_target_image_preview)
        sliderThreshold = findViewById(R.id.slider_threshold)
        tvThresholdValue = findViewById(R.id.tv_threshold_value)
        etClickInterval = findViewById(R.id.et_click_interval)
        etMaxRepetitions = findViewById(R.id.et_max_repetitions)
        btnSave = findViewById(R.id.btn_save)
        btnCancel = findViewById(R.id.btn_cancel)
        btnBack = findViewById(R.id.btn_back)

        // 기본값 설정
        sliderThreshold.value = 0.8f
        etClickInterval.setText("500")
        etMaxRepetitions.setText("10")
    }

    /**
     * 리스너를 설정합니다.
     */
    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        sliderThreshold.addOnChangeListener { _, value, _ ->
            tvThresholdValue.text = String.format("%.2f", value)
        }

        btnSave.setOnClickListener {
            saveMacro()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    /**
     * 이미지 선택기를 엽니다.
     */
    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
            Timber.tag(TAG).d("Image picker opened")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error opening image picker")
        }
    }

    /**
     * 매크로를 저장합니다.
     */
    private fun saveMacro() {
        try {
            val macroName = etMacroName.text.toString().trim()
            val threshold = sliderThreshold.value
            val clickInterval = etClickInterval.text.toString().toIntOrNull() ?: 500
            val maxRepetitions = etMaxRepetitions.text.toString().toIntOrNull() ?: 10

            // 유효성 검사
            if (macroName.isEmpty()) {
                Timber.tag(TAG).w("Macro name is empty")
                etMacroName.error = "매크로 이름을 입력하세요"
                return
            }

            if (selectedImageBitmap == null) {
                Timber.tag(TAG).w("Target image not selected")
                btnSelectImage.error = "이미지를 선택하세요"
                return
            }

            Timber.tag(TAG).d("Saving macro: $macroName (threshold: $threshold, interval: $clickInterval, repetitions: $maxRepetitions)")

            // TODO: 데이터베이스에 매크로 저장
            // val macro = Macro(
            //     name = macroName,
            //     targetImage = selectedImageBitmap,
            //     threshold = threshold,
            //     clickInterval = clickInterval,
            //     maxRepetitions = maxRepetitions
            // )
            // MacroRepository.saveMacro(macro)

            finish()

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error saving macro")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            try {
                val imageUri = data?.data ?: return
                // TODO: URI에서 Bitmap 로드
                // selectedImageBitmap = loadBitmapFromUri(imageUri)
                // ivTargetImagePreview.setImageBitmap(selectedImageBitmap)

                Timber.tag(TAG).d("Image selected: $imageUri")

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error loading image")
            }
        }
    }
}
