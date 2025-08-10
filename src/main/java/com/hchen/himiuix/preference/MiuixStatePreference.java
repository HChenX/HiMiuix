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
import com.hchen.himiuix.MiuixStateView;
import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnStateChangeListener;

/**
 * State Preference
 *
 * @author 焕晨HChen
 */
class MiuixStatePreference extends MiuixPreference implements OnStateChangeListener {
    private CharSequence tipOn;
    private CharSequence tipOff;
    private CharSequence summaryOn;
    private CharSequence summaryOff;
    boolean isChecked;
    private boolean isDisableDependentsState;
    OnStateChangeListener onStateChangeListener;

    public MiuixStatePreference(@NonNull Context context) {
        super(context);
    }

    public MiuixStatePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixStatePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixStatePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixStatePreference, defStyleAttr, defStyleRes);
        tipOn = array.getText(R.styleable.MiuixStatePreference_tipOn);
        tipOff = array.getText(R.styleable.MiuixStatePreference_tipOff);
        summaryOn = array.getText(R.styleable.MiuixStatePreference_android_summaryOn);
        summaryOff = array.getText(R.styleable.MiuixStatePreference_android_summaryOff);
        isDisableDependentsState = array.getBoolean(R.styleable.MiuixStatePreference_disableDependentsState, false);
        array.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    int loadLayoutResource() {
        return super.loadLayoutResource();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        MiuixStateView xStateView = holder.itemView.findViewById(R.id.miuix_prefs);

        xStateView.setManuallyRefreshViewMode(true);

        xStateView.setOnStateChangeListener(null);
        xStateView.setOnStateChangeListener(this);

        xStateView.setChecked(isChecked);
        xStateView.setTipOn(tipOn);
        xStateView.setTipOff(tipOff);
        xStateView.setSummaryOn(summaryOn);
        xStateView.setSummaryOff(summaryOff);

        xStateView.setManuallyRefreshViewMode(false);
        xStateView.refreshView();
    }

    @Override
    public void refreshed(MiuixBasicView view) {
        // 阻止显示指示器
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
        notifyChanged();
    }

    public void setTipOn(CharSequence tipOn) {
        this.tipOn = tipOn;
        notifyChanged();
    }

    public void setTipOff(CharSequence tipOff) {
        this.tipOff = tipOff;
        notifyChanged();
    }

    public void setSummaryOn(CharSequence summaryOn) {
        this.summaryOn = summaryOn;
        notifyChanged();
    }

    public void setSummaryOff(CharSequence summaryOff) {
        this.summaryOff = summaryOff;
        notifyChanged();
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        onStateChangeListener = listener;
        notifyChanged();
    }

    public void setDisableDependentsState(boolean disableDependentsState) {
        isDisableDependentsState = disableDependentsState;
        notifyChanged();
    }

    public CharSequence getTipOn() {
        return tipOn;
    }

    public CharSequence getTipOff() {
        return tipOff;
    }

    public CharSequence getSummaryOn() {
        return summaryOn;
    }

    public CharSequence getSummaryOff() {
        return summaryOff;
    }

    public boolean isDisableDependentsState() {
        return isDisableDependentsState;
    }

    @Override
    public boolean onStateChange(boolean newValue) {
        if (onStateChangeListener == null || onStateChangeListener.onStateChange(newValue)) {
            isChecked = newValue;
            persistBoolean(isChecked);
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldDisableDependents() {
        return (isDisableDependentsState == isChecked) || super.shouldDisableDependents();
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getBoolean(index, false);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue == null) defaultValue = false;
        setChecked(getPersistedBoolean((Boolean) defaultValue));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent()) return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.isChecked = isChecked();
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
        setChecked(savedState.isChecked);
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

        boolean isChecked;

        public SavedState(Parcel source) {
            super(source);
            isChecked = source.readBoolean();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBoolean(isChecked);
        }
    }
}
