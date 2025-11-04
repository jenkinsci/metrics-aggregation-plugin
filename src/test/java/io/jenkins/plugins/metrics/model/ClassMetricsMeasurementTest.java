package io.jenkins.plugins.metrics.model;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for the class {@link ClassMetricsMeasurement}.
 */
class ClassMetricsMeasurementTest {
    @Test
    void shouldGetMetric() {
        var metricId = "TESTMETRIC";
        int metricValue = 17;
        var metric = createIntMetric(metricId, metricValue);

        var measurement = new ClassMetricsMeasurement();
        measurement.addMetric(metric);

        assertThat(measurement.getMetric(metricId)).isNotEmpty();
        assertThat(measurement.getMetric(metricId)).hasValue(metricValue);
    }

    private Metric<Integer> createIntMetric(final String id, final int value) {
        var definition = new MetricDefinition(id, id, id, id, 0, Scope.CLASS, Scope.METHOD);

        return new IntegerMetric(definition, value);
    }
}
