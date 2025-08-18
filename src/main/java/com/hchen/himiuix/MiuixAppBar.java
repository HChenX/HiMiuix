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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;

import com.hchen.himiuix.callback.OnAppBarListener;
import com.hchen.himiuix.helper.AppBarHelper;

/**
 * Miuix AppBar
 *
 * @author 焕晨HChen
 */
public class MiuixAppBar extends LinearLayout implements NestedScrollingParent3, OnAppBarListener {
    private static final String TAG = "HiMiuix:AppBar";
    private NestedScrollingParentHelper helper;

    private Toolbar toolbar;
    private TextView toolbarTitleView;
    private TextView largeTitleView;
    private CollapsibleTitleLayout collapsibleTitleView;

    private CharSequence title;
    private View targetView;

    // --- Touch ---
    private int touchDirection;
    private final int TOUCH_UNKNOWN = 0;
    private final int TOUCH_UP = 1;
    private final int TOUCH_DOWN = 2;
    private float lastTouchY;

    // --- 动画参数 ---
    private boolean isScrollDown = false;
    private int collapsibleScrollRange; // 大标题完全滚动消失所需的距离
    private final float largeTitleAlphaStartOffsetFraction = 0.0f; // 大标题开始变透明的滚动偏移比例
    private final float largeTitleAlphaEndOffsetFraction = 0.45f; // 大标题完全变透明的滚动偏移比例 (早于完全移出)

    private final float toolbarTitleTargetTranslationY = 0.0f; // Toolbar 标题最终的 Y 轴位置
    private float toolbarTitleInitialTranslationY; // Toolbar 标题初始的 Y 轴偏移
    private final float toolbarTitleAlphaStartOffsetFraction = 0.6f; // Toolbar 标题开始出现的滚动偏移比例
    private final float toolbarTitleAlphaEndOffsetFraction = 1.0f; // Toolbar 标题完全出现的滚动偏移比例

    private int currentScrollOffset = 0; // 当前累积的滚动偏移量

    // --- Scroller 吸附动画相关 ---
    private OverScroller scroller;
    private Runnable scrollUpdateRunnable;

    // 动态速度控制参数
    private static final float BASE_VELOCITY = 2000.0f; // 基础速度 (px/s)
    private static final float VELOCITY_SCALE_FACTOR = 0.6f; // 速度缩放因子，控制距离对速度的影响程度
    private static final float MIN_VELOCITY_MULTIPLIER = 0.3f; // 最小速度倍数
    private static final float MAX_VELOCITY_MULTIPLIER = 2.0f; // 最大速度倍数


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

