package com.fast.jmx.controller;

import com.fast.jmx.cache.FastJmxCache;
import com.fast.jmx.domain.MemoryUsageBean;
import com.fast.jmx.domain.Monitor;
import com.fast.jmx.domain.R;
import com.fast.jmx.service.JmxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/jmx/memory")
public class MemoryController {

    @Resource
    private JmxService jmxService;


    @GetMapping("/")
    public R memoryDetail(String monitorId) throws IOException {
        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
            return R.error("未找到对应监控信息");
        }
        if (FastJmxCache.connectionMap.get(monitorId) == null) {
            return R.ok("当前进程未连接jmx");
        }

        MBeanServerConnection connection = FastJmxCache.connectionMap.get(monitorId);
        List<MemoryUsageBean> memoryUsageStatics = new ArrayList<>();

        memoryUsageStatics.add(jmxService.getMemoryInfo(connection, true));
        memoryUsageStatics.add(jmxService.getMemoryInfo(connection, false));
        memoryUsageStatics.addAll(jmxService.getManagementPoolInfo(connection));
        return R.ok(memoryUsageStatics);
    }

}
