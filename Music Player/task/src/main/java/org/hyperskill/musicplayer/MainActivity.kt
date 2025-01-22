package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.hyperskill.musicplayer.databinding.ActivityMainBinding
import org.hyperskill.musicplayer.stateEnums.MainActivityState
import org.hyperskill.musicplayer.stateEnums.TrackState
import org.hyperskill.musicplayer.models.PlaylistModel
import org.hyperskill.musicplayer.models.TrackModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var mainViewModel: MainViewModel

    var activityState: MainActivityState = MainActivityState.PLAY_MUSIC

    private var playlists = mutableListOf<PlaylistModel>()
    private var currentPlaylist: PlaylistModel? = null
    private var currentPlaylistSelectFrom: PlaylistModel? = null

    var songListAdapter: SongListAdapter? = null
    var songListSelectableAdapter: SongListSelectableAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupObservers()
        bindUI()
        changeActivityState(MainActivityState.PLAY_MUSIC)
    }

    private fun setupObservers() {
        mainViewModel.currentTrack.observe(this) { currentTrack ->
            songListAdapter?.currentTrack = currentTrack
        }
    }

    private fun onTrackLongClick(position: Int) {
        changeActivityState(MainActivityState.ADD_PLAYLIST)
        songListSelectableAdapter?.selectSongByPosition(position)
    }

    private fun updateCurrentTrack(position: Int) {
        if (currentPlaylist == null) return
        val track = currentPlaylist!!.songs[position]

        mainViewModel.updateCurrentTrack(track)
    }

    fun addPlaylist(name: String, songIds: List<Int>) {
        val allSongs = currentPlaylist ?: return
        val songsToAdd = allSongs.songs.filter { songIds.contains(it.id) }

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

                songListAdapter = SongListAdapter(
                    currentPlaylist?.songs ?: emptyList(),
                    mainViewModel.currentTrack.value,
                    ::updateCurrentTrack,
                    ::onTrackLongClick
                )

                binding.mainSongList.layoutManager = LinearLayoutManager(this)
                binding.mainSongList.adapter = songListAdapter
            }

            MainActivityState.ADD_PLAYLIST -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.mainFragmentContainer, MainAddPlaylistFragment()
                ).commit()

                currentPlaylistSelectFrom =
                    currentPlaylist ?: playlists.find { it.name == RESERVED_PLAYLIST_NAME }

                songListSelectableAdapter =
                    SongListSelectableAdapter(
                        currentPlaylistSelectFrom!!.songs,
                        null
                    )

                binding.mainSongList.layoutManager = LinearLayoutManager(this)
                binding.mainSongList.adapter = songListSelectableAdapter
            }
        }
    }

    private fun updateSelectableSongListAdapter(playlist: PlaylistModel?) {
        currentPlaylistSelectFrom = playlist

        val savedSelectedItems =
            songListSelectableAdapter?.selectedTrackIds.orEmpty()
        val newSelectedItems =
            currentPlaylistSelectFrom?.songs
                ?.filter { savedSelectedItems.contains(it.id) }
                .orEmpty()
                .map { it.id }

        songListSelectableAdapter =
            SongListSelectableAdapter(
                currentPlaylistSelectFrom!!.songs,
                newSelectedItems
            )

        binding.mainSongList.adapter = songListSelectableAdapter
    }

    private fun bindUI() {
        binding.mainButtonSearch.setOnClickListener {
            val created = PlaylistModel(RESERVED_PLAYLIST_NAME, LocalDatastore.songs)

            if (activityState == MainActivityState.ADD_PLAYLIST) {
                updateSelectableSongListAdapter(created)
                return@setOnClickListener
            }

            playlists = playlists.filter { it.name != RESERVED_PLAYLIST_NAME }.toMutableList()
            playlists.add(0, created)

            if (activityState == MainActivityState.PLAY_MUSIC) {
                currentPlaylist = created
                changeActivityState(MainActivityState.PLAY_MUSIC)
            }
        }

        setSupportActionBar(binding.toolbar)
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
                        val items = playlists.map { it.name }.toTypedArray()

                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("choose playlist to load")
                            .setItems(items, { dialog, idx ->
                                val playlistPressedTo = playlists[idx]

                                if (activityState == MainActivityState.ADD_PLAYLIST) {
                                    updateSelectableSongListAdapter(playlistPressedTo)
                                } else {
                                    currentPlaylist = playlistPressedTo

                                    if (mainViewModel.currentTrack.value == null
                                        || !currentPlaylist!!.songs.contains(
                                            mainViewModel.currentTrack.value?.song
                                        )
                                    ) {
                                        mainViewModel.setCurrentTrack(
                                            TrackModel(
                                                song = currentPlaylist!!.songs[0],
                                                state = TrackState.STOPPED,
                                            )
                                        )
                                    }

                                    changeActivityState(MainActivityState.PLAY_MUSIC)
                                }

                                dialog.dismiss()

                            })
                            .setNegativeButton(
                                "Cancel", null
                            ).show()
                        true
                    }

                    R.id.mainMenuDeletePlaylist -> {
                        val items = playlists.filter { it.name != RESERVED_PLAYLIST_NAME }
                            .map { it.name }
                            .toTypedArray()

                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("choose playlist to delete")
                            .setItems(items,
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