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

import android.view.animation.AnimationUtils;

public class SpringScroller {
    private static final int ORIENTATION_HORIZONTAL = 1;
    private static final int ORIENTATION_VERTICAL = 2;
    private static final float MAX_FRAME_DELTA_SECONDS = 0.016f; // 约 60fps
    private static final float MIN_FRAME_DELTA_SECONDS = 0.001f; // 避免 deltaTime 为 0
    private static final float VALUE_THRESHOLD = 1.0f; // 判断是否到达平衡位置的阈值
    private static final double HIGH_VELOCITY_THRESHOLD = 5000.0;
    private static final float CRITICAL_DAMPING_RATIO = 1.0f;
    private static final float STANDARD_SPRING_PERIOD = 0.4f;
    private static final float SLOWER_SPRING_PERIOD_FOR_HIGH_VELOCITY = 0.55f;
    private double mCurrX; // 当前 X 位置
    private double mCurrY; // 当前 Y 位置
    private long mCurrentTime; // 当前动画时间戳
    private double mEndX; // X 方向的目标/结束位置
    private double mEndY; // Y 方向的目标/结束位置
    private boolean mFinished; // 动画是否已完成
    private int mFirstStep; // 一个特殊的第一步位置值，用于立即跳转
    private boolean mLastStep; // 是否正在执行最后一步(用于精确停止在目标位置)
    private int mOrientation; // 滚动方向 (1: 水平, 2: 垂直)
    private double mOriginStartX; // 初始的X开始位置 (用于 isAtEquilibrium 判断)
    private double mOriginStartY; // 初始的Y开始位置 (用于 isAtEquilibrium 判断)
    private double mOriginVelocity; // 初始速度 (用于 isAtEquilibrium 判断)
    private SpringOperator mSpringOperator; // 弹簧物理模拟器
    private long mStartTime; // 动画开始时间戳
    private double mStartX; // 当前计算步的 X 开始位置 (会更新)
    private double mStartY; // 当前计算步的 Y 开始位置 (会更新)
    private double mVelocity; // 当前速度

    public SpringScroller() {
        mFinished = true;
    }

    public boolean computeScrollOffset() {
        if (mSpringOperator == null || mFinished) {
            return false;
        }

        if (mFirstStep != 0) {
            handleFirstStep();
            return true; // 动画在第一步后仍在进行 (等待下一帧的物理计算)
        }

        if (mLastStep) {
            // 完成最后的设置并标记动画结束
            if (mOrientation == ORIENTATION_VERTICAL) mCurrY = mEndY;
            else mCurrX = mEndX;
            mFinished = true;
            mLastStep = false; // 重置标志
            return false; // 动画在此帧结束后真正完成
        }

        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        float deltaTime = (currentTime - mStartTime) / 1000.0f;
        mStartTime = currentTime;

        // 限制 deltaTime 范围
        if (deltaTime <= 0.0f) { // 通常是第一帧或时钟问题
            deltaTime = MIN_FRAME_DELTA_SECONDS; // 使用一个小的正时间步
        } else if (deltaTime > MAX_FRAME_DELTA_SECONDS) {
            deltaTime = MAX_FRAME_DELTA_SECONDS;
        }

        // 更新物理状态
        if (mOrientation == ORIENTATION_VERTICAL) {
            updatePhysicsState(deltaTime, mEndY, mStartY, true);
        } else {
            updatePhysicsState(deltaTime, mEndX, mStartX, false);
        }

        return true; // 动画仍在进行
    }

