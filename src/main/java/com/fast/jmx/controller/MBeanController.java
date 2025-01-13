package com.fast.jmx.controller;

import com.fast.jmx.cache.FastJmxCache;
import com.fast.jmx.domain.MBeanNode;
import com.fast.jmx.domain.Monitor;
import com.fast.jmx.domain.R;
import com.fast.jmx.service.JmxService;
import com.fast.jmx.service.VmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jmx/mbean")
public class MBeanController {

    @Autowired
    private JmxService jmxService;
    @Autowired
    private VmService vmService;

    @GetMapping("/list")
    public R<List<MBeanNode>> mbeanTree(String monitorId) {

        if (monitorId == null) {
            return R.error("监控ID不能为空");
        }

        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
            return R.error("未找到对应监控信息");
        }

        MBeanServerConnection connection;
        if (FastJmxCache.connectionMap.get(monitorId) != null) {
            connection = FastJmxCache.connectionMap.get(monitorId);
            List<MBeanNode> nodes = jmxService.buildMBeanList(connection);
            return R.ok(nodes);
        }

        if (monitor.getType() == 1) {
            vmService.getJmxConnector(monitor);
        } else {
            jmxService.connect(monitor);
        }

        connection = FastJmxCache.connectionMap.get(monitorId);
        List<MBeanNode> nodes = jmxService.buildMBeanList(connection);
        return R.ok(nodes);
    }

    @GetMapping("/detail")
    public R mbeanDetail(String monitorId, String mbeanName) throws ReflectionException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, IOException {
        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
            return R.error("未找到对应监控信息");
        }

        MBeanServerConnection connection = FastJmxCache.connectionMap.get(monitorId);
        if (connection == null) {
            return R.error("未找到对应连接信息");
        }

        Map<String, Object> objectMap = jmxService.extractMBeanAttributes(connection, mbeanName);
        return R.ok(objectMap);
    }
}
