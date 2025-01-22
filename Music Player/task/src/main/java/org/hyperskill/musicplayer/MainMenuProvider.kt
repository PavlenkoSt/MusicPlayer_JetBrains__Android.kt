package org.hyperskill.musicplayer

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider

class MainMenuProvider(
    private val activity: MainActivity
) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.mainMenuAddPlaylist -> {
                activity.handleAddPlaylistMenuClick()
                return true
            }

            R.id.mainMenuLoadPlaylist -> {
                activity.handleLoadPlaylistMenuClick()
                return true
            }

            R.id.mainMenuDeletePlaylist -> {
                activity.handleDeletePlaylistMenuClick()
                return true
            }

            else -> false
        }
    }
}