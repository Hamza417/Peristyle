package app.simple.peri.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences
import java.util.Calendar

class AutoWallpaperService : Service() {

    private val displayWidth: Int by lazy {
        resources.displayMetrics.widthPixels
    }

    private val displayHeight: Int by lazy {
        resources.displayMetrics.heightPixels
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AutoWallpaperService", "Service started")
        init()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AutoWallpaperService", "Service destroyed")
    }

    private fun init() {
        SharedPreferences.init(this)
        setWallpaper()
        Log.d("AutoWallpaperService", "Wallpaper set")
    }

    private fun setWallpaper() {
        val dir = DocumentFile.fromTreeUri(this, Uri.parse(MainPreferences.getStorageUri()))
        val files = dir?.listFiles()
        files?.random()?.let {
            val wallpaperManager = WallpaperManager.getInstance(this)
            it.uri.let { uri ->
                contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    // Create a new bitmap with the specified width and height and cropped from the original bitmap
                    // from the center of the original bitmap
                    val bitmapCropped = Bitmap.createBitmap(bitmap, (bitmap.width - displayWidth) / 2, (bitmap.height - displayHeight) / 2, displayWidth, displayHeight)
                    wallpaperManager.setBitmap(bitmapCropped, null, true, WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM)
                    bitmapCropped.recycle()
                    setNextAlarm()
                    stopSelf()
                }
            }
        }
    }

    private fun setNextAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AutoWallpaperService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        // Set to 30 minutes from now
        calendar.add(Calendar.MINUTE, 30)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
