package com.dnagda.eliteG.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Toast;

/**
 * Utility class for UI-related operations in EliteG.
 * Provides helper methods for display metrics, screen dimensions, and UI feedback.
 */
public final class UIUtils {
    
    // Prevent instantiation
    private UIUtils() {
        throw new AssertionError("UIUtils class should not be instantiated");
    }
    
    /**
     * Get screen width in pixels (modern API compatible)
     */
    public static int getScreenWidth(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            return windowManager.getCurrentWindowMetrics().getBounds().width();
        } else {
            // Fallback for older devices
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            return size.x;
        }
    }
    
    /**
     * Get screen height in pixels (modern API compatible)
     */
    public static int getScreenHeight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            return windowManager.getCurrentWindowMetrics().getBounds().height();
        } else {
            // Fallback for older devices
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            return size.y;
        }
    }
    
    /**
     * Get screen density DPI
     */
    public static int getScreenDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.densityDpi;
    }
    
    /**
     * Convert dp to pixels
     */
    public static int dpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }
    
    /**
     * Convert pixels to dp
     */
    public static float pxToDp(Context context, int px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / metrics.density;
    }
    
    /**
     * Convert sp to pixels
     */
    public static int spToPx(Context context, float sp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(sp * metrics.scaledDensity);
    }
    
    /**
     * Get dialog width based on screen size
     */
    public static int getDialogWidth(Context context) {
        return Math.round(getScreenWidth(context) * Constants.DIALOG_WIDTH_RATIO);
    }
    
    /**
     * Get dialog height based on screen size and item count
     */
    public static int getDialogHeight(Context context, int itemCount, boolean hasExtraItem) {
        int maxHeight = Math.round(getScreenHeight(context) * Constants.DIALOG_HEIGHT_RATIO);
        int calculatedHeight = itemCount * Constants.GAME_ITEM_HEIGHT + (hasExtraItem ? Constants.GAME_ITEM_HEIGHT : 0);
        return Math.min(maxHeight, calculatedHeight);
    }
    
    /**
     * Show a short toast message
     */
    public static void showToast(Context context, String message) {
        if (context == null) return;
        if (message == null || message.isEmpty()) {
            message = ""; // Handle null/empty gracefully
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show a long toast message
     */
    public static void showLongToast(Context context, String message) {
        if (context == null) return;
        if (message == null || message.isEmpty()) {
            message = ""; // Handle null/empty gracefully
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Show a toast message with custom duration
     */
    public static void showToast(Context context, String message, int duration) {
        if (context == null) return;
        if (message == null || message.isEmpty()) {
            message = ""; // Handle null/empty gracefully
        }
        Toast.makeText(context, message, duration).show();
    }
    
    /**
     * Check if device is in landscape mode
     */
    public static boolean isLandscape(Context context) {
        return getScreenWidth(context) > getScreenHeight(context);
    }
    
    /**
     * Check if device is a tablet
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK)
                >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    
    /**
     * Get status bar height
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    
    /**
     * Get navigation bar height
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    
    /**
     * Check if device has navigation bar (modern API compatible)
     */
    public static boolean hasNavigationBar(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            WindowInsets windowInsets = windowMetrics.getWindowInsets();
            Insets navigationInsets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars());
            return navigationInsets.bottom > 0 || navigationInsets.left > 0 || navigationInsets.right > 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            
            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            display.getRealMetrics(realDisplayMetrics);
            
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            
            return (realDisplayMetrics.heightPixels - displayMetrics.heightPixels) > 0
                    || (realDisplayMetrics.widthPixels - displayMetrics.widthPixels) > 0;
        }
        return false;
    }
    
    /**
     * Format resolution string
     */
    public static String formatResolution(int width, int height) {
        return width + "x" + height;
    }
    
    /**
     * Format FPS percentage string
     */
    public static String formatFpsPercentage(int percentage) {
        return "+" + percentage + "%";
    }
    
    /**
     * Calculate optimal icon size based on screen density
     */
    public static int getOptimalIconSize(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density;
        
        if (density >= 4.0) { // xxxhdpi
            return Constants.LARGE_ICON_SIZE + 40;
        } else if (density >= 3.0) { // xxhdpi
            return Constants.LARGE_ICON_SIZE + 20;
        } else if (density >= 2.0) { // xhdpi
            return Constants.LARGE_ICON_SIZE;
        } else if (density >= 1.5) { // hdpi
            return Constants.ICON_SIZE;
        } else { // mdpi and below
            return Constants.ICON_SIZE - 20;
        }
    }
}