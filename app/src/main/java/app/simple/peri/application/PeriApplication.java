package app.simple.peri.application;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

import app.simple.peri.utils.WallpaperSort;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class PeriApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        WallpaperSort.INSTANCE.setSeed(System.currentTimeMillis());
    }
    
}
