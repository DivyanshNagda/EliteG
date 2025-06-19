package com.dnagda.eliteG.utils;

import android.util.Log;
import com.dnagda.eliteG.BuildConfig;

/**
 * Centralized logging utility for EliteG.
 * Provides consistent logging across the application with debug/release handling.
 */
public final class Logger {
    
    private static final String TAG_PREFIX = "EliteG_";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    
    // Prevent instantiation
    private Logger() {
        throw new AssertionError("Logger class should not be instantiated");
    }
    
    /**
     * Log a verbose message
     */
    public static void v(String tag, String message) {
        if (DEBUG) {
            Log.v(TAG_PREFIX + tag, message);
        }
    }
    
    /**
     * Log a verbose message with throwable
     */
    public static void v(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.v(TAG_PREFIX + tag, message, throwable);
        }
    }
    
    /**
     * Log a debug message
     */
    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(TAG_PREFIX + tag, message);
        }
    }
    
    /**
     * Log a debug message with throwable
     */
    public static void d(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.d(TAG_PREFIX + tag, message, throwable);
        }
    }
    
    /**
     * Log an info message
     */
    public static void i(String tag, String message) {
        if (DEBUG) {
            Log.i(TAG_PREFIX + tag, message);
        }
    }
    
    /**
     * Log an info message with throwable
     */
    public static void i(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.i(TAG_PREFIX + tag, message, throwable);
        }
    }
    
    /**
     * Log a warning message
     */
    public static void w(String tag, String message) {
        Log.w(TAG_PREFIX + tag, message);
    }
    
    /**
     * Log a warning message with throwable
     */
    public static void w(String tag, String message, Throwable throwable) {
        Log.w(TAG_PREFIX + tag, message, throwable);
    }
    
    /**
     * Log an error message
     */
    public static void e(String tag, String message) {
        Log.e(TAG_PREFIX + tag, message);
    }
    
    /**
     * Log an error message with throwable
     */
    public static void e(String tag, String message, Throwable throwable) {
        Log.e(TAG_PREFIX + tag, message, throwable);
    }
    
    /**
     * Log ADB command execution
     */
    public static void logAdbCommand(String command) {
        d("ADB", "Executing command: " + command);
    }
    
    /**
     * Log ADB command result
     */
    public static void logAdbResult(String command, int exitCode, String output) {
        if (exitCode == 0) {
            d("ADB", "Command succeeded: " + command + " | Output: " + output);
        } else {
            e("ADB", "Command failed: " + command + " | Exit code: " + exitCode + " | Output: " + output);
        }
    }
    
    /**
     * Log performance metrics
     */
    public static void logPerformance(String operation, long durationMs) {
        d("Performance", operation + " took " + durationMs + "ms");
    }
    
    /**
     * Log memory usage
     */
    public static void logMemoryUsage(String context) {
        if (DEBUG) {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            d("Memory", context + " - Used: " + (usedMemory / 1024 / 1024) + "MB, Max: " + (maxMemory / 1024 / 1024) + "MB");
        }
    }
}