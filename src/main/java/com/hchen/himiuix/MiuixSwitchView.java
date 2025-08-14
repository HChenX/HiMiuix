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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.callback.OnStateChangeListener;
import com.hchen.himiuix.widget.MiuixSwitch;

/**
 * Miuix Switch 视图
 *
 * @author 焕晨HChen
 */
public class MiuixSwitchView extends MiuixStateView implements OnStateChangeListener {
    private MiuixSwitch xSwitch;

    public MiuixSwitchView(@NonNull Context context) {
        super(context);
    }

    public MiuixSwitchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixSwitchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixSwitchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void loadViewWhenBuild() {
        super.loadViewWhenBuild();
        xSwitch = (MiuixSwitch) getIndicatorView();
        xSwitch.setOnStateChangeListener(this);
    }

    @Override
    DynamicIndicator loadDynamicIndicator() {
        return DynamicIndicator.INDICATOR_SWITCH;
    }

    @Override
    void updateVisibility() {
        super.updateVisibility();
        xSwitch.setVisibility(VISIBLE);
    }

    @Override
    void updateViewContent() {
        // 跳过非用户的 Check 动作
        if (!pass) {
            if (!xSwitch.setUserChecked(isChecked)) {
                isChecked = !isChecked; // 被拦截，还原
            }
        }
        super.updateViewContent();
    }

    @Override
    public boolean performClick() {
        isChecked = !isChecked;
        refreshView();
        return super.performClick();
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked == checked) return;
        isChecked = checked;
        xSwitch.setChecked(checked);
        passRefreshStateView();
    }

    @Override
    public boolean onStateChange(boolean newValue) {
        if (listener == null || listener.onStateChange(newValue)) {
            isChecked = newValue;
            passRefreshStateView();

            return true;
        }
        return false;
    }
}
