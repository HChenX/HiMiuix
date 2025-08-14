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
package com.hchen.himiuix.preference;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.CallSuper;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.hchen.himiuix.MiuixBasicView;
import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnRefreshViewListener;
import com.hchen.himiuix.utils.InvokeUtils;
import com.hchen.himiuix.widget.MiuixCardView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Preference
 *
 * @author 焕晨HChen
 */
public class MiuixPreference extends Preference implements OnRefreshViewListener {
    static final String TAG = "HiMiuix:Preference";
    static final int CARD_RADIUS = 0;
    static final int CARD_TOP_RADIUS = 1;
    static final int CARD_BOTTOM_RADIUS = 2;
    static final int CARD_NON_RADIUS = 3;
    private int cardState = CARD_RADIUS;
    private int radius;
    private CharSequence tip;
    private Drawable indicator;
    private View customView;
    private int iconRadius;
    private boolean isShadowEnabled;
    private boolean isHapticFeedbackEnabled;
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        @SuppressLint("RestrictedApi")
        public void onClick(View v) {
            performClick(v);
        }
    };

    public MiuixPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuixPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    @CallSuper
    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixPreference, defStyleAttr, defStyleRes);
        tip = typedArray.getText(R.styleable.MiuixPreference_tip);
        indicator = typedArray.getDrawable(R.styleable.MiuixPreference_indicator);
        iconRadius = typedArray.getDimensionPixelSize(R.styleable.MiuixPreference_iconRadius, -1);
        isShadowEnabled = typedArray.getBoolean(R.styleable.MiuixPreference_shadowEnabled, true);
        isHapticFeedbackEnabled = typedArray.getBoolean(R.styleable.MiuixPreference_android_hapticFeedbackEnabled, true);
        int layout = typedArray.getResourceId(R.styleable.MiuixPreference_android_layout, 0);
        if (layout != 0) customView = LayoutInflater.from(getContext()).inflate(layout, null);
        typedArray.recycle();

        setLayoutResource(loadLayoutResource());
        radius = getContext().getResources().getDimensionPixelSize(R.dimen.miuix_prefs_card_radius);
    }

    @LayoutRes
    int loadLayoutResource() {
        return R.layout.miuix_preference;
    }

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        MiuixCardView xCardView = (MiuixCardView) holder.itemView;
        MiuixBasicView xBasicView = holder.itemView.findViewById(R.id.miuix_prefs);

        // 取消自动刷新
        xBasicView.setManuallyRefreshViewMode(true);

        xBasicView.setOnRefreshViewListener(null);
        xBasicView.setOnClickListener(null);

        updateCardView(xCardView, cardState);
        xBasicView.setTip(tip);
        xBasicView.setTitle(getTitle());
        xBasicView.setSummary(getSummary());
        xBasicView.setIcon(getIcon());
        xBasicView.setIconRadius(iconRadius);
        xBasicView.setIndicator(indicator);
        if (canSetCustomView())
            xBasicView.setCustomView(customView);
        // 不要设置 BasicView 的 Intent，可能会执行两次
        // xBasicView.setIntent(getIntent());
        xBasicView.setEnabled(isEnabled());
        xBasicView.setShadowEnabled(isShadowEnabled);
        xBasicView.setHapticFeedbackEnabled(isHapticFeedbackEnabled);
        xBasicView.setOnRefreshViewListener(this);
        xBasicView.setOnClickListener(onClickListener);

        xBasicView.setManuallyRefreshViewMode(false);
        xBasicView.refreshView();
    }

    boolean canSetCustomView() {
        return true;
    }

    // 在视图刷新后回调
    @Override
    public void refreshed(MiuixBasicView view) {
        if (view.getIndicatorView() instanceof ImageView) {
            if (getFragment() != null || getIntent() != null ||
                getOnPreferenceChangeListener() != null ||
                getOnPreferenceClickListener() != null ||
                view.forceShowCustomIndicatorView()
            ) {
                view.getIndicatorView().setVisibility(VISIBLE);
            } else view.getIndicatorView().setVisibility(GONE);
        }
    }

    public void setTip(@StringRes int id) {
        setTip(getContext().getText(id));
    }

    public void setTip(@Nullable CharSequence tip) {
        if (Objects.equals(this.tip, tip)) return;
        this.tip = tip;
        notifyChanged();
    }

    public void setIndicator(@DrawableRes int id) {
        setIndicator(ContextCompat.getDrawable(getContext(), id));
    }

    public void setIndicator(@Nullable Drawable indicator) {
        if (Objects.equals(this.indicator, indicator)) return;
        this.indicator = indicator;
        notifyChanged();
    }

    public void setHapticFeedbackEnabled(boolean enabled) {
        if (isHapticFeedbackEnabled == enabled) return;
        isHapticFeedbackEnabled = enabled;
        notifyChanged();
    }

    public void setShadowEnabled(boolean enabled) {
        if (isShadowEnabled == enabled) return;
        isShadowEnabled = enabled;
        notifyChanged();
    }

    public void setIconRadius(int iconRadius) {
        if (this.iconRadius == iconRadius) return;
        this.iconRadius = iconRadius;
        notifyChanged();
    }

    public void setCustomView(View customView) {
        if (Objects.equals(this.customView, customView)) return;
        this.customView = customView;
        notifyChanged();
    }

    @Nullable
    public CharSequence getTip() {
        return tip;
    }

    @Nullable
    public Drawable getIndicator() {
        return indicator;
    }

    public int getIconRadius() {
        return iconRadius;
    }

    public View getCustomView() {
        return customView;
    }

    public boolean isShadowEnabled() {
        return isShadowEnabled;
    }

    public boolean isHapticFeedbackEnabled() {
        return isHapticFeedbackEnabled;
    }

    // ------------------------------ Inner Api -----------------------------------
    // 快速的设置 Card 状态
    void setCardState(int cardState) {
        this.cardState = cardState;
    }

    private void updateCardView(MiuixCardView xCardView, int state) {
        switch (state) {
            case CARD_RADIUS ->
                xCardView.setTrRadius(radius).setTlRadius(radius).setBlRadius(radius).setBrRadius(radius);
            case CARD_TOP_RADIUS ->
                xCardView.setTlRadius(radius).setTrRadius(radius).setBlRadius(0).setBrRadius(0);
            case CARD_BOTTOM_RADIUS ->
                xCardView.setBlRadius(radius).setBrRadius(radius).setTlRadius(0).setTrRadius(0);
            case CARD_NON_RADIUS ->
                xCardView.setTlRadius(0).setTrRadius(0).setBrRadius(0).setBlRadius(0);
        }
    }

    private String mDependencyKey;
    private final ArrayList<MiuixPreference> mDependents = new ArrayList<>();

    @Override
    public void onAttached() {
        registerDependency();
    }

    @Override
    public void onDetached() {
        unregisterDependency();
        InvokeUtils.setField(this, "mWasDetached", true);
    }

    @Override
    protected void onPrepareForRemoval() {
        unregisterDependency();
    }

    @Override
    public void setDependency(@Nullable String dependencyKey) {
        unregisterDependency();

        InvokeUtils.setField(this, "mDependencyKey", mDependencyKey);
        registerDependency();
    }

    @Override
    public void notifyDependencyChange(boolean disableDependents) {
        for (MiuixPreference xPreference : mDependents) {
            xPreference.setVisible(!shouldDisableDependents());
            xPreference.onDependencyChanged(this, disableDependents);
        }
    }

    private void registerDependency() {
        mDependencyKey = getDependency();
        if (mDependencyKey == null) return;
        MiuixPreference xPreference = findPreferenceInHierarchy(mDependencyKey);
        if (xPreference != null) {
            setVisible(!xPreference.shouldDisableDependents());
            onDependencyChanged(this, xPreference.shouldDisableDependents());
            xPreference.mDependents.add(this);
        } else {
            throw new IllegalStateException("Dependency \"" + mDependencyKey
                + "\" not found for preference \"" + getKey() + "\" (title: \"" + getTitle() + "\"");
        }
    }

    private void unregisterDependency() {
        mDependencyKey = getDependency();
        if (mDependencyKey == null) return;
        MiuixPreference xPreference = findPreferenceInHierarchy(mDependencyKey);
        if (xPreference != null) xPreference.mDependents.remove(this);
    }

    @Override
    protected void notifyChanged() {
        if (getContext() instanceof Activity activity) {
            // 防止冲突
            RecyclerView recyclerView = activity.findViewById(R.id.recycler_view);
            if (recyclerView != null && recyclerView.isComputingLayout())
                recyclerView.post(super::notifyChanged);
            else super.notifyChanged();
        } else super.notifyChanged();
    }

    // ------------------------------ UnSupport --------------------------------
    @Override
    public void setViewId(int viewId) {
        // UnSupport
    }

    @Override
    public void setWidgetLayoutResource(int widgetLayoutResId) {
        // UnSupport
    }

    @Override
    public void setSingleLineTitle(boolean singleLineTitle) {
        // UnSupport
    }

    @Override
    public void setIconSpaceReserved(boolean iconSpaceReserved) {
        // UnSupport
    }

    // setSummaryProvider // UnSupport
}
