package org.hyperskill.musicplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyperskill.musicplayer.stateEnums.TrackState
import org.hyperskill.musicplayer.models.SongModel
import org.hyperskill.musicplayer.models.TrackModel

class MainViewModel : ViewModel() {
    private val _currentTrack = MutableLiveData<TrackModel?>()
    var currentTrack: LiveData<TrackModel?> = _currentTrack

    fun updateCurrentTrack(song: SongModel) {
        if (_currentTrack.value?.song?.id == song.id) {
            if (currentTrack.value!!.state == TrackState.PLAYING) {
                _currentTrack.value = _currentTrack.value?.copy(
                    state = TrackState.PAUSED
                )
            } else {
                _currentTrack.value = _currentTrack.value?.copy(
                    state = TrackState.PLAYING
                )
            }

        } else {
            _currentTrack.value = TrackModel(song, TrackState.PLAYING)
        }
    }

    fun setCurrentTrack(track: TrackModel) {
        _currentTrack.value = track
    }

    fun updateCurrentTrackStatus(state: TrackState) {
        _currentTrack.value = _currentTrack.value?.copy(
            state = state
        )
    }
 }