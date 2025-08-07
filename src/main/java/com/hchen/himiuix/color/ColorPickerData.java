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
package com.hchen.himiuix.color;

import android.graphics.Color;


/**
 * 颜色数值储存类
 *
 * @author 焕晨HChen
 */
class ColorPickerData {
    int hue = 0;
    int saturation = 0;
    int lightness = 10000;
    int alpha = 255;

    int HSVToColor() {
        return Color.HSVToColor(alpha, new float[]{(float) hue / 100, (float) saturation / 10000, (float) lightness / 10000});
    }
}
