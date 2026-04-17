package com.example.autoclicker.data

import android.graphics.Bitmap
import java.io.Serializable

/**
 * 매크로 데이터를 나타내는 클래스입니다.
 */
data class Macro(
    val id: String,
    val name: String,
    val targetImage: Bitmap,
    val threshold: Float,
    val clickInterval: Int,
    val maxRepetitions: Int,
    val actionType: String = "CLICK",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val totalExecutions: Int = 0,
    val successfulMatches: Int = 0,
    val failedMatches: Int = 0,
    val lastExecutionTime: Long = 0L
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    /**
     * 매크로의 성공률을 반환합니다.
     */
    fun getSuccessRate(): Float {
        return if (totalExecutions == 0) 0f else (successfulMatches.toFloat() / totalExecutions) * 100
    }

    /**
     * 매크로의 통계를 반환합니다.
     */
    fun getStatistics(): String {
        return "총 실행: $totalExecutions | 성공: $successfulMatches | 실패: $failedMatches"
    }
}
