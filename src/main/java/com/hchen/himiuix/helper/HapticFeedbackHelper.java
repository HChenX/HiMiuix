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

import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;

import com.hchen.himiuix.utils.MiuixUtils;
import com.hchen.himiuix.utils.PropUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Miui HapticFeedback
 *
 * @author 焕晨HChen
 */
public class HapticFeedbackHelper {
    private static final String TAG = "HiMiuix:Haptic";

    // ------------------- Xiaomi ---------------------
    static final int MIUI_HAPTIC_VERSION_1_START = 0x10000000;
    public static final int MIUI_VIRTUAL_RELEASE = 0x10000000;
    public static final int MIUI_TAP_NORMAL = 0x10000001;
    public static final int MIUI_TAP_LIGHT = 0x10000002;
    public static final int MIUI_FLICK = 0x10000003;
    public static final int MIUI_SWITCH = 0x10000004;
    public static final int MIUI_MESH_HEAVY = 0x10000005;
    public static final int MIUI_MESH_NORMAL = 0x10000006;
    public static final int MIUI_MESH_LIGHT = 0x10000007;
    public static final int MIUI_LONG_PRESS = 0x10000008;
    public static final int MIUI_POPUP_NORMAL = 0x10000009;
    public static final int MIUI_POPUP_LIGHT = 0x1000000a;
    public static final int MIUI_PICK_UP = 0x1000000b;
    public static final int MIUI_SCROLL_EDGE = 0x1000000c;
    public static final int MIUI_TRIGGER_DRAWER = 0x1000000d;
    public static final int MIUI_FLICK_LIGHT = 0x1000000e;
    public static final int MIUI_HOLD = 0x1000000f;
    static final int MIUI_HAPTIC_VERSION_1_END = 0x10000010;

    static final int MIUI_HAPTIC_VERSION_2_START = 0x10000010;
    public static final int MIUI_BOUNDARY_SPATIAL = 0x10000010;
    public static final int MIUI_BOUNDARY_TIME = 0x10000011;
    public static final int MIUI_BUTTON_LARGE = 0x10000012;
    public static final int MIUI_BUTTON_MIDDLE = 0x10000013;
    public static final int MIUI_BUTTON_SMALL = 0x10000014;
    public static final int MIUI_GEAR_LIGHT = 0x10000015;
    public static final int MIUI_GEAR_HEAVY = 0x10000016;
    public static final int MIUI_KEYBOARD = 0x10000017;
    public static final int MIUI_ALERT = 0x10000018;
    public static final int MIUI_ZAXIS_SWITCH = 0x10000019;
    static final int MIUI_HAPTIC_VERSION_2_END = 0x1000001a;

    static final int MIUI_HAPTIC_END = 0x1000001a;
    static final int MIUI_HAPTIC_START = 0x10000000;

    public static final int MIUI_KEYBOARD_CLICKY_DOWN_RTP = 193;
    public static final int MIUI_KEYBOARD_CLICKY_UP_RTP = 194;
    public static final int MIUI_KEYBOARD_LINEAR_DOWN_RTP = 195;
    public static final int MIUI_KEYBOARD_LINEAR_UP_RTP = 196;

    private static final String ILLEGAL_FEEDBACK = "IllegalFeedback";

    private static final Map<Integer, String> NAMES = new HashMap<>();

    static {
        NAMES.put(MIUI_VIRTUAL_RELEASE, "MIUI_VIRTUAL_RELEASE");
        NAMES.put(MIUI_TAP_NORMAL, "MIUI_TAP_NORMAL");
        NAMES.put(MIUI_TAP_LIGHT, "MIUI_TAP_LIGHT");
        NAMES.put(MIUI_FLICK, "MIUI_FLICK");
        NAMES.put(MIUI_SWITCH, "MIUI_SWITCH");
        NAMES.put(MIUI_MESH_HEAVY, "MIUI_MESH_HEAVY");
        NAMES.put(MIUI_MESH_NORMAL, "MIUI_MESH_NORMAL");
        NAMES.put(MIUI_MESH_LIGHT, "MIUI_MESH_LIGHT");
        NAMES.put(MIUI_LONG_PRESS, "MIUI_LONG_PRESS");
        NAMES.put(MIUI_POPUP_NORMAL, "MIUI_POPUP_NORMAL");
        NAMES.put(MIUI_POPUP_LIGHT, "MIUI_POPUP_LIGHT");
        NAMES.put(MIUI_PICK_UP, "MIUI_PICK_UP");
        NAMES.put(MIUI_SCROLL_EDGE, "MIUI_SCROLL_EDGE");
        NAMES.put(MIUI_TRIGGER_DRAWER, "MIUI_TRIGGER_DRAWER");
        NAMES.put(MIUI_FLICK_LIGHT, "MIUI_FLICK_LIGHT");
        NAMES.put(MIUI_HOLD, "MIUI_HOLD");
        NAMES.put(MIUI_BOUNDARY_SPATIAL, "MIUI_BOUNDARY_SPATIAL");
        NAMES.put(MIUI_BOUNDARY_TIME, "MIUI_BOUNDARY_TIME");
        NAMES.put(MIUI_BUTTON_LARGE, "MIUI_BUTTON_LARGE");
        NAMES.put(MIUI_BUTTON_MIDDLE, "MIUI_BUTTON_MIDDLE");
        NAMES.put(MIUI_BUTTON_SMALL, "MIUI_BUTTON_SMALL");
        NAMES.put(MIUI_GEAR_LIGHT, "MIUI_GEAR_LIGHT");
        NAMES.put(MIUI_GEAR_HEAVY, "MIUI_GEAR_HEAVY");
        NAMES.put(MIUI_KEYBOARD, "MIUI_KEYBOARD");
        NAMES.put(MIUI_ALERT, "MIUI_ALERT");
        NAMES.put(MIUI_ZAXIS_SWITCH, "MIUI_ZAXIS_SWITCH");
    }

    public static void performHapticFeedback(@NonNull View view, int flag) {
        if (MiuixUtils.isXiaomi()) {
            if (!PropUtils.getProp("sys.haptic.version", "").isEmpty())
                view.performHapticFeedback(flag);
            else view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        } else view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
    }

    public static String nameOf(int i) {
        return NAMES.getOrDefault(i, ILLEGAL_FEEDBACK);
    }

    public static void testAll(@NonNull View view, int delay) {
        NAMES.keySet().forEach(key -> {
            try {
                Log.i(TAG, "HapticFeedback: " + NAMES.get(key));
                view.performHapticFeedback(key);
                Thread.sleep(delay);
            } catch (InterruptedException ignore) {
            }
        });
    }
}
