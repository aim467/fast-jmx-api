package com.fast.jmx.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Monitor implements Serializable {

    private String monitorId;

    private String name;

    private String pid;

    private String jmxUrl;

    /**
     * local 1
     * remote 2
     */
    private Integer type;

}
