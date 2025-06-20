package com.dnagda.eliteG.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class for managing WRITE_SECURE_SETTINGS permission
 * and setup completion tracking.
 */
public class PermissionHelper {
    private static final String TAG = "PermissionHelper";
    private static final String PREF_NAME = "permission_setup";
    private static final String KEY_SETUP_COMPLETE = "setup_complete";

    private final Context context;
    private final SharedPreferences preferences;

    public PermissionHelper(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if WRITE_SECURE_SETTINGS permission is granted using the strict method.
     */
    public boolean isWriteSecureSettingsGranted() {
        boolean granted = PermissionUtils.isWriteSecureSettingsGranted(context);
        if (granted && !isSetupComplete()) {
            markSetupComplete();
        }
        return granted;
    }

    /**
     * Check if setup has been completed successfully.
     */
    public boolean isSetupComplete() {
        return preferences.getBoolean(KEY_SETUP_COMPLETE, false);
    }

    /**
     * Mark setup as complete.
     */
    public void markSetupComplete() {
        preferences.edit().putBoolean(KEY_SETUP_COMPLETE, true).apply();
        Logger.d(TAG, "Setup marked as complete");
    }

    /**
     * Check if the setup dialog should be shown.
     */
    public boolean shouldShowSetupDialog() {
        return !isWriteSecureSettingsGranted();
    }

    /**
     * Get the ADB command for granting the permission.
     */
    public static String getAdbCommand(Context context) {
        return "adb shell pm grant " + context.getPackageName() + " android.permission.WRITE_SECURE_SETTINGS";
    }
}
