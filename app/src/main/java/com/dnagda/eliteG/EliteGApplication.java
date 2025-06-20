package com.dnagda.eliteG;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.dnagda.eliteG.utils.CrashHandler;
import com.dnagda.eliteG.utils.Logger;
import com.dnagda.eliteG.utils.PerformanceMonitor;
import com.dnagda.eliteG.utils.ThreadUtils;

/**
 * Custom Application class for EliteG.
 * Handles global app initialization, crash handling, and performance monitoring.
 */
public class EliteGApplication extends Application {
    private static final String TAG = "EliteGApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "EliteG Application starting...");
        
        // Install global crash handler
        CrashHandler.install(this);
        
        // Enable StrictMode in debug builds for development
        if (BuildConfig.DEBUG) {
            enableStrictMode();
        }
        
        // Initialize performance monitoring
        initializePerformanceMonitoring();
        
        Logger.d(TAG, "EliteG Application initialized successfully");
    }
    
    /**
     * Enable StrictMode for debugging in development builds
     */
    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectCustomSlowCalls()
                .penaltyLog()
                .build());
                
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectActivityLeaks()
                .detectCleartextNetwork()
                .penaltyLog()
                .build());
                
        Logger.d(TAG, "StrictMode enabled for debugging");
    }
    
    /**
     * Initialize performance monitoring
     */
    private void initializePerformanceMonitoring() {
        // Log memory usage at startup
        Logger.logMemoryUsage("Application startup");
        
        // Check device performance capabilities
        boolean isPerformanceAdequate = PerformanceMonitor.isDevicePerformanceAdequate(this);
        Logger.d(TAG, "Device performance adequate: " + isPerformanceAdequate);
        
        // Start frame rate monitoring if supported
        PerformanceMonitor.startFrameRateMonitoring();
        
        // Check if we're recovering from a crash
        int crashCount = CrashHandler.getCrashCount(this);
        if (crashCount > 0) {
            Logger.w(TAG, "App recovered from " + crashCount + " recent crashes");
        }
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Logger.d(TAG, "Base context attached");
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.d(TAG, "Application terminating");
        
        // Cleanup resources
        ExecuteADBCommands.cleanup();
        ThreadUtils.shutdown();
        
        // Log final performance report
        Logger.d(TAG, "Final performance report:\n" + PerformanceMonitor.getPerformanceReport());
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Logger.w(TAG, "Low memory warning received");
        Logger.logMemoryUsage("Low memory warning");
        
        // Force garbage collection
        System.gc();
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Logger.d(TAG, "Memory trim requested, level: " + level);
        
        switch (level) {
            case TRIM_MEMORY_RUNNING_MODERATE:
            case TRIM_MEMORY_RUNNING_LOW:
            case TRIM_MEMORY_RUNNING_CRITICAL:
                Logger.w(TAG, "App running with limited memory");
                break;
            case TRIM_MEMORY_UI_HIDDEN:
                Logger.d(TAG, "UI hidden, app in background");
                break;
            case TRIM_MEMORY_BACKGROUND:
            case TRIM_MEMORY_MODERATE:
            case TRIM_MEMORY_COMPLETE:
                Logger.w(TAG, "App in background with memory pressure");
                // Clear non-essential caches here if any
                break;
        }
    }
}
