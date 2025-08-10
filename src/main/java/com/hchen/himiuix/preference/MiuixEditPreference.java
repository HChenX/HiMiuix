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

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.hchen.himiuix.MiuixBasicView;
import com.hchen.himiuix.R;
import com.hchen.himiuix.dialog.MiuixAlertDialog;
import com.hchen.himiuix.widget.MiuixEditText;

/**
 * Edit Preference
 *
 * @author 焕晨HChen
 */
public class MiuixEditPreference extends MiuixPreference {
    private MiuixEditText xEditText;
    private CharSequence tip;
    private CharSequence hint;
    private Drawable icon;
    private boolean isAutoRequestFocus;
    private boolean isShowing;

    public MiuixEditPreference(@NonNull Context context) {
        super(context);
    }

    public MiuixEditPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixEditPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixEditPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixEditPreference, defStyleAttr, defStyleRes);
        tip = typedArray.getText(R.styleable.MiuixEditPreference_editTip);
        hint = typedArray.getText(R.styleable.MiuixEditPreference_android_hint);
        icon = typedArray.getDrawable(R.styleable.MiuixEditPreference_editIcon);
        isAutoRequestFocus = typedArray.getBoolean(R.styleable.MiuixEditPreference_autoRequestFocus, false);
        typedArray.recycle();

        xEditText = new MiuixEditText(getContext());
        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void refreshed(MiuixBasicView view) {
        // 始终显示指示器
        view.getIndicatorView().setVisibility(VISIBLE);
    }

    @Override
    @SuppressLint("RestrictedApi")
    protected void performClick(@NonNull View view) {
        if (isShowing) return;

        new MiuixAlertDialog(getContext())
            .setTitle(getTitle())
            .setMessage(getSummary())
            .setCustomView(xEditText)
            .setOnBindViewListener((root, view1) -> {
                xEditText.setHint(hint);
                xEditText.setTipText(tip);
                xEditText.setImageDrawable(icon);
                xEditText.setAutoRequestFocus(isAutoRequestFocus);
            })
            .setHapticFeedbackEnabled(true)
            .setCanceledOnTouchOutside(false)
            .setCancelable(false)
            .setNegativeButton(getContext().getText(R.string.dialog_negative), null)
            .setPositiveButton(getContext().getText(R.string.dialog_positive), (dialog, which) -> {
                persistString(xEditText.getText().toString());
                notifyDependencyChange(getShouldDisableView());
                notifyChanged();
            })
            .setOnShowListener(dialog -> isShowing = true)
            .setOnDismissListener(dialog -> {
                isShowing = false;
            })
            .show();
    }

    // -------------------- Inner EditText --------------------
    public final void setHint(@StringRes int resId) {
        xEditText.setHint(resId);
    }

    public void setHint(@Nullable CharSequence hint) {
        xEditText.setHint(hint);
    }

    @Nullable
    public CharSequence getHint() {
        return xEditText.getHint();
    }

    public void setText(@StringRes int resId) {
        xEditText.setText(resId);
    }

    public void setText(@Nullable CharSequence text) {
        xEditText.setText(text);
    }

    @Nullable
    public CharSequence getText() {
        return xEditText.getText();
    }

    public void setInputType(int type) {
        xEditText.setInputType(type);
    }

    public void setImeOptions(int imeOptions) {
        xEditText.setImeOptions(imeOptions);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        xEditText.setOnClickListener(listener);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        xEditText.addTextChangedListener(watcher);
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        xEditText.removeTextChangedListener(watcher);
    }

    public void setKeyListener(KeyListener listener) {
        xEditText.setKeyListener(listener);
    }

    @Nullable
    public KeyListener getKeyListener() {
        return xEditText.getKeyListener();
    }

    public void setFilters(InputFilter[] filters) {
        xEditText.setFilters(filters);
    }

    @Nullable
    public InputFilter[] getFilters() {
        return xEditText.getFilters();
    }

    public boolean hasEditFocus() {
        return xEditText.hasFocus();
    }

    // -------------------------------------------------------

    // ------------------- Inner TipView --------------------
    public void setTipText(@StringRes int text) {
        xEditText.setTipText(text);
    }

    public void setTipText(CharSequence text) {
        xEditText.setTipText(text);
    }

    @Nullable
    public CharSequence getTipText() {
        return xEditText.getTipText();
    }

    public void setTipOnClickListener(View.OnClickListener listener) {
        xEditText.setTipOnClickListener(listener);
    }

    // -------------------------------------------------------

    // -------------------- Inner IconView --------------------
    public void setImageDrawable(Drawable drawable) {
        xEditText.setImageDrawable(drawable);
    }

    public void setImageIcon(Icon icon) {
        xEditText.setImageIcon(icon);
    }

    public void setImageBitmap(Bitmap bitmap) {
        xEditText.setImageBitmap(bitmap);
    }

    public void setImageResource(@DrawableRes int resId) {
        xEditText.setImageResource(resId);
    }

    public Drawable getDrawable() {
        return xEditText.getDrawable();
    }

    public void setIconOnClickListener(View.OnClickListener listener) {
        xEditText.setIconOnClickListener(listener);
    }
    // ---------------------------------------------------------

    public void setAutoRequestFocus(boolean auto) {
        xEditText.setAutoRequestFocus(auto);
    }

    public void setIntercept(boolean intercept) {
        xEditText.setIntercept(intercept);
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue == null) defaultValue = "";
        setText(getPersistedString((String) defaultValue));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent())
            return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.content = (String) getText();
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
        setText(savedState.content);
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

        String content;

        public SavedState(Parcel source) {
            super(source);
            content = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(content);
        }
    }
}
