package app.simple.peri.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.addListener
import androidx.core.app.ShareCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.peri.R
import app.simple.peri.activities.SettingsActivity
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
import app.simple.peri.utils.PermissionUtils.isBatteryOptimizationDisabled
import app.simple.peri.utils.PermissionUtils.requestIgnoreBatteryOptimizations
import app.simple.peri.utils.ScreenUtils
import app.simple.peri.utils.ScreenUtils.isLandscape
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
    private var blurAnimator: ValueAnimator? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    private var displayWidth: Int = 0
    private var displayHeight: Int = 0
    private val blurRadius: Float = 25F
    private val blurDuration: Long = 250

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMainBackground()
        postponeEnterTransition()
        allowEnterTransitionOverlap = true
        allowReturnTransitionOverlap = true
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward = */ false)
        binding?.fab?.transitionName = requireArguments().getString(BundleConstants.FAB_TRANSITION)

        with(ScreenUtils.getScreenSize(requireContext())) {
            displayWidth = width
            displayHeight = height
        }

        fixNavigationBarOverlap()
        itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)

        binding?.bottomAppBar?.setOnMenuItemClickListener { it ->
            if (it.itemId != R.id.settings) {
                blurRoot()
            }

            when (it.itemId) {
                R.id.sort -> {
                    val items = arrayOf(getString(R.string.name),
                                        getString(R.string.date),
                                        getString(R.string.size),
                                        getString(R.string.width),
                                        getString(R.string.height))

                    val checkedItem = when (MainPreferences.getSort()) {
                        WallpaperSort.NAME -> 0
                        WallpaperSort.DATE -> 1
                        WallpaperSort.SIZE -> 2
                        WallpaperSort.WIDTH -> 3
                        WallpaperSort.HEIGHT -> 4
                        else -> -1
                    }

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.sort))
                        .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                            when (which) {
                                0 -> MainPreferences.setSort(WallpaperSort.NAME)
                                1 -> MainPreferences.setSort(WallpaperSort.DATE)
                                2 -> MainPreferences.setSort(WallpaperSort.SIZE)
                                3 -> MainPreferences.setSort(WallpaperSort.WIDTH)
                                4 -> MainPreferences.setSort(WallpaperSort.HEIGHT)
                            }

                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setOnDismissListener {
                            unBlurRoot()
                        }
                        .show()
                }

                R.id.order -> {
                    val items = arrayOf(getString(R.string.ascending), getString(R.string.descending))
                    val checkedItem = when (MainPreferences.getOrder()) {
                        WallpaperSort.ASC -> 0
                        WallpaperSort.DESC -> 1
                        else -> -1
                    }

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.order))
                        .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                            when (which) {
                                0 -> MainPreferences.setOrder(WallpaperSort.ASC)
                                1 -> MainPreferences.setOrder(WallpaperSort.DESC)
                            }

                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setOnDismissListener {
                            unBlurRoot()
                        }
                        .show()
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
                                                            adapterWallpaper?.removeWallpaper(wallpaper)
                                                            wallpaperViewModel.removeWallpaper(wallpaper)
                                                        }
                                                    }
                                                }
                                            }

                                            progressDialog.setOnDismissListener {
                                                unBlurRoot()
                                                adapterWallpaper?.cancelSelection()
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
                        if (this.wallpaperViewModel.getWallpapersLiveData().value?.isNotEmpty() == true) {
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
                        } else {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.delete)
                                .setMessage(R.string.no_wallpaper_found)
                                .setNegativeButton(R.string.close) { dialog, _ ->
                                    dialog.dismiss()
                                    unBlurRoot()
                                }
                                .show()
                        }
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
                                    val filesUri = wallpapers?.map { it.uri.toUri() }
                                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
                                    intent.type = "image/*"
                                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(filesUri!!))
                                    startActivity(Intent.createChooser(intent, "Share Wallpapers"))
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
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                }

                R.id.scroll_up -> {
                    unBlurRoot()
                    binding?.recyclerView?.scrollToPosition(0)
                }
            }

            true
        }

        wallpaperViewModel.getWallpapersLiveData().observe(requireActivity()) { wallpapers ->
            if (wallpapers.isNotEmpty()) {
                binding?.loadingStatus?.visibility = View.GONE
            }

            if (MainPreferences.isAutoWallpaperIntervalEnabled()) {
                if (requireContext().isBatteryOptimizationDisabled().invert()) {
                    requireContext().requestIgnoreBatteryOptimizations()
                }
            }

            adapterWallpaper = AdapterWallpaper(wallpapers, displayWidth, displayHeight, requireArguments().getInt(LAST_WALLPAPER_POSITION, -1))
            adapterWallpaper?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

            adapterWallpaper!!.setWallpaperCallbacks(object : WallpaperCallbacks {
                override fun onWallpaperClicked(wallpaper: Wallpaper?, position: Int, constraintLayout: ConstraintLayout?) {
                    // binding?.bottomAppBar?.performHide(false)
                    binding?.fab?.transitionName = null // remove transition name to prevent shared element transition
                    requireArguments().putInt(LAST_WALLPAPER_POSITION, position)
                    openWallpaperScreen(wallpaper!!, constraintLayout!!)
                }

                override fun onWallpaperLongClicked(wallpaper: Wallpaper, position: Int, view: View, checkBox: MaterialCheckBox) {
                    blurRoot()
                    val items = arrayOf(getString(R.string.send), getString(R.string.delete), getString(R.string.select), getString(R.string.reload_metadata), getString(R.string.edit))
                    val itemIds = intArrayOf(R.id.send, R.id.delete, R.id.select, R.id.reload_metadata, R.string.edit)

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Wallpaper Menu")
                        .setItems(items) { dialog, which ->
                            when (itemIds[which]) {
                                R.id.send -> {
                                    // Send
                                    ShareCompat.IntentBuilder(requireActivity())
                                        .setType("image/*")
                                        .setChooserTitle("Share Wallpaper")
                                        .setStream(wallpaper.uri.toUri())
                                        .startChooser()
                                }

                                R.id.delete -> {
                                    // Delete
                                    blurRoot()

                                    MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(R.string.delete)
                                        .setMessage(getString(R.string.delete_message, wallpaper.name))
                                        .setPositiveButton(R.string.delete) { dialog1, _ ->
                                            dialog1.dismiss()

                                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                                if (DocumentFile.fromSingleUri(requireContext(), wallpaper.uri.toUri())?.delete() == true) {
                                                    withContext(Dispatchers.Main) {
                                                        adapterWallpaper?.removeWallpaper(wallpaper)
                                                        wallpaperViewModel.removeWallpaper(wallpaper)
                                                        invalidateLayoutManager()
                                                    }
                                                }
                                            }
                                        }.setNegativeButton(R.string.close) { dialog1, _ ->
                                            dialog1.dismiss()
                                        }
                                        .setOnDismissListener {
                                            unBlurRoot()
                                        }
                                        .show()
                                }

                                R.id.select -> {
                                    // Select
                                    adapterWallpaper?.selectWallpaper(wallpaper)
                                }

                                R.id.reload_metadata -> {
                                    // Reload Metadata
                                    wallpaperViewModel.reloadMetadata(wallpaper) {
                                        adapterWallpaper?.updateWallpaper(wallpaper, position)
                                    }
                                }

                                R.string.edit -> {
                                    // Edit
                                    val intent = Intent(Intent.ACTION_EDIT)
                                    intent.setDataAndType(wallpaper.uri.toUri(), "image/*")
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    startActivity(Intent.createChooser(intent, getString(R.string.edit)))
                                }
                            }
                            dialog.dismiss()
                        }
                        .setOnDismissListener {
                            unBlurRoot()
                        }
                        .show()
                }
            })

            binding?.recyclerView?.setHasFixedSize(false)
            staggeredGridLayoutManager = StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL)
            staggeredGridLayoutManager?.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            binding?.recyclerView?.layoutManager = staggeredGridLayoutManager
            binding?.recyclerView?.adapter = adapterWallpaper
            binding?.recyclerView?.setActivity(requireActivity())
            binding?.recyclerView?.setOriId(R.id.wallpaperImageView)

            if (requireArguments().getBoolean(FIRST_RV_STATE, true)) {
                binding?.recyclerView?.scheduleLayoutAnimation()
                requireArguments().putBoolean(FIRST_RV_STATE, false)
            } else {
                binding?.recyclerView?.layoutAnimation = null
            }

            if (MainPreferences.getSwipeToDelete()) {
                itemTouchHelper?.attachToRecyclerView(binding?.recyclerView)
            } else {
                itemTouchHelper?.attachToRecyclerView(null)
            }

            binding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        MainPreferences.setScrollPosition(
                                (staggeredGridLayoutManager?.findFirstCompletelyVisibleItemPositions(null)?.get(0) ?: 0)
                                    .coerceAtLeast(0))
                    }
                }
            })

            if (MainPreferences.isRememberScrollPosition()) {
                // Make sure this is the first time the fragment is created
                if (requireArguments().getBoolean(BundleConstants.FIRST_RV_STATE, true)) {
                    binding?.recyclerView?.scrollToPosition(MainPreferences.getScrollPosition())
                    requireArguments().putBoolean(BundleConstants.FIRST_RV_STATE, false)
                }
            } else {
                requireArguments().putBoolean(BundleConstants.FIRST_RV_STATE, false)
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        wallpaperViewModel.getNewWallpapersLiveData().observe(viewLifecycleOwner) { wallpaper ->
            if (wallpaper.isNotNull()) {
                adapterWallpaper?.addWallpaper(wallpaper)
                wallpaperViewModel.getNewWallpapersLiveData().value = null
            }
        }

        wallpaperViewModel.getRemovedWallpapersLiveData().observe(viewLifecycleOwner) { wallpaper ->
            if (wallpaper.isNotNull()) {
                adapterWallpaper?.removeWallpaper(wallpaper)
                wallpaperViewModel.getRemovedWallpapersLiveData().value = null
            }
        }

        wallpaperViewModel.getLoadingStatusLiveData().observe(viewLifecycleOwner) { status ->
            if (status == "Done") {
                binding?.loadingStatus?.visibility = View.GONE
            } else {
                binding?.loadingStatus?.text = status
            }
        }

        wallpaperViewModel.getDatabaseLoaded().observe(viewLifecycleOwner) {
            if (it) {
                binding?.progressIndicator?.animate()
                    ?.alpha(0F)
                    ?.setDuration(500)
                    ?.withEndAction {
                        binding?.progressIndicator?.visibility = View.GONE
                    }
                    ?.start()
            } else {
                binding?.progressIndicator?.animate()
                    ?.alpha(1F)
                    ?.setDuration(500)
                    ?.withStartAction {
                        binding?.progressIndicator?.visibility = View.VISIBLE
                    }
                    ?.start()
            }
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

        wallpaperViewModel.getFailedURIs().observe(viewLifecycleOwner) { uris ->
            if (uris.isNotNull()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.failed_files)
                    .setMessage(uris.joinToString("\n"))
                    .setPositiveButton(R.string.delete) { dialog, _ ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                            uris.forEach {
                                with(DocumentFile.fromSingleUri(requireContext(), it.toUri())) {
                                    if (this?.delete() == true) {
                                        Log.d(TAG, "Failed file deleted: $it")
                                    }
                                }
                            }
                        }

                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.close) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        binding?.fab?.setOnClickListener {
            kotlin.runCatching {
                // Pick a random wallpaper from the list
                val randomWallpaper = adapterWallpaper?.getRandomWallpaper()
                binding?.fab?.transitionName = randomWallpaper?.uri.toString()
                requireArguments().putString(BundleConstants.FAB_TRANSITION, randomWallpaper?.uri.toString())

                if (randomWallpaper.isNotNull()) {
                    openWallpaperScreen(randomWallpaper!!, binding?.fab!!)
                } else {
                    throw Exception(getString(R.string.no_wallpaper_found))
                }
            }.onFailure {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.error)
                    .setMessage(it.message ?: it.stackTraceToString())
                    .setPositiveButton(R.string.close) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        binding?.fab?.setOnLongClickListener {
            adapterWallpaper?.shuffleWallpapers()
            true
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

    private val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object
        : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            // Remove swiped item from list and notify the RecyclerView
            val position = viewHolder.bindingAdapterPosition
            val wallpaper = adapterWallpaper?.getWallpaper(position)!!
            if (wallpaper.isNotNull()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete)
                    .setMessage(getString(R.string.delete_message, wallpaper.name))
                    .setPositiveButton(R.string.delete) { dialog, _ ->
                        dialog.dismiss()

                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            if (DocumentFile.fromSingleUri(requireContext(), wallpaper.uri.toUri())?.delete() == true) {
                                withContext(Dispatchers.Main) {
                                    adapterWallpaper?.removeWallpaper(wallpaper)
                                    wallpaperViewModel.removeWallpaper(wallpaper)
                                    invalidateLayoutManager()
                                }
                            }
                        }
                    }.setNegativeButton(R.string.close) { dialog, _ ->
                        dialog.dismiss()
                        adapterWallpaper?.notifyItemChanged(position)
                    }
                    .show()
            }
        }
    }

    private fun openWallpaperScreen(wallpaper: Wallpaper, view: View) {
        saveBottomBarState()
        requireArguments().putBoolean(SHOULD_REFRESH, false)
        if (MainPreferences.getReduceMotion()) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.mainContainer, WallpaperScreen.newInstance(wallpaper), WallpaperScreen.TAG)
                .addToBackStack(WallpaperScreen.TAG)
                .commit()
        } else {
            requireActivity().supportFragmentManager.beginTransaction()
                .addSharedElement(view, view.transitionName)
                .replace(R.id.mainContainer, WallpaperScreen.newInstance(wallpaper), WallpaperScreen.TAG)
                .addToBackStack(WallpaperScreen.TAG)
                .commit()
        }
    }

    private fun blurRoot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (MainPreferences.getBlur()) {
                blurAnimator = ValueAnimator.ofFloat(0.1F, blurRadius)
                blurAnimator?.addUpdateListener {
                    binding?.root?.setRenderEffect(
                            RenderEffect.createBlurEffect(it.animatedValue as Float, it.animatedValue as Float, Shader.TileMode.MIRROR))
                }
                blurAnimator?.addListener(onEnd = {
                    if ((it as ValueAnimator).animatedValue == 0.1F) {
                        binding?.root?.setRenderEffect(null)
                    } else {
                        binding?.root?.setRenderEffect(
                                RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.MIRROR))
                    }
                })
                blurAnimator?.interpolator = AccelerateDecelerateInterpolator()
                blurAnimator?.duration = blurDuration
                blurAnimator?.start()
            }
        }
    }

    private fun unBlurRoot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (MainPreferences.getBlur()) {
                blurAnimator?.reverse()
            }
            binding?.root?.setRenderEffect(null)
        }
    }

    private fun saveBottomBarState() {
        requireArguments().putBoolean(BundleConstants.BOTTOM_APP_BAR, binding?.bottomAppBar?.isScrolledUp!!)
    }

    private fun invalidateLayoutManager() {
        adapterWallpaper?.updateLayout()
    }

    /**
     * Making the Navigation system bar not overlapping with the activity
     */
    private fun fixNavigationBarOverlap() {
        ViewCompat.setOnApplyWindowInsetsListener(binding?.bottomAppBar!!) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            /**
             * Apply the insets as a margin to the view. Here the system is setting
             * only the bottom, left, and right dimensions, but apply whichever insets are
             * appropriate to your layout. You can also update the view padding
             * if that's more appropriate.
             */
            if (binding?.bottomAppBar?.paddingBottom!! < insets.bottom) {
                binding?.bottomAppBar?.apply {
                    setPadding(
                            paddingLeft + insets.left,
                            paddingTop,
                            paddingRight + insets.right,
                            paddingBottom + insets.bottom
                    )
                }
            }

            binding?.fab?.apply {
                // Set margin
                val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.bottomMargin = insets.bottom
                layoutParams.leftMargin = insets.left
                layoutParams.rightMargin = insets.right
                this.layoutParams = layoutParams
            }

            binding?.bottomAppBar?.post {
                if (requireArguments().getBoolean(BundleConstants.BOTTOM_APP_BAR, true)) {
                    binding?.bottomAppBar?.performShow(false)
                } else {
                    binding?.bottomAppBar?.performHide(false)
                }
            }

            /**
             * Return CONSUMED if you don't want want the window insets to keep being
             * passed down to descendant views.
             */
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setMainBackground() {
        if (MainPreferences.getMainScreenBackground() == "0") {
            requireActivity().findViewById<CoordinatorLayout>(R.id.mainContainer).setBackgroundColor(Color.WHITE)
        } else {
            requireActivity().findViewById<CoordinatorLayout>(R.id.mainContainer).setBackgroundColor(Color.BLACK)
        }
    }

    private fun getSpanCount(): Int {
        return when (MainPreferences.SPAN_DYNAMIC) {
            MainPreferences.getGridSpan() -> {
                2
            }

            else -> {
                if (requireContext().isLandscape()) {
                    if (MainPreferences.getGridSpan().toInt() == 1) {
                        1
                    } else {
                        MainPreferences.getGridSpan().toInt().times(2)
                    }
                } else {
                    MainPreferences.getGridSpan().toInt()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerSharedPreferenceChangeListener()
        if (requireArguments().getBoolean(SHOULD_REFRESH, false)) {
            wallpaperViewModel.refreshWallpapers {
                Log.d(TAG, "Wallpapers are already refreshing")
            }
        } else {
            requireArguments().putBoolean(SHOULD_REFRESH, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPreferenceChangeListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        when (p1) {
            MainPreferences.SORT,
            MainPreferences.ORDER -> {
                adapterWallpaper?.sortWallpapers()
            }

            MainPreferences.GRID_SPAN -> {
                staggeredGridLayoutManager?.spanCount = getSpanCount()
                adapterWallpaper?.notifyDataSetChanged()
            }

            MainPreferences.MARGIN_BETWEEN -> {
                adapterWallpaper?.setMarginLayout(MainPreferences.getMarginBetween())
            }

            MainPreferences.NAME,
            MainPreferences.DETAILS -> {
                adapterWallpaper?.notifyDataSetChanged()
            }

            MainPreferences.MAIN_SCREEN_BACKGROUND -> {
                setMainBackground()
            }

            MainPreferences.SWIPE_TO_DELETE -> {
                if (MainPreferences.getSwipeToDelete()) {
                    itemTouchHelper?.attachToRecyclerView(binding?.recyclerView)
                } else {
                    itemTouchHelper?.attachToRecyclerView(null)
                }
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

        private const val TAG = "MainScreen"
        private const val FIRST_RV_STATE = "first_rv_state"
        private const val SHOULD_REFRESH = "should_refresh"
        private const val LAST_WALLPAPER_POSITION = "last_wallpaper_position"
    }
}
