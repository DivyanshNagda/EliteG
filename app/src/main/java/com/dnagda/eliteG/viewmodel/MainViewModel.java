package com.dnagda.eliteG.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dnagda.eliteG.ExecuteADBCommands;
import com.dnagda.eliteG.GameApp;
import com.dnagda.eliteG.GameAppManager;
import com.dnagda.eliteG.MainActivity;
import com.dnagda.eliteG.SettingsManager;
import com.dnagda.eliteG.utils.Logger;
import com.dnagda.eliteG.utils.PerformanceUtils;

import java.util.List;

/**
 * ViewModel for MainActivity following MVVM architecture pattern.
 * Manages UI-related data in a lifecycle-conscious way and handles business logic.
 */
public class MainViewModel extends ViewModel {
    private static final String TAG = "MainViewModel";

    // LiveData for UI state management
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> adbPermissionsGranted = new MutableLiveData<>(false);
    private final MutableLiveData<List<GameApp>> gameApps = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentResolutionScale = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> performanceMode = new MutableLiveData<>(false);
    private final MutableLiveData<String> deviceInfo = new MutableLiveData<>();

    // Context and managers
    private MainActivity mainActivity;
    private SettingsManager settingsManager;

    public MainViewModel() {
        Logger.d(TAG, "MainViewModel created");
    }
    
    /**
     * Initialize with MainActivity context
     */
    public void initialize(MainActivity activity) {
        this.mainActivity = activity;
        initializeManagers();
        loadInitialData();
    }

