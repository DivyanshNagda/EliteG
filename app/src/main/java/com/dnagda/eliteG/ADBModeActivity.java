package com.dnagda.eliteG;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ADBModeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb_mode);

        TextView adbCommandText = findViewById(R.id.adb_command_text);
        Button btnCopy = findViewById(R.id.btn_copy_adb_command);
        TextView statusText = findViewById(R.id.adb_mode_status);
        TextView adbModeInstructions = findViewById(R.id.adb_mode_instructions);
        TextView adbModeTitle = findViewById(R.id.adb_mode_title);

        // Null checks for critical UI components
        if (adbCommandText == null || btnCopy == null) {
            Toast.makeText(this, "Error initializing ADB Mode", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set text content with null safety
        if (adbModeTitle != null) {
            adbModeTitle.setText(getString(R.string.adb_mode_title));
        }
        if (adbModeInstructions != null) {
            adbModeInstructions.setText(getString(R.string.adb_mode_setup_instruction));
        }
        
        String adbCommand = getString(R.string.adb_mode_command);
        adbCommandText.setText(adbCommand);
        btnCopy.setText(getString(R.string.adb_mode_copy));

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText("ADB Command", adbCommandText.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(ADBModeActivity.this, getString(R.string.adb_mode_copied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ADBModeActivity.this, "Clipboard not available", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ADBModeActivity.this, "Error copying command", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        if (statusText != null) {
            statusText.setText(""); // Placeholder for status
        }
    }
}
