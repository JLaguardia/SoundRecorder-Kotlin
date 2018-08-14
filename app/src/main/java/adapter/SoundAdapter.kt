package adapter

import `object`.Sound
import android.content.Context
import android.content.DialogInterface
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.Visibility
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.prismsoftworks.genericsoundboard.MainActivity
import com.prismsoftworks.genericsoundboard.R
import kotlinx.android.synthetic.main.activity_main.*
import util.CustomPlayer
import view.SoundView
import java.io.File

/**
 * Created by jameslaguardia on 3/13/18.
 *
 */

class SoundAdapter(sounds: MutableList<Sound>, val context: Context): RecyclerView.Adapter<SoundView>(){
    val TAG= SoundAdapter::class.java.simpleName
    private val items: MutableList<Sound> = sounds
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
        holder.soundName.text = sound.title
        if(sound.file != null) {
            holder.soundPlay.setImageResource(R.drawable.ic_play_arrow_black_48dp)
            holder.soundPlay.setOnClickListener { v ->
                startPlaying(sound.file, v as ImageView)
            }

            holder.soundEdit.visibility = View.VISIBLE
            holder.soundEdit.setOnClickListener { v ->
                var poppy = PopupMenu(context, v)
                poppy.inflate(R.menu.popup_menu)
                poppy.setOnMenuItemClickListener {
                    var builder = AlertDialog.Builder(context)
                    when (it.itemId) {
                        R.id.miRename -> {
                            builder.setTitle("Rename Sound File")
                            val input = EditText(context)
                            input.hint = sound.title
                            input.inputType = InputType.TYPE_CLASS_TEXT
                            builder.setView(input)
                            builder.setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
                                val chosenName = input.text.toString()
                                sound.title = chosenName
                                sound.file!!.renameTo(File(chosenName))
                                notifyDataSetChanged()
                            }

                            builder.setNegativeButton("Cancel") { dlg: DialogInterface, _: Int ->
                                dlg.cancel()
                            }

                            builder.show()

//                        layoutInflater.inflate(R.layout.rename_view, btnMainOpts.parent as ViewGroup, false)
                            true
                        }
                        R.id.miDelete -> {

                            builder.setTitle("Delete file: ${sound.file!!.name}?")
                            builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                                deleteSound(sound)
                                notifyDataSetChanged()
                            }

                            builder.setNegativeButton("No") { dlg: DialogInterface, _: Int ->
                                dlg.cancel()
                            }

                            builder.show()

                            true
                        }
                        R.id.miProperties -> {
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }

            poppy.show()

            }
        } else {
            holder.soundPlay.setImageResource(R.drawable.ic_mic_black_48dp)
            holder.soundPlay.setOnClickListener {v ->
                (context as MainActivity).startRecording(holder.soundPlay)
            }

            holder.soundEdit.visibility = View.GONE
        }
    }

    private fun startPlaying(aFile: File?, aImg: ImageView) {
        Log.d(TAG, "setting up waveform and starting playback for: " + aFile!!.name)

        if (mediaPlayer != null && mediaPlayer!!.isPlaying && aFile != mediaPlayer!!.filePlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }

        mediaPlayer = CustomPlayer.create(context, Uri.fromFile(aFile))
        mediaPlayer!!.imageView = aImg
        mediaPlayer!!.filePlaying = aFile

        mediaPlayer!!.playbackListener = (object : CustomPlayer.Companion.PlaybackListener {
            override fun onPlay() {
                Log.d(TAG, "Play")
                aImg.setOnClickListener { view -> mediaPlayer!!.stop()}

            }

            override fun onStopped() {
                Log.d(TAG, "Stopped")
                aImg.setOnClickListener { view -> startPlaying(aFile, view as ImageView) }
            }

            override fun onPause() {
                Log.d(TAG, "Paused")
                aImg.setOnClickListener { view -> mediaPlayer!!.pause() }
            }
        })

        mediaPlayer!!.setOnCompletionListener { mediaPlayer -> mediaPlayer.stop() }
        mediaPlayer!!.setOnPreparedListener { mediaPlayer -> mediaPlayer.start() }
    }

    fun deleteSound(sound: Sound){
        sound.file!!.delete()
        items.remove(sound)
    }

    fun stopPlaying(){
//        mediaPlayer.stop()
//        mediaPlayer.reset()
    }

    fun pausePlaying(){
//        mediaPlayer.pause()
    }
}