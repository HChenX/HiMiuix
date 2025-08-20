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
package com.hchen.himiuix.springback;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static androidx.core.view.ViewCompat.TYPE_TOUCH;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

import com.hchen.himiuix.R;
import com.hchen.himiuix.helper.HapticFeedbackHelper;
import com.hchen.himiuix.utils.MiuixUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringBackLayout
 * <p>
 * 回弹布局
 * <p>
 * 改编自 HyperOS 2
 *
 * @author 焕晨HChen
 */
public class SpringBackLayout extends ViewGroup implements NestedScrollingParent3, NestedScrollingChild3 {
    private static final String TAG = "HiMiuix:SpringBack";
    // private static final int INVALID_ID = -1;
    private static final int INVALID_POINTER = -1;
    private static final int VELOCITY_THRESHOLD = 2000;
    private static final int MAX_FLING_CONSUME_COUNTER = 4;
    private static final int SPRING_BACK_TOP = 1; // 顶部回弹
    private static final int SPRING_BACK_BOTTOM = 2; // 底部回弹
    private static final int SPRING_BACK_TOP_BOTTOM = 3; // 顶部/底部回弹
    static final int UNCHECK_ORIENTATION = 0;
    public static final int HORIZONTAL = 1; // 方向水平
    public static final int VERTICAL = 2; // 方向垂直
    private static final int X = 0;
    private static final int Y = 1;
    private static final int SCROLL_STATE_IDLE = 0;
    private static final int SCROLL_STATE_DRAGGING = 1;
    private static final int SCROLL_STATE_SETTLING = 2;
    // Wrong Value
    // public static final int ANGLE = 4;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final SpringBackLayoutHelper mHelper;
    private final SpringScroller mSpringScroller;
    private final List<ViewCompatOnScrollChangeListener> mOnScrollChangeListeners;
    private OnSpringListener mOnSpringListener;
    private int mConsumeNestFlingCounter;
    private int mActivePointerId;
    private int mFakeScrollX;
    private int mFakeScrollY;
    private float mInitialDownX; // 初始按下 X 值
    private float mInitialDownY; // 初始按下 Y 值
    private float mInitialMotionX; // 初始运动 X 值
    private float mInitialMotionY; // 初始运动 Y 值
    private int mNestedScrollAxes;
    private boolean isBeingDragged; // 正在被手指拖动
    private boolean isNestedFlingInProgress; // 抛掷运动中
    private boolean isNestedScrollInProgress; // 滚动运动中
    private final int[] mParentOffsetInWindow;
    private final int[] mParentScrollConsumed;
    private final int[] mNestedScrollingV2ConsumedCompat;
    private int mSupportScrollOrientation; // 支持的运动方向
    private float mTotalFlingUnconsumed;
    // ------------------------------------------
    // 保存的未消耗量，表现为布局被下拉途中再次向上滑动之类的
    private float mTotalScrollBottomUnconsumed;
    private float mTotalScrollTopUnconsumed;
    // ------------------------------------------
    private boolean isScrollByFling;
    private int mScrollOrientation;
    private int mScrollState;
    private final int mTouchSlop;
    private float mVelocityX;
    private float mVelocityY;
    private View mTarget;
    private final int mTargetId;
    private int mSpringBackMode; // 回弹模式：顶部/底部
    private boolean isGluttonEnabled; // 贪吃模式：本 Layout 处理全部滚动
    private boolean isSpringBackEnabled;
    private boolean isLinkageAppBar; // 关联 Appbar
    private boolean isHapticFeedbackEnabled;
    protected int mScreenHeight;
    protected int mScreenWidth;

    public interface OnSpringListener {
        boolean onSpringBack();
    }

    public SpringBackLayout(Context context) {
        this(context, null);
    }

