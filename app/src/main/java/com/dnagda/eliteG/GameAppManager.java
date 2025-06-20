package com.dnagda.eliteG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.dnagda.eliteG.utils.Constants;
import com.dnagda.eliteG.utils.Logger;
import com.dnagda.eliteG.utils.PerformanceUtils;
import com.dnagda.eliteG.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enhanced game application manager for EliteG.
 * Handles game detection, launching, and performance optimization.
 */
public class GameAppManager {
    private static final String TAG = "GameAppManager";

    // Prevent instantiation
    private GameAppManager() {
        throw new AssertionError("GameAppManager class should not be instantiated");
    }

    /**
     * Get list of game applications
     */
    public static List<GameApp> getGameApps(Context context, boolean onlyAddGames) {
        Logger.d(TAG, "Getting game apps, onlyAddGames: " + onlyAddGames);
        
        PackageManager packageManager = context.getPackageManager();
        List<GameApp> gameAppList = new ArrayList<>();
        
        try {
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
            Logger.d(TAG, "Found " + installedPackages.size() + " installed packages");

            for (PackageInfo packageInfo : installedPackages) {
                if (!isValidPackage(context, packageInfo, onlyAddGames)) {
                    continue;
                }

                String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                String packageName = packageInfo.applicationInfo.packageName;

                int iconSize = UIUtils.getOptimalIconSize(context);
                WrappedDrawable wrappedIcon = new WrappedDrawable(
                    packageInfo.applicationInfo.loadIcon(packageManager), 
                    0, 0, iconSize, iconSize
                );

                gameAppList.add(new GameApp(appName, wrappedIcon, packageName));
            }
            
            Logger.d(TAG, "Found " + gameAppList.size() + " valid game apps");
        } catch (Exception e) {
            Logger.e(TAG, "Error getting game apps", e);
        }
        
        return gameAppList;
    }

    /**
     * Get a specific game app by package name
     */
    public static GameApp getGameApp(Context context, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return null;
        }
        
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            
            int iconSize = UIUtils.getOptimalIconSize(context);
            WrappedDrawable wrappedIcon = new WrappedDrawable(
                packageManager.getApplicationIcon(appInfo), 
                0, 0, iconSize, iconSize
            );
            String name = packageManager.getApplicationLabel(appInfo).toString();

