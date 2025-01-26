package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import org.hyperskill.musicplayer.adapters.SongListAdapter
import org.hyperskill.musicplayer.adapters.SongListSelectableAdapter
import org.hyperskill.musicplayer.databinding.ActivityMainBinding
import org.hyperskill.musicplayer.db.PlaylistDbService
import org.hyperskill.musicplayer.fragments.MainAddPlaylistFragment
import org.hyperskill.musicplayer.fragments.MainPlayerControllerFragment
import org.hyperskill.musicplayer.enums.MainActivityState
import org.hyperskill.musicplayer.enums.TrackState
import org.hyperskill.musicplayer.models.PlaylistModel
import org.hyperskill.musicplayer.models.SongModel
import org.hyperskill.musicplayer.models.TrackModel
import org.hyperskill.musicplayer.services.AudioRequestService
import org.hyperskill.musicplayer.services.PermissionsService
import org.hyperskill.musicplayer.viewModels.MainViewModel
import org.hyperskill.musicplayer.viewModels.MainViewModelFactory

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

        val playlistDbService = PlaylistDbService(this)
        val factory = MainViewModelFactory(playlistDbService)
        mainViewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        setupObservers()
        bindUI()

        runBlocking {
            setupPlaylistsFromDb()
        }

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
                    stopTrack(old)
                    old.track.release()
                }
            }

            songListAdapter?.currentTrack = currentTrack
            val duration = currentTrack?.track?.duration

            if (duration != null && duration >= 0) {
                mainPlayerControllerFragment?.setTrackProgress(duration)
            }


            currentTrack?.track?.apply {
                setOnCompletionListener(null)
                setOnCompletionListener {
                    mainViewModel.updateCurrentTrackStatus(TrackState.STOPPED)
                    songListAdapter?.notifyDataSetChanged()
                }
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

    private suspend fun setupPlaylistsFromDb() {
        val playlistsFromDb = mainViewModel.getInitialPlaylists()

        playlistsFromDb.forEach {
            mainViewModel.addPlaylist(it.key, it.value.map { id -> id.toLong() }, true)
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

    fun addPlaylist(name: String, songIds: List<Long>) {
        mainViewModel.addPlaylist(name, songIds, false)
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
                    MediaPlayer.create(this, AudioRequestService.getUriBySongId(track.id))
                )
            )
        }
    }

    private fun updatePlayMusicAdapter() {
        songListAdapter = SongListAdapter(
            mainViewModel.currentPlaylistSongs,
            mainViewModel.currentTrack.value,
            ::onCurrentTrackClick,
            ::onTrackLongClick
        )
        setupAdapter(songListAdapter!!, LinearLayoutManager(this))
    }

    private fun updateAddPlaylistAdapter() {
        val songs = mainViewModel.currentPlaylistSelectFromSongs
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
            mainViewModel.currentPlaylistSelectFromSongs
                .filter { savedSelectedItems.contains(it.id) }
                .map { it.id }

        songListSelectableAdapter =
            SongListSelectableAdapter(
                mainViewModel.currentPlaylistSelectFromSongs,
                newSelectedItems
            )

        binding.mainSongList.adapter = songListSelectableAdapter
    }

    private fun bindUI() {
        binding.mainButtonSearch.setOnClickListener {
            if (PermissionsService.checkAudioPermission(this)) {
                setReservedPlaylist()
            } else {
                PermissionsService.requestAudioPermission(this)
            }
        }
        setSupportActionBar(binding.toolbar)
        addMenuProvider(MainMenuProvider(this))
    }

    private fun setReservedPlaylist() {
        val songs = AudioRequestService.getAudioFiles(this)

        if (songs.isEmpty()) {
            Toast.makeText(this, "no songs found", Toast.LENGTH_LONG).show()
            return
        }

        val created = PlaylistModel(RESERVED_PLAYLIST_NAME, songs.map { it.id })

        if (activityState == MainActivityState.ADD_PLAYLIST) {
            updateSelectableSongListAdapter(created)
            return
        }

        mainViewModel.setAllSongs(songs)
        mainViewModel.setReservedPlaylist(created)

        if (!songs.contains(mainViewModel.currentTrack.value?.song)) {
            val defaultTrack = songs[0]

            mainViewModel.setCurrentTrack(
                TrackModel(
                    song = defaultTrack,
                    state = TrackState.STOPPED,
                    track = MediaPlayer.create(
                        this,
                        AudioRequestService.getUriBySongId(defaultTrack.id)
                    )
                )
            )
        }

        changeActivityState(MainActivityState.PLAY_MUSIC)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionsService.READ_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setReservedPlaylist()
                changeActivityState(MainActivityState.PLAY_MUSIC)
            } else {
                Toast.makeText(this, "Songs cannot be loaded without permission", Toast.LENGTH_LONG)
                    .show()
            }
        }
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
        val playlistLabels = mainViewModel.playlists.map { it.name }.toMutableList()

        if(!playlistLabels.contains(RESERVED_PLAYLIST_NAME)) {
            playlistLabels.add(0, RESERVED_PLAYLIST_NAME)
        }

        val items = playlistLabels.toTypedArray()

        AlertDialog.Builder(this@MainActivity)
            .setTitle("choose playlist to load")
            .setItems(items, { dialog, idx ->
                if (mainViewModel.songs.isEmpty()) {
                    val songs = AudioRequestService.getAudioFiles(this)

                    if (songs.isEmpty()) return@setItems

                    val created = PlaylistModel(RESERVED_PLAYLIST_NAME, songs.map { it.id })

                    if (activityState == MainActivityState.ADD_PLAYLIST) {
                        updateSelectableSongListAdapter(created)
                        return@setItems
                    }

                    mainViewModel.setAllSongs(songs)
                    mainViewModel.setReservedPlaylist(created)
                }

                val playlistPressedTo = mainViewModel.playlists[idx]

                if (activityState == MainActivityState.ADD_PLAYLIST) {
                    updateSelectableSongListAdapter(playlistPressedTo)
                } else {
                    mainViewModel.updateCurrentPlaylist(playlistPressedTo)

                    if (mainViewModel.currentTrack.value == null
                        || !mainViewModel.currentPlaylistSongs.contains(
                            mainViewModel.currentTrack.value?.song
                        )
                    ) {
                        val defaultSong = mainViewModel.currentPlaylistSongs[0]
                        mainViewModel.setCurrentTrack(
                            TrackModel(
                                song = defaultSong,
                                state = TrackState.STOPPED,
                                track = MediaPlayer.create(
                                    this,
                                    AudioRequestService.getUriBySongId(defaultSong.id)
                                )
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

                    runBlocking {
                        mainViewModel.deletePlaylist(mainViewModel.playlists[idx + 1])
                    }
                    dialog.dismiss()
                }
            )
            .setNegativeButton("Cancel", null)
            .show()
    }
}