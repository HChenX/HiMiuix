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
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.hchen.himiuix.R;

/**
 * Card View
 *
 * @author 焕晨HChen
 */
public class MiuixCardView extends CardView {
    private final Path path = new Path();
    private final Rect rect = new Rect();
    private final RectF rectF = new RectF();
    private float tlRadius;
    private float trRadius;
    private float brRadius;
    private float blRadius;

    public MiuixCardView(@NonNull Context context) {
        this(context, null);
    }

    public MiuixCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixCardView, defStyleAttr, 0);
        tlRadius = typedArray.getDimension(R.styleable.MiuixCardView_cardTopLeftRadius, 0);
        trRadius = typedArray.getDimension(R.styleable.MiuixCardView_cardTopRightRadius, 0);
        blRadius = typedArray.getDimension(R.styleable.MiuixCardView_cardBottomLeftRadius, 0);
        brRadius = typedArray.getDimension(R.styleable.MiuixCardView_cardBottomRightRadius, 0);
        typedArray.recycle();

        setRadius(0.0f);
        setCardElevation(0.0f);
        setPreventCornerOverlap(false);
        setUseCompatPadding(false);
        setCardBackgroundColor(getContext().getColor(android.R.color.transparent));
    }

    public MiuixCardView setTlRadius(float tlRadius) {
        this.tlRadius = tlRadius;
        return this;
    }

    public MiuixCardView setTrRadius(float trRadius) {
        this.trRadius = trRadius;
        return this;
    }

    public MiuixCardView setBrRadius(float brRadius) {
        this.brRadius = brRadius;
        return this;
    }

    public MiuixCardView setBlRadius(float blRadius) {
        this.blRadius = blRadius;
        return this;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (getRadius() == 0.0f) {
            float[] radius = {tlRadius, tlRadius, trRadius, trRadius, brRadius, brRadius, blRadius, blRadius};
            RectF rectF = getRectF();
            path.reset();
            path.addRoundRect(rectF, radius, Path.Direction.CW);
            canvas.clipPath(path);
        }
        super.onDraw(canvas);
    }

    private RectF getRectF() {
        getDrawingRect(rect);
        rectF.set(rect);
        return rectF;
    }
}
