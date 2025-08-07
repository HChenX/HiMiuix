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
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.MiuixSeekBarView;
import com.hchen.himiuix.R;

/**
 * SeekBar Preference
 *
 * @author 焕晨HChen
 */
public class MiuixSeekBarPreference extends MiuixPreference implements SeekBar.OnSeekBarChangeListener {
    private MiuixSeekBarView xSeekBarView;
    private int value;
    private int defValue;
    private int maxValue;
    private int minValue;
    private int stepValue;
    private int dividerValue;
    private String format;
    private boolean isShowValueOnTip;
    private boolean isShowDefaultPoint;
    private boolean isDialogModeEnabled;
    private boolean isAlwaysHapticFeedback;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener;

    public MiuixSeekBarPreference(@NonNull Context context) {
        super(context);
    }

    public MiuixSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixSeekBarPreference, defStyleAttr, defStyleRes);
        value = typedArray.getInt(R.styleable.MiuixSeekBarPreference_android_value, Integer.MIN_VALUE);
        defValue = typedArray.getInt(R.styleable.MiuixSeekBarPreference_android_defaultValue, Integer.MIN_VALUE);
        maxValue = typedArray.getInt(R.styleable.MiuixSeekBarPreference_maxValue, Integer.MIN_VALUE);
        minValue = typedArray.getInt(R.styleable.MiuixSeekBarPreference_minValue, Integer.MIN_VALUE);
        stepValue = typedArray.getInt(R.styleable.MiuixSeekBarPreference_stepValue, Integer.MIN_VALUE);
        dividerValue = typedArray.getInt(R.styleable.MiuixSeekBarPreference_dividerValue, Integer.MIN_VALUE);
        format = typedArray.getString(R.styleable.MiuixSeekBarPreference_android_format);
        isShowDefaultPoint = typedArray.getBoolean(R.styleable.MiuixSeekBarPreference_showDefaultPoint, true);
        isShowValueOnTip = typedArray.getBoolean(R.styleable.MiuixSeekBarPreference_showValueOnTip, true);
        isDialogModeEnabled = typedArray.getBoolean(R.styleable.MiuixSeekBarPreference_enableDialogMode, false);
        isAlwaysHapticFeedback = typedArray.getBoolean(R.styleable.MiuixSeekBarPreference_alwaysHapticFeedback, false);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    int loadLayoutResource() {
        return R.layout.miuix_seekbar_preference;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        xSeekBarView = (MiuixSeekBarView) xBasicView;

        xSeekBarView.setValue(value);
        xSeekBarView.setDefValue(defValue);
        xSeekBarView.setMaxValue(maxValue);
        xSeekBarView.setMinValue(minValue);
        xSeekBarView.setStepValue(stepValue);
        xSeekBarView.setFormat(format);
        xSeekBarView.setDividerValue(dividerValue);
        xSeekBarView.setShowValueOnTip(isShowValueOnTip);
        xSeekBarView.setDialogModeEnabled(isDialogModeEnabled);
        xSeekBarView.setShowDefaultPoint(isShowDefaultPoint);
        xSeekBarView.setAlwaysHapticFeedback(isAlwaysHapticFeedback);
        xSeekBarView.setOnSeekBarChangeListener(this);
    }

    public void setValue(int value) {
        this.value = value;
        if (xSeekBarView != null)
            xSeekBarView.setValue(value);
    }

    public void setDefValue(int defValue) {
        this.defValue = defValue;
        if (xSeekBarView != null)
            xSeekBarView.setDefValue(defValue);
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        if (xSeekBarView != null)
            xSeekBarView.setMaxValue(maxValue);
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        if (xSeekBarView != null)
            xSeekBarView.setMinValue(minValue);
    }

    public void setStepValue(int stepValue) {
        this.stepValue = stepValue;
        if (xSeekBarView != null)
            xSeekBarView.setStepValue(stepValue);
    }

    public void setDividerValue(int dividerValue) {
        this.dividerValue = dividerValue;
        if (xSeekBarView != null)
            xSeekBarView.setDividerValue(dividerValue);
    }

    public void setFormat(String format) {
        this.format = format;
        if (xSeekBarView != null)
            xSeekBarView.setFormat(format);
    }

    public void setShowValueOnTip(boolean show) {
        this.isShowValueOnTip = show;
        if (xSeekBarView != null)
            xSeekBarView.setShowValueOnTip(show);
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        this.onSeekBarChangeListener = listener;
    }

    public void setDialogModeEnabled(boolean enabled) {
        this.isDialogModeEnabled = enabled;
        if (xSeekBarView != null)
            xSeekBarView.setDialogModeEnabled(enabled);
    }

    public void setShowDefaultPoint(boolean show) {
        this.isShowDefaultPoint = show;
        if (xSeekBarView != null)
            xSeekBarView.setShowDefaultPoint(show);
    }

    public void setAlwaysHapticFeedback(boolean enabled) {
        this.isAlwaysHapticFeedback = enabled;
        if (xSeekBarView != null)
            xSeekBarView.setAlwaysHapticFeedback(enabled);
    }

    public int getValue() {
        return xSeekBarView.getValue();
    }

    public int getDefValue() {
        return xSeekBarView.getDefValue();
    }

    public int getMaxValue() {
        return xSeekBarView.getMaxValue();
    }

    public int getMinValue() {
        return xSeekBarView.getMinValue();
    }

    public int getStepValue() {
        return xSeekBarView.getStepValue();
    }

    public int getDividerValue() {
        return xSeekBarView.getDividerValue();
    }

    public String getFormat() {
        return xSeekBarView.getFormat();
    }

    public boolean isShowValueOnTip() {
        return xSeekBarView.isShowValueOnTip();
    }

    public boolean isDialogModeEnabled() {
        return xSeekBarView.isDialogModeEnabled();
    }

    public boolean isShowDefaultPoint() {
        return xSeekBarView.isShowDefaultPoint();
    }

    public boolean isAlwaysHapticFeedback() {
        return xSeekBarView.isAlwaysHapticFeedback();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        value = progress;
        if (onSeekBarChangeListener != null)
            onSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (onSeekBarChangeListener != null)
            onSeekBarChangeListener.onStartTrackingTouch(seekBar);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        persistInt(getValue());
        notifyDependencyChange(getShouldDisableView());
        notifyChanged();
        if (onSeekBarChangeListener != null)
            onSeekBarChangeListener.onStopTrackingTouch(seekBar);
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue == null) defaultValue = 0;
        setValue(getPersistedInt((Integer) defaultValue));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent())
            return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.value = value;
        savedState.defValue = getDefValue();
        savedState.maxValue = getMaxValue();
        savedState.minValue = getMinValue();
        savedState.stepValue = getStepValue();
        savedState.dividerValue = getDividerValue();
        savedState.format = getFormat();
        savedState.isShowValueOnTip = isShowValueOnTip();
        savedState.isShowDefaultPoint = isShowDefaultPoint();
        savedState.isDialogModeEnabled = isDialogModeEnabled();
        savedState.isAlwaysHapticFeedback = isAlwaysHapticFeedback();

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
        setDefValue(savedState.defValue);
        setMaxValue(savedState.maxValue);
        setMinValue(savedState.minValue);
        setStepValue(savedState.stepValue);
        setDividerValue(savedState.dividerValue);
        setFormat(savedState.format);
        setShowValueOnTip(savedState.isShowValueOnTip);
        setShowDefaultPoint(savedState.isShowDefaultPoint);
        setDialogModeEnabled(savedState.isDialogModeEnabled);
        setAlwaysHapticFeedback(savedState.isAlwaysHapticFeedback);
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

        int value;
        int defValue;
        int maxValue;
        int minValue;
        int stepValue;
        int dividerValue;
        String format;
        boolean isShowValueOnTip;
        boolean isShowDefaultPoint;
        boolean isDialogModeEnabled;
        boolean isAlwaysHapticFeedback;

        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
            defValue = source.readInt();
            maxValue = source.readInt();
            minValue = source.readInt();
            stepValue = source.readInt();
            dividerValue = source.readInt();
            format = source.readString();
            isShowValueOnTip = source.readBoolean();
            isShowDefaultPoint = source.readBoolean();
            isDialogModeEnabled = source.readBoolean();
            isAlwaysHapticFeedback = source.readBoolean();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
            dest.writeInt(defValue);
            dest.writeInt(maxValue);
            dest.writeInt(minValue);
            dest.writeInt(stepValue);
            dest.writeInt(dividerValue);
            dest.writeString(format);
            dest.writeBoolean(isShowValueOnTip);
            dest.writeBoolean(isShowDefaultPoint);
            dest.writeBoolean(isDialogModeEnabled);
            dest.writeBoolean(isAlwaysHapticFeedback);
        }
    }
}
