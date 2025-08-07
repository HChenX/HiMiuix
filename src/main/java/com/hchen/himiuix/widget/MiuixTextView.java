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
package com.hchen.himiuix.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.hchen.himiuix.R;

/**
 * Miuix TextView
 *
 * @author 焕晨HChen
 */
public class MiuixTextView extends AppCompatTextView {
    private boolean focusable = false;
    private boolean singleLineCenter = false;

    public MiuixTextView(@NonNull Context context) {
        this(context, null);
    }

    public MiuixTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MiuixTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuixTextView, defStyleAttr, 0);
        focusable = array.getBoolean(R.styleable.MiuixTextView_android_focusable, false);
        singleLineCenter = array.getBoolean(R.styleable.MiuixTextView_singleLineCenter, false);
        array.recycle();
    }

    @Override
    public boolean isFocused() {
        return focusable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!singleLineCenter) return;
        if (getLineCount() <= 1) setGravity(Gravity.CENTER_HORIZONTAL);
        else setGravity(Gravity.LEFT);
    }
}
