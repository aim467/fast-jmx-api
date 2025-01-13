package com.fast.jmx.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JvmRuntimeInfo {

    private String bootClassPath;

    private String bootClassPathSupported;

    private String classPath;

    private List<String> inputArguments;

    private String libraryPath;

    private String managementSpecVersion;

    private String name;

    private String specName;

    private String specVendor;

    private String specVersion;

    private String startTime;

    private Map<String, Object> systemProperties;

    private String uptime;

    private String vmName;

    private String vmVendor;

    private String vmVersion;

}
