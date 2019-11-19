package io.jenkins.plugins.metrics.model;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MetricsNode {

    @JsonIgnore
    private MetricsMeasurement data = new MetricsMeasurement();
    @JsonIgnore
    private Map<String, MetricsNode> childrenMap = new HashMap<>();
    private String name = "";

    public MetricsNode() {
    }

    public MetricsNode(final String name) {
        this(name, new MetricsMeasurement());
    }

    public MetricsNode(final MetricsMeasurement data) {
        this("", data);
    }

    public MetricsNode(final String name, final MetricsMeasurement data) {
        this(name, data, new HashMap<>());
    }

    public MetricsNode(final String name, final MetricsMeasurement data, final Map<String, MetricsNode> childrenMap) {
        this.data = data;
        this.childrenMap = childrenMap;
        this.name = name;
    }

    @JsonIgnore
    public MetricsMeasurement getData() {
        return data;
    }

    public void setData(final MetricsMeasurement data) {
        this.data = data;
    }

    @JsonIgnore
    public Map<String, MetricsNode> getChildrenMap() {
        return childrenMap;
    }

    @JsonIgnore
    public void setChildrenMap(final Map<String, MetricsNode> childrenMap) {
        this.childrenMap = childrenMap;
    }

    public Collection<MetricsNode> getChildren() {
        return childrenMap.values();
    }

    public int getValue() {
        if (data == null || data.equals(MetricsMeasurement.EMPTY_MEASUREMENT)) {
            return childrenMap.values().stream().map(MetricsNode::getValue).reduce(0, Integer::sum);
        }
        else {
            return data.getMetrics().getOrDefault("LOC", 0.0).intValue();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void collapsePackage() {
        while (getChildren().size() == 1) {
            MetricsNode singleChild = getChildrenMap().values().iterator().next();
            if (!name.isEmpty()) {
                setName(name + "." + singleChild.getName());
            }
            else {
                setName(singleChild.getName());
            }
            setChildrenMap(singleChild.getChildrenMap());
        }
    }

    public void insertNode(final MetricsNode node) {
        Deque<String> packageLevels = new ArrayDeque<>(Arrays.asList(node.getData().getPackageName().split("\\.")));
        insertNode(node, packageLevels);
    }

    public void insertNode(final MetricsNode node, final Deque<String> packageLevels) {
        if (packageLevels.isEmpty()) {
            String className = node.getData().getClassName() + ".java";
            node.setName(className);
            childrenMap.put(className, node);
        }
        else {
            String nextLevelPackageName = packageLevels.pop();
            node.setName(nextLevelPackageName);
            childrenMap.putIfAbsent(nextLevelPackageName, new MetricsNode(nextLevelPackageName));
            childrenMap.get(nextLevelPackageName).insertNode(node, packageLevels);
        }
    }
}
