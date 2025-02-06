package com.fast.jmx.service;

import com.alibaba.fastjson2.JSON;
import com.fast.jmx.cache.FastJmxCache;
import com.fast.jmx.domain.BasicInfo;
import com.fast.jmx.domain.ClassLoaderInfo;
import com.fast.jmx.domain.CompilationInfo;
import com.fast.jmx.domain.JvmRuntimeInfo;
import com.fast.jmx.domain.MBeanNode;
import com.fast.jmx.domain.MemoryInfo;
import com.fast.jmx.domain.MemoryUsageBean;
import com.fast.jmx.domain.Monitor;
import com.fast.jmx.domain.OperatingSystemInfo;
import com.fast.jmx.domain.ThreadStackTrace;
import com.fast.jmx.domain.ThreadStatics;
import com.fast.jmx.domain.ThreadInfo;
import com.fast.jmx.exception.FastJmxException;
import com.fast.jmx.utils.FastJmxUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JmxService {

    /**
     * 连接Jmx并且存入缓存
     * @param monitor
     */
    public void connect(Monitor monitor) {
        JMXServiceURL url;
        try {
            url = new JMXServiceURL(monitor.getJmxUrl());
        } catch (MalformedURLException e) {
            throw new FastJmxException("jmx连接失败");
        }

        JMXConnector jmxc;
        try {
            jmxc = JMXConnectorFactory.connect(url, null);
        } catch (IOException e) {
            throw new FastJmxException("jmx factory 创建失败");
        }
        MBeanServerConnection mbsc;
        try {
            mbsc = jmxc.getMBeanServerConnection();
        } catch (IOException e) {
            throw new FastJmxException("mbean 连接创建失败");
        }
        FastJmxCache.monitorMap.put(monitor.getMonitorId(), monitor);
        FastJmxCache.connectionMap.put(monitor.getMonitorId(), mbsc);
    }


    /**
     * 提取指定 MBean 的所有属性名和属性值，并以 Map 形式返回
     *
     * @param connection MBeanServerConnection，用于访问 MBean
     * @param mbeanName  指定的 MBean 名称
     * @return 包含属性名和属性值的 Map
     */
    public Map<String, Object> extractMBeanAttributes(MBeanServerConnection connection, String mbeanName) throws MalformedObjectNameException, ReflectionException, InstanceNotFoundException, IntrospectionException, IOException {
        Map<String, Object> attributesMap = new HashMap<>();

        // 获取指定的 MBean 对象
        ObjectName objectName = new ObjectName(mbeanName);

        // 获取 MBean 的所有属性
        MBeanInfo mBeanInfo = connection.getMBeanInfo(objectName);
        MBeanAttributeInfo[] attributeInfos = mBeanInfo.getAttributes();

        // 提取每个属性的名称和值
        for (MBeanAttributeInfo attributeInfo : attributeInfos) {
            String attributeName = attributeInfo.getName();
            try {
                Object attributeValue = connection.getAttribute(objectName, attributeName);
                if (attributeValue.getClass().isArray()) {
                    int length = Array.getLength(attributeValue);
                    List<Object> arrayData = new ArrayList<>();
                    for (int i = 0; i < length; i++) {
                        Object item = Array.get(attributeValue, i);
                        if (item instanceof CompositeData) {
                            arrayData.add(extractFromCompositeData((CompositeData) item));
                        } else if (item instanceof TabularData) {
                            arrayData.add(extractFromTabularData((TabularData) item));
                        } else {
                            arrayData.add(item);
                        }
                    }
                    attributesMap.put(attributeName, arrayData);
                } else if (attributeValue instanceof CompositeData) {
                    attributesMap.put(attributeName, extractFromCompositeData((CompositeData) attributeValue));
                } else if (attributeValue instanceof TabularData) {
                    attributesMap.put(attributeName, extractFromTabularData((TabularData) attributeValue));
                } else if (attributeValue instanceof ObjectName) {
                    // 处理其他类型的数据
                    // do nothing
                } else {
                    attributesMap.put(attributeName, attributeValue);
                }
            } catch (Exception e) {
                log.error("获取 {} 的属性失败", attributeName);
                attributesMap.put(attributeName, null);
            }
        }

        return attributesMap;
    }

    /**
     * 提取 TabularData 的所有行，并返回为 Map，每行作为单独的键值对
     *
     * @param tabularData TabularData 实例
     * @return 包含所有行数据的 Map，每行作为一个键值对
     */
    public Map<String, Object> extractFromTabularData(TabularData tabularData) {
        Map<String, Object> tabularDataMap = new HashMap<>();
        // 遍历 TabularData 中的每一行（CompositeData）
        for (Object key : tabularData.keySet()) {
            // key 是 List<?> 类型的，表示一行的键
            List<?> compositeKey = (List<?>) key;

            // 使用 compositeKey 获取每行的 CompositeData
            CompositeData rowData = tabularData.get(compositeKey.toArray());

            // 将每行的 CompositeData 递归提取为 Map
            tabularDataMap.put(compositeKey.toString(), extractFromCompositeData(rowData));
        }

        return tabularDataMap;
    }

    public Map<String, Object> extractFromCompositeData(CompositeData compositeData) {
        Map<String, Object> compositeDataMap = new HashMap<>();

        CompositeType compositeType = compositeData.getCompositeType();
        Set<String> keys = compositeType.keySet();

        for (String key : keys) {
            Object value = compositeData.get(key);
            if (value instanceof CompositeData) {
                // 如果子数据也是 CompositeData，递归处理
                compositeDataMap.put(key, extractFromCompositeData((CompositeData) value));
            } else {
                compositeDataMap.put(key, value);
            }
        }

        return compositeDataMap;
    }

    /**
     * 构建MBean 树形结构视图
     * @param connection
     * @return
     */
    public List<MBeanNode> buildMBeanList(MBeanServerConnection connection) {
        String[] domains = new String[0];
        try {
            domains = connection.getDomains();
        } catch (IOException e) {
            throw new FastJmxException("获取domain失败");
        }

        // 树形结构列表
        List<MBeanNode> nodes = new ArrayList<>();

        for (String domain : domains) {
            MBeanNode mBeanNode = new MBeanNode();
            mBeanNode.setName(domain);
            mBeanNode.setNodes(extractMBeanTree(connection, domain));
            nodes.add(mBeanNode);
        }
        return nodes;
    }


    /**
     * 提取指定 domain 下的所有 MBean，并构建 MBeanNode 树
     *
     * @param connection MBeanServerConnection，用于访问 MBean
     * @param domain     指定的 domain 名称
     * @return MBeanNode 树的根节点
     * @throws Exception 如果发生错误
     */
    public List<MBeanNode> extractMBeanTree(MBeanServerConnection connection, String domain) {
        MBeanNode root = new MBeanNode();
        root.setDomain(domain);
        root.setType("root");
        root.setName(domain);

        Set<ObjectName> mbeans;
        try {
            // 获取指定 domain 下的所有 MBean 名称
            mbeans = connection.queryNames(new ObjectName(domain + ":*"), null);
        } catch (IOException | MalformedObjectNameException e) {
            throw new FastJmxException("获取MBeanList失败");
        }

        // 临时存储所有 MBean 的节点，按 type 分类
        Map<String, List<MBeanNode>> typeMap = new HashMap<>();

        // 1. 组装所有的 MBeanNode，并按 type 进行分组
        for (ObjectName mbeanName : mbeans) {
            // 获取 MBean 的属性
            String type = mbeanName.getKeyProperty("type");
            String name = mbeanName.getKeyProperty("name");
            String canonicalName = mbeanName.getCanonicalName();

            // 如果 type 或 name 不存在，使用默认值
            if (type == null) {
                type = "Unknown";
            }
            if (!StringUtils.hasText(name)) {
                name = canonicalName;
            }

            // 构建 MBeanNode 节点
            MBeanNode node = new MBeanNode(type, name, domain, canonicalName);

            // 将节点按 type 分组
            typeMap.computeIfAbsent(type, k -> new ArrayList<>()).add(node);
        }

        // 2. 处理分组：根据分组结果，决定是否创建父节点
        for (Map.Entry<String, List<MBeanNode>> entry : typeMap.entrySet()) {
            String type = entry.getKey();
            List<MBeanNode> nodes = entry.getValue();

            if (nodes.size() == 1) {
                // 如果分组只有一个节点，直接将该节点添加到根节点
                root.addNode(nodes.get(0));
            } else {
                // 如果分组有多个节点，创建一个新的父节点
                MBeanNode typeNode = new MBeanNode(type, type, domain, null);
                for (MBeanNode node : nodes) {
                    typeNode.addNode(node); // 将分组的子节点添加到父节点的 nodes 中
                }
                root.addNode(typeNode); // 将父节点添加到根节点
            }
        }
        return root.getNodes(); // 返回根节点的所有子节点
    }

    public List<ThreadInfo> getThreadList(MBeanServerConnection connection) throws IOException {

        // 获取 ThreadMXBean
        ThreadMXBean threadMXBean = ManagementFactory.newPlatformMXBeanProxy(connection, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
        // 获取所有活动线程的ID
        java.lang.management.ThreadInfo[] allThreads = threadMXBean.dumpAllThreads(true, true);
        List<ThreadInfo> threadInfos = new ArrayList<>();
        // 遍历每个线程ID并获取其详细信息
        for (java.lang.management.ThreadInfo threadInfo : allThreads) {
            // 输出线程的ID和名称
            if (threadInfo != null) {
                ThreadInfo thread = new ThreadInfo();
                thread.setThreadId(threadInfo.getThreadId());
                thread.setThreadName(threadInfo.getThreadName());
                String state = threadInfo.getThreadState().name();
                switch (state) {
                    case "BLOCKED":
                        state = "阻塞";
                        break;
                    case "RUNNABLE":
                        state = "运行";
                        break;
                    case "TIMED_WAITING":
                        state = "超时等待";
                        break;
                    case "TERMINATED":
                        state = "已终止";
                        break;
                    case "WAITING":
                        state = "等待";
                        break;
                    default:
                        state = "未知";
                }
                thread.setThreadState(state);
                thread.setIsSuspended(threadInfo.isSuspended());
                thread.setIsNative(threadInfo.isInNative());
                thread.setBlockedTime(threadInfo.getBlockedTime());
                thread.setBlockedCount(threadInfo.getBlockedCount());
                thread.setWaitedTime(threadInfo.getWaitedTime());
                thread.setWaitedCount(threadInfo.getWaitedCount());
                threadInfos.add(thread);
            }
        }
        return threadInfos;
    }

    // 获取堆内存和非堆内存的信息
    public MemoryUsageBean getMemoryInfo(MBeanServerConnection connection, boolean isHeap) throws IOException {
        MemoryMXBean memoryMXBean = ManagementFactory.newPlatformMXBeanProxy(connection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
        MemoryUsage memoryUsage = isHeap ? memoryMXBean.getHeapMemoryUsage() : memoryMXBean.getNonHeapMemoryUsage();
        MemoryUsageBean memoryUsageBean = new MemoryUsageBean();
        memoryUsageBean.setPoolName(isHeap ? "Heap" : "NonHeap");
        memoryUsageBean.setInit(memoryUsage.getInit());
        memoryUsageBean.setUsed(memoryUsage.getUsed());
        memoryUsageBean.setCommitted(memoryUsage.getCommitted());
        memoryUsageBean.setMax(memoryUsage.getMax());
        memoryUsageBean.setType(isHeap ? "Heap" : "NonHeap");
        return memoryUsageBean;
    }

    // 获取ManagementPool的内存信息
    public List<MemoryUsageBean> getManagementPoolInfo(MBeanServerConnection connection) throws IOException {
        List<MemoryPoolMXBean> list = ManagementFactory.getPlatformMXBeans(connection, MemoryPoolMXBean.class);
        List<MemoryUsageBean> memoryUsageList = new ArrayList<>();
        for (MemoryPoolMXBean memoryPoolMXBean : list) {
            MemoryUsageBean memoryUsageBean = new MemoryUsageBean();
            memoryUsageBean.setPoolName(memoryPoolMXBean.getName());
            memoryUsageBean.setInit(memoryPoolMXBean.getUsage().getInit());
            memoryUsageBean.setUsed(memoryPoolMXBean.getUsage().getUsed());
            memoryUsageBean.setCommitted(memoryPoolMXBean.getUsage().getCommitted());
            memoryUsageBean.setMax(memoryPoolMXBean.getUsage().getMax());
            memoryUsageBean.setType(memoryPoolMXBean.getType().name());
            memoryUsageList.add(memoryUsageBean);
        }
        return memoryUsageList;
    }

    public List<ThreadStackTrace> getThreadStackTrace(MBeanServerConnection connection, long threadId) throws IOException {
        ThreadMXBean threadMXBean = ManagementFactory.newPlatformMXBeanProxy(connection, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);

        java.lang.management.ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, Integer.MAX_VALUE);
        List<ThreadStackTrace> threadStackTraces = new ArrayList<>();
        if (threadInfo == null) {
            return threadStackTraces;
        }

        if (threadInfo.getStackTrace().length == 0) {
            return threadStackTraces;
        }

        for (StackTraceElement stackTraceElement : threadInfo.getStackTrace()) {
            ThreadStackTrace threadStackTrace = new ThreadStackTrace();
            threadStackTrace.setDeclaringClass(stackTraceElement.getClassName());
            threadStackTrace.setMethodName(stackTraceElement.getMethodName());
            threadStackTrace.setFileName(stackTraceElement.getFileName());
            threadStackTrace.setLineNumber(stackTraceElement.getLineNumber());
            threadStackTrace.setNative(stackTraceElement.isNativeMethod());
            threadStackTraces.add(threadStackTrace);
        }
        return threadStackTraces;
    }

    public ThreadStatics getThreadStatics(MBeanServerConnection connection) throws IOException {
        ThreadMXBean threadMXBean = ManagementFactory.newPlatformMXBeanProxy(connection, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);

        ThreadStatics threadStatics = new ThreadStatics();
        threadStatics.setThreadCount(threadMXBean.getThreadCount());
        threadStatics.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        threadStatics.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        threadStatics.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        threadStatics.setLiveThreadCount(threadMXBean.getThreadCount());
        threadStatics.setThreadCpuTime(threadMXBean.isThreadCpuTimeSupported() ? threadMXBean.getCurrentThreadCpuTime() : 0);
        threadStatics.setThreadUserTime(threadMXBean.isThreadCpuTimeSupported() ? threadMXBean.getCurrentThreadUserTime() : 0);
        return threadStatics;
    }
    // 获取内存池列表

    public List<Long> findDeadLock(MBeanServerConnection connection) throws IOException {
        ThreadMXBean threadMXBean = ManagementFactory.newPlatformMXBeanProxy(connection, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
        List<Long> deadLockedThreads = new ArrayList<>();
        try {
            deadLockedThreads = Arrays.stream(threadMXBean.findDeadlockedThreads()).boxed().collect(Collectors.toList());
        } catch (Exception e) {
            return deadLockedThreads;
        }
        return deadLockedThreads;
    }

    public BasicInfo getBasicInfo(MBeanServerConnection connection) throws Exception {
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setOperatingSystemInfo(getOperatingSystemInfo(connection));
        basicInfo.setJvmRuntimeInfo(getJvmRuntimeInfo(connection));
        basicInfo.setMemoryInfo(getMemoryInfo(connection));
        basicInfo.setClassLoaderInfo(getClassLoaderInfo(connection));
        basicInfo.setCompilationInfo(getCompilationInfo(connection));
        basicInfo.setCompilationInfo(getCompilationInfo(connection));
        return basicInfo;
    }

    public ClassLoaderInfo getClassLoaderInfo(MBeanServerConnection connection) throws IOException {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.newPlatformMXBeanProxy(connection, ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
        ClassLoaderInfo classLoaderInfo = new ClassLoaderInfo();
        classLoaderInfo.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        classLoaderInfo.setTotalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount());
        classLoaderInfo.setUnloadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        return classLoaderInfo;
    }

    public OperatingSystemInfo getOperatingSystemInfo(MBeanServerConnection connection) throws Exception {
        String canonicalName = ManagementFactory.getOperatingSystemMXBean().getObjectName().getCanonicalName();
        Map<String, Object> objectMap = extractMBeanAttributes(connection, canonicalName);
        Map<String, Object> resultMap = FastJmxUtils.changeMapKeyCase(objectMap);
        return (OperatingSystemInfo) FastJmxUtils.mapToObject(resultMap, OperatingSystemInfo.class);
    }

    public CompilationInfo getCompilationInfo(MBeanServerConnection connection) throws Exception {
        String canonicalName = ManagementFactory.getCompilationMXBean().getObjectName().getCanonicalName();
        Map<String, Object> objectMap = extractMBeanAttributes(connection, canonicalName);
        Map<String, Object> resultMap = FastJmxUtils.changeMapKeyCase(objectMap);
        return (CompilationInfo) FastJmxUtils.mapToObject(resultMap, CompilationInfo.class);
    }

    public MemoryInfo getMemoryInfo(MBeanServerConnection connection) throws Exception {
        String canonicalName = ManagementFactory.getMemoryMXBean().getObjectName().getCanonicalName();
        Map<String, Object> objectMap = extractMBeanAttributes(connection, canonicalName);
        Map<String, Object> resultMap = FastJmxUtils.changeMapKeyCase(objectMap);
        return (MemoryInfo) FastJmxUtils.mapToObject(resultMap, MemoryInfo.class);
    }

    public JvmRuntimeInfo getJvmRuntimeInfo(MBeanServerConnection connection) {
        JvmRuntimeInfo jvmRuntimeInfo = new JvmRuntimeInfo();
        try {
            // 获取运行时MXBean的规范名称，用于后续提取相关属性
            String runtimeMXBeanCanonicalName = ManagementFactory.getRuntimeMXBean().getObjectName().getCanonicalName();
            // 提取指定MBean的所有属性
            Map<String, Object> allObjectAttributes = extractMBeanAttributes(connection, runtimeMXBeanCanonicalName);
            // 获取系统属性对应的Map，如果不存在则返回空的Map，避免后续出现空指针异常
            Map<String, Object> systemProperties = allObjectAttributes.get("SystemProperties") instanceof Map ?
                    (Map<String, Object>) allObjectAttributes.get("SystemProperties") : new HashMap<>();

            Map<String, Object> cleanSystemPropertiesMap = systemProperties.values().stream()
                    .filter(obj -> obj instanceof Map)
                    .map(obj -> (Map<String, Object>) obj)
                    .collect(Collectors.toMap(
                            map -> map.get("key") instanceof String ? (String) map.get("key") : "unknown_key",
                            map -> map.get("value")
                    ));
            allObjectAttributes.remove("SystemProperties");
            jvmRuntimeInfo = (JvmRuntimeInfo) FastJmxUtils.mapToObject(
                    FastJmxUtils.changeMapKeyCase(allObjectAttributes), JvmRuntimeInfo.class);
            jvmRuntimeInfo.setSystemProperties(cleanSystemPropertiesMap);
        } catch (Exception e) {
            // 这里可以根据实际情况进行更合适的异常处理，比如记录日志等
            log.error("发生异常: {}", e.getMessage());
        }
        return jvmRuntimeInfo;
    }

}
