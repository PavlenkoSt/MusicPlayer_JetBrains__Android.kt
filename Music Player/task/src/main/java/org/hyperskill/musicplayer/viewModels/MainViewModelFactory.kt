package org.hyperskill.musicplayer.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.hyperskill.musicplayer.db.PlaylistDbService

class MainViewModelFactory(private val playlistDbService: PlaylistDbService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(playlistDbService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}