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

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class SpringBackLayoutHelper {
    float mInitialDownX; // 手指初次按下时的 X 坐标
    float mInitialDownY; // 手指初次按下时的 Y 坐标
    int mScrollOrientation; // 检测到的滚动方向 (0: 未确定, 1: 水平, 2: 垂直)
    int mActivePointerId = -1; // 当前活动的手指 ID
    private final int mTouchSlop; // 系统定义的最小滑动距离阈值
    private final ViewGroup mTarget; // 宿主 ViewGroup
    private static final int ORIENTATION_NONE = 0;
    private static final int ORIENTATION_HORIZONTAL = 1;
    private static final int ORIENTATION_VERTICAL = 2;

    public SpringBackLayoutHelper(ViewGroup target) {
        mTarget = target;
        mTouchSlop = ViewConfiguration.get(target.getContext()).getScaledTouchSlop();
    }

    private final Rect mTargetBoundsInWindow = new Rect(); // 可复用的Rect

    public boolean isTouchInTarget(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(event.getPointerId(0));
        if (pointerIndex < 0) {
            return false; // 无效指针
        }

        float touchScreenX = event.getX(pointerIndex); // 获取触摸点在事件源坐标系中的 X 坐标
        float touchScreenY = event.getY(pointerIndex); // 获取触摸点在事件源坐标系中的 Y 坐标

        int[] targetLocation = new int[2];
        mTarget.getLocationInWindow(targetLocation);
        int targetLeftInWindow = targetLocation[0];
        int targetTopInWindow = targetLocation[1];

        mTargetBoundsInWindow.set(
            targetLeftInWindow,
            targetTopInWindow,
            targetLeftInWindow + mTarget.getWidth(),
            targetTopInWindow + mTarget.getHeight()
        );

        return mTargetBoundsInWindow.contains((int) touchScreenX, (int) touchScreenY);
    }

    public void checkOrientation(MotionEvent event) {
        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 总是以第一个按下的手指作为活动指针进行方向检测
                mActivePointerId = event.getPointerId(0);
                int downPointerIndex = event.findPointerIndex(mActivePointerId);
                if (downPointerIndex >= 0) {
                    mInitialDownX = event.getX(downPointerIndex);
                    mInitialDownY = event.getY(downPointerIndex);
                } else mActivePointerId = -1;
                mScrollOrientation = ORIENTATION_NONE; // 重置滚动方向
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == -1) {
                    // 没有活动的指针，不处理移动
                    break;
                }
                int movePointerIndex = event.findPointerIndex(mActivePointerId);
                if (movePointerIndex >= 0) {
                    float currentX = event.getX(movePointerIndex);
                    float currentY = event.getY(movePointerIndex);

                    float deltaX = currentX - mInitialDownX;
                    float deltaY = currentY - mInitialDownY;

                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);

                    // 只有当滑动距离超过阈值时才确定方向
                    if (absDeltaX > mTouchSlop || absDeltaY > mTouchSlop) {
                        if (absDeltaX > absDeltaY) {
                            mScrollOrientation = ORIENTATION_HORIZONTAL;
                        } else {
                            // 如果 absDeltaX == absDeltaY，默认垂直优先 (或根据需求调整)
                            mScrollOrientation = ORIENTATION_VERTICAL;
                        }
                    }
                } else {
                    // 活动指针已失效
                    mActivePointerId = -1;
                    mScrollOrientation = ORIENTATION_NONE;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = -1; // 清除非活动指针
                mScrollOrientation = ORIENTATION_NONE;
                mTarget.requestDisallowInterceptTouchEvent(false);
                break;
        }
    }
}