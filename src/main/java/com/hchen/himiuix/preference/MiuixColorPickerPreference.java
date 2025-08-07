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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.MiuixColorPickerView;
import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnColorChangedListener;
import com.hchen.himiuix.color.ColorPickerType;

/**
 * ColorPicker Preference
 *
 * @author 焕晨HChen
 */
public class MiuixColorPickerPreference extends MiuixPreference implements OnColorChangedListener {
    private MiuixColorPickerView xColorPickerView;
    private OnColorChangedListener listener;
    private boolean isAlwaysHapticFeedback;
    private boolean isShowValueOnTip;
    private int color;

    public MiuixColorPickerPreference(@NonNull Context context) {
        super(context);
    }

    public MiuixColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixColorPickerPreference, defStyleAttr, defStyleRes);
        color = typedArray.getColor(R.styleable.MiuixColorPickerPreference_android_color, -1);
        isShowValueOnTip = typedArray.getBoolean(R.styleable.MiuixColorPickerPreference_showValueOnTip, true);
        isAlwaysHapticFeedback = typedArray.getBoolean(R.styleable.MiuixColorPickerPreference_alwaysHapticFeedback, true);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    int loadLayoutResource() {
        return R.layout.miuix_color_picker_preference;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        xColorPickerView = (MiuixColorPickerView) xBasicView;

        xColorPickerView.setColor(color);
        xColorPickerView.setShowValueOnTip(isShowValueOnTip);
        xColorPickerView.setAlwaysHapticFeedback(isAlwaysHapticFeedback);
        xColorPickerView.setOnColorChangedListener(this);
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
        if (xColorPickerView != null)
            xColorPickerView.setColor(color);
    }

    public void setShowValueOnTip(boolean show) {
        this.isShowValueOnTip = show;
        if (xColorPickerView != null)
            xColorPickerView.setShowValueOnTip(show);
    }

    public void setAlwaysHapticFeedback(boolean enabled) {
        this.isAlwaysHapticFeedback = enabled;
        if (xColorPickerView != null)
            xColorPickerView.setAlwaysHapticFeedback(enabled);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    @ColorInt
    public int getColor() {
        return xColorPickerView.getColor();
    }

    public boolean isAlwaysHapticFeedback() {
        return xColorPickerView.isAlwaysHapticFeedback();
    }

    public boolean isShowValueOnTip() {
        return xColorPickerView.isShowValueOnTip();
    }

    @Override
    public void onColorValueChanged(ColorPickerType type, int value) {
        if (type == ColorPickerType.FINAL_COLOR) {
            color = value;
            if (listener != null)
                listener.onColorValueChanged(type, value);
            persistInt(color);
            notifyDependencyChange(getShouldDisableView());
            notifyChanged();
        }
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getInt(index, -1);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue == null) defaultValue = -1;
        setColor(getPersistedInt((Integer) defaultValue));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent())
            return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.color = getColor();
        savedState.isAlwaysHapticFeedback = isAlwaysHapticFeedback();
        savedState.isShowValueOnTip = isShowValueOnTip();
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
        setColor(savedState.color);
        setAlwaysHapticFeedback(savedState.isAlwaysHapticFeedback);
        setShowValueOnTip(savedState.isShowValueOnTip);
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

        int color;
        boolean isAlwaysHapticFeedback;
        boolean isShowValueOnTip;

        public SavedState(Parcel source) {
            super(source);
            color = source.readInt();
            isAlwaysHapticFeedback = source.readBoolean();
            isShowValueOnTip = source.readBoolean();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(color);
            dest.writeBoolean(isAlwaysHapticFeedback);
            dest.writeBoolean(isShowValueOnTip);
        }
    }
}
