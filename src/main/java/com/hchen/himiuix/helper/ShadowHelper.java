/*
 * This file is part of HiMiuix.
 *
 * HiMiuix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * HiMiuix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with HiMiuix. If not, see <https://www.gnu.org/licenses/lgpl-2.1>.
 *
 * Copyright (C) 2023–2025 HChenX
 */
package com.hchen.himiuix.helper;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;

import com.hchen.himiuix.R;
import com.hchen.himiuix.utils.MiuixUtils;

/**
 * 阴影动画帮助程序
 *
 * @author 焕晨HChen
 */
public class ShadowHelper {
    private static final String TAG = "HiMiuix";
    private View targetView;
    private Drawable background;
    private int originalColor;
    private int shadowColor;
    private float initialX;
    private float initialY;
    private float touchSlop;
    private boolean isPressCandidate = false;
    private boolean isBackgroundChanged = false;
    private boolean shouldKeepShadow = false;
    private boolean isEnabled = true;
    private boolean isShadowEnabled = true;
    private int hapticFeedbackFlag = HapticFeedbackHelper.MIUI_HOLD;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ShadowAnimator shadowAnimator = new ShadowAnimator();
    private final Runnable touchDownRunnable = new Runnable() {
        @Override
        public void run() {
            shadowAnimator.endAny();
            shadowAnimator.startOriginalToShadow();
            isBackgroundChanged = true;
        }
    };
    private final Runnable touchUpRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPressCandidate) {
                if (targetView.isHapticFeedbackEnabled())
                    HapticFeedbackHelper.performHapticFeedback(targetView, hapticFeedbackFlag);
                shadowAnimator.startShadowToOriginal();
            }
            isPressCandidate = false;
            if (isBackgroundChanged)
                handler.post(touchCancelRunnable);
        }
    };
    private final Runnable touchCancelRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBackgroundChanged) {
                shadowAnimator.startShadowToOriginal();
                isBackgroundChanged = false;
            }
        }
    };

    private class ShadowAnimator {
        private ValueAnimator shadowAnimator;

        private void startOriginalToShadow() {
            if (!isShadowEnabled) return;
            if (shadowAnimator != null && shadowAnimator.isRunning())
                shadowAnimator.end();

            shadowAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), originalColor, shadowColor);
            shadowAnimator.setDuration(100);
            shadowAnimator.setInterpolator(new AccelerateInterpolator());
            shadowAnimator.addUpdateListener(animation ->
                setColor(background, (int) animation.getAnimatedValue())
            );
            shadowAnimator.start();
        }

        private void startShadowToOriginal() {
            if (!isShadowEnabled) return;
            if (shouldKeepShadow) {
                startOriginalToShadow();
                return;
            }
            if (shadowAnimator != null && shadowAnimator.isRunning())
                shadowAnimator.end();

            shadowAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), shadowColor, originalColor);
            shadowAnimator.setDuration(200);
            shadowAnimator.setInterpolator(new AccelerateInterpolator());
            shadowAnimator.addUpdateListener(animation ->
                setColor(background, (int) animation.getAnimatedValue())
            );
            shadowAnimator.start();
        }

        private void endAny() {
            if (shadowAnimator != null && shadowAnimator.isRunning())
                shadowAnimator.end();
        }
    }

    public static ShadowHelper init(@NonNull View targetView) {
        ShadowHelper shadowHelper = new ShadowHelper();
        shadowHelper.apply(targetView);
        return shadowHelper;
    }

    private void apply(@NonNull View targetView) {
        this.targetView = targetView;
        targetView.setClickable(true);

        background = targetView.getBackground();
        background = background.mutate();
        touchSlop = ViewConfiguration.get(targetView.getContext()).getScaledTouchSlop();
        if (background instanceof GradientDrawable drawable) {
            ColorStateList colorStateList = drawable.getColor();
            if (colorStateList != null)
                originalColor = colorStateList.getDefaultColor();
            else
                originalColor = targetView.getContext().getColor(R.color.miuix_basic_background_color);
        } else if (background instanceof ColorDrawable drawable)
            originalColor = drawable.getColor();


        // 清理
        shadowAnimator.endAny();
        setColor(background, originalColor);
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setShadowEnabled(boolean enabled) {
        isShadowEnabled = enabled;
    }

    public void setKeepShadow() {
        shouldKeepShadow = true;
    }

    public void restoreOriginalColor() {
        shouldKeepShadow = false;
        shadowAnimator.startShadowToOriginal();
    }

    public void setHapticFeedbackFlag(int hapticFeedbackFlag) {
        this.hapticFeedbackFlag = hapticFeedbackFlag;
    }

    public void onTouchEvent(MotionEvent event) {
        if (!isEnabled) return;
        if (!targetView.isEnabled()) return;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isPressCandidate = true;
                calculateShadowColor();
                initialX = event.getRawX();
                initialY = event.getRawY();
                handler.removeCallbacks(touchDownRunnable);
                handler.postDelayed(touchDownRunnable, 150);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isPressCandidate) {
                    float dx = event.getRawX() - initialX;
                    float dy = event.getRawY() - initialY;

                    if (Math.hypot(dx, dy) > touchSlop) {
                        isPressCandidate = false;
                        handler.removeCallbacks(touchDownRunnable);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(touchDownRunnable);
                handler.post(touchUpRunnable);
                break;
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(touchDownRunnable);
                handler.post(touchCancelRunnable);
                break;
        }
    }

    private void calculateShadowColor() {
        shadowColor = MiuixUtils.isDarkMode(targetView.getResources()) ?
            lightenColor(originalColor, 1.566f) :
            darkenColor(originalColor, 0.899f);
    }

    private int lightenColor(int color, float factor) {
        int r = Math.min(255, (int) (Color.red(color) * factor));
        int g = Math.min(255, (int) (Color.green(color) * factor));
        int b = Math.min(255, (int) (Color.blue(color) * factor));
        return Color.rgb(r, g, b);
    }

    private int darkenColor(int color, float factor) {
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.rgb(r, g, b);
    }

    private void setColor(Drawable drawable, int color) {
        if (drawable instanceof GradientDrawable gradientDrawable)
            gradientDrawable.setColor(color);
        else if (drawable instanceof ColorDrawable colorDrawable)
            colorDrawable.setColor(color);
    }
}
