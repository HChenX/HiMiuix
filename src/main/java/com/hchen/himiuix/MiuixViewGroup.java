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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

/**
 * Miuix 视图组
 *
 * @author 焕晨HChen
 */
public class MiuixViewGroup extends LinearLayout {
    private CharSequence dividerTitle;
    private LinearLayout innerLayout;

    public MiuixViewGroup(Context context) {
        this(context, null);
    }

    public MiuixViewGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixViewGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixViewGroup, defStyleAttr, defStyleRes);
        dividerTitle = typedArray.getText(R.styleable.MiuixViewGroup_dividerTitle);
        boolean isMarginBottomEnabled = typedArray.getBoolean(R.styleable.MiuixViewGroup_marginBottomEnabled, false);
        typedArray.recycle();

        LayoutInflater.from(getContext()).inflate(R.layout.miuix_group_layout, this, true);
        setOrientation(LinearLayout.VERTICAL);
        updateDivider();

        innerLayout = findViewById(R.id.miuix_group);
        CardView cardView = findViewById(R.id.miuix_card);

        // 是否启用底部 margin
        // 这在项目处于底部时有奇效
        if (isMarginBottomEnabled) {
            LinearLayout.LayoutParams params = (LayoutParams) cardView.getLayoutParams();
            params.bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.miuix_basic_margin);
            cardView.setLayoutParams(params);
        }
    }

    @Nullable
    public CharSequence getDividerTitle() {
        return dividerTitle;
    }

    public MiuixViewGroup setDividerTitle(@Nullable CharSequence dividerTitle) {
        this.dividerTitle = dividerTitle;
        updateDivider();
        return this;
    }

    private void updateDivider() {
        TextView textView = findViewById(R.id.miuix_divider_title);
        View view = findViewById(R.id.miuix_divider);
        if (dividerTitle == null) {
            textView.setVisibility(GONE);
            view.setVisibility(VISIBLE);
        } else {
            textView.setText(dividerTitle);
            textView.setVisibility(VISIBLE);
            view.setVisibility(GONE);
        }

        invalidate();
    }

    @Override
    public void bringChildToFront(View child) {
        if (innerLayout != null) innerLayout.bringChildToFront(child);
        else super.bringChildToFront(child);
    }

    @Override
    public void addView(View child) {
        if (innerLayout != null) innerLayout.addView(child);
        else super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (innerLayout != null) innerLayout.addView(child, index);
        else super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (innerLayout != null) innerLayout.addView(child, width, height);
        else super.addView(child, width, height);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (innerLayout != null) innerLayout.addView(child, params);
        else super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (innerLayout != null) innerLayout.addView(child, index, params);
        else super.addView(child, index, params);
    }
}
