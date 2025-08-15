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
import android.view.ViewGroup;

import com.hchen.himiuix.callback.OnToolbarListener;
import com.hchen.himiuix.springback.SpringBackLayout;

import java.util.HashSet;

/**
 * AppBar Helper
 *
 * @author 焕晨HChen
 */
public class AppBarHelper {
    private static final String TAG = "HiMiuix:AppBar";
    private static final HashSet<OnToolbarListener> mToolbarListeners = new HashSet<>();

    public static void addOnToolbarListener(OnToolbarListener listener) {
        mToolbarListeners.add(listener);
    }

    public static void removeOnToolbarListener(OnToolbarListener listener) {
        if (listener == null) mToolbarListeners.clear();
        mToolbarListeners.remove(listener);
    }

    public static void callTargetRegister(View view) {
        view.post(() -> {
            View target = findTargetView((ViewGroup) view);
            if (target == null) return;
            for (OnToolbarListener listener : mToolbarListeners) {
                listener.targetRegister(target);
            }
        });
    }

    private static View findTargetView(ViewGroup group) {
        if (group instanceof SpringBackLayout springBackLayout) {
            if (springBackLayout.isLinkageToolbar())
                return springBackLayout.getTarget();
        }
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);

            if (view instanceof SpringBackLayout springBackLayout) {
                if (springBackLayout.isLinkageToolbar()) {
                    return springBackLayout.getTarget();
                }
            }
            if (view instanceof ViewGroup viewGroup) {
                View target = findTargetView(viewGroup);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }
}
