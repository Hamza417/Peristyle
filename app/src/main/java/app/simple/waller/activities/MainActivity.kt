package app.simple.waller.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.waller.adapters.AdapterWallpaper
import app.simple.waller.databinding.ActivityMainBinding
import app.simple.waller.preferences.MainPreferences
import app.simple.waller.preferences.SharedPreferences
import app.simple.waller.viewmodels.WallpaperViewModel

class MainActivity : AppCompatActivity() {

    private val wallpaperViewModel: WallpaperViewModel by viewModels()
    private var adapterWallpaper: AdapterWallpaper? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null
    private var binding: ActivityMainBinding? = null

    private val storageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null) {
            val uri = result.data?.data
            if (uri != null) {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                MainPreferences.setStorageUri(uri.toString())
                Log.d("MainActivity", "Storage Uri: $uri")
                loadWallpaperImages()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        SharedPreferences.init(newBase!!)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (MainPreferences.getStorageUri() == null) {
            Log.d("MainActivity", "Storage Uri: no permission")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            storageResult.launch(intent)
        } else {
            Log.d("MainActivity", "Storage Uri: ${MainPreferences.getStorageUri()}")
            loadWallpaperImages()
        }
    }

    private fun loadWallpaperImages() {
        wallpaperViewModel.getWallpapersLiveData().observe(this) { wallpapers ->
            Log.d("MainActivity", "Wallpapers: ${wallpapers.size}")
            adapterWallpaper = AdapterWallpaper(wallpapers)
            binding?.recyclerView?.setHasFixedSize(true)
            staggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            staggeredGridLayoutManager?.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            binding?.recyclerView?.layoutManager = staggeredGridLayoutManager
            binding?.recyclerView?.adapter = adapterWallpaper
        }
    }
}
