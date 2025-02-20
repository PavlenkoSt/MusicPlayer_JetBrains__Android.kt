package org.hyperskill.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.hyperskill.musicplayer.MainActivity
import org.hyperskill.musicplayer.R
import org.hyperskill.musicplayer.RESERVED_PLAYLIST_NAME
import org.hyperskill.musicplayer.enums.MainActivityState

class MainAddPlaylistFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_add_playlist_fragment, container, false)

        val addPlaylistBtnCancel = view.findViewById<Button>(R.id.addPlaylistBtnCancel)
        val addPlaylistBtnOk = view.findViewById<Button>(R.id.addPlaylistBtnOk)
        val addPlaylistEtPlaylistName = view.findViewById<TextView>(R.id.addPlaylistEtPlaylistName)

        val mainActivity = (activity as MainActivity)

        addPlaylistBtnCancel.setOnClickListener {
            mainActivity.changeActivityState(MainActivityState.PLAY_MUSIC)
        }

        addPlaylistBtnOk.setOnClickListener {
            val playlistName = addPlaylistEtPlaylistName.text.toString().trim()
            val ids = mainActivity.songListSelectableAdapter?.selectedTrackIds

            if (ids.isNullOrEmpty() && !playlistName.equals(RESERVED_PLAYLIST_NAME, ignoreCase = true)) {
                Toast.makeText(
                    mainActivity,
                    "Add at least one song to your playlist",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (playlistName.equals(RESERVED_PLAYLIST_NAME, ignoreCase = true)) {
                Toast.makeText(
                    mainActivity,
                    "All Songs is a reserved name choose another playlist name",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (playlistName.isEmpty()) {
                Toast.makeText(mainActivity, "Add a name to your playlist", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            mainActivity.addPlaylist(addPlaylistEtPlaylistName.text.toString(), ids!!)
        }

        return view
    }
}