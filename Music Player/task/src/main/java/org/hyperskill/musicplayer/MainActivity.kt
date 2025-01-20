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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.stateEnums.MainActivityState
import org.hyperskill.musicplayer.stateEnums.TrackState
import org.hyperskill.musicplayer.models.PlaylistModel

class MainActivity : AppCompatActivity() {
    private lateinit var searchBtn: Button;
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar;
    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var mainSongList: RecyclerView

    lateinit var mainViewModel: MainViewModel

    var activityState: MainActivityState = MainActivityState.PLAY_MUSIC

    private var playlists = mutableListOf<PlaylistModel>()
    private var currentPlaylist: PlaylistModel? = null

    var songListAdapter: SongListAdapter? = null
    var songListSelectableAdapter: SongListSelectableAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setContentView(R.layout.activity_main)
        setupObservers()
        bindUI()
        changeActivityState(MainActivityState.PLAY_MUSIC)
    }

    private fun setupObservers() {
        mainViewModel.currentTrack.observe(this) { currentTrack ->
            if (currentTrack != null && currentTrack.state == TrackState.PLAYING) {
                songListAdapter?.currentTrack = currentTrack.song
            } else {
                songListAdapter?.currentTrack = null
            }
        }
    }

    private fun onTrackLongClick(position: Int) {
        changeActivityState(MainActivityState.ADD_PLAYLIST)
        songListSelectableAdapter?.selectedTrackPositions?.add(position)
    }

    private fun updateCurrentTrack(position: Int) {
        if (currentPlaylist == null) return
        val track = currentPlaylist!!.songs[position]

        mainViewModel.updateCurrentTrack(track)
    }

    fun addPlaylist(name: String, songPositions: List<Int>) {
        val allSongs = currentPlaylist ?: return
        val songsToAdd = allSongs.songs.filterIndexed { idx, _ -> songPositions.contains(idx) }

        val newPlaylist = PlaylistModel(name, songsToAdd)

        playlists.add(newPlaylist)

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
                    val allSongs = playlists.find { it.name == RESERVED_PLAYLIST_NAME }
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
                    val allSongsPlaylist = playlists.find { it.name == RESERVED_PLAYLIST_NAME }
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
            val created = PlaylistModel(RESERVED_PLAYLIST_NAME, LocalDatastore.songs)

            playlists = playlists.filter { it.name != RESERVED_PLAYLIST_NAME }.toMutableList()
            playlists.add(0, created)

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
                            .setItems(playlists.filter { it.name != RESERVED_PLAYLIST_NAME }.map { it.name }
                                .toTypedArray(),
                                { dialog, idx ->
                                    if (activityState == MainActivityState.ADD_PLAYLIST || currentPlaylist?.name == playlists[idx + 1].name) {
                                        currentPlaylist = playlists.first() // All Songs
                                        changeActivityState(activityState)
                                    }

                                    playlists.remove(playlists[idx + 1])

                                    dialog.dismiss()
                                }
                            )
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