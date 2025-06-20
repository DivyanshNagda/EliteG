package com.dnagda.eliteG.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.List;

/**
 * Accessibility utility class for EliteG.
 * Provides comprehensive accessibility support and WCAG compliance.
 */
public final class AccessibilityUtils {
    private static final String TAG = "AccessibilityUtils";
    
    // WCAG 2.1 compliance constants
    private static final double MIN_CONTRAST_RATIO_NORMAL = 4.5;
    private static final double MIN_CONTRAST_RATIO_LARGE = 3.0;
    private static final int MIN_TOUCH_TARGET_SIZE_DP = 48;
    
    // Prevent instantiation
    private AccessibilityUtils() {
        throw new AssertionError("AccessibilityUtils class should not be instantiated");
    }
    
    /**
     * Check if accessibility services are enabled
     */
    public static boolean isAccessibilityEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) 
            context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        
        if (manager == null) return false;
        
        return manager.isEnabled() && !manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_SPOKEN).isEmpty();
    }
    
    /**
     * Check if user is using large text
     */
    public static boolean isLargeTextEnabled(Context context) {
        Configuration config = context.getResources().getConfiguration();
        return config.fontScale >= 1.3f;
    }
    
    /**
     * Check if device is in high contrast mode
     */
    public static boolean isHighContrastEnabled(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AccessibilityManager manager = (AccessibilityManager) 
                    context.getSystemService(Context.ACCESSIBILITY_SERVICE);
                if (manager != null) {
                    // Use reflection for compatibility as this method might not be available
                    java.lang.reflect.Method method = manager.getClass().getMethod("isHighTextContrastEnabled");
                    return (Boolean) method.invoke(manager);
                }
            }
        } catch (Exception e) {
            Logger.d(TAG, "High contrast detection not available on this device");
        }
        return false;
    }
    
    /**
     * Calculate contrast ratio between two colors
     */
    public static double calculateContrastRatio(int foreground, int background) {
        double foregroundLum = calculateLuminance(foreground);
        double backgroundLum = calculateLuminance(background);
        
        double lighter = Math.max(foregroundLum, backgroundLum);
        double darker = Math.min(foregroundLum, backgroundLum);
        
        return (lighter + 0.05) / (darker + 0.05);
    }
    
    /**
     * Calculate relative luminance of a color
     */
    private static double calculateLuminance(int color) {
        double red = Color.red(color) / 255.0;
        double green = Color.green(color) / 255.0;
        double blue = Color.blue(color) / 255.0;
        
        red = (red <= 0.03928) ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        green = (green <= 0.03928) ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        blue = (blue <= 0.03928) ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);
        
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }
    
    /**
     * Validate if color combination meets WCAG contrast requirements
     */
    public static boolean isContrastSufficient(int foreground, int background, boolean isLargeText) {
        double ratio = calculateContrastRatio(foreground, background);
        double minRatio = isLargeText ? MIN_CONTRAST_RATIO_LARGE : MIN_CONTRAST_RATIO_NORMAL;
        return ratio >= minRatio;
    }
    
    /**
     * Set comprehensive accessibility properties for a view
     */
    public static void enhanceAccessibility(View view, String contentDescription, 
                                          String hint, boolean isClickable) {
        if (view == null) return;
        
        // Basic accessibility
        view.setContentDescription(contentDescription);
        ViewCompat.setImportantForAccessibility(view, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        
        // Clickable elements
        if (isClickable) {
            view.setClickable(true);
            view.setFocusable(true);
            ViewCompat.setAccessibilityDelegate(view, new androidx.core.view.AccessibilityDelegateCompat() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, 
                        AccessibilityNodeInfoCompat info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
                    info.setClickable(true);
                }
            });
        }
        
        // Edit text specific
        if (view instanceof EditText && hint != null) {
            ((EditText) view).setHint(hint);
        }
        
        // Ensure minimum touch target size
        ensureMinimumTouchTarget(view);
        
        Logger.d(TAG, "Enhanced accessibility for view: " + view.getClass().getSimpleName());
    }
    
    /**
     * Ensure view meets minimum touch target size requirements
     */
    public static void ensureMinimumTouchTarget(View view) {
        if (view == null) return;
        
        Context context = view.getContext();
        int minSize = UIUtils.dpToPx(context, MIN_TOUCH_TARGET_SIZE_DP);
        
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            if (params.width > 0 && params.width < minSize) {
                params.width = minSize;
            }
            if (params.height > 0 && params.height < minSize) {
                params.height = minSize;
            }
            view.setLayoutParams(params);
        }
        
        // Set minimum dimensions
        view.setMinimumWidth(minSize);
        view.setMinimumHeight(minSize);
    }
    
    /**
     * Apply high contrast styling if needed
     */
    public static void applyHighContrastIfNeeded(Context context, TextView textView) {
        if (!isHighContrastEnabled(context)) return;
        
        // Apply high contrast colors
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundColor(Color.BLACK);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        
        Logger.d(TAG, "Applied high contrast styling");
    }
    
    /**
     * Create accessible announcement
     */
    public static void announceForAccessibility(View view, String message) {
        if (view == null || message == null) return;
        
        view.announceForAccessibility(message);
        Logger.d(TAG, "Accessibility announcement: " + message);
    }
    
    /**
     * Validate accessibility of a view hierarchy
     */
    public static void validateAccessibility(ViewGroup parent, Context context) {
        if (parent == null) return;
        
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            
            // Check for missing content descriptions on interactive elements
            if ((child.isClickable() || child.isFocusable()) && 
                child.getContentDescription() == null) {
                Logger.w(TAG, "Interactive view missing content description: " + 
                         child.getClass().getSimpleName());
            }
            
            // Check touch target size
            if (child.isClickable()) {
                int width = child.getWidth();
                int height = child.getHeight();
                int minSize = UIUtils.dpToPx(context, MIN_TOUCH_TARGET_SIZE_DP);
                
                if (width > 0 && width < minSize || height > 0 && height < minSize) {
                    Logger.w(TAG, "Touch target too small: " + width + "x" + height + 
                           " (minimum: " + minSize + "x" + minSize + ")");
                }
            }
            
            // Recursively check child views
            if (child instanceof ViewGroup) {
                validateAccessibility((ViewGroup) child, context);
            }
        }
    }
    
    /**
     * Create accessible styled text
     */
    public static SpannableString createAccessibleStyledText(String text, 
                                                            boolean isBold, 
                                                            int color) {
        SpannableString spannable = new SpannableString(text);
        
        if (isBold) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
        }
        
        if (color != 0) {
            spannable.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);
        }
        
        return spannable;
    }
    
    /**
     * Set accessibility heading level
     */
    public static void setAccessibilityHeading(View view, boolean isHeading) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.setAccessibilityHeading(isHeading);
        } else {
            // Fallback for older versions
            ViewCompat.setAccessibilityDelegate(view, new androidx.core.view.AccessibilityDelegateCompat() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, 
                        AccessibilityNodeInfoCompat info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setHeading(isHeading);
                }
            });
        }
    }
    
    /**
     * Enable live region for dynamic content updates
     */
    public static void setLiveRegion(View view, int mode) {
        ViewCompat.setAccessibilityLiveRegion(view, mode);
    }
}
