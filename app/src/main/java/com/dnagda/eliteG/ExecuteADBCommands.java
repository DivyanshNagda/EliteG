package com.dnagda.eliteG;

import com.dnagda.eliteG.utils.Constants;
import com.dnagda.eliteG.utils.Logger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Enhanced ADB command execution utility for EliteG.
 * Provides robust command execution with proper error handling, timeout management,
 * background threading, and security validation.
 */
public class ExecuteADBCommands {
    private static final String TAG = "ADBCommands";
    
    // Thread pool for background ADB operations
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    
    // Security: Allowed ADB command prefixes
    private static final Set<String> ALLOWED_COMMANDS = new HashSet<>(Arrays.asList(
        "settings", "wm", "am", "pm", "dumpsys", "getprop"
    ));
    
    // Pattern for command sanitization
    private static final Pattern DANGEROUS_CHARS = Pattern.compile("[;&|`$<>]");

    // Prevent instantiation
    private ExecuteADBCommands() {
        throw new AssertionError("ExecuteADBCommands class should not be instantiated");
    }
    
    /**
     * Callback interface for async operations
     */
    public interface ADBCallback {
        void onSuccess(CommandResult result);
        void onError(String error);
    }
    
    /**
     * Cleanup resources when app is destroyed
     */
    public static void cleanup() {
        if (!EXECUTOR.isShutdown()) {
            EXECUTOR.shutdown();
            try {
                if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                    EXECUTOR.shutdownNow();
                }
            } catch (InterruptedException e) {
                EXECUTOR.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Result class for command execution
     */
    public static class CommandResult {
        public final boolean success;
        public final int exitCode;
        public final String output;
        public final String error;

        public CommandResult(boolean success, int exitCode, String output, String error) {
            this.success = success;
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }
    }

    /**
     * Validate and sanitize ADB command for security
     */
    private static boolean isValidCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        // Check for dangerous characters
        if (DANGEROUS_CHARS.matcher(command).find()) {
            Logger.w(TAG, "Command contains dangerous characters: " + command);
            return false;
        }
        
        // Check if command starts with allowed prefix
        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0) {
            return false;
        }
        
        String commandPrefix = parts[0];
        boolean isAllowed = ALLOWED_COMMANDS.contains(commandPrefix);
        
        if (!isAllowed) {
            Logger.w(TAG, "Command not in allowed list: " + commandPrefix);
        }
        
        return isAllowed;
    }
    
    /**
     * Sanitize command input
     */
    private static String sanitizeCommand(String command) {
        if (command == null) {
            return "";
        }
        // Remove dangerous characters and trim
        return DANGEROUS_CHARS.matcher(command.trim()).replaceAll("");
    }
    // --- End of WriteSettings functionality ---

    /**
     * Check if ADB permissions are granted (specifically WRITE_SECURE_SETTINGS)
     */
    public static boolean hasADBPermissions() {
        try {
            // Fix: Use pure Java filtering, avoid pipe/grep for compatibility
            CommandResult result = executeWithResult("dumpsys package " + Constants.APP_PACKAGE_NAME);
            return result.success && result.output.contains("WRITE_SECURE_SETTINGS") 
                && result.output.contains("granted=true");
        } catch (Exception e) {
            Logger.e(TAG, "Error checking ADB permissions", e);
            return false;
        }
    }
    
