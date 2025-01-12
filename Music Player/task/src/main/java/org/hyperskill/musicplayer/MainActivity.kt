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

class MainActivity : AppCompatActivity() {
    lateinit var searchBtn: Button;
    lateinit var toolbar: androidx.appcompat.widget.Toolbar;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindUI()
    }

    private fun bindUI() {
        searchBtn = findViewById(R.id.mainButtonSearch)
        toolbar = findViewById(R.id.toolbar)

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