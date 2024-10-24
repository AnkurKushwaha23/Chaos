package com.ankurkushwaha.chaos.presentation.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ankurkushwaha.chaos.R
import com.ankurkushwaha.chaos.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private lateinit var bottomNav: BottomNavigationView

    companion object {
        private const val PERMISSION_REQUEST_CODE = 234
    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        requestPermissions()
        setupUI()
    }

    private fun setupUI() {
        binding.miniPlayerContainerView.visibility = View.GONE
        bottomNav = findViewById(R.id.bottomNav)

        binding.imgMenu.setOnClickListener {
            MenuBottomSheetFragment().show(supportFragmentManager, "CustomBottomSheet")
        }

        binding.imgSearch.setOnClickListener {
            SearchBottomSheetFragment().show(supportFragmentManager, "CustomBottomSheet")
        }

        bottomNav.setOnItemSelectedListener { item ->
            resetNavigationIcons() // Reset all icons to default state
            when (item.itemId) {
                R.id.songsFragment -> {
                    loadFragment(SongFragment())
                }

                R.id.favoritesFragment -> {
                    item.setIcon(R.drawable.ic_favorite_filled) // Set filled favorite icon
                    loadFragment(FavoritesFragment())
                }

                R.id.playlistsFragment -> {
                    loadFragment(PlaylistFragment())
                }

                else -> return@setOnItemSelectedListener false
            }
            true
        }

        // Load the default fragment and set home icon to filled
        loadFragment(SongFragment())
        bottomNav.selectedItemId = R.id.songsFragment
    }

    // Function to load fragments
    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
        return true
    }

    private fun resetNavigationIcons() {
        bottomNav.menu.findItem(R.id.favoritesFragment).setIcon(R.drawable.ic_favorite_border)
    }

    /** permissions*/
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33+)
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                }
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10-12 (API 29-32)
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            else -> {
                // Android 9 (API 28)
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            grantResults.forEachIndexed { index, result ->
                if (result == PackageManager.PERMISSION_GRANTED) {
                    println("Permission ${permissions[index]} granted")
                    loadFragment(SongFragment())
                } else {
                    println("Permission ${permissions[index]} denied")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("OPEN_APP")?.let { fragmentToOpen ->
            if (fragmentToOpen == "Chaos") {
                // Handle the intent, e.g., refresh MainActivity content
            }
        }
    }

    fun miniPlayerVisible(isVisible: Boolean) {
        binding.miniPlayerContainerView.isVisible = isVisible
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}