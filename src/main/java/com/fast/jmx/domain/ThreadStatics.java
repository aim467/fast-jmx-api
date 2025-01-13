package com.fast.jmx.domain;

import lombok.Data;

@Data
public class ThreadStatics {

    private long threadCount;

    private long peakThreadCount;

    private long totalStartedThreadCount;

    private long daemonThreadCount;

    private long liveThreadCount;

    private long threadCpuTime;

    private long threadUserTime;

    private long monitorDeadlockCount;

    private long deadlockCount;
}
