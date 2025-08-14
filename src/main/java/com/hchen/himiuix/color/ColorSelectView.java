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
package com.hchen.himiuix.color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.R;

/**
 * 色盘缩略图
 *
 * @author 焕晨HChen
 */
public class ColorSelectView extends View {
    private float width;
    private float radius;
    private int color;
    private final Paint paint = new Paint();
    private final PaintFlagsDrawFilter filter = new PaintFlagsDrawFilter(0, 3);
    private SweepGradient sweepGradient;
    private final int[] colors = {
        Color.parseColor("#FF6565"),
        Color.parseColor("#FFBB55"),
        Color.parseColor("#FFF175"),
        Color.parseColor("#B3FFA7"),
        Color.parseColor("#98FFF9"),
        Color.parseColor("#7B98FF"),
        Color.parseColor("#FF75E1"),
    };

    public ColorSelectView(Context context) {
        this(context, null);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        width = -1.0F;
        post(() -> {
            update(getWidth());
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0) return;
        update(w);
    }

    public void setColor(int color) {
        if (this.color == color) return;
        this.color = color;
    }

    private void update(float width) {
        this.width = width;
        radius = width / 2;

        Matrix matrix = new Matrix();
        matrix.preRotate(90, radius, radius);
        sweepGradient = new SweepGradient(radius, radius, colors, null);
        sweepGradient.setLocalMatrix(matrix);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (width != -1.0F) {
            canvas.setDrawFilter(filter);
            paint.reset();
            paint.setColor(getContext().getColor(android.R.color.transparent));
            canvas.drawCircle(radius, radius, radius, paint);
            paint.reset();
            paint.setShader(sweepGradient);
            canvas.drawCircle(radius, radius, radius - 0.2F, paint);
            paint.reset();
            paint.setColor(getContext().getColor(R.color.miuix_color_select_def));
            canvas.drawCircle(radius, radius, radius - width / 9.0F, paint);
            paint.setColor(color);
            canvas.drawCircle(radius, radius, radius - width / 5.0F, paint);
        }

        super.onDraw(canvas);
    }
}
