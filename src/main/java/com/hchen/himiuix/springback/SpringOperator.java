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

public class SpringOperator {
    private final double dampingCoefficient; // 阻尼系数 (c/m 或 2 * ζ * ω)
    private final double stiffnessOverMass;  // 刚度与质量之比 (k/m 或 ω^2)

    /**
     * 创建一个新的 SpringOperator 实例
     *
     * @param dampingRatio  阻尼比 (ζ)。一个无量纲参数，描述振荡如何衰减
     *                      0: 无阻尼; (0,1): 欠阻尼; 1: 临界阻尼; >1: 过阻尼
     * @param naturalPeriod 固有振荡周期 (T)。在无阻尼情况下，完成一次完整振荡所需的时间
     *                      如果 period 为 0，可能会导致除零错误，应避免
     */
    public SpringOperator(float dampingRatio, float naturalPeriod) {
        if (naturalPeriod <= 0)
            throw new IllegalArgumentException("Natural period must be positive.");

        // 角频率 ω = 2π / T
        double angularFrequency = (2.0 * Math.PI) / naturalPeriod;
        // 张力 (或 k/m) = ω^2
        stiffnessOverMass = angularFrequency * angularFrequency;
        // 阻尼系数 (或 c/m) = 2 * ζ * ω
        dampingCoefficient = 2.0 * dampingRatio * angularFrequency;
    }

    /**
     * 根据当前状态使用简单的欧拉积分更新并返回弹簧系统的新速度
     *
     * @param currentVelocity 当前的速度
     * @param deltaTime       时间步长 (dt)
     * @param targetPosition  弹簧的目标位置或平衡位置
     * @param currentPosition 当前的位置
     * @return 更新后的速度
     */
    public double updateVelocity(double currentVelocity, float deltaTime, double targetPosition, double currentPosition) {
        double velocityDecayFactor = 1.0 - dampingCoefficient * deltaTime;
        double velocityIncreaseFromSpring = stiffnessOverMass * (targetPosition - currentPosition) * deltaTime;

        return currentVelocity * velocityDecayFactor + velocityIncreaseFromSpring;
    }
}