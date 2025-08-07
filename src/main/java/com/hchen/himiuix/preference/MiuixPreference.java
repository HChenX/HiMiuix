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
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.hchen.himiuix.MiuixBasicView;
import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnRefreshViewListener;
import com.hchen.himiuix.utils.InvokeUtils;
import com.hchen.himiuix.widget.MiuixCardView;

import java.util.ArrayList;

/**
 * Preference
 *
 * @author 焕晨HChen
 */
public class MiuixPreference extends Preference implements OnRefreshViewListener {
    static final String TAG = "HiMiuix";
    static final int CARD_RADIUS = 0;
    static final int CARD_TOP_RADIUS = 1;
    static final int CARD_BOTTOM_RADIUS = 2;
    static final int CARD_NON_RADIUS = 3;
    private MiuixCardView xCardView;
    MiuixBasicView xBasicView;
    private int cardState = CARD_RADIUS;
    private int radius;
    private String tip;
    private Drawable indicator;
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
        tip = typedArray.getString(R.styleable.MiuixPreference_tip);
        indicator = typedArray.getDrawable(R.styleable.MiuixPreference_indicator);
        isShadowEnabled = typedArray.getBoolean(R.styleable.MiuixPreference_shadowEnabled, true);
        isHapticFeedbackEnabled = typedArray.getBoolean(R.styleable.MiuixPreference_android_hapticFeedbackEnabled, true);
        typedArray.recycle();

        radius = getContext().getResources().getDimensionPixelSize(R.dimen.miuix_prefs_card_radius);
        setLayoutResource(loadLayoutResource());
    }

    @LayoutRes
    int loadLayoutResource() {
        return R.layout.miuix_preference;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        xCardView = (MiuixCardView) holder.itemView;
        xBasicView = holder.itemView.findViewById(R.id.miuix_prefs);

        updateCardView(cardState);
        xBasicView.setTip(tip);
        xBasicView.setTitle((String) getTitle());
        xBasicView.setSummary((String) getSummary());
        xBasicView.setIcon(getIcon());
        xBasicView.setIndicator(indicator);
        // 不要设置 BasicView 的 Intent，可能会执行两次
        // xBasicView.setIntent(getIntent());
        xBasicView.setShadowEnabled(isShadowEnabled);
        xBasicView.setHapticFeedbackEnabled(isHapticFeedbackEnabled);
        xBasicView.setEnabled(isEnabled());
        xBasicView.setOnRefreshViewListener(this);
        xBasicView.setOnClickListener(onClickListener);
    }

    @Override
    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        if (getPreferenceManager() != null) {
            getPreferenceManager().setSharedPreferencesName(getContext().getString(R.string.prefs_name));
        }
    }

    @Override
    public void refreshed(MiuixBasicView view) {
        if (getFragment() != null || getIntent() != null ||
            getOnPreferenceChangeListener() != null ||
            getOnPreferenceClickListener() != null
        ) {
            xBasicView.getIndicatorView().setVisibility(VISIBLE);
        } else xBasicView.getIndicatorView().setVisibility(GONE);
    }

    public void setTip(@StringRes int tip) {
        setTip(getContext().getString(tip));
    }

    public void setTip(@Nullable String tip) {
        this.tip = tip;
        if (xBasicView != null)
            xBasicView.setTip(tip);
    }

    public void setIndicator(@DrawableRes int indicator) {
        setIndicator(ContextCompat.getDrawable(getContext(), indicator));
    }

    public void setIndicator(@Nullable Drawable indicator) {
        this.indicator = indicator;
        if (xBasicView != null)
            xBasicView.setIndicator(indicator);
    }

    public void setCustomView(@LayoutRes int customView) {
        setCustomView(LayoutInflater.from(getContext()).inflate(customView, null));
    }

    public void setCustomView(@Nullable View customView) {
        if (xBasicView != null)
            xBasicView.setCustomView(customView);
    }

    public void setHapticFeedbackEnabled(boolean enabled) {
        this.isHapticFeedbackEnabled = enabled;
        if (xBasicView != null)
            xBasicView.setHapticFeedbackEnabled(enabled);
    }

    public void setShadowEnabled(boolean enabled) {
        isShadowEnabled = enabled;
        if (xBasicView != null)
            xBasicView.setShadowEnabled(enabled);
    }

    public boolean isHapticFeedbackEnabled() {
        return xBasicView.isHapticFeedbackEnabled();
    }

    @Nullable
    public String getTip() {
        return xBasicView.getTip();
    }

    @Nullable
    public Drawable getIndicator() {
        return xBasicView.getIndicator();
    }

    // ------------------ View ---------------------

    @NonNull
    public ImageView getIconView() {
        return xBasicView.getIconView();
    }

    @NonNull
    public TextView getTitleView() {
        return xBasicView.getTitleView();
    }

    @NonNull
    public TextView getSummaryView() {
        return xBasicView.getSummaryView();
    }

    @NonNull
    public TextView getTipView() {
        return xBasicView.getTipView();
    }

    @NonNull
    public ImageView getIndicatorView() {
        return xBasicView.getIndicatorView();
    }

    // ---------------------------------------------------------------

    void setCardState(int cardState) {
        this.cardState = cardState;
    }

    private void updateCardView(int state) {
        switch (state) {
            case CARD_RADIUS ->
                xCardView.setTrRadius(radius).setTlRadius(radius).setBlRadius(radius).setBrRadius(radius).invalidate();
            case CARD_TOP_RADIUS ->
                xCardView.setTlRadius(radius).setTrRadius(radius).setBlRadius(0).setBrRadius(0).invalidate();
            case CARD_BOTTOM_RADIUS ->
                xCardView.setBlRadius(radius).setBrRadius(radius).setTlRadius(0).setTrRadius(0).invalidate();
            case CARD_NON_RADIUS ->
                xCardView.setTlRadius(0).setTrRadius(0).setBrRadius(0).setBlRadius(0).invalidate();
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
            RecyclerView recyclerView = activity.findViewById(R.id.recycler_view);
            if (recyclerView != null && recyclerView.isComputingLayout())
                recyclerView.post(super::notifyChanged);
            else super.notifyChanged();
        } else super.notifyChanged();
    }
}
