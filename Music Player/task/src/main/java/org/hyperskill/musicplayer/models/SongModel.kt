package org.hyperskill.musicplayer.models

data class SongModel(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long
)
