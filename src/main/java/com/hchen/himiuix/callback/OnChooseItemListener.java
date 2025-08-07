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

/**
 * List 条目选中回调
 *
 * @author 焕晨HChen
 */
public interface OnChooseItemListener {
    /**
     * 选中条目前回调
     * <p>
     * 可以在此处阻止选中本条目
     */
    default boolean onChooseBefore(CharSequence item, int which) {
        return true;
    }

    /**
     * 选中条目后回调
     * <p>
     * 在此处可用获取当前已选中的所有条目
     */
    default void onChooseAfter(CharSequence[] items, CharSequence[] selectedItems, Integer[] selectedValues) {
    }
}
