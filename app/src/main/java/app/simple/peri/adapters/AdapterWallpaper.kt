package app.simple.peri.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.peri.R
import app.simple.peri.databinding.AdapterWallpaperBinding
import app.simple.peri.glide.utils.GlideUtils.loadWallpaper
import app.simple.peri.glide.utils.GlideUtils.loadWallpaperCrossfade
import app.simple.peri.interfaces.WallpaperCallbacks
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.utils.WallpaperSort.getSortedList
import com.bumptech.glide.Glide

class AdapterWallpaper(private val wallpapers: ArrayList<Wallpaper>,
                       private val displayWidth: Int,
                       private val displayHeight: Int,
                       val lastWallpaperPosition: Int) : RecyclerView.Adapter<AdapterWallpaper.WallpaperViewHolder>() {

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
        if (MainPreferences.getGridSpan() == MainPreferences.SPAN_DYNAMIC) {
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
            setTransitionName(wallpaper)
            setDimensions(wallpaper)
            setMarginLayout()
            setScaleType()
            loadImage(wallpaper)
            setNameVisibility(wallpaper)
            setDetailsVisibility(wallpaper)
            setErrorVisibility(wallpaper)
            setCheckBoxVisibility(wallpaper)
            setOnClickListeners(wallpaper)
        }

        private fun setTransitionName(wallpaper: Wallpaper) {
            binding.wallpaperContainer.transitionName = wallpaper.uri
        }

        private fun setDimensions(wallpaper: Wallpaper) {
            val width = getWidth(wallpaper)
            val height = getHeight(wallpaper)
            val ratio = String.format("%d:%d", width, height)
            set.clone(binding.wallpaperContainer)
            set.setDimensionRatio(binding.wallpaperImageView.id, ratio)
            set.applyTo(binding.wallpaperContainer)
        }

        private fun getWidth(wallpaper: Wallpaper): Int {
            return if (MainPreferences.getGridSpan() == MainPreferences.SPAN_DYNAMIC && bindingAdapterPosition % 5 == 0) {
                wallpaper.width ?: displayWidth
            } else {
                displayWidth
            }
        }

        private fun getHeight(wallpaper: Wallpaper): Int {
            return if (MainPreferences.getGridSpan() == MainPreferences.SPAN_DYNAMIC && bindingAdapterPosition % 5 == 0) {
                wallpaper.height ?: displayHeight
            } else {
                displayHeight
            }
        }

        private fun setMarginLayout() {
            val marginLayoutParams = binding.wallpaperContainer.layoutParams as ViewGroup.MarginLayoutParams
            if (isMarginLayout) {
                val margin = binding.root.resources.getDimensionPixelSize(R.dimen.margin_8dp).div(2)
                marginLayoutParams.setMargins(margin, margin, margin, margin)
            } else {
                marginLayoutParams.setMargins(0, 0, 0, 0)
            }
            binding.wallpaperContainer.layoutParams = marginLayoutParams
        }

        private fun setScaleType() {
            binding.wallpaperImageView.scaleType = if (MainPreferences.getGridSpan() == MainPreferences.SPAN_DYNAMIC && bindingAdapterPosition % 5 == 0) {
                ImageView.ScaleType.FIT_XY
            } else {
                ImageView.ScaleType.CENTER_CROP
            }
        }

        private fun loadImage(wallpaper: Wallpaper) {
            binding.wallpaperImageView.post {
                if (lastWallpaperPosition == -1 || bindingAdapterPosition != lastWallpaperPosition) {
                    binding.wallpaperImageView.loadWallpaper(wallpaper)
                } else {
                    binding.wallpaperImageView.loadWallpaperCrossfade(wallpaper)
                }
            }
        }

        private fun setNameVisibility(wallpaper: Wallpaper) {
            if (MainPreferences.getName()) {
                binding.name.text = wallpaper.name
                binding.name.visibility = View.VISIBLE
            } else {
                binding.name.visibility = View.GONE
            }
        }

        private fun setDetailsVisibility(wallpaper: Wallpaper) {
            if (MainPreferences.getDetails()) {
                binding.resolution.text = String.format("%dx%d • %s", wallpaper.width, wallpaper.height, wallpaper.size.toSize())
                binding.resolution.visibility = View.VISIBLE
            } else {
                binding.resolution.visibility = View.GONE
            }
        }

        private fun setErrorVisibility(wallpaper: Wallpaper) {
            if (wallpaper.width!! < displayWidth || wallpaper.height!! < displayHeight) {
                binding.error.visibility = View.VISIBLE
            } else {
                binding.error.visibility = View.GONE
            }
        }

        private fun setCheckBoxVisibility(wallpaper: Wallpaper) {
            if (selectionMode) {
                binding.checkBox.isChecked = wallpaper.isSelected
                binding.checkBox.visibility = View.VISIBLE
            } else {
                binding.checkBox.isChecked = false
                binding.checkBox.visibility = View.GONE
            }
        }

        private fun setOnClickListeners(wallpaper: Wallpaper) {
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
                if (binding.wallpaperImageView.scaleX == 1.0f) {
                    wallpaperCallbacks?.onWallpaperLongClicked(wallpaper, bindingAdapterPosition, it, binding.checkBox)
                }
                true
            }
        }
    }
}
