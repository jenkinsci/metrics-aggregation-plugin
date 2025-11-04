package io.jenkins.plugins.metrics.model;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.metrics.model.measurement.MethodMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MethodMetricsMeasurement.MethodMetricsMeasurementBuilder;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.MetricDefinitionBuilder;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for the class {@link MethodMetricsMeasurement}.
 */
class MethodMetricsMeasurementTest {
    /**
     * Test if two metrics are merged correctly.
     */
    @Test
    void shouldMergeTwoMetricsMeasurements() {
        var measurement2 = new MethodMetricsMeasurementBuilder();
        var metric = createIntMetric("TESTMETRIC", 17);
        measurement2.withMetric(metric);

        var measurement1 = new MethodMetricsMeasurementBuilder().build();

        assertThat(measurement1.getMetrics()).isEmpty();
        measurement1.merge(measurement2.build());
        assertThat(measurement1.getMetrics()).containsValue(metric);
    }

    /**
     * Test if it is possible to retrieve a metric correctly.
     */
    @Test
    void shouldGetMetric() {
        var metricId = "TESTMETRIC";
        int metricValue = 17;
        var metric = createIntMetric(metricId, metricValue);
        var measurement = new MethodMetricsMeasurementBuilder().withMetric(metric).build();

        assertThat(measurement.getMetric(metricId)).isNotEmpty();
        assertThat(measurement.getMetric(metricId)).hasValue(metricValue);
    }

    private Metric createIntMetric(final String id, final int value) {
        return new IntegerMetric(new MetricDefinitionBuilder(id)
                .withDisplayName("")
                .withDescription("")
                .withReportedBy("")
                .withPriority(0)
                .withScopes(ArrayUtils.toArray(Scope.CLASS, Scope.METHOD))
                .build(), value);
    }
}
