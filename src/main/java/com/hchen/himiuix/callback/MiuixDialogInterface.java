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

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * Dialog 回调接口
 *
 * @author 焕晨HChen
 */
public interface MiuixDialogInterface {
    /**
     * 未知 Button
     * <p>
     * 请勿使用
     */
    int BUTTON_NONE = 0;
    /**
     * 确认类型按钮
     */
    int BUTTON_POSITIVE = -1;
    /**
     * 拒绝类型按钮
     */
    int BUTTON_NEGATIVE = -2;
    /**
     * 自定义类型按钮
     */
    int BUTTON_NEUTRAL = -3;

    /**
     * 取消
     */
    void cancel();

    /**
     * 销毁
     */
    void dismiss();

    /**
     * 按钮点击回调
     */
    interface OnClickListener {
        void onClick(MiuixDialogInterface dialog, int which);
    }

    /**
     * 销毁时调用
     */
    interface OnDismissListener {
        void onDismiss(MiuixDialogInterface dialog);
    }

    /**
     * 显示时调用
     */
    interface OnShowListener {
        void onShow(MiuixDialogInterface dialog);
    }

    /**
     * 取消时调用
     */
    interface OnCancelListener {
        void onCancel(MiuixDialogInterface dialog);
    }

    /**
     * 绑定自定义视图时调用
     */
    interface OnBindViewListener {
        void onBindView(@NonNull ViewGroup root, @NonNull View view);
    }
}
