package com.ankurkushwaha.chaos.presentation.screens

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
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
import androidx.lifecycle.lifecycleScope
import com.ankurkushwaha.chaos.R
import com.ankurkushwaha.chaos.databinding.ActivityMainBinding
import com.ankurkushwaha.chaos.services.MusicService
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isPostNotificationPermissionGranted = false

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private lateinit var bottomNav: BottomNavigationView

    private var musicService: MusicService? = null
    private var isBound = false
    private val isPlayingSong = MutableStateFlow<Boolean>(false)


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.isPlaying()?.onEach { isPlaying ->
                isPlayingSong.update { isPlaying }
            }?.launchIn(lifecycleScope)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
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
        bindMusicService()
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

    private fun bindMusicService() {
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
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
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        if (!isPlayingSong.value) {
            stopService(Intent(this, MusicService::class.java))
        }
        _binding = null
    }
}
