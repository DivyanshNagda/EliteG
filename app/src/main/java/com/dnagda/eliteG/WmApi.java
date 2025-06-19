package com.dnagda.eliteG;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Display;

public class WmApi {
    private final ContentResolver contentResolver;
    private final int userId = -3;
    private final String[] blacklistGlobalSettings = new String[]{
            "hidden_api_policy",
            "hidden_api_policy_pre_p_apps",
            "hidden_api_policy_p_apps"
    };

    public WmApi(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    // If true, allow blacklisted APIs to be called
    public void setBypassBlacklist(boolean mode) {
        for (String setting : blacklistGlobalSettings) {
            Settings.Global.putInt(contentResolver, setting, mode ? 1 : 0);
        }
    }

    // Fetch the system WindowManager service
    @SuppressLint("PrivateApi")
    private Object getWindowManagerService() {
        try {
            return Class.forName("android.view.WindowManagerGlobal")
                    .getMethod("getWindowManagerService")
                    .invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    // Set display resolution
    @SuppressLint("PrivateApi")
    public void setDisplayResolution(int x, int y) {
        Object wmService = getWindowManagerService();
        if (wmService == null) return;
        try {
            Class.forName("android.view.IWindowManager")
                    .getMethod("setForcedDisplaySize", int.class, int.class, int.class)
                    .invoke(wmService, Display.DEFAULT_DISPLAY, x, y);
        } catch (Exception ignored) {}
    }

    // Clear display resolution
    @SuppressLint("PrivateApi")
    public void clearDisplayResolution() {
        Object wmService = getWindowManagerService();
        if (wmService == null) return;
        try {
            Class.forName("android.view.IWindowManager")
                    .getMethod("clearForcedDisplaySize", int.class)
                    .invoke(wmService, Display.DEFAULT_DISPLAY);
        } catch (Exception ignored) {}
    }

    // Set display density
    @SuppressLint("PrivateApi")
    public void setDisplayDensity(int density) {
        Object wmService = getWindowManagerService();
        if (wmService == null) return;
        // Try the old API for some devices
        try {
            Class.forName("android.view.IWindowManager")
                    .getMethod("setForcedDisplayDensity", int.class, int.class)
                    .invoke(wmService, Display.DEFAULT_DISPLAY, density);
        } catch (Exception ignored) {}
        try {
            Class.forName("android.view.IWindowManager")
                    .getMethod("setForcedDisplayDensityForUser", int.class, int.class, int.class)
                    .invoke(wmService, Display.DEFAULT_DISPLAY, density, userId);
        } catch (Exception ignored) {}
    }

    // Clear display density
    @SuppressLint("PrivateApi")
    public void clearDisplayDensity() {
        Object wmService = getWindowManagerService();
        if (wmService == null) return;
        // Try the old API for some devices
        try {
            Class.forName("android.view.IWindowManager")
                    .getMethod("clearForcedDisplayDensity", int.class)
                    .invoke(wmService, Display.DEFAULT_DISPLAY);
        } catch (Exception ignored) {}
        try {
            Class.forName("android.view.IWindowManager")
                    .getMethod("clearForcedDisplayDensityForUser", int.class, int.class)
                    .invoke(wmService, Display.DEFAULT_DISPLAY, userId);
        } catch (Exception ignored) {}
    }
}
