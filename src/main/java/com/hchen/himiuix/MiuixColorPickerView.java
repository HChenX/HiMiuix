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
package com.hchen.himiuix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.callback.OnColorChangedListener;
import com.hchen.himiuix.color.ColorPickerType;
import com.hchen.himiuix.color.ColorPickerView;
import com.hchen.himiuix.color.ColorSelectView;
import com.hchen.himiuix.dialog.MiuixAlertDialog;

/**
 * Miuix 调色盘视图
 *
 * @author 焕晨HChen
 */
public class MiuixColorPickerView extends MiuixBasicView implements OnColorChangedListener {
    private ColorSelectView colorSelectView;
    private ColorPickerView colorPickerView;
    private OnColorChangedListener listener;
    private boolean isAlwaysHapticFeedback;
    private boolean isDialogModeEnabled;
    private boolean isShowValueOnTip;
    private boolean isShowing;
    private int color;

    public MiuixColorPickerView(@NonNull Context context) {
        super(context);
    }

    public MiuixColorPickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixColorPickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixColorPickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixColorPickerView, defStyleAttr, defStyleRes);
        color = typedArray.getColor(R.styleable.MiuixColorPickerView_android_color, -1);
        isDialogModeEnabled = typedArray.getBoolean(R.styleable.MiuixColorPickerView_enableDialogMode, true);
        isShowValueOnTip = typedArray.getBoolean(R.styleable.MiuixColorPickerView_showValueOnTip, true);
        isAlwaysHapticFeedback = typedArray.getBoolean(R.styleable.MiuixColorPickerView_alwaysHapticFeedback, false);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void loadViewWhenBuild() {
        super.loadViewWhenBuild();
        colorSelectView = findViewById(R.id.miuix_color_indicator);
        colorPickerView = new ColorPickerView(getContext());
        colorPickerView.setColorValue(color);
        colorPickerView.setOnColorChangedListener(this);
        if (isDialogModeEnabled) colorPickerView.setDialogMode(true);
        else setCustomView(colorPickerView);
    }

    @Override
    void updateVisibility() {
        super.updateVisibility();
        colorSelectView.setVisibility(VISIBLE);
        if (isShowValueOnTip) getTipView().setVisibility(VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    void updateViewContent() {
        super.updateViewContent();
        colorSelectView.setColor(color);
        if (isShowValueOnTip) getTipView().setText("#" + colorPickerView.formatColor(color));
        colorPickerView.setAlwaysHapticFeedback(isAlwaysHapticFeedback);
    }

    @Override
    boolean canShowCustomIndicatorView() {
        return false;
    }

    @Override
    public boolean performClick() {
        if (isDialogModeEnabled) {
            if (!isShowing) {
                new MiuixAlertDialog(getContext())
                    .setTitle(getTitle())
                    .setMessage(getSummary())
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setHapticFeedbackEnabled(true)
                    .setCustomView(colorPickerView)
                    .setNegativeButton(getContext().getString(R.string.dialog_negative), null)
                    .setPositiveButton(getContext().getString(R.string.dialog_positive),
                        (dialog, which) -> {
                            setColor(colorPickerView.getColorValue());
                            if (listener != null)
                                listener.onColorValueChanged(ColorPickerType.FINAL_COLOR, color);
                        }
                    )
                    .setOnShowListener(dialog -> isShowing = true)
                    .setOnDismissListener(dialog -> {
                        isShowing = false;
                        getShadowHelper().restoreOriginalColor();
                    })
                    .show();
            }
        }
        return super.performClick();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isEnabled() && isDialogModeEnabled && ev.getAction() == MotionEvent.ACTION_UP) {
            getShadowHelper().setKeepShadow();
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 设置颜色值
     */
    public void setColor(@ColorInt int color) {
        this.color = color;
        refreshView();
    }

    public void setShowValueOnTip(boolean show) {
        this.isShowValueOnTip = show;
        refreshView();
    }

    public void setAlwaysHapticFeedback(boolean enabled) {
        this.isAlwaysHapticFeedback = enabled;
        refreshView();
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.listener = listener;
        refreshView();
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    public boolean isDialogModeEnabled() {
        return isDialogModeEnabled;
    }

    public boolean isAlwaysHapticFeedback() {
        return isAlwaysHapticFeedback;
    }

    public boolean isShowValueOnTip() {
        return isShowValueOnTip;
    }

    @Override
    public void onColorValueChanged(ColorPickerType type, int value) {
        if ((!isDialogModeEnabled && type == ColorPickerType.COLOR_VALUE) || type == ColorPickerType.FINAL_COLOR) {
            setColor(value);
            if (listener != null)
                listener.onColorValueChanged(type, value);
        }
    }
}
