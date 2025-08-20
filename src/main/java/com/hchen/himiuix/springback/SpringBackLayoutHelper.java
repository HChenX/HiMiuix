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

import static com.hchen.himiuix.springback.SpringBackLayout.HORIZONTAL;
import static com.hchen.himiuix.springback.SpringBackLayout.UNCHECK_ORIENTATION;
import static com.hchen.himiuix.springback.SpringBackLayout.VERTICAL;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * SpringBack 帮助
 * <p>
 * 改编自 HyperOS 2
 *
 * @author 焕晨HChen
 */
public class SpringBackLayoutHelper {
    private final Rect mTargetBoundsInWindow = new Rect();
    float mInitialDownX; // 手指按下时的 X 坐标
    float mInitialDownY; // 手指按下时的 Y 坐标
    int mScrollOrientation;
    int mActivePointerId = -1;
    private final int mTouchSlop;
    private final ViewGroup mTarget;

    public SpringBackLayoutHelper(ViewGroup target) {
        mTarget = target;
        mTouchSlop = ViewConfiguration.get(target.getContext()).getScaledTouchSlop();
    }

    public boolean isTouchInTarget(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(event.getPointerId(0));
        if (pointerIndex < 0) return false;

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
                mActivePointerId = event.getPointerId(0);
                int downPointerIndex = event.findPointerIndex(mActivePointerId);
                if (downPointerIndex >= 0) {
                    mInitialDownX = event.getX(downPointerIndex);
                    mInitialDownY = event.getY(downPointerIndex);
                } else mActivePointerId = -1;

                mScrollOrientation = UNCHECK_ORIENTATION;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == -1) break;

                int movePointerIndex = event.findPointerIndex(mActivePointerId);
                if (movePointerIndex >= 0) {
                    float currentX = event.getX(movePointerIndex);
                    float currentY = event.getY(movePointerIndex);

                    float deltaX = currentX - mInitialDownX;
                    float deltaY = currentY - mInitialDownY;

                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);

                    if (absDeltaX > mTouchSlop || absDeltaY > mTouchSlop) {
                        if (absDeltaX > absDeltaY) mScrollOrientation = HORIZONTAL;
                        else mScrollOrientation = VERTICAL;
                    }
                } else {
                    mActivePointerId = -1;
                    mScrollOrientation = UNCHECK_ORIENTATION;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = -1;
                mScrollOrientation = UNCHECK_ORIENTATION;
                mTarget.requestDisallowInterceptTouchEvent(false);
                break;
        }
    }
}