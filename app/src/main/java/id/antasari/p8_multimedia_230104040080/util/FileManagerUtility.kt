package id.antasari.p8_multimedia_230104040080.util

import android.content.Context
import android.media.MediaMetadataRetriever
import java.io.File
import kotlin.math.roundToInt

// Data class untuk Audio (VideoFileData biasanya dipisah di file sendiri sesuai langkah sebelumnya,
// tapi jika mau digabung di sini juga boleh. Asumsi: VideoFileData ada di file terpisah util/VideoFileData.kt)
data class AudioFileData(
    val file: File,
    val durationMs: Long,
    val sizeText: String
)

object FileManagerUtility {

    // ================= AUDIO =================

    // Ambil semua file .mp4 yang diawali "audio_"
    fun getAllAudioFiles(context: Context): List<File> {
        val dir = context.filesDir
        return dir.listFiles()
            ?.filter { it.extension.lowercase() == "mp4" && it.name.startsWith("audio_") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    // Ambil durasi audio
    fun getAudioDuration(context: Context, file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    // ================= VIDEO =================

    // Ambil semua file .mp4 yang diawali "video_"
    fun getAllVideoFiles(context: Context): List<File> {
        val dir = context.filesDir
        return dir.listFiles()
            ?.filter { it.extension.lowercase() == "mp4" && it.name.startsWith("video_") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    // Ambil durasi video
    fun getVideoDuration(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    // ================= COMMON / UMUM =================

    // Format ukuran file (KB/MB)
    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024f
        return if (kb > 1024) {
            "${(kb / 1024).roundToInt()} MB"
        } else {
            "${kb.roundToInt()} KB"
        }
    }

    // Rename file
    fun renameFile(oldFile: File, newName: String): Boolean {
        val newFile = File(oldFile.parent, newName)
        // Cek jika file baru belum ada, baru direname
        return if (!newFile.exists()) oldFile.renameTo(newFile) else false
    }

    // Delete file
    fun deleteFile(file: File): Boolean {
        return file.delete()
    }
}