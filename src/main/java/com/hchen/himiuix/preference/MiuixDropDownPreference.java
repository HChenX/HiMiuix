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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.MiuixBasicView;
import com.hchen.himiuix.MiuixDropDownView;
import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnChooseItemListener;

/**
 * DropDown Preference
 *
 * @author 焕晨HChen
 */
public class MiuixDropDownPreference extends MiuixPreference implements OnChooseItemListener {
    private CharSequence[] entries;
    private CharSequence entry;
    private String value;
    private boolean isShowOnTip;
    private OnChooseItemListener listener;

    public MiuixDropDownPreference(@NonNull Context context) {
        super(context);
    }

    public MiuixDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixDropDownPreference, defStyleAttr, defStyleRes);
        entries = typedArray.getTextArray(R.styleable.MiuixDropDownPreference_android_entries);
        entry = typedArray.getText(R.styleable.MiuixDropDownPreference_entry);
        value = typedArray.getString(R.styleable.MiuixDropDownPreference_android_value);
        isShowOnTip = typedArray.getBoolean(R.styleable.MiuixDropDownPreference_showOnTip, true);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    int loadLayoutResource() {
        return R.layout.miuix_drop_down_preference;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        MiuixDropDownView xDropDownView = holder.itemView.findViewById(R.id.miuix_prefs);

        xDropDownView.setOnChooseItemListener(null);
        xDropDownView.setOnChooseItemListener(this);

        xDropDownView.setEntries(entries);
        xDropDownView.setEntry(entry);
        xDropDownView.setValue(value);
        xDropDownView.setShowOnTip(isShowOnTip);
    }

    @Override
    public void refreshed(MiuixBasicView view) {
        // Do Nothing
    }

    public void setEntries(CharSequence[] entries) {
        this.entries = entries;
        notifyChanged();
    }

    public void setValue(String value) {
        this.value = value;
        notifyChanged();
    }

    public void setEntry(CharSequence entry) {
        this.entry = entry;
        notifyChanged();
    }

    public void setOnChooseItemListener(OnChooseItemListener listener) {
        this.listener = listener;
        notifyChanged();
    }

    public void setShowOnTip(boolean showOnTip) {
        isShowOnTip = showOnTip;
        notifyChanged();
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
    public boolean onChooseBefore(CharSequence item, int which) {
        if (listener == null || listener.onChooseBefore(item, which)) {
            entry = item;
            value = String.valueOf(which);
            persistString(value);
            notifyDependencyChange(getShouldDisableView());
            notifyChanged();
            return true;
        }
        return false;
    }

    @Override
    public void onChooseAfter(CharSequence[] items, CharSequence[] selectedItems, Integer[] selectedValues) {
        if (listener != null)
            listener.onChooseAfter(items, selectedItems, selectedValues);
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue == null) defaultValue = "0";
        setValue(getPersistedString((String) defaultValue));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent()) return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.value = getValue();
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
        setValue(savedState.value);
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

        String value;

        public SavedState(Parcel source) {
            super(source);
            value = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value);
        }
    }
}
