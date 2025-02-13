package com.fast.jmx.service;

import com.fast.jmx.cache.FastJmxCache;
import com.fast.jmx.domain.Monitor;
import com.fast.jmx.exception.FastJmxException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class VmService {

    @Resource
    private JmxService jmxService;

    public void getLocalMonitorList() {
        List<VirtualMachineDescriptor> descriptors = VirtualMachine.list();
        for (VirtualMachineDescriptor descriptor : descriptors) {
            Monitor monitor = new Monitor();
            String pidName = descriptor.id() + descriptor.displayName();
            String monitorId = DigestUtils.md5DigestAsHex(pidName.getBytes());
            monitor.setMonitorId(monitorId);
            monitor.setPid(descriptor.id());
            monitor.setName(StringUtils.hasText(descriptor.displayName()) ? descriptor.displayName() : "exit");
            monitor.setType(1);
            FastJmxCache.monitorMap.putIfAbsent(monitor.getMonitorId(), monitor);
        }
    }


    public void getJmxConnector(Monitor monitor) {
        VirtualMachine attach;
        try {
            attach = VirtualMachine.attach(monitor.getPid());
        } catch (IOException | AttachNotSupportedException e) {
            throw new FastJmxException("附着进程失败");
        }
        String address;

        try {
            // 获取 JVM 的 MBeanServer 地址
            address = attach.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            if (!StringUtils.hasText(address)) {
                address = attach.startLocalManagementAgent();
            }
        } catch (IOException e) {
            throw new FastJmxException("开启jmx失败");
        }
        monitor.setJmxUrl(address);
        jmxService.connect(monitor);
    }
}

