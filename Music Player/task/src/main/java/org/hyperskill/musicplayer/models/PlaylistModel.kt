package org.hyperskill.musicplayer.models

data class PlaylistModel(
    val name: String,
    val songs: List<SongModel>
)
