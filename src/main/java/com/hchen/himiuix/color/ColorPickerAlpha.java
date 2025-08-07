package com.hchen.himiuix.color;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnHueChangedListener;

/**
 * Alpha 色盘
 *
 * @author 焕晨HChen
 */
class ColorPickerAlpha extends ColorPickerBaseSeekBar implements OnHueChangedListener {
    public ColorPickerAlpha(@NonNull Context context) {
        super(context);
    }

    public ColorPickerAlpha(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerAlpha(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    void updateBasicInfo() {
        setType(ColorPickerType.ALPHA);
        setMax(255);
        setProgress(255);
        setColors(
            Color.HSVToColor(0, new float[]{0, 1, 1}),
            Color.HSVToColor(255, new float[]{0, 1, 1})
        );
        setColorPickerBackground(ContextCompat.getDrawable(getContext(), R.drawable.miuix_color_picker_alpha_bg));
    }

    @Override
    void setValue(@FloatRange(from = 0, to = 255) float value) {
        super.setValue(value);
    }

    @Override
    public void onHueValueChanged(int value) {
        setColors(
            Color.HSVToColor(0, new float[]{value, 1, 1}),
            Color.HSVToColor(255, new float[]{value, 1, 1})
        );
    }
}
