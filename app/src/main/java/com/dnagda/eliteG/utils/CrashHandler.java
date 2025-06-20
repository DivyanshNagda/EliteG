package com.dnagda.eliteG.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Global crash handler for EliteG application.
 * Provides crash reporting, recovery, and debugging information.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final String CRASH_PREF_NAME = "crash_data";
    private static final String CRASH_COUNT_KEY = "crash_count";
    private static final String LAST_CRASH_TIME_KEY = "last_crash_time";
    private static final int MAX_CRASH_COUNT = 3;
    private static final long CRASH_RESET_TIME = 24 * 60 * 60 * 1000; // 24 hours
    
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    
    public CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    
    /**
     * Install the crash handler
     */
    public static void install(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context));
        Logger.d(TAG, "Crash handler installed");
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            Logger.e(TAG, "Uncaught exception in thread: " + thread.getName(), throwable);
            
            // Record crash information
            recordCrash(throwable);
            
            // Try to restore app state if possible
            attemptRecovery(throwable);
            
        } catch (Exception e) {
            Logger.e(TAG, "Error in crash handler", e);
        } finally {
            // Always call the default handler to ensure proper cleanup
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        }
    }
    
    /**
     * Record crash information for debugging
     */
    private void recordCrash(Throwable throwable) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(CRASH_PREF_NAME, Context.MODE_PRIVATE);
            long currentTime = System.currentTimeMillis();
            long lastCrashTime = prefs.getLong(LAST_CRASH_TIME_KEY, 0);
            int crashCount = prefs.getInt(CRASH_COUNT_KEY, 0);
            
            // Reset crash count if enough time has passed
            if (currentTime - lastCrashTime > CRASH_RESET_TIME) {
                crashCount = 0;
            }
            
            crashCount++;
            
            // Create crash report
            String crashReport = generateCrashReport(throwable, crashCount);
            Logger.e(TAG, "Crash Report #" + crashCount + ":\n" + crashReport);
            
            // Store crash data
            prefs.edit()
                .putInt(CRASH_COUNT_KEY, crashCount)
                .putLong(LAST_CRASH_TIME_KEY, currentTime)
                .putString("last_crash_report", crashReport)
                .apply();
                
        } catch (Exception e) {
            Logger.e(TAG, "Failed to record crash", e);
        }
    }
    
    /**
     * Generate detailed crash report
     */
    private String generateCrashReport(Throwable throwable, int crashCount) {
        StringBuilder report = new StringBuilder();
        
        // Timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        report.append("Crash Time: ").append(dateFormat.format(new Date())).append("\n");
        report.append("Crash Count: ").append(crashCount).append("\n\n");
        
        // Device information
        report.append("Device Info:\n");
        report.append("- Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        report.append("- Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        report.append("- CPU ABI: ").append(Build.SUPPORTED_ABIS[0]).append("\n");
        report.append("- Available Memory: ").append(PerformanceUtils.getAvailableMemoryMB(context)).append("MB\n\n");
        
        // Stack trace
        report.append("Stack Trace:\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        report.append(sw.toString());
        
        return report.toString();
    }
    
    /**
     * Attempt to recover from crash
     */
    private void attemptRecovery(Throwable throwable) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(CRASH_PREF_NAME, Context.MODE_PRIVATE);
            int crashCount = prefs.getInt(CRASH_COUNT_KEY, 0);
            
            if (crashCount >= MAX_CRASH_COUNT) {
                Logger.w(TAG, "Too many crashes, clearing app data for recovery");
                clearAppData();
            }
            
            // If crash is related to settings, try to reset them
            String stackTrace = throwable.toString().toLowerCase();
            if (stackTrace.contains("settings") || stackTrace.contains("preference")) {
                Logger.w(TAG, "Settings-related crash detected, attempting reset");
                resetSettingsData();
            }
            
        } catch (Exception e) {
            Logger.e(TAG, "Failed to attempt recovery", e);
        }
    }
    
    /**
     * Clear app data for recovery
     */
    private void clearAppData() {
        try {
            // Clear SharedPreferences
            SharedPreferences settingsPrefs = context.getSharedPreferences(Constants.SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
            settingsPrefs.edit().clear().apply();
            
            Logger.d(TAG, "App data cleared for crash recovery");
        } catch (Exception e) {
            Logger.e(TAG, "Failed to clear app data", e);
        }
    }
    
    /**
     * Reset settings data specifically
     */
    private void resetSettingsData() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(Constants.SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                .remove(Constants.PREF_LAST_RESOLUTION_SCALE)
                .remove(Constants.PREF_AGGRESSIVE_LMK)
                .remove(Constants.PREF_IS_MURDERER)
                .apply();
                
            Logger.d(TAG, "Settings data reset for crash recovery");
        } catch (Exception e) {
            Logger.e(TAG, "Failed to reset settings data", e);
        }
    }
    
    /**
     * Get crash count from preferences
     */
    public static int getCrashCount(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(CRASH_PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getInt(CRASH_COUNT_KEY, 0);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to get crash count", e);
            return 0;
        }
    }
    
    /**
     * Clear crash data
     */
    public static void clearCrashData(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(CRASH_PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            Logger.d(TAG, "Crash data cleared");
        } catch (Exception e) {
            Logger.e(TAG, "Failed to clear crash data", e);
        }
    }
}
