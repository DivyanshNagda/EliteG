package com.dnagda.eliteG.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

/**
 * Utility class for checking special Android permissions, especially ADB-granted ones.
 */
public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    /**
     * Strictly checks if WRITE_SECURE_SETTINGS is granted via ADB (AppOpsManager only).
     * This is the only reliable way for this permission.
     *
     * @param context Application context
     * @return true if granted, false otherwise
     */
    public static boolean isWriteSecureSettingsGranted(Context context) {
        if (context == null) return false;
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (appOps != null) {
                int mode;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mode = appOps.unsafeCheckOpNoThrow("android:write_secure_settings",
                            android.os.Process.myUid(), context.getPackageName());
                } else {
                    mode = appOps.checkOpNoThrow("android:write_secure_settings",
                            android.os.Process.myUid(), context.getPackageName());
                }
                boolean granted = (mode == AppOpsManager.MODE_ALLOWED);
                Log.d(TAG, "WRITE_SECURE_SETTINGS AppOpsManager mode: " + mode + ", granted: " + granted);
                if (!granted) {
                    Log.w(TAG, "WRITE_SECURE_SETTINGS is NOT granted via ADB");
                }
                return granted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking WRITE_SECURE_SETTINGS via AppOpsManager", e);
        }
        Log.w(TAG, "WRITE_SECURE_SETTINGS is NOT granted (AppOpsManager unavailable)");
        return false;
    }

    /**
     * Robust check for WRITE_SECURE_SETTINGS: tries to write a dummy value to Settings.Secure.
     * Returns true if successful, false if SecurityException or any error.
     */
    public static boolean hasSecureSettingsPermission(Context context) {
        try {
            Settings.Secure.putInt(
                context.getContentResolver(),
                "test_write_secure_settings_permission",
                1
            );
            Log.d(TAG, "WRITE_SECURE_SETTINGS granted: true");
            return true;
        } catch (SecurityException e) {
            Log.w(TAG, "WRITE_SECURE_SETTINGS not granted (SecurityException)", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error checking WRITE_SECURE_SETTINGS", e);
            return false;
        }
    }
}
