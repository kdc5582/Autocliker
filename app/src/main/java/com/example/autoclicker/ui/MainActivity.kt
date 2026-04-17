package com.example.autoclicker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autoclicker.R
import com.example.autoclicker.util.PermissionHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import timber.log.Timber

/**
 * 홈 화면 액티비티 - 등록된 매크로 목록을 표시합니다.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var rvMacroList: RecyclerView
    private lateinit var fabAddMacro: FloatingActionButton
    private lateinit var btnSettings: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Timber 초기화
        if (!BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initializeViews()
        checkPermissions()
        loadMacroList()
    }

    /**
     * 뷰를 초기화합니다.
     */
    private fun initializeViews() {
        rvMacroList = findViewById(R.id.rv_macro_list)
        fabAddMacro = findViewById(R.id.fab_add_macro)
        btnSettings = findViewById(R.id.btn_settings)

        // RecyclerView 설정
        rvMacroList.layoutManager = LinearLayoutManager(this)

        // FAB 클릭 리스너
        fabAddMacro.setOnClickListener {
            Timber.tag(TAG).d("Add macro button clicked")
            startActivity(Intent(this, MacroRegistrationActivity::class.java))
        }

        // 설정 버튼 클릭 리스너
        btnSettings.setOnClickListener {
            Timber.tag(TAG).d("Settings button clicked")
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    /**
     * 권한을 확인합니다.
     */
    private fun checkPermissions() {
        Timber.tag(TAG).d("Checking permissions...")
        PermissionHelper.checkAllPermissions(this)

        if (!PermissionHelper.hasAllRequiredPermissions(this)) {
            Timber.tag(TAG).w("Some required permissions are missing")
        }
    }

    /**
     * 매크로 목록을 로드합니다.
     */
    private fun loadMacroList() {
        try {
            Timber.tag(TAG).d("Loading macro list...")
            
            // TODO: 데이터베이스에서 매크로 목록 로드
            // val macros = MacroRepository.getAllMacros()
            // rvMacroList.adapter = MacroAdapter(macros)

            Timber.tag(TAG).d("Macro list loaded")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading macro list")
        }
    }

    override fun onResume() {
        super.onResume()
        loadMacroList()
    }
}
