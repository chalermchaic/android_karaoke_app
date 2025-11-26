package com.example.karaoke

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

class KaraokePlayer(private val context: Context, private val playerView: PlayerView) {

    private var player: ExoPlayer? = null
    private var playlist: List<File> = emptyList()
    private var currentIndex = 0
    private var isVocalOn = true

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

    fun releasePlayer() {
        player?.release()
        player = null
    }
}
