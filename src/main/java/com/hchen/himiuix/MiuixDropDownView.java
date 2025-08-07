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
package com.hchen.himiuix;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hchen.himiuix.callback.OnChooseItemListener;
import com.hchen.himiuix.dialog.MiuixDropDownDialog;
import com.hchen.himiuix.helper.HapticFeedbackHelper;

/**
 * Miuix 弹窗选择视图
 *
 * @author 焕晨HChen
 */
public class MiuixDropDownView extends MiuixBasicView {
    private CharSequence[] entries;
    private CharSequence entry;
    private String value;
    private boolean isShowOnTip;
    private OnChooseItemListener listener;
    private boolean isShowing;

    public MiuixDropDownView(@NonNull Context context) {
        super(context);
    }

    public MiuixDropDownView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixDropDownView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixDropDownView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixDropDownView, defStyleAttr, defStyleRes);
        entries = typedArray.getTextArray(R.styleable.MiuixDropDownView_android_entries);
        entry = typedArray.getString(R.styleable.MiuixDropDownView_entry);
        value = typedArray.getString(R.styleable.MiuixDropDownView_android_value);
        isShowOnTip = typedArray.getBoolean(R.styleable.MiuixDropDownView_showOnTip, true);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void loadShadowHelper() {
        super.loadShadowHelper();
        getShadowHelper().setHapticFeedbackFlag(HapticFeedbackHelper.MIUI_POPUP_NORMAL);
    }

    @Override
    void loadViewWhenBuild() {
        super.loadViewWhenBuild();
        setIndicator(ContextCompat.getDrawable(getContext(), R.drawable.miuix_up_down));
    }

    @Override
    void updateViewContent() {
        super.updateViewContent();

        if (isShowOnTip) {
            if (entry != null) getTipView().setText(entry);
            else if (value != null) getTipView().setText(entries[Integer.parseInt(value)]);
        }
    }

    @Override
    void updateVisibility() {
        super.updateVisibility();
        if (isShowOnTip) getTipView().setVisibility(VISIBLE);
    }

    @Override
    boolean forceShowCustomIndicatorView() {
        return true;
    }

    /**
     * 设置可选条目列表
     */
    public void setEntries(CharSequence[] entries) {
        this.entries = entries;
        refreshView();
    }

    /**
     * 设置当前选中的条目索引值
     */
    public void setValue(String value) {
        this.value = value;
        refreshView();
    }

    /**
     * 设置当前选中的条目值
     */
    public void setEntry(CharSequence entry) {
        this.entry = entry;
        refreshView();
    }

    /**
     * 设置监听选中条目更改事件
     */
    public void setOnChooseItemListener(OnChooseItemListener listener) {
        this.listener = listener;
        refreshView();
    }

    /**
     * 选中的条目是否在 Tip 上展示
     */
    public void setShowOnTip(boolean showOnTip) {
        isShowOnTip = showOnTip;
        refreshView();
    }

    public CharSequence[] getEntries() {
        return entries;
    }

    public String getValue() {
        return value;
    }

    public CharSequence getEntry() {
        return entry;
    }

    public boolean isShowOnTip() {
        return isShowOnTip;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled()) return super.dispatchTouchEvent(ev);
        if (isShowing) return super.dispatchTouchEvent(ev);

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            getShadowHelper().setKeepShadow();

            new MiuixDropDownDialog(getContext())
                .setXY(ev.getX(), ev.getY())
                .setTargetView(this)
                .setEntries(entries)
                .setValue(value)
                .setEntry(entry)
                .setOnChooseItemListener(new OnChooseItemListener() {
                    @Override
                    public boolean onChooseBefore(CharSequence item, int which) {
                        if (listener == null || listener.onChooseBefore(item, which)) {
                            entry = item;
                            value = String.valueOf(which);
                            if (isShowOnTip) setTip((String) entry);
                            HapticFeedbackHelper.performHapticFeedback(
                                MiuixDropDownView.this,
                                HapticFeedbackHelper.MIUI_POPUP_NORMAL
                            );
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onChooseAfter(CharSequence[] items, CharSequence[] selectedItems, Integer[] selectedValues) {
                        if (listener != null)
                            listener.onChooseAfter(items, selectedItems, selectedValues);
                    }
                })
                .setOnShowListener(dialog -> isShowing = true)
                .setOnDismissListener(dialog -> {
                    isShowing = false;
                    getShadowHelper().restoreOriginalColor();
                })
                .show();
        }
        return super.dispatchTouchEvent(ev);
    }
}
