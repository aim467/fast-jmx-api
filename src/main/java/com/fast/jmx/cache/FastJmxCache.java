package com.fast.jmx.cache;

import com.fast.jmx.domain.Monitor;

import javax.management.MBeanServerConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FastJmxCache {
    public static Map<String, Monitor> monitorMap = new ConcurrentHashMap<>(16);

    public static Map<String, MBeanServerConnection> connectionMap = new ConcurrentHashMap<>(16);

    public static Map<String, String> tokenMap = new ConcurrentHashMap<>(16);
}
