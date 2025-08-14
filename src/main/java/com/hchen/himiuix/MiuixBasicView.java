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

import static android.view.Gravity.CENTER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hchen.himiuix.callback.OnRefreshViewListener;
import com.hchen.himiuix.helper.ShadowHelper;

import java.util.Objects;

/**
 * Miuix 基本布局
 *
 * @author 焕晨HChen
 */
public class MiuixBasicView extends LinearLayout {
    static final String TAG = "HiMiuix";
    private View marginView;
    private CardView cardView;
    private ImageView iconView;
    private TextView titleView;
    private TextView summaryView;
    private TextView tipView;
    private ViewStub dynamicViewStub;
    private View indicatorView;
    private LinearLayout customLayout;
    private CharSequence title;
    private CharSequence summary;
    private CharSequence tip;
    private Intent intent;
    private Drawable icon;
    private Drawable indicator;
    private View customView;
    private int background;
    private int iconRadius;
    private boolean isEnabled;
    private boolean isAdded;
    private boolean isHapticFeedbackEnabled;
    private boolean isShadowEnabled;
    private boolean isManuallyRefreshView;
    private ShadowHelper shadowHelper;
    private OnRefreshViewListener listener;

    enum DynamicIndicator {
        INDICATOR_CUSTOM,
        INDICATOR_SWITCH,
        INDICATOR_CHECKBOX,
        INDICATOR_RADIO_BUTTON,
        INDICATOR_COLOR
    }

    public MiuixBasicView(@NonNull Context context) {
        this(context, null);
    }

