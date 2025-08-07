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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnColorChangedListener;
import com.hchen.himiuix.dialog.MiuixAlertDialog;
import com.hchen.himiuix.widget.MiuixEditText;

/**
 * 色盘视图
 *
 * @author 焕晨HChen
 */
public class ColorPickerView extends LinearLayout implements OnColorChangedListener {
    private static final String TAG = "HiMiuix";
    private final ColorPickerData data = new ColorPickerData();
    private ColorPickerHue hue;
    private ColorPickerSaturation saturation;
    private ColorPickerLightness lightness;
    private ColorPickerAlpha alpha;
    private View colorView;
    private MiuixEditText xEditText;
    private MiuixEditText xEditTextDialog;
    private boolean isDialogMode;
    private boolean isShowing;
    private int colorValue;
    private OnColorChangedListener listener;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 8 && xEditText.hasEditFocus())
                setColorValue(Color.parseColor("#" + s));
        }
    };

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.miuix_color_picker, this, true);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);

        hue = findViewById(R.id.miuix_color_hue);
        saturation = findViewById(R.id.miuix_color_saturation);
        lightness = findViewById(R.id.miuix_color_lightness);
        alpha = findViewById(R.id.miuix_color_alpha);
        colorView = findViewById(R.id.miuix_color_view);
        xEditText = findViewById(R.id.miuix_color_edit);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.miuix_color_radius));
        colorView.setBackground(drawable);

        xEditText.setTipText("#");
        xEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789abcdefABCDEF"));
        xEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

        xEditTextDialog = new MiuixEditText(getContext());
        xEditTextDialog.setTipText("#");
        xEditTextDialog.setKeyListener(DigitsKeyListener.getInstance("0123456789abcdefABCDEF"));
        xEditTextDialog.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

        apply();
    }

    private void apply() {
        hue.setListener(this);
        saturation.setListener(this);
        lightness.setListener(this);
        alpha.setListener(this);

        hue.setListeners(saturation, lightness, alpha);
        colorValue = data.HSVToColor();
        updateContent(ColorPickerType.FINAL_COLOR, false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        xEditText.addTextChangedListener(textWatcher);
        xEditText.setOnClickListener(v -> {
            if (isDialogMode) {
                if (!isShowing) {
                    new MiuixAlertDialog(getContext())
                        .setTitle(getContext().getString(R.string.dialog_color_title))
                        .setMessage(getContext().getString(R.string.dialog_color_message))
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .setHapticFeedbackEnabled(true)
                        .setAutoDismiss(false)
                        .setCustomView(xEditTextDialog)
                        .setOnBindViewListener((root, view) -> {
                            ((MiuixEditText) view).setText(getFormatColor());
                        })
                        .setNegativeButton(getContext().getString(R.string.dialog_negative), (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(getContext().getString(R.string.dialog_positive), (dialog, which) -> {
                            String s = xEditTextDialog.getText().toString();

                            if (s.length() == 8) {
                                setColorValue(Color.parseColor("#" + s));
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getContext(), getContext().getString(R.string.dialog_color_message), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setOnShowListener(dialog -> {
                            isShowing = true;
                            getMiuixEditText().updateBackground(true);
                        })
                        .setOnDismissListener(dialog -> {
                            isShowing = false;
                            getMiuixEditText().updateBackground(false);
                        })
                        .show();
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        xEditText.removeTextChangedListener(textWatcher);
    }

    public void setColorValue(@ColorInt int argb) {
        this.colorValue = argb;
        updateContent(ColorPickerType.FINAL_COLOR, true);
    }

    public void setDialogMode(boolean dialogMode) {
        this.isDialogMode = dialogMode;
        xEditText.setIntercept(dialogMode);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    public void setAlwaysHapticFeedback(boolean alwaysHapticFeedback) {
        hue.setAlwaysHapticFeedback(alwaysHapticFeedback);
        lightness.setAlwaysHapticFeedback(alwaysHapticFeedback);
        saturation.setAlwaysHapticFeedback(alwaysHapticFeedback);
        alpha.setAlwaysHapticFeedback(alwaysHapticFeedback);
    }

    public int getColorValue() {
        return colorValue;
    }

    public String getFormatColor() {
        return formatColor(colorValue);
    }

    public MiuixEditText getMiuixEditText() {
        return xEditText;
    }

    public boolean isDialogMode() {
        return isDialogMode;
    }

    @Override
    public void onColorValueChanged(ColorPickerType type, int value) {
        switch (type) {
            case HUE -> data.hue = value;
            case SATURATION -> data.saturation = value;
            case LIGHTNESS -> data.lightness = value;
            case ALPHA -> data.alpha = value;
        }

        colorValue = data.HSVToColor();
        updateContent(ColorPickerType.COLOR_VALUE, false);
    }

    private void updateContent(ColorPickerType type, boolean updateValue) {
        ((GradientDrawable) colorView.getBackground()).setColor(colorValue);
        if (!xEditText.hasEditFocus()) xEditText.setText(formatColor(colorValue));
        if (listener != null) listener.onColorValueChanged(type, colorValue);

        if (updateValue) {
            float[] hsv = new float[3];
            Color.colorToHSV(colorValue, hsv);
            hue.setValue(Math.round(hsv[0]));
            saturation.setValue(Math.round(hsv[1]));
            lightness.setValue(Math.round(hsv[2]));
            alpha.setValue(Color.alpha(colorValue));

            data.hue = Math.round(hsv[0] * 100);
            data.saturation = Math.round(hsv[1] * 10000);
            data.lightness = Math.round(hsv[2] * 10000);
            data.alpha = Color.alpha(colorValue);
        }
    }

    public String formatColor(int argb) {
        int alpha = Color.alpha(argb);
        int red = Color.red(argb);
        int green = Color.green(argb);
        int blue = Color.blue(argb);
        return String.format("%02X%02X%02X%02X", alpha, red, green, blue);
    }
}
