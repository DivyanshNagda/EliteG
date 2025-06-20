package com.dnagda.eliteG;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.dnagda.eliteG.utils.Constants;
import com.dnagda.eliteG.utils.Logger;
import com.dnagda.eliteG.utils.PerformanceUtils;
import com.dnagda.eliteG.utils.UIUtils;

import java.util.List;

/**
 * Enhanced settings manager for EliteG.
 * Handles all application settings with improved error handling and performance.
 */
public class SettingsManager {
    private static final String TAG = "SettingsManager";
    
    // Settings state
    private boolean keepStockDpi;
    private boolean aggressiveLowMemoryKiller;
    private boolean killAllOtherApps;
    private boolean isRoot;
    
    // Core components
    private final SharedPreferences preferences;
    private final Context context; // Use Application context to avoid memory leaks
    private final int[] displayStats = new int[3]; // Width, Height, DPI

    public SettingsManager(Activity activity) {
        // Use Application context to avoid memory leaks
        this.context = activity.getApplicationContext();
        this.preferences = activity.getSharedPreferences(Constants.SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
        
        // Initialize display stats using the activity context (safe for one-time operation)
        initializeDisplayStats(activity);
        loadSettings();
        
        Logger.d(TAG, "SettingsManager initialized with display: " + 
                displayStats[0] + "x" + displayStats[1] + " @ " + displayStats[2] + "dpi");
    }
    
    /**
     * Initialize display statistics
     */
    private void initializeDisplayStats(Activity activity) {
        displayStats[0] = UIUtils.getScreenWidth(activity);
        displayStats[1] = UIUtils.getScreenHeight(activity);
        displayStats[2] = UIUtils.getScreenDensity(activity);
    }
    
    /**
     * Load settings from SharedPreferences
     */
    private void loadSettings() {
        isRoot = false; // Elite G uses ADB instead of root
        aggressiveLowMemoryKiller = preferences.getBoolean(Constants.PREF_AGGRESSIVE_LMK, false);
        killAllOtherApps = preferences.getBoolean(Constants.PREF_IS_MURDERER, false);
        keepStockDpi = preferences.getBoolean(Constants.PREF_KEEP_STOCK_DPI, false);
    }


    // Getter methods
    public int getCurrentWidth() {
        return displayStats[0];
    }

    public int getCurrentHeight() {
        return displayStats[1];
    }

    public int getCurrentDensity() {
        return displayStats[2];
    }

    public boolean isFirstLaunch() {
        return preferences.getBoolean(Constants.PREF_FIRST_LAUNCH, true);
    }

    public int getOriginalDensity() {
        return preferences.getInt(Constants.PREF_ORIGINAL_DPI, getCurrentDensity());
    }

    public String getOriginalResolution() {
        return preferences.getString(Constants.PREF_ORIGINAL_RESOLUTION, 
                UIUtils.formatResolution(getCurrentWidth(), getCurrentHeight()));
    }

    public int getOriginalWidth() {
        return preferences.getInt(Constants.PREF_ORIGINAL_WIDTH, getCurrentWidth());
    }

    public int getOriginalHeight() {
        return preferences.getInt(Constants.PREF_ORIGINAL_HEIGHT, getCurrentHeight());
    }

    public GameApp getRecentGameApp(int index) {
        if (index < 1 || index > Constants.MAX_RECENT_GAMES) {
            Logger.w(TAG, "Invalid game index: " + index);
            return null;
        }
        String packageName = preferences.getString(index + Constants.PREF_GAME_SUFFIX, "");
        if (packageName == null || packageName.isEmpty()) {
            return null;
        }
        return GameAppManager.getGameApp(context, packageName);
    }

    public boolean isLMKActivated() {
        return aggressiveLowMemoryKiller;
    }

    public boolean isMurderer() {
        return killAllOtherApps;
    }

    public boolean keepStockDPI() {
        return keepStockDpi;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public int getLastResolutionScale() {
        return preferences.getInt(Constants.PREF_LAST_RESOLUTION_SCALE, Constants.DEFAULT_RESOLUTION_SCALE);
    }

    /**
     * Get recommended resolution scale based on device capabilities
     */
    public int getRecommendedResolutionScale() {
        return PerformanceUtils.getRecommendedResolutionScale(context, getCurrentWidth(), getCurrentHeight());
    }




    // Setter methods
    public void initializeFirstLaunch() {
        Logger.d(TAG, "Initializing first launch settings");
        
        SharedPreferences.Editor editor = preferences.edit();
        int width = getCurrentWidth();
        int height = getCurrentHeight();
        int density = getCurrentDensity();
        
        editor.putInt(Constants.PREF_ORIGINAL_WIDTH, width);
        editor.putInt(Constants.PREF_ORIGINAL_HEIGHT, height);
        editor.putString(Constants.PREF_ORIGINAL_RESOLUTION, UIUtils.formatResolution(width, height));
        editor.putInt(Constants.PREF_ORIGINAL_DPI, density);
        editor.putBoolean(Constants.PREF_FIRST_LAUNCH, false);
        
        // Set recommended resolution scale for first-time users
        int recommendedScale = getRecommendedResolutionScale();
        editor.putInt(Constants.PREF_LAST_RESOLUTION_SCALE, recommendedScale);
        
        editor.apply(); // Non-blocking
        boolean success = true;
        Logger.d(TAG, "First launch initialization " + (success ? "successful" : "failed"));
    }

    public boolean setScreenDimension(int height, int width) {
        Logger.d(TAG, "Setting screen dimension to " + width + "x" + height);
        
        int densityDPI;
        if (!keepStockDpi) {
            densityDPI = PerformanceUtils.calculateOptimalDpi(getOriginalDensity(), getOriginalWidth(), width);
        } else {
            densityDPI = getOriginalDensity();
        }

        Logger.d(TAG, "Calculated DPI: " + densityDPI);
        
        boolean success;
        if (height < getCurrentHeight()) { // Scale Down
            success = ExecuteADBCommands.changeDensity(densityDPI) && 
                     ExecuteADBCommands.changeResolution(width, height);
        } else { // Scale up
            success = ExecuteADBCommands.changeResolution(width, height) && 
                     ExecuteADBCommands.changeDensity(densityDPI);
        }

        if (success) {
            displayStats[0] = width;
            displayStats[1] = height;
            displayStats[2] = densityDPI;
            Logger.d(TAG, "Screen dimension updated successfully");
        } else {
            Logger.e(TAG, "Failed to update screen dimension");
        }

        return success;
    }

    public void addGameApp(String packageName, int index) {
        if (index < 0 || index >= Constants.MAX_RECENT_GAMES) {
            Logger.w(TAG, "Invalid game index for adding: " + index);
            return;
        }
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString((index + 1) + Constants.PREF_GAME_SUFFIX, packageName);
        editor.apply(); // Non-blocking
        boolean success = true;
        
        Logger.d(TAG, "Game app " + packageName + " " + (success ? "added" : "failed to add") + 
                " at index " + index);
    }

    public void removeGameApp(int index) {
        if (index < 0 || index >= Constants.MAX_RECENT_GAMES) {
            Logger.w(TAG, "Invalid game index for removal: " + index);
            return;
        }
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString((index + 1) + Constants.PREF_GAME_SUFFIX, "");
        editor.apply(); // Non-blocking
        boolean success = true;
        
        Logger.d(TAG, "Game app " + (success ? "removed" : "failed to remove") + 
                " from index " + index);
    }

    public int findFirstEmptyRecentGameApp() {
        for (int i = 1; i <= Constants.MAX_RECENT_GAMES; i++) {
            if (getRecentGameApp(i) == null) {
                return i - 1;
            }
        }
        return 0; // All slots are full, return first slot
    }

    public void setLMK(boolean state) {
        aggressiveLowMemoryKiller = state;
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREF_AGGRESSIVE_LMK, state);
        editor.apply(); // Non-blocking
        boolean success = true;
        
        Logger.d(TAG, "LMK setting " + (success ? "updated" : "failed to update") + 
                " to " + state);
    }

    public void setMurderer(boolean state) {
        killAllOtherApps = state;
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREF_IS_MURDERER, state);
        editor.apply(); // Non-blocking
        boolean success = true;
        
        Logger.d(TAG, "Murderer setting " + (success ? "updated" : "failed to update") + 
                " to " + state);
    }

