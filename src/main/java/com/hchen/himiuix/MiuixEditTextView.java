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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.hchen.himiuix.widget.MiuixEditText;

/**
 * Miuix 输入框视图
 *
 * @author 焕晨HChen
 */
public class MiuixEditTextView extends MiuixBasicView {
    private String tip;
    private Drawable icon;
    private String hint;
    private boolean isAutoRequestFocus;
    private boolean isFirstLoad;
    private MiuixEditText xEditText;

    public MiuixEditTextView(@NonNull Context context) {
        super(context);
    }

    public MiuixEditTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixEditTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixEditTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixEditTextView, defStyleAttr, defStyleRes);
        tip = typedArray.getString(R.styleable.MiuixEditTextView_editTip);
        hint = typedArray.getString(R.styleable.MiuixEditTextView_android_hint);
        icon = typedArray.getDrawable(R.styleable.MiuixEditTextView_editIcon);
        isAutoRequestFocus = typedArray.getBoolean(R.styleable.MiuixEditTextView_autoRequestFocus, false);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void loadShadowHelper() {
        super.loadShadowHelper();
        setShadowHelperEnabled(false);
    }

    @Override
    void loadViewWhenBuild() {
        super.loadViewWhenBuild();
        xEditText = new MiuixEditText(getContext());
        setCustomView(xEditText);

        LayoutParams params = (LayoutParams) getTipView().getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        getTipView().setLayoutParams(params);

        isFirstLoad = true;
    }

    @Override
    void updateViewContent() {
        super.updateViewContent();

        if (isFirstLoad) {
            if (tip != null) setTipText(tip);
            if (hint != null) setHint(hint);
            if (icon != null) setImageDrawable(icon);
            setAutoRequestFocus(isAutoRequestFocus);
            isFirstLoad = false;
        }
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

    public void setOnClickListener(OnClickListener listener) {
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
        return xEditText.hasEditFocus();
    }

    @Override
    public void clearFocus() {
        xEditText.clearFocus();
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

    public void setTipOnClickListener(OnClickListener listener) {
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

    public void setIconOnClickListener(OnClickListener listener) {
        xEditText.setIconOnClickListener(listener);
    }

    // -----------------------------------------------------

    public void setAutoRequestFocus(boolean auto) {
        xEditText.setAutoRequestFocus(auto);
    }

    public void setIntercept(boolean intercept) {
        xEditText.setIntercept(intercept);
    }

    public MiuixEditText getInnerEditText() {
        return xEditText;
    }
}
