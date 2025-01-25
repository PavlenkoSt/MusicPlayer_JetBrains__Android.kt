package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.media.MediaPlayer
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
import org.hyperskill.musicplayer.models.SongModel
import org.hyperskill.musicplayer.models.TrackModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var mainPlayerControllerFragment: MainPlayerControllerFragment? = null
    private var mainAddPlaylistFragment: MainAddPlaylistFragment? = null

    lateinit var mainViewModel: MainViewModel

    private var activityState: MainActivityState = MainActivityState.PLAY_MUSIC

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
            if (currentTrack != null &&
                songListAdapter?.currentTrack != null &&
                currentTrack.song.id != songListAdapter!!.currentTrack!!.song.id
            ) {
                val old = songListAdapter?.currentTrack
                if (old != null) {
                    old.track.stop()
                    stopTrack(old)
                }
            }

            songListAdapter?.currentTrack = currentTrack
            val duration = currentTrack?.track?.duration

            if (duration != null && duration >= 0) {
                mainPlayerControllerFragment?.setTrackProgress(duration)
            }

            currentTrack?.track?.setOnCompletionListener {
                mainViewModel.updateCurrentTrackStatus(TrackState.STOPPED)
                songListAdapter?.notifyDataSetChanged()
            }

            when (currentTrack?.state) {
                TrackState.PLAYING -> {
                    currentTrack.track.start()
                    mainPlayerControllerFragment?.updateSeekBar(currentTrack.track)
                }

                TrackState.PAUSED -> {
                    currentTrack.track.pause()
                    mainPlayerControllerFragment?.stopSeekBarTracking()
                }

                TrackState.STOPPED -> {
                    stopTrack(currentTrack)
                }

                null -> {}
            }
        }
    }

    private fun stopTrack(currentTrack: TrackModel) {
        if (currentTrack.track.isPlaying) {
            currentTrack.track.pause()
        }
        currentTrack.track.seekTo(0)
        mainPlayerControllerFragment?.resetTrackProgress()
        mainPlayerControllerFragment?.stopSeekBarTracking()
    }

    private fun onTrackLongClick(position: Int) {
        changeActivityState(MainActivityState.ADD_PLAYLIST)
        songListSelectableAdapter?.selectSongByPosition(position)
    }

    fun addPlaylist(name: String, songIds: List<Int>) {
        mainViewModel.addPlaylist(name, songIds)
        changeActivityState(MainActivityState.PLAY_MUSIC)
    }

    fun changeActivityState(state: MainActivityState) {
        activityState = state

        val fragment = when (state) {
            MainActivityState.PLAY_MUSIC -> {
                if (mainPlayerControllerFragment == null) {
                    mainPlayerControllerFragment = MainPlayerControllerFragment()
                }

                mainPlayerControllerFragment
            }

            MainActivityState.ADD_PLAYLIST -> {
                if (mainAddPlaylistFragment == null) {
                    mainAddPlaylistFragment = MainAddPlaylistFragment()
                }
                mainAddPlaylistFragment
            }
        }

        if (fragment == null) return

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()

        when (state) {
            MainActivityState.PLAY_MUSIC -> updatePlayMusicAdapter()
            MainActivityState.ADD_PLAYLIST -> updateAddPlaylistAdapter()
        }
    }

    private fun onCurrentTrackClick(track: SongModel) {
        val currentTrack = mainViewModel.currentTrack.value

        if (currentTrack?.song?.id == track.id) {
            mainViewModel.updateCurrentTrackStatus(
                if (currentTrack.state == TrackState.PLAYING)
                    TrackState.PAUSED
                else
                    TrackState.PLAYING
            )
        } else {
            mainViewModel.setCurrentTrack(
                TrackModel(
                    track,
                    TrackState.PLAYING,
                    MediaPlayer.create(this, R.raw.wisdom)
                )
            )
        }
    }

    private fun updatePlayMusicAdapter() {
        songListAdapter = SongListAdapter(
            mainViewModel.currentPlaylist?.songs ?: emptyList(),
            mainViewModel.currentTrack.value,
            ::onCurrentTrackClick,
            ::onTrackLongClick
        )
        setupAdapter(songListAdapter!!, LinearLayoutManager(this))
    }

    private fun updateAddPlaylistAdapter() {
        val songs = mainViewModel.currentPlaylistSelectFrom?.songs ?: emptyList()
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
        mainViewModel.updateCurrentPlaylistSelectFrom(playlist)

        val savedSelectedItems =
            songListSelectableAdapter?.selectedTrackIds.orEmpty()
        val newSelectedItems =
            mainViewModel.currentPlaylistSelectFrom?.songs
                ?.filter { savedSelectedItems.contains(it.id) }
                .orEmpty()
                .map { it.id }

        songListSelectableAdapter =
            SongListSelectableAdapter(
                mainViewModel.currentPlaylistSelectFrom?.songs.orEmpty(),
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

            mainViewModel.setReservedPlaylist(created)
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

        if (mainViewModel.playlists.isEmpty()) {
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
        val items = mainViewModel.playlists.map { it.name }.toTypedArray()

        AlertDialog.Builder(this@MainActivity)
            .setTitle("choose playlist to load")
            .setItems(items, { dialog, idx ->
                val playlistPressedTo = mainViewModel.playlists[idx]

                if (activityState == MainActivityState.ADD_PLAYLIST) {
                    updateSelectableSongListAdapter(playlistPressedTo)
                } else {
                    mainViewModel.updateCurrentPlaylist(playlistPressedTo)

                    if (mainViewModel.currentTrack.value == null
                        || !mainViewModel.currentPlaylist?.songs.orEmpty().contains(
                            mainViewModel.currentTrack.value?.song
                        )
                    ) {
                        mainViewModel.setCurrentTrack(
                            TrackModel(
                                song = mainViewModel.currentPlaylist!!.songs[0],
                                state = TrackState.STOPPED,
                                track = MediaPlayer.create(this, R.raw.wisdom)
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
        val items = mainViewModel.playlists.filter { it.name != RESERVED_PLAYLIST_NAME }
            .map { it.name }
            .toTypedArray()

        AlertDialog.Builder(this@MainActivity)
            .setTitle("choose playlist to delete")
            .setItems(items,
                { dialog, idx ->
                    if (activityState == MainActivityState.ADD_PLAYLIST
                        || mainViewModel.currentPlaylist?.name == mainViewModel.playlists[idx + 1].name
                    ) {
                        mainViewModel.makeReservedPlaylistCurrent()
                        changeActivityState(activityState)
                    }

                    mainViewModel.deletePlaylist(mainViewModel.playlists[idx + 1])
                    dialog.dismiss()
                }
            )
            .setNegativeButton("Cancel", null)
            .show()
    }
}