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
import android.graphics.Point;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.annotation.UiContext;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.MiuixDialogInterface;
import com.hchen.himiuix.callback.OnChooseItemListener;
import com.hchen.himiuix.list.MiuixListAdapter;
import com.hchen.himiuix.utils.MiuixUtils;
import com.hchen.himiuix.widget.MiuixCardView;

import java.util.Arrays;
import java.util.Objects;

/**
 * Miuix DropDown Dialog
 *
 * @author 焕晨HChen
 */
public class MiuixDropDownDialog {
    private static final String TAG = "HiMiuix";
    @UiContext
    @NonNull
    private final Context context;
    @NonNull
    private final Dialog dialog;
    @NonNull
    private final Window window;
    @NonNull
    private final Point screenPoint;
    @NonNull
    private final Point windowPoint;
    private View targetView;
    private boolean isCreated;
    private final boolean isVerticalScreen;

    private String value;
    private String[] values;
    private CharSequence entry;
    private CharSequence[] entries;
    private OnChooseItemListener onChooseItemListener;
    private MiuixDialogInterface.OnShowListener onShowListener;
    private MiuixDialogInterface.OnDismissListener onDismissListener;

    private float x;
    private float y;

    public MiuixDropDownDialog(@UiContext @NonNull Context context) {
        this(context, R.style.MiuixDialogWindowStyle);
    }

    public MiuixDropDownDialog(@UiContext @NonNull Context context, @StyleRes int themeResId) {
        this.context = context;
        dialog = new Dialog(context, themeResId);
        window = Objects.requireNonNull(dialog.getWindow());
        screenPoint = MiuixUtils.getScreenSize(context);
        windowPoint = MiuixUtils.getWindowSize(context);
        isVerticalScreen = MiuixUtils.isVerticalScreen(context);
    }

    /**
     * 设置备选条目
     */
    public MiuixDropDownDialog setEntries(CharSequence[] entries) {
        this.entries = entries;
        return this;
    }

    /**
     * 设置选中的条目索引值
     * <p>
     * 注意！是索引值，从 0 开始
     */
    public MiuixDropDownDialog setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * 设置选中的条目
     */
    public MiuixDropDownDialog setEntry(CharSequence entry) {
        this.entry = entry;
        return this;
    }

    /**
     * 设置当前触摸点位的 X，Y 值
     * <p>
     * 用于确定 DropDown 显示位置
     */
    public MiuixDropDownDialog setXY(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * 设置当前目标 View
     * <p>
     * 用于计算高度等信息
     */
    public MiuixDropDownDialog setTargetView(View targetView) {
        this.targetView = targetView;
        return this;
    }

    /**
     * 选中条目回调
     */
    public MiuixDropDownDialog setOnChooseItemListener(OnChooseItemListener listener) {
        onChooseItemListener = listener;
        return this;
    }

    /**
     * Show listener
     */
    public MiuixDropDownDialog setOnShowListener(MiuixDialogInterface.OnShowListener listener) {
        onShowListener = listener;
        return this;
    }

    /**
     * Dismiss listener
     */
    public MiuixDropDownDialog setOnDismissListener(MiuixDialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
        return this;
    }

    /**
     * 获取当前选中条目的索引值
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取当前选中条目
     */
    public CharSequence getEntry() {
        return entry;
    }

    private void valueCheck() {
        if (values == null || value == null) return;
        if (Arrays.asList(values).contains(value)) return;

        throw new RuntimeException("MiuixDropDownDialog: The input value is not an existing set of available values!" +
            "Input: " + value + " Available values: " + Arrays.toString(values));
    }

    private void entryCheck() {
        if (entries == null || entry == null) return;
        if (Arrays.asList(entries).contains(entry)) return;

        throw new RuntimeException("MiuixDropDownDialog: The input entry is not an existing set of available entries!" +
            "Input: " + entry + " Available entries: " + Arrays.toString(entries));
    }

    public void show() {
        if (isCreated)
            return;

        create();
        dialog.create();
        dialog.show();
        isCreated = true;
    }

    public void dismiss() {
        dialog.dismiss();
        isCreated = false;
    }

    /**
     * @noinspection ExtractMethodRecommender
     */
    private void create() {
        values = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            values[i] = String.valueOf(i);
        }

        valueCheck();
        entryCheck();

        if (onShowListener != null)
            dialog.setOnShowListener(dialog -> onShowListener.onShow(null));
        if (onDismissListener != null)
            dialog.setOnDismissListener(dialog -> onDismissListener.onDismiss(null));

        SparseBooleanArray booleanArray = new SparseBooleanArray();
        if (value != null) booleanArray.put(Integer.parseInt(value), true);
        else if (entry != null) {
            int index = -1;
            for (int i = 0; i < entries.length; i++) {
                if (Objects.equals(entries[i], entry))
                    index = i;
            }
            if (index != -1) booleanArray.put(index, true);
        }

        MiuixCardView cardView = new MiuixCardView(context);
        cardView.setRadius(context.getResources().getDimensionPixelSize(R.dimen.miuix_item_radius));

        window.setContentView(cardView);
        window.getDecorView().setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        window.setWindowAnimations(R.style.MiuixDropDownDialogAnimation);

        MiuixListAdapter adapter = new MiuixListAdapter(context);
        adapter.setItems(entries);
        adapter.setBooleanArray(booleanArray);
        adapter.setOnChooseItemListener(new OnChooseItemListener() {
            @Override
            public boolean onChooseBefore(CharSequence item, int which) {
                if (onChooseItemListener == null || onChooseItemListener.onChooseBefore(item, which)) {
                    setValue(String.valueOf(which));
                    setEntry(entries[which]);
                    return true;
                } else return false;
            }

            @Override
            public void onChooseAfter(CharSequence[] items, CharSequence[] selectedItems, Integer[] selectedValues) {
                if (onChooseItemListener != null)
                    onChooseItemListener.onChooseAfter(items, selectedItems, selectedValues);
                dismiss();
            }
        });

        addView(cardView, adapter.getRecyclerView());
        updateLocation();
    }

