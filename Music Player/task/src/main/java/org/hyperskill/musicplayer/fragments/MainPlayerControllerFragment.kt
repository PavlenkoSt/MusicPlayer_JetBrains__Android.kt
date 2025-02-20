package org.hyperskill.musicplayer.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.hyperskill.musicplayer.MainActivity
import org.hyperskill.musicplayer.R
import org.hyperskill.musicplayer.formatter
import org.hyperskill.musicplayer.enums.TrackState

class MainPlayerControllerFragment : Fragment() {
    lateinit var handler: Handler
    val updateInterval = 100L

    var controllerSeekBar: SeekBar? = null
    var controllerTvTotalTime: TextView? = null
    var controllerTvCurrentTime: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_player_controller_fragment, container, false)

        val playPauseBtn = view.findViewById<Button>(R.id.controllerBtnPlayPause)
        val stopBtn = view.findViewById<Button>(R.id.controllerBtnStop)
        controllerSeekBar = view.findViewById(R.id.controllerSeekBar)
        controllerTvTotalTime = view.findViewById(R.id.controllerTvTotalTime)
        controllerTvCurrentTime = view.findViewById(R.id.controllerTvCurrentTime)

        val mainActivity = (activity as MainActivity)

        handler = Handler(mainActivity.mainLooper)

        setInitialData()

        controllerSeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                controllerTvCurrentTime?.text = formatter.format(p1 * 1000)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                val track = mainActivity.mainViewModel.currentTrack.value
                if (track?.state == TrackState.PLAYING) {
                    stopSeekBarTracking()
                }
            }

            override fun onStopTrackingTouch(seeker: SeekBar) {
                val track = mainActivity.mainViewModel.currentTrack.value
                track?.track?.seekTo(seeker.progress * 1000)
                if (track?.state == TrackState.PLAYING) {
                    updateSeekBar(track.track)
                }
            }
        })

        playPauseBtn.setOnClickListener {
            val currentTrack =
                mainActivity.mainViewModel.currentTrack.value ?: return@setOnClickListener

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

    private fun setInitialData() {
        val mainActivity = (activity as MainActivity)
        val currentTrack = mainActivity.mainViewModel.currentTrack.value ?: return
        setTrackProgress(currentTrack.track.duration)
    }

    fun setTrackProgress(duration: Int) {
        controllerSeekBar?.max = duration / 1000
        controllerTvTotalTime?.text = formatter.format(duration)
    }

    fun resetTrackProgress() {
        controllerTvCurrentTime?.text = formatter.format(0)
        controllerSeekBar?.progress = 0
    }

    fun updateSeekBar(mediaPlayer: MediaPlayer) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    val currentPosition = mediaPlayer.currentPosition
                    controllerSeekBar?.progress = if(currentPosition <= 0) 0 else currentPosition / 1000
                    controllerTvCurrentTime?.text = formatter.format(currentPosition)

                    handler.postDelayed(this, updateInterval)
                }
            }
        }, updateInterval)
    }

    fun stopSeekBarTracking() {
        handler.removeCallbacksAndMessages(null)
    }
}