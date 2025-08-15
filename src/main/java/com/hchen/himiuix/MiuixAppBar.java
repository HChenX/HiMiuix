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

import static androidx.core.view.ViewCompat.TYPE_TOUCH;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;

import com.hchen.himiuix.callback.OnToolbarListener;
import com.hchen.himiuix.helper.AppBarHelper;

/**
 * Miuix AppBar
 *
 * @author 焕晨HChen
 */
public class MiuixAppBar extends LinearLayout implements NestedScrollingParent3, OnToolbarListener {
    private static final String TAG = "HiMiuix:AppBar";
    private NestedScrollingParentHelper helper;

    private Toolbar toolbar;
    private TextView toolbarTitleView;
    private TextView largeTitleView;
    private CollapsibleTitleLayout collapsibleTitleView;

    private CharSequence title;
    private View targetView;

    // --- 动画参数 ---
    private boolean isScrollDown = false;
    private int largeTitleScrollRange; // 大标题完全滚动消失所需的距离
    private final float largeTitleAlphaStartOffsetFraction = 0.0f; // 大标题开始变透明的滚动偏移比例
    private final float largeTitleAlphaEndOffsetFraction = 0.45f; // 大标题完全变透明的滚动偏移比例 (早于完全移出)

    private final float toolbarTitleTargetTranslationY = 0.0f; // Toolbar 标题最终的 Y 轴位置
    private float toolbarTitleInitialTranslationY; // Toolbar 标题初始的 Y 轴偏移
    private final float toolbarTitleAlphaStartOffsetFraction = 0.6f; // Toolbar 标题开始出现的滚动偏移比例
    private final float toolbarTitleAlphaEndOffsetFraction = 1.0f; // Toolbar 标题完全出现的滚动偏移比例

    private int currentScrollOffset = 0; // 当前累积的滚动偏移量

    // --- 吸附动画相关 ---
    private ValueAnimator snapAnimator; // 用于执行吸附动画的 ValueAnimator
    private static final int MIN_SNAP_ANIMATION_DURATION = 50; // 最小吸附动画时间 (毫秒)
    private static final int MAX_SNAP_ANIMATION_DURATION = 300; // 最大吸附动画时间 (毫秒)

    public MiuixAppBar(@NonNull Context context) {
        this(context, null);
    }

