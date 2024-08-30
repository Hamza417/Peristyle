package app.simple.peri.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.peri.constants.Misc
import app.simple.peri.databinding.AdapterSystemWallpaperBinding
import app.simple.peri.glide.utils.GlideUtils.loadWallpaper
import app.simple.peri.glide.utils.GlideUtils.loadWallpaperCrossfade
import app.simple.peri.interfaces.WallpaperCallbacks
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.utils.WallpaperSort.getSortedList
import com.bumptech.glide.Glide
import java.util.Locale

class AdapterSystemWallpaper(private val wallpapers: ArrayList<Wallpaper>,
                             val lastWallpaperPosition: Int) : RecyclerView.Adapter<AdapterSystemWallpaper.WallpaperViewHolder>() {

    private val set = ConstraintSet()
    private var wallpaperCallbacks: WallpaperCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val binding = AdapterSystemWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    fun getWallpaper(position: Int): Wallpaper {
        return wallpapers[position]
    }

    fun updateWallpaper(wallpaper: Wallpaper, position: Int) {
        wallpapers[position] = wallpaper
        notifyItemChanged(position)
    }

    inner class WallpaperViewHolder(private val binding: AdapterSystemWallpaperBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wallpaper: Wallpaper) {
            setTransitionName(wallpaper)
            setDimensions(wallpaper)
            setScaleType()
            loadImage(wallpaper)
            setNameVisibility(wallpaper)
            setDetailsVisibility(wallpaper)
            setErrorVisibility(wallpaper)
            setOnClickListeners(wallpaper)
        }

        private fun setTransitionName(wallpaper: Wallpaper) {
            binding.wallpaperContainer.transitionName = wallpaper.uri
        }

        private fun setDimensions(wallpaper: Wallpaper) {
            val width = getWidth(wallpaper)
            val height = getHeight(wallpaper)
            val ratio = String.format(Locale.getDefault(), "%d:%d", width, height)
            set.clone(binding.wallpaperContainer)
            set.setDimensionRatio(binding.wallpaperImageView.id, ratio)
            set.applyTo(binding.wallpaperContainer)
        }

        private fun getWidth(wallpaper: Wallpaper): Int {
            return if (MainPreferences.getGridSpan() == MainPreferences.SPAN_DYNAMIC && bindingAdapterPosition % 5 == 0) {
                wallpaper.width ?: Misc.getDisplayWidth()
            } else {
                Misc.getDisplayWidth()
            }
        }

        private fun getHeight(wallpaper: Wallpaper): Int {
            return if (MainPreferences.getGridSpan() == MainPreferences.SPAN_DYNAMIC && bindingAdapterPosition % 5 == 0) {
                wallpaper.height ?: Misc.getDisplayHeight()
            } else {
                Misc.getDisplayHeight()
            }
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
                binding.resolution.text = String.format(Locale.getDefault(), "%dx%d • %s", wallpaper.width, wallpaper.height, wallpaper.size.toSize())
                binding.resolution.visibility = View.VISIBLE
            } else {
                binding.resolution.visibility = View.GONE
            }
        }

        private fun setErrorVisibility(wallpaper: Wallpaper) {
            when {
                wallpaper.width!! < Misc.getDisplayWidth() || wallpaper.height!! < Misc.getDisplayHeight() -> {
                    binding.error.imageTintList = ColorStateList.valueOf(Color.RED)
                }

                wallpaper.width!! > Misc.getDisplayWidth() || wallpaper.height!! > Misc.getDisplayHeight() -> {
                    binding.error.imageTintList = ColorStateList.valueOf(Color.GRAY)
                }

                else -> {
                    binding.error.visibility = View.GONE
                }
            }
        }

        private fun setOnClickListeners(wallpaper: Wallpaper) {
            binding.wallpaperContainer.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                wallpaperCallbacks?.onWallpaperClicked(wallpaper, bindingAdapterPosition, binding.wallpaperContainer)
            }

            binding.wallpaperContainer.setOnLongClickListener {
                if (binding.wallpaperImageView.scaleX == 1.0f) {
                    wallpaperCallbacks?.onWallpaperLongClicked(wallpaper, bindingAdapterPosition, it)
                }

                true
            }
        }
    }
}
