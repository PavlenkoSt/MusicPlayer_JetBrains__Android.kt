package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.databinding.ActivityMainBinding
import org.hyperskill.musicplayer.stateEnums.MainActivityState
import org.hyperskill.musicplayer.stateEnums.TrackState
import org.hyperskill.musicplayer.models.PlaylistModel
import org.hyperskill.musicplayer.models.TrackModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var mainViewModel: MainViewModel

    private var activityState: MainActivityState = MainActivityState.PLAY_MUSIC

    private var playlists = mutableListOf<PlaylistModel>()
    private var currentPlaylistSelectFrom: PlaylistModel? = null
    private var currentPlaylist: PlaylistModel? = null
        set(value) {
            currentPlaylistSelectFrom = value
            field = value
        }

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

        val fragment = when (state) {
            MainActivityState.PLAY_MUSIC -> MainPlayerControllerFragment()
            MainActivityState.ADD_PLAYLIST -> MainAddPlaylistFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()

        when (state) {
            MainActivityState.PLAY_MUSIC -> updatePlayMusicAdapter()
            MainActivityState.ADD_PLAYLIST -> updateAddPlaylistAdapter()
        }
    }

    private fun updatePlayMusicAdapter() {
        songListAdapter = SongListAdapter(
            currentPlaylist?.songs ?: emptyList(),
            mainViewModel.currentTrack.value,
            ::updateCurrentTrack,
            ::onTrackLongClick
        )
        setupAdapter(songListAdapter!!, LinearLayoutManager(this))
    }

    private fun updateAddPlaylistAdapter() {
        val songs = currentPlaylistSelectFrom?.songs ?: emptyList()
        songListSelectableAdapter = SongListSelectableAdapter(songs, null)
        setupAdapter(songListSelectableAdapter!!, LinearLayoutManager(this))
    }

    private fun setupAdapter(
        adapter: RecyclerView.Adapter<*>,
        layoutManager: RecyclerView.LayoutManager
    ) {
        binding.mainSongList.apply {
            this.layoutManager = layoutManager
            this.adapter = adapter
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

            currentPlaylist = created
            changeActivityState(MainActivityState.PLAY_MUSIC)
        }

        setSupportActionBar(binding.toolbar)
        addMenuProvider(MainMenuProvider(this))
    }

    /*
    * Menu actions
    * */

    fun handleAddPlaylistMenuClick() {
        if (activityState == MainActivityState.ADD_PLAYLIST) return

        if (playlists.isEmpty()) {
            Toast.makeText(
                this@MainActivity,
                "no songs loaded, click search to load songs",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        changeActivityState(MainActivityState.ADD_PLAYLIST)
    }

    fun handleLoadPlaylistMenuClick() {
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
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun handleDeletePlaylistMenuClick() {
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
            .setNegativeButton("Cancel", null)
            .show()
    }
}