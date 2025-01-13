package com.fast.jmx.domain.oshi;

import lombok.Data;

/**
 * 系统运行时信息
 *
 */
@Data
public class OSRuntimeInfo {

    /**
     * 时刻
     */
    private String timestamp;

    /**
     * cpu使用率
     */
    private double cpuUsage;

    /**
     * cpu基准速度（GHz）
     */
    private String cpuMaxFreq;

    /**
     * cpu当前速度（GHz）
     */
    private String cpuCurrentFreq;

    /**
     * 总内存
     */
    private long totalMemory;

    /**
     * 使用内存
     */
    private long usedMemory;

    /**
     * 可用虚拟总内存
     */
    private long swapTotalMemory;

    /**
     * 已用虚拟内存
     */
    private long swapUsedMemory;
}
