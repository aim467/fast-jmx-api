package com.fast.jmx.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class MBeanNode implements Serializable {


    public MBeanNode(String type, String name, String domain, String canonicalName) {
        this.type = type;
        this.name = name;
        this.domain = domain;
        this.canonicalName = canonicalName;
    }

    public MBeanNode() {
    }

    private String type;

    private String name;

    private String canonicalName;

    private String domain;

    private List<MBeanNode> nodes;

    // 添加子节点，并确保 nodes 非空
    public void addNode(MBeanNode node) {
        if (this.nodes == null) {
            this.nodes = new ArrayList<>();  // 在首次调用时初始化
        }
        this.nodes.add(node);
    }

//    public boolean hasSingleNode() {
//        return nodes != null && nodes.size() == 1;
//    }
//
//    public MBeanNode getSingleNode() {
//        return nodes.get(0);
//    }
}