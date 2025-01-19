package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.hyperskill.musicplayer.States.TrackState

class MainPlayerControllerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_player_controller_fragment, container, false)

        val playPauseBtn = view.findViewById<Button>(R.id.controllerBtnPlayPause)
        val stopBtn = view.findViewById<Button>(R.id.controllerBtnStop)

        val mainActivity = (activity as MainActivity)

        playPauseBtn.setOnClickListener {
            val currentTrack = mainActivity.mainViewModel.currentTrack.value ?: return@setOnClickListener

            if (currentTrack.state == TrackState.PAUSED || currentTrack.state == TrackState.STOPPED) {
                mainActivity.mainViewModel.updateCurrentTrackStatus(TrackState.PLAYING)
            } else {
                mainActivity.mainViewModel.updateCurrentTrackStatus(TrackState.PAUSED)
            }

            mainActivity.songListAdapter?.notifyDataSetChanged()
        }

        stopBtn.setOnClickListener {
            mainActivity.mainViewModel.updateCurrentTrackStatus(TrackState.STOPPED)
            mainActivity.songListAdapter?.notifyDataSetChanged()
        }

        return view
    }
}