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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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
    private CardView cardView;
    private ImageView iconView;
    private TextView titleView;
    private TextView summaryView;
    private TextView tipView;
    private ImageView indicatorView;
    private LinearLayout customLayout;
    private CharSequence title;
    private CharSequence summary;
    private CharSequence tip;
    private Intent intent;
    private Drawable icon;
    private Drawable indicator;
    private View customView;
    private int customId;
    private int background;
    private int iconRadius;
    private boolean enabled;
    private boolean isAdded;
    private boolean isHapticFeedbackEnabled;
    private boolean isShadowEnabled;
    private boolean isManuallyRefreshView;
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
        tip = typedArray.getText(R.styleable.MiuixBasicView_tip);
        title = typedArray.getText(R.styleable.MiuixBasicView_android_title);
        summary = typedArray.getText(R.styleable.MiuixBasicView_android_summary);
        icon = typedArray.getDrawable(R.styleable.MiuixBasicView_android_icon);
        indicator = typedArray.getDrawable(R.styleable.MiuixBasicView_indicator);
        iconRadius = typedArray.getDimensionPixelSize(R.styleable.MiuixBasicView_iconRadius, -1);
        background = typedArray.getResourceId(R.styleable.MiuixBasicView_android_background, 0);
        customId = typedArray.getResourceId(R.styleable.MiuixBasicView_android_layout, 0);
        enabled = typedArray.getBoolean(R.styleable.MiuixBasicView_android_enabled, true);
        isShadowEnabled = typedArray.getBoolean(R.styleable.MiuixBasicView_shadowEnabled, true);
        isHapticFeedbackEnabled = typedArray.getBoolean(R.styleable.MiuixBasicView_android_hapticFeedbackEnabled, true);
        typedArray.recycle();

        createLayout();
        loadViewWhenBuild();
        loadShadowHelper();
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
        this.enabled = enabled;
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
        cardView = findViewById(R.id.miuix_icon_card);
        iconView = findViewById(R.id.miuix_icon);
        titleView = findViewById(R.id.miuix_title);
        summaryView = findViewById(R.id.miuix_summary);
        tipView = findViewById(R.id.miuix_tip);
        indicatorView = findViewById(R.id.miuix_custom_indicator);
        customLayout = findViewById(R.id.miuix_custom);

        if (background != 0) setBackgroundResource(background);
        else setBackgroundResource(R.color.miuix_basic_background_color);
        if (customId != 0) customView = LayoutInflater.from(getContext()).inflate(customId, null);
    }

    /**
     * 更新布局内容
     */
    @CallSuper
    void updateViewContent() {
        if (iconRadius != -1) cardView.setRadius(iconRadius);
        if (shadowHelper != null) shadowHelper.setShadowEnabled(isShadowEnabled);
        if (icon != null) iconView.setImageDrawable(icon);
        if (title != null) titleView.setText(title);
        if (summary != null) summaryView.setText(summary);
        if (tip != null) tipView.setText(tip);
        if (indicator != null) indicatorView.setImageDrawable(indicator);
        if (customView != null) {
            if (!isAdded) {
                if (icon != null || title != null || summary != null ||
                    tip != null || indicator != null || forceShowCustomIndicatorView()) {
                    LinearLayout.LayoutParams params = (LayoutParams) customLayout.getLayoutParams();
                    params.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.miuix_basic_margin);
                    customLayout.setLayoutParams(params);
                }
                addView(customLayout, customView);
            }
            isAdded = true;
        }
    }

    /**
     * 更新组件可见性
     */
    @CallSuper
    void updateVisibility() {
        if (icon == null) {
            cardView.setVisibility(GONE);
            iconView.setVisibility(GONE);
        } else {
            cardView.setVisibility(VISIBLE);
            iconView.setVisibility(VISIBLE);
        }

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
        if (isManuallyRefreshView)
            return;

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
        setTitle(getContext().getText(title));
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        refreshView();
    }

    public void setSummary(@StringRes int summary) {
        setSummary(getContext().getText(summary));
    }

    public void setSummary(CharSequence summary) {
        this.summary = summary;
        refreshView();
    }

    public void setTip(@StringRes int tip) {
        setTip(getContext().getText(tip));
    }

    public void setTip(CharSequence tip) {
        this.tip = tip;
        refreshView();
    }

    public void setIcon(@DrawableRes int icon) {
        setIcon(ContextCompat.getDrawable(getContext(), icon));
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
        refreshView();
    }

    public void setIndicator(@DrawableRes int indicator) {
        setIndicator(ContextCompat.getDrawable(getContext(), indicator));
    }

    public void setIndicator(Drawable indicator) {
        this.indicator = indicator;
        refreshView();
    }

    public void setCustomView(View customView) {
        if (Objects.equals(this.customView, customView))
            return;
        if (this.customView != null && !Objects.equals(this.customView, customView)) {
            removeView(customLayout, this.customView);
            isAdded = false;
        }

        this.customView = customView;
        refreshView();
    }

    public void setIntent(@Nullable Intent intent) {
        this.intent = intent;
        refreshView();
    }

    public void setIconRadius(int iconRadius) {
        this.iconRadius = iconRadius;
    }

    @Override
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

    @Override
    public boolean isEnabled() {
        return enabled;
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
    ShadowHelper getShadowHelper() {
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

    public void setManuallyRefreshViewMode(boolean enabled) {
        isManuallyRefreshView = enabled;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        refreshView();
    }
}
