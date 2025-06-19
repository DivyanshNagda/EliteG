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

        adbModeTitle.setText(getString(R.string.adb_mode_title));
        adbModeInstructions.setText(getString(R.string.adb_mode_setup_instruction));
        String adbCommand = getString(R.string.adb_mode_command);
        adbCommandText.setText(adbCommand);
        btnCopy.setText(getString(R.string.adb_mode_copy));

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ADB Command", adbCommandText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ADBModeActivity.this, getString(R.string.adb_mode_copied), Toast.LENGTH_SHORT).show();
            }
        });
        statusText.setText(""); // Placeholder for status
    }
}
