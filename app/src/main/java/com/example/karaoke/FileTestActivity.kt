package com.example.karaoke

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class FileTestActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var tvStatus: TextView
    private val fileStatusList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_test)

        listView = findViewById(R.id.lv_file_status)
        tvStatus = findViewById(R.id.tv_test_status)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileStatusList)
        listView.adapter = adapter

        // Get files from PlaylistManager
        val playlistManager = PlaylistManager()
        val files = playlistManager.scanForKaraokeFiles()

        if (files.isEmpty()) {
            tvStatus.text = "❌ ไม่พบไฟล์เพลง"
            Toast.makeText(this, "ไม่พบไฟล์คาราโอเกะ", Toast.LENGTH_LONG).show()
            return
        }

        tvStatus.text = "กำลังทดสอบ ${files.size} ไฟล์..."
        testFiles(files)
    }

    private fun testFiles(files: List<File>) {
        var currentIndex = 0
        var successCount = 0
        var failCount = 0

        player = ExoPlayer.Builder(this).build()

        fun testNextFile() {
            if (currentIndex >= files.size) {
                // Testing complete
                tvStatus.text = "✅ ทดสอบเสร็จสิ้น: เล่นได้ $successCount ไฟล์, เล่นไม่ได้ $failCount ไฟล์"
                player?.release()
                player = null
                return
            }

            val file = files[currentIndex]
            val fileName = file.name

            player?.addListener(object : Player.Listener {
                var hasError = false
                var isReady = false

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY && !hasError) {
                        isReady = true
                        // File can be played
                        successCount++
                        fileStatusList.add("✅ $fileName")
                        adapter.notifyDataSetChanged()
                        listView.smoothScrollToPosition(fileStatusList.size - 1)

                        // Move to next file
                        player?.clearMediaItems()
                        player?.clearVideoSurface()
                        removeListener()
                        currentIndex++
                        testNextFile()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    hasError = true
                    failCount++
                    fileStatusList.add("❌ $fileName - ${error.errorCodeName}")
                    adapter.notifyDataSetChanged()
                    listView.smoothScrollToPosition(fileStatusList.size - 1)

                    // Move to next file
                    player?.clearMediaItems()
                    removeListener()
                    currentIndex++
                    testNextFile()
                }

                fun removeListener() {
                    player?.removeListener(this)
                }
            })

            // Load and prepare the file
            val mediaItem = MediaItem.fromUri(file.absolutePath)
            player?.setMediaItem(mediaItem)
            player?.prepare()
        }

        testNextFile()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
