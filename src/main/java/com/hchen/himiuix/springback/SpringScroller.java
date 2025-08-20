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
import static com.hchen.himiuix.springback.SpringBackLayout.VERTICAL;

import android.view.animation.AnimationUtils;

public class SpringScroller {
    private static final float MAX_FRAME_DELTA_SECONDS = 0.016f; // 约 60fps
    private static final float MIN_FRAME_DELTA_SECONDS = 0.001f; // 避免 deltaTime 为 0
    private static final float VALUE_THRESHOLD = 1.0f; // 判断是否到达平衡位置的阈值
    private static final double HIGH_VELOCITY_THRESHOLD = 5000.0;
    private static final float CRITICAL_DAMPING_RATIO = 1.0f;
    private static final float STANDARD_SPRING_PERIOD = 0.4f;
    private static final float SLOWER_SPRING_PERIOD_FOR_HIGH_VELOCITY = 0.55f;
    private SpringOperator mSpringOperator;
    private double mCurrX; // 当前 X 位置
    private double mCurrY; // 当前 Y 位置
    private long mStartTime; // 动画开始时间戳
    private double mEndX; // X 方向的目标/结束位置
    private double mEndY; // Y 方向的目标/结束位置
    private int mFirstStep; // 第一步位置值
    private boolean isLastStep; // 是否正在执行最后一步 (用于精确停止在目标位置)
    private boolean isFinished; // 动画是否已完成
    private int mOrientation; // 滚动方向 (1: 水平, 2: 垂直)
    private double mVelocity; // 当前速度
    private double mStartX; // 当前计算步的 X 开始位置 (会更新)
    private double mStartY; // 当前计算步的 Y 开始位置 (会更新)
    private double mInitialStartX; // 初始的 X 开始位置
    private double mInitialStartY; // 初始的 Y 开始位置
    private double mInitialVelocity; // 初始速度

    public SpringScroller() {
        isFinished = true;
    }

    public void setFirstStep(final int mFirstStep) {
        this.mFirstStep = mFirstStep;
    }

    public final int getCurrentX() {
        return (int) mCurrX;
    }

    public final int getCurrentY() {
        return (int) mCurrY;
    }

    public final void forceStop() {
        isFinished = true;
        mFirstStep = 0;
    }

    public final boolean isFinished() {
        return isFinished;
    }

    public boolean computeScrollOffset() {
        if (mSpringOperator == null || isFinished)
            return false;

        if (mFirstStep != 0) {
            handleFirstStep();
            return true;
        }

        if (isLastStep) {
            // 完成最后的设置并标记动画结束
            if (mOrientation == VERTICAL) mCurrY = mEndY;
            else mCurrX = mEndX;
            isFinished = true;
            isLastStep = false;
            return false;
        }

        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        float deltaTime = (currentTime - mStartTime) / 1000.0f;
        mStartTime = currentTime;

        // 限制 deltaTime 范围
        if (deltaTime <= 0.0f) deltaTime = MIN_FRAME_DELTA_SECONDS;
        else if (deltaTime > MAX_FRAME_DELTA_SECONDS) deltaTime = MAX_FRAME_DELTA_SECONDS;

        // 更新状态
        updatePhysicsState(deltaTime, mOrientation == VERTICAL);

        return true; // 动画仍在进行
    }

    private void handleFirstStep() {
        if (mOrientation == HORIZONTAL) {
            mCurrX = mFirstStep;
            mStartX = mFirstStep;
        } else {
            mCurrY = mFirstStep;
            mStartY = mFirstStep;
        }
        mFirstStep = 0;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    // 计算位置
    private void updatePhysicsState(float deltaTime, boolean isVertical) {
        double targetEndPos = isVertical ? mEndY : mEndX;
        double currentStartPos = isVertical ? mStartY : mStartX;
        double newVelocity = mSpringOperator.updateVelocity(mVelocity, deltaTime, currentStartPos, targetEndPos);
        double newCurrentPos = currentStartPos + deltaTime * newVelocity;

        if (isVertical) {
            mCurrY = newCurrentPos;
            mVelocity = newVelocity;
            if (isAtEquilibrium(mCurrY, mInitialStartY, targetEndPos)) {
                isLastStep = true;
                mCurrY = targetEndPos;
            } else mStartY = mCurrY;
        } else {
            mCurrX = newCurrentPos;
            mVelocity = newVelocity;
            if (isAtEquilibrium(mCurrX, mInitialStartX, targetEndPos)) {
                isLastStep = true;
                mCurrX = targetEndPos;
            } else mStartX = mCurrX;
        }
    }

    private boolean isAtEquilibrium(double currentPosition, double initialStartPosition, double targetEndPosition) {
        if (initialStartPosition < targetEndPosition && currentPosition > targetEndPosition) {
            return true;
        }

        if (initialStartPosition >= targetEndPosition || currentPosition <= targetEndPosition) {
            boolean velocityReversed = (initialStartPosition == targetEndPosition && Math.signum(mInitialVelocity) != Math.signum(currentPosition));
            boolean closeEnough = Math.abs(currentPosition - targetEndPosition) < VALUE_THRESHOLD;
            return velocityReversed || closeEnough;
        }

        return true;
    }

    // 启动 Fling 动画
    public void scrollByFling(float startX, float targetX, float startY, float targetY, float initialVelocity, int orientation, boolean disableHighSpeedAdjustment) {
        isFinished = false;
        isLastStep = false;
        mFirstStep = 0;

        // 初始化位置
        mStartX = startX;
        mCurrX = startX;
        mInitialStartX = startX;
        mEndX = targetX;

        mStartY = startY;
        mCurrY = startY;
        mInitialStartY = startY;
        mEndY = targetY;

        // 初始化速度
        double vel = initialVelocity;
        mInitialVelocity = vel;
        mVelocity = vel;

        // 初始化 SpringOperator
        float springPeriodToUse;
        if (Math.abs(vel) > HIGH_VELOCITY_THRESHOLD && !disableHighSpeedAdjustment) {
            // 速度高 且 允许高速调整 (即 disableHighSpeedAdjustment 为 false)
            springPeriodToUse = SLOWER_SPRING_PERIOD_FOR_HIGH_VELOCITY;
        } else {
            // 速度不高 或 禁止了高速调整
            springPeriodToUse = STANDARD_SPRING_PERIOD;
        }
        mSpringOperator = new SpringOperator(CRITICAL_DAMPING_RATIO, springPeriodToUse);

        // 设置方向和开始时间
        mOrientation = orientation;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }
}