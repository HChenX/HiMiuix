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
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.callback.MiuixDialogInterface;
import com.hchen.himiuix.dialog.MiuixAlertDialog;
import com.hchen.himiuix.helper.HapticFeedbackHelper;
import com.hchen.himiuix.widget.MiuixEditText;
import com.hchen.himiuix.widget.MiuixSeekBar;

import java.util.Optional;

/**
 * Miuix Seekbar 视图
 *
 * @author 焕晨HChen
 */
public class MiuixSeekBarView extends MiuixBasicView {
    private MiuixSeekBar xSeekBar;
    private int value;
    private int defValue;
    private int maxValue;
    private int minValue;
    private int stepValue;
    private int stepCount;
    private int dividerValue;
    private String format;
    private boolean isShowValueOnTip;
    private boolean isShowDefaultPoint;
    private boolean isDialogModeEnabled;
    private boolean isStep = false;
    private boolean isShowing = false;
    private boolean isAlwaysHapticFeedback;
    private MiuixEditText xEditText;
    private SeekBar.OnSeekBarChangeListener listener;

    public MiuixSeekBarView(@NonNull Context context) {
        super(context);
    }

    public MiuixSeekBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuixSeekBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuixSeekBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixSeekBarView, defStyleAttr, defStyleRes);
        value = typedArray.getInt(R.styleable.MiuixSeekBarView_android_value, Integer.MIN_VALUE);
        defValue = typedArray.getInt(R.styleable.MiuixSeekBarView_android_defaultValue, Integer.MIN_VALUE);
        maxValue = typedArray.getInt(R.styleable.MiuixSeekBarView_maxValue, Integer.MIN_VALUE);
        minValue = typedArray.getInt(R.styleable.MiuixSeekBarView_minValue, Integer.MIN_VALUE);
        stepValue = typedArray.getInt(R.styleable.MiuixSeekBarView_stepValue, Integer.MIN_VALUE);
        dividerValue = typedArray.getInt(R.styleable.MiuixSeekBarView_dividerValue, Integer.MIN_VALUE);
        format = typedArray.getString(R.styleable.MiuixSeekBarView_android_format);
        isShowDefaultPoint = typedArray.getBoolean(R.styleable.MiuixSeekBarView_showDefaultPoint, true);
        isShowValueOnTip = typedArray.getBoolean(R.styleable.MiuixSeekBarView_showValueOnTip, true);
        isDialogModeEnabled = typedArray.getBoolean(R.styleable.MiuixSeekBarView_enableDialogMode, false);
        isAlwaysHapticFeedback = typedArray.getBoolean(R.styleable.MiuixSeekBarView_alwaysHapticFeedback, false);
        typedArray.recycle();

        super.init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void loadViewWhenBuild() {
        super.loadViewWhenBuild();
        xSeekBar = new MiuixSeekBar(getContext());
        xSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isShowDefaultPoint) {
                    if (progress == defValue || (isStep && progress == calculateStepCount(defValue))) {
                        xSeekBar.setShowDefaultPoint(false);
                        HapticFeedbackHelper.performHapticFeedback(xSeekBar, HapticFeedbackHelper.MIUI_HOLD);
                    } else xSeekBar.setShowDefaultPoint(true);
                }
                if (fromUser) {
                    if (isAlwaysHapticFeedback)
                        HapticFeedbackHelper.performHapticFeedback(xSeekBar, HapticFeedbackHelper.MIUI_TAP_NORMAL);
                    MiuixSeekBarView.this.value = progress;
                }
                if (listener != null)
                    listener.onProgressChanged(seekBar, progress, fromUser);
                updateTipIfNeed();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (listener != null)
                    listener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (listener != null)
                    listener.onStopTrackingTouch(seekBar);
            }
        });
        setCustomView(xSeekBar);

        xEditText = new MiuixEditText(getContext());
        xEditText.setAutoRequestFocus(true);
        xEditText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);

        LayoutParams params = (LayoutParams) getTipView().getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        getTipView().setLayoutParams(params);
    }

    @Override
    void updateViewContent() {
        super.updateViewContent();

        initStepData();
        if (maxValue != Integer.MIN_VALUE) xSeekBar.setMax(isStep ? stepCount : maxValue);
        if (minValue != Integer.MIN_VALUE) xSeekBar.setMin(isStep ? 0 : minValue);
        if (value != Integer.MIN_VALUE) xSeekBar.setProgress(value);
        if (defValue != Integer.MIN_VALUE) {
            if (isStep) xSeekBar.setDefStepCount(calculateStepCount(defValue));
            else xSeekBar.setDefValue(defValue);

            xSeekBar.setShowDefaultPoint(
                isShowDefaultPoint && (
                    isStep ?
                        value != calculateStepCount(defValue) :
                        value != defValue
                )
            );
        }
        updateTipIfNeed();
    }

    @Override
    void updateVisibility() {
        super.updateVisibility();
        if (isShowValueOnTip) getTipView().setVisibility(VISIBLE);
    }

    private void updateTipIfNeed() {
        if (isShowValueOnTip) {
            String tip = calculateTipValue();
            tip = tip + (format != null ? format : "");
            getTipView().setText(tip);
        }
    }

    @Override
    public boolean performClick() {
        if (isDialogModeEnabled) {
            if (!isShowing) {
                new MiuixAlertDialog(getContext())
                    .setTitle(getTitle())
                    .setMessage(getSummary())
                    .setCustomView(xEditText)
                    .setOnBindViewListener(new MiuixDialogInterface.OnBindViewListener() {
                        @Override
                        public void onBindView(@NonNull ViewGroup root, @NonNull View view) {
                            xEditText.setText(calculateTipValue());
                        }
                    })
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setHapticFeedbackEnabled(true)
                    .setNegativeButton(getContext().getString(R.string.dialog_negative), null)
                    .setPositiveButton(getContext().getString(R.string.dialog_positive), new MiuixDialogInterface.OnClickListener() {
                        @Override
                        public void onClick(MiuixDialogInterface dialog, int which) {
                            float f = Float.MIN_VALUE;
                            String value = Optional.ofNullable(xEditText.getText()).orElse("").toString();
                            if (value.isEmpty()) value = String.valueOf(defValue);

                            if (dividerValue != Integer.MIN_VALUE)
                                f = Float.parseFloat(value) * dividerValue;
                            int finalValue = Integer.parseInt(f != Float.MIN_VALUE ? String.valueOf((int) f) : value);
                            xSeekBar.setShowDefaultPoint(isShowDefaultPoint && (isStep ?
                                MiuixSeekBarView.this.value != calculateStepCount(defValue) :
                                MiuixSeekBarView.this.value != defValue));
                            setValue(isStep ? calculateStepCount(finalValue) : finalValue);
                        }
                    })
                    .setOnShowListener(dialog -> isShowing = true)
                    .setOnDismissListener(dialog -> {
                        isShowing = false;
                        getShadowHelper().restoreOriginalColor();
                    })
                    .show();
            }
        }
        return super.performClick();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isEnabled() && isDialogModeEnabled && ev.getAction() == MotionEvent.ACTION_UP) {
            getShadowHelper().setKeepShadow();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setValue(int value) {
        if (isStep) {
            if (value < 0) value = 0;
            if (value > stepCount) value = stepCount;
        } else {
            if (minValue != Integer.MIN_VALUE && value < minValue) value = minValue;
            if (maxValue != Integer.MIN_VALUE && value > maxValue) value = maxValue;
        }

        this.value = value;
        refreshView();
    }

    public void setDefValue(int defValue) {
        this.defValue = defValue;
        refreshView();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        isStep = false;
        refreshView();
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        isStep = false;
        refreshView();
    }

    public void setStepValue(int stepValue) {
        this.stepValue = stepValue;
        isStep = false;
        refreshView();
    }

    public void setDividerValue(int dividerValue) {
        this.dividerValue = dividerValue;
        refreshView();
    }

    public void setFormat(String format) {
        this.format = format;
        refreshView();
    }

    public void setShowValueOnTip(boolean showValueOnTip) {
        this.isShowValueOnTip = showValueOnTip;
        refreshView();
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        this.listener = listener;
        refreshView();
    }

    public void setDialogModeEnabled(boolean enable) {
        this.isDialogModeEnabled = enable;
        refreshView();
    }

    public void setShowDefaultPoint(boolean show) {
        this.isShowDefaultPoint = show;
        refreshView();
    }

    public void setAlwaysHapticFeedback(boolean enable) {
        this.isAlwaysHapticFeedback = enable;
        refreshView();
    }

    public int getValue() {
        return isStep ? calculateOriginalValue(value) : value;
    }

    public int getDefValue() {
        return defValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getStepValue() {
        return stepValue;
    }

    public int getDividerValue() {
        return dividerValue;
    }

    public String getFormat() {
        return format;
    }

    public boolean isShowValueOnTip() {
        return isShowValueOnTip;
    }

    public boolean isDialogModeEnabled() {
        return isDialogModeEnabled;
    }

    public boolean isShowDefaultPoint() {
        return isShowDefaultPoint;
    }

    public boolean isAlwaysHapticFeedback() {
        return isAlwaysHapticFeedback;
    }

    // 初始化
    // 当启用跨步功能时会自动计算步数
    // 并通过步数设置 Progress
    // 最后通过步数等方法还原值
    private void initStepData() {
        if (isStep) return;

        if (stepValue != Integer.MIN_VALUE && maxValue != Integer.MIN_VALUE && minValue != Integer.MIN_VALUE) {
            // 计算步数
            stepCount = Math.round((float) (maxValue - minValue) / stepValue);
            if (stepCount * stepValue != maxValue - minValue) {
                maxValue = minValue + (stepCount * stepValue);
            }
            value = (value - minValue) / stepCount;
            isStep = true;
        }
    }

    // 计算步数
    // 通过给定原始值，计算出需要的步数
    private int calculateStepCount(int value) {
        return (value - minValue) / stepValue;
    }

    // 计算原始值
    // 通过步数和跨步值计算原值
    private int calculateOriginalValue(int stepCount) {
        return minValue + (stepValue * stepCount);
    }

    private String calculateTipValue() {
        return dividerValue != Integer.MIN_VALUE ?
            (
                getValue() == Integer.MIN_VALUE ?
                    String.valueOf(((float) getMinValue() / dividerValue)) :
                    String.valueOf(((float) getValue() / dividerValue))
            )
            :
            (
                getValue() == Integer.MIN_VALUE ?
                    String.valueOf(getMinValue()) :
                    String.valueOf(getValue())
            );
    }
}
