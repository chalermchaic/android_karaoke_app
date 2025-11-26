package com.example.karaoke

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
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
        val btnTestFiles = findViewById<Button>(R.id.btn_test_files)
        val btnAudioChannel = findViewById<Button>(R.id.btn_audio_channel)

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

        btnTestFiles.setOnClickListener {
            val intent = Intent(this, FileTestActivity::class.java)
            startActivity(intent)
        }

        btnAudioChannel.setOnClickListener {
            showAudioChannelDialog(btnAudioChannel)
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

    private fun showAudioChannelDialog(btnAudioChannel: Button) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_audio_channel, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rg_channel_mode)

        // Set current selection
        val currentMode = karaokePlayer.getCurrentChannelMode()
        when (currentMode) {
            KaraokePlayer.AudioChannelMode.STEREO ->
                radioGroup.check(R.id.rb_stereo)
            KaraokePlayer.AudioChannelMode.LEFT_ONLY ->
                radioGroup.check(R.id.rb_left_only)
            KaraokePlayer.AudioChannelMode.RIGHT_ONLY ->
                radioGroup.check(R.id.rb_right_only)
            KaraokePlayer.AudioChannelMode.MONO_MIX ->
                radioGroup.check(R.id.rb_mono_mix)
            KaraokePlayer.AudioChannelMode.VOCAL_REMOVER ->
                radioGroup.check(R.id.rb_vocal_remove)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Change mode immediately when radio button is clicked
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedMode = when (checkedId) {
                R.id.rb_stereo -> KaraokePlayer.AudioChannelMode.STEREO
                R.id.rb_left_only -> KaraokePlayer.AudioChannelMode.LEFT_ONLY
                R.id.rb_right_only -> KaraokePlayer.AudioChannelMode.RIGHT_ONLY
                R.id.rb_mono_mix -> KaraokePlayer.AudioChannelMode.MONO_MIX
                R.id.rb_vocal_remove -> KaraokePlayer.AudioChannelMode.VOCAL_REMOVER
                else -> KaraokePlayer.AudioChannelMode.STEREO
            }

            karaokePlayer.setAudioChannelMode(selectedMode)
            btnAudioChannel.text = "Audio: ${karaokePlayer.getChannelModeDescription()}"

            Toast.makeText(
                this,
                "${karaokePlayer.getChannelModeDescription()}",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Close button
        dialogView.findViewById<Button>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
