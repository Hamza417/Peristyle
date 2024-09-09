package app.simple.peri.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

public class StatusBarHeight {
    /**
     * Get status bar height using window object
     *
     * @param window instance of the activity
     * @return int
     */
    public static int getStatusBarHeight(Window window) {
        Rect rectangle = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top - window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }
    
    public static int getRotation(Context context) {
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            return Configuration.ORIENTATION_PORTRAIT;
        }
        
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            return Configuration.ORIENTATION_LANDSCAPE;
        }
        
        return -1;
    }
    
    public static boolean isLandscape(Context context) {
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            return false;
        }
        
        return rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
    }
    
    public static int getDisplayHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
    
    public static int getDisplayWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
