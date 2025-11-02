package io.jenkins.plugins.metrics.model;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.metrics.model.measurement.MethodMetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for the class {@link MethodMetricsMeasurement}.
 */
public class MethodMetricsMeasurementTest {
    /**
     * Test if two metrics are merged correctly.
     */
    @Test
    public void shouldMergeTwoMetricsMeasurements() {
        var measurement2 = new MethodMetricsMeasurement();
        var metric = createIntMetric("TESTMETRIC", 17);
        measurement2.addMetric(metric);

        var measurement1 = new MethodMetricsMeasurement();

        assertThat(measurement1.getMetrics()).isEmpty();
        measurement1.merge(measurement2);
        assertThat(measurement1.getMetrics()).containsValue(metric);
    }

    /**
     * Test if it is possible to retrieve a metric correctly.
     */
    @Test
    public void shouldGetMetric() {
        var metricId = "TESTMETRIC";
        int metricValue = 17;
        var metric = createIntMetric(metricId, metricValue);
        var measurement = new MethodMetricsMeasurement();
        measurement.addMetric(metric);

        assertThat(measurement.getMetric(metricId)).isNotEmpty();
        assertThat(measurement.getMetric(metricId)).hasValue(metricValue);
    }

    private Metric createIntMetric(final String id, final int value) {
        return new IntegerMetric(new MetricDefinition(id, "",
                "", "", 0, ArrayUtils.toArray(Scope.CLASS, Scope.METHOD)), value);
    }
}
