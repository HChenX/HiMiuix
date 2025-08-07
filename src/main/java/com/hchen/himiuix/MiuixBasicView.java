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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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
    private ImageView iconView;
    private TextView titleView;
    private TextView summaryView;
    private TextView tipView;
    private ImageView indicatorView;
    private LinearLayout customLayout;
    private String title;
    private String summary;
    private String tip;
    private Intent intent;
    private Drawable icon;
    private Drawable indicator;
    private View customView;
    private boolean enabled;
    private boolean isAdded;
    private boolean isHapticFeedbackEnabled;
    private boolean isShadowEnabled;
    private ShadowHelper shadowHelper;
    private OnRefreshViewListener onRefreshViewListener;

    public MiuixBasicView(@NonNull Context context) {
        this(context, null);
    }

    public MiuixBasicView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixBasicView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixBasicView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    @CallSuper
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixBasicView, defStyleAttr, defStyleRes);
        tip = typedArray.getString(R.styleable.MiuixBasicView_tip);
        title = typedArray.getString(R.styleable.MiuixBasicView_android_title);
        summary = typedArray.getString(R.styleable.MiuixBasicView_android_summary);
        icon = typedArray.getDrawable(R.styleable.MiuixBasicView_android_icon);
        indicator = typedArray.getDrawable(R.styleable.MiuixBasicView_indicator);
        enabled = typedArray.getBoolean(R.styleable.MiuixBasicView_android_enabled, true);
        isShadowEnabled = typedArray.getBoolean(R.styleable.MiuixBasicView_shadowEnabled, true);
        isHapticFeedbackEnabled = typedArray.getBoolean(R.styleable.MiuixBasicView_android_hapticFeedbackEnabled, true);
        typedArray.recycle();

        createLayout();
        loadShadowHelper();
        loadViewWhenBuild();
        updateViewContent();
        updateVisibility();
        setEnabled(enabled);
        setHapticFeedbackEnabled(isHapticFeedbackEnabled);
    }

    void createLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.miuix_layout, this, true);
    }

    @CallSuper
    void loadShadowHelper() {
        shadowHelper = ShadowHelper.init(this);
        shadowHelper.setShadowEnabled(isShadowEnabled);
    }

    @Override
    @CallSuper
    public void setEnabled(boolean enabled) {
        setEnabledInner(this, enabled);
        super.setEnabled(enabled);
    }

    private void setEnabledInner(ViewGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            if (view instanceof ViewGroup viewGroup) {
                viewGroup.setEnabled(enabled);
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
                else view.setAlpha(1f);
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

    /**
     * 加载基本布局
     * <p>
     * 仅在构建布局时调用一次
     */
    @CallSuper
    void loadViewWhenBuild() {
        iconView = findViewById(R.id.miuix_icon);
        titleView = findViewById(R.id.miuix_title);
        summaryView = findViewById(R.id.miuix_summary);
        tipView = findViewById(R.id.miuix_tip);
        indicatorView = findViewById(R.id.miuix_custom_indicator);
        customLayout = findViewById(R.id.miuix_custom);
    }

    /**
     * 更新布局内容
     */
    @CallSuper
    void updateViewContent() {
        if (icon != null) iconView.setImageDrawable(icon);
        if (title != null) titleView.setText(title);
        if (summary != null) summaryView.setText(summary);
        if (tip != null) tipView.setText(tip);
        if (indicator != null) indicatorView.setImageDrawable(indicator);
        if (customView != null) {
            if (!isAdded) addView(customLayout, customView);
            isAdded = true;
        }
    }

    /**
     * 更新组件可见性
     */
    @CallSuper
    void updateVisibility() {
        if (icon == null) iconView.setVisibility(GONE);
        else iconView.setVisibility(VISIBLE);

        if (title == null) titleView.setVisibility(GONE);
        else titleView.setVisibility(VISIBLE);

        if (summary == null) summaryView.setVisibility(GONE);
        else summaryView.setVisibility(VISIBLE);

        if (tip == null) tipView.setVisibility(GONE);
        else tipView.setVisibility(VISIBLE);

        if (canShowCustomIndicatorView() || forceShowCustomIndicatorView()) {
            if ((intent == null && !hasOnClickListeners()) && !forceShowCustomIndicatorView())
                indicatorView.setVisibility(GONE);
            else indicatorView.setVisibility(VISIBLE);
        } else indicatorView.setVisibility(GONE);

        if (customView == null) customLayout.setVisibility(GONE);
        else customLayout.setVisibility(VISIBLE);
    }

    /**
     * 是否展示自定义指示器
     */
    boolean canShowCustomIndicatorView() {
        return true;
    }

    /**
     * 是否强制展示自定义指示器
     */
    boolean forceShowCustomIndicatorView() {
        return false;
    }

    public final void refreshView() {
        updateViewContent();
        updateVisibility();
        if (onRefreshViewListener != null)
            onRefreshViewListener.refreshed(this);

        // invalidate();
    }

    @Override
    @CallSuper
    public boolean performClick() {
        if (intent != null) getContext().startActivity(intent);
        return super.performClick();
    }

    public void setTitle(@StringRes int title) {
        setTitle(getContext().getString(title));
    }

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        this.title = title;
        refreshView();
    }

    public void setSummary(@StringRes int summary) {
        setSummary(getContext().getString(summary));
    }

    /**
     * 设置摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
        refreshView();
    }

    public void setTip(@StringRes int tip) {
        setTip(getContext().getString(tip));
    }

    /**
     * 设置 Tip
     */
    public void setTip(String tip) {
        this.tip = tip;
        refreshView();
    }

    public void setIcon(@DrawableRes int icon) {
        setIcon(ContextCompat.getDrawable(getContext(), icon));
    }

    /**
     * 设置图标
     */
    public void setIcon(Drawable icon) {
        this.icon = icon;
        refreshView();
    }

    public void setIndicator(@DrawableRes int indicator) {
        setIndicator(ContextCompat.getDrawable(getContext(), indicator));
    }

    /**
     * 设置指示器
     */
    public void setIndicator(Drawable indicator) {
        this.indicator = indicator;
        refreshView();
    }

    public void setCustomView(@LayoutRes int customView) {
        setCustomView(LayoutInflater.from(getContext()).inflate(customView, null));
    }

    /**
     * 设置自定义视图
     */
    public void setCustomView(View customView) {
        if (this.customView != null &&
            (customView == null || !Objects.equals(this.customView, customView))) {
            removeView(customLayout, this.customView);
            isAdded = false;
        }
        this.customView = customView;
        refreshView();
    }

    /**
     * 设置意图
     */
    public void setIntent(@Nullable Intent intent) {
        this.intent = intent;
        refreshView();
    }

    /**
     * 是否启用震动反馈
     *
     * @param enabled whether haptic feedback enabled for this view.
     */
    public void setHapticFeedbackEnabled(boolean enabled) {
        this.isHapticFeedbackEnabled = enabled;
        setHapticFeedbackEnabledInner(this, enabled);
        super.setHapticFeedbackEnabled(enabled);
    }

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

    public void setOnRefreshViewListener(OnRefreshViewListener listener) {
        this.onRefreshViewListener = listener;
        refreshView();
    }

    public void setShadowEnabled(boolean shadowEnabled) {
        isShadowEnabled = shadowEnabled;
        refreshView();
    }

    public boolean isShadowEnabled() {
        return isShadowEnabled;
    }

    @Override
    public boolean isHapticFeedbackEnabled() {
        return isHapticFeedbackEnabled;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getSummary() {
        return summary;
    }

    @Nullable
    public String getTip() {
        return tip;
    }

    @Nullable
    public Intent getIntent() {
        return intent;
    }

    @Nullable
    public Drawable getIcon() {
        return icon;
    }

    @Nullable
    public Drawable getIndicator() {
        return indicator;
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
    public ImageView getIndicatorView() {
        return indicatorView;
    }

    // ----------------------------------------------

    @NonNull
    public ShadowHelper getShadowHelper() {
        return shadowHelper;
    }

    void setShadowHelperEnabled(boolean enabled) {
        shadowHelper.setEnabled(enabled);
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

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        refreshView();
    }
}
