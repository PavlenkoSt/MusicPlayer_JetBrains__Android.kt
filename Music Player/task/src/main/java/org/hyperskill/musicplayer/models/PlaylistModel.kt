package org.hyperskill.musicplayer.models

data class PlaylistModel(
    val name: String,
    val songIds: List<Long>
)
