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
package com.hchen.himiuix.dialog;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.annotation.UiContext;

import com.hchen.himiuix.utils.MiuixUtils;

/**
 * Miuix Dialog Factory
 *
 * @author 焕晨HChen
 */
class MiuixAlertDialogFactory {
    @UiContext
    @NonNull
    private final Context context;
    private final int themeResId;

    MiuixAlertDialogFactory(@UiContext @NonNull Context context, @StyleRes int themeResId) {
        this.context = context;
        this.themeResId = themeResId;
    }

    public MiuixAlertDialogBase create() {
        Dialog dialog = new Dialog(context, themeResId);
        return MiuixUtils.isVerticalScreen(context) ? new MiuixAlertVerticalDialog(dialog) :
            MiuixUtils.isPad(context) ? new MiuixAlertVerticalDialog(dialog) : new MiuixAlertHorizontalDialog(dialog);
    }
}
