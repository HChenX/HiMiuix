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

import static com.hchen.himiuix.utils.InvokeUtils.callStaticMethod;

import androidx.annotation.NonNull;

import java.util.Optional;

/**
 * Prop Utils
 *
 * @author 焕晨HChen
 */
public class PropUtils {
    private static final Class<?> propClass = InvokeUtils.findClass("android.os.SystemProperties");

    private PropUtils() {
    }

    /**
     * 获取 boolean 类型的 prop
     */
    public static boolean getProp(@NonNull String key, boolean def) {
        return Boolean.TRUE.equals(
            callStaticMethod(propClass, "getBoolean", new Class[]{String.class, boolean.class}, key, def)
        );
    }

    /**
     * 获取 int 类型的 prop
     */
    public static int getProp(@NonNull String key, int def) {
        return (int) Optional.ofNullable(
            callStaticMethod(propClass, "getInt", new Class[]{String.class, int.class}, key, def)
        ).orElse(def);
    }

    /**
     * 获取 long 类型的 prop
     */
    public static long getProp(@NonNull String key, long def) {
        return (long) Optional.ofNullable(
            callStaticMethod(propClass, "getLong", new Class[]{String.class, long.class}, key, def)
        ).orElse(def);
    }

    /**
     * 获取 String 类型的 prop
     */
    public static String getProp(@NonNull String key, String def) {
        return (String) Optional.ofNullable(
            callStaticMethod(propClass, "get", new Class[]{String.class, String.class}, key, def)
        ).orElse(def);
    }

    /**
     * 获取 String 类型的 prop，无默认值
     */
    public static String getProp(@NonNull String key) {
        return callStaticMethod(propClass, "get", new Class[]{String.class}, key);
    }
}