    public MiuixAppBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuixAppBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuixAppBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MiuixAppBar, defStyleAttr, defStyleRes);
        title = typedArray.getText(R.styleable.MiuixAppBar_android_title);
        typedArray.recycle();

        setOrientation(VERTICAL);
        AppBarHelper.addOnToolbarListener(this);
        helper = new NestedScrollingParentHelper(this);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        toolbar = new Toolbar(getContext()) {
            @Override
            public void setTitle(CharSequence title) {
                toolbarTitleView.setText(title);
            }
        };
        toolbar.setLayoutParams(params);
        toolbarTitleView = new TextView(getContext());
        toolbarTitleView.setSingleLine();
        toolbarTitleView.setGravity(Gravity.CENTER);
        toolbarTitleView.setTextColor(getResources().getColor(R.color.miuix_title_color));
        toolbarTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.miuix_appbar_title_size));
        toolbar.addView(toolbarTitleView);
        Toolbar.LayoutParams layoutParams = (Toolbar.LayoutParams) toolbarTitleView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        toolbarTitleView.setLayoutParams(layoutParams);

        collapsibleTitleView = new CollapsibleTitleLayout(getContext());
        collapsibleTitleView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.miuix_appbar_padding_top),
            0, getResources().getDimensionPixelSize(R.dimen.miuix_appbar_padding_bottom));
        largeTitleView = new TextView(getContext());
        largeTitleView.setSingleLine();
        largeTitleView.setGravity(Gravity.LEFT | Gravity.CLIP_VERTICAL);
        largeTitleView.setTextColor(getResources().getColor(R.color.miuix_title_color));
        largeTitleView.setPadding(getResources().getDimensionPixelSize(R.dimen.miuix_appbar_padding_start), 0, 0, 0);
        largeTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.miuix_appbar_large_title_size));
        largeTitleView.setLayoutParams(params);
        collapsibleTitleView.setTargetView(largeTitleView);
        collapsibleTitleView.addView(largeTitleView);
        addView(toolbar);
        addView(collapsibleTitleView);

        setTitle(title);
    }

    public void setTitle(CharSequence title) {
        toolbarTitleView.setText(title);
        largeTitleView.setText(title);
    }

    public void setTargetView(View targetView) {
        this.targetView = targetView;
    }

    public View getTargetView() {
        return targetView;
    }

    public CharSequence getTitle() {
        return title;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        post(() -> {
            largeTitleScrollRange = largeTitleView.getHeight();
            if (toolbar != null) toolbarTitleInitialTranslationY = toolbar.getHeight() * 0.1f;
            applyAnimationValues();
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AppBarHelper.removeOnToolbarListener(this);
        cancelSnapAnimation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        post(() -> {
            if (collapsibleTitleView.getMaxHeight() > 0)
                largeTitleScrollRange = collapsibleTitleView.getMaxHeight();
            else if (largeTitleView.getMeasuredHeight() > 0 && largeTitleScrollRange == 0)
                largeTitleScrollRange = largeTitleView.getMeasuredHeight() + largeTitleView.getPaddingTop() + largeTitleView.getPaddingBottom();
            if (toolbar.getMeasuredHeight() > 0)
                toolbarTitleInitialTranslationY = toolbar.getMeasuredHeight() * 0.1f;
            if (snapAnimator == null || !snapAnimator.isRunning()) applyAnimationValues();
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        post(() -> {
            if (collapsibleTitleView.getMaxHeight() > 0)
                largeTitleScrollRange = collapsibleTitleView.getMaxHeight();
            else if (largeTitleView.getMeasuredHeight() > 0)
                largeTitleScrollRange = largeTitleView.getMeasuredHeight() + largeTitleView.getPaddingTop() + largeTitleView.getPaddingBottom();
            if (toolbar.getMeasuredHeight() > 0)
                toolbarTitleInitialTranslationY = toolbar.getMeasuredHeight() * 0.1f;
            if (snapAnimator == null || !snapAnimator.isRunning()) applyAnimationValues();
        });
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        if ((axes & SCROLL_AXIS_VERTICAL) != 0) {
            cancelSnapAnimation();
            return true;
        }
        return false;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        helper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        helper.onStopNestedScroll(target, type);
        handleSnap();
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        // 当 targetView 滚动到顶部或底部后，还有未消耗的滚动量 dyUnconsumed
        // dyUnconsumed < 0: targetView 滚动到顶部后，还想继续向下滚动 (展开 Toolbar 的机会)
        if (dyUnconsumed < 0 && type == TYPE_TOUCH) {
            int previousOffset = currentScrollOffset;
            int newOffset = Math.max(0, currentScrollOffset + dyUnconsumed);
            int delta = newOffset - previousOffset;
            if (delta != 0) {
                currentScrollOffset = newOffset;
                applyAnimationValues();
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, new int[2]);
    }

    // 不应该消耗，保证顺滑
    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (target != targetView.getParent()) return;
        if (largeTitleScrollRange <= 0) return;

        // dy > 0: 手指向上滑动，内容向上滚动 (折叠Toolbar)
        // dy < 0: 手指向下滑动，内容向下滚动 (展开Toolbar)
        if (dy > 0 || (dy < 0 && !targetView.canScrollVertically(-1))) {
            isScrollDown = dy < 0;
            int previousOffset = currentScrollOffset;
            int newOffset = currentScrollOffset + dy;

            // 限制 currentScrollOffset 在 [0, largeTitleScrollRange] 之间
            newOffset = Math.max(0, Math.min(newOffset, largeTitleScrollRange));

            int delta = newOffset - previousOffset;
            if (delta != 0) {
                currentScrollOffset = newOffset;
                applyAnimationValues();
            }
        }
    }

    private void applyAnimationValues() {
        // 1. 计算整体进度百分比 (0.0 to 1.0)
        // 0.0 = 完全展开 (大标题可见), 1.0 = 完全折叠 (大标题不可见)
        float overallFraction = Math.max(0.0f, Math.min(1.0f, (float) currentScrollOffset / largeTitleScrollRange));

        // --- 大标题动画 ---
        // a. 位移: 大标题整体向上移动，直到完全移出父布局的顶部
        // translationY 为负值表示向上移动
        largeTitleView.setTranslationY(-currentScrollOffset);
        collapsibleTitleView.setCollapseOffset(currentScrollOffset);

        // b. 透明度: 在指定区间内从 1 (不透明) 渐变到 0 (透明)
        // (fraction - start) / (end - start) => 映射到 0-1
        float largeTitleAlphaProgress = (overallFraction - largeTitleAlphaStartOffsetFraction) /
            (largeTitleAlphaEndOffsetFraction - largeTitleAlphaStartOffsetFraction);
        largeTitleAlphaProgress = Math.max(0.0f, Math.min(1.0f, largeTitleAlphaProgress)); // 裁剪到 [0, 1]
        float largeTitleAlpha = 1.0f - largeTitleAlphaProgress;
        largeTitleView.setAlpha(largeTitleAlpha);

        // --- Toolbar 标题动画 ---
        // a. 透明度: 在指定区间内从 0 (透明) 渐变到 1 (不透明)
        float toolbarTitleAlphaProgress = (overallFraction - toolbarTitleAlphaStartOffsetFraction) /
            (toolbarTitleAlphaEndOffsetFraction - toolbarTitleAlphaStartOffsetFraction);
        toolbarTitleAlphaProgress = Math.max(0.0f, Math.min(1.0f, toolbarTitleAlphaProgress)); // 裁剪到 [0, 1]
        float toolbarTitleAlpha = toolbarTitleAlphaProgress;

        // b. 位移: 从初始位置 (toolbarTitleInitialTranslationY) 移动到目标位置 (toolbarTitleTargetTranslationY)
        // 位移的进度与透明度进度同步
        float currentToolbarTitleY = toolbarTitleInitialTranslationY +
            (toolbarTitleTargetTranslationY - toolbarTitleInitialTranslationY) * toolbarTitleAlphaProgress;
        toolbarTitleView.setTranslationY(currentToolbarTitleY);


        // 严格互斥：当一个标题的 alpha > 0.05 (即比较可见) 时，另一个强制为 0
        if (largeTitleAlpha > 0.05f) { // 如果大标题还比较可见
            toolbarTitleView.setAlpha(0.0f);
        } else { // 否则，大标题基本不可见，显示 Toolbar 标题
            toolbarTitleView.setAlpha(toolbarTitleAlpha);
        }
    }

    private void handleSnap() {
        if (largeTitleScrollRange <= 0) return;
        // 如果已经完全展开或完全收起，则无需吸附
        if (currentScrollOffset == 0 || currentScrollOffset == largeTitleScrollRange)
            return;

        cancelSnapAnimation();

        int targetOffset;
        // 判断吸附到哪个状态
        if (isScrollDown) targetOffset = 0;
        else targetOffset = largeTitleScrollRange;

        int scrollDistance = Math.abs(targetOffset - currentScrollOffset);
        long calculatedDuration;
        if (largeTitleScrollRange > 0) {
            final float PIXELS_PER_MILLISECOND = 0.5f; // 每毫秒移动 0.5 个像素
            calculatedDuration = (long) (scrollDistance / PIXELS_PER_MILLISECOND);
        } else calculatedDuration = MIN_SNAP_ANIMATION_DURATION;
        long actualDuration = Math.max(MIN_SNAP_ANIMATION_DURATION, Math.min(calculatedDuration, MAX_SNAP_ANIMATION_DURATION));

        // 如果当前偏移量与目标偏移量不同，则启动动画
        if (currentScrollOffset != targetOffset) {
            snapAnimator = ValueAnimator.ofInt(currentScrollOffset, targetOffset);
            snapAnimator.setDuration(actualDuration);
            snapAnimator.setInterpolator(new LinearInterpolator());
            snapAnimator.addUpdateListener(animation -> {
                currentScrollOffset = (int) animation.getAnimatedValue();
                applyAnimationValues();
            });
            snapAnimator.start();
        }
    }

    private void cancelSnapAnimation() {
        if (snapAnimator != null && snapAnimator.isRunning()) {
            snapAnimator.cancel();
        }
        snapAnimator = null;
    }

    @Override
    public void targetRegister(View view) {
        if (view == null) return;
        targetView = view;
    }

    public static class CollapsibleTitleLayout extends FrameLayout {
        private View targetView;
        private int maxHeight = 0;
        private int visibleHeight = 0;

        public CollapsibleTitleLayout(Context context) {
            super(context);
        }

        public CollapsibleTitleLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CollapsibleTitleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public CollapsibleTitleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            if (maxHeight == 0 && targetView.getMeasuredHeight() > 0) {
                maxHeight = targetView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
                visibleHeight = maxHeight;
            }
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.makeMeasureSpec(visibleHeight, MeasureSpec.EXACTLY));
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (targetView.getMeasuredHeight() > 0) {
                maxHeight = targetView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
                visibleHeight = maxHeight;
            }
        }

        public void setCollapseOffset(int offset) {
            if (maxHeight == 0) return;
            int newHeight = Math.max(0, maxHeight - offset);
            if (newHeight != visibleHeight) {
                visibleHeight = newHeight;
                requestLayout(); // 重新测量并刷新
            }
        }

        public void setTargetView(View targetView) {
            this.targetView = targetView;
        }

        public View getTargetView() {
            return targetView;
        }

        public int getMaxHeight() {
            return maxHeight;
        }
    }
}
