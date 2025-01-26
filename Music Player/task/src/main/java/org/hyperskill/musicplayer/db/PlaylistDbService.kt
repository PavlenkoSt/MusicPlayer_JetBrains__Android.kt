package org.hyperskill.musicplayer.db

import android.content.ContentValues
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistDbService(context: Context) {
    private val dbHelper = MusicPlayerDbHelper(context)

    companion object {
        @Volatile
        private var instance: PlaylistDbService? = null

        fun getInstance(context: Context): PlaylistDbService {
            return instance ?: synchronized(this) {
                instance ?: PlaylistDbService(context).also { instance = it }
            }
        }
    }

    suspend fun insertPlaylist(playlistName: String, songIds: List<Long>) {
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase

            songIds.forEach {
                val values = ContentValues().apply {
                    put(MusicPlayerContract.Playlist.COLUMN_NAME_PLAYLIST_NAME, playlistName)
                    put(MusicPlayerContract.Playlist.COLUMN_NAME_SONG_ID, it)
                }

                db.insert(MusicPlayerContract.Playlist.TABLE_NAME, null, values)
            }
        }
    }

    suspend fun getPlaylists(): Map<String, List<Int>> {
        return withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase

            val projection = arrayOf(
                MusicPlayerContract.Playlist.COLUMN_NAME_PLAYLIST_NAME,
                MusicPlayerContract.Playlist.COLUMN_NAME_SONG_ID
            )

            val cursor = db.query(
                MusicPlayerContract.Playlist.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                "${MusicPlayerContract.Playlist.COLUMN_NAME_PLAYLIST_NAME} ASC"
            )

            val playlistsMap = mutableMapOf<String, MutableList<Int>>()

            with(cursor) {
                while (moveToNext()) {
                    val playlistName =
                        getString(getColumnIndexOrThrow(MusicPlayerContract.Playlist.COLUMN_NAME_PLAYLIST_NAME))
                    val songId =
                        getInt(getColumnIndexOrThrow(MusicPlayerContract.Playlist.COLUMN_NAME_SONG_ID))

                    playlistsMap.getOrPut(playlistName) { mutableListOf() }.add(songId)
                }

                close()
            }

            playlistsMap
        }
    }

    suspend fun deletePlaylist(playlistName: String) {
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase

            val selection = "${MusicPlayerContract.Playlist.COLUMN_NAME_PLAYLIST_NAME} LIKE ?"
            val selectionArgs = arrayOf(playlistName)

            db.delete(
                MusicPlayerContract.Playlist.TABLE_NAME,
                selection,
                selectionArgs
            )
        }
    }
}