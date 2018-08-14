package view

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.prismsoftworks.genericsoundboard.R

/**
 * Created by jameslaguardia on 4/3/18.
 */
class SoundView(view: View): RecyclerView.ViewHolder(view){
    val soundPlay: ImageView = view.findViewById(R.id.sound_play)
    val soundName: TextView = view.findViewById(R.id.sound_name)
    val soundEdit: TextView = view.findViewById(R.id.sound_edit)
}