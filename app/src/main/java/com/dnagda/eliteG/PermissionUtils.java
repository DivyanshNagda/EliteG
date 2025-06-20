package com.dnagda.eliteG;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class PermissionUtils {
    public static boolean hasSecureSettingsPermission(Context context) {
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(
                "android:write_secure_settings",
                context.getApplicationInfo().uid,
                context.getPackageName()
            );
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Log.e("PermissionCheck", "WRITE_SECURE_SETTINGS check failed", e);
            return false;
        }
    }
}
