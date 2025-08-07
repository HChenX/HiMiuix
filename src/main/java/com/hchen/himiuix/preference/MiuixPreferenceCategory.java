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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.R;
import com.hchen.himiuix.utils.InvokeUtils;

import java.util.List;

/**
 * Preference Category
 *
 * @author 焕晨HChen
 */
public class MiuixPreferenceCategory extends PreferenceGroup {
    private TextView dividerTextView;
    private View dividerView;

    public MiuixPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.miuix_category);
        setSelectable(false);
        setPersistent(false);
    }

    @Override
    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        if (getPreferenceManager() != null) {
            getPreferenceManager().setSharedPreferencesName(getContext().getString(R.string.prefs_name));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        dividerTextView = holder.itemView.findViewById(R.id.miuix_divider_title);
        dividerView = holder.itemView.findViewById(R.id.miuix_divider);

        dividerTextView.setVisibility(GONE);
        dividerView.setVisibility(VISIBLE);

        if (getTitle() != null) {
            dividerTextView.setText(getTitle());
            dividerTextView.setVisibility(VISIBLE);
            dividerView.setVisibility(GONE);
        }
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean shouldDisableDependents() {
        return false;
    }

    /**
     * @noinspection SequencedCollectionMethodCanBeUsed
     */
    @Override
    protected void notifyHierarchyChanged() {
        List<Preference> preferences = InvokeUtils.getField(this, "mPreferences");
        if (preferences.isEmpty())
            super.notifyHierarchyChanged();
        else if (preferences.size() == 1) {
            if (preferences.get(0) instanceof MiuixPreference xPreference)
                xPreference.setCardState(MiuixPreference.CARD_RADIUS);
        } else {
            boolean isFirst = true;
            for (int i = 0; i < preferences.size(); i++) {
                if (!(preferences.get(i) instanceof MiuixPreference xPreference))
                    continue;

                if (isFirst) {
                    xPreference.setCardState(MiuixPreference.CARD_TOP_RADIUS);
                    isFirst = false;
                    continue;
                }
                xPreference.setCardState(MiuixPreference.CARD_NON_RADIUS);
            }

            for (int i = preferences.size() - 1; i >= 0; i--) {
                if (preferences.get(i) instanceof MiuixPreference xPreference) {
                    xPreference.setCardState(MiuixPreference.CARD_BOTTOM_RADIUS);
                    break;
                }
            }
        }

        super.notifyHierarchyChanged();
    }
}
