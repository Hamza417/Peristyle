package app.simple.peri.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import app.simple.peri.R
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.ACTION_COPY_ERROR_MESSAGE
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.ACTION_DELETE_WALLPAPER_HOME
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.ACTION_DELETE_WALLPAPER_LOCK
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.EXTRA_ERROR_MESSAGE
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.EXTRA_IS_HOME_SCREEN
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.EXTRA_NOTIFICATION_ID
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.EXTRA_WALLPAPER_PATH
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.TAG
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.receivers.CopyActionReceiver
import app.simple.peri.receivers.WallpaperActionReceiver
import app.simple.peri.utils.ConditionUtils.invert
import java.io.File

object WallpaperServiceNotification {

    private const val CHANNEL_ID_HOME = "wallpaper_home_channel"
    private const val CHANNEL_ID_LOCK = "wallpaper_lock_channel"

    private const val PENDING_INTENT_FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    const val HOME_NOTIFICATION_ID = 1234
    const val LOCK_NOTIFICATION_ID = 5367
    const val ERROR_NOTIFICATION_ID = 12345

    fun Context.createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val homeChannel = NotificationChannel(
                    CHANNEL_ID_HOME,
                    "Home Screen Wallpaper",
                    NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for home screen wallpaper changes"
            }

            val lockChannel = NotificationChannel(
                    CHANNEL_ID_LOCK,
                    "Lock Screen Wallpaper",
                    NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for lock screen wallpaper changes"
            }

            val errorChannel = NotificationChannel(
                    "error_channel",
                    "Error Channel",
                    NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for errors"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(homeChannel)
            notificationManager.createNotificationChannel(lockChannel)
            notificationManager.createNotificationChannel(errorChannel)
        }
    }

    fun Context.showErrorNotification(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (PermissionUtils.checkNotificationPermission(applicationContext).invert()) {
                Log.i(TAG, "Notification permission not granted, skipping notification")
                return
            }
        }

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val copyIntent = Intent(applicationContext, CopyActionReceiver::class.java).apply {
            action = ACTION_COPY_ERROR_MESSAGE
            putExtra(EXTRA_ERROR_MESSAGE, message)
            putExtra(EXTRA_NOTIFICATION_ID, ERROR_NOTIFICATION_ID)
        }

        val copyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
                applicationContext, 0, copyIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, "error_channel")
            .setSmallIcon(R.drawable.ic_peristyle)
            .setContentTitle("Peristyle auto wallpaper service has crashed!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSilent(false)
            .addAction(R.drawable.ic_copy_all, applicationContext.getString(R.string.copy), copyPendingIntent)
            .build()

        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }

    fun Context.showWallpaperChangedNotification(isHomeScreen: Boolean, file: File, bitmap: Bitmap) {
        Log.i(TAG, "Showing notification for wallpaper change for file: ${file.absolutePath}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (PermissionUtils.checkNotificationPermission(applicationContext).invert()) {
                Log.i(TAG, "Notification permission not granted, skipping notification")
                return
            }
        }

        if (MainComposePreferences.getAutoWallpaperNotification().invert()) {
            return
        }

        val channelId = if (isHomeScreen) CHANNEL_ID_HOME else CHANNEL_ID_LOCK
        val notificationId = if (isHomeScreen) HOME_NOTIFICATION_ID else LOCK_NOTIFICATION_ID
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(notificationId) // Clear existing notification

        val deleteIntent = Intent(applicationContext, WallpaperActionReceiver::class.java).apply {
            action = if (isHomeScreen) ACTION_DELETE_WALLPAPER_HOME else ACTION_DELETE_WALLPAPER_LOCK
            putExtra(EXTRA_IS_HOME_SCREEN, isHomeScreen)
            putExtra(EXTRA_WALLPAPER_PATH, file.absolutePath)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }

        val sendIntent = createSendIntent(file, this)

        val deletePendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this, notificationId, deleteIntent, PENDING_INTENT_FLAGS)

        val sendPendingIntent: PendingIntent = PendingIntent.getActivity(
                this, notificationId, Intent.createChooser(sendIntent, null), PENDING_INTENT_FLAGS)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_peristyle)
            .setContentText(applicationContext.getString(
                    R.string.wallpaper_changed,
                    if (isHomeScreen) {
                        applicationContext.getString(R.string.home_screen)
                    } else {
                        applicationContext.getString(R.string.lock_screen)
                    }))
            .addAction(R.drawable.ic_delete, applicationContext.getString(R.string.delete_current_wallpaper), deletePendingIntent)
            .addAction(R.drawable.ic_share, applicationContext.getString(R.string.send), sendPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createSendIntent(file: File, context: Context): Intent {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}