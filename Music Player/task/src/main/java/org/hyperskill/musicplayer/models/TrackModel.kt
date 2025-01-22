package org.hyperskill.musicplayer.models

import android.media.MediaPlayer
import org.hyperskill.musicplayer.stateEnums.TrackState

data class TrackModel(
    val song: SongModel,
    var state: TrackState? = null,
    var track: MediaPlayer
)
