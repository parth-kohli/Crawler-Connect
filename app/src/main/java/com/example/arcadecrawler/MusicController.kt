package com.example.arcadecrawler

import android.content.Context
import android.media.MediaPlayer
import java.io.File
import java.io.FileOutputStream

object MusicController {
    private var mediaPlayer: MediaPlayer? = null

    fun start(context: Context, music: File, volume: Float) {
        if (mediaPlayer == null) {
            println(music.absolutePath)
            mediaPlayer = MediaPlayer().apply { setDataSource(music.absolutePath) }
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        mediaPlayer?.setVolume(volume, volume)
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