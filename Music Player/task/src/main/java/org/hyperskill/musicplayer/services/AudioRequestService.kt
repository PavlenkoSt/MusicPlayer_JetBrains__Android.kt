package org.hyperskill.musicplayer.services

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import org.hyperskill.musicplayer.models.SongModel

object AudioRequestService {
    fun getAudioFiles(context: Context): List<SongModel> {
        val audioFiles = mutableListOf<SongModel>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val contentResolver: ContentResolver = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val duration = it.getLong(durationColumn)

                audioFiles.add(SongModel(id, title, artist, duration))
            }
        }

        return audioFiles
    }
}