package com.fast.jmx.controller;

import com.fast.jmx.cache.FastJmxCache;
import com.fast.jmx.domain.Monitor;
import com.fast.jmx.domain.R;
import com.fast.jmx.domain.ThreadStackTrace;
import com.fast.jmx.domain.ThreadStatics;
import com.fast.jmx.service.JmxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/jmx/thread")
public class ThreadController {

    @Resource
    private JmxService jmxService;

    @GetMapping("/list")
    public R threadList(String monitorId) throws IOException {
        if (monitorId == null) {
            return R.error("监控ID不能为z空");
        }

        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
            return R.error("未找到对应监控信息");
        }

        MBeanServerConnection connection = FastJmxCache.connectionMap.get(monitorId);
        if (connection == null) {
            return R.ok("当前进程未连接jmx");
        }

        return R.ok(jmxService.getThreadList(connection));
    }


    @GetMapping("/stack")
    public R<List<ThreadStackTrace>> exportThread(String monitorId, long threadId) throws IOException {
        if (monitorId == null) {
            return R.error("监控ID不能为z空");
        }

        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
            return R.error("未找到对应监控信息");
        }

        MBeanServerConnection connection = FastJmxCache.connectionMap.get(monitorId);
        if (connection == null) {
            return R.ok("当前进程未连接jmx");
        }
        return R.ok(jmxService.getThreadStackTrace(connection, threadId));
    }

    @GetMapping("/dump")
    public R dumpThread(String monitorId) throws IOException, MalformedObjectNameException,
            ReflectionException, InstanceNotFoundException, MBeanException {
        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
        }

        MBeanServerConnection connection = FastJmxCache.connectionMap.get(monitorId);
        if (connection == null) {
            return R.error("没有连接到jmx");
        }

        // 假设DiagnosticCommand相关的MBean名称是这个，实际需要根据应用程序来确定
        ObjectName mbeanName = new ObjectName("com.sun.management:type=DiagnosticCommand");
        // 定义调用threadPrint操作的参数（如果有参数的话），这里假设没有参数
        Object[] params = new Object[]{null};
        String[] signature = new String[]{"[Ljava.lang.String;"};
        // 调用threadPrint操作并获取返回值
        String result = (String) connection.invoke(mbeanName, "threadPrint", params, signature);
        return R.ok("获取线程堆栈数据成功", result);
    }

    @GetMapping("/info")
    public R<ThreadStatics> threadInfo(String monitorId) throws IOException {
        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
        }

        MBeanServerConnection connection = FastJmxCache.connectionMap.get(monitorId);
        if (connection == null) {
            return R.error("没有连接到jmx");
        }
        return R.ok(jmxService.getThreadStatics(connection));
    }

    @GetMapping("/deadLock")
    public R<List<Long>> deadLock(String monitorId) throws IOException {
        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
        }

        MBeanServerConnection connection = FastJmxCache.connectionMap.get(monitorId);
        if (connection == null) {
            return R.error("没有连接到jmx");
        }
        return R.ok(jmxService.findDeadLock(connection));
    }
}
