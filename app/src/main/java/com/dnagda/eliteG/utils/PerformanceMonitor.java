package com.dnagda.eliteG.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.SystemClock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Advanced performance monitoring utility for EliteG.
 * Tracks app performance metrics and provides optimization insights.
 */
public final class PerformanceMonitor {
    private static final String TAG = "PerformanceMonitor";
    
    // Performance tracking
    private static final Map<String, Long> operationStartTimes = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> operationTotalTimes = new ConcurrentHashMap<>();
    
    // Memory tracking
    private static long lastMemoryCheck = 0;
    private static final long MEMORY_CHECK_INTERVAL = 30000; // 30 seconds
    
    // Performance thresholds
    private static final long SLOW_OPERATION_THRESHOLD_MS = 1000;
    private static final long VERY_SLOW_OPERATION_THRESHOLD_MS = 3000;
    
    // Prevent instantiation
    private PerformanceMonitor() {
        throw new AssertionError("PerformanceMonitor class should not be instantiated");
    }
    
    /**
     * Start tracking an operation
     */
    public static void startOperation(String operationName) {
        long startTime = SystemClock.elapsedRealtime();
        operationStartTimes.put(operationName, startTime);
        
        // Initialize counters if needed
        operationCounts.putIfAbsent(operationName, new AtomicLong(0));
        operationTotalTimes.putIfAbsent(operationName, new AtomicLong(0));
        
        Logger.d(TAG, "Started tracking operation: " + operationName);
    }
    
    /**
     * End tracking an operation and log performance
     */
    public static long endOperation(String operationName) {
        Long startTime = operationStartTimes.remove(operationName);
        if (startTime == null) {
            Logger.w(TAG, "No start time found for operation: " + operationName);
            return 0;
        }
        
        long duration = SystemClock.elapsedRealtime() - startTime;
        
        // Update statistics
        operationCounts.get(operationName).incrementAndGet();
        operationTotalTimes.get(operationName).addAndGet(duration);
        
        // Log performance based on duration
        logOperationPerformance(operationName, duration);
        
        return duration;
    }
    
    /**
     * Log operation performance with appropriate level
     */
    private static void logOperationPerformance(String operationName, long duration) {
        if (duration >= VERY_SLOW_OPERATION_THRESHOLD_MS) {
            Logger.w(TAG, "VERY SLOW operation '" + operationName + "': " + duration + "ms");
        } else if (duration >= SLOW_OPERATION_THRESHOLD_MS) {
            Logger.w(TAG, "Slow operation '" + operationName + "': " + duration + "ms");
        } else {
            Logger.d(TAG, "Operation '" + operationName + "' completed in " + duration + "ms");
        }
    }
    
    /**
     * Get average operation time
     */
    public static double getAverageOperationTime(String operationName) {
        AtomicLong count = operationCounts.get(operationName);
        AtomicLong totalTime = operationTotalTimes.get(operationName);
        
        if (count == null || totalTime == null || count.get() == 0) {
            return 0.0;
        }
        
        return (double) totalTime.get() / count.get();
    }
    
    /**
     * Check and log memory usage if interval has passed
     */
    public static void checkMemoryUsage(Context context) {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastMemoryCheck < MEMORY_CHECK_INTERVAL) {
            return;
        }
        
        lastMemoryCheck = currentTime;
        
        // Get memory info
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            activityManager.getMemoryInfo(memInfo);
            
            long availableMemory = memInfo.availMem / (1024 * 1024); // MB
            long totalMemory = memInfo.totalMem / (1024 * 1024); // MB
            long usedMemory = totalMemory - availableMemory;
            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
            
            Logger.d(TAG, String.format("Memory usage: %d/%d MB (%.1f%%), Low memory: %s", 
                usedMemory, totalMemory, memoryUsagePercent, memInfo.lowMemory));
            
            // Warn if memory usage is high
            if (memoryUsagePercent > 85.0) {
                Logger.w(TAG, "High memory usage detected: " + String.format("%.1f%%", memoryUsagePercent));
            } else if (memInfo.lowMemory) {
                Logger.w(TAG, "System is in low memory state");
            }
        }
        
        // App-specific memory info
        logAppMemoryUsage();
    }
    
    /**
     * Log detailed app memory usage
     */
    private static void logAppMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024); // MB
        long totalMemory = runtime.totalMemory() / (1024 * 1024); // MB
        long freeMemory = runtime.freeMemory() / (1024 * 1024); // MB
        long usedMemory = totalMemory - freeMemory;
        
        // Native heap info
        long nativeUsed = Debug.getNativeHeapAllocatedSize() / (1024 * 1024); // MB
        long nativeTotal = Debug.getNativeHeapSize() / (1024 * 1024); // MB
        
        Logger.d(TAG, String.format("App memory - Used: %d MB, Total: %d MB, Max: %d MB", 
            usedMemory, totalMemory, maxMemory));
        Logger.d(TAG, String.format("Native heap - Used: %d MB, Total: %d MB", 
            nativeUsed, nativeTotal));
        
        // Warn if app is using too much memory
        double appMemoryUsagePercent = (double) usedMemory / maxMemory * 100;
        if (appMemoryUsagePercent > 80.0) {
            Logger.w(TAG, "High app memory usage: " + String.format("%.1f%%", appMemoryUsagePercent));
        }
    }
    
    /**
     * Get performance statistics for all operations
     */
    public static String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("Performance Report:\n");
        report.append("==================\n");
        
        for (Map.Entry<String, AtomicLong> entry : operationCounts.entrySet()) {
            String operation = entry.getKey();
            long count = entry.getValue().get();
            double avgTime = getAverageOperationTime(operation);
            
            report.append(String.format("%s: %d calls, avg %.1fms\n", 
                operation, count, avgTime));
        }
        
        return report.toString();
    }
    
    /**
     * Reset all performance statistics
     */
    public static void resetStatistics() {
        operationStartTimes.clear();
        operationCounts.clear();
        operationTotalTimes.clear();
        lastMemoryCheck = 0;
        
        Logger.d(TAG, "Performance statistics reset");
    }
    
    /**
     * Check if device performance is adequate for the app
     */
    public static boolean isDevicePerformanceAdequate(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return true;
        
        // Check available memory
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        
        long availableMemoryMB = memInfo.availMem / (1024 * 1024);
        boolean hasAdequateMemory = availableMemoryMB > 512; // Minimum 512MB free
        
        // Check if device is low-end
        boolean isLowEndDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && 
                                activityManager.isLowRamDevice();
        
        Logger.d(TAG, String.format("Device performance check - Memory: %d MB, Low-end: %s", 
            availableMemoryMB, isLowEndDevice));
        
        return hasAdequateMemory && !isLowEndDevice;
    }
    
    /**
     * Monitor frame rate performance
     */
    public static void startFrameRateMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // This would be implemented with Choreographer for real frame rate monitoring
            Logger.d(TAG, "Frame rate monitoring started");
        }
    }
    
    /**
     * Automatic operation timing using try-with-resources pattern
     */
    public static class OperationTimer implements AutoCloseable {
        private final String operationName;
        
        public OperationTimer(String operationName) {
            this.operationName = operationName;
            startOperation(operationName);
        }
        
        @Override
        public void close() {
            endOperation(operationName);
        }
    }
    
    /**
     * Convenience method for automatic operation timing
     */
    public static OperationTimer time(String operationName) {
        return new OperationTimer(operationName);
    }
}
