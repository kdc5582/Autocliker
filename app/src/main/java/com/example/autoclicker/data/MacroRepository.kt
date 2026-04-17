package com.example.autoclicker.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * 매크로 데이터를 관리하는 저장소입니다.
 */
class MacroRepository(private val context: Context) {

    companion object {
        private const val TAG = "MacroRepository"
        private const val MACRO_DIR = "macros"
        private const val MACRO_DATA_FILE = "macros.json"
        private const val IMAGE_DIR = "macro_images"

        private var instance: MacroRepository? = null

        fun getInstance(context: Context): MacroRepository {
            return instance ?: MacroRepository(context).also { instance = it }
        }
    }

    private val macroDir = File(context.filesDir, MACRO_DIR)
    private val imageDir = File(context.filesDir, IMAGE_DIR)
    private val dataFile = File(macroDir, MACRO_DATA_FILE)

    init {
        macroDir.mkdirs()
        imageDir.mkdirs()
    }

    /**
     * 모든 매크로를 반환합니다.
     */
    suspend fun getAllMacros(): List<Macro> = withContext(Dispatchers.IO) {
        return@withContext try {
            val macroFiles = macroDir.listFiles { file ->
                file.isFile && file.name.endsWith(".macro")
            } ?: emptyArray()

            macroFiles.mapNotNull { file ->
                try {
                    loadMacroFromFile(file)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error loading macro from file: ${file.name}")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error getting all macros")
            emptyList()
        }
    }

    /**
     * ID로 매크로를 조회합니다.
     */
    suspend fun getMacroById(id: String): Macro? = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(macroDir, "$id.macro")
            if (file.exists()) {
                loadMacroFromFile(file)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error getting macro by id: $id")
            null
        }
    }

    /**
     * 매크로를 저장합니다.
     */
    suspend fun saveMacro(macro: Macro): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val macroId = macro.id.ifEmpty { UUID.randomUUID().toString() }
            val file = File(macroDir, "$macroId.macro")

            // 이미지 저장
            val imagePath = saveImage(macro.targetImage, macroId)

            // 매크로 데이터 저장 (JSON 형식)
            val macroData = MacroData(
                id = macroId,
                name = macro.name,
                imagePath = imagePath,
                threshold = macro.threshold,
                clickInterval = macro.clickInterval,
                maxRepetitions = macro.maxRepetitions,
                actionType = macro.actionType,
                createdAt = macro.createdAt,
                updatedAt = System.currentTimeMillis(),
                isActive = macro.isActive,
                totalExecutions = macro.totalExecutions,
                successfulMatches = macro.successfulMatches,
                failedMatches = macro.failedMatches,
                lastExecutionTime = macro.lastExecutionTime
            )

            val json = macroDataToJson(macroData)
            file.writeText(json)

