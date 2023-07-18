package app.simple.waller.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import app.simple.waller.databinding.AdapterWallpaperBinding
import app.simple.waller.glide.GlideUtils.loadWallpaper
import app.simple.waller.models.Wallpaper

class AdapterWallpaper(private val wallpapers: ArrayList<Wallpaper>) : RecyclerView.Adapter<AdapterWallpaper.WallpaperViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val binding = AdapterWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WallpaperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(wallpapers[position])
    }

    override fun getItemCount(): Int {
        return wallpapers.size
    }

    inner class WallpaperViewHolder(private val binding: AdapterWallpaperBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wallpaper: Wallpaper) {
//            val ratio = String.format("%d:%d", wallpaper.width, wallpaper.height)
//            set.clone(binding.wallpaperContainer)
//            set.setDimensionRatio(binding.wallpaperContainer.id, ratio)
//            set.applyTo(binding.wallpaperContainer)
            binding.wallpaperImageView.apply {
                layoutParams.width = wallpaper.width?.div(2)!!
                layoutParams.height = wallpaper.height?.div(2)!!
                loadWallpaper(wallpaper)
            }
        }
    }
}