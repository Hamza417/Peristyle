package app.simple.peri.adapters

import android.annotation.SuppressLint
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.peri.R
import app.simple.peri.databinding.AdapterWallpaperBinding
import app.simple.peri.glide.utils.GlideUtils.loadWallpaper
import app.simple.peri.interfaces.WallpaperCallbacks
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.utils.WallpaperSort.getSortedList
import com.bumptech.glide.Glide

class AdapterWallpaper(private val wallpapers: ArrayList<Wallpaper>,
                       private val displayWidth: Int,
                       private val displayHeight: Int) : RecyclerView.Adapter<AdapterWallpaper.WallpaperViewHolder>() {

    private val set = ConstraintSet()
    private var wallpaperCallbacks: WallpaperCallbacks? = null
    private var isMarginLayout = MainPreferences.getMarginBetween()

    var selectionMode = false
        set(value) {
            if (field != value) {
                field = value
                for (i in 0 until wallpapers.size) {
                    notifyItemChanged(i)
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val binding = AdapterWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WallpaperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        if (MainPreferences.getGridSpan() == MainPreferences.SPAN_RANDOM) {
            if (holder.bindingAdapterPosition % 5 == 0) {
                val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = true
                holder.itemView.layoutParams = layoutParams
            } else {
                val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = false
                holder.itemView.layoutParams = layoutParams
            }
        } else {
            val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.isFullSpan = false
            holder.itemView.layoutParams = layoutParams
        }

        holder.bind(wallpapers[holder.bindingAdapterPosition])
    }

    override fun getItemCount(): Int {
        return wallpapers.size
    }

    override fun onViewRecycled(holder: WallpaperViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context).clear(holder.itemView)
    }

    fun setWallpaperCallbacks(wallpaperCallbacks: WallpaperCallbacks) {
        this.wallpaperCallbacks = wallpaperCallbacks
    }

    /**
     * Find best index for wallpaper
     * based on the sort parameters
     */
    fun addWallpaper(wallpaper: Wallpaper) {
        wallpapers.add(wallpaper)
        wallpapers.getSortedList()
        notifyItemInserted(wallpapers.indexOf(wallpaper))
        notifyItemRangeChanged(wallpapers.indexOf(wallpaper), wallpapers.size)
        Log.d("Wallpaper", "Added wallpaper: $wallpaper at index: ${wallpapers.indexOf(wallpaper)}")
    }

    fun removeWallpaper(wallpaper: Wallpaper) {
        val idx = this.wallpapers.indexOf(wallpaper)
        this.wallpapers.remove(wallpaper)
        notifyItemRemoved(idx)
        notifyItemRangeChanged(idx, this.wallpapers.size)
    }

    fun updateLayout() {
        for (i in 0 until wallpapers.size) {
            Log.d("Wallpaper", "Wallpaper: ${wallpapers[i]} at index: $i")
            notifyItemChanged(i)
        }
    }

    fun getRandomWallpaper(): Wallpaper? {
        return if (wallpapers.isNotEmpty()) {
            wallpapers[(0 until wallpapers.size).random()]
        } else {
            null
        }
    }

    fun selectWallpaper(wallpaper: Wallpaper) {
        selectionMode = true
        val idx = wallpapers.indexOf(wallpaper)
        wallpaper.isSelected = true
        wallpapers[idx] = wallpaper
        notifyItemChanged(idx)
    }

    fun getSelectedWallpapers(): ArrayList<Wallpaper> {
        val selectedWallpapers = ArrayList<Wallpaper>()
        for (wallpaper in wallpapers) {
            if (wallpaper.isSelected) {
                selectedWallpapers.add(wallpaper)
            }
        }
        return selectedWallpapers
    }

    @SuppressLint("NotifyDataSetChanged")
    fun cancelSelection() {
        selectionMode = false
        for (wallpaper in wallpapers) {
            wallpaper.isSelected = false
        }
        notifyDataSetChanged()
    }

    fun shuffleWallpapers() {
        wallpapers.shuffle()
        for (i in 0 until wallpapers.size) {
            notifyItemChanged(i)
        }
    }

    fun sortWallpapers() {
        wallpapers.getSortedList()
        for (i in 0 until wallpapers.size) {
            notifyItemChanged(i)
        }
    }

    fun getWallpaper(position: Int): Wallpaper {
        return wallpapers[position]
    }

    fun updateWallpaper(wallpaper: Wallpaper, position: Int) {
        wallpapers[position] = wallpaper
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMarginLayout(marginLayout: Boolean) {
        isMarginLayout = marginLayout
        notifyDataSetChanged()
    }

    inner class WallpaperViewHolder(private val binding: AdapterWallpaperBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wallpaper: Wallpaper) {
            binding.wallpaperContainer.transitionName = wallpaper.uri
            val width = if (MainPreferences.getGridSpan() == MainPreferences.SPAN_RANDOM) {
                if (bindingAdapterPosition % 5 == 0) {
                    wallpaper.width!!
                } else {
                    displayWidth
                }
            } else {
                wallpaper.width
            }

            val height = if (MainPreferences.getGridSpan() == MainPreferences.SPAN_RANDOM) {
                if (bindingAdapterPosition % 5 == 0) {
                    wallpaper.height!!
                } else {
                    displayHeight
                }
            } else {
                wallpaper.height
            }

            val ratio = String.format("%d:%d", width, height)
            set.clone(binding.wallpaperContainer)
            set.setDimensionRatio(binding.wallpaperImageView.id, ratio)
            set.applyTo(binding.wallpaperContainer)

            if (isMarginLayout) {
                val margin = binding.root.resources.getDimensionPixelSize(R.dimen.margin_8dp).div(2)
                val marginLayoutParams = binding.wallpaperContainer.layoutParams as ViewGroup.MarginLayoutParams
                marginLayoutParams.setMargins(margin, margin, margin, margin)
                binding.wallpaperContainer.layoutParams = marginLayoutParams
            } else {
                val marginLayoutParams = binding.wallpaperContainer.layoutParams as ViewGroup.MarginLayoutParams
                marginLayoutParams.setMargins(0, 0, 0, 0)
                binding.wallpaperContainer.layoutParams = marginLayoutParams
            }

            if (MainPreferences.getGridSpan() == MainPreferences.SPAN_RANDOM) {
                binding.wallpaperImageView.scaleType = if (bindingAdapterPosition % 5 == 0) {
                    ImageView.ScaleType.FIT_XY
                } else {
                    ImageView.ScaleType.CENTER_CROP
                }
            } else {
                binding.wallpaperImageView.scaleType = ImageView.ScaleType.FIT_XY
            }

            binding.wallpaperImageView.post {
                binding.wallpaperImageView.loadWallpaper(wallpaper)
            }

            if (MainPreferences.getName()) {
                binding.name.text = wallpaper.name
            } else {
                binding.name.visibility = View.GONE
            }

            if (MainPreferences.getDetails()) {
                binding.resolution.visibility = View.VISIBLE

                binding.resolution.text = String.format(
                        "%dx%d • %s",
                        wallpaper.width, wallpaper.height, wallpaper.size.toSize())

                binding.error.layoutParams = (binding.error.layoutParams as ConstraintLayout.LayoutParams).apply {
                    startToEnd = binding.resolution.id
                    bottomToBottom = binding.resolution.id
                    topToTop = binding.resolution.id
                    bottomMargin = 0
                }
            } else {
                binding.resolution.visibility = View.GONE

                // Set constraint start to parent start
                binding.error.layoutParams = (binding.error.layoutParams as ConstraintLayout.LayoutParams).apply {
                    startToStart = ConstraintSet.PARENT_ID
                    bottomToBottom = ConstraintSet.PARENT_ID
                    topToTop = ConstraintSet.PARENT_ID
                    bottomMargin = binding.root.resources.getDimensionPixelSize(R.dimen.margin_8dp)
                }
            }

            when (MainPreferences.getGridSpan()) {
                MainPreferences.SPAN_RANDOM -> {
                    val textMargin = binding.root.resources.getDimensionPixelSize(R.dimen.margin_8dp)
                    if (absoluteAdapterPosition == 0) {
                        val marginLayoutParams = binding.name.layoutParams as ViewGroup.MarginLayoutParams
                        marginLayoutParams.setMargins(textMargin, getStatusBarHeight(binding.name.resources), textMargin, textMargin)
                        binding.name.layoutParams = marginLayoutParams
                    } else {
                        val marginLayoutParams = binding.name.layoutParams as ViewGroup.MarginLayoutParams
                        marginLayoutParams.setMargins(textMargin, textMargin, textMargin, textMargin)
                        binding.name.layoutParams = marginLayoutParams
                    }
                }

                MainPreferences.SPAN_ONE -> {
                    if (absoluteAdapterPosition == 0) {
                        val marginLayoutParams = binding.name.layoutParams as ViewGroup.MarginLayoutParams
                        marginLayoutParams.setMargins(0, getStatusBarHeight(binding.name.resources), 0, 0)
                        binding.name.layoutParams = marginLayoutParams
                    } else {
                        val marginLayoutParams = binding.name.layoutParams as ViewGroup.MarginLayoutParams
                        marginLayoutParams.setMargins(0, 0, 0, 0)
                        binding.name.layoutParams = marginLayoutParams

                    }
                }

                MainPreferences.SPAN_TWO -> {
                    if (absoluteAdapterPosition == 0 || absoluteAdapterPosition == 1) {
                        val marginLayoutParams = binding.name.layoutParams as ViewGroup.MarginLayoutParams
                        marginLayoutParams.setMargins(0, getStatusBarHeight(binding.name.resources), 0, 0)
                        binding.name.layoutParams = marginLayoutParams
                    } else {
                        val marginLayoutParams = binding.name.layoutParams as ViewGroup.MarginLayoutParams
                        marginLayoutParams.setMargins(0, 0, 0, 0)
                        binding.name.layoutParams = marginLayoutParams

                    }
                }
            }

            if (wallpaper.width!! < displayWidth || wallpaper.height!! < displayHeight) {
                binding.error.visibility = View.VISIBLE
            } else {
                binding.error.visibility = View.GONE

                if (MainPreferences.getDetails()) {
                    binding.progressBar.layoutParams = (binding.progressBar.layoutParams as ConstraintLayout.LayoutParams).apply {
                        startToEnd = binding.resolution.id
                        bottomToBottom = binding.resolution.id
                        topToTop = binding.resolution.id
                        bottomMargin = 0
                    }
                } else {
                    binding.progressBar.layoutParams = (binding.progressBar.layoutParams as ConstraintLayout.LayoutParams).apply {
                        startToStart = ConstraintSet.PARENT_ID
                        bottomToBottom = ConstraintSet.PARENT_ID
                        topToTop = ConstraintSet.PARENT_ID
                        bottomMargin = binding.root.resources.getDimensionPixelSize(R.dimen.margin_8dp)
                    }
                }
            }

            if (selectionMode) {
                binding.checkBox.visibility = View.VISIBLE
                binding.checkBox.isChecked = wallpaper.isSelected
            } else {
                binding.checkBox.visibility = View.GONE
                binding.checkBox.isChecked = false
            }

            binding.wallpaperContainer.setOnClickListener {
                if (selectionMode) {
                    binding.checkBox.isChecked = !binding.checkBox.isChecked
                    wallpaper.isSelected = binding.checkBox.isChecked
                    selectionMode = wallpapers.any { wallpaper -> wallpaper.isSelected }
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                    wallpaperCallbacks?.onWallpaperClicked(wallpaper, bindingAdapterPosition, binding.wallpaperContainer)
                }
            }

            binding.wallpaperContainer.setOnLongClickListener {
                /**
                 * Pretty hacky but it works, if we are zooming the wallpaper
                 * we don't want to trigger the long click.
                 */
                if (binding.wallpaperImageView.scaleX == 1.0f) {
                    wallpaperCallbacks?.onWallpaperLongClicked(wallpaper, bindingAdapterPosition, it, binding.checkBox)
                }
                true
            }
        }
    }

    /**
     * Get status bar height using system framework resources
     *
     * @param resources of the android system package
     * @return int
     */
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getStatusBarHeight(resources: Resources): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}