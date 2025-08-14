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
package com.hchen.himiuix.helper;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.WindowInsetsCompat;

import com.hchen.himiuix.callback.OnImeVisibilityChangedListener;

import java.util.HashSet;

/**
 * Ime 监听
 *
 * @author 焕晨HChen
 */
public class ImeHelper implements OnApplyWindowInsetsListener {
    private static final ImeHelper imeHelper = new ImeHelper();
    private static boolean lastShown;
    private static final HashSet<OnImeVisibilityChangedListener> listeners = new HashSet<>();

    public static ImeHelper init() {
        return imeHelper;
    }

    public static void addListeners(OnImeVisibilityChangedListener listener) {
        listeners.add(listener);
    }

    public static void removeListeners(OnImeVisibilityChangedListener listener) {
        listeners.remove(listener);
    }

    private static void checkAndRemoveListenersIfNeed() {
        listeners.removeIf(listener -> {
            if (listener instanceof View v) {
                return !v.isAttachedToWindow();
            }
            return false;
        });
    }

    @NonNull @Override
    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
        checkAndRemoveListenersIfNeed();
        if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
            listeners.forEach(listener -> listener.visibilityChanged(true));
            lastShown = true;
        } else if (lastShown) {
            listeners.forEach(listener -> listener.visibilityChanged(false));
            lastShown = false;
        }
        return insets;
    }
}