    public SpringBackLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mScrollState = SCROLL_STATE_IDLE;
        mActivePointerId = INVALID_POINTER;
        mConsumeNestFlingCounter = 0;
        mParentScrollConsumed = new int[2];
        mParentOffsetInWindow = new int[2];
        mNestedScrollingV2ConsumedCompat = new int[2];
        mOnScrollChangeListeners = new ArrayList<>();
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SpringBackLayout);
        isSpringBackEnabled = typedArray.getBoolean(R.styleable.SpringBackLayout_springBackEnabled, true);
        isGluttonEnabled = typedArray.getBoolean(R.styleable.SpringBackLayout_gluttonEnabled, false);
        isLinkageAppBar = typedArray.getBoolean(R.styleable.SpringBackLayout_linkageAppBar, false);
        isHapticFeedbackEnabled = typedArray.getBoolean(R.styleable.SpringBackLayout_android_hapticFeedbackEnabled, true);
        mTargetId = typedArray.getResourceId(R.styleable.SpringBackLayout_scrollableView, -1);
        mSupportScrollOrientation = typedArray.getInt(R.styleable.SpringBackLayout_scrollOrientation, VERTICAL);
        mSpringBackMode = typedArray.getInt(R.styleable.SpringBackLayout_springBackMode, SPRING_BACK_TOP_BOTTOM);
        typedArray.recycle();

        mSpringScroller = new SpringScroller();
        mHelper = new SpringBackLayoutHelper(this);
        setHapticFeedbackEnabled(isHapticFeedbackEnabled);
        setNestedScrollingEnabled(true);

        Point screenSize = MiuixUtils.getScreenSize(context);
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    public void setSpringBackEnable(boolean enabled) {
        isSpringBackEnabled = enabled;
    }

    public boolean isSpringBackEnable() {
        return isSpringBackEnabled;
    }

    public void setGluttonEnabled(boolean enabled) {
        isGluttonEnabled = enabled;
    }

    public boolean isGluttonEnabled() {
        return isGluttonEnabled;
    }

    public void setLinkageAppBar(boolean linkageAppBar) {
        isLinkageAppBar = linkageAppBar;
    }

    public boolean isLinkageAppBar() {
        return isLinkageAppBar;
    }

    @Override
    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        isHapticFeedbackEnabled = hapticFeedbackEnabled;
        super.setHapticFeedbackEnabled(hapticFeedbackEnabled);
    }

    // 设置支持的运动方向
    public void setScrollOrientation(int orientation) {
        mSupportScrollOrientation = orientation;
    }

    public void setSpringBackMode(int mode) {
        mSpringBackMode = mode;
    }

    public int getSpringBackMode() {
        return mSpringBackMode;
    }

    private int getFakeScrollX() {
        return mFakeScrollX;
    }

    private int getFakeScrollY() {
        return mFakeScrollY;
    }

    private int getSpringScrollX() {
        if (isSpringBackEnabled) {
            return getScrollX();
        }
        return getFakeScrollX();
    }

    private int getSpringScrollY() {
        if (isSpringBackEnabled) {
            return getScrollY();
        }
        return getFakeScrollY();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mTarget instanceof NestedScrollingChild3 && enabled != mTarget.isNestedScrollingEnabled()) {
            mTarget.setNestedScrollingEnabled(enabled);
        }
    }

    // 是否支持顶部回弹
    private boolean isSpringBackTopModeSupported() {
        return (mSpringBackMode & SPRING_BACK_TOP) != 0;
    }

    private boolean isSpringBackBottomModeSupport() {
        return (mSpringBackMode & SPRING_BACK_BOTTOM) != 0;
    }

    public void setTarget(@NonNull View target) {
        mTarget = target;
        if ((mTarget instanceof NestedScrollingChild3) && !mTarget.isNestedScrollingEnabled())
            mTarget.setNestedScrollingEnabled(true);
        if (mTarget.getOverScrollMode() != OVER_SCROLL_NEVER && isSpringBackEnabled)
            mTarget.setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private void ensureTarget() {
        if (mTarget == null) {
            if (mTargetId == -1)
                throw new IllegalArgumentException("Invalid target id: " + mTargetId);
            mTarget = findViewById(mTargetId);
        }
        if (mTarget == null)
            throw new IllegalArgumentException("Failed to get target view!!");

        if (isEnabled())
            if (mTarget instanceof NestedScrollingChild3 && !mTarget.isNestedScrollingEnabled())
                mTarget.setNestedScrollingEnabled(true);
        if (mTarget.getOverScrollMode() != OVER_SCROLL_NEVER && isSpringBackEnabled)
            mTarget.setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public View getTarget() {
        ensureTarget();
        return mTarget;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mTarget.getVisibility() != GONE) {
            int measuredWidth = mTarget.getMeasuredWidth();
            int measuredHeight = mTarget.getMeasuredHeight();
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            mTarget.layout(paddingLeft, paddingTop, measuredWidth + paddingLeft, measuredHeight + paddingTop);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureTarget();

        int measuredWidth;
        int measuredHeight;
        final int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        measureChild(mTarget, widthMeasureSpec, heightMeasureSpec);

        // 父容器指定了精确尺寸
        if (widthMode == EXACTLY) measuredWidth = widthSize;
        else if (widthMode == AT_MOST)
            // 父容器指定了最大尺寸，子视图不能超过
            measuredWidth = Math.min(widthSize, mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight());
        else // 父容器未指定尺寸，包裹子视图
            measuredWidth = mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight();

        // 同上
        if (heightMode == EXACTLY) measuredHeight = heightSize;
        else if (heightMode == AT_MOST)
            measuredHeight = Math.min(heightSize, mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
        else measuredHeight = mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mSpringScroller.computeScrollOffset()) {
            scrollTo(mSpringScroller.getCurrentX(), mSpringScroller.getCurrentY());
            if (!mSpringScroller.isFinished()) {
                postInvalidateOnAnimation();
            } else {
                // 动画结束，检查是否滚回原点
                if (getSpringScrollX() != 0 || getSpringScrollY() != 0) {
                    // 未回到原点，错误的状态，再次触发回弹
                    if (mScrollState != SCROLL_STATE_SETTLING) {
                        Log.d("SpringBackLayout", "Scroll stop but state is not correct.");
                        springBack(mNestedScrollAxes == SCROLL_AXIS_VERTICAL ? VERTICAL : HORIZONTAL);
                        return;
                    }
                }
                // 滚动完成
                dispatchScrollState(SCROLL_STATE_IDLE);
            }
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (isSpringBackEnabled) {
            super.scrollTo(x, y);
        } else {
            final int oldFakeScrollX = mFakeScrollX;
            final int oldFakeScrollY = mFakeScrollY;

            if (oldFakeScrollX == x && oldFakeScrollY == y)
                return; // 位置未变更

            mFakeScrollX = x;
            mFakeScrollY = y;

            onScrollChanged(mFakeScrollX, mFakeScrollY, oldFakeScrollX, oldFakeScrollY);
            if (!awakenScrollBars()) postInvalidateOnAnimation();
            // requestLayout(); // 开销较大
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 通知所有注册的滚动监听器
        for (ViewCompatOnScrollChangeListener listener : mOnScrollChangeListeners) {
            listener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    // 判断目标视图的滚动方向是否为指定方向
    private boolean isTargetScrollOrientation(int orientation) {
        return mScrollOrientation == orientation;
    }

    private boolean isSupportScrollOrientation(int orientation) {
        return (mSupportScrollOrientation & orientation) != 0;
    }

    // 判断目标 View 是否滚动到顶部/左边
    private boolean isTargetScrollToTop(int orientation) {
        if (orientation == VERTICAL) {
            if (mTarget instanceof ListView listView) {
                return !listView.canScrollList(-1);
            }
            return !mTarget.canScrollVertically(-1);
        }
        return !mTarget.canScrollHorizontally(-1);
    }

    // 判断目标 View 是否滚动到底部/右边
    private boolean isTargetScrollToBottom(int orientation) {
        if (orientation == VERTICAL) {
            if (mTarget instanceof ListView listView) {
                return !listView.canScrollList(1);
            }
            return !mTarget.canScrollVertically(1);
        }
        return !mTarget.canScrollHorizontally(1);
    }

    /*
     * 代码中关于 Touch 方法的实现
     * 基本是为了不支持嵌套滚动的视图准备的，虽然可以使用但是兼容性较差
     * 这里不建议包裹使用非嵌套滚动视图，可能存在未知 BUG！！
     * */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // 当手指按下 (ACTION_DOWN) 并且当前正处于回弹动画中 (SCROLL_STATE_SETTLING)
        // 并且触摸点在目标 View 内，此时应该将滚动状态从 SETTLING 改为 DRAGGING
        // 因为用户可能想要在回弹动画未结束时再次拖动
        if (event.getActionMasked() == ACTION_DOWN &&
            mScrollState == SCROLL_STATE_SETTLING && mHelper.isTouchInTarget(event)) {
            dispatchScrollState(SCROLL_STATE_DRAGGING);
        }

        boolean dispatchTouchEvent = super.dispatchTouchEvent(event);

        // 当手指抬起 (ACTION_UP) 或事件取消 (ACTION_CANCEL)
        // 并且当前滚动状态不是回弹动画中 (SCROLL_STATE_SETTLING)
        // 这意味着拖动或点击结束，此时应该将滚动状态设置为空闲 (SCROLL_STATE_IDLE)
        if ((event.getActionMasked() == ACTION_UP || event.getActionMasked() == ACTION_CANCEL) &&
            mScrollState != SCROLL_STATE_SETTLING) {
            dispatchScrollState(SCROLL_STATE_IDLE);
        }
        return dispatchTouchEvent;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 如果回弹效果未启用、SpringBackLayout 本身未启用
        // 或者当前有嵌套 Fling 或嵌套 Scroll 正在进行中
        // 或者目标 View 已启用嵌套滚动 (由目标 View 自己处理)，则不拦截事件
        if (!isSpringBackEnabled || !isEnabled() || isNestedFlingInProgress ||
            isNestedScrollInProgress || mTarget.isNestedScrollingEnabled())
            return false;

        int action = event.getActionMasked();
        // 如果手指按下 (ACTION_DOWN) 时，回弹动画 (mSpringScroller) 尚未结束
        // 则强制停止当前的回弹动画，以便响应新的触摸操作
        if (!mSpringScroller.isFinished() && action == ACTION_DOWN) {
            mSpringScroller.forceStop();
            dispatchScrollState(SCROLL_STATE_IDLE);
        }

        if (isSpringBackTopModeSupported() || isSpringBackBottomModeSupport()) {
            checkOrientation(event);

            // 拦截垂直触摸事件
            if (isTargetScrollOrientation(VERTICAL) && isSupportScrollOrientation(VERTICAL))
                return shouldInterceptTouchEventInternal(event, VERTICAL);

            // 拦截水平触摸事件
            if (isTargetScrollOrientation(HORIZONTAL) && isSupportScrollOrientation(HORIZONTAL))
                return shouldInterceptTouchEventInternal(event, HORIZONTAL);

            return false;
        }
        return false;
    }

    // 检查触摸的方向
    private void checkOrientation(MotionEvent event) {
        mHelper.checkOrientation(event);
        int action = event.getActionMasked();
        switch (action) {
            case ACTION_DOWN -> {
                mInitialDownY = mHelper.mInitialDownY;
                mInitialDownX = mHelper.mInitialDownX;
                mActivePointerId = mHelper.mActivePointerId;
                if (getScrollY() != 0) {
                    mScrollOrientation = VERTICAL;
                    requestDisallowParentInterceptTouchEvent(true);
                } else if (getScrollX() != 0) {
                    mScrollOrientation = HORIZONTAL;
                    requestDisallowParentInterceptTouchEvent(true);
                } else {
                    mScrollOrientation = UNCHECK_ORIENTATION;
                }

                // 根据预设的原始滚动方向，进行滚动开始的检查
                if (isSupportScrollOrientation(VERTICAL)) calculateInitialMotionDistance(VERTICAL);
                if (isSupportScrollOrientation(HORIZONTAL))
                    calculateInitialMotionDistance(HORIZONTAL);
            }
            case ACTION_MOVE -> {
                if (mScrollOrientation == UNCHECK_ORIENTATION && mHelper.mScrollOrientation != UNCHECK_ORIENTATION)
                    mScrollOrientation = mHelper.mScrollOrientation;
            }
            case ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event);
            }
            case ACTION_UP, ACTION_CANCEL -> {
                // 根据预设的原始滚动方向执行回弹
                if (isSupportScrollOrientation(VERTICAL)) springBack(VERTICAL);
                if (isSupportScrollOrientation(HORIZONTAL)) springBack(HORIZONTAL);
                requestDisallowParentInterceptTouchEvent(false);
            }
        }
    }

    private boolean shouldInterceptTouchEventInternal(MotionEvent event, int orientation) {
        // 检查基本条件：目标 View 是否滚动到顶或底
        boolean isScrollToTop = isTargetScrollToTop(orientation);
        boolean isScrollToBottom = isTargetScrollToBottom(orientation);

        // 如果目标 View 没有滚动到顶或底，则不拦截
        if (!isScrollToTop && !isScrollToBottom) return false;
        // 如果目标滚动到顶部，但我们不支持回弹，则不拦截
        if (isScrollToTop && !isSpringBackTopModeSupported()) return false;
        // 如果目标滚动到底部，但我们不支持回弹，则不拦截
        if (isScrollToBottom && !isSpringBackBottomModeSupport()) return false;

        final int action = event.getActionMasked();
        switch (action) {
            case ACTION_DOWN -> {
                final int pointerId = event.getPointerId(0);
                final int pointerIndex = event.findPointerIndex(pointerId);
                if (pointerIndex < 0) {
                    Log.e("SpringBackLayout", "ACTION_DOWN: Invalid pointer index for ID: " + pointerId);
                    return false;
                }

                mActivePointerId = pointerId;
                float initialCoordinate;
                int currentScrollOffset;
                if (orientation == VERTICAL) {
                    initialCoordinate = event.getY(pointerIndex);
                    mInitialDownY = initialCoordinate;
                    currentScrollOffset = getScrollY();
                } else { // HORIZONTAL
                    initialCoordinate = event.getX(pointerIndex);
                    mInitialDownX = initialCoordinate;
                    currentScrollOffset = getScrollX();
                }

                // 如果 View 当前有滚动偏移
                // 并且触摸点在目标 View 内部，那么认为正在拖拽，中断当前的回弹或继续拖动
                if (currentScrollOffset != 0) {
                    isBeingDragged = true;
                    if (orientation == VERTICAL)
                        mInitialMotionY = initialCoordinate;
                    else mInitialMotionX = initialCoordinate;
                    dispatchScrollState(SCROLL_STATE_DRAGGING);
                    return true;
                } else isBeingDragged = false;

                return false;
            }
            case ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e("SpringBackLayout", "ACTION_MOVE: No active pointer ID.");
                    return false;
                }

                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e("SpringBackLayout", "ACTION_MOVE: Invalid pointer index for active ID: " + mActivePointerId);
                    mActivePointerId = INVALID_POINTER;
                    isBeingDragged = false;
                    return false;
                }

                float currentCoordinate;
                float initialDownCoordinate;

                if (orientation == VERTICAL) {
                    currentCoordinate = event.getY(pointerIndex);
                    initialDownCoordinate = mInitialDownY;
                } else { // HORIZONTAL
                    currentCoordinate = event.getX(pointerIndex);
                    initialDownCoordinate = mInitialDownX;
                }

                // 计算滑动距离
                final float delta = currentCoordinate - initialDownCoordinate;

                // 检查是否超过了滑动阈值 mTouchSlop
                if (Math.abs(delta) > mTouchSlop) {
                    // delta > 0: 垂直方向是向下滑动，水平方向是向右滑动
                    // delta < 0: 垂直方向是向上滑动，水平方向是向左滑动
                    if (delta > 0) {
                        if (isScrollToTop && isSpringBackTopModeSupported()) {
                            isBeingDragged = true;
                        }
                    } else {
                        if (isScrollToBottom && isSpringBackBottomModeSupport()) {
                            isBeingDragged = true;
                        }
                    }

                    if (isBeingDragged) {
                        // 更新拖拽起始点
                        if (orientation == VERTICAL) mInitialMotionY = currentCoordinate;
                        else mInitialMotionX = currentCoordinate;
                        dispatchScrollState(SCROLL_STATE_DRAGGING);
                        return true;
                    }
                }
                return false;
            }
            case ACTION_UP, ACTION_CANCEL -> {
                isBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event);
                return isBeingDragged;
            }
            default -> {
                return isBeingDragged;
            }
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (isEnabled() && isSpringBackEnabled) return; // 不允许子视图请求
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void internalRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void requestDisallowParentInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        while (parent != null) {
            if (parent instanceof SpringBackLayout) {
                ((SpringBackLayout) parent).internalRequestDisallowInterceptTouchEvent(disallowIntercept);
            }
            parent = parent.getParent();
        }
    }

    private enum ScrollMode {
        SCROLL_NORMAL,
        SCROLL_TOP,
        SCROLL_BOTTOM
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (!isSpringBackEnabled || !isEnabled() ||
            isNestedFlingInProgress || isNestedScrollInProgress || mTarget.isNestedScrollingEnabled()) {
            return false;
        }
        if (!mSpringScroller.isFinished() && action == ACTION_DOWN)
            mSpringScroller.forceStop();

        int orientation;
        ScrollMode scrollMode;

        if (isTargetScrollOrientation(VERTICAL)) {
            orientation = VERTICAL;
            if (!isTargetScrollToTop(orientation) && !isTargetScrollToBottom(orientation))
                scrollMode = ScrollMode.SCROLL_NORMAL;
                // 滚动到底部，接下来应该向上回弹滚动
            else if (isTargetScrollToTop(orientation)) scrollMode = ScrollMode.SCROLL_TOP;
            else scrollMode = ScrollMode.SCROLL_BOTTOM;
        } else if (isTargetScrollOrientation(HORIZONTAL)) {
            orientation = HORIZONTAL;
            if (!isTargetScrollToTop(orientation) && !isTargetScrollToBottom(orientation))
                scrollMode = ScrollMode.SCROLL_NORMAL;
            else if (isTargetScrollToTop(orientation)) scrollMode = ScrollMode.SCROLL_TOP;
            else scrollMode = ScrollMode.SCROLL_BOTTOM;
        } else return false;


        switch (action) {
            case ACTION_DOWN -> {
                final int pointerId = event.getPointerId(0);
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) return false;

                mActivePointerId = pointerId;
                calculateInitialMotionDistance(orientation);
                return true;
            }
            case ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e("SpringBackLayout", "ACTION_MOVE: No active pointer ID.");
                    return false;
                }

                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e("SpringBackLayout", "ACTION_MOVE: Invalid pointer for active ID " + mActivePointerId);
                    return false;
                }

                if (isBeingDragged) {
                    float currentCoordinate;
                    float initialMotionCoordinate;
                    if (orientation == VERTICAL) {
                        currentCoordinate = event.getY(pointerIndex);
                        initialMotionCoordinate = mInitialMotionY;
                    } else {
                        currentCoordinate = event.getX(pointerIndex);
                        initialMotionCoordinate = mInitialMotionX;
                    }
                    float delta = currentCoordinate - initialMotionCoordinate;
                    if (scrollMode == ScrollMode.SCROLL_BOTTOM) // 对于 SCROLL_END 我们关心的是 initial - current
                        delta = initialMotionCoordinate - currentCoordinate;

                    float dampedDistance = obtainSpringBackDistance(delta, orientation);
                    float signedDampedDistance = Math.signum(delta) * dampedDistance; // 带符号的距离

                    if (scrollMode == ScrollMode.SCROLL_NORMAL) {
                        requestDisallowParentInterceptTouchEvent(true);
                        moveTarget(signedDampedDistance, orientation);
                    } else if (scrollMode == ScrollMode.SCROLL_TOP) {
                        if (signedDampedDistance > 0.0f) { // 上 移动内容
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(signedDampedDistance, orientation);
                        } else { // 左 移动内容
                            moveTarget(orientation == VERTICAL ? 0.0f : signedDampedDistance, orientation);
                            return false;
                        }
                    } else { // SCROLL_BOTTOM
                        if (signedDampedDistance > 0.0f) { // 下 移动内容
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(-signedDampedDistance, orientation);
                        } else { // 右  移动内容
                            moveTarget(orientation == VERTICAL ? 0.0f : -signedDampedDistance, orientation);
                            return false;
                        }
                    }
                    return true;
                }
            }
            case ACTION_UP, ACTION_CANCEL -> {
                if (isBeingDragged) {
                    isBeingDragged = false;
                    springBack(orientation);
                }
                mScrollOrientation = UNCHECK_ORIENTATION;
                mActivePointerId = INVALID_POINTER;
                requestDisallowParentInterceptTouchEvent(false);
                return true;
            }
            case ACTION_POINTER_DOWN -> {
                final int actionIndex = event.getActionIndex(); // 新按下的手指的索引
                final int newPointerId = event.getPointerId(actionIndex);
                final int oldPointerIndex = event.findPointerIndex(mActivePointerId);
                if (oldPointerIndex < 0) {
                    // 旧的活动指针无效，直接使用新的手指作为基准
                    if (orientation == VERTICAL) {
                        mInitialDownY = event.getY(actionIndex);
                        mInitialMotionY = mInitialDownY;
                    } else {
                        mInitialDownX = event.getX(actionIndex);
                        mInitialMotionX = mInitialDownX;
                    }
                } else {
                    // 根据旧指针的偏移量，计算新指针的等效初始按下点
                    if (orientation == VERTICAL) {
                        float previousPointerOffset = event.getY(oldPointerIndex) - mInitialDownY;
                        float newInitialDownY = event.getY(actionIndex) - previousPointerOffset;
                        mInitialDownY = newInitialDownY;
                        mInitialMotionY = newInitialDownY;
                    } else {
                        float previousPointerOffset = event.getX(oldPointerIndex) - mInitialDownX;
                        float newInitialDownX = event.getX(actionIndex) - previousPointerOffset;
                        mInitialDownX = newInitialDownX;
                        mInitialMotionX = newInitialDownX;
                    }
                }
                mActivePointerId = newPointerId;
                return true;
            }
            case ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event);
                return true;
            }
        }
        return false;
    }

    // 计算初始的运动距离
    private void calculateInitialMotionDistance(int orientation) {
        // - 向下/右移动内容，+ 向上 /左移动内容
        final int scrollCoordinate = orientation == VERTICAL ? getScrollY() : getScrollX();

        if (scrollCoordinate != 0) {
            isBeingDragged = true;
            float touchDistance = obtainTouchDistance(Math.abs(scrollCoordinate), orientation);
            if (scrollCoordinate < 0) {
                if (orientation == VERTICAL) mInitialDownY -= touchDistance;
                else mInitialDownX -= touchDistance;
            } else {
                if (orientation == VERTICAL) mInitialDownY += touchDistance;
                else mInitialDownX += touchDistance;
            }
            if (orientation == VERTICAL) mInitialMotionY = mInitialDownY;
            else mInitialMotionX = mInitialDownX;
            return;
        }
        isBeingDragged = false;
    }

    private void moveTarget(float distance /* + 表示向下/右移动内容， - 表示向上/左移动内容 */, int orientation) {
        if (orientation == VERTICAL) scrollTo(0, (int) (-distance));
        else scrollTo((int) (-distance), 0);
    }

    private void springBack(int orientation) {
        springBack(0.0f, orientation, true);
    }

    private void springBack(float velocity, int orientation, boolean shouldInvalidate) {
        if (mOnSpringListener != null && mOnSpringListener.onSpringBack()) return;
        if (!mSpringScroller.isFinished()) mSpringScroller.forceStop();

        int currentScrollX = getScrollX();
        int currentScrollY = getScrollY();
        // 目标是回弹到 (0,0)
        final int targetX = 0;
        final int targetY = 0;

        mSpringScroller.scrollByFling(currentScrollX, targetX, currentScrollY, targetY, velocity, orientation, false);

        if (currentScrollX == targetX && currentScrollY == targetY && velocity == 0.0f)
            dispatchScrollState(SCROLL_STATE_IDLE);
        else dispatchScrollState(SCROLL_STATE_SETTLING);

        if (shouldInvalidate) postInvalidateOnAnimation();
        if (isHapticFeedbackEnabled) {
            if (orientation == VERTICAL) {
                if (Math.abs(currentScrollY) >= 250)
                    HapticFeedbackHelper.performHapticFeedback(this, HapticFeedbackHelper.MIUI_SCROLL_EDGE);
            } else {
                if (Math.abs(currentScrollX) >= 150)
                    HapticFeedbackHelper.performHapticFeedback(this, HapticFeedbackHelper.MIUI_SCROLL_EDGE);
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent event) {
        final int pointerIndexUp = event.getActionIndex();
        final int pointerIdUp = event.getPointerId(pointerIndexUp);

        if (pointerIdUp == mActivePointerId) {
            // 当前活动的指针抬起了，需要选择一个新的活动指针
            // 遍历所有指针，找到第一个仍然按下的指针作为新的活动指针
            int newActivePointerId = -1;
            for (int i = 0; i < event.getPointerCount(); i++) {
                if (i != pointerIndexUp) { // 跳过刚刚抬起的指针
                    newActivePointerId = event.getPointerId(i);
                    break; // 找到第一个即可
                }
            }
            mActivePointerId = newActivePointerId;
        }
    }

    private int getSpringBackRange(int orientation) {
        return orientation == VERTICAL ? mScreenHeight : mScreenWidth;
    }

    // 获取折算阻尼后的回弹距离
    private float obtainSpringBackDistance(float touchAmount, int orientation) {
        int range = getSpringBackRange(orientation);
        return obtainDampingDistance(Math.min(Math.abs(touchAmount) / range, 1.0f), range);
    }

    private float obtainMaxSpringBackDistance(int orientation) {
        return obtainDampingDistance(1.0f, getSpringBackRange(orientation));
    }

    private float obtainDampingDistance(float normalizedInput, int range) {
        // 确保 normalizedInput 在 [0, 1] 范围内
        double x = Math.max(0.0, Math.min(normalizedInput, 1.0));
        // 阻尼函数: x - x^2 + x^3/3
        double dampedFactor = x - Math.pow(x, 2.0) + (Math.pow(x, 3.0) / 3.0);
        return (float) (dampedFactor * range);
    }

    // 获取折算阻尼后的实际可移动距离
    private float obtainTouchDistance(float currentPixelOffset, int orientation) {
        int range = getSpringBackRange(orientation);
        float absPixelOffset = Math.abs(currentPixelOffset);
        float absMaxPixelOffset = Math.abs(obtainMaxSpringBackDistance(orientation));

        // 确保 currentPixelOffset 不超过 maxAchievablePixelOffset
        if (absPixelOffset >= absMaxPixelOffset) return absMaxPixelOffset;
        if (absPixelOffset <= 0) return 0;

        // 原始公式: range - (range^(2/3)) * (range - 3 * currentPixelOffset)^(1/3)
        double part2 = Math.pow(range, 2.0 / 3.0) * Math.pow(range - (3.0 * absPixelOffset), 1.0 / 3.0);
        return (float) (range - part2);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, TYPE_TOUCH, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        final boolean isVertical = (mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL);
        final int primaryConsumed = isVertical ? dyConsumed : dxConsumed; // 主轴已消费量
        final int axisIndex = isVertical ? Y : X;  // consumed 数组索引 (0:X, 1:Y)
        final int beforeParentConsumed = consumed[axisIndex]; // dispatch 前父容器已消费量

        // 先把未消费量让父容器处理
        if (!isGluttonEnabled) // 不是贪吃鬼就交给父布局先处理
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow, type, consumed);

        if (!isSpringBackEnabled) return;

        // 父容器本次新消耗了多少
        final int parentConsumedDelta = consumed[axisIndex] - beforeParentConsumed;
        // netUnconsumed = child 提供的未消费量 - 父容器在 dispatchNestedScroll 中新消耗掉的量
        final int netUnconsumed = (isVertical ? (dyUnconsumed - parentConsumedDelta) : (dxUnconsumed - parentConsumedDelta));
        final int orientation = isVertical ? VERTICAL : HORIZONTAL;

        // 如果 netUnconsumed < 0：认为是到顶部方向的越界；如果 > 0：认为是到底部方向的越界
        if (netUnconsumed < 0 && isTargetScrollToTop(orientation) && isSpringBackTopModeSupported()) {
            handleEdgeSpringBack(netUnconsumed, orientation, axisIndex, primaryConsumed, type, consumed, /* isTop */ true);
        } else if (netUnconsumed > 0 && isTargetScrollToBottom(orientation) && isSpringBackBottomModeSupport()) {
            handleEdgeSpringBack(netUnconsumed, orientation, axisIndex, primaryConsumed, type, consumed, /* isTop */ false);
        }
    }

    private void handleEdgeSpringBack(int netUnconsumed, int orientation, int axisIndex, int primaryConsumed, int type, int[] consumed, boolean isTop) {
        final int absUnconsumed = Math.abs(netUnconsumed);
        final float maxSpring = obtainMaxSpringBackDistance(orientation);

        // 非触摸 (fling) 情形
        if (type != TYPE_TOUCH) {
            // 如果存在速度，则把状态标记为 fling
            if (mVelocityY != 0.0f || mVelocityX != 0.0f) {
                isScrollByFling = true;
                // 当主轴有移动且未消费量不超过最大回弹距离时设置
                if (primaryConsumed != 0 && absUnconsumed <= maxSpring) {
                    mSpringScroller.setFirstStep(netUnconsumed);
                }
                dispatchScrollState(SCROLL_STATE_SETTLING);
                return;
            }

            if (isTop ? (mTotalScrollTopUnconsumed != 0.0f) : (mTotalScrollBottomUnconsumed != 0.0f)) {
                return;
            }

            // 逐步把 fling 的未消费部分转成越界位移
            float remainingCapacity = maxSpring - mTotalFlingUnconsumed;
            if (mConsumeNestFlingCounter >= MAX_FLING_CONSUME_COUNTER || remainingCapacity <= 0.0f) {
                return; // 超过允许的处理次数或无剩余容量，直接返回
            }

            if (remainingCapacity <= absUnconsumed) {
                mTotalFlingUnconsumed += remainingCapacity;
                consumed[axisIndex] += (int) (Math.signum(netUnconsumed) * remainingCapacity);
            } else {
                mTotalFlingUnconsumed += absUnconsumed;
                consumed[axisIndex] += netUnconsumed;
            }

            dispatchScrollState(SCROLL_STATE_SETTLING);
            float springDistance = obtainSpringBackDistance(mTotalFlingUnconsumed, orientation);
            moveTarget(isTop ? springDistance : -springDistance, orientation);
            mConsumeNestFlingCounter++;
            return;
        }

        // 触摸 (手指拖动) 情形：只有当回弹已完成时才开始累积触摸未消化量
        if (!mSpringScroller.isFinished()) return;

        if (isTop) {
            mTotalScrollTopUnconsumed += absUnconsumed;
            dispatchScrollState(SCROLL_STATE_DRAGGING);
            moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, orientation), orientation);
        } else {
            mTotalScrollBottomUnconsumed += absUnconsumed;
            dispatchScrollState(SCROLL_STATE_DRAGGING);
            moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, orientation), orientation);
        }
        consumed[axisIndex] += netUnconsumed;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        mNestedScrollAxes = axes;

        final boolean vertical = (axes & SCROLL_AXIS_VERTICAL) != 0;
        final int orientation = vertical ? VERTICAL : HORIZONTAL;

        // 如果当前组件不支持该方向就直接拒绝
        if (!isSupportScrollOrientation(orientation))
            return false;

        if (isSpringBackEnabled) {
            if (!onStartNestedScroll(child, child, axes))
                return false;

            final float currentScroll = vertical ? getScrollY() : getScrollX();

            // 如果是 fling (非触摸)，且已经存在滚动位置，并且 target 是 NestedScrollView 则为了避免冲突直接拒绝
            if (type != TYPE_TOUCH && currentScroll != 0.0f && (mTarget instanceof NestedScrollView)) {
                return false;
            }
        }

        mNestedScrollingChildHelper.startNestedScroll(axes, type);
        return true;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return isEnabled();
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        if (isSpringBackEnabled) {
            final boolean containsVertical = (mNestedScrollAxes & SCROLL_AXIS_VERTICAL) != 0;
            final int orientation = containsVertical ? VERTICAL : HORIZONTAL;
            final float currentScroll = containsVertical ? getScrollY() : getScrollX();

            if (type != TYPE_TOUCH) {
                // fling 开始
                if (currentScroll == 0.0f) {
                    mTotalFlingUnconsumed = 0.0f;
                } else {
                    mTotalFlingUnconsumed = obtainTouchDistance(Math.abs(currentScroll), orientation);
                }
                isNestedFlingInProgress = true;
                mConsumeNestFlingCounter = 0;
            } else {
                // touch 开始
                if (currentScroll == 0.0f) {
                    mTotalScrollTopUnconsumed = 0.0f;
                    mTotalScrollBottomUnconsumed = 0.0f;
                } else if (currentScroll < 0.0f) {
                    mTotalScrollTopUnconsumed = obtainTouchDistance(Math.abs(currentScroll), orientation);
                    mTotalScrollBottomUnconsumed = 0.0f;
                } else {
                    mTotalScrollTopUnconsumed = 0.0f;
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(Math.abs(currentScroll), orientation);
                }
                isNestedScrollInProgress = true;
            }

            // 重置速度、停止回弹动画、标记状态
            mVelocityY = 0.0f;
            mVelocityX = 0.0f;
            isScrollByFling = false;
            mSpringScroller.forceStop();
        }

        onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, TYPE_TOUCH);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (isSpringBackEnabled) {
            if ((mNestedScrollAxes & SCROLL_AXIS_VERTICAL) != 0) {
                handlePreScrollOnPrimaryAxis(dy, consumed, type);
            } else {
                handlePreScrollOnPrimaryAxis(dx, consumed, type);
            }
        }

        // 把剩余的交给上层父容器处理 (dispatchNestedPreScroll)
        int[] parentConsumed = mParentScrollConsumed;
        final int remainingX = dx - consumed[0];
        final int remainingY = dy - consumed[1];
        if (dispatchNestedPreScroll(remainingX, remainingY, parentConsumed, null, type)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    private void handlePreScrollOnPrimaryAxis(int distance, int[] consumed, int type) {
        final boolean isVertical = (mNestedScrollAxes & SCROLL_AXIS_VERTICAL) != 0;
        final int orientation = isVertical ? VERTICAL : HORIZONTAL;
        final int axisIndex = isVertical ? Y : X;
        float remainingSpring = 0.0f;

        // TOUCH 情形 (手指拖动)
        if (type == TYPE_TOUCH) {
            if (distance > 0) { // 正向 (顶部方向)
                if (mTotalScrollTopUnconsumed > 0.0f) {
                    if (distance >= mTotalScrollTopUnconsumed) {
                        consumeDelta(consumed, axisIndex, (int) mTotalScrollTopUnconsumed);
                        mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        mTotalScrollTopUnconsumed -= distance;
                        consumeDelta(consumed, axisIndex, distance);
                    }
                    dispatchScrollState(SCROLL_STATE_DRAGGING);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, orientation), orientation);
                    return;
                }
            } else if (distance < 0) { // 反向 (底部方向)
                if (mTotalScrollBottomUnconsumed > 0.0f) {
                    if (-distance >= mTotalScrollBottomUnconsumed) {
                        consumeDelta(consumed, axisIndex, (int) mTotalScrollBottomUnconsumed);
                        mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        mTotalScrollBottomUnconsumed += distance;
                        consumeDelta(consumed, axisIndex, distance);
                    }
                    dispatchScrollState(SCROLL_STATE_DRAGGING);
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, orientation), orientation);
                    return;
                }
            }
            return;
        }

        // FLING 情形 (非触摸)
        final float velocity = (orientation == VERTICAL) ? mVelocityY : mVelocityX;

        if (distance > 0) { // 向顶部方向
            if (mTotalScrollTopUnconsumed > 0.0f) {
                if (velocity > VELOCITY_THRESHOLD) {
                    // 速度足够快，尝试按回弹距离消化一部分或全部
                    float springDistance = obtainSpringBackDistance(mTotalScrollTopUnconsumed, orientation);
                    if (distance > springDistance) {
                        consumeDelta(consumed, axisIndex, (int) springDistance);
                        mTotalScrollTopUnconsumed = 0.0f;
                        remainingSpring = 0.0f;
                    } else {
                        consumeDelta(consumed, axisIndex, distance);
                        remainingSpring = springDistance - distance;
                        mTotalScrollTopUnconsumed = obtainTouchDistance(remainingSpring, orientation);
                    }
                    moveTarget(remainingSpring, orientation);
                    dispatchScrollState(SCROLL_STATE_DRAGGING);
                    return;
                }

                if (!isScrollByFling) {
                    isScrollByFling = true;
                    springBack(velocity, orientation, false);
                }
                if (mSpringScroller.computeScrollOffset()) {
                    scrollTo(mSpringScroller.getCurrentX(), mSpringScroller.getCurrentY());
                    mTotalScrollTopUnconsumed = obtainTouchDistance(Math.abs(isVertical ? getScrollY() : getScrollX()), orientation);
                } else {
                    mTotalScrollTopUnconsumed = 0.0f;
                }

                consumeDelta(consumed, axisIndex, distance);
                return;
            }
        } else if (distance < 0) { // 向底部方向
            if (mTotalScrollBottomUnconsumed > 0.0f) {
                if (velocity < -VELOCITY_THRESHOLD) {
                    float springDistance = obtainSpringBackDistance(mTotalScrollBottomUnconsumed, orientation);
                    if (distance < -springDistance) {
                        consumeDelta(consumed, axisIndex, (int) springDistance);
                        mTotalScrollBottomUnconsumed = 0.0f;
                        remainingSpring = 0.0f;
                    } else {
                        consumeDelta(consumed, axisIndex, distance);
                        remainingSpring = springDistance + distance;
                        mTotalScrollBottomUnconsumed = obtainTouchDistance(remainingSpring, orientation);
                    }
                    dispatchScrollState(SCROLL_STATE_DRAGGING);
                    moveTarget(-remainingSpring, orientation);
                    return;
                }

                if (!isScrollByFling) {
                    isScrollByFling = true;
                    springBack(velocity, orientation, false);
                }
                if (mSpringScroller.computeScrollOffset()) {
                    scrollTo(mSpringScroller.getCurrentX(), mSpringScroller.getCurrentY());
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(Math.abs(isVertical ? getScrollY() : getScrollX()), orientation);
                } else {
                    mTotalScrollBottomUnconsumed = 0.0f;
                }
                consumeDelta(consumed, axisIndex, distance);
                return;
            }
        }

        // 兜底：如果存在 fling 状态且视图在中立位置，有时候需要消费此 distance
        if (distance != 0) {
            if ((mTotalScrollBottomUnconsumed == 0.0f || mTotalScrollTopUnconsumed == 0.0f)
                && isScrollByFling && (getScrollY() == 0 || getScrollX() == 0)) {
                consumeDelta(consumed, axisIndex, distance);
            }
        }
    }

    private void consumeDelta(int[] consumed, int axisIndex, int delta) {
        // axisIndex: 0 -> X, 1 -> Y
        consumed[axisIndex] += delta;
    }

    @Override
    public void onStopNestedScroll(@NonNull View child) {
        onStopNestedScroll(child, TYPE_TOUCH);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mNestedScrollingParentHelper.onStopNestedScroll(target, type);
        mNestedScrollingChildHelper.stopNestedScroll(type);

        if (!isSpringBackEnabled) return;

        boolean isVertical = (mNestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        int orientation = isVertical ? VERTICAL : HORIZONTAL;
        float currentScroll = isVertical ? getScrollY() : getScrollX();

        if (isNestedScrollInProgress) { // 触摸滚动正在进行中
            isNestedScrollInProgress = false;
            // 如果触摸滚动结束时，没有并行的 fling，并且视图偏离了原点，则触发回弹
            if (!isNestedFlingInProgress && currentScroll != 0.0f) {
                springBack(orientation);
            } else if (isNestedFlingInProgress && currentScroll != 0.0f) {
                // 如果触摸结束时，一个外部 fling 仍在进行，按 fling 停止逻辑处理
                stopNestedFlingScroll(orientation);
            }
            // 如果 currentScroll == 0.0f，则已在原点，无需操作
        } else if (isNestedFlingInProgress) { // Fling 正在进行中
            stopNestedFlingScroll(orientation);
        }
    }

    private void stopNestedFlingScroll(int orientation) {
        isNestedFlingInProgress = false; // 标记 fling 已结束

        if (isScrollByFling) {
            isScrollByFling = false; // 重置标记，因为这次 fling 事件处理完毕

            if (mSpringScroller.isFinished()) {
                // 如果 Scroller 已经结束，我们需要确保它回到原点
                // 使用最后记录的速度来启动一次归位回弹
                float lastVelocity = (orientation == VERTICAL) ? mVelocityY : mVelocityX;
                springBack(lastVelocity, orientation, true);
            } else {
                // 如果 Scroller 还没结束，我们需要停止当前的 Scroller 动画，并立即回弹
                mSpringScroller.forceStop();
                springBack(orientation);
            }
        } else {
            // 如果 mScrollByFling 为 false，但 mNestedFlingInProgress 为 true 并结束
            // 这可能是一个未被 mScrollByFling 正确追踪的 fling，或者是一个需要清理的状态
            // 直接触发一次无速度回弹以确保归位
            float currentScroll = (orientation == VERTICAL) ? getScrollY() : getScrollX();
            if (currentScroll != 0.0f) { // 只有偏离原点才需要回弹
                springBack(orientation);
            } else {
                dispatchScrollState(SCROLL_STATE_IDLE);
            }
        }

        mVelocityX = 0;
        mVelocityY = 0;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public void stopNestedScroll(int type) {
        mNestedScrollingChildHelper.stopNestedScroll(type);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mNestedScrollingChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mNestedScrollingChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type, @NonNull int[] consumed) {
        mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Point screenSize = MiuixUtils.getScreenSize(getContext());
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    private void dispatchScrollState(int scrollState) {
        int lastState = mScrollState;
        if (lastState != scrollState) {
            mScrollState = scrollState;
            for (ViewCompatOnScrollChangeListener listener : mOnScrollChangeListeners) {
                listener.onStateChanged(lastState, scrollState, mSpringScroller.isFinished());
            }
        }
    }

    public void addOnScrollChangeListener(ViewCompatOnScrollChangeListener listener) {
        mOnScrollChangeListeners.add(listener);
    }

    public void removeOnScrollChangeListener(ViewCompatOnScrollChangeListener listener) {
        mOnScrollChangeListeners.remove(listener);
    }

    public void setOnSpringListener(OnSpringListener listener) {
        mOnSpringListener = listener;
    }

    public boolean hasSpringListener() {
        return mOnSpringListener != null;
    }

    public boolean onNestedCurrentFling(float velocityX, float velocityY) {
        mVelocityX = velocityX;
        mVelocityY = velocityY;
        return true;
    }
}