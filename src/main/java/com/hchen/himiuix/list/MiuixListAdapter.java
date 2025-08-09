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
package com.hchen.himiuix.list;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.UiContext;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hchen.himiuix.R;
import com.hchen.himiuix.callback.OnChooseItemListener;
import com.hchen.himiuix.widget.MiuixCheckBox;

import java.util.ArrayList;
import java.util.Map;

/**
 * Miuix List Adapter
 *
 * @author 焕晨HChen
 */
public class MiuixListAdapter extends RecyclerView.Adapter<MiuixListAdapter.MiuixListViewHolder> {
    private static final String TAG = "HiMiuix";
    @NonNull
    @UiContext
    private final Context context;
    @NonNull
    private final RecyclerView recyclerView;
    private final int layoutRes;
    private CharSequence[] items;
    private Map<CharSequence, Drawable> map;
    private int itemBackgroundColor;
    private int itemChooseBackgroundColor;
    private boolean isChooseBackgroundColorEnabled = true;
    private boolean isMultipleChoiceEnabled = false;
    private SparseBooleanArray booleanArray = new SparseBooleanArray();
    private OnChooseItemListener onChooseItemListener;

    public MiuixListAdapter(@NonNull @UiContext Context context) {
        this(context, 0);
    }

    public MiuixListAdapter(@NonNull @UiContext Context context, @LayoutRes int res) {
        this.context = context;
        layoutRes = res;
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        recyclerView.setHorizontalScrollBarEnabled(false);
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setAdapter(this);

        itemBackgroundColor = context.getColor(R.color.miuix_item_background);
        itemChooseBackgroundColor = context.getColor(R.color.miuix_item_choose_background);
    }

    @NonNull
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setItems(@NonNull CharSequence[] items) {
        this.items = items;
    }

    public void setIconItemsMap(Map<CharSequence, Drawable> map) {
        this.map = map;
    }

    public void setItemBackgroundColor(int color) {
        itemBackgroundColor = color;
    }

    public void setItemChooseBackgroundColor(int color) {
        itemChooseBackgroundColor = color;
    }

    public void setChooseBackgroundColorEnabled(boolean enabled) {
        isChooseBackgroundColorEnabled = enabled;
    }

    public void setOnChooseItemListener(OnChooseItemListener listener) {
        onChooseItemListener = listener;
    }

    public void setBooleanArray(SparseBooleanArray booleanArray) {
        this.booleanArray = booleanArray;
    }

    public void setMultipleChoiceEnabled(boolean enabled) {
        isMultipleChoiceEnabled = enabled;
    }

    @NonNull
    @Override
    public MiuixListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MiuixListViewHolder(
            LayoutInflater.from(context)
                .inflate(layoutRes == 0 ? R.layout.miuix_item : layoutRes, parent, false)
        );
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NonNull MiuixListViewHolder holder, int position) {
        holder.layout.setOnTouchListener(null);
        holder.xCheckBox.setOnStateChangeListener(null);

        if (map == null) holder.iconView.setVisibility(GONE);
        else {
            holder.iconView.setImageDrawable(map.get(items[position]));
            holder.iconView.setVisibility(VISIBLE);
        }
        updateItemBackground(holder);
        holder.textView.setText(items[position]);
        holder.xCheckBox.setChecked(booleanArray.get(position));
        holder.layout.setOnTouchListener((v, event) -> holder.xCheckBox.dispatchTouchEvent(event));
        holder.xCheckBox.setOnStateChangeListener(newValue -> {
            if (onChooseItemListener == null || onChooseItemListener.onChooseBefore(items[holder.getAbsoluteAdapterPosition()], holder.getAbsoluteAdapterPosition())) {
                if (isMultipleChoiceEnabled) {
                    booleanArray.put(holder.getAbsoluteAdapterPosition(), newValue);
                    updateItemBackground(holder);
                    if (onChooseItemListener != null) {
                        ArrayList<CharSequence> resultItems = new ArrayList<>();
                        ArrayList<Integer> resultValues = new ArrayList<>();
                        for (int i = 0; i < items.length; i++) {
                            if (booleanArray.get(i)) {
                                resultItems.add(items[i]);
                                resultValues.add(i);
                            }
                        }
                        onChooseItemListener.onChooseAfter(items,
                            resultItems.toArray(new CharSequence[0]),
                            resultValues.toArray(new Integer[0]));
                    }
                } else {
                    int lastSelected = getCheckedPosition();
                    if (lastSelected == holder.getAbsoluteAdapterPosition())
                        return false;

                    booleanArray.clear();
                    booleanArray.put(holder.getAbsoluteAdapterPosition(), newValue);
                    notifyItemChanged(lastSelected);
                    notifyItemChanged(holder.getAbsoluteAdapterPosition());
                    if (onChooseItemListener != null)
                        onChooseItemListener.onChooseAfter(items,
                            new CharSequence[]{items[holder.getAbsoluteAdapterPosition()]},
                            new Integer[]{holder.getAbsoluteAdapterPosition()});
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    private void updateItemBackground(MiuixListViewHolder holder) {
        if (booleanArray.get(holder.getAbsoluteAdapterPosition())) {
            if (isChooseBackgroundColorEnabled)
                holder.layout.setBackgroundColor(itemChooseBackgroundColor);
            holder.textView.setTextColor(context.getColor(R.color.miuix_item_choose_text));
        } else {
            if (isChooseBackgroundColorEnabled)
                holder.layout.setBackgroundColor(itemBackgroundColor);
            holder.textView.setTextColor(context.getColor(R.color.miuix_item_text));
        }
    }

    private int getCheckedPosition() {
        for (int i = 0; i < booleanArray.size(); i++) {
            if (booleanArray.valueAt(i)) return booleanArray.keyAt(i);
        }
        return -1;
    }

    public static class MiuixListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        ImageView iconView;
        TextView textView;
        MiuixCheckBox xCheckBox;

        public MiuixListViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (LinearLayout) itemView;
            iconView = itemView.findViewById(R.id.miuix_item_icon);
            textView = itemView.findViewById(R.id.miuix_item_text);
            xCheckBox = itemView.findViewById(R.id.miuix_item_check_box);
        }
    }
}
