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
package com.hchen.himiuix.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.MiuixBasicView;
import com.hchen.himiuix.MiuixListView;
import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnChooseItemListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Miuix List Preference
 *
 * @author 焕晨HChen
 */
public class MiuixListPreference extends MiuixPreference implements OnChooseItemListener {
    private CharSequence[] items;
    private CharSequence[] selectedItems;
    private Integer[] selectedValues;
    private int maxHeight;
    private boolean isMultipleChoiceEnabled;
    private OnChooseItemListener onChooseItemListener;

    public MiuixListPreference(@NonNull Context context) {
        super(context);
    }

    public MiuixListPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixListPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixListPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixListPreference, defStyleAttr, defStyleRes);
        items = typedArray.getTextArray(R.styleable.MiuixListPreference_android_entries);
        maxHeight = typedArray.getDimensionPixelSize(R.styleable.MiuixListPreference_android_maxHeight, LinearLayout.LayoutParams.WRAP_CONTENT);
        isMultipleChoiceEnabled = typedArray.getBoolean(R.styleable.MiuixListPreference_multipleChoiceEnabled, true);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    int loadLayoutResource() {
        return R.layout.miuix_list_preference;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        MiuixListView xListView = holder.itemView.findViewById(R.id.miuix_prefs);

        xListView.setManuallyRefreshViewMode(true);

        xListView.setOnChooseItemListener(null);
        xListView.setOnChooseItemListener(this);

        xListView.setItems(items);
        xListView.setSelectedItems(selectedItems);
        xListView.setSelectedValues(selectedValues);
        xListView.setMultipleChoiceEnabled(isMultipleChoiceEnabled);
        xListView.setMaxHeight(maxHeight);

        xListView.setManuallyRefreshViewMode(false);
        xListView.refreshView();
    }

    @Override
    public void refreshed(MiuixBasicView view) {
    }

    public void setItems(CharSequence[] items) {
        this.items = items;
        notifyChanged();
    }

    public void setSelectedItems(CharSequence[] selectedItems) {
        this.selectedItems = selectedItems;
        notifyChanged();
    }

    public void setSelectedValues(Integer[] selectedValues) {
        this.selectedValues = selectedValues;
        notifyChanged();
    }

    public void setMultipleChoiceEnabled(boolean enabled) {
        isMultipleChoiceEnabled = enabled;
        notifyChanged();
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        notifyChanged();
    }

    public void setOnChooseItemListener(OnChooseItemListener listener) {
        this.onChooseItemListener = listener;
        notifyChanged();
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

    public boolean isMultipleChoiceEnabled() {
        return isMultipleChoiceEnabled;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public boolean onChooseBefore(CharSequence item, int which) {
        return onChooseItemListener == null || onChooseItemListener.onChooseBefore(item, which);
    }

    @Override
    public void onChooseAfter(CharSequence[] items, CharSequence[] selectedItems, Integer[] selectedValues) {
        this.selectedItems = selectedItems;
        this.selectedValues = selectedValues;
        persistStringSet(Arrays.stream(selectedValues).map(String::valueOf).collect(Collectors.toSet()));
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();

        if (onChooseItemListener != null)
            onChooseItemListener.onChooseAfter(items, selectedItems, selectedValues);
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getTextArray(index);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue == null) defaultValue = new HashSet<>();
        Set<String> set = getPersistedStringSet((Set<String>) defaultValue);
        setSelectedValues(set.stream().map(Integer::parseInt).toArray(Integer[]::new));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent()) return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.selectedValues = getSelectedValues();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setSelectedValues(savedState.selectedValues);
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private Integer[] selectedValues;

        public SavedState(Parcel source) {
            super(source);
            int length = source.readInt();
            if (length != -1) {
                int[] ints = new int[length];
                source.readIntArray(ints);
                selectedValues = Arrays.stream(ints).boxed().toArray(Integer[]::new);
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeIntArray(Arrays.stream(selectedValues).mapToInt(value -> value).toArray());
        }
    }
}
