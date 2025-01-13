package com.fast.jmx.domain;

import lombok.Data;

@Data
public class MemoryInfo {

    private MemoryUsageBean heapMemoryUsage;

    private MemoryUsageBean nonHeapMemoryUsage;
}
