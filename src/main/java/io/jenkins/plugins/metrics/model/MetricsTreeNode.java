package io.jenkins.plugins.metrics.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MetricsTreeNode {

    private String name;
    @JsonIgnore
    private final OptionalDouble value;

    @JsonIgnore
    private Map<String, MetricsTreeNode> childrenMap = new HashMap<>();

    public MetricsTreeNode(final OptionalDouble value, final String name) {
        this.value = value;
        this.name = name;
    }

    public double getValue() {
        return value.orElseGet(() ->
                getChildren().stream()
                        .map(MetricsTreeNode::getValue)
                        .reduce(0.0, Double::sum)
        );
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @JsonIgnore
    public Map<String, MetricsTreeNode> getChildrenMap() {
        return childrenMap;
    }

    public List<MetricsTreeNode> getChildren() {
        return new ArrayList<>(childrenMap.values());
    }

    public void collapsePackage() {
        while (getChildren().size() == 1) {
            MetricsTreeNode singleChild = getChildrenMap().values().iterator().next();
            if (!name.isEmpty()) {
                setName(name + "." + singleChild.getName());
            }
            else {
                setName(singleChild.getName());
            }
            childrenMap = singleChild.getChildrenMap();
        }
    }

    public void insertNode(final MetricsTreeNode node) {
        Deque<String> packageLevels = new ArrayDeque<>(Arrays.asList(node.getName().split("\\.")));
        insertNode(node, packageLevels);
    }

    public void insertNode(final MetricsTreeNode node, final Deque<String> levels) {
        String nextLevelName = levels.pop();

        if (levels.isEmpty()) {
            System.out.printf("insert last %s \n", node);
            node.setName(nextLevelName);
            childrenMap.put(nextLevelName, node);
        }
        else {
            System.out.printf("insert %s at %s \n", node, levels);
            childrenMap.putIfAbsent(nextLevelName, new MetricsTreeNode(OptionalDouble.empty(), nextLevelName));
            childrenMap.get(nextLevelName).insertNode(node, levels);
        }
    }

    @Override
    public String toString() {
        return String.format("MetricsTreeNode '%s' (%s)", name, value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, childrenMap, name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof MetricsTreeNode) {
            MetricsTreeNode other = (MetricsTreeNode) o;
            return Objects.equals(name, other.name) &&
                    Objects.equals(value, other.value) &&
                    Objects.equals(childrenMap, other.childrenMap);
        }

        return false;
    }
}
