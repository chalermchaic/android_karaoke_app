package com.example.karaoke

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

class KaraokePlayer(private val context: Context, private val playerView: PlayerView) {

    enum class AudioChannelMode {
        STEREO,           // ทั้ง 2 ช่อง
        LEFT_ONLY,        // ช่องซ้ายอย่างเดียว
        RIGHT_ONLY,       // ช่องขวาอย่างเดียว
        MONO_MIX,         // ผสมเป็น mono
        VOCAL_REMOVER     // ลดเสียงร้อง
    }

    private var player: ExoPlayer? = null
    private var playlist: List<File> = emptyList()
    private var currentIndex = 0
    private var isVocalOn = true
    private var currentChannelMode = AudioChannelMode.STEREO

    fun initializePlayer() {
        if (player == null) {
            val renderersFactory = DefaultRenderersFactory(context)
                .setEnableDecoderFallback(true)

            player = ExoPlayer.Builder(context, renderersFactory).build()

            playerView.player = player
            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        playNext()
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.widget.Toast.makeText(context, "Error: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                    android.util.Log.e("KaraokePlayer", "Player Error: ", error)
                }
            })
        }
    }

    fun setPlaylist(files: List<File>) {
        playlist = files
        currentIndex = 0
        if (playlist.isNotEmpty()) {
            playCurrent()
        }
    }

    private fun playCurrent() {
        if (playlist.isEmpty()) return
        
        val file = playlist[currentIndex]
        val mediaItem = MediaItem.fromUri(file.absolutePath)
        
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    fun playNext() {
        if (playlist.isEmpty()) return
        
        currentIndex = (currentIndex + 1) % playlist.size
        playCurrent()
    }
    
    fun getCurrentSongTitle(): String {
        if (playlist.isEmpty()) return "No Songs Found"
        return playlist[currentIndex].nameWithoutExtension
    }

    fun toggleVocal(): Boolean {
        isVocalOn = !isVocalOn
        player?.let { p ->
            val trackSelectionParameters = p.trackSelectionParameters
                .buildUpon()
                .setMaxAudioChannelCount(if (isVocalOn) Int.MAX_VALUE else 1)
                .build()
            p.trackSelectionParameters = trackSelectionParameters
        }
        return isVocalOn
    }

    fun isVocalEnabled(): Boolean = isVocalOn

    fun setAudioChannelMode(mode: AudioChannelMode) {
        currentChannelMode = mode
        player?.let { p ->
            val audioChannelCount = when (mode) {
                AudioChannelMode.STEREO -> Int.MAX_VALUE
                AudioChannelMode.LEFT_ONLY -> 1
                AudioChannelMode.RIGHT_ONLY -> 1
                AudioChannelMode.MONO_MIX -> 1
                AudioChannelMode.VOCAL_REMOVER -> 1
            }

            val trackSelectionParameters = p.trackSelectionParameters
                .buildUpon()
                .setMaxAudioChannelCount(audioChannelCount)
                .build()

            p.trackSelectionParameters = trackSelectionParameters

            // Update volume for left/right channel selection
            when (mode) {
                AudioChannelMode.LEFT_ONLY -> {
                    p.volume = 1.0f
                }
                AudioChannelMode.RIGHT_ONLY -> {
                    p.volume = 1.0f
                }
                else -> {
                    p.volume = 1.0f
                }
            }
        }
    }

    fun getCurrentChannelMode(): AudioChannelMode = currentChannelMode

    fun getChannelModeDescription(): String {
        return when (currentChannelMode) {
            AudioChannelMode.STEREO -> "Stereo (เสียงเต็ม)"
            AudioChannelMode.LEFT_ONLY -> "Left Channel"
            AudioChannelMode.RIGHT_ONLY -> "Right Channel"
            AudioChannelMode.MONO_MIX -> "Mono Mix"
            AudioChannelMode.VOCAL_REMOVER -> "Vocal Remover"
        }
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }
}
