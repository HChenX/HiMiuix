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

import java.util.Objects;

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
        colorSelectView = (ColorSelectView) getIndicatorView();
        colorPickerView = new ColorPickerView(getContext());
        colorPickerView.setColorValue(color); // 初始化 color
        colorPickerView.setOnColorChangedListener(this);

        // 设置是否启用 Dialog 模式
        if (isDialogModeEnabled) colorPickerView.setDialogModeEnabled(true);
        else setCustomView(colorPickerView);
    }

    @Override
    DynamicIndicator loadDynamicIndicator() {
        return DynamicIndicator.INDICATOR_COLOR;
    }

    @Override
    void updateVisibility() {
        super.updateVisibility();
        // 必要时隐藏颜色指示器，视觉平衡
        if (getTitle() != null || getSummary() != null || getIcon() != null || isShowValueOnTip)
            colorSelectView.setVisibility(VISIBLE);
        else colorSelectView.setVisibility(GONE);
        if (isShowValueOnTip) getTipView().setVisibility(VISIBLE);
    }

    @Override
    void updateViewContent() {
        super.updateViewContent();
        colorSelectView.setColor(color);
        colorPickerView.setAlwaysHapticFeedback(isAlwaysHapticFeedback);
        if (isShowValueOnTip)
            getTipView().setText(getContext().getString(R.string.color_display, colorPickerView.formatColor(color)));
    }

    @Override
    public boolean performClick() {
        if (isDialogModeEnabled) {
            if (!isShowing) {
                colorPickerView.setColorValue(color);

                new MiuixAlertDialog(getContext())
                    .setTitle(getTitle())
                    .setMessage(getSummary())
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setHapticFeedbackEnabled(true)
                    .setCustomView(colorPickerView)
                    .setNegativeButton(getContext().getText(R.string.dialog_negative), null)
                    .setPositiveButton(getContext().getText(R.string.dialog_positive),
                        (dialog, which) -> {
                            setColor(colorPickerView.getColorValue());
                            if (listener != null)
                                listener.onColorValueChanged(ColorPickerType.FINAL_COLOR, color);
                        }
                    )
                    .setOnShowListener(dialog -> isShowing = true)
                    .setOnDismissListener(dialog -> {
                        isShowing = false;
                        // 解除保持阴影状态
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
            // 保持阴影状态
            getShadowHelper().setKeepShadow();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setColor(@ColorInt int color) {
        if (this.color == color) return;
        this.color = color;
        refreshView();
    }

    public void setShowValueOnTip(boolean show) {
        if (isShowValueOnTip == show) return;
        isShowValueOnTip = show;
        refreshView();
    }

    public void setAlwaysHapticFeedback(boolean enabled) {
        if (isAlwaysHapticFeedback == enabled) return;
        isAlwaysHapticFeedback = enabled;
        refreshView();
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        if (Objects.equals(this.listener, listener)) return;
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
        // 仅在非 Dialog 模式持续刷新状态，保证性能
        if ((!isDialogModeEnabled && type == ColorPickerType.COLOR_VALUE) || type == ColorPickerType.FINAL_COLOR) {
            setColor(value);
            if (listener != null)
                listener.onColorValueChanged(type, value);
        }
    }
}
