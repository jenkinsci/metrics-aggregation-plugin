package io.jenkins.plugins.metrics.model;

import java.util.OptionalDouble;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class MetricNodeTest {

    @Test
    public void shouldInsertTwoLevelPackage() {
        MetricsTreeNode root = new MetricsTreeNode(OptionalDouble.empty(), "");

        MetricsTreeNode myClass = new MetricsTreeNode(OptionalDouble.empty(), "com.example.MyClass");
        MetricsTreeNode otherClass = new MetricsTreeNode(OptionalDouble.empty(), "com.example.other.OtherClass");

        root.insertNode(myClass);
        root.insertNode(otherClass);

        assertThat(root.getChildren()).hasSize(1);
        MetricsTreeNode child = root.getChildren().get(0);
        MetricsTreeNodeAssert.assertThat(child).hasName("com");

        assertThat(child.getChildren()).hasSize(1);
        child = child.getChildren().get(0);
        MetricsTreeNodeAssert.assertThat(child).hasName("example");

        assertThat(child.getChildren()).hasSize(2);
        MetricsTreeNodeAssert.assertThat(child.getChildren().get(0)).hasName("MyClass");

        child = child.getChildren().get(1);
        MetricsTreeNodeAssert.assertThat(child).hasName("other");
        assertThat(child.getChildren()).hasSize(1);
        MetricsTreeNodeAssert.assertThat(child.getChildren().get(0)).hasName("OtherClass");
    }

    @Test
    public void shouldGetSpecificMetricValue() {
        final double metricValue = 42;

        MetricsTreeNode node = new MetricsTreeNode(OptionalDouble.of(metricValue), "node");
        assertThat(node.getValue()).isEqualTo(42);
    }

    @Test
    public void shouldSumUpChildrenValues() {
        final double metricValue1 = 42;
        final double metricValue2 = 5;

        MetricsTreeNode node1 = new MetricsTreeNode(OptionalDouble.of(metricValue1), "node1");
        MetricsTreeNode node2 = new MetricsTreeNode(OptionalDouble.of(metricValue2), "node2");

        MetricsTreeNode root = new MetricsTreeNode(OptionalDouble.empty(), "");
        root.insertNode(node1);
        root.insertNode(node2);

        assertThat(root.getValue()).isEqualTo(metricValue1 + metricValue2);
    }

    @Test
    public void shouldCollapsePackage() {
        MetricsTreeNode rootNode = threeLevelTree();
        rootNode.collapsePackage();

        assertThat(rootNode.getName()).isEqualTo("levelOneNode.levelTwoNode");
        assertThat(rootNode.getChildren()).hasSize(2);
    }

    private MetricsTreeNode threeLevelTree() {
        MetricsTreeNode leafNode2 = new MetricsTreeNode(OptionalDouble.empty(), "leafNode1");
        MetricsTreeNode leafNode1 = new MetricsTreeNode(OptionalDouble.empty(), "leafNode2");

        MetricsTreeNode levelTwoNode = new MetricsTreeNode(OptionalDouble.empty(), "levelTwoNode");
        levelTwoNode.insertNode(leafNode1);
        levelTwoNode.insertNode(leafNode2);

        MetricsTreeNode levelOneNode = new MetricsTreeNode(OptionalDouble.empty(), "levelOneNode");
        levelOneNode.insertNode(levelTwoNode);

        MetricsTreeNode rootNode = new MetricsTreeNode(OptionalDouble.empty(), "");
        rootNode.insertNode(levelOneNode);

        return rootNode;
    }
}
