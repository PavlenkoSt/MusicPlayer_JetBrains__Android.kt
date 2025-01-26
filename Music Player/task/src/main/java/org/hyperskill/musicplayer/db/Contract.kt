package org.hyperskill.musicplayer.db

import android.provider.BaseColumns

object MusicPlayerContract {
    object Playlist: BaseColumns {
        const val TABLE_NAME = "playlist"
        const val COLUMN_NAME_PLAYLIST_NAME = "playlistName"
        const val COLUMN_NAME_SONG_ID = "songId"
    }
}