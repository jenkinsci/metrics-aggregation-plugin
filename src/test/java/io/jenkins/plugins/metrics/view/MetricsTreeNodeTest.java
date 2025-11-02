package io.jenkins.plugins.metrics.view;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for the class {@link MetricsTreeNode}.
 */
public class MetricsTreeNodeTest {
    /**
     * Test if packages with two identical package levels are inserted correctly.
     */
    @Test
    public void shouldInsertTwoLevelPackage() {
        var root = new MetricsTreeNode("");

        var myClass = new MetricsTreeNode("com.example.MyClass");
        var otherClass = new MetricsTreeNode("com.example.other.OtherClass");

        root.insertNode(myClass);
        root.insertNode(otherClass);

        assertThat(root.getChildren()).hasSize(1);
        var child = root.getChildren().get(0);
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

    /**
     * Test if the value of the metric is kept correctly.
     */
    @Test
    public void shouldGetSpecificMetricValue() {
        final double metricValue = 42;

        var node = new MetricsTreeNode("node", metricValue);
        assertThat(node.getValue()).isEqualTo(42);
    }

    /**
     * Test if all children values are summed up correctly.
     */
    @Test
    public void shouldSumUpChildrenValues() {
        final double metricValue1 = 42;
        final double metricValue2 = 47;
        final double metricValue3 = 11;

        var root = new MetricsTreeNode("");
        root.insertNode(new MetricsTreeNode("test.node1", metricValue1));
        root.insertNode(new MetricsTreeNode("test.node2", metricValue2));
        root.insertNode(new MetricsTreeNode("node3", metricValue3));

        assertThat(root.getValue()).isEqualTo(metricValue1 + metricValue2 + metricValue3);
    }

    /**
     * Test if the package is collapsed correctly.
     */
    @Test
    public void shouldCollapsePackage() {
        var rootNode = threeLevelTree();
        rootNode.collapsePackage();

        assertThat(rootNode.getName()).isEqualTo("levelOneNode.levelTwoNode");
        assertThat(rootNode.getChildren()).hasSize(2);
    }

    private MetricsTreeNode threeLevelTree() {
        var leafNode2 = new MetricsTreeNode("leafNode1");
        var leafNode1 = new MetricsTreeNode("leafNode2");
        var levelTwoNode = new MetricsTreeNode("levelTwoNode");
        levelTwoNode.insertNode(leafNode1);
        levelTwoNode.insertNode(leafNode2);

        var levelOneNode = new MetricsTreeNode("levelOneNode");
        levelOneNode.insertNode(levelTwoNode);

        var rootNode = new MetricsTreeNode("");
        rootNode.insertNode(levelOneNode);

        return rootNode;
    }

    /**
     * Test the equals and hash functions.
     */
    @Test
    public void shouldBeEqualAndHash() {
        var name = "name";
        double one = 1.0;
        var node = new MetricsTreeNode(name);

        assertThat(node).isNotEqualTo(new MetricsTreeNode(name, one));
        assertThat(node).isEqualTo(node);
        assertThat(node).isEqualTo(new MetricsTreeNode(name));

        node.insertNode(new MetricsTreeNode("name1"));
        assertThat(node).isNotEqualTo(new MetricsTreeNode(name));

        assertThat(node).isNotEqualTo("test");

        assertThat(node.hashCode()).isEqualTo(node.hashCode());

        assertThat(node.hashCode()).isNotEqualTo(new MetricsTreeNode(name).hashCode());
    }

    /**
     * Test if the JSON serialisation is correct.
     */
    @Test
    public void shouldContainRelevantInformationInJson() {
        var facade = new JacksonFacade();
        var root = new MetricsTreeNode("");
        root.insertNode(new MetricsTreeNode("com.example.Bar", 5.0));
        root.insertNode(new MetricsTreeNode("com.example.package.Foo", 2.0));
        root.collapsePackage();

        assertThat(facade.toJson(root)).isEqualTo("{\"name\":\"com.example\",\"value\":7.0,\"children\":["
                + "{\"name\":\"Bar\",\"value\":5.0,\"children\":[]},"
                + "{\"name\":\"package\",\"value\":2.0,\"children\":["
                + "{\"name\":\"Foo\",\"value\":2.0,\"children\":[]}"
                + "]}]}");
    }
}
