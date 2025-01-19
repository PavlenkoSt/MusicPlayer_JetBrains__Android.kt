package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.hyperskill.musicplayer.States.MainActivityState

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
            val positions = mainActivity.songListSelectableAdapter?.selectedTrackPositions
            if (positions.isNullOrEmpty()) {
                Toast.makeText(
                    mainActivity,
                    "Add at least one song to your playlist",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (addPlaylistEtPlaylistName.text.toString().isEmpty()) {
                Toast.makeText(mainActivity, "Add a name to your playlist", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            if (addPlaylistEtPlaylistName.text.toString() == "All Songs") {
                Toast.makeText(
                    mainActivity,
                    "All Songs is a reserved name choose another playlist name",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            mainActivity.addPlaylist(addPlaylistEtPlaylistName.text.toString(), positions)
        }

        return view
    }
}