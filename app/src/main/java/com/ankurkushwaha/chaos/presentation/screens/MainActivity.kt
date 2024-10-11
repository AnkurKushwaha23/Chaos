package com.ankurkushwaha.chaos.presentation.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isPostNotificationPermissionGranted = false

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private lateinit var bottomNav: BottomNavigationView


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
    @SuppressLint("InlinedApi")
    private fun requestPermissions() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadPermissionGranted =
                    permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: isReadPermissionGranted
                isPostNotificationPermissionGranted =
                    permissions[android.Manifest.permission.POST_NOTIFICATIONS]
                        ?: isPostNotificationPermissionGranted

                // Check if the necessary permissions are granted
                if (isReadPermissionGranted) {
                    loadFragment(SongFragment())
                }
            }

        val permissionsToRequest = mutableListOf<String>().apply {

            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
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
