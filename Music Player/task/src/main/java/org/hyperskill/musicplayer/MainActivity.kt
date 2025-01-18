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
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var searchBtn: Button;
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar;
    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var mainSongList: RecyclerView

    lateinit var activityState: MainActivityState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindUI()
        changeActivityState(MainActivityState.PLAY_MUSIC)
    }

    fun changeActivityState(state: MainActivityState) {
        if (this::activityState.isInitialized && activityState == state) return
        activityState = state

        when (state) {
            MainActivityState.PLAY_MUSIC -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.mainFragmentContainer, MainPlayerControllerFragment()
                ).commit()
            }

            MainActivityState.ADD_PLAYLIST -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.mainFragmentContainer, MainAddPlaylistFragment()
                ).commit()
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