package com.fast.jmx.domain;

import lombok.Data;

import java.lang.management.MemoryUsage;
import java.util.List;

@Data
public class MemoryUsageStatics {

    private MemoryUsage heapUsage;

    private MemoryUsage nonHeapUsage;

    private List<MemoryUsage> poolUsageList;

}
