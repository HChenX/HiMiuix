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
package com.hchen.himiuix.fill;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hchen.himiuix.R;

/**
 * 适用于 MiuixAppBar/MiuixBottomNavigatorView 的填充视图
 *
 * @author 焕晨HChen
 */
public class MiuixRecyclerFillView extends RecyclerView {
    public static final String View_Tag = "Miuix:RecyclerFillView";
    private int collapsibleTitleHeight;
    private int navigationBarHeight;
    private int statusBarHeight;
    private int defToolbarHeight;
    public static int defBottomHeight;

    public MiuixRecyclerFillView(Context context) {
        this(context, null);
    }

    public MiuixRecyclerFillView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixRecyclerFillView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setTag(View_Tag);
        initialStatusBarHeight();
        initialNavigationBarHeight();

        collapsibleTitleHeight = getResources().getDimensionPixelSize(R.dimen.miuix_appbar_collapsible_title_height);
        defBottomHeight = getResources().getDimensionPixelSize(R.dimen.miuix_bottom_menu_target_height);
        TypedArray array = getContext().obtainStyledAttributes(android.R.style.Widget_Toolbar, new int[]{android.R.attr.minHeight});
        defToolbarHeight = array.getDimensionPixelOffset(0, 0);
        array.recycle();

        setPaddingTop(statusBarHeight + defToolbarHeight + collapsibleTitleHeight);
        setPaddingBottom(navigationBarHeight + defBottomHeight);
    }

    private void setPaddingTop(int top) {
        setPadding(getPaddingLeft(), top, getPaddingRight(), getPaddingBottom());
    }

    private void setPaddingBottom(int bottom) {
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottom);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int lastStatusBarHeight = statusBarHeight;
        int lastNavigationBarHeight = navigationBarHeight;
        initialStatusBarHeight();
        initialNavigationBarHeight();
        if (statusBarHeight != lastStatusBarHeight)
            setPaddingTop(statusBarHeight + defToolbarHeight + collapsibleTitleHeight);
        if (lastNavigationBarHeight != navigationBarHeight)
            setPaddingBottom(navigationBarHeight + defBottomHeight);
    }

    private void initialStatusBarHeight() {
        @SuppressLint("InternalInsetResource")
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            statusBarHeight = getContext().getResources().getDimensionPixelSize(resourceId);
    }

    private void initialNavigationBarHeight() {
        @SuppressLint("InternalInsetResource")
        int resourceId = getContext().getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0)
            navigationBarHeight = getContext().getResources().getDimensionPixelSize(resourceId);
    }
}