        scroller = new OverScroller(getContext());
        scrollUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (scroller.computeScrollOffset()) {
                    int newOffset = scroller.getCurrY();
                    if (newOffset != currentScrollOffset) {
                        currentScrollOffset = newOffset;
                        applyAnimationValues();
                    }
                    post(this);
                } else {
                    finalizeSnapAnimation();
                }
            }
        };

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        toolbar = new Toolbar(getContext()) {
            @Override
            public void setTitle(CharSequence title) {
                largeTitleView.setText(title);
                toolbarTitleView.setText(title);
            }

            @Override
            public CharSequence getTitle() {
                return title;
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
        toolbarTitleView.setAlpha(0);

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

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initialParament(false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelSnapAnimation();
        AppBarHelper.removeOnToolbarListener(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                lastTouchY = ev.getRawY();
            }
            case MotionEvent.ACTION_MOVE -> {
                if (ev.getRawY() - lastTouchY > 0) touchDirection = TOUCH_DOWN;
                else if (ev.getRawY() - lastTouchY < 0) touchDirection = TOUCH_UP;
                lastTouchY = ev.getRawY();
            }
            case MotionEvent.ACTION_UP -> {
                lastTouchY = 0.0f;
                touchDirection = TOUCH_UNKNOWN;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initialParament(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initialParament(true);
    }

    private void initialParament(boolean reset) {
        post(() -> {
            if (reset) {
                collapsibleScrollRange = 0;
                toolbarTitleInitialTranslationY = 0;
            }

            if (collapsibleTitleView.getMaxHeight() > 0 && collapsibleScrollRange == 0)
                collapsibleScrollRange = collapsibleTitleView.getMaxHeight();
            else if (largeTitleView.getMeasuredHeight() > 0 && collapsibleScrollRange == 0)
                collapsibleScrollRange = largeTitleView.getMeasuredHeight() + collapsibleTitleView.getPaddingTop() + collapsibleTitleView.getPaddingBottom();
            if (toolbar.getMeasuredHeight() > 0 && toolbarTitleInitialTranslationY == 0)
                toolbarTitleInitialTranslationY = toolbar.getMeasuredHeight() * 0.1f;
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
        if (dyUnconsumed < 0) {
            if (type == TYPE_TOUCH && touchDirection != TOUCH_DOWN) return;

            int previousOffset = currentScrollOffset;
            int newOffset = Math.max(0, currentScrollOffset + dyUnconsumed);
            int delta = newOffset - previousOffset;
            if (delta != 0) {
                currentScrollOffset = newOffset;
                applyAnimationValues();
                consumed[1] += delta;
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, new int[2]);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, TYPE_TOUCH);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (target != targetView.getParent()) return;
        if (collapsibleScrollRange <= 0) return;

        // dy > 0: 手指向上滑动，内容向上滚动 (折叠Toolbar)
        // dy < 0: 手指向下滑动，内容向下滚动 (展开Toolbar)
        if (dy > 0 || (dy < 0 && !targetView.canScrollVertically(-1))) {
            if (type == TYPE_TOUCH) {
                if (dy > 0 && touchDirection != TOUCH_UP) return;
                if (dy < 0 && touchDirection != TOUCH_DOWN) return;
            }

            isScrollDown = dy < 0;
            int previousOffset = currentScrollOffset;
            int newOffset = currentScrollOffset + dy;

            // 限制 currentScrollOffset 在 [0, collapsibleScrollRange] 之间
            newOffset = Math.max(0, Math.min(newOffset, collapsibleScrollRange));

            int delta = newOffset - previousOffset;
            if (delta != 0) {
                currentScrollOffset = newOffset;
                applyAnimationValues();
                consumed[1] = delta;
            }
        }
    }

    private void applyAnimationValues() {
        // 1. 计算整体进度百分比 (0.0 to 1.0)
        // 0.0 = 完全展开 (大标题可见), 1.0 = 完全折叠 (大标题不可见)
        float overallFraction = Math.max(0.0f, Math.min(1.0f, (float) currentScrollOffset / collapsibleScrollRange));

        // --- 大标题动画 ---
        // a. 位移: 大标题整体向上移动，直到完全移出父布局的顶部
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

        // 透明的互斥
        if (largeTitleAlpha > 0.05f) toolbarTitleView.setAlpha(0.0f);
        else toolbarTitleView.setAlpha(toolbarTitleAlpha);
    }

    private void handleSnap() {
        if (collapsibleScrollRange <= 0) return;
        if (currentScrollOffset == 0 || currentScrollOffset == collapsibleScrollRange)
            return;

        int targetOffset = determineSnapTarget();
        if (currentScrollOffset == targetOffset) return;

        startSnapAnimation(targetOffset);
    }

    private int determineSnapTarget() {
        if (isScrollDown) return 0; // 向下滚动，展开
        else return collapsibleScrollRange;
    }

    private void startSnapAnimation(int targetOffset) {
        cancelSnapAnimation();

        int scrollDistance = targetOffset - currentScrollOffset;
        int initialVelocity = calculateDynamicVelocity(Math.abs(scrollDistance));
        // 根据滚动方向调整速度符号
        if (scrollDistance < 0) initialVelocity = -initialVelocity;

        scroller.fling(
            0, currentScrollOffset,     // 起始位置 (startX, startY)
            0, initialVelocity,         // 初始速度 (velocityX, velocityY)
            0, 0,                       // X 方向边界 (minX, maxX)
            Math.min(0, targetOffset),  // Y 方向最小值
            Math.max(collapsibleScrollRange, targetOffset), // Y 方向最大值
            0, 0                        // 过度滚动距离 (overX, overY)
        );
        post(scrollUpdateRunnable);
    }

    private int calculateDynamicVelocity(int distance) {
        if (collapsibleScrollRange <= 0) {
            return (int) BASE_VELOCITY;
        }

        // 计算距离比例 (0.0 - 1.0)
        float distanceRatio = Math.min(1.0f, (float) distance / collapsibleScrollRange);

        // 使用非线性函数计算速度倍数
        // 短距离（小比例）-> 高速度倍数
        // 长距离（大比例）-> 低速度倍数
        float velocityMultiplier = MIN_VELOCITY_MULTIPLIER +
            (MAX_VELOCITY_MULTIPLIER - MIN_VELOCITY_MULTIPLIER) * (1.0f - (float) Math.pow(distanceRatio, VELOCITY_SCALE_FACTOR));

        return (int) (BASE_VELOCITY * velocityMultiplier);
    }

    private void finalizeSnapAnimation() {
        int targetOffset = determineSnapTarget();
        if (currentScrollOffset != targetOffset) {
            currentScrollOffset = targetOffset;
            applyAnimationValues();
        }
    }

    private void cancelSnapAnimation() {
        if (scroller.isFinished()) return;

        scroller.forceFinished(true);
        removeCallbacks(scrollUpdateRunnable);
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
            visibleHeight = Math.max(0, maxHeight - offset);
            requestLayout(); // 重新测量并刷新
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
