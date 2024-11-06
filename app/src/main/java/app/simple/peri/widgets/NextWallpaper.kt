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

open class NextWallpaper : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate")

        // Loop through all widget instances
        appWidgetIds?.forEach { appWidgetId ->
            val intent = Intent(context, AutoWallpaperService::class.java)
            intent.action = AutoWallpaperService.ACTION_NEXT_WALLPAPER
            val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // Get the layout for the App Widget and attach an on-click listener to the button
            val views = RemoteViews(context?.packageName, getLayoutID())
            views.setOnClickPendingIntent(getButtonID(), pendingIntent)

            // Log a message when the widget is clicked
            Log.d(TAG, "Widget clicked")

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager?.updateAppWidget(appWidgetId, views)
        }
    }

    open fun getLayoutID(): Int {
        return R.layout.widget_next_wallpaper
    }

    open fun getButtonID(): Int {
        return R.id.widget_next_wallpaper
    }

    companion object {
        private const val TAG = "NextWallpaperWidget"
    }
}
