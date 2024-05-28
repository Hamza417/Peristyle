package app.simple.peri.crash;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import androidx.annotation.NonNull;
import app.simple.peri.preferences.CrashPreferences;

/*
 * Ref: https://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application
 */
public class CrashReport implements Thread.UncaughtExceptionHandler {
    
    private final String TAG = getClass().getSimpleName();
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
    
    public CrashReport(Context context) {
        this.context = context;
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    
    public void uncaughtException(@NonNull Thread thread, Throwable throwable) {
        final long crashTimeStamp = System.currentTimeMillis();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        
        throwable.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        
        Utils.create(stacktrace, new File(context.getExternalFilesDir("logs"), "crashLog_" + crashTimeStamp));
        CrashPreferences.INSTANCE.saveCrashLog(crashTimeStamp);
        CrashPreferences.INSTANCE.saveMessage(throwable.toString());
        CrashPreferences.INSTANCE.saveCause(Utils.getCause(throwable).toString());
        Log.e(TAG, "Crash detected: " + stacktrace);
        
        defaultUncaughtExceptionHandler.uncaughtException(thread, throwable);
    }
    
    public void initialize() {
        long timeStamp = CrashPreferences.INSTANCE.getCrashLog();
        Log.d(TAG, "initialize: " + timeStamp + " " + CrashPreferences.CRASH_TIMESTAMP_EMPTY_DEFAULT);
        
        try {
            if (timeStamp != CrashPreferences.CRASH_TIMESTAMP_EMPTY_DEFAULT) {
                String stack = Utils.read(new File(context.getExternalFilesDir("logs"), "crashLog_" + timeStamp));
                
                MaterialAlertDialogBuilder builder = getMaterialAlertDialogBuilder(stack);
                builder.show();
            }
            
            Thread.setDefaultUncaughtExceptionHandler(new CrashReport(context));
        } catch (RuntimeException e) {
            if (context.getExternalFilesDir("logs").delete()) {
                Log.e(TAG, "Crash handler crashed -----> deleted crash logs");
            }
        }
    }
    
    @NonNull
    private MaterialAlertDialogBuilder getMaterialAlertDialogBuilder(String stack) {
        MaterialAlertDialogBuilder builder = getAlertDialogBuilder(stack);
        builder.setNegativeButton("Restart", (dialog, which) -> {
            clearCrashLogs();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            assert intent != null;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            dialog.dismiss();
        });
        return builder;
    }
    
    @NonNull
    private MaterialAlertDialogBuilder getAlertDialogBuilder(String stack) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Crash Detected");
        builder.setMessage("stacktrace: " + stack);
        builder.setPositiveButton("Copy", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("stacktrace", stack);
            clipboard.setPrimaryClip(clip);
            clearCrashLogs();
            dialog.dismiss();
        });
        builder.setNeutralButton("Exit", (dialog, which) -> {
            clearCrashLogs();
            dialog.dismiss();
        });
        return builder;
    }
    
    private void clearCrashLogs() {
        CrashPreferences.INSTANCE.saveCrashLog(CrashPreferences.CRASH_TIMESTAMP_EMPTY_DEFAULT);
        CrashPreferences.INSTANCE.saveMessage(null);
        CrashPreferences.INSTANCE.saveCause(null);
    }
}
