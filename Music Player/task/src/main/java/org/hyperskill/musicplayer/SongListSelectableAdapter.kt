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
    private val dataSet: List<SongModel>,
    initialPositions: List<Int>?
) : RecyclerView.Adapter<SongListSelectableAdapter.ViewHolder>() {
    var selectedTrackIds = mutableListOf<Int>()

    init {
        if(initialPositions != null) {
            selectedTrackIds = initialPositions.toMutableList()
        }
    }

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

        val id = dataSet[position].id

        if (selectedTrackIds.contains(id)) {
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
            viewHolder.selectedCheckbox.isChecked = true
        } else {
            viewHolder.itemView.setBackgroundColor(Color.WHITE)
            viewHolder.selectedCheckbox.isChecked = false
        }

        viewHolder.selectedCheckbox.setOnCheckedChangeListener { btn, checked ->
            if (checked) {
                selectedTrackIds.add(id)
                viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                selectedTrackIds.remove(id)
                viewHolder.itemView.setBackgroundColor(Color.WHITE)
            }
        }

        viewHolder.itemView.setOnClickListener {
            if (selectedTrackIds.contains(id)) {
                selectedTrackIds.remove(id)
                viewHolder.itemView.setBackgroundColor(Color.WHITE)
                viewHolder.selectedCheckbox.isChecked = false
            } else {
                selectedTrackIds.add(id)
                viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
                viewHolder.selectedCheckbox.isChecked = true
            }
        }
    }

    fun selectSongByPosition (position: Int) {
        val id = dataSet[position].id
        selectedTrackIds.add(id)
    }

    override fun getItemCount() = dataSet.size
}