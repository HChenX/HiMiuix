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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.UiContext;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.MiuixDialogInterface;
import com.hchen.himiuix.callback.OnChooseItemListener;

/**
 * Miuix Dialog
 *
 * @author 焕晨HChen
 */
public class MiuixAlertDialog {
    private final MiuixAlertDialogBase base;

    public MiuixAlertDialog(@NonNull @UiContext Context context) {
        this(context, R.style.MiuixDialogWindowStyle);
    }

    MiuixAlertDialog(@NonNull @UiContext Context context, @StyleRes int themeResId) {
        base = new MiuixAlertDialogFactory(context, themeResId).create();
    }

    public Window getWindow() {
        return base.window;
    }

    /**
     * 设置标题
     */
    public MiuixAlertDialog setTitle(String title) {
        base.title = title;
        return this;
    }

    /**
     * 设置消息内容
     */
    public MiuixAlertDialog setMessage(String message) {
        base.message = message;
        return this;
    }

    /**
     * 设置图标
     */
    public MiuixAlertDialog setIcon(@NonNull Drawable icon) {
        base.icon = icon;
        return this;
    }

    // ！！ 请注意 ！！
    // 设置按钮的先后顺序将会影响按钮的实际显示位置！！
    // 理论上不建议同个类型按钮设置多次，但是实际上是可以的，但是按钮数量最大为三个！！

    /**
     * 设置确认类型按钮
     */
    public MiuixAlertDialog setPositiveButton(String text, @Nullable MiuixDialogInterface.OnClickListener listener) {
        base.buttonArray.add(new MiuixAlertDialogBase.ButtonInfo(null, MiuixDialogInterface.BUTTON_POSITIVE, text, listener));
        return this;
    }

    /**
     * 设置拒绝类型按钮
     */
    public MiuixAlertDialog setNegativeButton(String text, @Nullable MiuixDialogInterface.OnClickListener listener) {
        base.buttonArray.add(new MiuixAlertDialogBase.ButtonInfo(null, MiuixDialogInterface.BUTTON_NEGATIVE, text, listener));
        return this;
    }

    /**
     * 设置自定义类型按钮
     */
    public MiuixAlertDialog setNeutralButton(String text, @Nullable MiuixDialogInterface.OnClickListener listener) {
        base.buttonArray.add(new MiuixAlertDialogBase.ButtonInfo(null, MiuixDialogInterface.BUTTON_NEUTRAL, text, listener));
        return this;
    }

    /**
     * 设置自定义布局
     */
    public MiuixAlertDialog setCustomView(@NonNull View view) {
        base.customView = view;
        return this;
    }

    public MiuixAlertDialog setOnBindViewListener(@Nullable MiuixDialogInterface.OnBindViewListener listener) {
        base.onBindViewListener = listener;
        return this;
    }

    public MiuixAlertDialog setListModeEnabled(boolean enabled) {
        base.isListModeEnabled = enabled;
        return this;
    }

    public MiuixAlertDialog setMultipleChoiceEnabled(boolean enabled) {
        base.isMultipleChoiceEnabled = enabled;
        return this;
    }

    public MiuixAlertDialog setItems(CharSequence[] items) {
        base.items = items;
        return this;
    }

    public MiuixAlertDialog setSelectedValues(Integer[] selectedValues) {
        base.selectedValues = selectedValues;
        return this;
    }

    public MiuixAlertDialog setOnChooseItemListener(OnChooseItemListener listener) {
        base.onChooseItemListener = listener;
        return this;
    }

    public MiuixAlertDialog setCardViewModeEnabled(boolean enabled) {
        base.isCardViewModeEnabled = enabled;
        return this;
    }

    /**
     * 设置启用按钮点击震动效果
     */
    public MiuixAlertDialog setHapticFeedbackEnabled(boolean enabled) {
        base.isEnableHapticFeedback = enabled;
        return this;
    }

    /**
     * 设置 Dialog show 回调
     */
    public MiuixAlertDialog setOnShowListener(MiuixDialogInterface.OnShowListener onShowListener) {
        base.onShowListener = onShowListener;
        return this;
    }

    /**
     * 设置 Dialog cancel 回调
     */
    public MiuixAlertDialog setOnCancelListener(MiuixDialogInterface.OnCancelListener onCancelListener) {
        base.onCancelListener = onCancelListener;
        return this;
    }

    /**
     * 设置 Dialog dismiss 回调
     */
    public MiuixAlertDialog setOnDismissListener(MiuixDialogInterface.OnDismissListener onDismissListener) {
        base.onDismissListener = onDismissListener;
        return this;
    }

    /**
     * 设置可取消
     * <p>
     * 默认为 true
     */
    public MiuixAlertDialog setCancelable(boolean cancelable) {
        base.isCancelable = cancelable;
        return this;
    }

    /**
     * 设置触摸 Dialog 外围是否自动销毁
     * <p>
     * 默认为 true
     */
    public MiuixAlertDialog setCanceledOnTouchOutside(boolean cancel) {
        base.isCanceledOnTouchOutside = cancel;
        return this;
    }

    /**
     * 设置是否点击按钮后自动销毁
     * <p>
     * 默认为 true
     */
    public MiuixAlertDialog setAutoDismiss(boolean autoDismiss) {
        base.isAutoDismiss = autoDismiss;
        return this;
    }

    public boolean isShowing() {
        return base.isShowing();
    }

    public void create() {
        base.create();
    }

    public void show() {
        base.show();
    }

    public void dismiss() {
        base.dismiss();
    }
}