    private void handleFirstStep() {
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            mCurrX = mFirstStep;
            mStartX = mFirstStep;
        } else {
            mCurrY = mFirstStep;
            mStartY = mFirstStep;
        }
        mFirstStep = 0; // 清除标记
        // mStartTime 应该在这里也更新为当前时间，否则第一帧物理计算的 deltaTime 会很大
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    private void updatePhysicsState(float deltaTime, double targetEndPos, double currentStartPos, boolean isVertical) {
        // 更新速度
        double newVelocity = mSpringOperator.updateVelocity(mVelocity, deltaTime, targetEndPos, currentStartPos);
        // 更新位置 (使用上一帧的 startPos 和新计算的 newVelocity)
        double newCurrentPos = currentStartPos + deltaTime * newVelocity;

        if (isVertical) {
            mCurrY = newCurrentPos;
            mVelocity = newVelocity;
            if (isAtEquilibrium(mCurrY, mOriginStartY, targetEndPos)) {
                mLastStep = true;
                mCurrY = targetEndPos; // 精确设置
            } else {
                mStartY = mCurrY; // 为下一帧准备
            }
        } else {
            mCurrX = newCurrentPos;
            mVelocity = newVelocity;
            if (isAtEquilibrium(mCurrX, mOriginStartX, targetEndPos)) {
                mLastStep = true;
                mCurrX = targetEndPos; // 精确设置
            } else {
                mStartX = mCurrX; // 为下一帧准备
            }
        }
    }

    public final void forceStop() {
        mFinished = true;
        mFirstStep = 0;
    }

    public final int getCurrentX() {
        return (int) mCurrX;
    }

    public final int getCurrentY() {
        return (int) mCurrY;
    }

    public boolean isAtEquilibrium(double currentPosition, double initialStartPosition, double targetEquilibriumPosition) {
        double currentVelocity = mVelocity; // 获取当前速度
        double initialVelocity = mOriginVelocity; // 获取初始速度
        // 是否非常接近目标位置
        if (Math.abs(currentPosition - targetEquilibriumPosition) < VALUE_THRESHOLD) {
            return true;
        }

        // 是否已经越过平衡点
        boolean startedBeforeTarget = initialStartPosition < targetEquilibriumPosition;
        boolean startedAfterTarget = initialStartPosition > targetEquilibriumPosition;
        if (startedBeforeTarget && currentPosition >= targetEquilibriumPosition) { // 从左/下开始，现在到达或超过目标
            return true;
        }
        if (startedAfterTarget && currentPosition <= targetEquilibriumPosition) { // 从右/上开始，现在到达或超过目标
            return true;
        }

        // 特殊情况：如果从平衡点开始，并且速度已经反向
        return initialStartPosition == targetEquilibriumPosition
            && initialVelocity != 0 && Math.signum(currentVelocity) != Math.signum(initialVelocity);// 如果以上条件都不满足，则认为还未到达平衡状态
    }

    public final boolean isFinished() {
        return mFinished;
    }

    /**
     * 根据给定的初始参数启动一个 Fling 动画
     *
     * @param startX                     X轴的起始位置
     * @param targetX                    X轴的目标（平衡）位置
     * @param startY                     Y轴的起始位置
     * @param targetY                    Y轴的目标（平衡）位置
     * @param initialVelocity            沿主滚动轴的初始速度
     * @param orientation                滚动方向
     * @param disableHighSpeedAdjustment 如果为 true，则即使在高速情况下也使用标准（较快）的弹簧周期；
     *                                   如果为 false，则在高速时会使用稍慢的弹簧周期进行调整
     */
    public void scrollByFling(float startX, float targetX, float startY, float targetY, float initialVelocity, int orientation, boolean disableHighSpeedAdjustment) {
        mFinished = false;
        mLastStep = false;
        mFirstStep = 0;

        // 初始化位置
        mStartX = startX;
        mCurrX = startX;
        mOriginStartX = startX;
        mEndX = targetX;

        mStartY = startY;
        mCurrY = startY;
        mOriginStartY = startY;
        mEndY = targetY;

        // 初始化速度
        double vel = initialVelocity;
        mOriginVelocity = vel;
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

    public void setFirstStep(final int mFirstStep) {
        this.mFirstStep = mFirstStep;
    }
}