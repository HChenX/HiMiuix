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
import android.util.AttributeSet;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.callback.OnStateChangeListener;
import com.hchen.himiuix.widget.MiuixRadioButton;

/**
 * Miuix 单选框
 *
 * @author 焕晨HChen
 */
public class MiuixRadioButtonView extends MiuixStateView implements OnStateChangeListener {
    private MiuixRadioButton xRadioButton;
    private OnInnerCheckedListener onInnerCheckedListener;

    public MiuixRadioButtonView(@NonNull Context context) {
        super(context);
    }

    public MiuixRadioButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixRadioButtonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixRadioButtonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void loadViewWhenBuild() {
        super.loadViewWhenBuild();
        xRadioButton = findViewById(R.id.miuix_radio_button_indicator);
        xRadioButton.setOnStateChangeListener(this);
    }

    @Override
    void updateVisibility() {
        super.updateVisibility();
        xRadioButton.setVisibility(VISIBLE);
    }

    @Override
    void updateViewContent() {
        if (!pass) {
            if (!xRadioButton.setUserChecked(isChecked))
                isChecked = !isChecked;
        }
        setShadowHelperEnabled(!isChecked);
        super.updateViewContent();
    }

    @Override
    boolean canShowCustomIndicatorView() {
        return false;
    }

    @Override
    public boolean performClick() {
        if (!isChecked) {
            isChecked = true;
            refreshView();
        }
        return super.performClick();
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        isChecked = checked;
        xRadioButton.setChecked(checked);
        if (isChecked && onInnerCheckedListener != null)
            onInnerCheckedListener.onChecked(getId());
        passRefreshStateView();
    }

    MiuixRadioButton getRadioButton() {
        return xRadioButton;
    }

    @Override
    public boolean onStateChange(boolean newValue) {
        if (listener == null || listener.onStateChange(newValue)) {
            isChecked = newValue;
            passRefreshStateView();

            if (isChecked && onInnerCheckedListener != null)
                onInnerCheckedListener.onChecked(getId());
            return true;
        }
        return false;
    }

    void setInnerCheckedListener(OnInnerCheckedListener listener) {
        onInnerCheckedListener = listener;
    }

    interface OnInnerCheckedListener {
        void onChecked(@IdRes int id);
    }
}
