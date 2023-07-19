package app.simple.waller.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import app.simple.waller.databinding.AdapterWallpaperBinding
import app.simple.waller.glide.utils.GlideUtils.loadWallpaper
import app.simple.waller.interfaces.WallpaperCallbacks
import app.simple.waller.models.Wallpaper
import app.simple.waller.utils.FileUtils.toSize
import com.bumptech.glide.Glide

class AdapterWallpaper(private val wallpapers: ArrayList<Wallpaper>) : RecyclerView.Adapter<AdapterWallpaper.WallpaperViewHolder>() {

    private val set = ConstraintSet()
    private var wallpaperCallbacks: WallpaperCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val binding = AdapterWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WallpaperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(wallpapers[position], position)
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

    fun addWallpapers(wallpapers: java.util.ArrayList<Wallpaper>?) {
        wallpapers?.forEach {
            for (wallpaper in this.wallpapers) {
                if (it.dateModified < wallpaper.dateModified) {
                    this.wallpapers.add(this.wallpapers.indexOf(wallpaper), it)
                    notifyItemInserted(this.wallpapers.indexOf(wallpaper))
                    notifyItemRangeChanged(this.wallpapers.indexOf(wallpaper), this.wallpapers.size)
                    break
                }
            }
        }
    }

    fun addWallpaper(wallpaper: Wallpaper) {
        Log.d("Wallpaper", "Adding wallpaper: $wallpaper")
        wallpapers.add(0, wallpaper)
        notifyItemInserted(wallpapers.indexOf(wallpaper))
        notifyItemRangeChanged(wallpapers.indexOf(wallpaper), wallpapers.size)
    }

    fun removeWallpapers(wallpapers: java.util.ArrayList<Wallpaper>?) {
        wallpapers?.forEach {
            Log.d("Wallpaper", "Removing wallpaper: $it")
            val idx = this.wallpapers.indexOf(it)
            Log.d("Wallpaper", "Removing wallpaper at index: $idx")
            this.wallpapers.remove(it)
            notifyItemRemoved(idx)
            notifyItemRangeChanged(idx, this.wallpapers.size)
        }
    }

    fun removeWallpaper(wallpaper: Wallpaper) {
        val idx = this.wallpapers.indexOf(wallpaper)
        this.wallpapers.remove(wallpaper)
        notifyItemRemoved(idx)
        notifyItemRangeChanged(idx, this.wallpapers.size)
    }

    inner class WallpaperViewHolder(private val binding: AdapterWallpaperBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wallpaper: Wallpaper, position: Int) {
            val ratio = String.format("%d:%d", wallpaper.width, wallpaper.height)
            set.clone(binding.wallpaperContainer)
            set.setDimensionRatio(binding.wallpaperImageView.id, ratio)
            set.applyTo(binding.wallpaperContainer)
            binding.wallpaperContainer.transitionName = wallpaper.uri
            binding.wallpaperImageView.loadWallpaper(wallpaper)

            binding.wallpaperContainer.setOnClickListener {
                wallpaperCallbacks?.onWallpaperClicked(wallpaper, position, binding.wallpaperContainer)
            }

            binding.wallpaperContainer.setOnLongClickListener {
                wallpaperCallbacks?.onWallpaperLongClicked(wallpaper, position)
                true
            }

            binding.resolution.text = String.format("%dx%d • %s", wallpaper.width, wallpaper.height, wallpaper.size.toSize())
        }
    }
}