package app.simple.peri.ui

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.peri.R
import app.simple.peri.constants.BundleConstants
import app.simple.peri.databinding.FragmentWallpaperScreenBinding
import app.simple.peri.databinding.WallpaperEditBinding
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.tools.StackBlur
import app.simple.peri.utils.BitmapUtils.changeBitmapContrastBrightness
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.utils.ParcelUtils.parcelable
import app.simple.peri.utils.ResourceUtils.getFloatCompat
import app.simple.peri.utils.ScreenUtils.isWallpaperFittingScreen
import app.simple.peri.viewmodels.WallpaperViewModel
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.telephoto.zoomable.glide.ZoomableGlideImage
import java.io.IOException

class WallpaperScreen : Fragment() {

    private var binding: FragmentWallpaperScreenBinding? = null
    private var wallpaper: Wallpaper? = null
    private var drawable: Drawable? = null
    private var bitmap: Bitmap? = null
    private var composeWallpaperViewModel: WallpaperViewModel? = null

    private var wallpaperExportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("image/x-png")) { uri ->
        uri?.let {
            try {
                requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.exported)
                    .setPositiveButton(R.string.close) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } catch (e: IOException) {
                e.printStackTrace()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.error)
                    .setMessage(e.message)
                    .setPositiveButton(R.string.close) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWallpaperScreenBinding.inflate(inflater, container, false)

        wallpaper = requireArguments().parcelable(BundleConstants.WALLPAPER)
        binding?.composeView?.transitionName = wallpaper?.uri
        composeWallpaperViewModel = ViewModelProvider(requireActivity())[WallpaperViewModel::class.java]

        binding?.composeView?.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Set the content of the ComposeView to a @Composable
            // function
            setContent {
                val currentScale = remember {
                    mutableStateOf(ContentScale.Crop)
                }

                ZoomableGlideImage(
                        model = wallpaper?.uri?.toUri(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        alignment = Alignment.Center,
                        contentScale = currentScale.value,
                        onLongClick = {
                            binding?.fab?.performClick()
                        },
                        onClick = {
                            // Set content scale to alternate between crop and fill
                            currentScale.value = if (currentScale.value == ContentScale.Crop) {
                                ContentScale.Inside
                            } else {
                                ContentScale.Crop
                            }
                        }
                )
                {
                    it.addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            startPostponedEnterTransition()
                            setRenderEffectOnWallpaper()
                            drawable = resource
                            return false
                        }
                    })
                        .transition(withCrossFade())
                        .disallowHardwareConfig()
                        .fitCenter()
                }
            }
        }

        return binding?.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<CoordinatorLayout>(R.id.mainContainer).setBackgroundColor(Color.BLACK)
        runCatching {
            fixNavigationBarOverlap()
        }
        postponeEnterTransition()
        allowEnterTransitionOverlap = true
        allowReturnTransitionOverlap = true
        enterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = resources.getInteger(R.integer.animation_duration).toLong()
            scrimColor = Color.TRANSPARENT
        }

        binding?.fab?.setOnClickListener {
            if (MainPreferences.getAutoWallpaperInterval().toInt() > 0) {
                // Since auto wallpaper is on, we'll  show a warning
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.auto_wallpaper_warning)
                    .setPositiveButton(R.string.yes) { dialog, _ ->
                        MainPreferences.turnOffAutoWallpaperInterval()
                        showWallpaperPopup()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        showWallpaperPopup()
                        dialog.dismiss()
                    }
                    .setNeutralButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                showWallpaperPopup()
            }
        }

        binding?.fab0?.setOnClickListener {
            val wallpaperEditBinding = WallpaperEditBinding.inflate(layoutInflater)
            wallpaperEditBinding.root.setBackgroundColor(Color.TRANSPARENT)

            wallpaperEditBinding.saturationSlider.value = requireArguments().getFloat(BundleConstants.SATURATION_VALUE, resources.getFloatCompat(R.dimen.default_saturation))
            wallpaperEditBinding.contrastSlider.value = requireArguments().getFloat(BundleConstants.CONTRAST_VALUE, resources.getFloatCompat(R.dimen.default_contrast))
            wallpaperEditBinding.brightnessSlider.value = requireArguments().getFloat(BundleConstants.BRIGHTNESS_VALUE, resources.getFloatCompat(R.dimen.default_brightness))
            wallpaperEditBinding.hueSlider.value = requireArguments().getFloat(BundleConstants.HUE_VALUE, resources.getFloatCompat(R.dimen.default_hue))
            wallpaperEditBinding.blurSlider.value = requireArguments().getFloat(BundleConstants.BLUR_VALUE, resources.getFloatCompat(R.dimen.default_blur))

            wallpaperEditBinding.saturationSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requireArguments().putFloat(BundleConstants.SATURATION_VALUE, value)
                        setRenderEffectOnWallpaper()
                    }
                }
            }

            wallpaperEditBinding.brightnessSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requireArguments().putFloat(BundleConstants.BRIGHTNESS_VALUE, value)
                        setRenderEffectOnWallpaper()
                    }
                }
            }

            wallpaperEditBinding.contrastSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requireArguments().putFloat(BundleConstants.CONTRAST_VALUE, value)
                        setRenderEffectOnWallpaper()
                    }
                }
            }

            wallpaperEditBinding.hueSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requireArguments().putFloat(BundleConstants.HUE_VALUE, value)
                        setRenderEffectOnWallpaper()
                    }
                }
            }

            wallpaperEditBinding.blurSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requireArguments().putFloat(BundleConstants.BLUR_VALUE, value)
                        setRenderEffectOnWallpaper()
                    }
                }
            }

            val dialog = MaterialAlertDialogBuilder(ContextThemeWrapper(requireContext(), R.style.Theme_BlurryDialog))
                .setView(wallpaperEditBinding.root)
                .show()

            dialog.window?.attributes?.width = requireContext().resources.displayMetrics.widthPixels.times(0.75F).toInt()
            dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dialog.window?.setBackgroundBlurRadius(30)
            }
            dialog.window?.setDimAmount(0F)

            dialog.show()
        }

        /**
         * Remove the edit button if the device is not running
         * on Android 12 or above
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding?.fab0?.visibility = View.VISIBLE
        } else {
            binding?.fab0?.visibility = View.GONE
        }
    }

    private fun showWallpaperPopup() {
        val list = arrayOf(
                getString(R.string.home_screen),
                getString(R.string.lock_screen),
                getString(R.string.both),
                getString(R.string.export))

        if (wallpaper?.isWallpaperFittingScreen(requireContext())!!) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.set_as_wallpaper)
                .setItems(list) { d, which ->
                    setWallpaper(mode = which, shouldCrop = false)
                    d.dismiss()
                }
                .setNegativeButton(R.string.close) { d, _ ->
                    d.dismiss()
                }
                .show()
            return
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.set_as_wallpaper)
                .setItems(list) { d, which ->
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.crop_wallpaper_warning)
                        .setPositiveButton(R.string.yes) { dialog, _ ->
                            setWallpaper(mode = which, shouldCrop = true)
                            d.dismiss()
                            dialog.dismiss()
                        }
                        .setNegativeButton(R.string.no) { dialog, _ ->
                            setWallpaper(mode = which, shouldCrop = false)
                            dialog.dismiss()
                        }
                        .setNeutralButton(R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
                .setNegativeButton(R.string.close) { d, _ ->
                    d.dismiss()
                }
                .show()
        }
    }

    private fun setRenderEffectOnWallpaper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val contrast = requireArguments().getFloat(BundleConstants.CONTRAST_VALUE, resources.getFloatCompat(R.dimen.default_contrast))
            val brightness = requireArguments().getFloat(BundleConstants.BRIGHTNESS_VALUE, resources.getFloatCompat(R.dimen.default_brightness))
            val saturation = requireArguments().getFloat(BundleConstants.SATURATION_VALUE, resources.getFloatCompat(R.dimen.default_saturation))
            val hue = requireArguments().getFloat(BundleConstants.HUE_VALUE, resources.getFloatCompat(R.dimen.default_hue))
            val blur = requireArguments().getFloat(BundleConstants.BLUR_VALUE, resources.getFloatCompat(R.dimen.default_blur)).coerceAtLeast(0.01F)

            val blurEffect = RenderEffect.createBlurEffect(blur, blur, Shader.TileMode.MIRROR)
            val colorFilterEffect = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(ColorMatrix().apply {
                set(floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                ))

                // Set the hue
                postConcat(ColorMatrix().apply {
                    setRotate(0, hue)
                    setRotate(1, hue)
                    setRotate(2, hue)
                })

                //                val cos = cos(hueValue.toDouble())
                //                val sin = sin(hueValue.toDouble())
                //                val lumR = 0.213
                //                val lumG = 0.715
                //                val lumB = 0.072
                //                val mat = floatArrayOf(
                //                        ((lumR + cos * (1 - lumR) + sin * (-lumR)).toFloat()), ((lumG + cos * (-lumG) + sin * (-lumG)).toFloat()), ((lumB + cos * (-lumB) + sin * (1 - lumB)).toFloat()), 0f, 0f,
                //                        ((lumR + cos * (-lumR) + sin * (0.143)).toFloat()), ((lumG + cos * (1 - lumG) + sin * (0.140)).toFloat()), ((lumB + cos * (-lumB) + sin * (-0.283)).toFloat()), 0f, 0f,
                //                        ((lumR + cos * (-lumR) + sin * (-(1 - lumR))).toFloat()), ((lumG + cos * (-lumG) + sin * (lumG)).toFloat()), ((lumB + cos * (1 - lumB) + sin * (lumB)).toFloat()), 0f, 0f,
                //                        0f, 0f, 0f, 1f, 0f
                //                )
                //
                //                postConcat(ColorMatrix(mat))

                postConcat(ColorMatrix().apply {
                    setSaturation(saturation)
                })
            }))

            binding?.composeView?.setRenderEffect(RenderEffect.createChainEffect(blurEffect, colorFilterEffect))
        }
    }

    private fun setWallpaper(mode: Int, shouldCrop: Boolean) {
        val loader = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.preparing)
            .setMessage(R.string.copying)
            .show()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val wallpaperManager = WallpaperManager.getInstance(requireContext())
                val contrast = requireArguments().getFloat(BundleConstants.CONTRAST_VALUE, resources.getFloatCompat(R.dimen.default_contrast))
                val brightness = requireArguments().getFloat(BundleConstants.BRIGHTNESS_VALUE, resources.getFloatCompat(R.dimen.default_brightness))
                val saturation = requireArguments().getFloat(BundleConstants.SATURATION_VALUE, resources.getFloatCompat(R.dimen.default_saturation))
                val hue = requireArguments().getFloat(BundleConstants.HUE_VALUE, resources.getFloatCompat(R.dimen.default_hue))
                val blur = requireArguments().getFloat(BundleConstants.BLUR_VALUE, resources.getFloatCompat(R.dimen.default_blur))

                bitmap = when {
                    shouldCrop -> {
                        binding?.composeView?.drawToBitmap()
                            ?.changeBitmapContrastBrightness(contrast, brightness, saturation, hue)
                    }

                    else -> {
                        drawable?.toBitmap()?.changeBitmapContrastBrightness(contrast, brightness, saturation, hue)
                    }
                }

                bitmap?.let {
                    try {
                        StackBlur().blurRgb(it, blur.toInt())
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    }
                }

                withContext(Dispatchers.Main) {
                    kotlin.runCatching {
                        when (mode) {
                            HOME_SCREEN -> {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                            }

                            LOCK_SCREEN -> {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                            }

                            BOTH -> {
                                /**
                                 * Setting them separately to avoid the wallpaper not setting
                                 * in some devices for lock screen
                                 */
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                            }

                            EXPORT -> {
                                val extension = wallpaper?.name?.substringAfterLast(".")
                                val fileName = wallpaper?.name?.replace(extension!!, "_edited.png")
                                wallpaperExportLauncher.launch(fileName)
                            }
                        }

                        loader.dismiss()
                    }.onFailure {
                        loader.dismiss()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.error)
                            .setMessage(it.message)
                            .setPositiveButton(R.string.close) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }.onFailure {
                it.printStackTrace()
                withContext(Dispatchers.Main) {
                    loader.dismiss()
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(it.message ?: it.stackTraceToString())
                        .setPositiveButton(R.string.close) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    /**
     * Making the Navigation system bar not overlapping with the activity
     */
    private fun fixNavigationBarOverlap() {
        ViewCompat.setOnApplyWindowInsetsListener(binding?.fab!!) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            if (binding?.fab?.marginBottom!! < insets.bottom) {
                binding?.fab?.apply {
                    runCatching {
                        try {
                            layoutParams = (layoutParams as FrameLayout.LayoutParams).apply {
                                leftMargin += insets.left
                                rightMargin += insets.right
                                topMargin += insets.top
                                bottomMargin += insets.bottom
                            }
                        } catch (e: ClassCastException) {
                            layoutParams = (layoutParams as CoordinatorLayout.LayoutParams).apply {
                                leftMargin += insets.left
                                rightMargin += insets.right
                                topMargin += insets.top
                                bottomMargin += insets.bottom
                            }
                        }
                    }
                }
            }

            /**
             * Return CONSUMED if you don't want want the window insets to keep being
             * passed down to descendant views.
             */
            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding?.fab0!!) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            if (binding?.fab0?.marginBottom!! < insets.bottom) {
                binding?.fab0?.apply {
                    runCatching {
                        try {
                            layoutParams = (layoutParams as FrameLayout.LayoutParams).apply {
                                leftMargin += insets.left
                                rightMargin += insets.right
                                topMargin += insets.top
                                bottomMargin += insets.bottom
                            }
                        } catch (e: ClassCastException) {
                            layoutParams = (layoutParams as CoordinatorLayout.LayoutParams).apply {
                                leftMargin += insets.left
                                rightMargin += insets.right
                                topMargin += insets.top
                                bottomMargin += insets.bottom
                            }
                        }
                    }
                }
            }

            /**
             * Return CONSUMED if you don't want want the window insets to keep being
             * passed down to descendant views.
             */
            WindowInsetsCompat.CONSUMED
        }
    }

    companion object {
        fun newInstance(wallpaper: Wallpaper): WallpaperScreen {
            val args = Bundle()
            args.putParcelable(BundleConstants.WALLPAPER, wallpaper)
            val fragment = WallpaperScreen()
            fragment.arguments = args
            return fragment
        }

        private const val HOME_SCREEN = 0
        private const val LOCK_SCREEN = 1
        private const val BOTH = 2
        private const val EXPORT = 3

        const val TAG = "WallpaperScreen"
    }
}
