package util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.ImageView
import com.prismsoftworks.genericsoundboard.R
import java.io.File
import java.io.IOException

class CustomPlayer(aContext: Context): MediaPlayer(){
    val TAG = CustomPlayer::class.java.simpleName
    var imageView: ImageView = ImageView(aContext)
    lateinit var playbackListener: PlaybackListener
    lateinit var filePlaying: File


    companion object {
        fun create(context: Context, uri: Uri): CustomPlayer{
            var player = CustomPlayer(context)
            try {
                player.setDataSource(context, uri)
                player.prepare()
            } catch (e: IOException){
                e.printStackTrace()
                player = CustomPlayer(context)
            }

            return player
        }

        interface PlaybackListener{
            fun onPlay()
            fun onStopped()
            fun onPause()
        }
    }


    override fun start() {
        super.start()
        imageView.setImageResource(R.drawable.ic_pause_black_48dp)
        playbackListener.onPlay()
    }

    override fun pause() {
        super.pause()
        playbackListener.onPause()
    }

    override fun stop() {
        super.stop()
        imageView.setImageResource(R.drawable.ic_play_arrow_black_48dp)
        playbackListener.onStopped()
    }
}