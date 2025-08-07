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
package com.hchen.himiuix.callback;

import com.hchen.himiuix.color.ColorPickerType;

/**
 * 颜色值更改时调用
 *
 * @author 焕晨HChen
 */
public interface OnColorChangedListener {
    /**
     * 仅建议在 type 返回 {@link ColorPickerType#COLOR_VALUE} 时使用 value 值
     * <p>
     * 其他 type 值 value 值为内部使用
     */
    void onColorValueChanged(ColorPickerType type, int value);
}
