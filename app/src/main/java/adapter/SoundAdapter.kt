package adapter

import `object`.Sound
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.prismsoftworks.genericsoundboard.MainActivity
import com.prismsoftworks.genericsoundboard.R
import util.CustomPlayer
import view.SoundView
import java.io.File

/**
 * Created by jameslaguardia on 3/13/18.
 *
 */

class SoundAdapter(sounds: List<Sound>, val context: Context): RecyclerView.Adapter<SoundView>(){
    val TAG= SoundAdapter::class.java.simpleName
    private val items: List<Sound> = sounds
    private var mediaPlayer: CustomPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SoundView {
        val v: View = LayoutInflater.from(parent!!.context).inflate(R.layout.rv_sound, parent, false)
        return SoundView(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: SoundView, position: Int) {
        val sound = items[position]
//        if (holder != null) {
            holder.soundName.text = sound.getTitle()
            if(sound.getSoundFile() != null) {
                holder.soundPlay.setImageResource(R.drawable.ic_play_arrow_black_48dp)
                holder.soundPlay.setOnClickListener { v ->
                    startPlaying(sound.getSoundFile(), v as ImageView)
                }
            } else {
                holder.soundPlay.setImageResource(R.drawable.ic_mic_black_48dp)
                holder.soundPlay.setOnClickListener {v ->
                    (context as MainActivity).startRecording(holder.soundPlay)
                }
            }
//        }
    }

    private fun startPlaying(file: File?, img: ImageView) {
        Log.d(TAG, "setting up waveform and starting playback for: " + file!!.name)

        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }

        mediaPlayer = CustomPlayer.create(context, Uri.fromFile(file))
        mediaPlayer!!.imageView = img
//        mediaPlayer!!.Uri = Uri.fromFile(file)

        mediaPlayer!!.playbackListener = (object : CustomPlayer.Companion.PlaybackListener {
            override fun onPlay() {
                Log.d(TAG, "Play")
                img.setOnClickListener { view -> stopPlaying(file, view as ImageView) }

            }

            override fun onStopped() {
                Log.d(TAG, "Stopped")
                img.setOnClickListener { view -> startPlaying(file, view as ImageView) }
            }

            override fun onPause() {
                Log.d(TAG, "Paused")
            }
        })

        mediaPlayer!!.setOnCompletionListener({ mediaPlayer -> mediaPlayer.stop() })
        mediaPlayer!!.setOnPreparedListener({ mediaPlayer -> mediaPlayer.start() })
//        mediaPlayer!!.start()
//        img.setOnClickListener { view -> stopPlaying(file, view as ImageView) }
    }

    fun stopPlaying(file: File?, img: ImageView){
        mediaPlayer!!.stop()
        mediaPlayer!!.reset()
    }
}