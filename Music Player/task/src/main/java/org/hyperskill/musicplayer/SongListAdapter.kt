package org.hyperskill.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.models.SongModel
import org.hyperskill.musicplayer.models.TrackModel
import org.hyperskill.musicplayer.stateEnums.TrackState
import java.text.SimpleDateFormat
import java.util.Locale

class SongListAdapter(
    private val dataSet: List<SongModel>,
    currentTrackParam: TrackModel?,
    private val changeCurrentTrack: (position: Int) -> Unit,
    private val onLongClickedItem: (position: Int) -> Unit
) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {
    var currentTrack: TrackModel? = null

    init {
        currentTrack = currentTrackParam
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val artistTv: TextView
        val titleTv: TextView
        val durationTv: TextView
        val playPauseBtn: ImageButton

        init {
            artistTv = view.findViewById(R.id.songItemTvArtist)
            titleTv = view.findViewById(R.id.songItemTvTitle)
            durationTv = view.findViewById(R.id.songItemTvDuration)
            playPauseBtn = view.findViewById(R.id.songItemImgBtnPlayPause)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_song, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.artistTv.text = dataSet[position].artist
        viewHolder.titleTv.text = dataSet[position].title
        val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        viewHolder.durationTv.text = formatter.format(dataSet[position].duration)

        if (dataSet[position].id == currentTrack?.song?.id) {
            if (currentTrack?.state == TrackState.PLAYING) {
                viewHolder.playPauseBtn.setImageResource(R.drawable.ic_pause)
            } else {
                viewHolder.playPauseBtn.setImageResource(R.drawable.ic_play)
            }
        } else {
            viewHolder.playPauseBtn.setImageResource(R.drawable.ic_play)
        }

        viewHolder.itemView.setOnLongClickListener {
            onLongClickedItem(position)
            true
        }

        viewHolder.playPauseBtn.setOnClickListener {
            val prevTrackPosition = dataSet.indexOf(currentTrack?.song)

            changeCurrentTrack(position)

            if (dataSet[position].id == currentTrack?.song?.id) {
                if (currentTrack?.state == TrackState.PLAYING) {
                    viewHolder.playPauseBtn.setImageResource(R.drawable.ic_pause)
                } else {
                    viewHolder.playPauseBtn.setImageResource(R.drawable.ic_play)
                }
            } else {
                viewHolder.playPauseBtn.setImageResource(R.drawable.ic_play)
            }

            if (prevTrackPosition >= 0) {
                notifyItemChanged(prevTrackPosition)
            }
        }
    }

    override fun getItemCount() = dataSet.size
}