package com.dnagda.eliteG;

import android.content.Context;
import java.io.*;
import com.dnagda.eliteG.utils.Logger;

public class Utils {
    public static void copyAssetToExternal(Context context, String assetName, String outPath) {
        try (InputStream in = context.getAssets().open(assetName);
             OutputStream out = new FileOutputStream(outPath)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            Logger.e("Utils", "Exception in utility method", e);
        }
    }
}
