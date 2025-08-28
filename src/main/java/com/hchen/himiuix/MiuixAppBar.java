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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.WindowInsetsCompat;

import com.hchen.himiuix.callback.OnAppBarListener;
import com.hchen.himiuix.helper.AppBarHelper;
import com.hchen.himiuix.helper.WindowInsetsHelper;
import com.hchen.himiuix.springback.SpringBackLayout;
import com.hchen.himiuix.springback.SpringScroller;
import com.hchen.himiuix.utils.InvokeUtils;
import com.hchen.himiuix.utils.MiuiSuperBlur;
import com.hchen.himiuix.utils.MiuixUtils;

import java.util.WeakHashMap;

/**
 * Miuix AppBar
 * <p>
 * 此视图不响应 paddingTop/Bottom
 *
 * @author 焕晨HChen
 * @noinspection FieldCanBeLocal
 */
public class MiuixAppBar extends ViewGroup implements NestedScrollingParent3, OnAppBarListener {
    private static final String TAG = "HiMiuix:AppBar";
    private NestedScrollingParentHelper helper;

    private ViewGroup overallView;
    private Toolbar toolbar;
    private TextView toolbarTitleView;
    private TextView largeTitleView;
    private CollapsibleTitleLayout collapsibleTitleView;
    private final WeakHashMap<View, Boolean> targetSet = new WeakHashMap<>();

    private CharSequence title;
    private View targetView;
    private boolean isAdded;
    private boolean isInitialled;

    // --- Touch ---
    private static final int TOUCH_UNKNOWN = 0;
    private static final int TOUCH_UP = 1;
    private static final int TOUCH_DOWN = 2;
    private int touchDirection = TOUCH_UNKNOWN;
    private float lastTouchY;

    // --- 动画参数 ---
    private int collapsibleScrollRange; // 大标题完全滚动消失所需的距离
    private float toolbarTitleInitialTranslationY; // Toolbar 标题初始的 Y 轴偏移
    private final float toolbarTitleTargetTranslationY = 0.0f; // Toolbar 标题最终的 Y 轴位置
    private final float toolbarTitleTranslationYConvert = 0.15f;
    private final int LARGE_TITLE_ANIMATION_DURATION = 300;
    private final int TOOLBAR_TITLE_ANIMATION_DURATION = 100;

    // --- 动画状态 ---
    private AnimatorSet currentAnimationSet;
    private float largeTitleAlphaProgress = 1.0f;
    private float toolbarTitleAlphaProgress = 0.0f;

    // --- 状态追踪 ---
    private static final int ANIMATION_IDLE = 0;
    private static final int ANIMATION_COLLAPSING = 1;
    private static final int ANIMATION_EXPANDING = 2;
    private int animationRunningState = ANIMATION_IDLE;
    private boolean isCollapsing = false; // 是否正在折叠过程中
    private boolean isExpanding = false; // 是否正在展开过程中

    private int currentScrollOffset = 0; // 当前累积的滚动偏移量

    // --- SpringScroller 弹性动画相关 ---
    private SpringScroller springScroller;
    private Runnable springUpdateRunnable;

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

        AppBarHelper.addOnToolbarListener(this);
        helper = new NestedScrollingParentHelper(this);

