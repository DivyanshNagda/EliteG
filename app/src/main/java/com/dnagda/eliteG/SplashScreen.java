package com.dnagda.eliteG;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dnagda.eliteG.utils.Constants;
import com.dnagda.eliteG.utils.Logger;

/**
 * Enhanced splash screen activity for EliteG.
 * Provides smooth app initialization and permission checking.
 */
public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    private static final long SPLASH_DELAY_MS = 2000; // 2 seconds

    private Handler splashHandler;
    private Runnable splashRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "SplashScreen onCreate started");

        try {
            // Set the splash screen theme (handled by manifest)
            // No need to set content view as background is handled by theme
            
            initializeSplashTimer();
            
        } catch (Exception e) {
            Logger.e(TAG, "Error in splash screen onCreate", e);
            // If there's an error, proceed immediately
            proceedToNextActivity();
        }
    }

    /**
     * Initialize splash screen timer
     */
    private void initializeSplashTimer() {
        splashHandler = new Handler(Looper.getMainLooper());
        splashRunnable = this::proceedToNextActivity;
        
        // Show splash for specified duration
        splashHandler.postDelayed(splashRunnable, SPLASH_DELAY_MS);
        
        Logger.d(TAG, "Splash timer initialized for " + SPLASH_DELAY_MS + "ms");
    }

    /**
     * Proceed to the next activity based on app state
     */
    private void proceedToNextActivity() {
        Logger.d(TAG, "Proceeding to next activity");
        
        try {
            Intent nextIntent = new Intent(this, MainActivity.class);
            // Add flags to prevent returning to splash screen
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(nextIntent);
            finish();
            // Add smooth transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Logger.e(TAG, "Error proceeding to next activity", e);
            // Fallback to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up handler to prevent memory leaks
        if (splashHandler != null && splashRunnable != null) {
            splashHandler.removeCallbacks(splashRunnable);
        }
        
        Logger.d(TAG, "SplashScreen destroyed");
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash screen
        // This prevents users from accidentally exiting during initialization
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "SplashScreen paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "SplashScreen resumed");
    }
}
