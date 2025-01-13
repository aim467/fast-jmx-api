package com.fast.jmx.domain;

import lombok.Data;

@Data
public class ClassLoaderInfo {
    private long loadedClassCount;
    private long totalLoadedClassCount;
    private long unloadedClassCount;
}
