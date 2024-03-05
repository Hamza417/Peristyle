package app.simple.peri.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import app.simple.peri.R
import app.simple.peri.services.AutoWallpaperService

class NextWallpaperWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate")

        // Loop through all widget instances
        appWidgetIds?.forEach { appWidgetId ->
            // Create an Intent to launch MainActivity
            val intent = Intent(context, AutoWallpaperService::class.java)
            intent.action = AutoWallpaperService.ACTION_NEXT_WALLPAPER
            val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // Get the layout for the App Widget and attach an on-click listener to the button
            val views = RemoteViews(context?.packageName, R.layout.widget_next_wallpaper)
            views.setOnClickPendingIntent(R.id.widget_next_wallpaper_container, pendingIntent)

            // Log a message when the widget is clicked
            Log.d(TAG, "Widget clicked")

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager?.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        private const val TAG = "NextWallpaperWidget"
    }
}
