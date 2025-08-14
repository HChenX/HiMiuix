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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillValue;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.callback.OnCheckedChangeListener;
import com.hchen.himiuix.widget.MiuixRadioButton;

import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Radio Group
 *
 * @author 焕晨HChen
 */
public class MiuixRadioGroup extends LinearLayout implements MiuixRadioButtonView.OnInnerCheckedListener {
    private static final String TAG = "HiMiuix:RadioGroup";
    private final HashSet<MiuixRadioButtonView> radioButtonSet = new HashSet<>();
    private OnCheckedChangeListener onCheckedChangeListener;
    private int checkedId = -1;

    public MiuixRadioGroup(@NonNull Context context) {
        this(context, null);
    }

    public MiuixRadioGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixRadioGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixRadioGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(VERTICAL);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (checkedId != -1) updateCheckedState(checkedId);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof MiuixRadioButtonView view) {
            int viewId = view.getId();
            if (viewId == NO_ID) {
                viewId = generateViewId();
                view.setId(viewId);
            }
            view.setInnerCheckedListener(this);
            radioButtonSet.add(view);

            MiuixRadioButton button = view.getRadioButton();
            if (button.isChecked()) {
                if (checkedId != -1)
                    updateCheckedState(viewId);
                checkedId = viewId;
            }
        }
        super.addView(child, index, params);
    }

    public void check(@IdRes int id) {
        if (id != -1 && checkedId == id) return;
        updateCheckedState(checkedId);
    }

    @IdRes
    public int getCheckedId() {
        return checkedId;
    }

    public void clearCheck() {
        check(-1);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    private void updateCheckedState(int id) {
        if (checkedId == id) return;
        if (onCheckedChangeListener != null)
            onCheckedChangeListener.onCheckedChanged(this, id);

        checkedId = id;
        radioButtonSet.forEach(new Consumer<MiuixRadioButtonView>() {
            @Override
            public void accept(MiuixRadioButtonView view) {
                if (id == -1) view.setChecked(false);
                else view.setChecked(view.getId() == id);
            }
        });
    }

    @Override
    public void autofill(AutofillValue value) {
        if (!isEnabled()) return;

        if (!value.isList()) {
            Log.w(TAG, value + " could not be autofilled into " + this);
            return;
        }

        final int index = value.getListValue();
        final View child = getChildAt(index);
        if (child == null) {
            Log.w(TAG, "RadioGroup.autoFill(): no child with index " + index);
            return;
        }

        check(child.getId());
    }

    @Override
    public void onChecked(int id) {
        updateCheckedState(id);
    }
}
