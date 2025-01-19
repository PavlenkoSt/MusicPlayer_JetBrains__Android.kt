package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.States.MainActivityState
import org.hyperskill.musicplayer.States.TrackState
import org.hyperskill.musicplayer.models.PlaylistModel
import org.hyperskill.musicplayer.models.SongModel
import org.hyperskill.musicplayer.models.TrackModel

class MainActivity : AppCompatActivity() {
    private lateinit var searchBtn: Button;
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar;
    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var mainSongList: RecyclerView

    lateinit var activityState: MainActivityState

    private var currentPlaylist: PlaylistModel? = PlaylistModel("Init", LocalDatastore.songs)
    var currentTrack: TrackModel? = null
        set(value) {
            if (value != null && value.state == TrackState.PLAYING) {
                songListAdapter?.currentTrack = value.song
            } else {
                songListAdapter?.currentTrack = null
            }
            field = value
        }

    private var songListAdapter: SongListAdapter? = null
    private var songListSelectableAdapter: SongListSelectableAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindUI()
        changeActivityState(MainActivityState.PLAY_MUSIC)
    }

    private fun onTrackLongClick(position: Int) {
        changeActivityState(MainActivityState.ADD_PLAYLIST)
        songListSelectableAdapter?.selectedTrackPositions?.add(position)
    }

    private fun updateCurrentTrack(position: Int): SongModel? {
        if (currentPlaylist == null) return null
        val track = currentPlaylist!!.songs[position]
        if (currentTrack?.song?.id != track.id) {
            currentTrack = TrackModel(track, TrackState.PLAYING)
            return track
        } else {
            currentTrack = null
            return null
        }
    }

    fun changeActivityState(state: MainActivityState) {
        if (this::activityState.isInitialized && activityState == state) return
        activityState = state

        when (state) {
            MainActivityState.PLAY_MUSIC -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.mainFragmentContainer, MainPlayerControllerFragment()
                ).commit()

                if (currentPlaylist != null) {
                    songListAdapter = SongListAdapter(
                        currentPlaylist!!.songs,
                        ::updateCurrentTrack,
                        ::onTrackLongClick
                    )

                    mainSongList.layoutManager = LinearLayoutManager(this)
                    mainSongList.adapter = songListAdapter
                }
            }

            MainActivityState.ADD_PLAYLIST -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.mainFragmentContainer, MainAddPlaylistFragment()
                ).commit()

                if (currentPlaylist != null) {
                    songListSelectableAdapter =
                        SongListSelectableAdapter(
                            currentPlaylist!!.songs,
                        )

                    mainSongList.layoutManager = LinearLayoutManager(this)
                    mainSongList.adapter = songListSelectableAdapter
                }
            }
        }
    }

    private fun bindUI() {
        searchBtn = findViewById(R.id.mainButtonSearch)
        toolbar = findViewById(R.id.toolbar)
        fragmentContainerView = findViewById(R.id.mainFragmentContainer)
        mainSongList = findViewById(R.id.mainSongList)

        searchBtn.setOnClickListener {
            Toast.makeText(this, "no songs found", Toast.LENGTH_LONG).show()
        }

        setSupportActionBar(toolbar)
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mainMenuAddPlaylist -> {
                        changeActivityState(MainActivityState.ADD_PLAYLIST)

                        Toast.makeText(
                            this@MainActivity,
                            "no songs loaded, click search to load songs",
                            Toast.LENGTH_LONG
                        ).show()
                        true
                    }

                    R.id.mainMenuLoadPlaylist -> {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("choose playlist to load")
                            .setNegativeButton(
                                "Cancel", null
                            ).show()
                        true
                    }

                    R.id.mainMenuDeletePlaylist -> {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("choose playlist to delete")
                            .setNegativeButton(
                                "Cancel", null
                            ).show()
                        true
                    }

                    else -> false
                }
            }
        })
    }
}