    /**
     * Initialize managers
     */
    private void initializeManagers() {
        try {
            if (mainActivity != null) {
                settingsManager = new SettingsManager(mainActivity);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error initializing managers", e);
            errorMessage.setValue("Failed to initialize app components");
        }
    }

    /**
     * Load initial data
     */
    private void loadInitialData() {
        checkADBPermissions();
        loadSettings();
        loadGameApps();
        loadDeviceInfo();
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getAdbPermissionsGranted() { return adbPermissionsGranted; }
    public LiveData<List<GameApp>> getGameApps() { return gameApps; }
    public LiveData<Integer> getCurrentResolutionScale() { return currentResolutionScale; }
    public LiveData<Boolean> getPerformanceMode() { return performanceMode; }
    public LiveData<String> getDeviceInfo() { return deviceInfo; }

    /**
     * Check ADB permissions asynchronously
     */
    public void checkADBPermissions() {
        isLoading.setValue(true);
        
        ExecuteADBCommands.hasADBPermissionsAsync(new ExecuteADBCommands.ADBCallback() {
            @Override
            public void onSuccess(ExecuteADBCommands.CommandResult result) {
                isLoading.setValue(false);
                adbPermissionsGranted.setValue(true);
                Logger.d(TAG, "ADB permissions granted");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                adbPermissionsGranted.setValue(false);
                errorMessage.setValue("ADB permissions not granted: " + error);
                Logger.e(TAG, "ADB permissions check failed: " + error);
            }
        });
    }

    /**
     * Load settings from SettingsManager
     */
    public void loadSettings() {
        try {
            if (settingsManager != null) {
                int resolutionScale = settingsManager.getLastResolutionScale();
                boolean perfMode = settingsManager.isLMKActivated(); // Using LMK as performance mode
                
                currentResolutionScale.setValue(resolutionScale);
                performanceMode.setValue(perfMode);
                
                Logger.d(TAG, "Settings loaded - Resolution: " + resolutionScale + ", Performance: " + perfMode);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error loading settings", e);
            errorMessage.setValue("Failed to load settings");
        }
    }

    /**
     * Load game apps from GameAppManager
     */
    public void loadGameApps() {
        try {
            if (mainActivity != null) {
                List<GameApp> apps = GameAppManager.getGameApps(mainActivity, false);
                gameApps.setValue(apps);
                Logger.d(TAG, "Loaded " + (apps != null ? apps.size() : 0) + " game apps");
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error loading game apps", e);
            errorMessage.setValue("Failed to load game apps");
        }
    }

    /**
     * Load device information
     */
    public void loadDeviceInfo() {
        try {
            if (mainActivity != null) {
                // Build device info from available methods
                StringBuilder info = new StringBuilder();
                info.append("Memory: ").append(PerformanceUtils.getAvailableMemoryMB(mainActivity)).append("MB available\n");
                info.append("Total Memory: ").append(PerformanceUtils.getTotalMemoryMB(mainActivity)).append("MB\n");
                info.append("Memory Usage: ").append(PerformanceUtils.getMemoryUsagePercentage(mainActivity)).append("%\n");
                info.append("Low Memory Device: ").append(PerformanceUtils.isLowMemoryDevice(mainActivity));
                
                deviceInfo.setValue(info.toString());
                Logger.d(TAG, "Device info loaded");
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error loading device info", e);
            errorMessage.setValue("Failed to load device information");
        }
    }

    /**
     * Change resolution scale
     */
    public void changeResolutionScale(int scale) {
        if (scale < 0 || scale > 100) {
            errorMessage.setValue("Invalid resolution scale: " + scale);
            return;
        }

        isLoading.setValue(true);
        
        // Calculate resolution based on scale and current settings
        if (settingsManager == null) {
            errorMessage.setValue("Settings manager not initialized");
            return;
        }
        
        int originalWidth = settingsManager.getOriginalWidth();
        int originalHeight = settingsManager.getOriginalHeight();
        
        float widthCoeff = PerformanceUtils.calculateWidthCoefficient(originalWidth);
        float heightCoeff = PerformanceUtils.calculateHeightCoefficient(originalHeight);
        
        int newWidth = PerformanceUtils.calculateNewWidth(originalWidth, widthCoeff, scale);
        int newHeight = PerformanceUtils.calculateNewHeight(originalHeight, heightCoeff, scale);
        
        ExecuteADBCommands.changeResolutionAsync(newWidth, newHeight, new ExecuteADBCommands.ADBCallback() {
            @Override
            public void onSuccess(ExecuteADBCommands.CommandResult result) {
                isLoading.setValue(false);
                currentResolutionScale.setValue(scale);
                
                // Save to settings
                if (settingsManager != null) {
                    settingsManager.setLastResolutionScale(scale);
                }
                
                successMessage.setValue("Resolution changed successfully");
                Logger.d(TAG, "Resolution changed to scale: " + scale);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to change resolution: " + error);
                Logger.e(TAG, "Resolution change failed: " + error);
            }
        });
    }

    /**
     * Reset resolution to default
     */
    public void resetResolution() {
        isLoading.setValue(true);
        
        ExecuteADBCommands.resetResolutionAsync(new ExecuteADBCommands.ADBCallback() {
            @Override
            public void onSuccess(ExecuteADBCommands.CommandResult result) {
                isLoading.setValue(false);
                currentResolutionScale.setValue(0);
                
                // Save to settings
                if (settingsManager != null) {
                    settingsManager.setLastResolutionScale(0);
                }
                
                successMessage.setValue("Resolution reset to default");
                Logger.d(TAG, "Resolution reset to default");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to reset resolution: " + error);
                Logger.e(TAG, "Resolution reset failed: " + error);
            }
        });
    }

    /**
     * Toggle performance mode
     */
    public void togglePerformanceMode() {
        boolean currentMode = Boolean.TRUE.equals(performanceMode.getValue());
        boolean newMode = !currentMode;
        
        isLoading.setValue(true);
        
        // Apply performance optimizations (using LMK as performance mode)
        try {
            performanceMode.setValue(newMode);
            
            // Save to settings
            if (settingsManager != null) {
                settingsManager.setLMK(newMode);
            }
            
            isLoading.setValue(false);
            successMessage.setValue("Performance mode " + (newMode ? "enabled" : "disabled"));
            Logger.d(TAG, "Performance mode " + (newMode ? "enabled" : "disabled"));
            
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("Failed to toggle performance mode: " + e.getMessage());
            Logger.e(TAG, "Performance mode toggle failed", e);
        }
    }

    /**
     * Launch game with optimizations
     */
    public void launchGame(GameApp gameApp) {
        if (gameApp == null) {
            errorMessage.setValue("Invalid game app");
            return;
        }

        isLoading.setValue(true);
        
        try {
            if (mainActivity != null) {
                // Launch the game using intent
                Intent launchIntent = mainActivity.getPackageManager().getLaunchIntentForPackage(gameApp.getPackageName());
                if (launchIntent != null) {
                    mainActivity.startActivity(launchIntent);
                    
                    // Add to recent games (find first empty slot)
                    if (settingsManager != null) {
                        int emptySlot = settingsManager.findFirstEmptyRecentGameApp();
                        if (emptySlot >= 0) {
                            settingsManager.addGameApp(gameApp.getPackageName(), emptySlot);
                        }
                    }
                    
                    isLoading.setValue(false);
                    successMessage.setValue("Game launched: " + gameApp.getGameName());
                    Logger.d(TAG, "Game launched: " + gameApp.getPackageName());
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to launch game: " + gameApp.getGameName());
                    Logger.e(TAG, "Failed to launch game: " + gameApp.getPackageName());
                }
            }
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("Error launching game: " + e.getMessage());
            Logger.e(TAG, "Error launching game", e);
        }
    }

    /**
     * Refresh game apps list
     */
    public void refreshGameApps() {
        isLoading.setValue(true);
        
        try {
            if (mainActivity != null) {
                List<GameApp> apps = GameAppManager.getGameApps(mainActivity, false);
                gameApps.setValue(apps);
                
                isLoading.setValue(false);
                successMessage.setValue("Game apps refreshed");
                Logger.d(TAG, "Game apps refreshed - found " + (apps != null ? apps.size() : 0) + " apps");
            }
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("Failed to refresh game apps: " + e.getMessage());
            Logger.e(TAG, "Error refreshing game apps", e);
        }
    }

    /**
     * Clear error message
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    /**
     * Clear success message
     */
    public void clearSuccessMessage() {
        successMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.d(TAG, "MainViewModel cleared");
        
        // Cleanup resources
        try {
            ExecuteADBCommands.cleanup();
        } catch (Exception e) {
            Logger.e(TAG, "Error during cleanup", e);
        }
    }
}