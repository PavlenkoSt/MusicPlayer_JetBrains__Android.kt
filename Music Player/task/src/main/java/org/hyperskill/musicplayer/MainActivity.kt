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

    var activityState: MainActivityState = MainActivityState.PLAY_MUSIC

    private var playlists = mutableListOf<PlaylistModel>()
    private var currentPlaylist: PlaylistModel? = null

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
    var songListSelectableAdapter: SongListSelectableAdapter? = null

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
            currentTrack = TrackModel(track, TrackState.PAUSED)
            return null
        }
    }

    fun addPlaylist(name: String, songPositions: List<Int>) {
        val allSongs = currentPlaylist ?: return
        val songsToAdd = allSongs.songs.filterIndexed { idx, _ -> songPositions.contains(idx) }

        val newPlaylist = PlaylistModel(name, songsToAdd)

        playlists.add(newPlaylist)

        currentPlaylist = newPlaylist

        changeActivityState(MainActivityState.PLAY_MUSIC)
    }

    fun changeActivityState(state: MainActivityState) {
        activityState = state

        when (state) {
            MainActivityState.PLAY_MUSIC -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.mainFragmentContainer, MainPlayerControllerFragment()
                ).commit()

                if (currentPlaylist == null) {
                    val allSongs = playlists.find { it.name == "All Songs" }
                    currentPlaylist = allSongs
                }

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

                if (currentPlaylist == null) {
                    val allSongsPlaylist = playlists.find{it.name == "All Songs"}
                    currentPlaylist = allSongsPlaylist
                }

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
            val created = PlaylistModel("All Songs", LocalDatastore.songs)

            playlists = playlists.filter { it.name != "All Songs" }.toMutableList()
            playlists.add(created)

            if (activityState == MainActivityState.PLAY_MUSIC) {
                currentPlaylist = created
                changeActivityState(MainActivityState.PLAY_MUSIC)
            }
        }

        setSupportActionBar(toolbar)
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mainMenuAddPlaylist -> {
                        if (activityState == MainActivityState.ADD_PLAYLIST) return true

                        if (playlists.isEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "no songs loaded, click search to load songs",
                                Toast.LENGTH_LONG
                            ).show()

                            return true
                        }

                        changeActivityState(MainActivityState.ADD_PLAYLIST)

                        return true
                    }

                    R.id.mainMenuLoadPlaylist -> {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("choose playlist to load")
                            .setItems(playlists.map { it.name }.toTypedArray(), { dialog, idx ->
                                currentPlaylist = playlists[idx]
                                dialog.dismiss()
                                if (activityState == MainActivityState.ADD_PLAYLIST) {
                                    changeActivityState(MainActivityState.ADD_PLAYLIST)
                                } else {
                                    changeActivityState(MainActivityState.PLAY_MUSIC)
                                }
                            })
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