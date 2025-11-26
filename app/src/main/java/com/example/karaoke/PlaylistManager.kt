package com.example.karaoke

import android.os.Environment
import java.io.File

class PlaylistManager {

    private val supportedExtensions = listOf("mpg", "mpeg", "dat", "vob", "ts", "mp4", "mkv")

    fun scanForKaraokeFiles(): List<File> {
        val karaokeFiles = mutableListOf<File>()
        
        // Scan internal storage (Movies folder)
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        scanDirectory(moviesDir, karaokeFiles)

        // You can add more paths here, e.g., external SD card paths if known
        // val sdCardPath = File("/storage/XXXX-XXXX/")
        // scanDirectory(sdCardPath, karaokeFiles)

        return karaokeFiles
    }

    private fun scanDirectory(directory: File, fileList: MutableList<File>) {
        if (!directory.exists() || !directory.isDirectory) return

        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                scanDirectory(file, fileList)
            } else {
                if (supportedExtensions.any { file.extension.equals(it, ignoreCase = true) }) {
                    fileList.add(file)
                }
            }
        }
    }
}
