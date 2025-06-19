package com.dnagda.eliteG;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dnagda.eliteG.utils.Logger;

/**
 * Enhanced wrapper drawable for EliteG.
 * Provides a wrapper around existing drawables with improved bounds handling.
 */
public class WrappedDrawable extends Drawable {
    private static final String TAG = "WrappedDrawable";

    private final Drawable wrappedDrawable;

    /**
     * Get the wrapped drawable
     * @return The wrapped drawable instance
     */
    @Nullable
    protected Drawable getDrawable() {
        return wrappedDrawable;
    }

    /**
     * Create a wrapped drawable
     * @param drawable The drawable to wrap
     */
    public WrappedDrawable(@Nullable Drawable drawable) {
        super();
        this.wrappedDrawable = drawable;
        
        if (drawable == null) {
            Logger.w(TAG, "WrappedDrawable created with null drawable");
        }
    }

    /**
     * Create a wrapped drawable with specific bounds
     * @param drawable The drawable to wrap
     * @param left Left bound
     * @param top Top bound
     * @param right Right bound
     * @param bottom Bottom bound
     */
    public WrappedDrawable(@Nullable Drawable drawable, int left, int top, int right, int bottom) {
        this(drawable);
        setBounds(left, top, right, bottom);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        
        if (wrappedDrawable != null) {
            wrappedDrawable.setBounds(left, top, right, bottom);
        }
    }

    @Override
    public void setBounds(@NonNull Rect bounds) {
        super.setBounds(bounds);
        
        if (wrappedDrawable != null) {
            wrappedDrawable.setBounds(bounds);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (wrappedDrawable != null) {
            wrappedDrawable.setAlpha(alpha);
        }
    }

    @Override
    public int getAlpha() {
        return wrappedDrawable != null ? wrappedDrawable.getAlpha() : 255;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (wrappedDrawable != null) {
            wrappedDrawable.setColorFilter(colorFilter);
        }
    }

    @Nullable
    @Override
    public ColorFilter getColorFilter() {
        return wrappedDrawable != null ? wrappedDrawable.getColorFilter() : null;
    }

    @Override
    public int getOpacity() {
        return wrappedDrawable != null ? wrappedDrawable.getOpacity() : PixelFormat.UNKNOWN;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (wrappedDrawable != null) {
            wrappedDrawable.draw(canvas);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        if (wrappedDrawable != null) {
            int intrinsicWidth = wrappedDrawable.getIntrinsicWidth();
            return intrinsicWidth > 0 ? intrinsicWidth : wrappedDrawable.getBounds().width();
        }
        return 0;
    }

    @Override
    public int getIntrinsicHeight() {
        if (wrappedDrawable != null) {
            int intrinsicHeight = wrappedDrawable.getIntrinsicHeight();
            return intrinsicHeight > 0 ? intrinsicHeight : wrappedDrawable.getBounds().height();
        }
        return 0;
    }

    @Override
    public boolean isStateful() {
        return wrappedDrawable != null && wrappedDrawable.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (wrappedDrawable != null && wrappedDrawable.isStateful()) {
            return wrappedDrawable.setState(state);
        }
        return false;
    }

    @Override
    public void invalidateSelf() {
        super.invalidateSelf();
        if (wrappedDrawable != null) {
            wrappedDrawable.invalidateSelf();
        }
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (wrappedDrawable != null) {
            wrappedDrawable.setVisible(visible, restart);
        }
        return changed;
    }

    /**
     * Check if the wrapped drawable is valid
     * @return true if the wrapped drawable is not null
     */
    public boolean isValid() {
        return wrappedDrawable != null;
    }
}
