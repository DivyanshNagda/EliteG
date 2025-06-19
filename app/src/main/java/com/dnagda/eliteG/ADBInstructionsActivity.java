package com.dnagda.eliteG;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dnagda.eliteG.utils.Logger;
import com.dnagda.eliteG.utils.UIUtils;

/**
 * Enhanced ADB instructions activity for EliteG.
 * Provides step-by-step instructions for granting ADB permissions.
 */
public class ADBInstructionsActivity extends AppCompatActivity {
    private static final String TAG = "ADBInstructions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "ADBInstructionsActivity onCreate started");
        
        try {
            setContentView(R.layout.activity_adb_instructions);
            Logger.d(TAG, "ADBInstructionsActivity onCreate completed");
        } catch (Exception e) {
            Logger.e(TAG, "Error in onCreate", e);
            UIUtils.showToast(this, getString(R.string.error_initializing_app));
            finish();
        }
    }
}