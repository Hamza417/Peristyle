package app.simple.peri.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService
import app.simple.peri.utils.WallpaperServiceNotification

class CopyActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AbstractComposeAutoWallpaperService.ACTION_COPY_ERROR_MESSAGE) {
            val message = intent.getStringExtra(AbstractComposeAutoWallpaperService.EXTRA_ERROR_MESSAGE)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Error Message", message)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Error message copied to clipboard", Toast.LENGTH_SHORT).show()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(WallpaperServiceNotification.ERROR_NOTIFICATION_ID)
        }
    }
}
