package org.hyperskill.musicplayer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.models.SongModel
import java.text.SimpleDateFormat
import java.util.Locale

class SongListSelectableAdapter(
    private val dataSet: Array<SongModel>,
) : RecyclerView.Adapter<SongListSelectableAdapter.ViewHolder>() {
    var selectedTrackPositions = mutableListOf<Int>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val artistTv: TextView
        val titleTv: TextView
        val durationTv: TextView
        val selectedCheckbox: CheckBox

        init {
            artistTv = view.findViewById(R.id.songSelectorItemTvArtist)
            titleTv = view.findViewById(R.id.songSelectorItemTvTitle)
            durationTv = view.findViewById(R.id.songSelectorItemTvDuration)
            selectedCheckbox = view.findViewById(R.id.songSelectorItemCheckBox)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_song_selector, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.artistTv.text = dataSet[position].artist
        viewHolder.titleTv.text = dataSet[position].title
        val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        viewHolder.durationTv.text = formatter.format(dataSet[position].duration)

        if(selectedTrackPositions.contains(position)) {
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
            viewHolder.selectedCheckbox.isChecked = true
        }else{
            viewHolder.itemView.setBackgroundColor(Color.WHITE)
            viewHolder.selectedCheckbox.isChecked = false
        }

        viewHolder.selectedCheckbox.setOnCheckedChangeListener({ _, checked ->
            if (checked) {
                selectedTrackPositions.add(position)
                viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                selectedTrackPositions =
                    selectedTrackPositions.filter { it != position }.toMutableList()
                viewHolder.itemView.setBackgroundColor(Color.WHITE)
            }
        })
    }

    override fun getItemCount() = dataSet.size
}