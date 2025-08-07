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
import androidx.preference.Preference;

import com.hchen.himiuix.callback.OnCheckedChangeListener;

import java.util.HashSet;

/**
 * Radio Group Preference
 *
 * @author 焕晨HChen
 */
public class MiuixRadioGroupPreference extends MiuixPreferenceCategory implements MiuixRadioButtonPreference.OnInnerCheckedListener {
    private final HashSet<MiuixRadioButtonPreference> hashSet = new HashSet<>();
    private OnCheckedChangeListener onCheckedChangeListener;

    public MiuixRadioGroupPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixRadioGroupPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixRadioGroupPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean addPreference(@NonNull Preference preference) {
        if (preference instanceof MiuixRadioButtonPreference xRadioButtonPreference) {
            xRadioButtonPreference.setOnInnerCheckedListener(this);
            hashSet.add(xRadioButtonPreference);
        }
        return super.addPreference(preference);
    }

    @Override
    public boolean removePreference(@NonNull Preference preference) {
        if (preference instanceof MiuixRadioButtonPreference xRadioButtonPreference)
            hashSet.remove(xRadioButtonPreference);
        return super.removePreference(preference);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }

    private void updateCheckedState(MiuixRadioButtonPreference preference) {
        if (onCheckedChangeListener != null)
            onCheckedChangeListener.onCheckedChanged(preference);

        hashSet.forEach(x -> {
            if (!x.equals(preference))
                x.setChecked(false);
        });
    }

    @Override
    public void onChecked(MiuixRadioButtonPreference preference) {
        updateCheckedState(preference);
    }
}
