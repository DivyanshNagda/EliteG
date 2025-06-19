package com.dnagda.eliteG.utils;

/**
 * Constants used throughout the EliteG application.
 * Centralized location for all constant values to improve maintainability.
 */
public final class Constants {
    
    // Prevent instantiation
    private Constants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
    
    // Application Constants
    public static final String APP_PACKAGE_NAME = "com.dnagda.eliteG";
    public static final String PACKAGE_NAME = APP_PACKAGE_NAME; // Alias for tests
    public static final String SETTINGS_FILE_NAME = "SETTINGS";
    public static final String TEMP_FILE_NAME = "tmp";
    
    // Performance Constants
    public static final float FPS_BOOST_MULTIPLIER = 0.8f;
    public static final float RESOLUTION_COEFFICIENT_MULTIPLIER = 0.005f;
    public static final int MAX_RECENT_GAMES = 6;
    public static final int DEFAULT_RESOLUTION_SCALE = 75;
    public static final int MIN_RESOLUTION_SCALE = 50;
    public static final int MAX_RESOLUTION_SCALE = 100;
    
    // UI Constants
    public static final float DIALOG_WIDTH_RATIO = 0.90f;
    public static final float DIALOG_HEIGHT_RATIO = 0.85f;
    public static final int GAME_ITEM_HEIGHT = 200;
    public static final int ICON_SIZE = 160;
    public static final int LARGE_ICON_SIZE = 180;
    
    // Animation Constants
    public static final float ANIMATION_SCALE_DISABLED = 0.5f;
    public static final float ANIMATION_SCALE_DEFAULT = 1.0f;
    
    // ADB Commands
    public static final String ADB_GRANT_COMMAND = "adb shell pm grant com.dnagda.eliteG android.permission.WRITE_SECURE_SETTINGS";
    public static final String ADB_COMMAND_KILL_ALL = "am kill-all";
    public static final String ADB_COMMAND_WM_SIZE = "wm size";
    public static final String ADB_COMMAND_WM_DENSITY = "wm density";
    public static final String ADB_COMMAND_WM_SIZE_RESET = "wm size reset";
    public static final String ADB_COMMAND_WM_DENSITY_RESET = "wm density reset";
    public static final String ADB_COMMAND_FORCE_STOP = "am force-stop ";
    public static final String ADB_COMMAND_SETTINGS_GET = "settings get secure android_id";
    public static final String ADB_COMMAND_FONT_SCALE = "settings put system font_scale ";
    
    // Settings Keys
    public static final String PREF_FIRST_LAUNCH = "firstLaunch";
    public static final String PREF_ORIGINAL_WIDTH = "originalWidth";
    public static final String PREF_ORIGINAL_HEIGHT = "originalHeight";
    public static final String PREF_ORIGINAL_RESOLUTION = "originalResolution";
    public static final String PREF_ORIGINAL_DPI = "originalDPI";
    public static final String PREF_AGGRESSIVE_LMK = "aggressiveLMK";
    public static final String PREF_IS_MURDERER = "isMurderer";
    public static final String PREF_KEEP_STOCK_DPI = "keepStockDPI";
    public static final String PREF_LAST_RESOLUTION_SCALE = "lastResolutionScale";
    public static final String PREF_IS_ROOT = "isRoot";
    public static final String PREF_GAME_SUFFIX = "thGame";
    
    // Performance Settings
    public static final String SETTING_WINDOW_ANIMATION_SCALE = "settings put global window_animation_scale ";
    public static final String SETTING_TRANSITION_ANIMATION_SCALE = "settings put global transition_animation_scale ";
    public static final String SETTING_ANIMATOR_DURATION_SCALE = "settings put global animator_duration_scale ";
    public static final String SETTING_LOW_POWER_MODE = "settings put global low_power_mode ";
    public static final String SETTING_BACKGROUND_APP_REFRESH = "settings put global background_app_refresh_disabled ";
    
    // Permissions
    public static final String PERMISSION_WRITE_SECURE_SETTINGS = "android.permission.WRITE_SECURE_SETTINGS";
    public static final String PERMISSION_QUERY_ALL_PACKAGES = "android.permission.QUERY_ALL_PACKAGES";
    
    // Unkillable Apps
    public static final String[] UNKILLABLE_APPS = {
        APP_PACKAGE_NAME,
        "com.topjohnwu.magisk",
        "eu.chainfire.supersu-1",
        "com.android.systemui",
        "android",
        "com.android.phone"
    };
    
    // Error Codes
    public static final int ERROR_CODE_SUCCESS = 0;
    public static final int ERROR_CODE_PERMISSION_DENIED = -1;
    public static final int ERROR_CODE_COMMAND_FAILED = 1;
    
    // Timeouts
    public static final long ADB_COMMAND_TIMEOUT_MS = 5000;
    public static final long ADB_TIMEOUT_MS = ADB_COMMAND_TIMEOUT_MS; // Alias for tests
    public static final long UI_TIMEOUT_MS = 3000;
    public static final long PERFORMANCE_CHECK_TIMEOUT_MS = 2000;
    public static final long SPLASH_SCREEN_DELAY_MS = 2000;
    
    // URLs
    public static final String GITHUB_SETUP_URL = "https://github.com/DivyanshNagda/EliteG#setup";
    public static final String GITHUB_ISSUES_URL = "https://github.com/DivyanshNagda/EliteG/issues";
    
    // Layout Guidelines
    public static final float GUIDELINE_33_PERCENT = 0.33f;
    public static final float GUIDELINE_50_PERCENT = 0.50f;
    public static final float GUIDELINE_80_PERCENT = 0.8f;
    public static final float GUIDELINE_20_PERCENT = 0.20f;
    
    // Memory Thresholds (in MB)
    public static final int LOW_MEMORY_THRESHOLD_MB = 1024; // 1GB
    public static final int HIGH_MEMORY_THRESHOLD_MB = 4096; // 4GB
}