    private void updateLocation() {
        boolean isSmallWindowMode = screenPoint.x != windowPoint.x || screenPoint.y != windowPoint.y;

        int viewWidth = targetView.getWidth();
        int viewHeight = targetView.getHeight();
        int[] location = new int[2];
        targetView.getLocationInWindow(location);
        int viewX = location[0];
        int viewY = location[1];

        int dialogHeight = calculateHeight();
        int spaceBelow = windowPoint.y - (viewY + viewHeight);
        boolean showBelow = (spaceBelow - dialogHeight) > (viewY - viewHeight - dialogHeight);
        boolean showRight = x > ((float) windowPoint.x / 2);

        window.setGravity(Gravity.TOP | (showRight ? Gravity.RIGHT : Gravity.LEFT));
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = context.getResources().getDimensionPixelSize(R.dimen.miuix_drop_down_margin_start_end);
        params.y = isSmallWindowMode ?
            (showBelow ? viewY + viewHeight : viewY - dialogHeight) :
            (showBelow ?
                viewY + (int) (viewHeight / 3.2) :
                viewY - dialogHeight - (int) (viewHeight / 3.2));
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = dialogHeight;
        window.setAttributes(params);

        if (showBelow) {
            if (showRight)
                window.setWindowAnimations(R.style.MiuixDropDownDialogAnimation_RightTop);
            else window.setWindowAnimations(R.style.MiuixDropDownDialogAnimation_LeftTop);
        } else {
            if (showRight)
                window.setWindowAnimations(R.style.MiuixDropDownDialogAnimation_RightBottom);
            else window.setWindowAnimations(R.style.MiuixDropDownDialogAnimation_LeftBottom);
        }
    }

    private int calculateHeight() {
        if (entries != null) {
            int height = context.getResources().getDimensionPixelSize(R.dimen.miuix_item_min_height) * entries.length;
            int maxHeight = isVerticalScreen ? (int) (screenPoint.y / 2.9) : (int) (screenPoint.y / 2.1);
            return Math.min(height, maxHeight);
        }
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void addView(@NonNull ViewGroup viewGroup, @NonNull View view) {
        ViewGroup group = (ViewGroup) view.getParent();
        if (group != viewGroup) {
            if (group != null) group.removeView(view);
            viewGroup.addView(view);
        }
    }
}
