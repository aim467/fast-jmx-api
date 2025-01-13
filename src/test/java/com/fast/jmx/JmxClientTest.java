package com.fast.jmx;


import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class JmxClientTest {

    public static void main(String[] args) throws Exception {
//        VirtualMachine attach = VirtualMachine.attach(String.valueOf(20780));
//        String address = attach.startLocalManagementAgent();
//        JMXServiceURL serviceURL = new JMXServiceURL(address);
//        JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
//        MBeanServerConnection mbsc = connector.getMBeanServerConnection();
//
//        JmxService jmxService = new JmxService();
//        jmxService.getJvmRuntimeInfo(mbsc);



        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    }
}
