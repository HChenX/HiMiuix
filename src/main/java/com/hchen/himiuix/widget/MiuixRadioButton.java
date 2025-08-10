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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnStateChangeListener;

/**
 * Miuix Radio Button
 *
 * @author 焕晨HChen
 */
public class MiuixRadioButton extends AppCompatRadioButton {
    private OnStateChangeListener onStateChangeListener;

    public MiuixRadioButton(Context context) {
        this(context, null);
    }

    public MiuixRadioButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixRadioButton, defStyleAttr, 0);
        int buttonId = typedArray.getResourceId(R.styleable.MiuixRadioButton_android_button, 0);
        typedArray.recycle();

        setClickable(true);
        setBackground(null);
        setSaveEnabled(false);
        if (buttonId == 0) setButtonDrawable(R.drawable.miuix_radio_button);
        else setButtonDrawable(buttonId);
    }

    @Override
    public void toggle() {
        if (!isChecked())
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
            super.setChecked(checked);
            return true;
        }
        return false;
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.onStateChangeListener = listener;
    }
}