            return new GameApp(name, wrappedIcon, packageName);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w(TAG, "Package not found: " + packageName);
        } catch (Exception e) {
            Logger.e(TAG, "Error getting game app: " + packageName, e);
        }
        return null;
    }

    /**
     * Check if package is a system package
     */
    private static boolean isSystemPackage(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    /**
     * Check if package is a game package
     */
    @SuppressLint("NewApi")
    private static boolean isGamePackage(ApplicationInfo appInfo, boolean onlyAddGames) {
        // If not filtering for games only, consider all apps as valid
        if (!onlyAddGames) {
            return true;
        }

        // Check both new and deprecated game flags for compatibility
        boolean isGameByCategory = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && 
                                  appInfo.category == ApplicationInfo.CATEGORY_GAME;
        boolean isGameByFlag = (appInfo.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME;
        
        return isGameByCategory || isGameByFlag;
    }

    /**
     * Check if package is valid for inclusion in game list
     */
    private static boolean isValidPackage(Context context, PackageInfo packageInfo, boolean onlyAddGames) {
        if (context == null) {
            Logger.w(TAG, "Context is null");
            return false;
        }
        
        // Basic validation
        if (isSystemPackage(packageInfo) || 
            packageInfo.packageName.equals(Constants.APP_PACKAGE_NAME) ||
            !isGamePackage(packageInfo.applicationInfo, onlyAddGames)) {
            return false;
        }

        // Skip recent games check for generic context usage
        // Recent games filtering will be handled at the UI level

        return true;
    }

    /**
     * Kill background apps for better performance
     */
    private static void murderApps(MainActivity context) {
        Logger.d(TAG, "Starting background app termination");
        
        if (context == null || context.isFinishing() || context.isDestroyed()) {
            Logger.w(TAG, "Context is invalid - cannot kill background apps");
            return;
        }
        
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages;
        
        try {
            installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        } catch (Exception e) {
            Logger.e(TAG, "Error getting installed packages", e);
            return;
        }
        
        List<String> appsToKill = new ArrayList<>();

        for (PackageInfo packageInfo : installedPackages) {
            if (packageInfo == null || packageInfo.packageName == null) {
                continue; // Skip null entries
            }
            
            String packageName = packageInfo.packageName;

            // Check if app is killable
            boolean isKillable = true;
            for (String unkillableApp : Constants.UNKILLABLE_APPS) {
                if (isSystemPackage(packageInfo) || packageName.equals(unkillableApp)) {
                    isKillable = false;
                    break;
                }
            }

            if (isKillable) {
                appsToKill.add(packageName);
            }
        }

        Logger.d(TAG, "Killing " + appsToKill.size() + " background apps");
        
        // Kill apps in batches to avoid overwhelming the system
        for (int i = 0; i < appsToKill.size(); i++) {
            if (context.isFinishing() || context.isDestroyed()) {
                Logger.w(TAG, "Activity destroyed - stopping app killing");
                break;
            }
            
            String packageName = appsToKill.get(i);
            ExecuteADBCommands.forceStopApp(packageName);
            
            // Rate limit to prevent overwhelming system
            if (i % 10 == 0 && i > 0) {
                try {
                    Thread.sleep(100); // Brief pause every 10 apps
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Logger.w(TAG, "Interrupted while rate limiting app killing");
                    break;
                }
            }
        }
        
        Logger.d(TAG, "Background app termination completed");
    }

    /**
     * Activate performance optimizations
     */
    private static void activatePerformanceOptimizations(MainActivity context) {
        Logger.d(TAG, "Activating performance optimizations");
        boolean success = ExecuteADBCommands.applyPerformanceOptimizations();
        
        if (success) {
            Logger.d(TAG, "Performance optimizations applied successfully");
        } else {
            Logger.w(TAG, "Failed to apply some performance optimizations");
        }
    }

    /**
     * Restore original performance settings
     */
    public static void restoreOriginalPerformanceSettings(MainActivity context) {
        Logger.d(TAG, "Restoring original performance settings");
        boolean success = ExecuteADBCommands.resetPerformanceOptimizations();
        
        if (success) {
            Logger.d(TAG, "Performance settings restored successfully");
        } else {
            Logger.w(TAG, "Failed to restore some performance settings");
        }
    }

    /**
     * Launch game app with optimizations
     */
    public static void launchGameApp(MainActivity context, String packageName) {
        Logger.d(TAG, "Launching game app: " + packageName);
        
        try {
            SettingsManager settingsManager = context.settingsManager;
            int resolutionScale = context.getResolutionScale();

            // Save the resolution scale for this session
            settingsManager.setLastResolutionScale(resolutionScale);

            // Apply resolution changes
            int newWidth = PerformanceUtils.calculateNewWidth(
                settingsManager.getOriginalWidth(), 
                context.coefficients[0], 
                resolutionScale
            );
            int newHeight = PerformanceUtils.calculateNewHeight(
                settingsManager.getOriginalHeight(), 
                context.coefficients[1], 
                resolutionScale
            );

            Logger.d(TAG, "Applying resolution: " + newWidth + "x" + newHeight);
            boolean resolutionSuccess = settingsManager.setScreenDimension(newHeight, newWidth);
            
            if (!resolutionSuccess) {
                Logger.w(TAG, "Failed to apply resolution changes");
                UIUtils.showToast(context, "Failed to apply resolution changes");
            }

            // Apply performance optimizations
            if (settingsManager.isMurderer()) {
                murderApps(context);
            }

            if (settingsManager.isLMKActivated()) {
                activatePerformanceOptimizations(context);
            }

            // Create temp file to indicate DPI change
            File tempFile = new File(context.getApplicationInfo().dataDir + "/" + Constants.TEMP_FILE_NAME);
            try {
                tempFile.createNewFile();
            } catch (Exception e) {
                Logger.w(TAG, "Could not create temp file", e);
            }

            // Launch the game
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(launchIntent);
                
                Logger.d(TAG, "Game launched successfully: " + packageName);
                
                // Finish the booster app to free memory for the game
                context.finish();
            } else {
                Logger.e(TAG, "Could not find launch intent for: " + packageName);
                UIUtils.showToast(context, "Could not launch game");
            }
            
        } catch (Exception e) {
            Logger.e(TAG, "Error launching game app: " + packageName, e);
            UIUtils.showToast(context, "Error launching game");
        }
    }

    /**
     * Check if app is installed
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get app version
     */
    public static String getAppVersion(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }
}
