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
package com.hchen.himiuix.utils;

import android.graphics.Outline;
import android.graphics.Point;
import android.view.View;
import android.view.ViewOutlineProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @noinspection JavaReflectionMemberAccess
 */
public class MiuiSuperBlur {
    private static final Class<?> viewClass = View.class;
    private static Method chooseBackgroundBlurContainer;
    private static Method setMiViewBlurMode;
    private static Method setMiBackgroundBlurMode;
    private static Method setPassWindowBlurEnabled;
    private static Method setMiBackgroundBlurRadius;
    private static Method addMiBackgroundBlendColor;
    private static Method setMiBackgroundBlurScaleRatio;
    private static Method clearMiBackgroundBlendColor;
    private static Method disableMiBackgroundContainBelow;
    private static Method setMiBackgroundBlendColors;
    private static Method setMiBackgroundBlurEnhanceFlag;

    static {
        run(() -> {
            chooseBackgroundBlurContainer = viewClass.getDeclaredMethod("chooseBackgroundBlurContainer", View.class);
            chooseBackgroundBlurContainer.setAccessible(true);
        });

        run(() -> {
            setMiViewBlurMode = viewClass.getDeclaredMethod("setMiViewBlurMode", int.class);
            setMiViewBlurMode.setAccessible(true);
        });

        run(() -> {
            setMiBackgroundBlurMode = viewClass.getDeclaredMethod("setMiBackgroundBlurMode", int.class);
            setMiBackgroundBlurMode.setAccessible(true);
        });

        run(() -> {
            setPassWindowBlurEnabled = viewClass.getDeclaredMethod("setPassWindowBlurEnabled", boolean.class);
            setPassWindowBlurEnabled.setAccessible(true);
        });

        run(() -> {
            setMiBackgroundBlurRadius = viewClass.getDeclaredMethod("setMiBackgroundBlurRadius", int.class);
            setMiBackgroundBlurRadius.setAccessible(true);
        });

        run(() -> {
            addMiBackgroundBlendColor = viewClass.getDeclaredMethod("addMiBackgroundBlendColor", int.class, int.class);
            addMiBackgroundBlendColor.setAccessible(true);
        });

        run(() -> {
            setMiBackgroundBlurScaleRatio = viewClass.getDeclaredMethod("setMiBackgroundBlurScaleRatio", float.class);
            setMiBackgroundBlurScaleRatio.setAccessible(true);
        });

        run(() -> {
            clearMiBackgroundBlendColor = viewClass.getDeclaredMethod("clearMiBackgroundBlendColor");
            clearMiBackgroundBlendColor.setAccessible(true);
        });

        run(() -> {
            disableMiBackgroundContainBelow = viewClass.getDeclaredMethod("disableMiBackgroundContainBelow", boolean.class);
            disableMiBackgroundContainBelow.setAccessible(true);
        });

        run(() -> {
            setMiBackgroundBlendColors = viewClass.getDeclaredMethod("setMiBackgroundBlendColors", ArrayList.class);
            setMiBackgroundBlendColors.setAccessible(true);
        });

        run(() -> {
            setMiBackgroundBlurEnhanceFlag = viewClass.getDeclaredMethod("setMiBackgroundBlurEnhanceFlag", int.class, int.class);
            setMiBackgroundBlurEnhanceFlag.setAccessible(true);
        });
    }

    public static void chooseBackgroundBlurContainer(View view, View container) {
        run(() -> chooseBackgroundBlurContainer.invoke(view, container));
    }

    public static void setMiViewBlurMode(View view, int mode) {
        run(() -> setMiViewBlurMode.invoke(view, mode));
    }

    public static void setMiViewBlurMode(View view, int mode, int radius) {
        setMiViewBlurMode(view, mode);
        view.setClipToOutline(true);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setRoundRect(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight(), radius);
            }
        });
    }

    /*
     * 101 子 view 模糊
     * 103 当前 view 模糊
     * 105 当前 view 和子 view 都模糊
     * */
    public static void setMiBackgroundBlurMode(View view, int mode) {
        run(() -> setMiBackgroundBlurMode.invoke(view, mode));
    }

    public static void setPassWindowBlurEnabled(View view, boolean enabled) {
        run(() -> setPassWindowBlurEnabled.invoke(view, enabled));
    }

    public static void setMiBackgroundBlurRadius(View view, int radius) {
        run(() -> setMiBackgroundBlurRadius.invoke(view, radius));
    }

    public static void addMiBackgroundBlendColor(View view, int color, int mode) {
        run(() -> addMiBackgroundBlendColor.invoke(view, color, mode));
    }

    public static void setMiBackgroundBlurScaleRatio(View view, float ratio) {
        run(() -> setMiBackgroundBlurScaleRatio.invoke(view, ratio));
    }

    public static void clearMiBackgroundBlendColor(View view) {
        run(() -> clearMiBackgroundBlendColor.invoke(view));
    }

    public static void setDisableMiBackgroundContainBelow(View view, boolean enabled) {
        run(() -> disableMiBackgroundContainBelow.invoke(view, enabled));
    }

    public static void setMiBackgroundBlendColors(View view, Point... colors) {
        setMiBackgroundBlendColors(view, new ArrayList<>(Arrays.asList(colors)));
    }

    public static void setMiBackgroundBlendColors(View view, ArrayList<Point> colors) {
        run(() -> setMiBackgroundBlendColors.invoke(view, colors));
    }

    public static void setMiBackgroundBlendColors(View view, int[] colors, int ratio) {
        clearMiBackgroundBlendColor(view);

        for (int i = 0; i < colors.length / 2; i++) {
            int j = i * 2;
            int color = colors[j];
            int mode = colors[j + 1];

            if (ratio != 1.0f) {
                int alpha = (color >> 24) & 0xFF;
                color = (color & (~((color >> 24 & 0xFF) << 24))) | ((int) (alpha * ratio) << 24);
            }

            addMiBackgroundBlendColor(view, color, mode);
        }
    }

    public static void setMiBackgroundBlurEnhanceFlag(View view, int flag, int mask) {
        run(() -> setMiBackgroundBlurEnhanceFlag.invoke(view, flag, mask));
    }

    public static void clearAllBlur(View view) {
        clearMiBackgroundBlendColor(view);
        setMiBackgroundBlurMode(view, 0);
        setMiViewBlurMode(view, 0);
        setMiBackgroundBlurRadius(view, 0);
        setPassWindowBlurEnabled(view, false);
    }

    public static int[] getBlendColor(int color, int[] colors) {
        int length = colors.length;
        int[] newColors = new int[length];
        System.arraycopy(colors, 0, newColors, 0, length);
        if (color != 0) newColors[1] = (16777215 & color) | ((-16777216) & colors[1]);
        return newColors;
    }

    private static void run(RunnableTry runnable) {
        try {
            runnable.run();
        } catch (Throwable ignore) {
        }
    }

    private interface RunnableTry {
        void run() throws Throwable;
    }
}
