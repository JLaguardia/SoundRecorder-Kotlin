package util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.prismsoftworks.genericsoundboard.R
import java.io.IOException

class CustomPlayer(aContext: Context): MediaPlayer(){
    val TAG = CustomPlayer::class.java.simpleName
//    var uri: Uri = android.net.Uri.EMPTY
    var imageView: ImageView = ImageView(aContext)
    lateinit var playbackListener: PlaybackListener


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
        Log.e(TAG, "Beginning playback...")
        super.start()
        imageView.setImageResource(R.drawable.ic_pause_black_48dp)
//        if(playbackListener != null){
//            playbackListener!!.onPlay()
            playbackListener.onPlay()
//        }
    }

    override fun pause() {
        super.pause()
//        imageView.setImageResource(R.drawable.ic_play_arrow_black_48dp)
//        if(playbackListener != null) {
            playbackListener.onPause()
//            playbackListener!!.onPause()
//        }
    }

    override fun stop() {
        super.stop()
        imageView.setImageResource(R.drawable.ic_play_arrow_black_48dp)
//        if(playbackListener != null) {
            playbackListener.onStopped()
//            playbackListener!!.onPause()
//        }
    }
}