        springScroller = new SpringScroller();
        springUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (springScroller.computeScrollOffset()) {
                    int newOffset = springScroller.getCurrentY();
                    newOffset = Math.max(0, Math.min(newOffset, collapsibleScrollRange));

                    if (newOffset != currentScrollOffset) {
                        int delta = currentScrollOffset - newOffset;
                        currentScrollOffset = newOffset;
                        targetView.offsetTopAndBottom(delta);
                        applyAnimationValues();
                    }
                    post(this);
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

            @Override
            public void setNavigationIcon(@Nullable Drawable icon) {
                super.setNavigationIcon(icon);
                ImageButton button = InvokeUtils.callMethod(this, "getNavButtonView", new Class[0]);
                if (button != null) {
                    button.setBackground(null);
                }
            }
        };
        toolbar.setNavigationIcon(R.drawable.miuix_back);
        toolbar.setPadding(getResources().getDimensionPixelSize(R.dimen.miuix_appbar_padding), 0, getResources().getDimensionPixelSize(R.dimen.miuix_appbar_padding), 0);
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
        largeTitleView = new TextView(getContext());
        largeTitleView.setSingleLine();
        largeTitleView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        largeTitleView.setTextColor(getResources().getColor(R.color.miuix_title_color));
        largeTitleView.setPadding(getResources().getDimensionPixelSize(R.dimen.miuix_appbar_padding_start), 0, 0, 0);
        largeTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.miuix_appbar_large_title_size));
        LayoutParams titleParams = new LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.miuix_appbar_collapsible_title_height));
        largeTitleView.setLayoutParams(titleParams);
        collapsibleTitleView.setTitleView(largeTitleView);
        collapsibleTitleView.addView(largeTitleView);

        overallView = new ViewGroup(getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int measuredWidth;
                int measuredHeight;
                final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                Toolbar toolbar = (Toolbar) getChildAt(0);
                CollapsibleTitleLayout titleLayout = (CollapsibleTitleLayout) getChildAt(1);

                measureChild(toolbar, widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                measureChild(titleLayout, widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                if (widthMode == MeasureSpec.EXACTLY) {
                    measuredWidth = widthSize;
                } else {
                    int maxChildWidth = 0;
                    maxChildWidth = Math.max(maxChildWidth, toolbar.getMeasuredWidth());
                    maxChildWidth = Math.max(maxChildWidth, titleLayout.getMeasuredWidth());
                    measuredWidth = maxChildWidth + getPaddingLeft() + getPaddingRight();
                    if (widthMode == MeasureSpec.AT_MOST) {
                        measuredWidth = Math.min(widthSize, measuredWidth);
                    }
                }

                if (heightMode == MeasureSpec.EXACTLY) {
                    measuredHeight = heightSize;
                } else {
                    int totalChildHeight = 0;
                    totalChildHeight += toolbar.getMeasuredHeight();
                    totalChildHeight += titleLayout.getMeasuredHeight();
                    measuredHeight = totalChildHeight + getPaddingTop() + getPaddingBottom();
                    if (heightMode == MeasureSpec.AT_MOST) {
                        measuredHeight = Math.min(heightSize, measuredHeight);
                    }
                }

                setMeasuredDimension(measuredWidth, measuredHeight);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                Toolbar toolbar = (Toolbar) getChildAt(0);
                CollapsibleTitleLayout titleLayout = (CollapsibleTitleLayout) getChildAt(1);

                int currentTop = getPaddingTop();
                int currentLeft = getPaddingLeft();
                int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();

                int toolbarHeight = toolbar.getMeasuredHeight();
                toolbar.layout(currentLeft, currentTop, currentLeft + width, currentTop + toolbarHeight);
                currentTop += toolbarHeight;

                int titleHeight = titleLayout.getMeasuredHeight();
                titleLayout.layout(currentLeft, currentTop, currentLeft + width, currentTop + titleHeight);
            }
        };
        overallView.addView(toolbar);
        overallView.addView(collapsibleTitleView);
        if (!MiuiSuperBlur.isSupportBlur())
            overallView.setBackgroundColor(getContext().getColor(R.color.miuix_theme_color));
        addView(overallView);

        setTitle(title);
        applyWindowInsets();
        isInitialled = true;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        toolbarTitleView.setText(title);
        largeTitleView.setText(title);
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (MiuiSuperBlur.isSupportBlur()) applyBlur();
    }

    private void applyBlur() {
        int colorBottomSurface = getContext().getColor(R.color.miuix_theme_color);
        MiuiSuperBlur.setMiViewBlurMode(overallView, 1);
        MiuiSuperBlur.setMiBackgroundBlurMode(overallView, 1);
        MiuiSuperBlur.setMiBackgroundBlurRadius(overallView, (int) (getContext().getResources().getDisplayMetrics().density * 66 + 0.5f));
        int[] colors = MiuiSuperBlur.getBlendColor(colorBottomSurface, !MiuixUtils.isDarkMode(getResources()) ?
            new int[]{-1889443744, -1543503873} :
            new int[]{1970500467, -1979711488, 184549375});
        int[] colorMode = !MiuixUtils.isDarkMode(getResources()) ? new int[]{18, 3} : new int[]{19, 3, 3};
        for (int i = 0; i < colors.length; i++) {
            MiuiSuperBlur.addMiBackgroundBlendColor(overallView, colors[i], colorMode[i]);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        targetSet.clear();
        cancelSpringAnimation();
        MiuiSuperBlur.clearAllBlur(overallView);
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
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth;
        int measuredHeight;
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        ViewGroup overallView;
        View contentView = isAdded ? getChildAt(0) : null;
        if (contentView != null) overallView = (ViewGroup) getChildAt(1);
        else overallView = (ViewGroup) getChildAt(0);

        measureChild(overallView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        if (contentView != null) {
            int contentHeightMeasureSpec;
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            } else {
                int availableHeight = heightSize + collapsibleScrollRange;
                contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(availableHeight, heightMode);
            }
            measureChild(contentView, widthMeasureSpec, contentHeightMeasureSpec);
        }

        if (widthMode == MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
        } else {
            int maxChildWidth = 0;
            maxChildWidth = Math.max(maxChildWidth, overallView.getMeasuredWidth());
            if (contentView != null)
                maxChildWidth = Math.max(maxChildWidth, contentView.getMeasuredWidth());
            measuredWidth = maxChildWidth + getPaddingLeft() + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST) {
                measuredWidth = Math.min(widthSize, measuredWidth);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = heightSize;
        } else {
            int totalChildHeight = 0;
            if (contentView != null) totalChildHeight += contentView.getMeasuredHeight();
            totalChildHeight += overallView.getMeasuredHeight();
            measuredHeight = totalChildHeight /* + getPaddingTop() + getPaddingBottom() ignore */;
            if (heightMode == MeasureSpec.AT_MOST)
                measuredHeight = Math.min(heightSize, measuredHeight);
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ViewGroup overallView;
        View contentView = isAdded ? getChildAt(0) : null;
        if (contentView != null) overallView = (ViewGroup) getChildAt(1);
        else overallView = (Toolbar) getChildAt(0);

        int currentTop = 0;
        int currentLeft = getPaddingLeft();
        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();

        if (contentView != null) {
            int contentHeight = contentView.getMeasuredHeight();
            contentView.layout(currentLeft, currentTop, currentLeft + width, currentTop + contentHeight);
        }

        int toolbarHeight = overallView.getMeasuredHeight();
        overallView.layout(currentLeft, currentTop, currentLeft + width, currentTop + toolbarHeight);

        if (!targetSet.isEmpty()) {
            for (View lastTargetView : targetSet.keySet()) {
                lastTargetView.layout(lastTargetView.getLeft(), lastTargetView.getTop(), lastTargetView.getRight(), lastTargetView.getBottom());
                if (isCollapsed()) {
                    if (lastTargetView.getTop() == 0)
                        lastTargetView.offsetTopAndBottom(-currentScrollOffset);
                    else if (lastTargetView.getTop() < 0 && lastTargetView.getTop() > (-currentScrollOffset))
                        lastTargetView.offsetTopAndBottom(-((currentScrollOffset) - Math.abs(lastTargetView.getTop())));
                    else if (lastTargetView.getTop() > 0)
                        lastTargetView.offsetTopAndBottom(-(currentScrollOffset + lastTargetView.getTop()));
                } else if (isExpanded()) {
                    if (lastTargetView.getTop() != 0) {
                        if (lastTargetView.getTop() < 0)
                            lastTargetView.offsetTopAndBottom(Math.abs(lastTargetView.getTop()));
                        else if (lastTargetView.getTop() > 0)
                            lastTargetView.offsetTopAndBottom(-lastTargetView.getTop());
                    }
                }
            }
        }

        if (targetView != null) {
            targetView.layout(targetView.getLeft(), targetView.getTop(), targetView.getRight(), targetView.getBottom());
            if (targetView.getTop() == 0 && currentScrollOffset != 0) {
                targetView.offsetTopAndBottom(-currentScrollOffset);
            }
        }
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

            if (collapsibleTitleView.getOriginalHeight() > 0 && collapsibleScrollRange == 0)
                collapsibleScrollRange = collapsibleTitleView.getOriginalHeight();
            if (toolbar.getMeasuredHeight() > 0 && toolbarTitleInitialTranslationY == 0)
                toolbarTitleInitialTranslationY = toolbar.getMeasuredHeight() * toolbarTitleTranslationYConvert;
        });
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!isInitialled) super.addView(child, index, params);
        else if (!isAdded) {
            isAdded = true;
            super.addView(child, 0, params);
        } else throw new RuntimeException("Only supports a single sub view!");
    }

    private void applyWindowInsets() {
        WindowInsetsHelper.setOnApplyWindowInsetsListener(new androidx.core.view.OnApplyWindowInsetsListener() {
            @NonNull @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                overallView.setPadding(0, systemBars.top, 0, 0);
                return insets;
            }
        });
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (axes & SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        helper.onNestedScrollAccepted(child, target, axes);
        cancelSpringAnimation();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        helper.onStopNestedScroll(target, type);
        handleSpringSnap();
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        if (target != targetView.getParent()) return;

        // 当 targetView 滚动到顶部或底部后，还有未消耗的滚动量 dyUnconsumed
        // dyUnconsumed < 0: targetView 滚动到顶部后，还想继续向下滚动 (展开 Toolbar 的机会)
        if (dyUnconsumed < 0) {
            int previousOffset = currentScrollOffset;
            int newOffset = Math.max(0, Math.min(collapsibleScrollRange, currentScrollOffset + dyUnconsumed));
            int delta = newOffset - previousOffset;
            if (delta != 0) {
                cancelSpringAnimation();
                currentScrollOffset = newOffset;
                targetView.offsetTopAndBottom(-delta);
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
        if (dy > 0 || (dy < 0 && !canScrollVertically())) {
            int previousOffset = currentScrollOffset;
            int newOffset = currentScrollOffset + dy;
            newOffset = Math.max(0, Math.min(newOffset, collapsibleScrollRange));

            int delta = newOffset - previousOffset;
            if (delta != 0) {
                cancelSpringAnimation();
                currentScrollOffset = newOffset;
                targetView.offsetTopAndBottom(-delta);
                applyAnimationValues();
                consumed[1] = delta;
            }
        }
    }

    // 是否移动到顶/底
    private boolean canScrollVertically() {
        return targetView != null && targetView.canScrollVertically(-1);
    }

    private boolean isCollapsed() {
        return currentScrollOffset == collapsibleScrollRange;
    }

    private boolean isExpanded() {
        return currentScrollOffset == 0;
    }

    private void applyAnimationValues() {
        if (collapsibleScrollRange <= 0) return;

        // 计算整体进度百分比 (0.0 to 1.0)
        float overallFraction = Math.max(0.0f, Math.min(1.0f, (float) currentScrollOffset / collapsibleScrollRange));

        // 更新大标题位移和容器高度
        largeTitleView.setTranslationY(-currentScrollOffset);
        collapsibleTitleView.setCollapseOffset(currentScrollOffset);

        handleAnimationLogic(overallFraction);
        if (currentAnimationSet == null || !currentAnimationSet.isRunning())
            applyTitleBasicState();
    }

    private void handleAnimationLogic(float overallFraction) {
        boolean isCurrentlyCollapsing = touchDirection == TOUCH_UP;
        boolean isCurrentlyExpanding = touchDirection == TOUCH_DOWN;

        if (overallFraction == 1.0f) {
            if (currentAnimationSet != null && currentAnimationSet.isRunning()) {
                cancelCollapseExpandAnimation();
                pushCollapseExpandAnimation(true, true);
            }
            return;
        }

        if (isCurrentlyCollapsing && !isCollapsing) {
            isCollapsing = true;
            isExpanding = false;
        } else if (isCurrentlyExpanding && !isExpanding) {
            isExpanding = true;
            isCollapsing = false;
        }

        if (isCollapsing) handleCollapsingLogic();
        else if (isExpanding) handleExpandingLogic();
    }

    private void handleCollapsingLogic() {
        if (animationRunningState == ANIMATION_COLLAPSING) return;
        cancelCollapseExpandAnimation();
        pushCollapseExpandAnimation(true, false);
    }

    private void handleExpandingLogic() {
        if (animationRunningState == ANIMATION_EXPANDING) return;
        cancelCollapseExpandAnimation();
        pushCollapseExpandAnimation(false, false);
    }

    private void pushCollapseExpandAnimation(boolean isCollapse, boolean onlyToolbar) {
        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator largeTitleAnimator = ValueAnimator.ofFloat(
            largeTitleAlphaProgress, isCollapse ? 0.0f : 1.0f);
        largeTitleAnimator.setDuration(LARGE_TITLE_ANIMATION_DURATION);
        largeTitleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        largeTitleAnimator.addUpdateListener(animation -> {
            largeTitleAlphaProgress = (float) animation.getAnimatedValue();
            applyTitleBasicState();
        });

        ValueAnimator toolbarTitleAnimator = ValueAnimator.ofFloat(
            toolbarTitleAlphaProgress, isCollapse ? 1.0f : 0.0f);
        toolbarTitleAnimator.setDuration(TOOLBAR_TITLE_ANIMATION_DURATION);
        largeTitleAnimator.setInterpolator(new DecelerateInterpolator());
        toolbarTitleAnimator.addUpdateListener(animation -> {
            toolbarTitleAlphaProgress = (float) animation.getAnimatedValue();
            applyTitleBasicState();
        });

        if (!onlyToolbar) {
            if (isCollapse) animatorSet.playSequentially(largeTitleAnimator, toolbarTitleAnimator);
            else animatorSet.playSequentially(toolbarTitleAnimator, largeTitleAnimator);
        } else {
            animatorSet.play(toolbarTitleAnimator);
        }

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationRunningState = ANIMATION_IDLE;
                currentAnimationSet = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animationRunningState = ANIMATION_IDLE;
                currentAnimationSet = null;
            }
        });
        currentAnimationSet = animatorSet;
        currentAnimationSet.start();

        animationRunningState = isCollapse ? ANIMATION_COLLAPSING : ANIMATION_EXPANDING;
    }

    private void applyTitleBasicState() {
        largeTitleView.setAlpha(largeTitleAlphaProgress);

        toolbarTitleView.setAlpha(toolbarTitleAlphaProgress);
        toolbarTitleView.setTranslationY(Math.max(toolbarTitleTargetTranslationY,
            toolbarTitleInitialTranslationY - (toolbarTitleInitialTranslationY * toolbarTitleAlphaProgress)));
    }

    private void cancelCollapseExpandAnimation() {
        if (currentAnimationSet != null && currentAnimationSet.isRunning())
            currentAnimationSet.cancel();

        animationRunningState = ANIMATION_IDLE;
        currentAnimationSet = null;
    }

    private void handleSpringSnap() {
        if (collapsibleScrollRange <= 0) return;
        if (currentScrollOffset == 0 || currentScrollOffset == collapsibleScrollRange)
            return;

        int targetOffset = determineSnapTarget();
        if (currentScrollOffset == targetOffset) return;

        startSpringSnapAnimation(targetOffset);
    }

    private int determineSnapTarget() {
        if (touchDirection == TOUCH_DOWN) return 0;
        else return collapsibleScrollRange;
    }

    private void startSpringSnapAnimation(int targetOffset) {
        cancelSpringAnimation();

        // 启动 SpringScroller 动画
        springScroller.scrollByFling(
            0, 0,
            currentScrollOffset, targetOffset,
            0,
            SpringBackLayout.VERTICAL,
            false
        );
        post(springUpdateRunnable);
    }

    private void cancelSpringAnimation() {
        if (springScroller.isFinished())
            return;

        springScroller.forceStop();
        removeCallbacks(springUpdateRunnable);
    }

    @Override
    public void targetStart(View view) {
        if (view != null) targetSet.put(view, Boolean.TRUE);
    }

    @Override
    public void targetRegister(View view) {
        if (view == null) return;
        targetView = view;
        targetSet.remove(targetView);
        if (targetView.getTop() != -currentScrollOffset)
            targetView.requestLayout();
    }

    @Override
    public void targetUnregister(View view) {
        if (view == null) return;
        targetSet.put(view, Boolean.TRUE);
    }

    @Override
    public void targetDestroy(View view) {
        if (view != null) targetSet.remove(view);
    }

    private static class CollapsibleTitleLayout extends FrameLayout {
        private View titleView;
        private int originalHeight;
        private int visibleHeight;

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
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            updateCurrentHeight(false);
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.makeMeasureSpec(visibleHeight, MeasureSpec.EXACTLY));
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            updateCurrentHeight(true);
        }

        public void updateCurrentHeight(boolean reset) {
            if (reset || (originalHeight == 0 && titleView.getMeasuredHeight() > 0)) {
                originalHeight = titleView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
                visibleHeight = originalHeight;
            }
        }

        public void setCollapseOffset(int offset) {
            if (originalHeight == 0) return;
            visibleHeight = Math.max(0, originalHeight - offset);
            requestLayout(); // 重新测量并刷新
        }

        public void setTitleView(View titleView) {
            this.titleView = titleView;
        }

        public View getTitleView() {
            return titleView;
        }

        public int getOriginalHeight() {
            return originalHeight;
        }
    }
}
