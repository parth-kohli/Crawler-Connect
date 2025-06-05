package com.example.arcadecrawler

import android.content.Context
import android.media.MediaPlayer

object MusicController {
    private var mediaPlayer: MediaPlayer? = null

    fun start(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.music1)
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}