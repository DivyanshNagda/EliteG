package com.dnagda.eliteG.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

/**
 * Utility class for performance-related operations in EliteG.
 * Provides methods for calculating performance metrics and optimizations.
 */
public final class PerformanceUtils {
    
    // Prevent instantiation
    private PerformanceUtils() {
        throw new AssertionError("PerformanceUtils class should not be instantiated");
    }
    
    /**
     * Calculate FPS boost percentage based on resolution scale
     */
    public static int calculateFpsBoost(int resolutionScale) {
        return Math.round(resolutionScale * Constants.FPS_BOOST_MULTIPLIER);
    }
    
    /**
     * Calculate width coefficient for resolution scaling
     */
    public static float calculateWidthCoefficient(int originalWidth) {
        return -originalWidth * Constants.RESOLUTION_COEFFICIENT_MULTIPLIER;
    }
    
    /**
     * Calculate height coefficient for resolution scaling
     */
    public static float calculateHeightCoefficient(int originalHeight) {
        return -originalHeight * Constants.RESOLUTION_COEFFICIENT_MULTIPLIER;
    }
    
    /**
     * Calculate new width based on scale and coefficient
     */
    public static int calculateNewWidth(int originalWidth, float coefficient, int scale) {
        return (int) Math.ceil(coefficient * scale) + originalWidth;
    }
    
    /**
     * Calculate new height based on scale and coefficient
     */
    public static int calculateNewHeight(int originalHeight, float coefficient, int scale) {
        return (int) Math.ceil(coefficient * scale) + originalHeight;
    }
    
    /**
     * Calculate optimal DPI based on resolution scaling
     */
    public static int calculateOptimalDpi(int originalDpi, int originalWidth, int newWidth) {
        return (int) (originalDpi * ((float) newWidth / (float) originalWidth));
    }
    
    /**
     * Check if device has low memory
     */
    public static boolean isLowMemoryDevice(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return activityManager.isLowRamDevice();
            } else {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);
                // Consider devices with less than 1GB RAM as low memory
                return memoryInfo.totalMem < 1024 * 1024 * 1024;
            }
        }
        return false;
    }
    
    /**
     * Get available memory in MB
     */
    public static long getAvailableMemoryMB(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo.availMem / (1024 * 1024);
        }
        return 0;
    }
    
    /**
     * Get total memory in MB
     */
    public static long getTotalMemoryMB(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo.totalMem / (1024 * 1024);
        }
        return 0;
    }
    
    /**
     * Calculate memory usage percentage
     */
    public static int getMemoryUsagePercentage(Context context) {
        long total = getTotalMemoryMB(context);
        long available = getAvailableMemoryMB(context);
        if (total > 0) {
            return (int) ((total - available) * 100 / total);
        }
        return 0;
    }
    
    /**
     * Check if resolution scaling is recommended based on device specs
     */
    public static boolean isResolutionScalingRecommended(Context context, int currentWidth, int currentHeight) {
        // Recommend scaling for high-resolution devices or low-memory devices
        boolean isHighResolution = (currentWidth * currentHeight) > (1920 * 1080);
        boolean isLowMemory = isLowMemoryDevice(context);
        
        return isHighResolution || isLowMemory;
    }
    
    /**
     * Get recommended resolution scale based on device specs
     */
    public static int getRecommendedResolutionScale(Context context, int currentWidth, int currentHeight) {
        if (!isResolutionScalingRecommended(context, currentWidth, currentHeight)) {
            return Constants.DEFAULT_RESOLUTION_SCALE;
        }
        
        long totalMemory = getTotalMemoryMB(context);
        int pixelCount = currentWidth * currentHeight;
        
        // Scale based on memory and resolution
        if (totalMemory < 2048) { // Less than 2GB RAM
            if (pixelCount > (1920 * 1080)) {
                return 30; // Aggressive scaling for low-memory high-res devices
            } else {
                return 20; // Moderate scaling for low-memory devices
            }
        } else if (totalMemory < 4096) { // 2-4GB RAM
            if (pixelCount > (2560 * 1440)) {
                return 25; // Scale down very high-res displays
            } else {
                return 15; // Light scaling
            }
        } else { // 4GB+ RAM
            if (pixelCount > (3840 * 2160)) {
                return 20; // Scale down 4K displays
            } else {
                return 10; // Minimal scaling for high-end devices
            }
        }
    }
    
    /**
     * Validate resolution scale value
     */
    public static int validateResolutionScale(int scale) {
        return Math.max(Constants.DEFAULT_RESOLUTION_SCALE, 
                Math.min(Constants.MAX_RESOLUTION_SCALE, scale));
    }
    
    /**
     * Check if performance optimizations should be applied automatically
     */
    public static boolean shouldApplyPerformanceOptimizations(Context context) {
        return isLowMemoryDevice(context) || getMemoryUsagePercentage(context) > 80;
    }
    
    /**
     * Get performance profile recommendation
     */
    public static String getPerformanceProfileRecommendation(Context context) {
        if (isLowMemoryDevice(context)) {
            return "Battery Saver";
        } else if (getTotalMemoryMB(context) > 6144) { // 6GB+ RAM
            return "Performance";
        } else {
            return "Balanced";
        }
    }
}