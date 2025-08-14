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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

import com.hchen.himiuix.R;

/**
 * Miuix SeekBar
 *
 * @author 焕晨HChen
 */
public class MiuixSeekBar extends AppCompatSeekBar {
    private Paint paint;
    private int defValue;
    private int stepCount;
    private boolean isStep;
    private boolean isShowDefaultPoint;
    private boolean isAlwaysHapticFeedback;

    public MiuixSeekBar(@NonNull Context context) {
        this(context, null);
    }

    public MiuixSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    protected void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(getContext().getColor(R.color.miuix_seekbar_def));

        setThumb(null);
        setBackground(null);
        setSplitTrack(false);
        setPadding(0, 0, 0, 0);
        setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.miuix_seekbar_progress));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isShowDefaultPoint) {
            int width = getWidth();
            int height = getHeight();
            float scaleWidth = (float) width / (getMax() - getMin());
            float xPosition = scaleWidth * (isStep ? (stepCount) : (defValue - getMin()));
            float yPosition = (float) height / 2;
            canvas.drawCircle(xPosition, yPosition, (float) ((float) height / 6.5), paint);
        }
    }

    public void setDefValue(int defValue) {
        this.defValue = defValue;
    }

    public void setDefStepCount(int stepCount) {
        this.stepCount = stepCount;
        isStep = true;
    }

    public void setShowDefaultPoint(boolean show) {
        isShowDefaultPoint = show;
    }

    public void setAlwaysHapticFeedback(boolean enabled) {
        isAlwaysHapticFeedback = enabled;
    }

    public boolean isAlwaysHapticFeedback() {
        return isAlwaysHapticFeedback;
    }
}
