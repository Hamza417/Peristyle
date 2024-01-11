package app.simple.peri.ui

import android.annotation.SuppressLint
import android.app.WallpaperManager
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import app.simple.peri.R
import app.simple.peri.constants.BundleConstants
import app.simple.peri.databinding.FragmentWallpaperScreenBinding
import app.simple.peri.databinding.WallpaperEditBinding
import app.simple.peri.models.Wallpaper
import app.simple.peri.tools.StackBlur
import app.simple.peri.utils.BitmapUtils.changeBitmapContrastBrightness
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.utils.ParcelUtils.parcelable
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

class WallpaperScreen : Fragment() {

    private var binding: FragmentWallpaperScreenBinding? = null
    private var wallpaper: Wallpaper? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWallpaperScreenBinding.inflate(inflater, container, false)

        wallpaper = requireArguments().parcelable(BundleConstants.WALLPAPER)
        binding?.composeView?.transitionName = wallpaper?.uri

        binding?.composeView?.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Set the content of the ComposeView to a @Composable
            // function
            setContent {
                ZoomableGlideImage(
                        model = wallpaper?.uri?.toUri(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                        onLongPress = {
                                            binding?.fab?.performClick()
                                        }
                                )
                            },
                        alignment = Alignment.Center,
                        contentScale = ContentScale.FillHeight
                )
                {
                    it.addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            startPostponedEnterTransition()
                            setRenderEffectOnWallpaper()
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
        fixNavigationBarOverlap()
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
            val list = arrayOf(
                    getString(R.string.home_screen),
                    getString(R.string.lock_screen),
                    getString(R.string.both))

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.set_as_wallpaper)
                .setItems(list) { d, which ->
                    when (which) {
                        0 -> {
                            setWallpaper(WallpaperManager.FLAG_SYSTEM)
                            d.dismiss()
                        }

                        1 -> {
                            setWallpaper(WallpaperManager.FLAG_LOCK)
                            d.dismiss()
                        }

                        2 -> {
                            setWallpaper(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                            d.dismiss()
                        }
                    }
                }
                .setNegativeButton(R.string.close) { d, _ ->
                    d.dismiss()
                }
                .show()
        }

        binding?.fab0?.setOnClickListener {
            val wallpaperEditBinding = WallpaperEditBinding.inflate(layoutInflater)
            wallpaperEditBinding.root.setBackgroundColor(Color.TRANSPARENT)

            wallpaperEditBinding.saturationSlider.value = requireArguments().getFloat(BundleConstants.SATURATION_VALUE, DEFAULT_SATURATION)
            wallpaperEditBinding.contrastSlider.value = requireArguments().getFloat(BundleConstants.CONTRAST_VALUE, DEFAULT_CONTRAST)
            wallpaperEditBinding.brightnessSlider.value = requireArguments().getFloat(BundleConstants.BRIGHTNESS_VALUE, DEFAULT_BRIGHTNESS)
            wallpaperEditBinding.blurSlider.value = requireArguments().getFloat(BundleConstants.BLUR_VALUE, DEFAULT_BLUR)

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
            dialog.window?.attributes = dialog.window?.attributes

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

    private fun setRenderEffectOnWallpaper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val contrast = requireArguments().getFloat(BundleConstants.CONTRAST_VALUE, DEFAULT_CONTRAST)
            val brightness = requireArguments().getFloat(BundleConstants.BRIGHTNESS_VALUE, DEFAULT_BRIGHTNESS)
            val saturation = requireArguments().getFloat(BundleConstants.SATURATION_VALUE, DEFAULT_SATURATION)
            val blur = requireArguments().getFloat(BundleConstants.BLUR_VALUE, DEFAULT_BLUR)

            val blurEffect = RenderEffect.createBlurEffect(blur.toBlur(), blur.toBlur(), Shader.TileMode.MIRROR)
            val colorFilterEffect = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(ColorMatrix().apply {
                set(floatArrayOf(
                        contrast.toContrast(), 0f, 0f, 0f, brightness.toBrightness(),
                        0f, contrast.toContrast(), 0f, 0f, brightness.toBrightness(),
                        0f, 0f, contrast.toContrast(), 0f, brightness.toBrightness(),
                        0f, 0f, 0f, 1f, 0f
                ))

                postConcat(ColorMatrix().apply {
                    setSaturation(saturation.toSaturation())
                })
            }))

            binding?.composeView?.setRenderEffect(RenderEffect.createChainEffect(blurEffect, colorFilterEffect))
        }
    }

    private fun setWallpaper(mode: Int) {
        val loader = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.preparing)
            .setMessage(R.string.copying)
            .show()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val wallpaperManager = WallpaperManager.getInstance(requireContext())
                val contrast = requireArguments().getFloat(BundleConstants.CONTRAST_VALUE, DEFAULT_CONTRAST).toContrast()
                val brightness = requireArguments().getFloat(BundleConstants.BRIGHTNESS_VALUE, DEFAULT_BRIGHTNESS).toBrightness()
                val saturation = requireArguments().getFloat(BundleConstants.SATURATION_VALUE, DEFAULT_SATURATION).toSaturation()
                val blur = requireArguments().getFloat(BundleConstants.BLUR_VALUE, DEFAULT_BLUR).toBlur()

                val bitmap = binding?.composeView?.drawToBitmap()
                    ?.changeBitmapContrastBrightness(contrast, brightness, saturation)

                bitmap?.let {
                    StackBlur().blurRgb(it, blur.toInt())
                }

                withContext(Dispatchers.Main) {
                    kotlin.runCatching {
                        wallpaperManager.setBitmap(bitmap, null, true, mode)
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
     * Function that takes a Float input from 0.0F to 1.0F
     * and returns a Float from -255.0F to 255.0F depending
     * on the input
     */
    private fun Float.toBrightness(): Float {
        return (this - 0.5F) * 510
    }

    private fun Float.toSaturation(): Float {
        return this * 2
    }

    private fun Float.toContrast(): Float {
        return this * 10
    }

    private fun Float.toBlur(): Float {
        return (this * 100).coerceAtLeast(1F)
    }

    /**
     * Making the Navigation system bar not overlapping with the activity
     */
    private fun fixNavigationBarOverlap() {
        ViewCompat.setOnApplyWindowInsetsListener(binding?.fab!!) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            if (binding?.fab?.marginBottom!! < insets.bottom) {
                binding?.fab?.apply {
                    layoutParams = (layoutParams as FrameLayout.LayoutParams).apply {
                        leftMargin += insets.left
                        rightMargin += insets.right
                        topMargin += insets.top
                        bottomMargin += insets.bottom
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

        private const val DEFAULT_SATURATION = 0.5F
        private const val DEFAULT_BRIGHTNESS = 0.5F
        private const val DEFAULT_CONTRAST = 0.1F
        private const val DEFAULT_BLUR = 0F

    }
}