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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnStateChangeListener;

/**
 * Miuix CheckBox
 *
 * @author 焕晨HChen
 */
public class MiuixCheckBox extends AppCompatCheckBox {
    private static final String TAG = "HiMiuix:CheckBox";
    private OnStateChangeListener onStateChangeListener;

    public MiuixCheckBox(@NonNull Context context) {
        this(context, null);
    }

    public MiuixCheckBox(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixCheckBox(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixCheckBox, defStyleAttr, 0);
        int buttonId = typedArray.getResourceId(R.styleable.MiuixCheckBox_android_button, 0);
        typedArray.recycle();

        setClickable(true);
        setBackground(null);
        setSaveEnabled(false);
        setSaveFromParentEnabled(false);
        if (buttonId == 0) setButtonDrawable(R.drawable.miuix_checkbox);
        else setButtonDrawable(buttonId);
    }

    @Override
    public void toggle() {
        setUserChecked(!isChecked());
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked() == checked) return;
        super.setChecked(checked);
    }

    // 当且仅当 onStateChange 拦截操作时才会返回 false
    public boolean setUserChecked(boolean checked) {
        if (isChecked() == checked) return true;

        if (onStateChangeListener == null || onStateChangeListener.onStateChange(checked)) {
            playScaleAnimation(checked);
            super.setChecked(checked);
            return true;
        }
        return false;
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        onStateChangeListener = listener;
    }

    private void playScaleAnimation(boolean checked) {
        ScaleAnimation anim = new ScaleAnimation(
            1.0f, checked ? 1.05f : 0.95f, checked ? 1.05f : 0.95f, checked ? 1.05f : 0.95f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        anim.setDuration(100);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setInterpolator(new OvershootInterpolator(1.3f));
        startAnimation(anim);
    }
}
