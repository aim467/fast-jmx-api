package com.fast.jmx.domain;

import lombok.Data;

@Data
public class BasicInfo {

    private OperatingSystemInfo operatingSystemInfo;

    private MemoryInfo memoryInfo;

    private JvmRuntimeInfo jvmRuntimeInfo;

    private ClassLoaderInfo classLoaderInfo;

    private CompilationInfo compilationInfo;

    private GarbageCollectionInfo garbageCollectionInfo;
}
