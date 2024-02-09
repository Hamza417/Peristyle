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

public class BootReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
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
        SharedPreferences.INSTANCE.init(context);
        int interval = Integer.parseInt(MainPreferences.INSTANCE.getAutoWallpaperInterval());
        
        if (interval > 0) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context.getApplicationContext(), AutoWallpaperService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            
            long currentTimeMillis = System.currentTimeMillis();
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, currentTimeMillis, interval, pendingIntent);
            
            Log.d("BootReceiver", "Auto wallpaper alarm set for every " + MainPreferences.INSTANCE.getAutoWallpaperInterval() + " ms");
        } else {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context.getApplicationContext(), AutoWallpaperService.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);
            Log.d("BootReceiver", "Auto wallpaper alarm cancelled");
        }
    }
}