            Timber.tag(TAG).d("Macro saved: $macroId")
            true

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error saving macro")
            false
        }
    }

    /**
     * 매크로를 삭제합니다.
     */
    suspend fun deleteMacro(id: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(macroDir, "$id.macro")
            val imageFile = File(imageDir, "$id.png")

            val fileDeleted = file.delete()
            val imageDeleted = imageFile.delete()

            if (fileDeleted || imageDeleted) {
                Timber.tag(TAG).d("Macro deleted: $id")
                true
            } else {
                Timber.tag(TAG).w("Failed to delete macro: $id")
                false
            }

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error deleting macro")
            false
        }
    }

    /**
     * 매크로 통계를 업데이트합니다.
     */
    suspend fun updateMacroStatistics(
        id: String,
        totalExecutions: Int,
        successfulMatches: Int,
        failedMatches: Int
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val macro = getMacroById(id) ?: return@withContext false

            val updatedMacro = macro.copy(
                totalExecutions = totalExecutions,
                successfulMatches = successfulMatches,
                failedMatches = failedMatches,
                lastExecutionTime = System.currentTimeMillis()
            )

            saveMacro(updatedMacro)

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error updating macro statistics")
            false
        }
    }

    /**
     * 이미지를 저장합니다.
     */
    private fun saveImage(bitmap: Bitmap, macroId: String): String {
        val imageFile = File(imageDir, "$macroId.png")
        FileOutputStream(imageFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        return imageFile.absolutePath
    }

    /**
     * 파일에서 매크로를 로드합니다.
     */
    private fun loadMacroFromFile(file: File): Macro? {
        return try {
            val json = file.readText()
            val macroData = jsonToMacroData(json) ?: return null

            val imageBitmap = BitmapFactory.decodeFile(macroData.imagePath)
            if (imageBitmap == null) {
                Timber.tag(TAG).w("Failed to load image: ${macroData.imagePath}")
                return null
            }

            Macro(
                id = macroData.id,
                name = macroData.name,
                targetImage = imageBitmap,
                threshold = macroData.threshold,
                clickInterval = macroData.clickInterval,
                maxRepetitions = macroData.maxRepetitions,
                actionType = macroData.actionType,
                createdAt = macroData.createdAt,
                updatedAt = macroData.updatedAt,
                isActive = macroData.isActive,
                totalExecutions = macroData.totalExecutions,
                successfulMatches = macroData.successfulMatches,
                failedMatches = macroData.failedMatches,
                lastExecutionTime = macroData.lastExecutionTime
            )

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading macro from file: ${file.name}")
            null
        }
    }

    /**
     * MacroData를 JSON으로 변환합니다.
     */
    private fun macroDataToJson(data: MacroData): String {
        return """
            {
                "id": "${data.id}",
                "name": "${data.name}",
                "imagePath": "${data.imagePath}",
                "threshold": ${data.threshold},
                "clickInterval": ${data.clickInterval},
                "maxRepetitions": ${data.maxRepetitions},
                "actionType": "${data.actionType}",
                "createdAt": ${data.createdAt},
                "updatedAt": ${data.updatedAt},
                "isActive": ${data.isActive},
                "totalExecutions": ${data.totalExecutions},
                "successfulMatches": ${data.successfulMatches},
                "failedMatches": ${data.failedMatches},
                "lastExecutionTime": ${data.lastExecutionTime}
            }
        """.trimIndent()
    }

    /**
     * JSON을 MacroData로 변환합니다.
     */
    private fun jsonToMacroData(json: String): MacroData? {
        return try {
            // 간단한 JSON 파싱 (실제로는 Gson이나 Moshi 사용 권장)
            val regex = """"([^"]+)"\s*:\s*([^,}]+)""".toRegex()
            val map = mutableMapOf<String, String>()

            regex.findAll(json).forEach { match ->
                val key = match.groupValues[1]
                val value = match.groupValues[2].trim().removeSurrounding("\"")
                map[key] = value
            }

            MacroData(
                id = map["id"] ?: return null,
                name = map["name"] ?: return null,
                imagePath = map["imagePath"] ?: return null,
                threshold = map["threshold"]?.toFloatOrNull() ?: 0.8f,
                clickInterval = map["clickInterval"]?.toIntOrNull() ?: 500,
                maxRepetitions = map["maxRepetitions"]?.toIntOrNull() ?: 10,
                actionType = map["actionType"] ?: "CLICK",
                createdAt = map["createdAt"]?.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = map["updatedAt"]?.toLongOrNull() ?: System.currentTimeMillis(),
                isActive = map["isActive"]?.toBoolean() ?: true,
                totalExecutions = map["totalExecutions"]?.toIntOrNull() ?: 0,
                successfulMatches = map["successfulMatches"]?.toIntOrNull() ?: 0,
                failedMatches = map["failedMatches"]?.toIntOrNull() ?: 0,
                lastExecutionTime = map["lastExecutionTime"]?.toLongOrNull() ?: 0L
            )

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error parsing JSON to MacroData")
            null
        }
    }

    /**
     * 매크로 데이터를 나타내는 내부 클래스입니다.
     */
    private data class MacroData(
        val id: String,
        val name: String,
        val imagePath: String,
        val threshold: Float,
        val clickInterval: Int,
        val maxRepetitions: Int,
        val actionType: String,
        val createdAt: Long,
        val updatedAt: Long,
        val isActive: Boolean,
        val totalExecutions: Int,
        val successfulMatches: Int,
        val failedMatches: Int,
        val lastExecutionTime: Long
    )
}
