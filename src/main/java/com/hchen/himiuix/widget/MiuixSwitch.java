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
package com.hchen.himiuix.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnStateChangeListener;
import com.hchen.himiuix.helper.HapticFeedbackHelper;

/**
 * Miuix Switch
 *
 * @author 焕晨HChen
 */
public class MiuixSwitch extends LinearLayout {
    static final String TAG = "HiMiuix";
    private View thumbView;
    private int THUMB_MARGINS;
    private int movableDistance = 0;
    private final int ANIMATION_DURATION = 280;
    private final float ANIMATION_TENSION = 1.2f;
    private ViewPropertyAnimator thumbViewAnimator;
    private ValueAnimator valueAnimator;
    private boolean isChecked;
    private TransitionDrawable offToOnTransition;
    private TransitionDrawable onToOffTransition;
    private OnStateChangeListener onStateChangeListener;

    public MiuixSwitch(Context context) {
        this(context, null);
    }

    public MiuixSwitch(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setClickable(true);
        setHapticFeedbackEnabled(true);
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams params = new LayoutParams(
            getContext().getResources().getDimensionPixelSize(R.dimen.miuix_switch_width),
            getContext().getResources().getDimensionPixelSize(R.dimen.miuix_switch_height)
        );
        setLayoutParams(params);
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.miuix_switch_background_off));

        thumbView = new View(getContext()) {
            private float switchViewX;
            private float maxMoveX;
            private float minMoveX;
            private boolean isMoved;
            private boolean shouldHapticFeedback;
            private final int[] location = new int[2];

            @Override
            public boolean dispatchTouchEvent(MotionEvent event) {
                if (!isEnabled()) return true;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN -> {
                        isMoved = false;
                        getLocationInWindow(location);
                        switchViewX = location[0];
                        maxMoveX = movableDistance + THUMB_MARGINS;
                        minMoveX = THUMB_MARGINS;
                        zoomAnimation();
                        getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE -> {
                        isMoved = true;
                        float moveX = event.getRawX() - switchViewX;
                        if (moveX > maxMoveX) {
                            moveX = maxMoveX;
                            hapticFeedbackIfNeed();
                        } else if (moveX < minMoveX) {
                            moveX = minMoveX;
                            hapticFeedbackIfNeed();
                        } else shouldHapticFeedback = true;
                        setX(moveX);
                        return true;
                    }
                    case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        getParent().requestDisallowInterceptTouchEvent(false);
                        revertAnimation();
                        if (isMoved) {
                            float finalX = getX();
                            boolean change = false;
                            if (finalX < (minMoveX + maxMoveX) / 2) {
                                finalX = minMoveX;
                            } else {
                                finalX = maxMoveX;
                                change = true;
                            }
                            thumbViewAnimator.x(finalX);
                            if (change != isChecked()) {
                                if (!MiuixSwitch.this.setChecked(!isChecked())) {
                                    if (isChecked()) thumbViewAnimator.x(maxMoveX);
                                    else thumbViewAnimator.x(minMoveX);
                                }
                            }
                        } else {
                            MiuixSwitch.this.setChecked(!isChecked());
                            MiuixSwitch.this.performHapticFeedback();
                        }
                        return true;
                    }
                }
                return super.dispatchTouchEvent(event);
            }

            @Override
            protected boolean dispatchHoverEvent(MotionEvent event) {
                if (!isEnabled()) return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER -> {
                        zoomAnimation();
                        return true;
                    }
                    case MotionEvent.ACTION_HOVER_EXIT -> {
                        revertAnimation();
                        return true;
                    }
                }
                return super.dispatchHoverEvent(event);
            }

            private void hapticFeedbackIfNeed() {
                if (!shouldHapticFeedback) return;
                MiuixSwitch.this.performHapticFeedback();
                shouldHapticFeedback = false;
            }

            private void zoomAnimation() {
                thumbViewAnimator.scaleX(1.1f).scaleY(1.1f);
            }

            private void revertAnimation() {
                thumbViewAnimator.scaleX(1f).scaleY(1f);
            }
        };
        THUMB_MARGINS = getContext().getResources().getDimensionPixelSize(R.dimen.miuix_switch_thumb_margin);
        params = new LayoutParams(
            getContext().getResources().getDimensionPixelSize(R.dimen.miuix_switch_thumb_width),
            getContext().getResources().getDimensionPixelSize(R.dimen.miuix_switch_thumb_height)
        );
        params.setMargins(THUMB_MARGINS, THUMB_MARGINS, THUMB_MARGINS, THUMB_MARGINS);
        thumbView.setLayoutParams(params);
        thumbView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.miuix_thumb));
        addView(thumbView);

        post(() -> {
            movableDistance = getWidth() - thumbView.getWidth() - THUMB_MARGINS * 2;
            valueAnimator = ValueAnimator.ofInt(0, movableDistance);
            valueAnimator.setDuration(ANIMATION_DURATION);
            valueAnimator.setInterpolator(new AnticipateOvershootInterpolator(ANIMATION_TENSION));
        });

        thumbViewAnimator = thumbView.animate()
            .setInterpolator(new AnticipateOvershootInterpolator(ANIMATION_TENSION))
            .setDuration(ANIMATION_DURATION);
        offToOnTransition = new TransitionDrawable(new Drawable[]{
            ContextCompat.getDrawable(getContext(), R.drawable.miuix_switch_background_off),
            ContextCompat.getDrawable(getContext(), R.drawable.miuix_switch_background_on)
        });
        onToOffTransition = new TransitionDrawable(new Drawable[]{
            ContextCompat.getDrawable(getContext(), R.drawable.miuix_switch_background_on),
            ContextCompat.getDrawable(getContext(), R.drawable.miuix_switch_background_off)
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (thumbView != null)
            thumbView.setEnabled(enabled);
    }

    @Override
    public boolean performClick() {
        setChecked(!isChecked());
        performHapticFeedback();

        return super.performClick();
    }

    public boolean isChecked() {
        return isChecked;
    }

    /**
     * 当且仅当 onStateChange 拦截操作时才会返回 false
     */
    public boolean setChecked(boolean checked) {
        final boolean changed = isChecked != checked;
        if (changed) {
            if (onStateChangeListener == null || onStateChangeListener.onStateChange(checked)) {
                isChecked = checked;
                showThumbAnimation(isChecked, true);
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * 仅供内部使用
     */
    public void setCheckedNoAnimation(boolean checked) {
        final boolean changed = isChecked != checked;
        if (changed) {
            isChecked = checked;
            showThumbAnimation(isChecked, false);
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        onStateChangeListener = listener;
    }

    private void performHapticFeedback() {
        if (isHapticFeedbackEnabled())
            HapticFeedbackHelper.performHapticFeedback(this, HapticFeedbackHelper.MIUI_FLICK);
    }

    private void showThumbAnimation(boolean toRight, boolean showAnimation) {
        if (valueAnimator != null && showAnimation) {
            if (valueAnimator.isRunning()) valueAnimator.end();

            valueAnimator.removeAllUpdateListeners();
            valueAnimator.addUpdateListener(animation -> {
                int x = (int) animation.getAnimatedValue();
                thumbView.setTranslationX(x);
            });
            if (toRight) valueAnimator.start();
            else valueAnimator.reverse();
        } else {
            thumbView.post(() -> {
                if (toRight) thumbView.setTranslationX(movableDistance);
                else thumbView.setTranslationX(0);
            });
        }
        updateSwitchBackground(showAnimation);
    }

    private void updateSwitchBackground(boolean showAnimation) {
        if (valueAnimator != null && showAnimation) {
            if (isChecked()) {
                setBackground(offToOnTransition);
                offToOnTransition.startTransition(ANIMATION_DURATION);
            } else {
                setBackground(onToOffTransition);
                onToOffTransition.startTransition(ANIMATION_DURATION);
            }
        } else {
            setBackgroundResource(isChecked() ? R.drawable.miuix_switch_background_on : R.drawable.miuix_switch_background_off);
        }
    }
}
