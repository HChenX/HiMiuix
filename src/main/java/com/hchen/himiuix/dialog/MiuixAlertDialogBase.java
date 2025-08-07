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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiContext;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.MiuixDialogInterface;
import com.hchen.himiuix.callback.OnChooseItemListener;
import com.hchen.himiuix.helper.HapticFeedbackHelper;
import com.hchen.himiuix.list.MiuixListAdapter;
import com.hchen.himiuix.utils.MiuixUtils;
import com.hchen.himiuix.widget.MiuixCardView;
import com.hchen.himiuix.widget.MiuixEditText;
import com.hchen.himiuix.widget.MiuixTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Miuix Dialog Base
 *
 * @author 焕晨HChen
 */
abstract class MiuixAlertDialogBase implements MiuixDialogInterface {
    static final String TAG = "HiMiuix";
    @NonNull
    final Dialog dialog;
    @NonNull
    @UiContext
    final Context context;
    @NonNull
    final Resources resources;
    @NonNull
    final Point point;
    @NonNull
    final Window window;
    boolean isCreated;
    LinearLayout mainLayout;
    ImageView iconView;
    TextView titleView;
    MiuixTextView messageView;
    LinearLayout customLayout;
    LinearLayout buttonsLayout;
    List<ButtonInfo> buttonArray = new ArrayList<>();
    @Nullable
    ButtonInfo button1;
    @Nullable
    ButtonInfo button2;
    @Nullable
    ButtonInfo button3;
    MiuixListAdapter xListAdapter;

    String title;
    String message;
    Drawable icon;
    View customView;
    int customViewId = -1;
    OnBindViewListener onBindViewListener;
    boolean isEnableHapticFeedback;
    boolean isAutoDismiss = true;
    boolean isCancelable = true;
    boolean isCanceledOnTouchOutside = true;
    OnShowListener onShowListener;
    OnCancelListener onCancelListener;
    OnDismissListener onDismissListener;
    CharSequence[] items;
    Integer[] selectedValues;
    SparseBooleanArray booleanArray;
    boolean isListModeEnabled = false;
    boolean isMultipleChoiceEnabled = false;
    OnChooseItemListener onChooseItemListener;

