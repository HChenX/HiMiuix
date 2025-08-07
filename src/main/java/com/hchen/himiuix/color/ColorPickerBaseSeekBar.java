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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnColorChangedListener;
import com.hchen.himiuix.helper.HapticFeedbackHelper;
import com.hchen.himiuix.widget.MiuixSeekBar;

/**
 * 色盘 Seekbar 基类
 *
 * @author 焕晨HChen
 */
class ColorPickerBaseSeekBar extends MiuixSeekBar implements SeekBar.OnSeekBarChangeListener {
    private ColorPickerType type;
    private OnColorChangedListener listener;
    private LayerDrawable layerDrawable;
    private GradientDrawable drawable;
    private int[] colors;
    private int value;

    public ColorPickerBaseSeekBar(@NonNull Context context) {
        super(context);
    }

    public ColorPickerBaseSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerBaseSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setOrientation(GradientDrawable.Orientation.TL_BR);
        drawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.miuix_color_radius));
        drawable.setSize(-1, getResources().getDimensionPixelSize(R.dimen.miuix_color_height));
        drawable.setStroke(0, 0);

        updateBasicInfo();
        updateProgressBackground();
        setThumb(ContextCompat.getDrawable(getContext(), R.drawable.miuix_color_picker_with_hole));
        setThumbOffset(getResources().getDimensionPixelSize(R.dimen.miuix_color_picker_offset));
        setBackground(null);
        setSplitTrack(false); // 去除按钮的奇怪遮罩
        setPadding(0, 0, 0, 0);
        setOnSeekBarChangeListener(this);
    }

    final void updateProgressBackground() {
        drawable.setColors(colors);
        if (layerDrawable == null) setProgressDrawable(drawable);
        else {
            layerDrawable.setDrawable(1, drawable);
            setProgressDrawable(layerDrawable);
        }
    }

    void updateBasicInfo() {
    }

    final void setListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    final void setType(ColorPickerType type) {
        this.type = type;
    }

    final void setColorPickerBackground(Drawable colorPickerBackground) {
        layerDrawable = new LayerDrawable(new Drawable[]{colorPickerBackground, drawable});
    }

    final void setColors(int... colors) {
        this.colors = colors;
        updateProgressBackground();
    }

    void setValue(float value) {
        this.value = Math.round(value);
        setProgress(Math.round(value));
    }

    final int getValue() {
        return value;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            value = progress;
            if (listener != null) listener.onColorValueChanged(type, value);
            if (isAlwaysHapticFeedback())
                HapticFeedbackHelper.performHapticFeedback(this, HapticFeedbackHelper.MIUI_TAP_NORMAL);
        }
    }

    @Override
    public final void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public final void onStopTrackingTouch(SeekBar seekBar) {
    }
}
