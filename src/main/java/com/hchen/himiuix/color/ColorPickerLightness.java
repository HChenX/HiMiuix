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

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.callback.OnHueChangedListener;

/**
 * Lightness 色盘
 *
 * @author 焕晨HChen
 */
class ColorPickerLightness extends ColorPickerBaseSeekBar implements OnHueChangedListener {
    public ColorPickerLightness(@NonNull Context context) {
        super(context);
    }

    public ColorPickerLightness(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerLightness(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    void updateBasicInfo() {
        setType(ColorPickerType.LIGHTNESS);
        setColors(
            Color.HSVToColor(new float[]{0, 1, 0}),
            Color.HSVToColor(new float[]{0, 1, 1})
        );
        setMax(10000);
        setProgress(10000);
    }

    @Override
    void setValue(@FloatRange(from = 0, to = 1) float value) {
        value = value * 10000;
        super.setValue(value);
    }

    @Override
    public void onHueValueChanged(int value) {
        setColors(
            Color.HSVToColor(new float[]{value, 1, 0}),
            Color.HSVToColor(new float[]{value, 1, 1})
        );
    }
}
