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
package com.hchen.himiuix.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.R;

/**
 * Radio Button Preference
 *
 * @author 焕晨HChen
 */
public class MiuixRadioButtonPreference extends MiuixStatePreference {
    private OnInnerCheckedListener onInnerCheckedListener;

    public MiuixRadioButtonPreference(@NonNull Context context) {
        super(context);
    }

    public MiuixRadioButtonPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixRadioButtonPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixRadioButtonPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    int loadLayoutResource() {
        return R.layout.miuix_radio_button_preference;
    }

    void setOnInnerCheckedListener(OnInnerCheckedListener listener) {
        this.onInnerCheckedListener = listener;
    }

    @Override
    public boolean onStateChange(boolean newValue) {
        boolean change = super.onStateChange(newValue);
        if (change && newValue && onInnerCheckedListener != null)
            onInnerCheckedListener.onChecked(this);

        return change;
    }

    interface OnInnerCheckedListener {
        void onChecked(MiuixRadioButtonPreference preference);
    }
}