    /**
     * @noinspection DeconstructionCanBeUsed
     */
    public record ButtonInfo(Button button, int key, String text, OnClickListener listener) {
        @Override
        @NonNull
        public String toString() {
            return "ButtonInfo{" +
                "key=" + key +
                ", button=" + button +
                ", text='" + text + '\'' +
                ", listener=" + listener +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ButtonInfo that)) return false;
            return key == that.key &&
                Objects.equals(text, that.text) &&
                Objects.equals(button, that.button) &&
                Objects.equals(listener, that.listener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, button, text, listener);
        }
    }

    MiuixAlertDialogBase(@NonNull Dialog dialog) {
        this.dialog = dialog;
        this.context = dialog.getContext();
        this.resources = context.getResources();
        this.window = Objects.requireNonNull(dialog.getWindow());
        this.point = MiuixUtils.getWindowSize(context);
    }

    final void init() {
        inflater();
        initWindow();
        loadView();
        updateContent();
        updateVisibility();
        updateLocation();

        if (isExistEditTextView(customLayout))
            window.setWindowAnimations(R.style.MiuixDialogAnimation_ExistIme);
    }

    abstract void inflater();

    void initWindow() {
    }

    @CallSuper
    void loadView() {
        iconView = mainLayout.findViewById(R.id.miuix_dialog_icon);
        titleView = mainLayout.findViewById(R.id.miuix_dialog_title);
        messageView = mainLayout.findViewById(R.id.miuix_dialog_message);
        customLayout = mainLayout.findViewById(R.id.miuix_dialog_custom);
        buttonsLayout = mainLayout.findViewById(R.id.miuix_dialog_buttons);

        if (customViewId != -1)
            customView = LayoutInflater.from(context).inflate(customViewId, null);
    }

    @CallSuper
    void updateContent() {
        iconView.setImageDrawable(icon);
        titleView.setText(title);
        messageView.setText(message);

        loadListViewIfNeed();
        if (customView != null) {
            addView(customLayout, customView);
            if (onBindViewListener != null)
                onBindViewListener.onBindView(customLayout, customView);
        }
    }

    @CallSuper
    void updateVisibility() {
        if (icon != null) iconView.setImageDrawable(icon);
        else iconView.setVisibility(GONE);

        if (title != null) titleView.setVisibility(VISIBLE);
        else titleView.setVisibility(GONE);

        if (message != null) messageView.setVisibility(VISIBLE);
        else messageView.setVisibility(GONE);

        if (customView != null) customLayout.setVisibility(VISIBLE);
        else customLayout.setVisibility(GONE);
    }

    @CallSuper
    void updateLocation() {
        // 基本的位置更新
        LinearLayout.LayoutParams params;
        if (iconView.getDrawable() != null && title != null) {
            params = (LinearLayout.LayoutParams) titleView.getLayoutParams();
            params.topMargin = resources.getDimensionPixelSize(R.dimen.miuix_dialog_margin);
            titleView.setLayoutParams(params);
        }
        if ((iconView.getDrawable() != null || title != null) && message != null) {
            params = (LinearLayout.LayoutParams) messageView.getLayoutParams();
            params.topMargin = resources.getDimensionPixelSize(R.dimen.miuix_dialog_margin);
            messageView.setLayoutParams(params);
        }
        if ((iconView.getDrawable() != null || title != null || message != null) && customView != null) {
            params = (LinearLayout.LayoutParams) customView.getLayoutParams();
            params.topMargin = resources.getDimensionPixelSize(R.dimen.miuix_dialog_margin);
            customView.setLayoutParams(params);
        }
        if (!isHorizontalMode()) {
            if ((iconView.getDrawable() != null || title != null || message != null || customView != null) && !buttonArray.isEmpty()) {
                params = (LinearLayout.LayoutParams) buttonsLayout.getLayoutParams();
                params.topMargin = resources.getDimensionPixelSize(R.dimen.miuix_dialog_margin);
                buttonsLayout.setLayoutParams(params);
            }
        }
    }

    boolean isHorizontalMode() {
        return false;
    }

    final void loadListViewIfNeed() {
        if (!isListModeEnabled) return;
        booleanArray = new SparseBooleanArray();

        MiuixCardView xCardView = new MiuixCardView(context);
        xCardView.setRadius(context.getResources().getDimensionPixelSize(R.dimen.miuix_item_radius));

        xListAdapter = new MiuixListAdapter(context);
        addView(xCardView, xListAdapter.getRecyclerView());
        customView = xCardView;

        xListAdapter.setItems(items);
        xListAdapter.setMultipleChoiceEnabled(isMultipleChoiceEnabled);
        xListAdapter.setOnChooseItemListener(onChooseItemListener);
        if (selectedValues != null) {
            HashSet<Integer> hashSet = new HashSet<>(Arrays.asList(selectedValues));
            for (int i = 0; i < items.length; i++) {
                if (hashSet.contains(i))
                    booleanArray.put(i, true);
            }
            xListAdapter.setBooleanArray(booleanArray);
        }
    }

    final void addView(@NonNull ViewGroup viewGroup, @NonNull View view) {
        ViewGroup group = (ViewGroup) view.getParent();
        if (group != viewGroup) {
            if (group != null) group.removeView(view);
            viewGroup.addView(view);
        }
    }

    final View.OnClickListener createButtonClickListener(int key, OnClickListener listener) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int finalKey = key;
                if (isEnableHapticFeedback)
                    HapticFeedbackHelper.performHapticFeedback(v, HapticFeedbackHelper.MIUI_BUTTON_MIDDLE);

                if (listener != null)
                    listener.onClick(MiuixAlertDialogBase.this, finalKey);
                if (isAutoDismiss) dismiss();
            }
        };
    }

    private boolean isExistEditTextView(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);
            if (v instanceof ViewGroup viewGroup) {
                isExistEditTextView(viewGroup);
            }
            if (v instanceof EditText || v instanceof MiuixEditText) {
                return true;
            }
        }
        return false;
    }

    final boolean isShowing() {
        return dialog.isShowing();
    }

    final void create() {
        if (isCreated) return;

        init();
        dialog.setOnShowListener(dialog -> {
            if (onShowListener != null)
                onShowListener.onShow(MiuixAlertDialogBase.this);
        });
        dialog.setOnCancelListener(dialog -> {
            if (onCancelListener != null)
                onCancelListener.onCancel(MiuixAlertDialogBase.this);
        });
        dialog.setOnDismissListener(dialog -> {
            if (onDismissListener != null)
                onDismissListener.onDismiss(MiuixAlertDialogBase.this);
        });
        dialog.setCancelable(isCancelable);
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        dialog.create();
        isCreated = true;
    }

    final void show() {
        if (!isCreated) create();
        dialog.show();
    }

    final public void cancel() {
        dialog.cancel();
        isCreated = false;
    }

    final public void dismiss() {
        dialog.dismiss();
        isCreated = false;
    }
}
