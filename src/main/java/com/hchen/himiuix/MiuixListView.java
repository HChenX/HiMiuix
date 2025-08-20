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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.callback.MiuixDialogInterface;
import com.hchen.himiuix.callback.OnChooseItemListener;
import com.hchen.himiuix.dialog.MiuixAlertDialog;
import com.hchen.himiuix.helper.HapticFeedbackHelper;
import com.hchen.himiuix.list.MiuixListAdapter;
import com.hchen.himiuix.springback.SpringBackLayout;
import com.hchen.himiuix.widget.MiuixCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

/**
 * Miuix List
 *
 * @author 焕晨HChen
 */
public class MiuixListView extends MiuixBasicView implements OnChooseItemListener {
    private MiuixListAdapter xListAdapter;
    private MiuixCardView xCardView;
    private SparseBooleanArray booleanArray;
    private CharSequence[] lastItems;
    private CharSequence[] items;
    private CharSequence[] selectedItems;
    private Integer[] selectedValues;
    private Drawable[] icons;
    private int maxHeight;
    private boolean isMultipleChoiceEnabled;
    private boolean isDialogModeEnabled;
    private OnChooseItemListener listener;
    private boolean isShowing;

    public MiuixListView(@NonNull Context context) {
        super(context);
    }

    public MiuixListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixListView, defStyleAttr, defStyleRes);
        items = typedArray.getTextArray(R.styleable.MiuixListView_android_entries);
        maxHeight = typedArray.getDimensionPixelSize(R.styleable.MiuixListView_android_maxHeight, LayoutParams.WRAP_CONTENT);
        isMultipleChoiceEnabled = typedArray.getBoolean(R.styleable.MiuixListView_multipleChoiceEnabled, true);
        isDialogModeEnabled = typedArray.getBoolean(R.styleable.MiuixListView_enableDialogMode, true);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void loadViewWhenBuild() {
        super.loadViewWhenBuild();
        if (!isDialogModeEnabled) {
            booleanArray = new SparseBooleanArray();
            xCardView = new MiuixCardView(getContext());
            xCardView.setCardBackgroundColor(getContext().getColor(R.color.miuix_card_other));
            xCardView.setRadius(getContext().getResources().getDimensionPixelSize(R.dimen.miuix_item_radius));

            xListAdapter = new MiuixListAdapter(getContext());
            xListAdapter.setItemBackgroundColor(getContext().getColor(android.R.color.transparent));
            SpringBackLayout springBackLayout = new SpringBackLayout(getContext());
            springBackLayout.setGluttonEnabled(true);
            springBackLayout.setTarget(xListAdapter.getRecyclerView());
            addView(springBackLayout, xListAdapter.getRecyclerView());
            addView(xCardView, springBackLayout);

            if (maxHeight != LayoutParams.WRAP_CONTENT) {
                ViewGroup.LayoutParams params = xListAdapter.getRecyclerView().getLayoutParams();
                params.height = maxHeight;
                xListAdapter.getRecyclerView().setLayoutParams(params);
            }
            setCustomView(xCardView);
        }
    }

    @Override
    void updateViewContent() {
        super.updateViewContent();
        if (!isDialogModeEnabled) {
            booleanArray.clear();
            if (!Arrays.equals(lastItems, items)) {
                xListAdapter.setItems(items);
                if (lastItems != null)
                    xListAdapter.notifyDataSetChanged();
                lastItems = items;
            }
            xListAdapter.setIcons(icons);
            xListAdapter.setOnChooseItemListener(this);
            xListAdapter.setMultipleChoiceEnabled(isMultipleChoiceEnabled);
            if (selectedValues != null || selectedItems != null) {
                if (selectedItems != null && selectedValues == null) {
                    ArrayList<Integer> integers = new ArrayList<>();
                    HashSet<CharSequence> set = new HashSet<>(Arrays.asList(selectedItems));
                    for (int i = 0; i < items.length; i++) {
                        if (set.contains(items[i]))
                            integers.add(i);
                    }
                    selectedValues = integers.toArray(new Integer[0]);
                }

                HashSet<Integer> hashSet = new HashSet<>(Arrays.asList(selectedValues));
                for (int i = 0; i < items.length; i++) {
                    if (hashSet.contains(i)) {
                        booleanArray.put(i, true);
                        if (!isMultipleChoiceEnabled)
                            break;
                    }
                }
                xListAdapter.setBooleanArray(booleanArray);
            }
        }
    }

    @Override
    void updateVisibility() {
        super.updateVisibility();
    }

    @Override
    public boolean forceShowCustomIndicatorView() {
        return isDialogModeEnabled;
    }

    @Override
    public boolean performClick() {
        if (isDialogModeEnabled) {
            if (!isShowing) {
                new MiuixAlertDialog(getContext())
                    .setTitle(getTitle())
                    .setMessage(getSummary())
                    .setListModeEnabled(true)
                    .setItems(items)
                    .setSelectedValues(selectedValues)
                    .setMultipleChoiceEnabled(isMultipleChoiceEnabled)
                    .setOnBindViewListener(new MiuixDialogInterface.OnBindViewListener() {
                        @Override
                        public void onBindView(@NonNull ViewGroup root, @NonNull View view) {
                            if (maxHeight != LayoutParams.WRAP_CONTENT) {
                                ViewGroup.LayoutParams params = view.getLayoutParams();
                                params.height = maxHeight;
                                view.setLayoutParams(params);
                            }
                        }
                    })
                    .setOnChooseItemListener(this)
                    .setNegativeButton(getContext().getText(R.string.dialog_negative), null)
                    .setPositiveButton(getContext().getText(R.string.dialog_positive),
                        (dialog, which) -> {
                            if (listener != null)
                                listener.onChooseAfter(items, selectedItems, selectedValues);
                        }
                    )
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setHapticFeedbackEnabled(true)
                    .setOnShowListener(dialog -> isShowing = true)
                    .setOnDismissListener(dialog -> {
                        isShowing = false;
                        getShadowHelper().restoreOriginalColor();
                    })
                    .show();
            }
        }
        return super.performClick();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isEnabled() && isDialogModeEnabled && ev.getAction() == MotionEvent.ACTION_UP) {
            getShadowHelper().setKeepShadow();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setItems(CharSequence[] items) {
        if (Arrays.equals(this.items, items)) return;
        this.items = items;
        refreshView();
    }

    public void setSelectedItems(CharSequence[] selectedItems) {
        if (Arrays.equals(this.selectedItems, selectedItems)) return;
        this.selectedItems = selectedItems;
        refreshView();
    }

    public void setSelectedValues(Integer[] selectedValues) {
        if (Arrays.equals(this.selectedValues, selectedValues)) return;
        this.selectedValues = selectedValues;
        refreshView();
    }

    public void setIcons(Drawable[] icons) {
        if (Arrays.deepEquals(this.icons, icons)) return;
        this.icons = icons;
        refreshView();
    }

    public void setMultipleChoiceEnabled(boolean enabled) {
        if (isMultipleChoiceEnabled == enabled) return;
        isMultipleChoiceEnabled = enabled;
        refreshView();
    }

    public void setOnChooseItemListener(OnChooseItemListener listener) {
        if (Objects.equals(this.listener, listener)) return;
        this.listener = listener;
        refreshView();
    }

    public void setMaxHeight(int maxHeight) {
        if (this.maxHeight == maxHeight) return;
        this.maxHeight = maxHeight;
        refreshView();
    }

    public CharSequence[] getItems() {
        return items;
    }

    public CharSequence[] getSelectedItems() {
        return selectedItems;
    }

    public Integer[] getSelectedValues() {
        return selectedValues;
    }

    public Drawable[] getIcons() {
        return icons;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public boolean isMultipleChoiceEnabled() {
        return isMultipleChoiceEnabled;
    }

    @Override
    public boolean onChooseBefore(CharSequence item, int which) {
        if (listener == null || listener.onChooseBefore(item, which)) {
            HapticFeedbackHelper.performHapticFeedback(this, HapticFeedbackHelper.MIUI_POPUP_NORMAL);
            return true;
        }
        return false;
    }

    @Override
    public void onChooseAfter(CharSequence[] items, CharSequence[] selectedItems, Integer[] selectedValues) {
        this.selectedItems = selectedItems;
        this.selectedValues = selectedValues;

        if (listener != null && !isDialogModeEnabled)
            listener.onChooseAfter(items, selectedItems, selectedValues);
    }
}
