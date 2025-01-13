package com.fast.jmx.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThreadStackTrace implements Serializable {

    private String declaringClass;

    private String methodName;

    private String fileName;

    private int lineNumber;

    private boolean isNative;
}
