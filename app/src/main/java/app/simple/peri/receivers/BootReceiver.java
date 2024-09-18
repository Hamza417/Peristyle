package app.simple.peri.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import app.simple.peri.BuildConfig;
import app.simple.peri.preferences.MainPreferences;
import app.simple.peri.preferences.SharedPreferences;
import app.simple.peri.services.AutoWallpaperService;

import static android.content.Context.ALARM_SERVICE;

public class BootReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences.INSTANCE.init(context);
        
        if (intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                try {
                    setAutoWallpaperAlarm(context);
                    
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(context, "Peristyle: Auto wallpaper enabled", Toast.LENGTH_SHORT).show();
                    }
                    
                    Log.d("BootReceiver", "Boot completed action received, auto wallpaper enabled");
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(context, "Peristyle: Error setting auto wallpaper", Toast.LENGTH_SHORT).show();
                    }
                    
                    Log.e("BootReceiver", "Error setting auto wallpaper", e);
                }
            }
        }
    }
    
    private void setAutoWallpaperAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AutoWallpaperService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent);
        
        if (Integer.parseInt(MainPreferences.INSTANCE.getAutoWallpaperInterval()) > 0) {
            int interval = Integer.parseInt(MainPreferences.INSTANCE.getAutoWallpaperInterval());
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
            Log.d("MainActivity", "Auto wallpaper alarm set for every " + MainPreferences.INSTANCE.getAutoWallpaperInterval() + " ms");
        } else {
            Log.d("MainActivity", "Auto wallpaper alarm cancelled");
        }
    }
}
