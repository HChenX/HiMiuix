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
package com.hchen.himiuix.color;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.callback.OnHueChangedListener;

import java.util.Arrays;

/**
 * Hue 色盘
 *
 * @author 焕晨HChen
 */
class ColorPickerHue extends ColorPickerBaseSeekBar {
    private OnHueChangedListener[] listeners;

    public ColorPickerHue(@NonNull Context context) {
        super(context);
    }

    public ColorPickerHue(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerHue(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    void updateBasicInfo() {
        setType(ColorPickerType.HUE);
        setColors(
            Color.HSVToColor(new float[]{0, 1, 1}),
            Color.HSVToColor(new float[]{60, 1, 1}),
            Color.HSVToColor(new float[]{120, 1, 1}),
            Color.HSVToColor(new float[]{180, 1, 1}),
            Color.HSVToColor(new float[]{240, 1, 1}),
            Color.HSVToColor(new float[]{300, 1, 1}),
            Color.HSVToColor(new float[]{360, 1, 1})
        );
        setMin(0);
        setMax(36000);
        setProgress(0);
    }

    void setListeners(OnHueChangedListener... listeners) {
        this.listeners = listeners;
    }

    @Override
    void setValue(@FloatRange(from = 0, to = 360) float value) {
        value = value * 100;
        super.setValue(value);
        callChanged();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        if (listeners != null && fromUser) callChanged();
    }

    private void callChanged() {
        Arrays.stream(listeners).forEach(
            listener ->
                listener.onHueValueChanged(getValue() / 100)
        );
    }
}
