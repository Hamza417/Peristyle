package app.simple.peri.ui

import android.app.WallpaperManager
import android.graphics.Rect
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.view.doOnPreDraw
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.peri.R
import app.simple.peri.adapters.AdapterWallpaper
import app.simple.peri.constants.BundleConstants
import app.simple.peri.databinding.FragmentMainScreenBinding
import app.simple.peri.interfaces.WallpaperCallbacks
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ConditionUtils.isNotNull
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.viewmodels.WallpaperViewModel
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis

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
        allowEnterTransitionOverlap = true
        allowReturnTransitionOverlap = true
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ false)
        binding?.fab?.transitionName = requireArguments().getString(BundleConstants.FAB_TRANSITION)

        wallpaperViewModel.getWallpapersLiveData().observe(requireActivity()) { wallpapers ->
            if (wallpapers.isNotEmpty()) {
                binding?.loadingStatus?.visibility = View.GONE
            }

            adapterWallpaper = AdapterWallpaper(wallpapers)
            adapterWallpaper?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

            adapterWallpaper!!.setWallpaperCallbacks(object : WallpaperCallbacks {
                override fun onWallpaperClicked(wallpaper: Wallpaper?, position: Int, constraintLayout: ConstraintLayout?) {
                    binding?.fab?.transitionName = null // remove transition name to prevent shared element transition

                    requireActivity().supportFragmentManager.beginTransaction()
                        .addSharedElement(constraintLayout!!, constraintLayout.transitionName)
                        .replace(R.id.mainContainer, WallpaperScreen.newInstance(wallpaper!!), "WallpaperScreen")
                        .addToBackStack("WallpaperScreen")
                        .commit()
                }

                override fun onWallpaperLongClicked(wallpaper: Wallpaper, position: Int, view: View) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        binding?.root?.setRenderEffect(RenderEffect.createBlurEffect(100F, 100F, Shader.TileMode.MIRROR))
                        view.setRenderEffect(null)
                    }

                    val popup = PopupMenu(requireContext(), view, Gravity.CENTER)
                    popup.menuInflater.inflate(R.menu.wallpaper_menu, popup.menu)

                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.set_as_wallpaper -> {
                                val intent = WallpaperManager.getInstance(requireContext())
                                    .getCropAndSetWallpaperIntent(wallpaper.uri.toUri())
                                startActivity(intent)
                            }

                            R.id.send -> {
                                ShareCompat.IntentBuilder.from(requireActivity())
                                    .setType("image/*")
                                    .setChooserTitle("Share Wallpaper")
                                    .setStream(wallpaper.uri.toUri())
                                    .startChooser()
                            }

                            R.id.delete -> {
                                val documentFile = DocumentFile.fromSingleUri(requireContext(), wallpaper.uri.toUri())
                                documentFile?.delete()
                                adapterWallpaper?.removeWallpaper(wallpaper)
                            }
                        }

                        true
                    }

                    popup.setOnDismissListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            binding?.root?.setRenderEffect(null)
                        }
                    }

                    // Show the popup menu.
                    popup.show()
                }
            })

            val spanCount = resources.getInteger(R.integer.span_count)
            binding?.recyclerView?.setHasFixedSize(true)
            staggeredGridLayoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
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

        wallpaperViewModel.getLoadingStatusLiveData().observe(requireActivity()) { status ->
            binding?.loadingStatus?.text = status
        }

        binding?.fab?.setOnClickListener {
            // Pick a random wallpaper from the list
            val randomWallpaper = adapterWallpaper?.getRandomWallpaper()
            binding?.fab?.transitionName = randomWallpaper?.uri.toString()
            requireArguments().putString(BundleConstants.FAB_TRANSITION, randomWallpaper?.uri.toString())
            if (randomWallpaper.isNotNull()) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .addSharedElement(binding!!.fab, binding!!.fab.transitionName)
                    .replace(R.id.mainContainer, WallpaperScreen.newInstance(randomWallpaper!!), "WallpaperScreen")
                    .addToBackStack("WallpaperScreen")
                    .commit()
            }
        }
    }

    /**
     * Get status bar height using window object
     *
     * @param window instance of the activity
     * @return int
     */
    fun getStatusBarHeight(window: Window): Int {
        val rectangle = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        return rectangle.top - window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
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