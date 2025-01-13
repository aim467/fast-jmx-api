package com.fast.jmx.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperatingSystemInfo implements Serializable {

    private String name;

    private String arch;

    private String availableProcessors;

    private String committedVirtualMemorySize;

    private String freePhysicalMemorySize;

    private String freeSwapSpaceSize;

    private String physicalMemoryUsagePercent;

    private String processCpuLoad;

    private String processCpuTime;

    private String systemCpuLoad;

    private String systemLoadAverage;

    private String totalPhysicalMemorySize;

    private String usedPhysicalMemorySize;

    private String totalSwapSpaceSize;

    private String version;
}
