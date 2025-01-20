package org.hyperskill.musicplayer.models

import org.hyperskill.musicplayer.states.TrackState

data class TrackModel(
    val song: SongModel,
    var state: TrackState = TrackState.STOPPED
)
