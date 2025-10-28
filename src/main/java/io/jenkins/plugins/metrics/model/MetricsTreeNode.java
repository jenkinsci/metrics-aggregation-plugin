package io.jenkins.plugins.metrics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Node for constructing a tree structure of all metrics.
 */
public class MetricsTreeNode {

    private String name;
    private double value;

    @JsonIgnore
    private Map<String, MetricsTreeNode> childrenMap = new HashMap<>();

    /**
     * Create a new {@link MetricsTreeNode} with value 0.0.
     *
     * @param name
     *         the name of the node
     */
    public MetricsTreeNode(final String name) {
        this(name, 0.0);
    }

    /**
     * Create a new {@link MetricsTreeNode}.
     *
     * @param name
     *         the name of the node
     * @param value
     *         the value of the node
     */
    public MetricsTreeNode(final String name, final double value) {
        this.value = value;
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(final double value) {
        this.value = value;
    }

    /**
     * Add to the current value of this node.
     *
     * @param amount
     *         the amount to add
     */
    private void addValue(final double amount) {
        this.value += amount;
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

    /**
     * Collapse the package names. If a node only has one child, its name is appended to the current node and its
     * children are now the children of the current node. This is repeated as long as there are nodes with only one
     * child (package nodes at the top of the hierarchy).
     */
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

    /**
     * Insert a node in the tree.
     *
     * @param node
     *         the node to insert
     */
    public void insertNode(final MetricsTreeNode node) {
        Deque<String> packageLevels = new ArrayDeque<>(Arrays.asList(node.getName().split("\\.")));
        insertNode(node, packageLevels);
    }

    private void insertNode(final MetricsTreeNode node, final Deque<String> levels) {
        String nextLevelName = levels.pop();

        addValue(node.getValue());
        if (levels.isEmpty()) {
            node.setName(nextLevelName);
            childrenMap.put(nextLevelName, node);
        }
        else {
            childrenMap.putIfAbsent(nextLevelName, new MetricsTreeNode(nextLevelName));
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

        if (o instanceof MetricsTreeNode other) {
            return Objects.equals(name, other.name)
                    && Objects.equals(value, other.value)
                    && Objects.equals(childrenMap, other.childrenMap);
        }

        return false;
    }
}
