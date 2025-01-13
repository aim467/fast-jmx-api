package com.fast.jmx.domain;

import lombok.Data;

@Data
public class MemoryUsageBean {
    private String poolName;
    private long init;
    private long used;
    private long committed;
    private long max;
    private String type;
}
