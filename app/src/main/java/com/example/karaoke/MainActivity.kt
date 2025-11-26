package com.example.karaoke

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var karaokePlayer: KaraokePlayer
    private lateinit var playlistManager: PlaylistManager
    private lateinit var tvSongTitle: TextView

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playerView = findViewById<PlayerView>(R.id.player_view)
        tvSongTitle = findViewById(R.id.tv_song_title)
        val btnNext = findViewById<Button>(R.id.btn_next_song)
        val btnScan = findViewById<Button>(R.id.btn_scan_files)
        val btnToggleVocal = findViewById<Button>(R.id.btn_toggle_vocal)

        karaokePlayer = KaraokePlayer(this, playerView)
        playlistManager = PlaylistManager()

        btnNext.setOnClickListener {
            karaokePlayer.playNext()
            updateSongTitle()
        }

        btnScan.setOnClickListener {
            checkPermissionsAndScan()
        }

        btnToggleVocal.setOnClickListener {
            val isVocalOn = karaokePlayer.toggleVocal()
            btnToggleVocal.text = if (isVocalOn) "Vocal: ON" else "Vocal: OFF"
            btnToggleVocal.setBackgroundColor(
                if (isVocalOn)
                    android.graphics.Color.parseColor("#4CAF50")
                else
                    android.graphics.Color.parseColor("#F44336")
            )
            Toast.makeText(this, if (isVocalOn) "เปิดเสียงร้อง" else "ปิดเสียงร้อง", Toast.LENGTH_SHORT).show()
        }

        checkPermissionsAndScan()
    }

    private fun checkPermissionsAndScan() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            scanAndPlay()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    private fun scanAndPlay() {
        val files = playlistManager.scanForKaraokeFiles()
        if (files.isNotEmpty()) {
            karaokePlayer.setPlaylist(files)
            updateSongTitle()
            Toast.makeText(this, "Found ${files.size} songs", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No karaoke files found in Movies directory", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateSongTitle() {
        tvSongTitle.text = karaokePlayer.getCurrentSongTitle()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanAndPlay()
            } else {
                Toast.makeText(this, "Permission denied. Cannot play files.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        karaokePlayer.initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        karaokePlayer.releasePlayer()
    }
}
