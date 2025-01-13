package com.fast.jmx.controller;

import com.fast.jmx.cache.FastJmxCache;
import com.fast.jmx.domain.Monitor;
import com.fast.jmx.domain.R;
import com.fast.jmx.service.JmxService;
import com.fast.jmx.service.VmService;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.management.MBeanServerConnection;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/jmx")
public class JmxController {

    @Resource
    private JmxService jmxService;

    @Resource
    private VmService vmService;

    @GetMapping("/list")
    public R<List<Monitor>> getLocalMonitorList() {
        List<Monitor> monitorList = new ArrayList<>(FastJmxCache.monitorMap.values());
        return R.ok(monitorList);
    }


    /**
     * 添加Jmx并且连接
     * @param monitor
     * @return
     */
    @PostMapping("/add")
    public R addMonitor(@RequestBody Monitor monitor) {
        String md5Str = monitor.getName() + monitor.getJmxUrl();
        String monitorId = DigestUtils.md5DigestAsHex(md5Str.getBytes());
        monitor.setMonitorId(monitorId);
        monitor.setType(2);
        if (FastJmxCache.monitorMap.get(monitorId) != null) {
            return R.error("已经存在相同的jmx监控");
        }

        jmxService.connect(monitor);
        return R.ok("添加Jmx地址成功", monitorId);
    }


    @GetMapping("/connect")
    public R connect(String monitorId) {
        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
            return R.error("未找到对应监控信息");
        }

        if (FastJmxCache.connectionMap.get(monitorId) != null) {
            return R.ok("此Jmx已连接");
        }

        if (monitor.getType() == 1) {
            vmService.getJmxConnector(monitor);
        } else {
            jmxService.connect(monitor);
        }
        return R.ok("首次连接成功");
    }

    @GetMapping("/basic")
    private R basic(String monitorId) throws Exception {
        if (monitorId == null) {
            return R.error("请先连接指定的jmx");
        }

        Monitor monitor = FastJmxCache.monitorMap.get(monitorId);
        if (monitor == null) {
            return R.error("未找到对应监控信息");
        }
        if (FastJmxCache.connectionMap.get(monitorId) == null) {
            return R.ok("当前进程未连接jmx");
        }

        MBeanServerConnection connection = FastJmxCache.connectionMap.get(monitorId);
        return R.ok(jmxService.getBasicInfo(connection));
    }
}
