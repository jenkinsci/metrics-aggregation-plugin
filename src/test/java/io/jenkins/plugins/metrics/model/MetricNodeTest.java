package io.jenkins.plugins.metrics.model;

import org.junit.jupiter.api.Test;

public class MetricNodeTest {

    @Test
    public void shouldInsertTwoLevelPackage() {
        MetricsNode root = new MetricsNode();

        MetricsMeasurement measurement = new MetricsMeasurement();
        measurement.setPackageName("com.example");
        measurement.setClassName("Test.java");

        //root.insertMetricsMeasurement(measurement);

/*
        assertThat(root.getChildrenMap()).hasSize(1);

        MetricsNode firstLevel = root.getChildrenMap().get("com");
        assertThat(firstLevel).isNotNull();
        assertThat(firstLevel.getData()).isEqualTo(MetricsMeasurement.EMPTY_MEASUREMENT);
        assertThat(firstLevel.getChildrenMap()).hasSize(1);

        MetricsNode secondLevel = firstLevel.getChildrenMap().get("example");
        assertThat(secondLevel).isNotNull();
        assertThat(secondLevel.getData()).isEqualTo(MetricsMeasurement.EMPTY_MEASUREMENT);
        assertThat(secondLevel.getChildrenMap()).hasSize(1);

        assertThat(secondLevel.getChildrenMap().get("Test.java").getData()).isEqualTo(measurement);
        */
 
    }
}
