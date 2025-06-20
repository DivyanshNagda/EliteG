package com.dnagda.eliteG;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dnagda.eliteG.utils.AccessibilityUtils;
import com.dnagda.eliteG.utils.Constants;
import com.dnagda.eliteG.utils.Logger;
import com.dnagda.eliteG.utils.PerformanceMonitor;
import com.dnagda.eliteG.utils.PerformanceUtils;
import com.dnagda.eliteG.utils.ThreadUtils;
import com.dnagda.eliteG.utils.UIUtils;
import com.dnagda.eliteG.SettingsManager;
import com.dnagda.eliteG.GameApp;
import com.dnagda.eliteG.GameAppManager;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.widget.EditText;

/**
 * Enhanced main activity for EliteG Android Game Booster.
 * Provides an intuitive interface for game performance optimization.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_CODE = 100;
    // UI Components
    private TextView fpsPercentageText;
    private TextView tweakedResolutionText;
    private SeekBar resolutionSeekBar;
    private ProgressBar circularProgressBar;
    private ImageButton settingsSwitch;
    
    // Settings and state
    public SettingsManager settingsManager;
    private boolean settingsShown = false;
    private int lastProgress = 0;
    
    // Performance calculations
    public float[] coefficients = new float[2]; // Width, Height coefficients
    
    // Options
    private CheckBox[] optionCheckboxes = new CheckBox[3];
    
    // Layout management
    private ConstraintSet layoutSettingsHidden = new ConstraintSet();
    private ConstraintSet layoutSettingShown = new ConstraintSet();
    
    // Game management
    private List<GameApp> gameList;
    private GameApp[] recentGameApps = new GameApp[Constants.MAX_RECENT_GAMES];
    private TextView[] recentGameTitles = new TextView[Constants.MAX_RECENT_GAMES];
    private ImageButton[] recentGameIcons = new ImageButton[Constants.MAX_RECENT_GAMES];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "MainActivity onCreate started");
        
        // Start performance monitoring
        try (PerformanceMonitor.OperationTimer timer = PerformanceMonitor.time("MainActivity.onCreate")) {
            setContentView(R.layout.activity_main);
            
            // Initialize core components
            initializeCore();

            // Check for resolution reset
            checkResolutionReset();

            // Initialize UI components
            initializeUI();

            // Initialize permissions and log device info
            initializePermissions();

            // Check permissions and first launch
            checkPermissionsAndFirstLaunch();
            
            // Initialize options and recent games
            initializeOptions();
            initializeRecentGames();
            
            // Setup event listeners
            setupEventListeners();
            
            // Storage permission check (only for older Android versions)
            checkStoragePermissions(); 
            
            // --- Centralized robust permission check ---
            checkPermissions();
            Logger.d(TAG, "MainActivity onCreate completed successfully");
        } catch (Exception e) {
            Logger.e(TAG, "Error in onCreate", e);
            UIUtils.showToast(this, "Error initializing app");
            finish();
        }
        // Performance monitoring auto-closes with try-with-resources
    }
    
    /**
     * Initialize core components
     */
    private void initializeCore() {
        settingsManager = new SettingsManager(this);
        gameList = GameAppManager.getGameApps(this, true); // Initialize gameList
        // Compute performance coefficients
        coefficients[0] = PerformanceUtils.calculateWidthCoefficient(settingsManager.getOriginalWidth());
        coefficients[1] = PerformanceUtils.calculateHeightCoefficient(settingsManager.getOriginalHeight());
        
        Logger.d(TAG, "Core components initialized");
    }
    
    /**
     * Check if resolution needs to be reset
     */
    private void checkResolutionReset() {
        File tempFile = new File(getApplicationInfo().dataDir + "/" + Constants.TEMP_FILE_NAME);
        if (tempFile.exists()) {
            boolean deleted = tempFile.delete();
            Logger.d(TAG, "Temp file " + (deleted ? "deleted" : "failed to delete"));
        } else {
            if (settingsManager.getOriginalWidth() != settingsManager.getCurrentWidth()) {
                showResetPopup();
            }
        }
    }
    
    /**
     * Initialize UI components
     */
    private void initializeUI() {
        try {
            // Find and initialize UI components with comprehensive null checks
            fpsPercentageText = findViewById(R.id.textViewPercentage);
            tweakedResolutionText = findViewById(R.id.textViewTweakedResolution);
            resolutionSeekBar = findViewById(R.id.seekBarRes);
            circularProgressBar = findViewById(R.id.progressBar);
            settingsSwitch = findViewById(R.id.imageButtonSettingSwitch);
            
            // Critical UI component validation with detailed error reporting
            if (!validateCriticalUIComponents()) {
                Logger.e(TAG, "Critical UI component missing - cannot continue");
                UIUtils.showToast(this, getString(R.string.ui_init_error));
                finish();
                return;
            }
            if (fpsPercentageText != null) {
                int lastScale = settingsManager.getLastResolutionScale();
                int fpsBoost = PerformanceUtils.calculateFpsBoost(lastScale);
                fpsPercentageText.setText(UIUtils.formatFpsPercentage(fpsBoost));
            }
            TextView nativeResolution = findViewById(R.id.textViewNativeResolution);
            if (nativeResolution != null) {
                nativeResolution.setText(String.format("%s\n%s%s", 
                    getString(R.string.resolution), 
                    UIUtils.formatResolution(settingsManager.getOriginalWidth(), settingsManager.getOriginalHeight()),
                    getString(R.string.progressive)));
            }
            updateTweakedResolutionDisplay(settingsManager.getLastResolutionScale());
            if (resolutionSeekBar != null) {
                resolutionSeekBar.setProgress(settingsManager.getLastResolutionScale());
            }
            if (circularProgressBar != null) {
                circularProgressBar.setProgress(settingsManager.getLastResolutionScale());
            }
            lastProgress = settingsManager.getLastResolutionScale();
            ConstraintLayout rootLayout = findViewById(R.id.MainActivity);
            if (rootLayout != null) {
                layoutSettingsHidden.clone(rootLayout);
                layoutSettingShown.clone(this, R.layout.activity_main_options_shown);
            } else {
                Logger.e(TAG, "Root layout not found");
            }
            Logger.d(TAG, "UI components initialized");
        } catch (Exception e) {
            Logger.e(TAG, "Error initializing UI", e);
        }
    }
    
    /**
     * Check permissions and handle first launch
     */
    private void checkPermissionsAndFirstLaunch() {
        if (!settingsManager.hasADBPermissions()) {
            showNoPermissionsPopup();
            return;
        }
        if (settingsManager.isFirstLaunch()) {
            showDisclaimerPopup();
        }
    }
    
    /**
     * Update tweaked resolution display
     */
    private void updateTweakedResolutionDisplay(int scale) {
        int newWidth = PerformanceUtils.calculateNewWidth(
            settingsManager.getOriginalWidth(), coefficients[0], scale);
        int newHeight = PerformanceUtils.calculateNewHeight(
            settingsManager.getOriginalHeight(), coefficients[1], scale);
        
        tweakedResolutionText.setText(String.format("%s\n%s%s", 
            getString(R.string.resolution_tweaked),
            UIUtils.formatResolution(newWidth, newHeight),
            getString(R.string.progressive)));
    }


    /**
     * Setup event listeners for UI components
     */
    private void setupEventListeners() {
        // Settings switch listener
        settingsSwitch.setOnClickListener(view -> toggleSettings());
        
        // Resolution seekbar listener
        if (resolutionSeekBar != null) {
            resolutionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Optionally, update a preview UI here, but do not apply changes yet
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Haptic feedback
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        seekBar.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    updateProgressAndDisplay(progress);
                    settingsManager.setLastResolutionScale(progress);
                    Logger.d(TAG, "Resolution scale saved: " + progress);
                }
            });
        }
        
        // Add game button listener
        ImageButton addGameButton = findViewById(R.id.addGameButton);
        addGameButton.setOnClickListener(view -> showAddGame(true, settingsManager.findFirstEmptyRecentGameApp()));
        
        // Setup options click listeners
        setOptionsOnClickListener();
        setOptionsClickable(false); // Initially hidden

        Button btnOpenADBMode = findViewById(R.id.btnOpenADBMode);
        btnOpenADBMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ADBModeActivity.class);
                startActivity(intent);
            }
        });
        
        Logger.d(TAG, "Event listeners setup completed");
    }
    
    /**
     * Toggle settings panel visibility
     */
    private void toggleSettings() {
        ConstraintLayout mainLayout = findViewById(R.id.MainActivity);
        if (mainLayout == null) {
            Logger.e(TAG, "Main layout not found");
            UIUtils.showToast(this, getString(R.string.ui_init_error));
            return;
        }
        TransitionManager.beginDelayedTransition(mainLayout);
        
        ConstraintSet constraintSet = settingsShown ? layoutSettingsHidden : layoutSettingShown;
        setRecentGameAppClickable(!settingsShown);
        setOptionsClickable(settingsShown);
        constraintSet.applyTo(mainLayout);
        settingsShown = !settingsShown;
        
        Logger.d(TAG, "Settings panel " + (settingsShown ? "shown" : "hidden"));
    }
    
    /**
     * Update progress bar and display values
     */
    private void updateProgressAndDisplay(int progress) {
        // Update progress bar
        if (circularProgressBar != null) {
            circularProgressBar.setProgress(progress);
        }
        lastProgress = progress;
        
        // Update FPS percentage
        if (fpsPercentageText != null) {
            int fpsBoost = PerformanceUtils.calculateFpsBoost(progress);
            fpsPercentageText.setText(UIUtils.formatFpsPercentage(fpsBoost));
        }
        
        // Update tweaked resolution display
        updateTweakedResolutionDisplay(progress);
    }

    /**
     * Initialize and check permissions
     */
    private void initializePermissions() {
        int writeSecureSettings = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_SECURE_SETTINGS");
        Logger.d(TAG, "WRITE_SECURE_SETTINGS permission: " + 
                (writeSecureSettings == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
        if (writeSecureSettings != PackageManager.PERMISSION_GRANTED) {
            showNoPermissionsPopup();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int queryPackages = ContextCompat.checkSelfPermission(this, Constants.PERMISSION_QUERY_ALL_PACKAGES);
            Logger.d(TAG, "QUERY_ALL_PACKAGES permission: " + 
                    (queryPackages == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
            
            if (queryPackages == PackageManager.PERMISSION_DENIED) {
                UIUtils.showToast(this, "Some features may be limited without package query permission");
            }
        }
        
        // Log device performance info
        Logger.d(TAG, "Device is low memory: " + PerformanceUtils.isLowMemoryDevice(this));
        Logger.d(TAG, "Available memory: " + PerformanceUtils.getAvailableMemoryMB(this) + "MB");
    }

    /**
     * Show game list popup dialog
     */
    private void showGameListPopup(Context context, boolean onlyAddGames, final int gameAppIndex) {
        Logger.d(TAG, "Showing game list popup, onlyAddGames: " + onlyAddGames);
        
        final Dialog gameListDialog = new Dialog(this);
        gameListDialog.setContentView(R.layout.add_game_layout);
        
        // Calculate dialog dimensions
        int dialogWidth = UIUtils.getDialogWidth(context);
        int dialogHeight = UIUtils.getDialogHeight(context, gameList.size(), onlyAddGames);
        
        gameListDialog.getWindow().setLayout(dialogWidth, dialogHeight);
        gameListDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        LinearLayout layout = gameListDialog.findViewById(R.id.gameListLayout);
        LayoutInflater inflater = getLayoutInflater();
        
        // Add game items
        for (int i = 0; i < gameList.size(); i++) {
            View gameItemView = inflater.inflate(R.layout.game_app_item, null);
            setupGameItem(gameItemView, gameList.get(i), gameListDialog, gameAppIndex);
            layout.addView(gameItemView);
            
            // Add spacing
            Space space = new Space(this);
            space.setMinimumHeight(UIUtils.dpToPx(this, 10));
            layout.addView(space);
        }
        
        // Add "show all apps" option if only showing games
        if (onlyAddGames) {
            View showAllView = inflater.inflate(R.layout.no_game_app_item, null);
            layout.addView(showAllView);
            showAllView.setOnClickListener(v -> {
                gameListDialog.dismiss();
                gameList = GameAppManager.getGameApps(MainActivity.this, false);
                showGameListPopup(MainActivity.this, false, gameAppIndex);
            });
        }
        
        gameListDialog.show();
    }
    
    /**
     * Setup individual game item in the list
     */
    private void setupGameItem(View itemView, GameApp gameApp, Dialog dialog, int gameAppIndex) {
        TextView title = itemView.findViewById(R.id.textViewGameAppTitle);
        TextView packageName = itemView.findViewById(R.id.textViewGameAppPackageName);
        ImageView icon = itemView.findViewById(R.id.imageViewGameIcon);
        
        title.setText(gameApp.getGameName());
        packageName.setText(gameApp.getPackageName());
        icon.setImageDrawable(gameApp.getIcon());
        
        // Add content description for accessibility
        itemView.setContentDescription("Select " + gameApp.getGameName() + " to add to recent games slot " + (gameAppIndex + 1));
        
        itemView.setOnClickListener(view -> {
            Logger.d(TAG, "Selected game: " + gameApp.getGameName());
            dialog.dismiss();
            addGameUI(gameApp.getPackageName(), gameAppIndex);
        });
    }


    /**
     * Initialize options checkboxes
     */
    private void initializeOptions() {
        // Link option switches
        optionCheckboxes[0] = findViewById(R.id.checkBoxAggressive);
        optionCheckboxes[1] = findViewById(R.id.checkBoxMurderer);
        optionCheckboxes[2] = findViewById(R.id.checkBoxStockDPI);

        // Load their previous state
        optionCheckboxes[0].setChecked(settingsManager.isLMKActivated());
        optionCheckboxes[1].setChecked(settingsManager.isMurderer());
        optionCheckboxes[2].setChecked(settingsManager.keepStockDPI());
        
        // Add content descriptions for accessibility
        optionCheckboxes[0].setContentDescription("Kill background apps for better performance");
        optionCheckboxes[1].setContentDescription("Apply performance optimizations");
        optionCheckboxes[2].setContentDescription("Keep original screen density");

        Logger.d(TAG, "Options initialized");
    }

    /**
     * Set clickable state for option checkboxes
     */
    private void setOptionsClickable(boolean state) {
        for (CheckBox checkbox : optionCheckboxes) {
            checkbox.setClickable(state);
        }
        Logger.d(TAG, "Options clickable state set to: " + state);
    }

    private void setOptionsOnClickListener(){
        // Elite G uses ADB permissions instead of root
        if(settingsManager.hasADBPermissions()) {
            optionCheckboxes[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (optionCheckboxes[0].isChecked()) {
                        ExecuteADBCommands.killBackgroundApps();
                    }
                    settingsManager.setLMK(optionCheckboxes[0].isChecked());
                }
            });

            optionCheckboxes[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (optionCheckboxes[1].isChecked()) {
                        ExecuteADBCommands.applyPerformanceOptimizations();
                    } else {
                        ExecuteADBCommands.resetPerformanceOptimizations();
                    }
                    settingsManager.setMurderer(optionCheckboxes[1].isChecked());
                }
            });
        }else{
            optionCheckboxes[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionCheckboxes[0].setChecked(false);
                    Toast.makeText(MainActivity.this, "ADB permissions required for this feature", Toast.LENGTH_SHORT).show();
                }
            });

            optionCheckboxes[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionCheckboxes[1].setChecked(false);
                    Toast.makeText(MainActivity.this, "ADB permissions required for this feature", Toast.LENGTH_SHORT).show();
                }
            });
        }

        optionCheckboxes[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsManager.setKeepStockDPI(optionCheckboxes[2].isChecked());
            }
        });
        
        // Setup reset button if it exists
        ImageButton resetButton = findViewById(R.id.imageButtonReset);
        if (resetButton != null) {
            resetButton.setOnClickListener(v -> resetToOriginalSettings());
            resetButton.setContentDescription("Reset all settings to original values");
        }
    }
    
    /**
     * Reset all settings to original values
     */
    private void resetToOriginalSettings() {
        Logger.d(TAG, "Resetting to original settings");
        
        try {
            // Restore original performance settings
            GameAppManager.restoreOriginalPerformanceSettings(MainActivity.this);
            
            // Reset resolution
            boolean success = settingsManager.setScreenDimension(
                settingsManager.getOriginalHeight(), 
                settingsManager.getOriginalWidth()
            );
            
            if (success) {
                // Update UI
                resolutionSeekBar.setProgress(Constants.DEFAULT_RESOLUTION_SCALE);
                circularProgressBar.setProgress(Constants.DEFAULT_RESOLUTION_SCALE);
                lastProgress = Constants.DEFAULT_RESOLUTION_SCALE;
                
                fpsPercentageText.setText(UIUtils.formatFpsPercentage(0));
                updateTweakedResolutionDisplay(Constants.DEFAULT_RESOLUTION_SCALE);
                
                UIUtils.showToast(this, "Settings reset successfully");
                Logger.d(TAG, "Settings reset completed");
            } else {
                UIUtils.showToast(this, "Failed to reset some settings");
                Logger.e(TAG, "Failed to reset resolution settings");
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error resetting settings", e);
            UIUtils.showToast(this, "Error resetting settings");
        }
    }

    /**
     * Initialize recent games UI components
     */
    private void initializeRecentGames() {
        // Link recent games UI components
        recentGameTitles[0] = findViewById(R.id.textViewRecentGame1);
        recentGameTitles[1] = findViewById(R.id.textViewRecentGame2);
        recentGameTitles[2] = findViewById(R.id.textViewRecentGame3);
        recentGameTitles[3] = findViewById(R.id.textViewRecentGame4);
        recentGameTitles[4] = findViewById(R.id.textViewRecentGame5);
        recentGameTitles[5] = findViewById(R.id.textViewRecentGame6);

        recentGameIcons[0] = findViewById(R.id.imageViewRecentGame1);
        recentGameIcons[1] = findViewById(R.id.imageViewRecentGame2);
        recentGameIcons[2] = findViewById(R.id.imageViewRecentGame3);
        recentGameIcons[3] = findViewById(R.id.imageViewRecentGame4);
        recentGameIcons[4] = findViewById(R.id.imageViewRecentGame5);
        recentGameIcons[5] = findViewById(R.id.imageViewRecentGame6);

        loadRecentGamesUI();
        Logger.d(TAG, "Recent games initialized");
    }

    /**
     * Load and display recent games in UI
     */
    private void loadRecentGamesUI() {
        Logger.d(TAG, "Loading recent games UI");
        
        for (int i = 0; i < Constants.MAX_RECENT_GAMES; i++) {
            recentGameApps[i] = settingsManager.getRecentGameApp(i + 1);
            final int index = i;
            
            if (recentGameApps[i] != null) {
                // Game exists, set up game display
                setupGameSlot(index, recentGameApps[i]);
            } else {
                // Empty slot, set up add game display
                setupEmptySlot(index);
            }
        }
        
        Logger.d(TAG, "Recent games UI loaded");
    }
    
    /**
     * Setup a game slot with game information
     */
    private void setupGameSlot(int index, GameApp gameApp) {
        recentGameTitles[index].setText(gameApp.getGameName());
        recentGameIcons[index].setImageDrawable(gameApp.getIcon());
        
        // Set click listener to launch game
        recentGameIcons[index].setOnClickListener(view -> {
            Logger.d(TAG, "Launching game: " + gameApp.getGameName());
            GameAppManager.launchGameApp(MainActivity.this, gameApp.getPackageName());
        });
        
        // Set long click listener to show remove options
        recentGameIcons[index].setOnLongClickListener(view -> {
            showRemoveGamePopup(MainActivity.this, index);
            return true; // Consume the event
        });
        
        // Add content description for accessibility
        recentGameIcons[index].setContentDescription("Launch " + gameApp.getGameName());
    }
    
    /**
     * Setup an empty game slot
     */
    private void setupEmptySlot(int index) {
        recentGameTitles[index].setText(R.string.empty_recent_game);
        recentGameIcons[index].setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.empty_recent_game));
        
        // Set click listener to add game
        recentGameIcons[index].setOnClickListener(view -> showAddGame(true, index));
        
        // Remove long click listener
        recentGameIcons[index].setOnLongClickListener(null);
        
        // Add content description for accessibility
        recentGameIcons[index].setContentDescription("Add game to slot " + (index + 1));
    }

    /**
     * Set clickable state for recent game app icons
     */
    private void setRecentGameAppClickable(boolean state) {
        for (int i = 0; i < Constants.MAX_RECENT_GAMES; i++) {
            recentGameIcons[i].setClickable(state);
        }
        Logger.d(TAG, "Recent game apps clickable state set to: " + state);
    }

    /**
     * Show resolution reset popup dialog
     */
    private void showResetPopup() {
        Logger.d(TAG, "Showing resolution reset popup");
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_popup_title)
                .setMessage(R.string.reset_popup_text)
                .setPositiveButton(R.string.reset_popup_positive_choice, (dialog, which) -> {
                    Logger.d(TAG, "User chose to reset resolution");
                    dialog.dismiss();
                    
                    // Restore original performance settings and resolution
                    GameAppManager.restoreOriginalPerformanceSettings(MainActivity.this);
                    boolean success = settingsManager.setScreenDimension(
                        settingsManager.getOriginalHeight(), 
                        settingsManager.getOriginalWidth()
                    );
                    
                    if (success) {
                        UIUtils.showToast(this, "Resolution reset successfully");
                    } else {
                        UIUtils.showToast(this, "Failed to reset resolution");
                    }
                })
                .setNegativeButton(R.string.reset_popup_negative_choice, (dialog, which) -> {
                    Logger.d(TAG, "User chose to keep current resolution");
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Show no permissions popup dialog
     */
    private void showNoPermissionsPopup() {
        Logger.d(TAG, "Showing no permissions popup");
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_permission_popup_title)
                .setMessage(R.string.no_permission_popup_text)
                .setPositiveButton(R.string.no_permission_popup_positive_choice, (dialog, which) -> {
                    Logger.d(TAG, "User chose to view ADB Mode setup");
                    dialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, ADBModeActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.no_permission_popup_negative_choice, (dialog, which) -> {
                    Logger.d(TAG, "User chose to quit app");
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Show disclaimer popup dialog
     */
    private void showDisclaimerPopup() {
        Logger.d(TAG, "Showing disclaimer popup");
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.disclaimer_popup_title)
                .setMessage(R.string.disclaimer_popup_text)
                .setPositiveButton(R.string.disclaimer_popup_positive_choice, (dialog, which) -> {
                    Logger.d(TAG, "User accepted disclaimer");
                    dialog.dismiss();
                    
                    // Initialize first launch settings
                    settingsManager.initializeFirstLaunch();
                    
                    // Show performance recommendation if applicable
                    if (PerformanceUtils.shouldApplyPerformanceOptimizations(this)) {
                        showPerformanceRecommendationDialog();
                    }
                })
                .setNegativeButton(R.string.disclaimer_popup_negative_choice, (dialog, which) -> {
                    Logger.d(TAG, "User declined disclaimer");
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }
    
    /**
     * Show performance recommendation dialog for low-end devices
     */
    private void showPerformanceRecommendationDialog() {
        String recommendation = PerformanceUtils.getPerformanceProfileRecommendation(this);
        int recommendedScale = settingsManager.getRecommendedResolutionScale();
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.performance_recommendation_title)
                .setMessage(getString(R.string.performance_recommendation_message,
                        recommendation, recommendedScale))
                .setPositiveButton(R.string.apply_recommendation, (dialog, which) -> {
                    resolutionSeekBar.setProgress(recommendedScale);
                    updateProgressAndDisplay(recommendedScale);
                    UIUtils.showToast(this, getString(R.string.recommendation_applied));
                })
                .setNegativeButton(R.string.keep_current_settings, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showRemoveGamePopup(Context context, final int gameAppIndex){
        if (context == null) {
            Logger.e(TAG, "Context is null - cannot show remove game popup");
            return;
        }
        
        if (isFinishing() || isDestroyed()) {
            Logger.w(TAG, "Activity is finishing - cannot show dialog");
            return;
        }
        
        final Dialog gameListPopUp = new Dialog(this);

        final int xScreen = context.getResources().getDisplayMetrics().widthPixels;
        final int yScreen = context.getResources().getDisplayMetrics().heightPixels;

        gameListPopUp.setContentView(R.layout.add_game_layout);
        gameListPopUp.getWindow().setLayout(
            (int) Math.ceil(xScreen * Constants.DIALOG_WIDTH_RATIO),
            (int) Math.min(Math.ceil(yScreen * Constants.DIALOG_HEIGHT_RATIO), Constants.GAME_ITEM_HEIGHT * 2)
        );

        LinearLayout layout = gameListPopUp.findViewById(R.id.gameListLayout);
        LayoutInflater inflater = getLayoutInflater();



        //First the GameApp to show details
        View child = inflater.inflate(R.layout.game_app_item, null);
        TextView title = child.findViewById(R.id.textViewGameAppTitle);
        TextView packageName = child.findViewById(R.id.textViewGameAppPackageName);
        ImageView icon = child.findViewById(R.id.imageViewGameIcon);

        GameApp game = settingsManager.getRecentGameApp(gameAppIndex+1);
        if (game == null) {
            Logger.e(TAG, "Game not found at index: " + (gameAppIndex + 1));
            UIUtils.showToast(this, getString(R.string.game_not_found));
            gameListPopUp.dismiss();
            return;
        }

        title.setText(game.getGameName());
        packageName.setText(game.getPackageName());
        icon.setImageDrawable(game.getIcon());

        layout.addView(child);


        //Then a space
        child = new Space(this);
        child.setMinimumHeight(10);

        layout.addView(child);

        //Then the remove from list button
        child = inflater.inflate(R.layout.detail_item, null);
        title = child.findViewById(R.id.textViewGameAppTitle);
        packageName = child.findViewById(R.id.textViewGameAppPackageName);

        title.setText(getString(R.string.remove_popup_title));
        packageName.setText(getString(R.string.remove_popup_text));

        layout.addView(child);

        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeGameUI(gameAppIndex);
                gameListPopUp.dismiss();
            }
        });





        gameListPopUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        gameListPopUp.show();
    }

    /**
     * Show add game dialog
     */
    private void showAddGame(boolean onlyAddGames, int gameAppIndex) {
        Logger.d(TAG, "Showing add game dialog, onlyAddGames: " + onlyAddGames + ", index: " + gameAppIndex);
        
        try {
            gameList = GameAppManager.getGameApps(MainActivity.this, onlyAddGames);
            if (gameList.isEmpty()) {
                UIUtils.showToast(this, "No games found to add");
                return;
            }
            showGameListPopup(MainActivity.this, onlyAddGames, gameAppIndex);
        } catch (Exception e) {
            Logger.e(TAG, "Error showing add game dialog", e);
            UIUtils.showToast(this, "Error loading games");
        }
    }

    /**
     * Add game to UI and update recent games
     */
    private void addGameUI(String packageName, int index) {
        Logger.d(TAG, "Adding game to UI: " + packageName + " at index: " + index);
        
        try {
            settingsManager.addGameApp(packageName, index);
            loadRecentGamesUI();
            UIUtils.showToast(this, "Game added successfully");
        } catch (Exception e) {
            Logger.e(TAG, "Error adding game to UI", e);
            UIUtils.showToast(this, "Error adding game");
        }
    }

    /**
     * Remove game from UI and update recent games
     */
    private void removeGameUI(int index) {
        Logger.d(TAG, "Removing game from UI at index: " + index);
        
        if (settingsManager == null) {
            Logger.e(TAG, "SettingsManager is null - cannot remove game");
            UIUtils.showToast(this, getString(R.string.error_settings_not_initialized));
            return;
        }
        
        if (index < 0 || index >= Constants.MAX_RECENT_GAMES) {
            Logger.e(TAG, "Invalid game index for removal: " + index);
            UIUtils.showToast(this, getString(R.string.error_invalid_game_index));
            return;
        }
        
        try {
            settingsManager.removeGameApp(index);
            
            // Refresh UI on main thread
            if (!isFinishing() && !isDestroyed()) {
                runOnUiThread(() -> {
                    loadRecentGamesUI();
                    UIUtils.showToast(this, getString(R.string.game_removed_success));
                });
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error removing game from UI", e);
            if (!isFinishing() && !isDestroyed()) {
                runOnUiThread(() -> UIUtils.showToast(this, getString(R.string.error_removing_game)));
            }
        }
    }

    // --- Step 1: Add robust AppOpsManager-based permission check ---
    private boolean hasSecureSettingsPermission() {
        try {
            android.app.AppOpsManager appOps = (android.app.AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mode = appOps.unsafeCheckOpNoThrow("android:write_secure_settings",
                        getApplicationInfo().uid, getPackageName());
            } else {
                mode = appOps.checkOpNoThrow("android:write_secure_settings",
                        getApplicationInfo().uid, getPackageName());
            }
            Logger.d(TAG, "AppOpsManager mode: " + mode);
            return mode == android.app.AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Logger.e(TAG, "Failed to check WRITE_SECURE_SETTINGS", e);
            return false;
        }
    }

    // --- Step 2: Centralized permission check and dialog with SharedPreferences flag and reboot prompt ---
    private void checkPermissions() {
        android.content.SharedPreferences sharedPrefs = getSharedPreferences("eliteg_prefs", MODE_PRIVATE);
        boolean setupComplete = sharedPrefs.getBoolean("setup_complete", false);
        if (hasSecureSettingsPermission()) {
            if (!setupComplete) {
                sharedPrefs.edit().putBoolean("setup_complete", true).apply();
            }
            Logger.d(TAG, "WRITE_SECURE_SETTINGS permission granted");
            // Permission granted, proceed as normal
            return;
        }
        if (setupComplete) {
            // User had permission before, but now it's missing (rare). Show dialog anyway.
            Logger.d(TAG, "Permission lost after setup_complete. Showing dialog.");
        }
        // Show dialog only if permission is NOT granted
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required");
        builder.setMessage("EliteG needs ADB-granted permission to function.\n\nPlease run:\n\nadb shell pm grant " + getPackageName() + " android.permission.WRITE_SECURE_SETTINGS");
        builder.setCancelable(false);
        builder.setPositiveButton("Check Again", (dialog, which) -> {
            if (hasSecureSettingsPermission()) {
                sharedPrefs.edit().putBoolean("setup_complete", true).apply();
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                // Optional: reload activity or mark setup complete
            } else {
                Toast.makeText(this, "Permission not granted yet", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Exit App", (dialog, which) -> finish());
        builder.setNeutralButton("Restart Device", (dialog, which) -> {
            Toast.makeText(this, "Please reboot your device manually.", Toast.LENGTH_LONG).show();
        });
        builder.show();
    }
    
    @Override
    public void onBackPressed() {
        if (settingsShown) {
            // Close settings panel instead of exiting app
            settingsSwitch.performClick();
            return;
        }
        super.onBackPressed();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "MainActivity resumed");
        // Always check permission again when resuming (auto-dismiss if granted)
        checkPermissions();
        if (settingsManager != null) {
            // Refresh recent games in case something changed
            loadRecentGamesUI();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "MainActivity paused");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "MainActivity destroyed");
        
        // Critical: Clean up all references to prevent memory leaks
        cleanupResources();
    }
    
    /**
     * Comprehensive resource cleanup to prevent memory leaks
     */
    private void cleanupResources() {
        try {
            // Clear UI component references
            fpsPercentageText = null;
            tweakedResolutionText = null;
            resolutionSeekBar = null;
            circularProgressBar = null;
            settingsSwitch = null;
            
            // Clear arrays and lists
            if (optionCheckboxes != null) {
                for (int i = 0; i < optionCheckboxes.length; i++) {
                    optionCheckboxes[i] = null;
                }
                optionCheckboxes = null;
            }
            
            if (recentGameApps != null) {
                for (int i = 0; i < recentGameApps.length; i++) {
                    recentGameApps[i] = null;
                }
                recentGameApps = null;
            }
            
            if (recentGameTitles != null) {
                for (int i = 0; i < recentGameTitles.length; i++) {
                    recentGameTitles[i] = null;
                }
                recentGameTitles = null;
            }
            
            if (recentGameIcons != null) {
                for (int i = 0; i < recentGameIcons.length; i++) {
                    recentGameIcons[i] = null;
                }
                recentGameIcons = null;
            }
            
            // Clear game list
            if (gameList != null) {
                gameList.clear();
                gameList = null;
            }
            
            // Clear constraint sets
            layoutSettingsHidden = null;
            layoutSettingShown = null;
            
            // Clear coefficients array
            if (coefficients != null) {
                coefficients = null;
            }
            
            // Settings manager cleanup handled by its own lifecycle
            settingsManager = null;
            
            Logger.d(TAG, "Resource cleanup completed");
        } catch (Exception e) {
            Logger.e(TAG, "Error during resource cleanup", e);
        }
    }
    
    /**
     * Validate that all critical UI components are properly initialized
     */
    private boolean validateCriticalUIComponents() {
        String[] missingComponents = new String[5];
        int missingCount = 0;
        
        if (fpsPercentageText == null) missingComponents[missingCount++] = "fpsPercentageText";
        if (tweakedResolutionText == null) missingComponents[missingCount++] = "tweakedResolutionText";
        if (resolutionSeekBar == null) missingComponents[missingCount++] = "resolutionSeekBar";
        if (circularProgressBar == null) missingComponents[missingCount++] = "circularProgressBar";
        if (settingsSwitch == null) missingComponents[missingCount++] = "settingsSwitch";
        
        if (missingCount > 0) {
            StringBuilder errorMsg = new StringBuilder("Missing UI components: ");
            for (int i = 0; i < missingCount; i++) {
                errorMsg.append(missingComponents[i]);
                if (i < missingCount - 1) errorMsg.append(", ");
            }
            Logger.e(TAG, errorMsg.toString());
            return false;
        }
        return true;
    }

    /**
     * Get the current resolution scale from the seek bar
     */
    public int getResolutionScale() {
        return resolutionSeekBar != null ? resolutionSeekBar.getProgress() : Constants.DEFAULT_RESOLUTION_SCALE;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Logger.d(TAG, "Storage permissions granted");
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.storage_permission_rationale))
                            .setPositiveButton("Retry", (d, w) -> checkStoragePermissions())
                            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                            .show();
                } else {
                    Toast.makeText(this, getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_CODE);
            }
        } else {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_CODE);
            }
        }
    }
}