    public void setKeepStockDPI(boolean state) {
        keepStockDpi = state;
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREF_KEEP_STOCK_DPI, state);
        editor.apply(); // Non-blocking
        boolean success = true;
        
        Logger.d(TAG, "Keep stock DPI setting " + (success ? "updated" : "failed to update") + 
                " to " + state);
    }

    public void setLastResolutionScale(int scale) {
        int validatedScale = PerformanceUtils.validateResolutionScale(scale);
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.PREF_LAST_RESOLUTION_SCALE, validatedScale);
        editor.apply(); // Non-blocking
        boolean success = true;
        
        Logger.d(TAG, "Resolution scale " + (success ? "updated" : "failed to update") + 
                " to " + validatedScale);
    }

    public void setRootState(boolean state) {
        isRoot = state;
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREF_IS_ROOT, state);
        editor.apply(); // Non-blocking
        boolean success = true;
        
        Logger.d(TAG, "Root state " + (success ? "updated" : "failed to update") + 
                " to " + state);
    }

    public boolean hasADBPermissions() {
        return ExecuteADBCommands.hasADBPermissions();
    }

    /**
     * Reset all settings to default values
     */
    public void resetToDefaults() {
        Logger.d(TAG, "Resetting all settings to defaults");
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply(); // Non-blocking
        boolean success = true;
        
        if (success) {
            loadSettings(); // Reload default settings
            Logger.d(TAG, "Settings reset successfully");
        } else {
            Logger.e(TAG, "Failed to reset settings");
        }
    }

    /**
     * Export settings for backup
     */
    public String exportSettings() {
        StringBuilder export = new StringBuilder();
        export.append("EliteG Settings Export\n");
        export.append("Original Resolution: ").append(getOriginalResolution()).append("\n");
        export.append("Original DPI: ").append(getOriginalDensity()).append("\n");
        export.append("Last Resolution Scale: ").append(getLastResolutionScale()).append("\n");
        export.append("Keep Stock DPI: ").append(keepStockDPI()).append("\n");
        export.append("LMK Activated: ").append(isLMKActivated()).append("\n");
        export.append("Murderer Mode: ").append(isMurderer()).append("\n");
        
        return export.toString();
    }
}

