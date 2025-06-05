package com.example.arcadecrawler
import android.content.Context
import android.media.MediaPlayer
object Soundplayer {
    private var mediaPlayer: MediaPlayer? = null

    fun shoot(context: Context) {

        mediaPlayer = MediaPlayer.create(context, R.raw.shoot)
        mediaPlayer?.start()
    }


}