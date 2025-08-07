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

import static com.hchen.himiuix.utils.PropUtils.getProp;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Miuix 工具类
 *
 * @author 焕晨HChen
 */
public class MiuixUtils {
    private static final String[] ROM_XIAOMI = {"xiaomi", "redmi"};

    /**
     * 判断当前厂商是否为 Xiaomi
     */
    public static boolean isXiaomi() {
        for (String name : ROM_XIAOMI) {
            if (Build.BRAND.toLowerCase().contains(name.toLowerCase()) ||
                Build.MANUFACTURER.toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取 WindowManager 实例
     */
    public static WindowManager getWindowManager(@NonNull Context context) {
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * 获取当前上下文的 Display 对象
     */
    public static Display getDisplay(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return context.getDisplay();
        } else {
            return getWindowManager(context).getDefaultDisplay();
        }
    }

    /**
     * 获取窗口尺寸
     */
    public static Point getWindowSize(@NonNull Context context) {
        return getWindowSize(getWindowManager(context));
    }

    /**
     * 获取窗口尺寸
     */
    public static Point getWindowSize(@NonNull WindowManager windowManager) {
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
            point.x = bounds.width();
            point.y = bounds.height();
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            point.x = metrics.widthPixels;
            point.y = metrics.heightPixels;
        }
        return point;
    }

    /**
     * 获取屏幕尺寸
     */
    public static Point getScreenSize(@NonNull Context context) {
        return getScreenSize(getWindowManager(context));
    }

    /**
     * 获取屏幕尺寸
     */
    public static Point getScreenSize(@NonNull WindowManager windowManager) {
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect bounds = windowManager.getMaximumWindowMetrics().getBounds();
            point.x = bounds.width();
            point.y = bounds.height();
        } else {
            windowManager.getDefaultDisplay().getSize(point);
        }
        return point;
    }

    /**
     * 判断屏幕是否为横屏
     */
    public static boolean isHorizontalScreen(@NonNull Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 判断屏幕是否为竖屏
     */
    public static boolean isVerticalScreen(@NonNull Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 是否是深色模式
     */
    public static boolean isDarkMode(@NonNull Resources resources) {
        return (resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * 将像素值转换为密度独立像素值
     */
    public static int px2dp(@NonNull Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将像素值转换为缩放独立的字体像素值
     */
    public static int px2sp(@NonNull Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将密度独立像素值转换为像素值
     */
    public static int dp2px(@NonNull Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将缩放独立的字体像素值转换为像素值
     */
    public static int sp2px(@NonNull Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 是否是平板
     */
    public static boolean isPad(@NonNull Context context) {
        int flag = 0;
        if (isXiaomiPad()) return true;
        if (isPadByProp()) ++flag;
        if (isPadBySize(context)) ++flag;
        if (isPadByApi(context)) ++flag;
        return flag >= 2;
    }

    /**
     * 是否是小米平板
     */
    public static boolean isXiaomiPad() {
        try {
            return Boolean.TRUE.equals(InvokeUtils.getStaticField("miui.os.Build", "IS_TABLET"));
        } catch (Throwable ignore) {
            return false;
        }
    }

    private static boolean isPadByProp() {
        String deviceType = getProp("ro.build.characteristics", "default");
        return (deviceType != null && deviceType.toLowerCase().contains("tablet")) || getProp("persist.sys.muiltdisplay_type", 0) == 2;
    }

    private static boolean isPadBySize(@NonNull Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        return Math.sqrt(x + y) >= 7.0;
    }

    private static boolean isPadByApi(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static String getStackTrace() {
        StringBuilder stringBuilder = new StringBuilder();
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        Arrays.stream(stackTraceElements).forEach(new Consumer<StackTraceElement>() {
            @Override
            public void accept(StackTraceElement stackTraceElement) {
                String clazz = stackTraceElement.getClassName();
                String method = stackTraceElement.getMethodName();
                String field = stackTraceElement.getFileName();
                int line = stackTraceElement.getLineNumber();
                stringBuilder.append("\nat ").append(clazz).append(".")
                    .append(method).append("(")
                    .append(field).append(":")
                    .append(line).append(")");
            }
        });
        return stringBuilder.toString();
    }
}
