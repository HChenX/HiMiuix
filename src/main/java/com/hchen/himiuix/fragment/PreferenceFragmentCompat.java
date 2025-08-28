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
package com.hchen.himiuix.fragment;

import androidx.annotation.CallSuper;

import com.hchen.himiuix.helper.AppBarHelper;

/**
 * 继承此 Fragment 才能使用 MiuixAppBar
 *
 * @author 焕晨HChen
 */
public abstract class PreferenceFragmentCompat extends androidx.preference.PreferenceFragmentCompat {
    @Override
    @CallSuper
    public void onStart() {
        AppBarHelper.callTargetStart(getView());
        super.onStart();
    }

    @Override
    @CallSuper
    public void onResume() {
        AppBarHelper.callTargetRegister(getView());
        super.onResume();
    }

    @CallSuper
    @Override public void onPause() {
        AppBarHelper.callTargetUnregister(getView());
        super.onPause();
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        AppBarHelper.onDestroyView(getView());
        super.onDestroyView();
    }
}
