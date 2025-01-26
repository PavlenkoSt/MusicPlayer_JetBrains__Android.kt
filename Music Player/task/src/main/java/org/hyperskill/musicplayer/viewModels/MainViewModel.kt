package org.hyperskill.musicplayer.viewModels

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hyperskill.musicplayer.RESERVED_PLAYLIST_NAME
import org.hyperskill.musicplayer.db.PlaylistDbService
import org.hyperskill.musicplayer.models.PlaylistModel
import org.hyperskill.musicplayer.enums.TrackState
import org.hyperskill.musicplayer.models.SongModel
import org.hyperskill.musicplayer.models.TrackModel

class MainViewModel(
    private val playlistDbService: PlaylistDbService
) : ViewModel() {
    private val _currentTrack = MutableLiveData<TrackModel?>()
    var currentTrack: LiveData<TrackModel?> = _currentTrack

    var songs: List<SongModel> = mutableListOf()

    var playlists = mutableListOf<PlaylistModel>()
    var currentPlaylistSelectFrom: PlaylistModel? = null
    var currentPlaylist: PlaylistModel? = null
        set(value) {
            currentPlaylistSelectFrom = value
            field = value
        }
    var currentPlaylistSongs = emptyList<SongModel>()
        get() {
            if (currentPlaylist == null) return emptyList()
            if (songs.isEmpty()) return emptyList()
            return songs.filter { currentPlaylist!!.songIds.contains((it.id)) }
        }
    var currentPlaylistSelectFromSongs = emptyList<SongModel>()
        get() {
            if (currentPlaylistSelectFrom == null) return emptyList()
            if (songs.isEmpty()) return emptyList()
            return songs.filter { currentPlaylistSelectFrom!!.songIds.contains((it.id)) }
        }

    suspend fun getInitialPlaylists(): Map<String, List<Int>> {
        val playlists = playlistDbService.getPlaylists()
        return playlists
    }

    fun setAllSongs(allSongs: List<SongModel>) {
        songs = allSongs
    }

    fun setReservedPlaylist(playlist: PlaylistModel) {
        playlists = playlists
            .filter { it.name != RESERVED_PLAYLIST_NAME }
            .toMutableList()
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

    @SuppressLint("NewApi")
    fun addPlaylist(name: String, songIds: List<Long>, skipDbInsert: Boolean) {
        val newPlaylist = PlaylistModel(name, songIds)

        if (!skipDbInsert) {
            val alreadyExistsPlaylist = playlists.find { it.name == name }

            if (alreadyExistsPlaylist != null) {
               runBlocking {
                   deletePlaylist(alreadyExistsPlaylist)
               }
            }
        }

        playlists.removeIf { it.name == name }
        playlists.add(newPlaylist)

        if (!skipDbInsert) {
            viewModelScope.launch {
                playlistDbService.insertPlaylist(name, songIds)
            }
        }

    }

    suspend fun deletePlaylist(playlist: PlaylistModel) {
        playlists.remove(playlist)
        playlistDbService.deletePlaylist(playlist.name)
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