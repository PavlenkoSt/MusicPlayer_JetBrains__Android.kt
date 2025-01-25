package org.hyperskill.musicplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyperskill.musicplayer.models.PlaylistModel
import org.hyperskill.musicplayer.enums.TrackState
import org.hyperskill.musicplayer.models.TrackModel

class MainViewModel() : ViewModel() {
    private val _currentTrack = MutableLiveData<TrackModel?>()
    var currentTrack: LiveData<TrackModel?> = _currentTrack

    var playlists = mutableListOf<PlaylistModel>()
    var currentPlaylistSelectFrom: PlaylistModel? = null
    var currentPlaylist: PlaylistModel? = null
        set(value) {
            currentPlaylistSelectFrom = value
            field = value
        }

    fun setReservedPlaylist(playlist: PlaylistModel) {
        playlists =
            playlists.filter { it.name != RESERVED_PLAYLIST_NAME }.toMutableList()
        playlists.add(0, playlist)

        currentPlaylist = playlist
    }

    fun setCurrentTrack(track: TrackModel) {
        _currentTrack.value = track
    }

    fun updateCurrentTrackStatus(state: TrackState) {
        _currentTrack.value = _currentTrack.value?.copy(
            state = state
        )
    }

    fun addPlaylist(name: String, songIds: List<Long>) {
        val allSongs = currentPlaylist ?: return
        val songsToAdd = allSongs.songs.filter { songIds.contains(it.id) }

        val newPlaylist = PlaylistModel(name, songsToAdd)

        playlists.add(newPlaylist)
    }

    fun deletePlaylist(playlist: PlaylistModel) {
        playlists.remove(playlist)
    }

    fun makeReservedPlaylistCurrent() {
        currentPlaylist = playlists.first()
    }

    fun updateCurrentPlaylist(playlist: PlaylistModel) {
        currentPlaylist = playlist
    }

    fun updateCurrentPlaylistSelectFrom(playlist: PlaylistModel?) {
        currentPlaylistSelectFrom = playlist
    }
}