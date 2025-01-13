package com.fast.jmx.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThreadInfo implements Serializable {

    private Long threadId;

    private String threadName;

    private String threadState;

    private Boolean isNative;

    private Boolean isSuspended;

    private Long blockedCount;

    private Long blockedTime;

    private Long waitedCount;

    private Long waitedTime;
}
