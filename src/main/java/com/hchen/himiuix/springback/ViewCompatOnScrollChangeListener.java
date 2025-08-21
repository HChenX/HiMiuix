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
package com.hchen.himiuix.springback;

import android.view.View;

/**
 * ViewCompatOnScrollChangeListener
 * <p>
 * 改编自 HyperOS 2
 *
 * @author 焕晨HChen
 */
public interface ViewCompatOnScrollChangeListener {
    void onStateChanged(int oldState, int state, boolean finished);

    void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
}
