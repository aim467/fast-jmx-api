package com.fast.jmx;

import com.alibaba.fastjson2.JSON;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class JMXClient {

    private String url;

    private JMXConnector connector;
    public MBeanServerConnection connection;

    // 构造方法，指定 JMX 服务的 URL
    public JMXClient(String url) {
        this.url = url;
    }

    // 连接到 JMX 服务器
    public void connect() throws IOException {
        JMXServiceURL serviceURL = new JMXServiceURL(this.url);
        connector = JMXConnectorFactory.connect(serviceURL);
        connection = connector.getMBeanServerConnection();
        System.out.println("连接成功: " + this.url);
    }

    // 获取所有的域名（domains）
    public String[] getDomains() throws IOException {
        checkConnection();
        return connection.getDomains();
    }

    // 获取指定 domain 下的所有 MBeans
    public Set<ObjectName> getMBeans(String domain) throws IOException, MalformedObjectNameException {
        checkConnection();
        String domainQuery = domain + ":*";
        ObjectName query = new ObjectName(domainQuery);
        return connection.queryNames(query, null);
    }

    // 关闭 JMX 连接
    public void close() throws IOException {
        if (connector != null) {
            connector.close();
            System.out.println("连接关闭");
        }
    }

    // 获取指定 MBean 的详细信息
    public void printMBeanDetail(ObjectName mbean) throws Exception {
        checkConnection();
        MBeanInfo mbeanInfo = connection.getMBeanInfo(mbean);
        printMBeanInfo(mbeanInfo);
        getMBeanAttributes(mbean);
    }

    // 获取指定 MBean 的所有属性的详细信息
    public void getMBeanAttributes(ObjectName mbean) throws Exception {
        checkConnection();
        MBeanInfo mbeanInfo = connection.getMBeanInfo(mbean);
        MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
        System.out.println("MBean: " + mbean.getCanonicalName() + " 的属性:");
        for (MBeanAttributeInfo attribute : attributes) {
            boolean readable = attribute.isReadable();
            Object a;
            if (readable) {
                try {
                    a = connection.getAttribute(mbean, attribute.getName());
                } catch (Exception e) {
                    a = "不可用";
                }
            } else {
                a = "不可读";
            }
            System.out.println("---------------------------------------------------");
            System.out.println("属性名: " + attribute.getName());
            System.out.println("  类型: " + attribute.getType());
            System.out.println("  描述: " + attribute.getDescription());
            System.out.println("  是否可读: " + attribute.isReadable());
            System.out.println("  是否可写: " + attribute.isWritable());
            System.out.println("---------------------------------------------------");
            System.out.println(JSON.toJSONString(a));
        }
    }

    private void checkConnection() throws IllegalStateException {
        if (connection == null) {
            throw new IllegalStateException("没有连接到 JMX 服务器。请先调用 connect() 方法。");
        }
    }

    private void printMBeanInfo(MBeanInfo mbeanInfo) {
        System.out.println("MBean: " + mbeanInfo.getClassName());
        System.out.println("描述: " + mbeanInfo.getDescription());
        printAttributes(mbeanInfo.getAttributes());
        printOperations(mbeanInfo.getOperations());
        printNotifications(mbeanInfo.getNotifications());
    }

    private void printAttributes(MBeanAttributeInfo[] attributes) {
        System.out.println("属性:");
        Arrays.stream(attributes).forEach(attribute ->
                System.out.println("  " + attribute.getName() + " (" + attribute.getType() + "): " + attribute.getDescription()));
    }

    private void printOperations(MBeanOperationInfo[] operations) {
        System.out.println("操作:");
        Arrays.stream(operations).forEach(operation ->
                System.out.println("  " + operation.getName() + " (" + operation.getReturnType() + "): " + operation.getDescription()));
    }

    private void printNotifications(MBeanNotificationInfo[] notifications) {
        System.out.println("通知:");
        Arrays.stream(notifications).forEach(notification ->
                System.out.println("  " + notification.getName() + ": " + notification.getDescription()));
    }
}