package app.simple.peri.ui

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.SharedPreferences
import android.graphics.Rect
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.addCallback
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.peri.R
import app.simple.peri.adapters.AdapterWallpaper
import app.simple.peri.constants.BundleConstants
import app.simple.peri.databinding.DialogDeleteBinding
import app.simple.peri.databinding.FragmentMainScreenBinding
import app.simple.peri.interfaces.WallpaperCallbacks
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.peri.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.ConditionUtils.isNotNull
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.utils.WallpaperSort
import app.simple.peri.viewmodels.WallpaperViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainScreen : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val wallpaperViewModel: WallpaperViewModel by viewModels()
    private var adapterWallpaper: AdapterWallpaper? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null
    private var binding: FragmentMainScreenBinding? = null

    private var displayWidth: Int = 0
    private var displayHeight: Int = 0
    private val blurRadius: Float = 75F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        allowEnterTransitionOverlap = true
        allowReturnTransitionOverlap = true
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ false)
        binding?.fab?.transitionName = requireArguments().getString(BundleConstants.FAB_TRANSITION)

        displayWidth = requireContext().resources.displayMetrics.widthPixels
        displayHeight = requireContext().resources.displayMetrics.heightPixels

        binding?.bottomAppBar?.setOnMenuItemClickListener {
            blurRoot()

            when (it.itemId) {
                R.id.sort -> {
                    val popup = PopupMenu(requireContext(), binding?.bottomAppBar?.findViewById(R.id.sort)!!, Gravity.START)
                    popup.menuInflater.inflate(R.menu.wallpaper_sort, popup.menu)

                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.name -> {
                                MainPreferences.setSort(WallpaperSort.NAME)
                            }

                            R.id.date -> {
                                MainPreferences.setSort(WallpaperSort.DATE)
                            }

                            R.id.size -> {
                                MainPreferences.setSort(WallpaperSort.SIZE)
                            }

                            R.id.width -> {
                                MainPreferences.setSort(WallpaperSort.WIDTH)
                            }

                            R.id.height -> {
                                MainPreferences.setSort(WallpaperSort.HEIGHT)
                            }

                            R.id.order -> {
                                blurRoot()

                                val popupOrder = PopupMenu(requireContext(), binding?.bottomAppBar?.findViewById(R.id.sort)!!, Gravity.START)
                                popupOrder.menuInflater.inflate(R.menu.wallpaper_order, popupOrder.menu)

                                popupOrder.setOnMenuItemClickListener { itemOrder ->
                                    when (itemOrder.itemId) {
                                        R.id.ascending -> {
                                            MainPreferences.setOrder(WallpaperSort.ASC)
                                        }

                                        R.id.descending -> {
                                            MainPreferences.setOrder(WallpaperSort.DESC)
                                        }
                                    }

                                    true
                                }

                                popupOrder.setOnDismissListener {
                                    unBlurRoot()
                                }

                                popupOrder.show()
                            }
                        }

                        true
                    }

                    popup.setOnDismissListener {
                        unBlurRoot()
                    }

                    popup.show()
                }

                R.id.delete -> {
                    val wallpapers = adapterWallpaper?.getSelectedWallpapers()
                    val totalWallpapers = wallpapers?.size ?: 0
                    var deleteCount = 0

                    if (wallpapers.isNullOrEmpty().invert()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete)
                            .setMessage(getString(R.string.delete_message, wallpapers?.size.toString()))
                            .setPositiveButton(R.string.delete) { dialog, _ ->
                                blurRoot()

                                if (wallpapers.isNullOrEmpty().invert()) {
                                    if (wallpapers != null) {
                                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                            // Create progress dialog
                                            val dialogDeleteBinding = DialogDeleteBinding.inflate(layoutInflater)
                                            dialogDeleteBinding.progress.text = getString(R.string.preparing)

                                            val progressDialog = MaterialAlertDialogBuilder(requireContext())
                                                .setTitle(R.string.deleting)
                                                .setView(dialogDeleteBinding.root)
                                                .setCancelable(false)
                                                .show()

                                            for (wallpaper in wallpapers) {
                                                withContext(Dispatchers.IO) {
                                                    val documentFile = DocumentFile.fromSingleUri(requireContext(), wallpaper.uri.toUri())
                                                    if (documentFile?.delete() == true) {
                                                        deleteCount++
                                                        withContext(Dispatchers.Main) {
                                                            dialogDeleteBinding.progress.text =
                                                                getString(R.string.delete_progress, deleteCount, totalWallpapers, wallpaper.name)
                                                        }
                                                    }
                                                }

                                                adapterWallpaper?.removeWallpaper(wallpaper)
                                                wallpaperViewModel.removeWallpaper(wallpaper)
                                            }

                                            progressDialog.setOnDismissListener {
                                                unBlurRoot()
                                            }

                                            progressDialog.dismiss()
                                        }
                                    }
                                } else {
                                    adapterWallpaper?.selectionMode = true
                                }

                                dialog.dismiss()
                            }
                            .setNegativeButton(R.string.close) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setOnDismissListener {
                                unBlurRoot()
                            }
                            .show()
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete)
                            .setMessage(R.string.no_wallpaper_selected)
                            .setNegativeButton(R.string.close) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setPositiveButton(R.string.select) { dialog, _ ->
                                adapterWallpaper?.selectionMode = true
                                dialog.dismiss()
                            }
                            .setOnDismissListener {
                                unBlurRoot()
                            }
                            .show()
                    }
                }

                R.id.send -> {
                    val wallpapers = adapterWallpaper?.getSelectedWallpapers()
                    if (wallpapers.isNullOrEmpty().invert()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.send)
                            .setMessage(getString(R.string.send_message, wallpapers?.size.toString()))
                            .setPositiveButton(R.string.send) { dialog, _ ->
                                if (wallpapers.isNullOrEmpty().invert()) {
                                    if (wallpapers != null) {
                                        for (wallpaper in wallpapers) {
                                            ShareCompat.IntentBuilder(requireActivity())
                                                .setType("image/*")
                                                .setChooserTitle("Share Wallpaper")
                                                .setStream(wallpaper.uri.toUri())
                                                .startChooser()
                                        }
                                    }
                                } else {
                                    adapterWallpaper?.selectionMode = true
                                }

                                dialog.dismiss()
                            }
                            .setNegativeButton(R.string.close) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setOnDismissListener {
                                unBlurRoot()
                            }
                            .show()
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.send)
                            .setMessage(R.string.no_wallpaper_selected)
                            .setNegativeButton(R.string.close) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setPositiveButton(R.string.select) { dialog, _ ->
                                adapterWallpaper?.selectionMode = true
                                dialog.dismiss()
                            }
                            .setOnDismissListener {
                                unBlurRoot()
                            }
                            .show()
                    }
                }

                R.id.settings -> {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.mainContainer, Preferences.newInstance(), "Preferences")
                        .addToBackStack("SendScreen")
                        .commit()
                }
            }
            true
        }

        wallpaperViewModel.getWallpapersLiveData().observe(requireActivity()) { wallpapers ->
            if (wallpapers.isNotEmpty()) {
                binding?.loadingStatus?.visibility = View.GONE
            }

            adapterWallpaper = AdapterWallpaper(wallpapers, displayWidth, displayHeight)
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

                override fun onWallpaperLongClicked(wallpaper: Wallpaper, position: Int, view: View, checkBox: MaterialCheckBox) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (MainPreferences.getBlur()) {
                            binding?.recyclerView?.children?.forEach {
                                if (it is ConstraintLayout && it != view) {
                                    it.setRenderEffect(RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP))
                                }
                            }
                        }
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
                                ShareCompat.IntentBuilder(requireActivity())
                                    .setType("image/*")
                                    .setChooserTitle("Share Wallpaper")
                                    .setStream(wallpaper.uri.toUri())
                                    .startChooser()
                            }

                            R.id.delete -> {
                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                                    val documentFile = DocumentFile.fromSingleUri(requireContext(), wallpaper.uri.toUri())
                                    if (documentFile?.delete() == true) {
                                        withContext(Dispatchers.Main) {
                                            adapterWallpaper?.removeWallpaper(wallpaper)
                                            wallpaperViewModel.removeWallpaper(wallpaper)
                                        }
                                    }
                                }
                            }

                            R.id.select -> {
                                adapterWallpaper?.selectWallpaper(wallpaper)
                            }
                        }

                        true
                    }

                    popup.setOnDismissListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            binding?.recyclerView?.children?.forEach {
                                if (it is ConstraintLayout && it != view) {
                                    it.setRenderEffect(null)
                                }
                            }
                        }
                    }

                    // Show the popup menu.
                    popup.show()
                }
            })

            val spanCount = MainPreferences.getGridSpan()
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

        wallpaperViewModel.getIsNomediaDirectoryLiveData().observe(viewLifecycleOwner) {
            if (MainPreferences.getShowNomediaDialog()) {
                if (it) {
                    blurRoot()
                    val documentFile = DocumentFile.fromTreeUri(requireContext(), Uri.parse(MainPreferences.getStorageUri()))

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.nomedia)
                        .setMessage(getString(R.string.nomedia_message, documentFile?.name))
                        .setPositiveButton(R.string.yes) { dialog, _ ->
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                                documentFile?.createFile("*/*", ".nomedia")

                                if (documentFile?.findFile(".nomedia")?.exists() == true) {
                                    withContext(Dispatchers.Main) {
                                        blurRoot()
                                        MaterialAlertDialogBuilder(requireContext())
                                            .setTitle(R.string.nomedia)
                                            .setMessage(R.string.nomedia_success)
                                            .setPositiveButton(R.string.close) { dialog, _ ->
                                                dialog.dismiss()
                                                unBlurRoot()
                                            }
                                            .show()

                                        dialog.dismiss()
                                    }
                                }
                            }
                        }.setNeutralButton(R.string.dont_show_again) { dialog, _ ->
                            MainPreferences.setShowNomediaDialog(false)
                            dialog.dismiss()
                        }.setNegativeButton(R.string.close) { dialog, _ ->
                            dialog.dismiss()
                        }.setOnDismissListener {
                            unBlurRoot()
                        }
                        .show()
                }
            }
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (adapterWallpaper?.selectionMode == true) {
                adapterWallpaper?.cancelSelection()
            } else {
                requireActivity().finish()
            }

            unBlurRoot()
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

    private fun blurRoot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (MainPreferences.getBlur()) {
                binding?.root?.setRenderEffect(
                        RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.MIRROR))
            }
        }
    }

    private fun unBlurRoot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding?.root?.setRenderEffect(null)
        }
    }

    override fun onResume() {
        super.onResume()
        registerSharedPreferenceChangeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPreferenceChangeListener()
    }

    companion object {
        fun newInstance(): MainScreen {
            val args = Bundle()
            val fragment = MainScreen()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        when (p1) {
            MainPreferences.sort,
            MainPreferences.order -> {
                wallpaperViewModel.sortWallpapers()
            }

            MainPreferences.gridSpan -> {
                val spanCount = MainPreferences.getGridSpan()
                staggeredGridLayoutManager?.spanCount = spanCount
            }
        }
    }
}