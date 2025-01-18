package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MainAddPlaylistFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_add_playlist_fragment, container, false)

        val addPlaylistBtnCancel = view.findViewById<Button>(R.id.addPlaylistBtnCancel)

        addPlaylistBtnCancel.setOnClickListener {
            (activity as MainActivity).changeActivityState(MainActivityState.PLAY_MUSIC)
        }

        return view
    }
}