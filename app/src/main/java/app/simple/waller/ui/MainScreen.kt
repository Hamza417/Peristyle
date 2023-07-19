package app.simple.waller.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.waller.R
import app.simple.waller.adapters.AdapterWallpaper
import app.simple.waller.databinding.FragmentMainScreenBinding
import app.simple.waller.interfaces.WallpaperCallbacks
import app.simple.waller.models.Wallpaper
import app.simple.waller.utils.ConditionUtils.isNotNull
import app.simple.waller.viewmodels.WallpaperViewModel

class MainScreen : Fragment() {

    private val wallpaperViewModel: WallpaperViewModel by viewModels()
    private var adapterWallpaper: AdapterWallpaper? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null
    private var binding: FragmentMainScreenBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        wallpaperViewModel.getWallpapersLiveData().observe(requireActivity()) { wallpapers ->
            adapterWallpaper = AdapterWallpaper(wallpapers)
            adapterWallpaper?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

            adapterWallpaper!!.setWallpaperCallbacks(object : WallpaperCallbacks {
                override fun onWallpaperClicked(wallpaper: Wallpaper?, position: Int, constraintLayout: ConstraintLayout?) {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .addSharedElement(constraintLayout!!, constraintLayout.transitionName)
                        .replace(R.id.mainContainer, WallpaperScreen.newInstance(wallpaper!!), "WallpaperScreen")
                        .addToBackStack("WallpaperScreen")
                        .commit()
                }

                override fun onWallpaperLongClicked(wallpaper: Wallpaper, position: Int) {
                    Log.d("Wallpaper", "Long clicked")
                }
            })

            binding?.recyclerView?.setHasFixedSize(true)
            staggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            staggeredGridLayoutManager?.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            binding?.recyclerView?.layoutManager = staggeredGridLayoutManager
            binding?.recyclerView?.adapter = adapterWallpaper

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        wallpaperViewModel.getNewWallpapersLiveData().observe(requireActivity()) { wallpaper ->
            if (wallpaper.isNotNull()) {
                adapterWallpaper?.addWallpaper(wallpaper)
                wallpaperViewModel.getNewWallpapersLiveData().value = null
            }
        }

        wallpaperViewModel.getRemovedWallpapersLiveData().observe(requireActivity()) { wallpaper ->
            if (wallpaper.isNotNull()) {
                adapterWallpaper?.removeWallpaper(wallpaper)
                wallpaperViewModel.getRemovedWallpapersLiveData().value = null
            }
        }
    }

    companion object {
        fun newInstance(): MainScreen {
            val args = Bundle()
            val fragment = MainScreen()
            fragment.arguments = args
            return fragment
        }
    }
}