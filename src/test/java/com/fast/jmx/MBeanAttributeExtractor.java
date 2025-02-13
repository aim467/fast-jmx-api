package com.fast.jmx;

import com.alibaba.fastjson2.JSON;
import com.fast.jmx.domain.MBeanNode;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.reflect.Array;
import java.util.*;

@Slf4j
public class MBeanAttributeExtractor {

    public static void main(String[] args) {
        try {
            String pid = "9400";
            VirtualMachine attach = VirtualMachine.attach(pid);
            String address = attach.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            System.out.println("connect address is: " + address);
            if (address == null) {
                address = attach.startLocalManagementAgent();
            }
            JMXServiceURL jmxServiceURL = new JMXServiceURL(address);

            // 假设通过 JMX Connector 获取 MBeanServerConnection
            // 这里的连接代码需要根据具体的环境修改，比如远程连接
            MBeanServerConnection connection = JMXConnectorFactory.connect(jmxServiceURL).getMBeanServerConnection();
            // 指定的 domain
            String domain = "java.lang";
            // 提取 MBean 树
//            MBeanNode mbeanTree = extractMBeanTree(connection, domain);
//            List<MBeanNode> nodes = mbeanTree.getNodes();
            Map<String, Object> objectMap = extractMBeanAttributes(connection, "java.lang:type=Runtime");
            System.out.println(JSON.toJSONString(objectMap));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * 提取指定 domain 下的所有 MBean，并构建 MBeanNode 树
     *
     * @param connection MBeanServerConnection，用于访问 MBean
     * @param domain     指定的 domain 名称
     * @return MBeanNode 树的根节点
     * @throws Exception 如果发生错误
     */
    public static MBeanNode extractMBeanTree(MBeanServerConnection connection, String domain) throws Exception {
        MBeanNode root = new MBeanNode();
        root.setDomain(domain);
        root.setType("root");
        root.setName(domain);

        // 获取指定 domain 下的所有 MBean 名称
        Set<ObjectName> mbeans = connection.queryNames(new ObjectName(domain + ":*"), null);

        // 临时存储所有 MBean 的节点，按 type 分类
        Map<String, MBeanNode> typeMap = new HashMap<>();

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
                name = mbeanName.getCanonicalName();
            }

            // 构建 MBeanNode 节点
            MBeanNode node = new MBeanNode(type, name, domain, canonicalName);

            // 如果该 type 没有节点，创建一个父节点
            if (!typeMap.containsKey(type)) {
                typeMap.put(type, node);  // 初始化时直接将第一个节点设置为该类型的代表
            } else {
                MBeanNode typeNode = typeMap.get(type);

                // 如果已存在同类节点，初始化其 nodes 并处理为多子节点
                if (typeNode.getNodes() == null) {
                    typeNode.addNode(new MBeanNode(typeNode.getType(), typeNode.getName(), typeNode.getDomain(), canonicalName));
                    typeNode.setName("");  // 清空父节点的 name，因为它代表多个子节点
                }
                // 重置type和canonicalName，使其作为父节点
                typeNode.setName(typeNode.getType());
                typeNode.setCanonicalName(null);
                typeNode.addNode(node);
            }
        }

        // 遍历 typeMap，将所有类型节点添加到根节点
        for (MBeanNode typeNode : typeMap.values()) {
            List<MBeanNode> nodes = typeNode.getNodes();
            if (nodes != null && nodes.size() == 1) {
                root.addNode(typeNode.getNodes().get(0));
            }
            {
                // 否则，将该类型的父节点添加到根节点
                root.addNode(typeNode);
            }

//            if (typeNode.hasSingleNode()) {
//                // 如果该类型只有一个节点，直接将其作为根节点的子节点
//                root.addNode(typeNode.getSingleNode());
//            } else
        }
        return root;
    }


    /**
     * 提取指定 MBean 的所有属性名和属性值，并以 Map 形式返回
     *
     * @param connection MBeanServerConnection，用于访问 MBean
     * @param mbeanName  指定的 MBean 名称
     * @return 包含属性名和属性值的 Map
     * @throws Exception 如果发生错误
     */
    public static Map<String, Object> extractMBeanAttributes(MBeanServerConnection connection, String mbeanName) throws Exception {
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
                } else {
                    // 处理其他类型的数据
                    attributesMap.put(attributeName, attributeValue);
                }

            } catch (Exception e) {
                // 处理属性值提取错误，可能某些属性不可读
                System.err.println("无法读取属性: " + attributeName + " 错误: " + e.getMessage());
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
    public static Map<String, Object> extractFromTabularData(TabularData tabularData) {
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

    public static Map<String, Object> extractFromCompositeData(CompositeData compositeData) {
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


//    public static void main(String[] args) {
//        try {
//            String pid = "26692";
//            VirtualMachine attach = VirtualMachine.attach(pid);
//            String address = ConnectorAddressLink.importFrom(Integer.valueOf(pid));
//            System.out.println("connect address is: " + address);
//            if (address == null) {
//                address = attach.startLocalManagementAgent();
//            }
//            JMXServiceURL jmxServiceURL = new JMXServiceURL(address);
//
//            // 假设通过 JMX Connector 获取 MBeanServerConnection
//            // 这里的连接代码需要根据具体的环境修改，比如远程连接
//            MBeanServerConnection connection = JMXConnectorFactory.connect(jmxServiceURL).getMBeanServerConnection();
//            // 指定 MBean 的名称（可以从 JMX 控制台查看 MBean 名称）
//            String mbeanName = "java.lang:type=Memory";
//
//            // 提取属性
//            Map<String, Object> attributes = extractMBeanAttributes(connection, mbeanName);
//
//            System.out.println(attributes.size());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}