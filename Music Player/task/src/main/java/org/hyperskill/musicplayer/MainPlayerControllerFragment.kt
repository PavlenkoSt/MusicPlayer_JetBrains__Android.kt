package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.hyperskill.musicplayer.States.TrackState
import org.hyperskill.musicplayer.models.TrackModel

class MainPlayerControllerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_player_controller_fragment, container, false)

        val playPauseBtn = view.findViewById<Button>(R.id.controllerBtnPlayPause)

        val mainActivity = (activity as MainActivity)

        playPauseBtn.setOnClickListener {
            if (mainActivity.currentTrack == null) return@setOnClickListener

            if (mainActivity.currentTrack!!.state == TrackState.PAUSED || mainActivity.currentTrack!!.state == TrackState.STOPPED) {
                mainActivity.currentTrack = TrackModel(mainActivity.currentTrack!!.song, TrackState.PLAYING)
            } else {
                mainActivity.currentTrack = TrackModel(mainActivity.currentTrack!!.song, TrackState.STOPPED)
            }
        }

        return view
    }
}