    /**
     * Async version of hasADBPermissions
     */
    public static void hasADBPermissionsAsync(ADBCallback callback) {
        CompletableFuture.supplyAsync(() -> {
            try {
                CommandResult result = executeWithResult(Constants.ADB_COMMAND_SETTINGS_GET);
                return result;
            } catch (Exception e) {
                Logger.e(TAG, "Error checking ADB permissions", e);
                return new CommandResult(false, -1, "", e.getMessage());
            }
        }, EXECUTOR).thenAccept(result -> {
            if (result.success && result.exitCode == Constants.ERROR_CODE_SUCCESS) {
                callback.onSuccess(result);
            } else {
                callback.onError("ADB permissions not granted");
            }
        }).exceptionally(throwable -> {
            callback.onError("Error checking ADB permissions: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Execute a single ADB command
     */
    public static boolean execute(String command) {
        return execute(new ArrayList<>(Collections.singletonList(command)));
    }

    /**
     * Execute multiple ADB commands
     */
    public static boolean execute(List<String> commands) {
        boolean overallSuccess = true;
        
        for (String command : commands) {
            CommandResult result = executeWithResult(command);
            if (!result.success) {
                overallSuccess = false;
                Logger.e(TAG, "Command failed: " + command + " | Exit code: " + result.exitCode + " | Error: " + result.error);
            } else {
                Logger.d(TAG, "Command succeeded: " + command);
            }
        }
        
        return overallSuccess;
    }
    
    /**
     * Execute a single ADB command asynchronously
     */
    public static void executeAsync(String command, ADBCallback callback) {
        executeAsync(new ArrayList<>(Collections.singletonList(command)), callback);
    }
    
    /**
     * Execute multiple ADB commands asynchronously
     */
    public static void executeAsync(List<String> commands, ADBCallback callback) {
        CompletableFuture.supplyAsync(() -> {
            boolean overallSuccess = true;
            StringBuilder errorMessages = new StringBuilder();
            CommandResult lastResult = null;
            for (String command : commands) {
                CommandResult result = executeWithResult(command);
                lastResult = result;
                if (!result.success) {
                    overallSuccess = false;
                    errorMessages.append("Command failed: ").append(command)
                                 .append(" | Error: ").append(result.error).append(System.lineSeparator());
                    Logger.e(TAG, "Command failed: " + command + " | Exit code: " + result.exitCode + " | Error: " + result.error);
                } else {
                    Logger.d(TAG, "Command succeeded: " + command);
                }
            }
            if (overallSuccess) {
                return lastResult != null ? lastResult : new CommandResult(true, 0, "All commands executed successfully", "");
            } else {
                return new CommandResult(false, -1, "", errorMessages.toString());
            }
        }, EXECUTOR).thenAccept(result -> {
            if (result.success) {
                callback.onSuccess(result);
            } else {
                callback.onError(result.error);
            }
        }).exceptionally(throwable -> {
            callback.onError("Error executing commands: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Execute a command and return detailed result
     */
    public static CommandResult executeWithResult(String command) {
        // Validate and sanitize command
        if (!isValidCommand(command)) {
            String error = "Invalid or unsafe command: " + command;
            Logger.e(TAG, error);
            return new CommandResult(false, -1, "", error);
        }
        
        String sanitizedCommand = sanitizeCommand(command);
        Logger.logAdbCommand(sanitizedCommand);
        
        try {
            Process process = Runtime.getRuntime().exec(sanitizedCommand);
            
            // Set timeout for command execution
            boolean finished = process.waitFor(Constants.ADB_COMMAND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                Logger.e(TAG, "Command timed out: " + sanitizedCommand);
                return new CommandResult(false, -1, "", "Command timed out");
            }
            
            // Read output streams
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());
            int exitCode = process.exitValue();
            
            boolean success = exitCode == Constants.ERROR_CODE_SUCCESS;
            Logger.logAdbResult(sanitizedCommand, exitCode, output);
            
            return new CommandResult(success, exitCode, output, error);
            
        } catch (IOException e) {
            Logger.e(TAG, "IOException executing command: " + sanitizedCommand, e);
            return new CommandResult(false, -1, "", e.getMessage());
        } catch (InterruptedException e) {
            Logger.e(TAG, "InterruptedException executing command: " + sanitizedCommand, e);
            Thread.currentThread().interrupt();
            return new CommandResult(false, -1, "", e.getMessage());
        } catch (SecurityException e) {
            Logger.e(TAG, "SecurityException executing command: " + sanitizedCommand, e);
            return new CommandResult(false, -1, "", "Security error: " + e.getMessage());
        }
    }

    // Fix 1: Use correct line separator in readStream()
    private static String readStream(java.io.InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator()); // FIXED
            }
        }
        return output.toString().trim();
    }

    /**
     * Change display resolution
     */
    public static boolean changeResolution(int width, int height) {
        if (width <= 0 || height <= 0) {
            Logger.e(TAG, "Invalid resolution dimensions: " + width + "x" + height);
            return false;
        }
        String command = Constants.ADB_COMMAND_WM_SIZE + " " + width + "x" + height;
        return execute(command);
    }
    
    /**
     * Change display resolution asynchronously
     */
    public static void changeResolutionAsync(int width, int height, ADBCallback callback) {
        if (width <= 0 || height <= 0) {
            callback.onError("Invalid resolution dimensions: " + width + "x" + height);
            return;
        }
        String command = Constants.ADB_COMMAND_WM_SIZE + " " + width + "x" + height;
        executeAsync(command, callback);
    }

    /**
     * Reset resolution to default
     */
    public static boolean resetResolution() {
        return execute(Constants.ADB_COMMAND_WM_SIZE_RESET);
    }
    
    /**
     * Reset resolution to default asynchronously
     */
    public static void resetResolutionAsync(ADBCallback callback) {
        executeAsync(Constants.ADB_COMMAND_WM_SIZE_RESET, callback);
    }

    /**
     * Change display density (DPI)
     */
    public static boolean changeDensity(int density) {
        if (density <= 0 || density > 1000) {
            Logger.e(TAG, "Invalid density value: " + density);
            return false;
        }
        String command = Constants.ADB_COMMAND_WM_DENSITY + " " + density;
        return execute(command);
    }
    
    /**
     * Change display density (DPI) asynchronously
     */
    public static void changeDensityAsync(int density, ADBCallback callback) {
        if (density <= 0 || density > 1000) {
            callback.onError("Invalid density value: " + density);
            return;
        }
        String command = Constants.ADB_COMMAND_WM_DENSITY + " " + density;
        executeAsync(command, callback);
    }

    /**
     * Reset density to default
     */
    public static boolean resetDensity() {
        return execute(Constants.ADB_COMMAND_WM_DENSITY_RESET);
    }
    
    /**
     * Reset density to default asynchronously
     */
    public static void resetDensityAsync(ADBCallback callback) {
        executeAsync(Constants.ADB_COMMAND_WM_DENSITY_RESET, callback);
    }

    /**
     * Change font scale
     */
    public static boolean changeFontScale(float scale) {
        String command = Constants.ADB_COMMAND_FONT_SCALE + scale;
        return execute(command);
    }

    /**
     * Kill all background apps
     */
    public static boolean killBackgroundApps() {
        return execute(Constants.ADB_COMMAND_KILL_ALL);
    }

    /**
     * Force stop a specific app
     */
    public static boolean forceStopApp(String packageName) {
        String command = Constants.ADB_COMMAND_FORCE_STOP + packageName;
        return execute(command);
    }

    /**
     * Apply performance optimizations
     */
    public static boolean applyPerformanceOptimizations() {
        List<String> commands = new ArrayList<>();
        
        // Reduce animations for better performance
        commands.add(Constants.SETTING_WINDOW_ANIMATION_SCALE + Constants.ANIMATION_SCALE_DISABLED);
        commands.add(Constants.SETTING_TRANSITION_ANIMATION_SCALE + Constants.ANIMATION_SCALE_DISABLED);
        commands.add(Constants.SETTING_ANIMATOR_DURATION_SCALE + Constants.ANIMATION_SCALE_DISABLED);
        
        // Optimize memory management
        commands.add(Constants.SETTING_LOW_POWER_MODE + "0");
        
        // Disable background app refresh for better performance
        commands.add(Constants.SETTING_BACKGROUND_APP_REFRESH + "1");
        
        return execute(commands);
    }

    /**
     * Reset performance optimizations to default
     */
    public static boolean resetPerformanceOptimizations() {
        List<String> commands = new ArrayList<>();
        
        // Reset animations to default
        commands.add(Constants.SETTING_WINDOW_ANIMATION_SCALE + Constants.ANIMATION_SCALE_DEFAULT);
        commands.add(Constants.SETTING_TRANSITION_ANIMATION_SCALE + Constants.ANIMATION_SCALE_DEFAULT);
        commands.add(Constants.SETTING_ANIMATOR_DURATION_SCALE + Constants.ANIMATION_SCALE_DEFAULT);
        
        // Reset other settings
        commands.add(Constants.SETTING_BACKGROUND_APP_REFRESH + "0");
        
        return execute(commands);
    }

    /**
     * Get current resolution as string
     */
    public static String getCurrentResolution() {
        CommandResult result = executeWithResult(Constants.ADB_COMMAND_WM_SIZE);
        if (result.success && result.output.contains("Physical size:")) {
            // Use regex split for all platforms
            String[] lines = result.output.split("\\r?\\n");
            for (String line : lines) {
                if (line.contains("Physical size:")) {
                    return line.substring(line.indexOf(":") + 1).trim();
                }
            }
        }
        Logger.w(TAG, "Could not get current resolution: " + result.error);
        return "Unknown";
    }

    /**
     * Get current density as string
     */
    public static String getCurrentDensity() {
        CommandResult result = executeWithResult(Constants.ADB_COMMAND_WM_DENSITY);
        if (result.success && result.output.contains("Physical density:")) {
            String[] lines = result.output.split("\\r?\\n");
            for (String line : lines) {
                if (line.contains("Physical density:")) {
                    return line.substring(line.indexOf(":") + 1).trim();
                }
            }
        }
        Logger.w(TAG, "Could not get current density: " + result.error);
        return "Unknown";
    }

    /**
     * Test ADB connectivity
     */
    public static boolean testADBConnectivity() {
        CommandResult result = executeWithResult("echo test");
        return result.success && result.output.contains("test");
    }

    /**
     * Get device information
     */
    public static String getDeviceInfo() {
        CommandResult result = executeWithResult("getprop ro.build.version.release");
        return result.success ? result.output : "Unknown";
    }

    /**
     * Check if specific permission is granted
     */
    public static boolean hasPermission(String permission) {
        // Avoid using | grep, use Java filtering
        CommandResult result = executeWithResult("dumpsys package " + Constants.APP_PACKAGE_NAME);
        return result.success && result.output.contains(permission) && result.output.contains("granted=true");
    }

    /**
     * Check if app has WRITE_SETTINGS permission
     */
    public static boolean hasWriteSettingsPermission(android.content.Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return android.provider.Settings.System.canWrite(context);
        } else {
            // Pre-Marshmallow devices automatically grant this permission
            return true;
        }
    }

    /**
     * Request WRITE_SETTINGS permission from user
     */
    public static void requestWriteSettingsPermission(android.content.Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                android.widget.Toast.makeText(context, "Please grant 'Modify system settings' permission for full functionality.", android.widget.Toast.LENGTH_LONG).show();
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                Logger.e(TAG, "Error starting write settings activity", ex);
            }
        }
    }

    /**
     * Grant WRITE_SETTINGS permission via root shell (for rooted devices)
     */
    public static void grantWriteSettingsPermissionByRoot(android.content.Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String cmd = "appops set " + context.getPackageName() + " WRITE_SETTINGS allow";
            CommandResult result = executeWithResult(cmd);
            Logger.d(TAG, "Root permission grant result: " + result.output);
        } else {
            String cmd = "pm grant " + context.getPackageName() + " android.permission.WRITE_SETTINGS";
            executeWithResult(cmd);
        }
    }

    // Fix 8: Register shutdown hook for graceful shutdown
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ExecuteADBCommands::cleanup));
    }
}