    public MiuixBasicView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixBasicView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.MiuixBasicStyle);
    }

    public MiuixBasicView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    @CallSuper
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixBasicView, defStyleAttr, defStyleRes);
        tip = typedArray.getText(R.styleable.MiuixBasicView_tip);
        title = typedArray.getText(R.styleable.MiuixBasicView_android_title);
        summary = typedArray.getText(R.styleable.MiuixBasicView_android_summary);
        icon = typedArray.getDrawable(R.styleable.MiuixBasicView_android_icon);
        iconRadius = typedArray.getDimensionPixelSize(R.styleable.MiuixBasicView_iconRadius, -1);
        background = typedArray.getResourceId(R.styleable.MiuixBasicView_android_background, 0);
        isEnabled = typedArray.getBoolean(R.styleable.MiuixBasicView_android_enabled, true);
        isShadowEnabled = typedArray.getBoolean(R.styleable.MiuixBasicView_shadowEnabled, true);
        isHapticFeedbackEnabled = typedArray.getBoolean(R.styleable.MiuixBasicView_android_hapticFeedbackEnabled, true);
        indicator = typedArray.getDrawable(R.styleable.MiuixBasicView_indicator);
        if (indicator == null)
            indicator = AppCompatResources.getDrawable(getContext(), R.drawable.miuix_indicator_arrow_right);
        int customId = typedArray.getResourceId(R.styleable.MiuixBasicView_android_layout, 0);
        if (customId != 0) customView = LayoutInflater.from(getContext()).inflate(customId, null);
        typedArray.recycle();

        createLayout();
        loadViewWhenBuild();
        loadShadowHelper();
        updateViewContent();
        updateVisibility();
        setEnabled(isEnabled);
        setHapticFeedbackEnabled(isHapticFeedbackEnabled);
    }

    // 加载基本布局
    // 这是本组件的核心布局
    void createLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.miuix_layout, this, true);
        setOrientation(VERTICAL);
        setGravity(CENTER);
    }

    // 加载阴影动画帮助程序
    // 此程序提供点击阴影效果
    @CallSuper
    void loadShadowHelper() {
        shadowHelper = ShadowHelper.init(this);
        shadowHelper.setShadowEnabled(isShadowEnabled);
    }

    // 加载布局
    // 仅在构建时调用一次
    @CallSuper
    void loadViewWhenBuild() {
        marginView = findViewById(R.id.miuix_margin_view);
        cardView = findViewById(R.id.miuix_icon_card);
        iconView = findViewById(R.id.miuix_icon);
        titleView = findViewById(R.id.miuix_title);
        summaryView = findViewById(R.id.miuix_summary);
        tipView = findViewById(R.id.miuix_tip);
        dynamicViewStub = findViewById(R.id.miuix_dynamic_indicator_stub);
        customLayout = findViewById(R.id.miuix_custom_view);

        if (background != 0) setBackgroundResource(background);
        else setBackgroundResource(R.color.miuix_basic_background_color);

        switch (loadDynamicIndicator()) {
            case INDICATOR_CUSTOM -> {
                dynamicViewStub.setInflatedId(R.id.miuix_custom_indicator);
                dynamicViewStub.setLayoutResource(R.layout.miuix_custom_indicator);
            }
            case INDICATOR_SWITCH -> {
                dynamicViewStub.setInflatedId(R.id.miuix_switch_indicator);
                dynamicViewStub.setLayoutResource(R.layout.miuix_switch_indicator);
            }
            case INDICATOR_CHECKBOX -> {
                dynamicViewStub.setInflatedId(R.id.miuix_checkbox_indicator);
                dynamicViewStub.setLayoutResource(R.layout.miuix_checkbox_indicator);
            }
            case INDICATOR_RADIO_BUTTON -> {
                dynamicViewStub.setInflatedId(R.id.miuix_radio_button_indicator);
                dynamicViewStub.setLayoutResource(R.layout.miuix_radio_button_indicator);
            }
            case INDICATOR_COLOR -> {
                dynamicViewStub.setInflatedId(R.id.miuix_color_indicator);
                dynamicViewStub.setLayoutResource(R.layout.miuix_color_indicator);
            }
        }
        // 每个指示器布局最多包含一个视图
        indicatorView = ((LinearLayout) dynamicViewStub.inflate()).getChildAt(0);
    }

    DynamicIndicator loadDynamicIndicator() {
        return DynamicIndicator.INDICATOR_CUSTOM;
    }

    // 更新布局内容
    // 将在调用 refreshView 后被执行
    // 请不要在这里调用会触发 refreshView 的操作，会导致死循环
    @CallSuper
    void updateViewContent() {
        if (iconRadius != -1) cardView.setRadius(iconRadius);
        if (shadowHelper != null) shadowHelper.setShadowEnabled(isShadowEnabled);
        if (icon != null) iconView.setImageDrawable(icon);
        if (title != null && !Objects.equals(titleView.getText(), title)) titleView.setText(title);
        if (summary != null && !Objects.equals(summaryView.getText(), summary))
            summaryView.setText(summary);
        if (tip != null && !Objects.equals(titleView.getText(), tip)) tipView.setText(tip);
        if (indicator != null && indicatorView instanceof ImageView imageView)
            imageView.setImageDrawable(indicator);
        if (customView != null) {
            if (!isAdded) {
                addView(customLayout, customView);
                isAdded = true;
            }
        }
    }

    // 更新布局可见性
    // 将在调用 refreshView 后被执行
    // 请不要在这里调用会触发 refreshView 的操作，会导致死循环
    @CallSuper
    void updateVisibility() {
        if (icon != null) cardView.setVisibility(VISIBLE);
        else cardView.setVisibility(GONE);
        if (title != null) titleView.setVisibility(VISIBLE);
        else titleView.setVisibility(GONE);
        if (summary != null) summaryView.setVisibility(VISIBLE);
        else summaryView.setVisibility(GONE);
        if (tip != null) tipView.setVisibility(VISIBLE);
        else tipView.setVisibility(GONE);
        if (indicatorView instanceof ImageView) {
            if (intent != null || hasOnClickListeners() || forceShowCustomIndicatorView())
                indicatorView.setVisibility(VISIBLE);
            else indicatorView.setVisibility(GONE);
        } else indicatorView.setVisibility(GONE);
        if (customView != null) {
            if (icon != null || title != null || summary != null || tip != null ||
                indicatorView.getVisibility() == VISIBLE)
                marginView.setVisibility(VISIBLE);
            else marginView.setVisibility(GONE);
            customLayout.setVisibility(VISIBLE);
        } else {
            marginView.setVisibility(GONE);
            customLayout.setVisibility(GONE);
        }
    }

    // 是否无视判断强制显示指示器
    public boolean forceShowCustomIndicatorView() {
        return false;
    }

    // 刷新布局
    public final void refreshView() {
        if (isManuallyRefreshView)
            return;

        updateViewContent();
        updateVisibility();
        if (listener != null)
            listener.refreshed(this);
        // invalidate();
    }

    public void setTitle(@StringRes int id) {
        setTitle(getContext().getText(id));
    }

    public void setTitle(CharSequence title) {
        if (Objects.equals(this.title, title)) return;
        this.title = title;
        refreshView();
    }

    public void setSummary(@StringRes int id) {
        setSummary(getContext().getText(id));
    }

    public void setSummary(CharSequence summary) {
        if (Objects.equals(this.summary, summary)) return;
        this.summary = summary;
        refreshView();
    }

    public void setTip(@StringRes int id) {
        setTip(getContext().getText(id));
    }

    public void setTip(CharSequence tip) {
        if (Objects.equals(this.tip, tip)) return;
        this.tip = tip;
        refreshView();
    }

    public void setIcon(@DrawableRes int id) {
        setIcon(ContextCompat.getDrawable(getContext(), id));
    }

    public void setIcon(Drawable icon) {
        if (Objects.equals(this.icon, icon)) return;
        this.icon = icon;
        refreshView();
    }

    public void setIndicator(@DrawableRes int id) {
        setIndicator(ContextCompat.getDrawable(getContext(), id));
    }

    public void setIndicator(Drawable indicator) {
        if (Objects.equals(this.indicator, indicator)) return;
        this.indicator = indicator;
        refreshView();
    }

    public void setCustomView(@LayoutRes int id) {
        setCustomView(LayoutInflater.from(getContext()).inflate(id, null));
    }

    public void setCustomView(View customView) {
        if (Objects.equals(this.customView, customView)) return;
        if (this.customView != null && !Objects.equals(this.customView, customView)) {
            removeView(customLayout, this.customView);
            isAdded = false;
        }

        this.customView = customView;
        refreshView();
    }

    public void setIntent(@Nullable Intent intent) {
        if (Objects.equals(this.intent, intent)) return;
        this.intent = intent;
        refreshView();
    }

    public void setIconRadius(int iconRadius) {
        if (this.iconRadius == iconRadius) return;
        this.iconRadius = iconRadius;
        refreshView();
    }

    public void setOnRefreshViewListener(OnRefreshViewListener listener) {
        if (Objects.equals(this.listener, listener)) return;
        this.listener = listener;
        refreshView();
    }

    // 是否启用阴影动画
    public void setShadowEnabled(boolean enabled) {
        if (isShadowEnabled == enabled) return;
        isShadowEnabled = enabled;
        refreshView();
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getSummary() {
        return summary;
    }

    public CharSequence getTip() {
        return tip;
    }

    public Intent getIntent() {
        return intent;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Drawable getIndicator() {
        return indicator;
    }

    public View getCustomView() {
        return customView;
    }

    public int getIconRadius() {
        return iconRadius;
    }

    public boolean isShadowEnabled() {
        return isShadowEnabled;
    }

    // ------------------ View ---------------------
    @NonNull
    public ImageView getIconView() {
        return iconView;
    }

    @NonNull
    public TextView getTitleView() {
        return titleView;
    }

    @NonNull
    public TextView getSummaryView() {
        return summaryView;
    }

    @NonNull
    public TextView getTipView() {
        return tipView;
    }

    @NonNull
    public View getIndicatorView() {
        return indicatorView;
    }

    // ----------------------------------------------
    @NonNull
    ShadowHelper getShadowHelper() {
        return shadowHelper;
    }

    void setShadowHelperEnabled(boolean enabled) {
        shadowHelper.setEnabled(enabled);
    }

    // 跳过自动执行 refreshView，需要你手动调用 refreshView
    public void setManuallyRefreshViewMode(boolean enabled) {
        isManuallyRefreshView = enabled;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        refreshView();
    }

    @Override
    @CallSuper
    public boolean performClick() {
        if (intent != null) getContext().startActivity(intent);
        return super.performClick();
    }

    @Override
    @CallSuper
    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            isEnabled = enabled;
            super.setEnabled(enabled);
            setEnabledInner(this, enabled);
        }
    }

    // 设置布局内所有组件 Enabled 状态
    private void setEnabledInner(ViewGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            if (view instanceof ViewGroup viewGroup) {
                viewGroup.setEnabled(enabled);
                // 对 RecyclerView 特殊处理
                if (viewGroup instanceof RecyclerView recyclerView) {
                    recyclerView.setLayoutFrozen(!enabled);
                    recyclerView.setOnTouchListener((v, event) -> !enabled);
                    recyclerView.post(() -> {
                        setEnabledInner(recyclerView, enabled);
                    });
                    return;
                }
                setEnabledInner(viewGroup, enabled);
            } else {
                // 使用 50% 透明度作为 Disabled 状态
                if (!enabled) view.setAlpha(0.5f);
                else view.setAlpha(1.0f);
                view.setEnabled(enabled);
            }
        }
    }

    @Override
    @CallSuper
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (shadowHelper != null)
            shadowHelper.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void setHapticFeedbackEnabled(boolean enabled) {
        if (isHapticFeedbackEnabled() != enabled) {
            isHapticFeedbackEnabled = enabled;
            super.setHapticFeedbackEnabled(enabled);
            setHapticFeedbackEnabledInner(this, enabled);
        }
    }

    // 设置布局内组件 HapticFeedback 状态
    private void setHapticFeedbackEnabledInner(ViewGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            if (view instanceof ViewGroup viewGroup) {
                viewGroup.setHapticFeedbackEnabled(enabled);
                setHapticFeedbackEnabledInner(viewGroup, enabled);
            } else {
                view.setHapticFeedbackEnabled(enabled);
            }
        }
    }

    void addView(@NonNull ViewGroup viewGroup, @NonNull View view) {
        ViewGroup group = (ViewGroup) view.getParent();
        if (group != viewGroup) {
            if (group != null) group.removeView(view);
            viewGroup.addView(view);
        }
    }

    void removeView(@NonNull ViewGroup group, @NonNull View view) {
        group.removeView(view);
    }
}
