package com.dnagda.eliteG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class WriteSettings {
    public static boolean checkPermission(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.System.canWrite(context);
            } else {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    // Only works on rooted devices!
    public static void setPermissionByRoot(Context context) {
        // You must implement KeepShellPublic.doCmdSync or use your own shell exec method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // KeepShellPublic.doCmdSync("appops set " + context.getPackageName() + " WRITE_SETTINGS allow");
        } else {
            // KeepShellPublic.doCmdSync("pm grant " + context.getPackageName() + " android.permission.WRITE_SETTINGS");
        }
    }

    public static void requestPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Toast.makeText(context, "Please grant 'Modify system settings' permission for full functionality.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                // Handle error
            }
        } else {
            // KeepShellPublic.doCmdSync("pm grant " + context.getPackageName() + " android.permission.WRITE_SETTINGS");
        }
    }
}
